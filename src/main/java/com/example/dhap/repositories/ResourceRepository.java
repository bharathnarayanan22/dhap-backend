package com.example.dhap.repositories;

import com.example.dhap.entities.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceRepository extends MongoRepository<Resource, String> {

    /**
     * Used by GET /resources?donorId={id} — "My Contributions" screen in Flutter.
     * Spring Data MongoDB derives the query from the method name.
     */
    Page<Resource> findByDonorId(String donorId, Pageable pageable);
    long countByResourceType(String resourceType);
}