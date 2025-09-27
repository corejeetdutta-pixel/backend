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
        try {
            System.out.println("=== EMPLOYEE REGISTRATION STARTED ===");
            System.out.println("Email: " + emp.getEmail() + ", EmpID: " + emp.getEmpId());
            
            if (repo.existsByEmail(emp.getEmail())) {
                System.out.println("❌ Email already registered: " + emp.getEmail());
                return ResponseEntity.badRequest().body("Email already registered");
            }

            if (!PASSWORD_PATTERN.matcher(emp.getPassword()).matches()) {
                System.out.println("❌ Weak password for: " + emp.getEmail());
                return ResponseEntity.badRequest().body("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
            }

            // Encode password
            emp.setPassword(passwordEncoder.encode(emp.getPassword()));
            emp.setAgreedToTerms(true);
            emp.setRole("EMPLOYER");
            emp.setVerified(false); // Not verified until email confirmation

            Employee savedEmp = repo.save(emp);
            System.out.println("✅ Employee saved with ID: " + savedEmp.getId());

            // Generate token and send email
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setEmployee(savedEmp);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            tokenRepo.save(verificationToken);

            System.out.println("✅ Verification token generated: " + token);

            // Use frontend URL for verification - FIXED URL FORMAT
            String frontendUrl = "https://backend-n4w7.onrender.com"; // https://backend-n4w7.onrender.com
            String verificationUrl = frontendUrl + "/auth/employee/verify?token=" + token;
            
            emailService.sendVerificationEmail(emp.getEmail(), emp.getName(), verificationUrl);
            
            System.out.println("✅ Verification email sent to: " + emp.getEmail());
            System.out.println("✅ Verification URL: " + verificationUrl);

            return ResponseEntity.ok("Registration successful. Please check your email to verify your account.");
            
        } catch (Exception e) {
            System.err.println("❌ Employee registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Registration failed. Please try again.");
        }
    }
    
    // ✅ Resend Verification Email for Employee
 // ✅ Resend Verification Email for Employee - FIXED endpoint
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            System.out.println("=== RESEND VERIFICATION FOR EMPLOYEE ===");
            System.out.println("Email: " + email);
            
            Optional<Employee> employeeOpt = repo.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                System.out.println("❌ Employee not found: " + email);
                return ResponseEntity.badRequest().body("Employee not found with this email");
            }

            Employee employee = employeeOpt.get();
            
            if (employee.isVerified()) {
                System.out.println("ℹ️ Employee already verified: " + email);
                return ResponseEntity.badRequest().body("Email is already verified");
            }

            // Check if token exists and is still valid
            Optional<VerificationToken> existingTokenOpt = tokenRepo.findByEmployee(employee);
            VerificationToken verificationToken;
            
            if (existingTokenOpt.isPresent() && 
                existingTokenOpt.get().getExpiryDate().isAfter(LocalDateTime.now())) {
                // Use existing token if still valid
                verificationToken = existingTokenOpt.get();
                System.out.println("✅ Using existing valid token");
            } else {
                // Generate new token
                if (existingTokenOpt.isPresent()) {
                    // Remove expired token
                    tokenRepo.delete(existingTokenOpt.get());
                }
                
                String newToken = UUID.randomUUID().toString();
                verificationToken = new VerificationToken();
                verificationToken.setToken(newToken);
                verificationToken.setEmployee(employee);
                verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
                tokenRepo.save(verificationToken);
                System.out.println("✅ Generated new token: " + newToken);
            }

            // Send verification email
            String frontendUrl = "https://backend-n4w7.onrender.com"; // Change to your actual domain
            String verificationUrl = frontendUrl + "/auth/employee/verify?token=" + verificationToken.getToken();
            
            emailService.sendVerificationEmail(employee.getEmail(), employee.getName(), verificationUrl);
            
            System.out.println("✅ Verification email resent to: " + employee.getEmail());
            return ResponseEntity.ok("Verification email sent successfully. Please check your inbox.");
            
        } catch (Exception e) {
            System.err.println("❌ Resend verification error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to resend verification email");
        }
    }

    // ✅ Verify Employee Email - FIXED with better debugging
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            System.out.println("=== EMPLOYEE EMAIL VERIFICATION STARTED ===");
            System.out.println("Received token: " + token);
            
            // Debug: List all tokens
            System.out.println("=== ALL EMPLOYEE TOKENS IN DATABASE ===");
            tokenRepo.findAll().forEach(vt -> {
                System.out.println("Token: " + vt.getToken() + 
                                 " | Employee: " + (vt.getEmployee() != null ? vt.getEmployee().getEmail() : "null") +
                                 " | Expiry: " + vt.getExpiryDate() +
                                 " | Verified: " + vt.isVerified());
            });
            System.out.println("=== END TOKENS LIST ===");
            
            Optional<VerificationToken> opt = tokenRepo.findByToken(token);
            if (opt.isEmpty()) {
                System.out.println("❌ Invalid verification token. Token not found in database.");
                return ResponseEntity.badRequest().body("Invalid verification token.");
            }

            VerificationToken vt = opt.get();
            System.out.println("✅ Token found for employee: " + 
                             (vt.getEmployee() != null ? vt.getEmployee().getEmail() : "null"));
            
            if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
                System.out.println("❌ Token expired");
                System.out.println("Token expiry: " + vt.getExpiryDate());
                System.out.println("Current time: " + LocalDateTime.now());
                return ResponseEntity.badRequest().body("Verification token has expired.");
            }

            if (vt.isVerified()) {
                System.out.println("ℹ️ Email already verified");
                return ResponseEntity.ok("Email already verified.");
            }

            // Get the employee and mark as verified
            Employee employee = vt.getEmployee();
            if (employee == null) {
                System.out.println("❌ Employee not found for token");
                return ResponseEntity.badRequest().body("Employee not found for this token.");
            }
            
            employee.setVerified(true);
            repo.save(employee);
            
            // Update token status
            vt.setVerified(true);
            tokenRepo.save(vt);

            System.out.println("🎉 Employee email verified successfully for: " + employee.getEmail());
            return ResponseEntity.ok("Email verified successfully. You can now login.");
            
        } catch (Exception e) {
            System.err.println("❌ Employee email verification error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Email verification failed. Please try again.");
        }
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
                System.out.println("❌ Email not verified for: " + existingEmp.getEmail());
                return ResponseEntity.status(401).body("Email not verified. Please check your email for verification link.");
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