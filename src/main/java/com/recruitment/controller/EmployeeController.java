package com.recruitment.controller;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    // ✅ Aadhar regex: 12 digits
    private static final Pattern AADHAR_PATTERN = Pattern.compile("^\\d{12}$");
    
    // ✅ PAN regex: 10 characters, 5 letters, 4 numbers, 1 letter
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    // ✅ Mobile regex: 10 digits
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{10}$");
    
    // Employee registration key from properties
    @Value("${system.employee.key}")
    private String systemEmpKey;

    // ✅ Register new Employee with registration key validation
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("=== EMPLOYEE REGISTRATION STARTED ===");
            
            // Extract employee data and registration key
            Employee emp = new Employee();
            emp.setName((String) requestData.get("name"));
            emp.setEmail((String) requestData.get("email"));
            emp.setMobile((String) requestData.get("mobile"));
            emp.setAddress((String) requestData.get("address"));
            emp.setGender((String) requestData.get("gender"));
            emp.setDateOfBirth(LocalDate.parse((String) requestData.get("dateOfBirth")));
            emp.setAadharNumber((String) requestData.get("aadharNumber"));
            emp.setPanNumber((String) requestData.get("panNumber"));
            emp.setPassword((String) requestData.get("password"));
            
            String registrationKey = (String) requestData.get("registrationKey");
            
            System.out.println("Email: " + emp.getEmail() + ", Name: " + emp.getName());
            System.out.println("Registration Key provided: " + registrationKey);
            System.out.println("Expected Registration Key: " + systemEmpKey);
            
            // Check if email already exists
            if (repo.existsByEmail(emp.getEmail())) {
                System.out.println("❌ Email already registered: " + emp.getEmail());
                return ResponseEntity.badRequest().body("Email already registered");
            }

            // Validate registration key
            if (registrationKey == null || registrationKey.isEmpty()) {
                System.out.println("❌ Registration key is required");
                return ResponseEntity.badRequest().body("Registration key is required");
            }
            
            if (!systemEmpKey.equals(registrationKey)) {
                System.out.println("❌ Invalid registration key provided");
                return ResponseEntity.badRequest().body("Invalid registration key. Please provide the correct registration key.");
            }

            // Validate password strength
            if (!PASSWORD_PATTERN.matcher(emp.getPassword()).matches()) {
                System.out.println("❌ Weak password for: " + emp.getEmail());
                return ResponseEntity.badRequest().body("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
            }

            // Validate Aadhar number
            if (emp.getAadharNumber() != null && !AADHAR_PATTERN.matcher(emp.getAadharNumber()).matches()) {
                return ResponseEntity.badRequest().body("Aadhar number must be 12 digits");
            }

            // Validate PAN number
            if (emp.getPanNumber() != null && !PAN_PATTERN.matcher(emp.getPanNumber()).matches()) {
                return ResponseEntity.badRequest().body("PAN number must be in valid format (e.g., ABCDE1234F)");
            }

            // Validate mobile number
            if (emp.getMobile() != null && !MOBILE_PATTERN.matcher(emp.getMobile()).matches()) {
                return ResponseEntity.badRequest().body("Mobile number must be 10 digits");
            }

            // Validate date of birth (must be at least 18 years old)
            if (emp.getDateOfBirth() != null && emp.getDateOfBirth().isAfter(LocalDate.now().minusYears(18))) {
                return ResponseEntity.badRequest().body("Employee must be at least 18 years old");
            }

            // Encode password and set default values
            emp.setPassword(passwordEncoder.encode(emp.getPassword()));
            emp.setAgreedToTerms(true);
            emp.setRole("EMPLOYER");
            emp.setVerified(false); // Not verified until email confirmation
            // EmpId will be auto-generated by @PrePersist

            Employee savedEmp = repo.save(emp);
            System.out.println("✅ Employee saved with ID: " + savedEmp.getId() + ", EmpId: " + savedEmp.getEmpId());

            // Generate token and send email
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setEmployee(savedEmp);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            tokenRepo.save(verificationToken);

            System.out.println("✅ Verification token generated: " + token);

            // Use frontend URL for verification
            String frontendUrl = "http://localhost:8080";
            String verificationUrl = frontendUrl + "/auth/employee/verify?token=" + token;
            
            emailService.sendVerificationEmail(emp.getEmail(), emp.getName(), verificationUrl);
            
            System.out.println("✅ Verification email sent to: " + emp.getEmail());
            System.out.println("✅ Verification URL: " + verificationUrl);

            // Return EmpId in response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful. Please check your email to verify your account.");
            response.put("empId", savedEmp.getEmpId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Employee registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Registration failed. Please try again.");
        }
    }

    // ... rest of your existing methods (login, resend-verification, verify, etc.) remain the same ...

    // ✅ Enhanced Login with EmpId verification
 // In your EmployeeController - update the login method
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> loginRequest) {
        try {
            String email = (String) loginRequest.get("email");
            String password = (String) loginRequest.get("password");
            Object empIdObj = loginRequest.get("empId");
            
            // Handle the empId properly - it might be coming as String with quotes
            String empId;
            if (empIdObj instanceof String) {
                empId = ((String) empIdObj).trim().replaceAll("^\"|\"$", "");
            } else {
                empId = String.valueOf(empIdObj).trim();
            }
            
            System.out.println("Employee login attempt - Email: " + email + ", EmpId: " + empId);
            System.out.println("EmpId class: " + (empIdObj != null ? empIdObj.getClass().getSimpleName() : "null"));
            System.out.println("Cleaned EmpId: " + empId);
            
            if (email == null || password == null || empId == null || empId.isEmpty()) {
                return ResponseEntity.badRequest().body("Email, password, and EmpId are required");
            }

            // Find employee by email
            Optional<Employee> employeeOptional = repo.findByEmail(email);
            if (employeeOptional.isEmpty()) {
                System.out.println("❌ Employee not found with email: " + email);
                return ResponseEntity.status(401).body("Invalid credentials");
            }
            
            final Employee existingEmp = employeeOptional.get();
            
            // Debug information
            System.out.println("Expected EmpId from DB: " + existingEmp.getEmpId());
            System.out.println("Received EmpId from request: " + empId);
            System.out.println("EmpId match: " + existingEmp.getEmpId().equals(empId));
            
            // Verify EmpId matches
            if (!existingEmp.getEmpId().equals(empId)) {
                System.out.println("❌ EmpId mismatch. Expected: " + existingEmp.getEmpId() + ", Received: " + empId);
                return ResponseEntity.status(401).body("Invalid credentials");
            }
            
            // Authenticate using Spring Security
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            
            System.out.println("✅ Authentication successful for: " + existingEmp.getName());
            
            // Check if email is verified
            if (!existingEmp.isVerified()) {
                System.out.println("❌ Email not verified for: " + existingEmp.getEmail());
                return ResponseEntity.status(401).body("Email not verified. Please check your email for verification link.");
            }
            
            System.out.println("✅ Email verified, generating token...");
            
            // Load user details and generate JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            final String jwt = jwtUtil.generateToken(userDetails, existingEmp.getEmpId(), "EMPLOYEE");
            
            System.out.println("✅ Token generated successfully");
            
            // Send login email notification
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

    // ✅ Resend Verification Email for Employee
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
            String frontendUrl = "http://localhost:8080";  //https://api.atract.in
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
    
 // ✅ Retrieve Employee ID by Email
    @PostMapping("/retrieve-empId")
    public ResponseEntity<?> retrieveEmpId(@RequestParam String email) {
        try {
            System.out.println("=== RETRIEVE EMPLOYEE ID ===");
            System.out.println("Email: " + email);
            
            Optional<Employee> employeeOpt = repo.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                System.out.println("❌ Employee not found with email: " + email);
                return ResponseEntity.badRequest().body("No employee found with this email address");
            }

            Employee employee = employeeOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("empId", employee.getEmpId());
            response.put("message", "Employee ID retrieved successfully");
            
            System.out.println("✅ Employee ID retrieved: " + employee.getEmpId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Retrieve Employee ID error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve Employee ID");
        }
    }

    // ✅ Forgot Password - Send Reset Link
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            System.out.println("=== FORGOT PASSWORD REQUEST ===");
            System.out.println("Email: " + email);
            
            Optional<Employee> employeeOpt = repo.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                // Don't reveal whether email exists or not for security
                System.out.println("ℹ️ Password reset request for non-existent email: " + email);
                return ResponseEntity.ok("If an account with this email exists, a password reset link has been sent");
            }

            Employee employee = employeeOpt.get();
            
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            
            // In a real application, you'd save this token to the database with expiry
            // For now, we'll just send a mock email
            
            String resetUrl = "http://localhost:8080/auth/employee/reset-password?token=" + resetToken;
            
            // Send password reset email
         //   emailService.sendPasswordResetEmail(employee.getEmail(), employee.getName(), resetUrl);
            
            System.out.println("✅ Password reset email sent to: " + employee.getEmail());
            return ResponseEntity.ok("If an account with this email exists, a password reset link has been sent");
            
        } catch (Exception e) {
            System.err.println("❌ Forgot password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to process password reset request");
        }
    }

    // ✅ Verify Employee Email
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            System.out.println("=== EMPLOYEE EMAIL VERIFICATION STARTED ===");
            System.out.println("Received token: " + token);
            
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