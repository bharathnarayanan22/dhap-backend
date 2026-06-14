package com.example.dhap.controllers;

import com.example.dhap.dto.user.*;
import com.example.dhap.services.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /users?page=0&size=20
     * Returns paginated list of all users.
     * Coordinator-facing: used to browse volunteer/donor profiles.
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    /**
     * GET /users/{id}
     * Returns a single user profile by UUID string.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /**
     * PATCH /users/{id}
     * Partial profile update (name, mobile, address fields).
     * All body fields are optional — only non-null values are applied.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String id,
            @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateProfile(id, req));
    }

    /**
     * PATCH /users/{id}/role
     * Switch role between DONOR and VOLUNTEER.
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest req) {
        return ResponseEntity.ok(userService.updateRole(id, req));
    }

    /**
     * PATCH /users/{id}/submission-status
     * Mark that the user has submitted a coordinator application.
     * Body: { "isSubmitted": true }
     */
    @PatchMapping("/{id}/submission-status")
    public ResponseEntity<UserResponse> updateSubmissionStatus(
            @PathVariable String id,
            @RequestBody UpdateSubmissionStatusRequest req) {
        return ResponseEntity.ok(userService.updateSubmissionStatus(id, req));
    }
}