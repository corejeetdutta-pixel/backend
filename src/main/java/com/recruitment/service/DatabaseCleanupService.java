package com.recruitment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.Arrays;
import java.util.List;

@Service
public class DatabaseCleanupService implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("Starting database cleanup...");
        
        try {
            // Check if users table exists before proceeding
            if (!tableExists("users")) {
                System.out.println("Users table not found, skipping cleanup");
                return;
            }
            
            // Disable foreign key constraints
            try {
                jdbcTemplate.execute("SET CONSTRAINTS ALL DEFERRED");
                System.out.println("Temporarily disabled foreign key constraints");
            } catch (Exception e) {
                System.err.println("Warning: Could not disable constraints - " + e.getMessage());
            }
            
            // Clean up orphaned user_skills if table exists
            cleanupOrphanedRecords("user_skills", "user_id");
            
            // Clean up other orphaned records if tables exist
            cleanupOrphanedRecordsIfExists("applications", "user_id");
            cleanupOrphanedRecordsIfExists("bookmarks", "user_id");
            cleanupOrphanedRecordsIfExists("evaluation_records", "user_id");
            cleanupOrphanedRecordsIfExists("evaluation_requests", "user_id");
            
            // Re-enable foreign key constraints
            try {
                jdbcTemplate.execute("SET CONSTRAINTS ALL IMMEDIATE");
                System.out.println("Re-enabled foreign key constraints");
            } catch (Exception e) {
                System.err.println("Warning: Could not re-enable constraints - " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error during database cleanup: " + e.getMessage());
            // Don't rethrow to prevent application startup failure
        }
        
        System.out.println("Database cleanup completed successfully");
    }
    
    private boolean tableExists(String tableName) {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM " + tableName + " LIMIT 1", Integer.class);
            return true;
        } catch (Exception e) {
            System.out.println("Table " + tableName + " does not exist or is not accessible");
            return false;
        }
    }
    
    private void cleanupOrphanedRecordsIfExists(String tableName, String foreignKeyColumn) {
        if (tableExists(tableName)) {
            cleanupOrphanedRecords(tableName, foreignKeyColumn);
        }
    }
    
    private void cleanupOrphanedRecords(String tableName, String foreignKeyColumn) {
        try {
            String sql = String.format("DELETE FROM %s WHERE %s NOT IN (SELECT id FROM users)", tableName, foreignKeyColumn);
            int count = jdbcTemplate.update(sql);
            System.out.println("Cleaned up " + count + " orphaned records from " + tableName);
        } catch (Exception e) {
            System.err.println("Warning cleaning " + tableName + ": " + e.getMessage());
        }
    }
}
