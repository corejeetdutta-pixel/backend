package com.recruitment.controller;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.recruitment.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/auth/employee")
public class EmployeeController {

    @Autowired
    private EmployeeRepo repo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationTokenRepository tokenRepo;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.recruitment.service.CustomUserDetailsService userDetailsService;

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

        // Encode password
        emp.setPassword(passwordEncoder.encode(emp.getPassword()));
        emp.setAgreedToTerms(true);
        emp.setRole("Employer");
        emp.setVerified(false); // Not verified until email confirmation

        repo.save(emp);

        // Generate token and send email
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setEmployee(emp);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepo.save(verificationToken);

        // Use frontend URL for verification - FIXED URL FORMAT
        String frontendUrl = "https://backend-n4w7.onrender.com";
        String link = frontendUrl + "/auth/employee/verify?token=" + token;
        
        emailService.sendVerificationEmail(emp.getEmail(), emp.getName(), link);

        return ResponseEntity.ok("Check your email to verify your account.");
    }
    
 // Updated verifyEmail endpoint
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
        employee.setVerified(true);
        
        // Save updated employee status
        repo.save(employee);
        
        // Update token status
        vt.setVerified(true);
        tokenRepo.save(vt);

        // Return success message instead of redirecting
        return ResponseEntity.ok("Email verified successfully. You can now login.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Employee emp) {
        try {
            System.out.println("Employee login attempt: " + emp.getEmail());
            
            // Authenticate
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(emp.getEmail(), emp.getPassword())
            );
            
            System.out.println("Authentication successful");
            
            // Load user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(emp.getEmail());
            
            // Find employee - handle case where employee might not be found
            Optional<Employee> employeeOptional = repo.findByEmail(emp.getEmail());
            if (employeeOptional.isEmpty()) {
                return ResponseEntity.status(401).body("Employee not found with email: " + emp.getEmail());
            }
            
            final Employee existingEmp = employeeOptional.get();
            System.out.println("User details loaded: " + existingEmp.getName());
            
            // Check if email is verified
            if (!existingEmp.isVerified()) {
                return ResponseEntity.status(401).body("Email not verified. Please check your email.");
            }
            
            System.out.println("Email verified, generating token...");
            
            // Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails, existingEmp.getEmpId(), "EMPLOYEE");
            
            System.out.println("Token generated successfully");
            
            // Send login email
            emailService.sendLoginNotification(existingEmp.getEmail(), existingEmp.getName());
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwt);
            response.put("user", existingEmp);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid credentials: " + e.getMessage());
        }
    }

    // ✅ Get current logged-in employee using JWT
    @GetMapping("/current-employee")
    public ResponseEntity<?> currentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Authorization header missing or invalid");
        }
        
        String jwt = authHeader.substring(7);
        String email = jwtUtil.extractUsername(jwt);
        
        Optional<Employee> emp = repo.findByEmail(email);
        if (emp.isEmpty()) {
            return ResponseEntity.status(401).body("Employee not found");
        }
        
        return ResponseEntity.ok(emp.get());
    }

    // ✅ Logout - client should remove token
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}