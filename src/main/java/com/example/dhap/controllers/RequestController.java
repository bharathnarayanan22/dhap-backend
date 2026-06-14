package com.example.dhap.controllers;

import com.example.dhap.dto.request.CreateRequestRequest;
import com.example.dhap.dto.request.RequestResponse;
import com.example.dhap.dto.response.ResponseResponse;
import com.example.dhap.services.RequestService;
import com.example.dhap.services.ResponseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService  requestService;
    private final ResponseService responseService;

    public RequestController(RequestService requestService,
                             ResponseService responseService) {
        this.requestService  = requestService;
        this.responseService = responseService;
    }

    /**
     * GET /requests?page=0&size=20
     * All published resource needs — donors and coordinators see this.
     */
    @GetMapping
    public ResponseEntity<Page<RequestResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(requestService.getAll(pageable));
    }

    /**
     * POST /requests — coordinator publishes a resource need.
     * Returns 201 with the created request.
     */
    @PostMapping
    public ResponseEntity<RequestResponse> create(
            @Valid @RequestBody CreateRequestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.create(req));
    }

    /**
     * GET /requests/{requestId}/responses?page=0&size=20
     * All donor pledges for a specific request.
     * Nested under /requests to reflect the parent-child relationship.
     */
    @GetMapping("/{requestId}/responses")
    public ResponseEntity<Page<ResponseResponse>> getResponses(
            @PathVariable String requestId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(responseService.getForRequest(requestId, pageable));
    }
}