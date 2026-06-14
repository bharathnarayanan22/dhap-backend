package com.example.dhap.repositories;

import com.example.dhap.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** Bulk-clear inTask for all given users — single MongoDB updateMany. */
    @Query("{ '_id': { '$in': ?0 } }")
    @Update("{ '$set': { 'inTask': false } }")
    void bulkSetInTaskFalse(List<String> userIds);
    long countByInTask(boolean inTask);
    long countByRole(String role);
}