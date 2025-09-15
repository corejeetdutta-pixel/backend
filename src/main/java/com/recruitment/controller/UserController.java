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
        if (repo.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        if (!PASSWORD_PATTERN.matcher(dto.getPassword()).matches()) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long with one uppercase letter, one lowercase letter, one digit, and one special character.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Encode password
        user.setMobile(dto.getMobile());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setQualification(dto.getQualification());
        user.setPassoutYear(dto.getPassoutYear());
        user.setSkills(dto.getSkills());
        user.setProfilePicture(dto.getProfilePicture());
        user.setResume(dto.getResume());
        user.setDob(dto.getDob());
        user.setExperience(dto.getExperience());
        user.setLinkedin(dto.getLinkedin());
        user.setGithub(dto.getGithub());
        user.setRole("USER");
        
        // Generate verification token and set expiry
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
        user.setEmailVerified(false);

        repo.save(user);
        
        // Send verification email - use frontend URL - FIXED URL FORMAT
        String frontendUrl = "https://backend-n4w7.onrender.com";
        String link = frontendUrl + "/auth/user/verify-email?token=" + verificationToken;
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), link);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
        
        return ResponseEntity.ok("User registered successfully. Please check your email for verification.");
    }

    // Verify Email
 // Updated verifyEmail endpoint
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOptional = repo.findByVerificationToken(token);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }
        
        User user = userOptional.get();
        
        if (user.isEmailVerified()) {
            return ResponseEntity.ok("Email already verified.");
        }
        
        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Verification token has expired.");
        }
        
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiryDate(null);
        repo.save(user);
        
        // Return success message instead of redirecting
        return ResponseEntity.ok("Email verified successfully. You can now login.");
    }

    // Resend Verification Email
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        Optional<User> userOptional = repo.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        
        User user = userOptional.get();
        
        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest().body("Email is already verified.");
        }
        
        // Generate new token
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
        repo.save(user);
        
        // Resend verification email - FIXED URL FORMAT
        try {
            String frontendUrl = "https://backend-n4w7.onrender.com";
            String link = frontendUrl + "/auth/user/verify-email?token=" + token;
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), link);
            return ResponseEntity.ok("Verification email sent successfully.");
        } catch (Exception e) {
            System.out.println("resend part not working");
            return ResponseEntity.status(500).body("Failed to send verification email.");
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
                return ResponseEntity.status(401).body("Email not verified. Please check your email for verification link.");
            }
            
            // Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails, existingUser.getUserId(), "USER");
            
            // Create a clean user object without sensitive data
            User responseUser = new User();
            responseUser.setId(existingUser.getId());
            responseUser.setUserId(existingUser.getUserId());
            responseUser.setName(existingUser.getName());
            responseUser.setEmail(existingUser.getEmail());
            responseUser.setRole(existingUser.getRole());
            responseUser.setAddress(existingUser.getAddress());
            responseUser.setDob(existingUser.getDob());
            responseUser.setExperience(existingUser.getExperience());
            responseUser.setLinkedin(existingUser.getLinkedin());
            responseUser.setGithub(existingUser.getGithub());
            responseUser.setSkills(existingUser.getSkills());
            responseUser.setProfilePicture(existingUser.getProfilePicture());
            
            // Return response with token
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwt);
            response.put("user", responseUser);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
    
    // Update user profile
    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> optionalUser = repo.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = optionalUser.get();
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setMobile(updatedUser.getMobile());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setDob(updatedUser.getDob());
        existingUser.setExperience(updatedUser.getExperience());
        existingUser.setLinkedin(updatedUser.getLinkedin());
        existingUser.setGithub(updatedUser.getGithub());
        existingUser.setSkills(updatedUser.getSkills());
        existingUser.setProfilePicture(updatedUser.getProfilePicture());
        repo.save(existingUser);

        return ResponseEntity.ok(existingUser);
    }
    
    @GetMapping("/{userId}/resume")
    public ResponseEntity<?> getUserResume(@PathVariable String userId) {
        // Fetch resume from your DB
        String resume = repo.findByUserId(userId)
            .map(u -> u.getResume())
            .orElse("Sample default resume text here.");
        return ResponseEntity.ok(Map.of("resume", resume));
    }

    // Get current logged-in user using JWT
    @GetMapping("/current-user")
    public ResponseEntity<?> currentUser(@RequestHeader("Authorization") String authHeader) {
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
        
        return ResponseEntity.ok(response);
    }

    // Logout - client should remove token
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