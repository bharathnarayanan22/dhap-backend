package com.example.dhap.controllers;

import com.example.dhap.dto.task.*;
import com.example.dhap.enums.TaskStatus;
import com.example.dhap.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * GET /tasks?page=0&size=20[&status=PENDING][&volunteerId=uuid]
     *
     * Coordinator: GET /tasks                     → all tasks
     * Coordinator: GET /tasks?status=IN_VERIFICATION → tasks needing review
     * Volunteer:   GET /tasks?volunteerId={id}    → my accepted tasks
     * Volunteer:   GET /tasks?status=PENDING      → available tasks to accept
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String volunteerId) {
        return ResponseEntity.ok(taskService.getAll(pageable, status, volunteerId));
    }

    /**
     * POST /tasks — coordinator creates a task.
     * Returns 201 with the created task.
     */
    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody CreateTaskRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(req));
    }

    /**
     * POST /tasks/{id}/accept — volunteer accepts (user resolved from JWT, no body).
     * Returns 200 with updated task. 409 if full or already accepted.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<TaskResponse> accept(@PathVariable String id) {
        return ResponseEntity.ok(taskService.accept(id));
    }

    /**
     * PATCH /tasks/{id} — proof submission or status update.
     *
     * Volunteer: { "status": "IN_VERIFICATION", "proofs": [{ "message": "...",
     *              "mediaPaths": ["http://localhost:8080/files/proof.jpg"] }] }
     * Coordinator: { "status": "COMPLETED" }
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable String id,
            @RequestBody UpdateTaskRequest req) {
        return ResponseEntity.ok(taskService.update(id, req));
    }

    /**
     * DELETE /tasks/{id} — remove a task. Returns 204.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}