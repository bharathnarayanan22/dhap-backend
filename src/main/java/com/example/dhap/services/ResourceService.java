package com.example.dhap.services;

import com.example.dhap.dto.resource.CreateResourceRequest;
import com.example.dhap.dto.resource.ResourceResponse;
import com.example.dhap.entities.Resource;
import com.example.dhap.entities.User;
import com.example.dhap.repositories.ResourceRepository;
import com.example.dhap.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository     userRepository;

    public ResourceService(ResourceRepository resourceRepository,
                           UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository     = userRepository;
    }

    // ── GET /resources?page=&size=[&donorId=] ─────────────────────────────

    /**
     * No donorId  → all donated resources (coordinator / volunteer view).
     * With donorId → that donor's contributions only ("My Contributions" in Flutter).
     */
    public Page<ResourceResponse> getAll(Pageable pageable, String donorId) {
        if (donorId != null) {
            return resourceRepository.findByDonorId(donorId, pageable)
                    .map(ResourceResponse::from);
        }
        return resourceRepository.findAll(pageable).map(ResourceResponse::from);
    }

    // ── POST /resources ───────────────────────────────────────────────────

    /**
     * Donor identity comes from the JWT — never from the request body.
     * Stores donorId + donorName as a snapshot so response reads need no join.
     */
    public ResourceResponse create(CreateResourceRequest req) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User donor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Resource resource = new Resource();
        resource.setResource(req.resource);
        resource.setQuantity(req.quantity);
        resource.setAddress(req.address);
        if (req.location != null) resource.setLocation(req.location.toEntity());
        resource.setDonorId(donor.getId());
        resource.setDonorName(donor.getName());
        resource.setResourceType(req.resourceType != null ? req.resourceType.name() : null);

        return ResourceResponse.from(resourceRepository.save(resource));
    }

    // ── DELETE /resources/{id} ────────────────────────────────────────────

    public void delete(String id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        resourceRepository.deleteById(id);
    }
}