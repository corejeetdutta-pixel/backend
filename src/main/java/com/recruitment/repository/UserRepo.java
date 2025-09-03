package com.recruitment.repository;

import com.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUserId(String userId);
 // Add this method for email verification
    Optional<User> findByVerificationToken(String token);
}