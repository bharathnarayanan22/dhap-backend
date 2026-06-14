package com.example.dhap.controllers;

import com.example.dhap.dto.resource.CreateResourceRequest;
import com.example.dhap.dto.resource.ResourceResponse;
import com.example.dhap.services.ResourceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * GET /resources?page=0&size=20[&donorId=uuid]
     *
     * All donors:    GET /resources              → coordinator / volunteer overview
     * One donor:     GET /resources?donorId={id} → "My Contributions" (donor dashboard)
     */
    @GetMapping
    public ResponseEntity<Page<ResourceResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String donorId) {
        return ResponseEntity.ok(resourceService.getAll(pageable, donorId));
    }

    /**
     * POST /resources
     * Donor is the authenticated user — no donorId in the request body.
     * Returns 201 with the created resource.
     */
    @PostMapping
    public ResponseEntity<ResourceResponse> create(
            @Valid @RequestBody CreateResourceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.create(req));
    }

    /**
     * DELETE /resources/{id}
     * Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}