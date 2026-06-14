package com.example.dhap.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /auth/refresh.
 * Matches the refreshToken issued by /auth/login and /auth/register.
 */
public class RefreshRequest {
    @NotBlank(message = "refreshToken is required")
    public String refreshToken;
}