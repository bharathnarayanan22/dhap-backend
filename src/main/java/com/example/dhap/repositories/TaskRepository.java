package com.example.dhap.repositories;

import com.example.dhap.entities.Task;
import com.example.dhap.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TaskRepository extends MongoRepository<Task, String> {

    /** Filter by status — used for coordinator views (e.g. all IN_VERIFICATION tasks). */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Tasks a specific volunteer has accepted.
     * MongoDB query: find docs where assignedUserIds array contains the given userId.
     */
    @Query("{ 'assignedUserIds': ?0 }")
    Page<Task> findByAssignedUserId(String userId, Pageable pageable);

    /** Tasks accepted by a volunteer AND matching a status — used for dashboard filtering. */
    @Query("{ 'assignedUserIds': ?0, 'status': ?1 }")
    Page<Task> findByAssignedUserIdAndStatus(String userId, TaskStatus status, Pageable pageable);
    long countByStatus(TaskStatus status);
}