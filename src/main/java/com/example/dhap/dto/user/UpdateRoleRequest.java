package com.example.dhap.dto.user;

import com.example.dhap.enums.Role;
import jakarta.validation.constraints.NotNull;

/** Body for PATCH /users/{id}/role */
public class UpdateRoleRequest {
    @NotNull(message = "role is required")
    public Role role;    // DONOR | VOLUNTEER  (only user-switchable roles)
}