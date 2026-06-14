package com.example.dhap.dto.application;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for POST /applications.
 * Applicant identity (userId, userEmail) is resolved from the JWT — not sent by the client.
 */
public class CreateApplicationRequest {

    @NotBlank(message = "message is required")
    public String message;
}