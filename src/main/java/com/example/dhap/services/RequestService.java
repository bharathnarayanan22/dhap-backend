package com.example.dhap.services;

import com.example.dhap.dto.request.CreateRequestRequest;
import com.example.dhap.dto.request.RequestResponse;
import com.example.dhap.entities.Request;
import com.example.dhap.repositories.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // ── GET /requests?page=&size= ─────────────────────────────────────────

    public Page<RequestResponse> getAll(Pageable pageable) {
        return requestRepository.findAll(pageable).map(RequestResponse::from);
    }

    // ── POST /requests ────────────────────────────────────────────────────

    public RequestResponse create(CreateRequestRequest req) {
        Request request = new Request();
        request.setResource(req.resource);
        request.setQuantity(req.quantity);
        request.setDescription(req.description);
        request.setAddress(req.address);
        if (req.location != null) request.setLocation(req.location.toEntity());
        return RequestResponse.from(requestRepository.save(request));
    }

    // ── Internal helper used by ResponseService ───────────────────────────

    public Request findOrThrow(String id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }
}