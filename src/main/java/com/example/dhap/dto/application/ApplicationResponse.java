package com.example.dhap.dto.application;

import com.example.dhap.entities.CoordinatorApplication;
import java.time.LocalDateTime;

/**
 * Returned by all /applications endpoints.
 *
 * submittedAt serialises as an ISO-8601 string — the default in
 * Jackson 3 (Spring Boot 4); no configuration needed.
 */
public class ApplicationResponse {

    public String        id;
    public String        userId;
    public String        email;
    public String        message;
    public String        status;         // "PENDING" | "ACCEPTED" | "REJECTED"
    public LocalDateTime submittedAt;

    public ApplicationResponse() {}

    public static ApplicationResponse from(CoordinatorApplication app) {
        ApplicationResponse dto = new ApplicationResponse();
        dto.id          = app.getId();
        dto.userId      = app.getUserId();
        dto.email       = app.getUserEmail();
        dto.message     = app.getMessage();
        dto.status      = app.getStatus();
        dto.submittedAt = app.getSubmittedAt();
        return dto;
    }
}