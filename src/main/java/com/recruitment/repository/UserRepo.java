package com.recruitment.repository;

import com.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUserId(String userId);
    @Query("SELECT u FROM User u JOIN u.appliedJobs j WHERE j.jobId = :jobId")
    List<User> findUsersByJobId(@Param("jobId") String jobId);
 // Add this method for email verification
 //   Optional<User> findByVerificationToken(String token);
    Optional<User> findByVerificationToken(String verificationToken); // ✅ This is critical
}