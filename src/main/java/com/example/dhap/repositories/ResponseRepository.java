package com.example.dhap.repositories;

import com.example.dhap.entities.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResponseRepository extends MongoRepository<Response, String> {

    /**
     * Used by GET /requests/{requestId}/responses.
     * Spring Data derives the query from the method name — matches on Response.requestId field.
     */
    Page<Response> findByRequestId(String requestId, Pageable pageable);
}