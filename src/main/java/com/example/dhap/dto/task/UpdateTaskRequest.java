package com.example.dhap.dto.task;

import com.example.dhap.enums.TaskStatus;

import java.util.List;

/**
 * Body for PATCH /tasks/{id} — all fields optional.
 *
 * Volunteer submits proof: { "status": "IN_VERIFICATION", "proofs": [...] }
 * Coordinator marks done:  { "status": "COMPLETED" }
 */
public class UpdateTaskRequest {
    public TaskStatus status;
    public List<ProofDto> proofs;
}