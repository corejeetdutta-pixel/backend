package com.recruitment.controller;

import com.recruitment.dto.UserDto;
import com.recruitment.entity.User;
import com.recruitment.repository.UserRepo;
import com.recruitment.service.EmailService;
import com.recruitment.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // ✅ Frontend URL injected from application.properties
    @Value("${spring.mail.frontendUrl:http://localhost:5173}")
    private String frontendUrl;

    private final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // ============================
    // REGISTER
    // ============================
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody UserDto dto) {
        try {
            System.out.println("=== USER REGISTRATION STARTED ===");
            System.out.println("Email: " + dto.getEmail());

            if (repo.existsByEmail(dto.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
            }

            if (dto.getPassword() == null || !PASSWORD_PATTERN.matcher(dto.getPassword()).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Password must be at least 8 characters long with one uppercase letter, one lowercase letter, one digit, and one special character."
                ));
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
            user.setUserId(UUID.randomUUID().toString());

            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
            user.setEmailVerified(false);

            repo.save(user);

            // ✅ Use frontend URL dynamically
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationUrl);

            System.out.println("✅ Verification email sent to: " + user.getEmail());
            System.out.println("✅ Verification URL: " + verificationUrl);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully. Please check your email for verification."
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Registration failed. Please try again."));
        }
    }

    // ============================
    // VERIFY EMAIL
    // ============================
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            System.out.println("=== USER EMAIL VERIFICATION ===");
            System.out.println("Token received: " + token);

            Optional<User> userOptional = repo.findByVerificationToken(token);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired verification token."));
            }

            User user = userOptional.get();

            if (user.isEmailVerified()) {
                return ResponseEntity.ok(Map.of("message", "Email already verified."));
            }

            if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Verification token has expired."));
            }

            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setTokenExpiryDate(null);
            repo.save(user);

            return ResponseEntity.ok(Map.of("message", "Email verified successfully! You can now login."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Email verification failed. Please try again."));
        }
    }

    // ============================
    // RESEND VERIFICATION EMAIL
    // ============================
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            System.out.println("=== RESEND VERIFICATION FOR USER ===");
            System.out.println("Email: " + email);

            Optional<User> userOptional = repo.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
            }

            User user = userOptional.get();
            if (user.isEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is already verified."));
            }

            String newToken = UUID.randomUUID().toString();
            user.setVerificationToken(newToken);
            user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
            repo.save(user);

            String verificationUrl = frontendUrl + "/verify-email?token=" + newToken;
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationUrl);

            System.out.println("✅ Verification email resent to: " + user.getEmail());
            System.out.println("✅ Verification URL: " + verificationUrl);

            return ResponseEntity.ok(Map.of("message", "Verification email sent successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to resend verification email."));
        }
    }

    // ============================
    // LOGIN
    // ============================
    @PostMapping("/login")
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            System.out.println("Login attempt for email: " + user.getEmail());

            if (user.getEmail() == null || user.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required."));
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            final User existingUser = repo.findByEmail(user.getEmail()).orElseThrow();

            if (!existingUser.isEmailVerified()) {
                return ResponseEntity.status(401).body(Map.of(
                        "message", "Email not verified. Please check your email for verification link."
                ));
            }

            final String jwt = jwtUtil.generateToken(userDetails, existingUser.getUserId(), "USER");

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

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", jwt);
            response.put("user", responseUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password."));
        }
    }

    // ============================
    // CURRENT USER
    // ============================
    @GetMapping("/current-user")
    public ResponseEntity<?> currentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("message", "Authorization header missing or invalid."));
            }

            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractUsername(jwt);

            Optional<User> userOptional = repo.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("message", "User not found."));
            }

            return ResponseEntity.ok(userOptional.get());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token."));
        }
    }

    // ============================
    // LOGOUT
    // ============================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
