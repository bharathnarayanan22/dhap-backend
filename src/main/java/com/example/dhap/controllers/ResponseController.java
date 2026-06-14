package com.example.dhap.controllers;

import com.example.dhap.dto.response.CreateResponseRequest;
import com.example.dhap.dto.response.ResponseResponse;
import com.example.dhap.services.ResponseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/responses")
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    /**
     * GET /responses?page=0&size=20
     * All donor pledges — coordinator overview.
     */
    @GetMapping
    public ResponseEntity<Page<ResponseResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(responseService.getAll(pageable));
    }

    /**
     * POST /responses — donor pledges against a resource request.
     * Body: { requestId, message, quantityProvided, address, location }
     * Responder identity resolved from JWT.
     * Returns 201. Auto-accepts the parent request when total pledged >= needed.
     */
    @PostMapping
    public ResponseEntity<ResponseResponse> create(
            @Valid @RequestBody CreateResponseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseService.create(req));
    }

    /**
     * PATCH /responses/{id}/assign-task
     * Coordinator marks a pledge as converted to a delivery task.
     * No request body. Returns the updated response with taskAssigned: true.
     */
    @PatchMapping("/{id}/assign-task")
    public ResponseEntity<ResponseResponse> assignTask(@PathVariable String id) {
        return ResponseEntity.ok(responseService.assignTask(id));
    }
}