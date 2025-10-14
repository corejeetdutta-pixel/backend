package com.recruitment.controller;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.recruitment.entity.Employee;
import com.recruitment.entity.VerificationToken;
import com.recruitment.repository.EmployeeRepo;
import com.recruitment.repository.VerificationTokenRepository;
import com.recruitment.service.EmailService;
import com.recruitment.util.JwtUtil;

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
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ✅ Password regex pattern
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // ============================
    // REGISTER EMPLOYER
    // ============================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Employee emp) {
        try {
            System.out.println("=== EMPLOYER REGISTRATION STARTED ===");
            System.out.println("Email: " + emp.getEmail());

            if (repo.existsByEmail(emp.getEmail())) {
                return ResponseEntity.badRequest().body("Email already registered");
            }

            if (emp.getPassword() == null || !PASSWORD_PATTERN.matcher(emp.getPassword()).matches()) {
                return ResponseEntity.badRequest().body(
                        "Password must be at least 8 characters with uppercase, lowercase, number, and special character."
                );
            }

            emp.setPassword(passwordEncoder.encode(emp.getPassword()));
            emp.setAgreedToTerms(true);
            emp.setRole("EMPLOYER");
            emp.setVerified(false);

            Employee savedEmp = repo.save(emp);

            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setEmployee(savedEmp);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            verificationToken.setVerified(false);
            tokenRepo.save(verificationToken);

            // ✅ Use dynamic frontend URL (same pattern as user controller)
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            emailService.sendVerificationEmail(emp.getEmail(), emp.getName(), verificationUrl);

            System.out.println("✅ Verification email sent to: " + emp.getEmail());
            System.out.println("✅ Verification URL: " + verificationUrl);

            return ResponseEntity.ok("Registration successful. Please check your email to verify your account.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed. Please try again.");
        }
    }

    // ============================
    // RESEND VERIFICATION EMAIL
    // ============================
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            Optional<Employee> employeeOpt = repo.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("No employee found with this email.");
            }

            Employee employee = employeeOpt.get();

            if (employee.isVerified()) {
                return ResponseEntity.badRequest().body("Email already verified.");
            }

            Optional<VerificationToken> existingTokenOpt = tokenRepo.findByEmployee(employee);
            VerificationToken verificationToken;

            if (existingTokenOpt.isPresent() &&
                    existingTokenOpt.get().getExpiryDate().isAfter(LocalDateTime.now())) {
                verificationToken = existingTokenOpt.get();
            } else {
                existingTokenOpt.ifPresent(tokenRepo::delete);
                verificationToken = new VerificationToken();
                verificationToken.setToken(UUID.randomUUID().toString());
                verificationToken.setEmployee(employee);
                verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
                verificationToken.setVerified(false);
                tokenRepo.save(verificationToken);
            }

            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken.getToken();
            emailService.sendVerificationEmail(employee.getEmail(), employee.getName(), verificationUrl);

            return ResponseEntity.ok("Verification email resent successfully. Please check your inbox.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to resend verification email.");
        }
    }

    // ============================
    // VERIFY EMAIL
    // ============================
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            Optional<VerificationToken> opt = tokenRepo.findByToken(token);
            if (opt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired verification token."));
            }

            VerificationToken vt = opt.get();

            if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Verification token has expired."));
            }

            if (vt.isVerified()) {
                return ResponseEntity.ok(Map.of("message", "Email already verified."));
            }

            Employee employee = vt.getEmployee();
            if (employee == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Employee not found for this token."));
            }

            employee.setVerified(true);
            repo.save(employee);

            vt.setVerified(true);
            tokenRepo.save(vt);

            return ResponseEntity.ok(Map.of("message", "Email verified successfully! You can now login."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Email verification failed. Please try again."));
        }
    }

    // ============================
    // LOGIN
    // ============================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Employee emp) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(emp.getEmail(), emp.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(emp.getEmail());
            Optional<Employee> employeeOptional = repo.findByEmail(emp.getEmail());

            if (employeeOptional.isEmpty()) {
                return ResponseEntity.status(401).body("Employee not found");
            }

            Employee existingEmp = employeeOptional.get();

            if (!existingEmp.isVerified()) {
                return ResponseEntity.status(401)
                        .body("Email not verified. Please check your email for the verification link.");
            }

            String jwt = jwtUtil.generateToken(userDetails, existingEmp.getEmpId(), "EMPLOYEE");
            emailService.sendLoginNotification(existingEmp.getEmail(), existingEmp.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwt);
            response.put("user", existingEmp);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid credentials: " + e.getMessage());
        }
    }

    // ============================
    // CURRENT EMPLOYEE
    // ============================
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

    // ============================
    // LOGOUT
    // ============================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}
