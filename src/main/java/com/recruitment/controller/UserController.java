package com.recruitment.controller;


import com.recruitment.dto.UserDto;
import com.recruitment.entity.User;
import com.recruitment.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth/user")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserRepo repo;

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
        user.setPassword(dto.getPassword());
        user.setMobile(dto.getMobile());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setQualification(dto.getQualification());
        user.setPassoutYear(dto.getPassoutYear());
        user.setSkills(dto.getSkills());
        user.setProfilePicture(dto.getProfilePicture());
        user.setResume(dto.getResume());

        repo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }


    // Login
    @PostMapping("/login")
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        try {
            System.out.println("Login attempt for email: " + user.getEmail());
            
            if (user.getEmail() == null || user.getPassword() == null) {
                return ResponseEntity.badRequest().body("Email and password are required");
            }

            Optional<User> optionalUser = repo.findByEmail(user.getEmail().trim());
            if (optionalUser.isEmpty()) {
                System.out.println("Login failed: User not found with email " + user.getEmail());
                return ResponseEntity.status(401).body("Invalid email or password");
            }

            User existingUser = optionalUser.get();
            if (!existingUser.getPassword().equals(user.getPassword())) {
                System.out.println("Login failed: Invalid password for user " + user.getEmail());
                return ResponseEntity.status(401).body("Invalid email or password");
            }

            // Create a clean user object without sensitive data for the session
            User sessionUser = new User();
            sessionUser.setId(existingUser.getId());
            sessionUser.setUserId(existingUser.getUserId());
            sessionUser.setName(existingUser.getName());
            sessionUser.setEmail(existingUser.getEmail());
            sessionUser.setRole(existingUser.getRole());

            // Set session timeout to 30 minutes
            session.setMaxInactiveInterval(30 * 60);
            // Set session attributes
            session.setAttribute("user", sessionUser);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            System.out.println("Login successful for user: " + existingUser.getEmail() + " (ID: " + existingUser.getId() + ")");
            
            // Return user data (without sensitive information) with session cookie
            Map<String, Object> response = new HashMap<>();
            
            // Add CORS headers to the response
            response.put("Access-Control-Allow-Credentials", "true");
            response.put("Access-Control-Expose-Headers", "Set-Cookie");
            response.put("message", "Login successful");
            response.put("user", sessionUser);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred during login");
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
        existingUser.setProfilePicture(updatedUser.getProfilePicture());
        repo.save(existingUser);

        return ResponseEntity.ok(existingUser);
    }
    
    @GetMapping("/{userId}/resume")
    public ResponseEntity<?> getUserResume(@PathVariable String userId) {
    	System.out.println("resume fetch is triggered");
        // Fetch resume from your DB (you may need to adjust this)
        String resume = repo.findByUserId(userId)
            .map(u -> u.getResume())
            .orElse("Sample default resume text here.");
        return ResponseEntity.ok(Map.of("resume", resume));
    }



    // Get current logged-in user
    @GetMapping("/current-user")
    public ResponseEntity<?> currentUser(HttpSession session) {
        try {
            User sessionUser = (User) session.getAttribute("user");
            if (sessionUser == null || sessionUser.getId() == null) {
                System.out.println("Current user check: No valid session found");
                return ResponseEntity.status(401).body(createErrorResponse("Not logged in"));
            }
            
            // Refresh session timeout
            session.setMaxInactiveInterval(30 * 60);
            
            // Get fresh user data from database
            Optional<User> optionalUser = repo.findById(sessionUser.getId());
            if (optionalUser.isEmpty()) {
                System.out.println("User not found in database, invalidating session");
                session.invalidate();
                return ResponseEntity.status(401).body(createErrorResponse("User not found"));
            }
            
            User user = optionalUser.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("userId", user.getUserId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("sessionId", session.getId());
            response.put("sessionCreated", new Date(session.getCreationTime()));
            response.put("lastAccessed", new Date(session.getLastAccessedTime()));
            response.put("maxInactiveInterval", session.getMaxInactiveInterval());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in current user endpoint: " + e.getMessage());
            return ResponseEntity.status(500).body(createErrorResponse("An error occurred"));
        }
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                System.out.println("Logging out user: " + user.getEmail() + " (ID: " + user.getId() + ")");
            } else {
                System.out.println("Logout called with no active user session");
            }
            
            // Invalidate the session
            session.invalidate();
            
            // Clear any session cookies by setting an expired cookie
            ResponseCookie cookie = ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(true)  // Enable in production with HTTPS
                .path("/")
                .maxAge(0)     // Immediately expire the cookie
                .build();
                
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(createSuccessResponse("Logout successful"));
                
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(createErrorResponse("An error occurred during logout"));
        }
    }
    
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
