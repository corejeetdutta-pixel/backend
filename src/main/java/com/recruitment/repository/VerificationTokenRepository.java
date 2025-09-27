package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.recruitment.entity.VerificationToken;
import com.recruitment.entity.Employee;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByEmployee(Employee employee); // ✅ used in resend-verification
}
