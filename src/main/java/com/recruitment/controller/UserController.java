package com.recruitment.controller;

import com.recruitment.dto.UserDto;
import com.recruitment.entity.User;
import com.recruitment.repository.UserRepo;
import com.recruitment.service.EmailService;
import com.recruitment.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth/user")
public class UserController {

    @Autowired
    private UserRepo repo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.recruitment.service.CustomUserDetailsService userDetailsService;

    private final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // Register
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody UserDto dto) {
        try {
            System.out.println("Registration attempt for email: " + dto.getEmail());
            
            if (repo.existsByEmail(dto.getEmail())) {
                System.out.println("Email already exists: " + dto.getEmail());
                return ResponseEntity.badRequest().body("Email already registered");
            }
            
            if (dto.getPassword() == null || !PASSWORD_PATTERN.matcher(dto.getPassword()).matches()) {
                System.out.println("Password validation failed for: " + dto.getEmail());
                return ResponseEntity.badRequest().body("Password must be at least 8 characters long with one uppercase letter, one lowercase letter, one digit, and one special character.");
            }

            User user = new User();
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setMobile(dto.getMobile());
            user.setAddress(dto.getAddress());
            user.setGender(dto.getGender());
            user.setQualification(dto.getQualification());
            user.setPassoutYear(dto.getPassoutYear());
            user.setSkills(dto.getSkills() != null ? dto.getSkills() : new ArrayList<>());
            user.setProfilePicture(dto.getProfilePicture());
            user.setResume(dto.getResume());
            user.setDob(dto.getDob());
            user.setExperience(dto.getExperience());
            user.setLinkedin(dto.getLinkedin());
            user.setGithub(dto.getGithub());
            user.setRole("USER");
            
            // Generate user ID
            user.setUserId(UUID.randomUUID().toString());
            
            // Generate verification token and set expiry
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
            user.setEmailVerified(false);

            User savedUser = repo.save(user);
            System.out.println("User registered successfully with ID: " + savedUser.getUserId());
            System.out.println("Verification token generated: " + verificationToken);
            
            // Send verification email - FIXED URL
            try {
                // Use your actual frontend domain here
                String frontendUrl = "https://backend-n4w7.onrender.com"; //https://backend-n4w7.onrender.com
                String verificationUrl = frontendUrl + "/auth/user/verify-email?token=" + verificationToken;
                
                emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationUrl);
                System.out.println("Verification email sent to: " + user.getEmail());
                System.out.println("Verification URL: " + verificationUrl);
            } catch (Exception e) {
                System.err.println("Failed to send verification email: " + e.getMessage());
                // Don't fail registration if email fails
            }
            
            return ResponseEntity.ok("User registered successfully. Please check your email for verification.");
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Registration failed. Please try again.");
        }
    }

    // Verify Email - FIXED with better debugging
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            System.out.println("=== EMAIL VERIFICATION STARTED ===");
            System.out.println("Received token: " + token);
            
            // Log all tokens in database for debugging
            List<User> allUsers = repo.findAll();
            System.out.println("=== ALL TOKENS IN DATABASE ===");
            for (User u : allUsers) {
                if (u.getVerificationToken() != null) {
                    System.out.println("User: " + u.getEmail() + " | Token: " + u.getVerificationToken() + 
                                     " | Verified: " + u.isEmailVerified() +
                                     " | Expiry: " + u.getTokenExpiryDate());
                }
            }
            System.out.println("=== END TOKENS LIST ===");
            
            Optional<User> userOptional = repo.findByVerificationToken(token);
            
            if (userOptional.isEmpty()) {
                System.out.println("❌ ERROR: Invalid verification token. Token not found in database.");
                System.out.println("Available tokens: ");
                allUsers.stream()
                    .filter(u -> u.getVerificationToken() != null)
                    .forEach(u -> System.out.println(" - " + u.getVerificationToken()));
                    
                return ResponseEntity.badRequest().body("Invalid verification token.");
            }
            
            User user = userOptional.get();
            System.out.println("✅ User found: " + user.getEmail());
            
            if (user.isEmailVerified()) {
                System.out.println("ℹ️ Email already verified: " + user.getEmail());
                return ResponseEntity.ok("Email already verified.");
            }
            
            if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
                System.out.println("❌ Token expired for: " + user.getEmail());
                System.out.println("Token expiry: " + user.getTokenExpiryDate());
                System.out.println("Current time: " + LocalDateTime.now());
                return ResponseEntity.badRequest().body("Verification token has expired.");
            }
            
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setTokenExpiryDate(null);
            repo.save(user);
            
            System.out.println("🎉 Email verified successfully for: " + user.getEmail());
            return ResponseEntity.ok("Email verified successfully. You can now login.");
            
        } catch (Exception e) {
            System.err.println("❌ Email verification error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Email verification failed. Please try again.");
        }
    }

    // Resend Verification Email - FIXED URL
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            System.out.println("Resend verification attempt for: " + email);
            
            Optional<User> userOptional = repo.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                System.out.println("User not found: " + email);
                return ResponseEntity.badRequest().body("User not found.");
            }
            
            User user = userOptional.get();
            
            if (user.isEmailVerified()) {
                System.out.println("Email already verified: " + email);
                return ResponseEntity.badRequest().body("Email is already verified.");
            }
            
            // Generate new token
            String newToken = UUID.randomUUID().toString();
            user.setVerificationToken(newToken);
            user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
            repo.save(user);
            
            // Resend verification email with correct URL
            try {
                String frontendUrl = "https://backend-n4w7.onrender.com";//https://backend-n4w7.onrender.com
                String verificationUrl = frontendUrl + "/auth/user/verify-email?token=" + newToken;
                
                emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationUrl);
                System.out.println("Verification email resent to: " + user.getEmail());
                System.out.println("New verification URL: " + verificationUrl);
                return ResponseEntity.ok("Verification email sent successfully.");
            } catch (Exception e) {
                System.err.println("Failed to send verification email: " + e.getMessage());
                return ResponseEntity.status(500).body("Failed to send verification email.");
            }
            
        } catch (Exception e) {
            System.err.println("Resend verification error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to resend verification email.");
        }
    }

    // Login with JWT
    @PostMapping("/login")
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            System.out.println("Login attempt for email: " + user.getEmail());
            
            if (user.getEmail() == null || user.getPassword() == null) {
                return ResponseEntity.badRequest().body("Email and password are required");
            }

            // Authenticate
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
            
            // Load user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            final User existingUser = repo.findByEmail(user.getEmail()).orElseThrow();
            
            // Check if email is verified
            if (!existingUser.isEmailVerified()) {
                System.out.println("Email not verified for: " + user.getEmail());
                return ResponseEntity.status(401).body("Email not verified. Please check your email for verification link.");
            }
            
            // Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails, existingUser.getUserId(), "USER");
            
            // Create a clean user object without sensitive data
            Map<String, Object> responseUser = new HashMap<>();
            responseUser.put("id", existingUser.getId());
            responseUser.put("userId", existingUser.getUserId());
            responseUser.put("name", existingUser.getName());
            responseUser.put("email", existingUser.getEmail());
            responseUser.put("role", existingUser.getRole());
            responseUser.put("address", existingUser.getAddress());
            responseUser.put("dob", existingUser.getDob());
            responseUser.put("experience", existingUser.getExperience());
            responseUser.put("linkedin", existingUser.getLinkedin());
            responseUser.put("github", existingUser.getGithub());
            responseUser.put("skills", existingUser.getSkills());
            responseUser.put("profilePicture", existingUser.getProfilePicture());
            responseUser.put("mobile", existingUser.getMobile());
            responseUser.put("qualification", existingUser.getQualification());
            responseUser.put("passoutYear", existingUser.getPassoutYear());
            responseUser.put("gender", existingUser.getGender());
            
            // Return response with token
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwt);
            response.put("user", responseUser);
            
            System.out.println("Login successful for: " + user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
    
    // Get current logged-in user using JWT
    @GetMapping("/current-user")
    public ResponseEntity<?> currentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Authorization header missing or invalid");
            }
            
            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            
            Optional<User> userOptional = repo.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(401).body("User not found");
            }
            
            User user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("userId", user.getUserId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("address", user.getAddress());
            response.put("dob", user.getDob());
            response.put("experience", user.getExperience());
            response.put("linkedin", user.getLinkedin());
            response.put("github", user.getGithub());
            response.put("skills", user.getSkills());
            response.put("profilePicture", user.getProfilePicture());
            response.put("mobile", user.getMobile());
            response.put("qualification", user.getQualification());
            response.put("passoutYear", user.getPassoutYear());
            response.put("gender", user.getGender());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Current user error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(createSuccessResponse("Logout successful"));
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
    
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}