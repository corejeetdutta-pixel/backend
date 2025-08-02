package com.recruitment.controller;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.recruitment.entity.Employee;
import com.recruitment.entity.VerificationToken;
import com.recruitment.repository.EmployeeRepo;
import com.recruitment.repository.VerificationTokenRepository;
import com.recruitment.service.EmailService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth/employee")
public class EmployeeController {

    @Autowired
    private EmployeeRepo repo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationTokenRepository tokenRepo;

    // ✅ Password regex: min 8 chars, 1 upper, 1 lower, 1 digit, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // ✅ Register new Employee
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Employee emp) {
        if (repo.existsByEmail(emp.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        if (!PASSWORD_PATTERN.matcher(emp.getPassword()).matches()) {
            return ResponseEntity.badRequest().body("Weak password");
        }

        emp.setAgreedToTerms(true);
        emp.setRole("Employer");

        repo.save(emp);

        // Generate token and send email
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setEmployee(emp);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepo.save(verificationToken);

        String link = "https://recruitment-backend-s4la.onrender.com/auth/employee/verify?token=" + token;
        emailService.sendSimpleMessage(emp.getEmail(), "Email Verification", "Click to verify: " + link);

        return ResponseEntity.ok("Check your email to verify your account.");
    }
    
 // Updated verifyEmail endpoint in EmployeeController.java
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<VerificationToken> opt = tokenRepo.findByToken(token);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("Invalid token");

        VerificationToken vt = opt.get();
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        // Get the employee and mark as verified
        Employee employee = vt.getEmployee();
        employee.setVerified(true); // THIS WAS MISSING
        
        // Save updated employee status
        repo.save(employee);
        
        // Update token status
        vt.setVerified(true);
        tokenRepo.save(vt);

        return ResponseEntity.ok("Email verified successfully. You can now login.");
    }

 // EmployeeController.java - Updated login verification
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Employee emp, HttpSession session) {
        System.out.println("Login is triggered");

        Optional<Employee> optionalUser = repo.findByEmail(emp.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Employee not found");
        }

        Employee existingEmp = optionalUser.get();
        
        // Check if email is verified
        if (!existingEmp.isVerified()) {
            return ResponseEntity.status(401).body("Email not verified. Please check your email.");
        }
        
        if (!existingEmp.getPassword().equals(emp.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        // Set session timeout to 30 minutes
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("emp", existingEmp); // Set session key

        // Send login email
        emailService.sendLoginNotification(existingEmp.getEmail(), existingEmp.getName());

        // Create response with CORS headers
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("user", existingEmp);
        response.put("Access-Control-Allow-Credentials", "true");
        response.put("Access-Control-Expose-Headers", "Set-Cookie");

        return ResponseEntity.ok(response);    // Return response with CORS headers
    }


    // ✅ Get current logged-in employee
    @GetMapping("/current-employee")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Employee emp = (Employee) session.getAttribute("emp");
        if (emp == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        System.out.println("EmpId : " + emp.getEmpId());
        return ResponseEntity.ok(emp);
    }

    // ✅ Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
}
