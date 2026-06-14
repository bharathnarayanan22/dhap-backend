package com.example.dhap.services;

import com.example.dhap.dto.response.CreateResponseRequest;
import com.example.dhap.dto.response.ResponseResponse;
import com.example.dhap.entities.Request;
import com.example.dhap.entities.Response;
import com.example.dhap.entities.User;
import com.example.dhap.repositories.ResponseRepository;
import com.example.dhap.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final RequestService     requestService;
    private final UserRepository     userRepository;

    public ResponseService(ResponseRepository responseRepository,
                           RequestService requestService,
                           UserRepository userRepository) {
        this.responseRepository = responseRepository;
        this.requestService     = requestService;
        this.userRepository     = userRepository;
    }

    // ── GET /responses?page=&size= ────────────────────────────────────────

    public Page<ResponseResponse> getAll(Pageable pageable) {
        return responseRepository.findAll(pageable).map(ResponseResponse::from);
    }

    // ── GET /requests/{requestId}/responses ───────────────────────────────

    public Page<ResponseResponse> getForRequest(String requestId, Pageable pageable) {
        return responseRepository.findByRequestId(requestId, pageable)
                .map(ResponseResponse::from);
    }

    // ── POST /responses ───────────────────────────────────────────────────

    /**
     * Donor pledges against a resource request.
     *
     * One @Transactional block (MongoConfig enables Atlas multi-doc transactions):
     *   1. Resolve donor from JWT
     *   2. Find the target request
     *   3. Save the response document
     *   4. Recompute quantityPledged on the request
     *   5. Auto-accept the request when total pledged >= quantity needed
     */
    @Transactional
    public ResponseResponse create(CreateResponseRequest req) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User donor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Request request = requestService.findOrThrow(req.requestId);

        Response response = new Response();
        response.setRequestId(req.requestId);
        response.setResponderId(donor.getId());
        response.setResponderName(donor.getName());
        response.setMessage(req.message);
        response.setQuantityProvided(req.quantityProvided);
        response.setAddress(req.address);
        if (req.location != null) response.setLocation(req.location.toEntity());
        response = responseRepository.save(response);

        // Recompute pledged total and auto-accept when the need is filled
        int newTotal = request.getQuantityPledged() + req.quantityProvided;
        request.setQuantityPledged(newTotal);
        if (newTotal >= request.getQuantity()) {
            request.setStatus("ACCEPTED");
        }
        requestService.save(request);

        return ResponseResponse.from(response);
    }

    // ── PATCH /responses/{id}/assign-task ─────────────────────────────────

    /**
     * Coordinator marks a pledge as converted to a delivery task.
     * Sets taskAssigned = true. Returns the updated response.
     */
    public ResponseResponse assignTask(String id) {
        Response response = responseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Response not found"));
        response.setTaskAssigned(true);
        return ResponseResponse.from(responseRepository.save(response));
    }
}