package com.example.dhap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Enables multi-document @Transactional support for MongoDB Atlas.
 *
 * Atlas always runs on a replica set, so transactions are available
 * out of the box — no extra Atlas config needed.
 *
 * Why this matters for tasks:
 *   POST /tasks/{id}/accept   → updates Task AND User in one atomic operation
 *   PATCH /tasks/{id} COMPLETED → updates Task AND bulk-clears inTask on all
 *                                  assigned User documents atomically
 */
@Configuration
@EnableTransactionManagement
public class MongoConfig {

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }
}