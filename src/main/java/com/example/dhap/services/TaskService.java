package com.example.dhap.services;

import com.example.dhap.dto.task.*;
import com.example.dhap.entities.Proof;
import com.example.dhap.entities.Task;
import com.example.dhap.entities.User;
import com.example.dhap.enums.TaskStatus;
import com.example.dhap.repositories.TaskRepository;
import com.example.dhap.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // ── Queries ────────────────────────────────────────────────────────────

    /**
     * GET /tasks?page=&size=[&status=][&volunteerId=]
     *
     * All four filter combinations are supported:
     *   - no filters       → all tasks
     *   - status only      → tasks with that status
     *   - volunteerId only → tasks the volunteer has accepted
     *   - both             → tasks accepted by that volunteer with that status
     */
    public Page<TaskResponse> getAll(Pageable pageable,
                                     TaskStatus status,
                                     String volunteerId) {
        if (status != null && volunteerId != null) {
            return taskRepository
                    .findByAssignedUserIdAndStatus(volunteerId, status, pageable)
                    .map(TaskResponse::from);
        }
        if (volunteerId != null) {
            return taskRepository
                    .findByAssignedUserId(volunteerId, pageable)
                    .map(TaskResponse::from);
        }
        if (status != null) {
            return taskRepository
                    .findByStatus(status, pageable)
                    .map(TaskResponse::from);
        }
        return taskRepository.findAll(pageable).map(TaskResponse::from);
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    /** POST /tasks — coordinator creates a task. */
    @Transactional
    public TaskResponse create(CreateTaskRequest req) {
        Task task         = new Task();
        task.title        = req.title;
        task.description  = req.description;
        task.volunteer    = req.volunteer;
        task.startAddress = req.startAddress;
        task.endAddress   = req.endAddress;
        if (req.startLocation != null) task.startLocation = req.startLocation.toEntity();
        if (req.endLocation   != null) task.endLocation   = req.endLocation.toEntity();
        return TaskResponse.from(taskRepository.save(task));
    }

    /**
     * POST /tasks/{id}/accept — volunteer accepts a task (user resolved from JWT).
     *
     * One atomic operation (@Transactional):
     *   1. Capacity check  — 409 if task is full
     *   2. Duplicate check — 409 if this volunteer already accepted
     *   3. Add userId to assignedUserIds, increment volunteersAccepted
     *   4. Transition PENDING → IN_PROGRESS
     *   5. Set user.inTask = true
     */
    @Transactional
    public TaskResponse accept(String taskId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Task task = findOrThrow(taskId);

        if (task.volunteersAccepted >= task.volunteer) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Task already has required volunteers");
        }
        if (task.assignedUserIds.contains(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Task already accepted by this user");
        }

        task.assignedUserIds.add(user.getId());
        task.volunteersAccepted++;

        if (task.status == TaskStatus.PENDING) {
            task.status = TaskStatus.IN_PROGRESS;
        }

        user.setInTask(true);
        userRepository.save(user);

        return TaskResponse.from(taskRepository.save(task));
    }

    /**
     * PATCH /tasks/{id} — proof submission or status update.
     *
     * Volunteer submits proof: { status: "IN_VERIFICATION", proofs: [...] }
     * Coordinator marks done:  { status: "COMPLETED" }
     *
     * On COMPLETED: all assigned volunteers get inTask=false in a single
     * bulk update (one MongoDB updateMany — no N+1 queries).
     */
    @Transactional
    public TaskResponse update(String taskId, UpdateTaskRequest req) {
        Task task = findOrThrow(taskId);

        if (req.status != null) {
            task.status = req.status;

            if (req.status == TaskStatus.COMPLETED && !task.assignedUserIds.isEmpty()) {
                // Bulk clear inTask for all assigned volunteers — single DB call
                userRepository.bulkSetInTaskFalse(task.assignedUserIds);
            }
        }

        if (req.proofs != null && !req.proofs.isEmpty()) {
            for (ProofDto dto : req.proofs) {
                Proof proof    = new Proof();
                proof.message  = dto.message;
                proof.mediaPaths = dto.mediaPaths != null ? dto.mediaPaths : List.of();
                task.proofs.add(proof);
            }
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    /**
     * DELETE /tasks/{id} — removes the task.
     * Does NOT clear inTask on assigned users; call this only when
     * the task hasn't been accepted yet (PENDING status is the safe window).
     */
    @Transactional
    public void delete(String taskId) {
        Task task = findOrThrow(taskId);
        taskRepository.delete(task);
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    public Task findOrThrow(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"));
    }
}