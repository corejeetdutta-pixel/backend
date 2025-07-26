package com.recruitment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.Arrays;
import java.util.List;

@Service
public class DatabaseCleanupService implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Starting database cleanup...");
        
        // List of tables with foreign key references to users table
        List<String> userDependentTables = Arrays.asList(
            "user_skills",
            "applications",
            "bookmarks",
            "evaluation_records",
            "evaluation_requests"
        );
        
        // Disable foreign key checks temporarily
        try {
            entityManager.createNativeQuery("SET CONSTRAINTS ALL DEFERRED").executeUpdate();
            System.out.println("Temporarily disabled foreign key constraints");
        } catch (Exception e) {
            System.out.println("Could not disable constraints: " + e.getMessage());
        }
        
        // Clean up orphaned records in all user-dependent tables
        for (String table : userDependentTables) {
            try {
                String sql = String.format(
                    "DELETE FROM %s WHERE user_id NOT IN (SELECT id FROM users)",
                    table
                );
                int deleted = entityManager.createNativeQuery(sql).executeUpdate();
                if (deleted > 0) {
                    System.out.println(String.format("Cleaned up %d orphaned records from %s", deleted, table));
                }
            } catch (Exception e) {
                System.out.println(String.format("Warning cleaning %s: %s", table, e.getMessage()));
            }
        }
        
        // Re-enable foreign key checks
        try {
            entityManager.createNativeQuery("SET CONSTRAINTS ALL IMMEDIATE").executeUpdate();
            System.out.println("Re-enabled foreign key constraints");
        } catch (Exception e) {
            System.out.println("Could not re-enable constraints: " + e.getMessage());
        }
        
        System.out.println("Database cleanup completed successfully");
    }
}
