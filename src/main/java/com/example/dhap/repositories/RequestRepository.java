package com.example.dhap.repositories;

import com.example.dhap.entities.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestRepository extends MongoRepository<Request, String> {
    long countByStatus(String status);
}