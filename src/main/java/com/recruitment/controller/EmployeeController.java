package com.recruitment.controller;

import com.recruitment.dto.EmployeeDto;
import com.recruitment.entity.Employee;
import com.recruitment.entity.VerificationToken;
import com.recruitment.repository.EmployeeRepo;
import com.recruitment.repository.VerificationTokenRepository;
import com.recruitment.service.EmailService;
import com.recruitment.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    // ✅ Inject frontend URL from application.properties
    @Value("${spring.mail.frontendUrl:http://localhost:5173}")
    private String frontendUrl;

    @Value("${system.employee.key}")
    private String systemEmpKey;

    // ✅ Password regex pattern (from first class)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // ============================
    // REGISTER EMPLOYEE (Enhanced with DTO validation)
    // ============================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody EmployeeDto employeeDto, BindingResult bindingResult) {
        try {
            System.out.println("=== EMPLOYEE REGISTRATION STARTED ===");
            System.out.println("Email: " + employeeDto.getEmail() + ", Name: " + employeeDto.getName());

            // Check for validation errors from DTO
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                System.out.println("❌ Validation errors: " + errors);
                return ResponseEntity.badRequest().body(errors);
            }

            // Check if email already exists
            if (repo.existsByEmail(employeeDto.getEmail())) {
                System.out.println("❌ Email already registered: " + employeeDto.getEmail());
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("email", "Email already registered")
                );
            }

            // Validate registration key
            if (!systemEmpKey.equals(employeeDto.getRegistrationKey())) {
                System.out.println("❌ Invalid registration key provided");
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("registrationKey", "Invalid registration key. Please provide the correct registration key.")
                );
            }

            // Validate age (must be at least 18 years old)
            if (employeeDto.getDateOfBirth() != null) {
                int age = java.time.Period.between(employeeDto.getDateOfBirth(), LocalDate.now()).getYears();
                if (age < 18) {
                    return ResponseEntity.badRequest().body(
                        Collections.singletonMap("dateOfBirth", "Employee must be at least 18 years old")
                    );
                }
                if (age > 100) {
                    return ResponseEntity.badRequest().body(
                        Collections.singletonMap("dateOfBirth", "Please provide a valid date of birth")
                    );
                }
            }

            // Create employee entity
            Employee emp = new Employee();
            emp.setName(employeeDto.getName().trim());
            emp.setEmail(employeeDto.getEmail().trim().toLowerCase());
            emp.setMobile(employeeDto.getMobile().trim());
            emp.setAddress(employeeDto.getAddress().trim());
            emp.setGender(employeeDto.getGender());
            emp.setDateOfBirth(employeeDto.getDateOfBirth());
            emp.setAadharNumber(employeeDto.getAadharNumber().trim());
            emp.setPanNumber(employeeDto.getPanNumber().trim().toUpperCase());
            emp.setPassword(passwordEncoder.encode(employeeDto.getPassword()));
            emp.setAgreedToTerms(employeeDto.getAgreedToTerms());
            emp.setRole("EMPLOYER");
            emp.setVerified(false);

            // Perform custom entity validation
            try {
                emp.validate();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("validationError", e.getMessage())
                );
            }

            // Save employee with error handling
            Employee savedEmp;
            try {
                savedEmp = repo.save(emp);
                System.out.println("✅ Employee saved with ID: " + savedEmp.getId() + ", EmpId: " + savedEmp.getEmpId());
            } catch (Exception e) {
                System.err.println("❌ Database error while saving employee: " + e.getMessage());
                if (e.getMessage().contains("null value in column") && e.getMessage().contains("id")) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Database configuration error. Please contact administrator."));
                }
                throw e;
            }

            // Generate token and send email (using frontend URL from first class)
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setEmployee(savedEmp);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            verificationToken.setVerified(false);

            try {
                tokenRepo.save(verificationToken);
                System.out.println("✅ Verification token generated: " + token);
            } catch (Exception e) {
                System.err.println("❌ Error saving verification token: " + e.getMessage());
                // Continue with registration even if token saving fails
            }

            // Use dynamic frontend URL (from first class)
            String verificationUrl = frontendUrl + "/verify-employee?token=" + token;
            
            try {
                emailService.sendVerificationEmail(emp.getEmail(), emp.getName(), verificationUrl,EmailService.EmailType.EMPLOYEE_VERIFICATION);
                System.out.println("✅ Verification email sent to: " + emp.getEmail());
                System.out.println("✅ Verification URL: " + verificationUrl);
            } catch (Exception e) {
                System.err.println("❌ Error sending verification email: " + e.getMessage());
                // Continue with registration even if email fails
            }

            // Return EmpId in response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful. Please check your email to verify your account.");
            response.put("empId", savedEmp.getEmpId());
            response.put("email", savedEmp.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Employee registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Registration failed. Please try again."));
        }
    }

    // ============================
    // LOGIN (Enhanced with proper validation)
    // ============================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> loginRequest) {
        try {
            String email = (String) loginRequest.get("email");
            String password = (String) loginRequest.get("password");
            Object empIdObj = loginRequest.get("empId");
            
            // Input validation
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Email is required")
                );
            }
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Password is required")
                );
            }
            if (empIdObj == null) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Employee ID is required")
                );
            }
            
            String empId = empIdObj.toString().trim().replaceAll("^\"|\"$", "");
            
            System.out.println("Employee login attempt - Email: " + email + ", EmpId: " + empId);

            // Find employee by email
            Optional<Employee> employeeOptional = repo.findByEmail(email.trim().toLowerCase());
            if (employeeOptional.isEmpty()) {
                System.out.println("❌ Employee not found with email: " + email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Invalid credentials")
                );
            }
            
            final Employee existingEmp = employeeOptional.get();
            
            // Verify EmpId matches
            if (!existingEmp.getEmpId().equals(empId)) {
                System.out.println("❌ EmpId mismatch. Expected: " + existingEmp.getEmpId() + ", Received: " + empId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Invalid credentials")
                );
            }
            
            // Authenticate using Spring Security (from first class)
            try {
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
                );
            } catch (Exception e) {
                System.err.println("Authentication failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Invalid credentials")
                );
            }
            
            System.out.println("✅ Authentication successful for: " + existingEmp.getName());
            
            // Check if email is verified (from first class)
            if (!existingEmp.isVerified()) {
                System.out.println("❌ Email not verified for: " + existingEmp.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Email not verified. Please check your email for verification link.")
                );
            }
            
            // Load user details and generate JWT token (from first class)
            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            final String jwt = jwtUtil.generateToken(userDetails, existingEmp.getEmpId(), "EMPLOYEE");
            
            // Send login email notification
            emailService.sendLoginNotification(existingEmp.getEmail(), existingEmp.getName(), EmailService.EmailType.EMPLOYEE_VERIFICATION);
            
            // Create response (exclude sensitive data)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwt);
            response.put("user", createSafeEmployeeResponse(existingEmp));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Collections.singletonMap("error", "Invalid credentials")
            );
        }
    }

    // ============================
    // VERIFY EMAIL (Enhanced)
    // ============================
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            System.out.println("=== EMPLOYEE EMAIL VERIFICATION STARTED ===");
            System.out.println("Received token: " + token);
            
            Optional<VerificationToken> opt = tokenRepo.findByToken(token);
            if (opt.isEmpty()) {
                System.out.println("❌ Invalid verification token. Token not found in database.");
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired verification token."));
            }

            VerificationToken vt = opt.get();
            System.out.println("✅ Token found for employee: " + 
                             (vt.getEmployee() != null ? vt.getEmployee().getEmail() : "null"));
            
            if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
                System.out.println("❌ Token expired");
                return ResponseEntity.badRequest().body(Map.of("message", "Verification token has expired."));
            }

            if (vt.isVerified()) {
                System.out.println("ℹ️ Email already verified");
                return ResponseEntity.ok(Map.of("message", "Email already verified."));
            }

            // Get the employee and mark as verified
            Employee employee = vt.getEmployee();
            if (employee == null) {
                System.out.println("❌ Employee not found for token");
                return ResponseEntity.badRequest().body(Map.of("message", "Employee not found for this token."));
            }
            
            employee.setVerified(true);
            repo.save(employee);
            
            // Update token status
            vt.setVerified(true);
            tokenRepo.save(vt);

            System.out.println("🎉 Employee email verified successfully for: " + employee.getEmail());
            return ResponseEntity.ok(Map.of("message", "Email verified successfully! You can now login."));
            
        } catch (Exception e) {
            System.err.println("❌ Employee email verification error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Email verification failed. Please try again."));
        }
    }

    // ============================
    // RESEND VERIFICATION EMAIL (Enhanced)
    // ============================
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            System.out.println("=== RESEND VERIFICATION FOR EMPLOYEE ===");
            System.out.println("Email: " + email);
            
            Optional<Employee> employeeOpt = repo.findByEmail(email.trim().toLowerCase());
            if (employeeOpt.isEmpty()) {
                System.out.println("❌ Employee not found: " + email);
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "No employee found with this email")
                );
            }

            Employee employee = employeeOpt.get();
            
            if (employee.isVerified()) {
                System.out.println("ℹ️ Employee already verified: " + email);
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Email is already verified")
                );
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
                verificationToken.setVerified(false);
                tokenRepo.save(verificationToken);
                System.out.println("✅ Generated new token: " + newToken);
            }

            // Send verification email using frontend URL
            String verificationUrl = frontendUrl + "/verify-employee?token=" + verificationToken.getToken();
            emailService.sendVerificationEmail(employee.getEmail(), employee.getName(), verificationUrl);
            
            System.out.println("✅ Verification email resent to: " + employee.getEmail());
            return ResponseEntity.ok(
                Collections.singletonMap("message", "Verification email sent successfully. Please check your inbox.")
            );
            
        } catch (Exception e) {
            System.err.println("❌ Resend verification error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to resend verification email"));
        }
    }

    // ============================
    // CURRENT EMPLOYEE (Enhanced)
    // ============================
    @GetMapping("/current-employee")
    public ResponseEntity<?> currentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Authorization header missing or invalid")
                );
            }
            
            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            
            Optional<Employee> emp = repo.findByEmail(email);
            if (emp.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Employee not found")
                );
            }
            
            return ResponseEntity.ok(createSafeEmployeeResponse(emp.get()));
            
        } catch (Exception e) {
            System.err.println("Current employee error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Collections.singletonMap("error", "Invalid token")
            );
        }
    }

    // ============================
    // ADDITIONAL FEATURES FROM SECOND CLASS
    // ============================

    // ✅ Retrieve Employee ID by Email
    @PostMapping("/retrieve-empId")
    public ResponseEntity<?> retrieveEmpId(@RequestParam String email) {
        try {
            System.out.println("=== RETRIEVE EMPLOYEE ID ===");
            System.out.println("Email: " + email);
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Email is required")
                );
            }
            
            Optional<Employee> employeeOpt = repo.findByEmail(email.trim().toLowerCase());
            if (employeeOpt.isEmpty()) {
                System.out.println("❌ Employee not found with email: " + email);
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "No employee found with this email address")
                );
            }

            Employee employee = employeeOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("empId", employee.getEmpId());
            response.put("message", "Employee ID retrieved successfully");
            response.put("name", employee.getName());
            
            System.out.println("✅ Employee ID retrieved: " + employee.getEmpId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Retrieve Employee ID error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to retrieve Employee ID"));
        }
    }

    // ✅ Forgot Password - Send Reset Link
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            System.out.println("=== FORGOT PASSWORD REQUEST ===");
            System.out.println("Email: " + email);
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Email is required")
                );
            }
            
            Optional<Employee> employeeOpt = repo.findByEmail(email.trim().toLowerCase());
            if (employeeOpt.isEmpty()) {
                // Don't reveal whether email exists or not for security
                System.out.println("ℹ️ Password reset request for non-existent email: " + email);
                return ResponseEntity.ok(
                    Collections.singletonMap("message", "If an account with this email exists, a password reset link has been sent")
                );
            }

            Employee employee = employeeOpt.get();
            
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            
            // Create verification token for password reset
            VerificationToken resetVerificationToken = new VerificationToken();
            resetVerificationToken.setToken(resetToken);
            resetVerificationToken.setEmployee(employee);
            resetVerificationToken.setExpiryDate(LocalDateTime.now().plusHours(2)); // 2 hours for password reset
            resetVerificationToken.setVerified(false);
            tokenRepo.save(resetVerificationToken);
            
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            
            // Send password reset email
            // emailService.sendPasswordResetEmail(employee.getEmail(), employee.getName(), resetUrl);
            
            System.out.println("✅ Password reset email sent to: " + employee.getEmail());
            return ResponseEntity.ok(
                Collections.singletonMap("message", "If an account with this email exists, a password reset link has been sent")
            );
            
        } catch (Exception e) {
            System.err.println("❌ Forgot password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to process password reset request"));
        }
    }

    // ✅ Reset Password with Token
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, 
                                         @RequestBody Map<String, String> passwordData) {
        try {
            System.out.println("=== PASSWORD RESET ATTEMPT ===");
            System.out.println("Token: " + token);
            
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");
            
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "New password is required")
                );
            }
            
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Confirm password is required")
                );
            }
            
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Passwords do not match")
                );
            }
            
            // Validate password strength using the pattern from first class
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Password must be at least 8 characters with uppercase, lowercase, number and special character")
                );
            }
            
            Optional<VerificationToken> tokenOpt = tokenRepo.findByToken(token);
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Invalid or expired reset token")
                );
            }
            
            VerificationToken resetToken = tokenOpt.get();
            
            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Password reset token has expired")
                );
            }
            
            Employee employee = resetToken.getEmployee();
            if (employee == null) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Employee not found for this token")
                );
            }
            
            // Update password
            employee.setPassword(passwordEncoder.encode(newPassword));
            repo.save(employee);
            
            // Delete the used token
            tokenRepo.delete(resetToken);
            
            System.out.println("✅ Password reset successfully for: " + employee.getEmail());
            return ResponseEntity.ok(
                Collections.singletonMap("message", "Password reset successfully. You can now login with your new password.")
            );
            
        } catch (Exception e) {
            System.err.println("❌ Reset password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Password reset failed. Please try again."));
        }
    }

    // ✅ Update Employee Profile
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody Map<String, Object> updateData) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Authorization header missing or invalid")
                );
            }
            
            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            
            Optional<Employee> employeeOptional = repo.findByEmail(email);
            if (employeeOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Employee not found")
                );
            }
            
            Employee employee = employeeOptional.get();
            
            // Update allowed fields with validation
            if (updateData.containsKey("name")) {
                String name = (String) updateData.get("name");
                if (name != null && name.length() >= 2 && name.length() <= 50) {
                    employee.setName(name.trim());
                } else {
                    return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Name must be between 2 and 50 characters")
                    );
                }
            }
            
            if (updateData.containsKey("mobile")) {
                String mobile = (String) updateData.get("mobile");
                if (mobile != null && mobile.matches("^[6-9]\\d{9}$")) {
                    employee.setMobile(mobile.trim());
                } else {
                    return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Please provide a valid 10-digit mobile number")
                    );
                }
            }
            
            if (updateData.containsKey("address")) {
                String address = (String) updateData.get("address");
                if (address != null && address.length() >= 10 && address.length() <= 500) {
                    employee.setAddress(address.trim());
                } else {
                    return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Address must be between 10 and 500 characters")
                    );
                }
            }
            
            if (updateData.containsKey("gender")) {
                String gender = (String) updateData.get("gender");
                if (gender != null && gender.matches("^(MALE|FEMALE|OTHER)$")) {
                    employee.setGender(gender);
                } else {
                    return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Gender must be MALE, FEMALE, or OTHER")
                    );
                }
            }
            
            if (updateData.containsKey("profilePicture")) {
                String profilePicture = (String) updateData.get("profilePicture");
                if (profilePicture != null && profilePicture.length() > 0) {
                    employee.setProfilePicture(profilePicture);
                }
            }
            
            // Perform entity validation
            try {
                employee.validate();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", e.getMessage())
                );
            }
            
            Employee updatedEmployee = repo.save(employee);
            return ResponseEntity.ok(createSafeEmployeeResponse(updatedEmployee));
            
        } catch (Exception e) {
            System.err.println("Update profile error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Profile update failed. Please try again."));
        }
    }

    // ✅ Change Password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody Map<String, String> passwordData) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Authorization header missing or invalid")
                );
            }
            
            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            
            Optional<Employee> employeeOptional = repo.findByEmail(email);
            if (employeeOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Employee not found")
                );
            }
            
            Employee employee = employeeOptional.get();
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");
            
            if (currentPassword == null || currentPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Current password is required")
                );
            }
            
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "New password is required")
                );
            }
            
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Confirm password is required")
                );
            }
            
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "New password and confirm password do not match")
                );
            }
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, employee.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Current password is incorrect")
                );
            }
            
            // Validate new password strength using pattern from first class
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "New password must be at least 8 characters with uppercase, lowercase, number and special character")
                );
            }
            
            // Update password
            employee.setPassword(passwordEncoder.encode(newPassword));
            repo.save(employee);
            
            // Send password change notification email
            // emailService.sendPasswordChangeNotification(employee.getEmail(), employee.getName());
            
            return ResponseEntity.ok(
                Collections.singletonMap("message", "Password updated successfully")
            );
            
        } catch (Exception e) {
            System.err.println("Change password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Password change failed. Please try again."));
        }
    }

    // ✅ Check if email exists
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Email is required")
                );
            }
            
            boolean exists = repo.existsByEmail(email.trim().toLowerCase());
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("exists", exists);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Check email error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to check email"));
        }
    }

    // ============================
    // ADMIN ENDPOINTS
    // ============================

    // ✅ Get employee by ID (for internal use)
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            Optional<Employee> employeeOpt = repo.findById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Collections.singletonMap("error", "Employee not found")
                );
            }
            
            return ResponseEntity.ok(createSafeEmployeeResponse(employeeOpt.get()));
            
        } catch (Exception e) {
            System.err.println("Get employee by ID error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to retrieve employee"));
        }
    }

    // ✅ Get all employees (admin only)
    @GetMapping("/all")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<Employee> employees = repo.findAll();
            List<Map<String, Object>> safeEmployees = employees.stream()
                .map(this::createSafeEmployeeResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(safeEmployees);
            
        } catch (Exception e) {
            System.err.println("Get all employees error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to retrieve employees"));
        }
    }

    // ✅ Delete employee (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            Optional<Employee> employeeOpt = repo.findById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Collections.singletonMap("error", "Employee not found")
                );
            }
            
            repo.deleteById(id);
            return ResponseEntity.ok(
                Collections.singletonMap("message", "Employee deleted successfully")
            );
            
        } catch (Exception e) {
            System.err.println("Delete employee error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to delete employee"));
        }
    }

    // ✅ Verify employee account (admin only)
    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyEmployee(@PathVariable Long id) {
        try {
            Optional<Employee> employeeOpt = repo.findById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Collections.singletonMap("error", "Employee not found")
                );
            }
            
            Employee employee = employeeOpt.get();
            employee.setVerified(true);
            repo.save(employee);
            
            return ResponseEntity.ok(
                Collections.singletonMap("message", "Employee verified successfully")
            );
            
        } catch (Exception e) {
            System.err.println("Verify employee error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Failed to verify employee"));
        }
    }

    // ============================
    // LOGOUT (from first class)
    // ============================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(
            Collections.singletonMap("message", "Logged out successfully")
        );
    }

    // ============================
    // HELPER METHODS
    // ============================

    // Helper method to create safe employee response (without sensitive data)
    private Map<String, Object> createSafeEmployeeResponse(Employee employee) {
        Map<String, Object> safeEmployee = new HashMap<>();
        safeEmployee.put("id", employee.getId());
        safeEmployee.put("empId", employee.getEmpId());
        safeEmployee.put("name", employee.getName());
        safeEmployee.put("email", employee.getEmail());
        safeEmployee.put("mobile", employee.getMobile());
        safeEmployee.put("address", employee.getAddress());
        safeEmployee.put("gender", employee.getGender());
        safeEmployee.put("dateOfBirth", employee.getDateOfBirth());
        safeEmployee.put("aadharNumber", employee.getAadharNumber());
        safeEmployee.put("panNumber", employee.getPanNumber());
        safeEmployee.put("role", employee.getRole());
        safeEmployee.put("verified", employee.isVerified());
        safeEmployee.put("profilePicture", employee.getProfilePicture());
        safeEmployee.put("agreedToTerms", employee.getAgreedToTerms());
        return safeEmployee;
    }
}