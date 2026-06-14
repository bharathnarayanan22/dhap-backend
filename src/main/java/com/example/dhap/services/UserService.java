package com.example.dhap.services;

import com.example.dhap.dto.user.UpdateRoleRequest;
import com.example.dhap.dto.user.UpdateSubmissionStatusRequest;
import com.example.dhap.dto.user.UpdateUserRequest;
import com.example.dhap.dto.user.UserResponse;
import com.example.dhap.entities.User;
import com.example.dhap.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── Queries ────────────────────────────────────────────────────────────

    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    public UserResponse getById(String id) {
        return UserResponse.from(findOrThrow(id));
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    /**
     * PATCH /users/{id} — partial profile update.
     * Only non-null fields in the request body are applied.
     */
    public UserResponse updateProfile(String id, UpdateUserRequest req) {
        User user = findOrThrow(id);
        if (req.name        != null) user.setName(req.name);
        if (req.mobile      != null) user.setMobile(req.mobile);
        if (req.addressLine != null) user.setAddressLine(req.addressLine);
        if (req.city        != null) user.setCity(req.city);
        if (req.country     != null) user.setCountry(req.country);
        if (req.pincode     != null) user.setPincode(req.pincode);
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * PATCH /users/{id}/role — switch between DONOR and VOLUNTEER.
     * COORDINATOR is only assigned via application acceptance, not here.
     */
    public UserResponse updateRole(String id, UpdateRoleRequest req) {
        User user = findOrThrow(id);
        user.setRole(req.role.name());
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * PATCH /users/{id}/submission-status — mark coordinator application submitted.
     * Called alongside POST /applications so the profile reflects pending status.
     */
    public UserResponse updateSubmissionStatus(String id, UpdateSubmissionStatusRequest req) {
        User user = findOrThrow(id);
        user.setIsSubmitted(req.isSubmitted);
        return UserResponse.from(userRepository.save(user));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Returns the currently authenticated user from the security context.
     * The principal name is the user's email (set by CustomUserDetailsService).
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    public User findOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }
}