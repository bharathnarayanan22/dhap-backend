package com.example.dhap.controllers;

import com.example.dhap.dto.application.ApplicationResponse;
import com.example.dhap.dto.application.CreateApplicationRequest;
import com.example.dhap.enums.ApplicationStatus;
import com.example.dhap.services.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * GET /applications?page=0&size=20[&status=PENDING]
     *
     * Coordinator review queue: GET /applications?status=PENDING
     * Full history:             GET /applications
     */
    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.getAll(pageable, status));
    }

    /**
     * POST /applications
     * Authenticated user applies to become a coordinator.
     * Body: { "message": "..." }
     * Side effect: user.isSubmitted = true (same transaction).
     * Returns 201 with the created application.
     */
    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
            @Valid @RequestBody CreateApplicationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.create(req));
    }

    /**
     * PATCH /applications/{id}/accept
     * Approve the application and promote the user to COORDINATOR atomically.
     * Returns 200 with updated application (status: "ACCEPTED").
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApplicationResponse> accept(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.accept(id));
    }

    /**
     * PATCH /applications/{id}/reject
     * Reject the application.
     * Returns 200 with updated application (status: "REJECTED").
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponse> reject(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.reject(id));
    }
}