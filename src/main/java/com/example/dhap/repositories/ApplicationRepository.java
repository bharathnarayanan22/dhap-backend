package com.example.dhap.repositories;

import com.example.dhap.entities.CoordinatorApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApplicationRepository extends MongoRepository<CoordinatorApplication, String> {

    /**
     * Used by GET /applications?status=PENDING|ACCEPTED|REJECTED.
     * status is the stored String name of ApplicationStatus enum.
     */
    Page<CoordinatorApplication> findByStatus(String status, Pageable pageable);
}