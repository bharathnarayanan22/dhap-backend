package com.example.dhap.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A user's application to become a coordinator — stored in "applications" collection.
 *
 * userId + userEmail are resolved from the JWT at creation time (never from request body)
 * and stored as snapshots so reads need no join.
 *
 * status lifecycle: PENDING → ACCEPTED (triggers role promotion) | REJECTED
 */
@Document(collection = "applications")
public class CoordinatorApplication {

    @Id
    private String id = UUID.randomUUID().toString();

    private String userId;       // User.id of the applicant
    private String userEmail;    // User.email snapshot

    private String message;

    /** Stored as ApplicationStatus enum name: "PENDING", "ACCEPTED", "REJECTED". */
    private String status = "PENDING";

    private LocalDateTime submittedAt;

    public CoordinatorApplication() {}

    // ── Getters ────────────────────────────────────────────────────────────
    public String        getId()          { return id; }
    public String        getUserId()      { return userId; }
    public String        getUserEmail()   { return userEmail; }
    public String        getMessage()     { return message; }
    public String        getStatus()      { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setUserId(String userId)           { this.userId = userId; }
    public void setUserEmail(String userEmail)      { this.userEmail = userEmail; }
    public void setMessage(String message)          { this.message = message; }
    public void setStatus(String status)            { this.status = status; }
    public void setSubmittedAt(LocalDateTime t)     { this.submittedAt = t; }
}