package com.recruitment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class DatabaseCleanupService implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            // Clean up orphaned user_skills records
            int deletedRecords = entityManager.createNativeQuery(
                "DELETE FROM user_skills WHERE user_id NOT IN (SELECT id FROM users)"
            ).executeUpdate();
            
            if (deletedRecords > 0) {
                System.out.println("Cleaned up " + deletedRecords + " orphaned user_skills records");
            }
            
        } catch (Exception e) {
            System.err.println("Database cleanup failed: " + e.getMessage());
            // Don't throw exception to prevent application startup failure
        }
    }
}
