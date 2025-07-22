package com.recruitment.controller;


import com.recruitment.dto.UserDto;
import com.recruitment.entity.User;
import com.recruitment.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth/user")
@CrossOrigin(origins = "https://1c.atract.in/", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserRepo repo;

    private final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // Register
    @PostMapping("/register")
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
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        System.out.println("Login endpoint called");
        System.out.println(user.getEmail()+ " "+ user.getPassword());

        Optional<User> optionalUser = repo.findByEmail(user.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        User existingUser = optionalUser.get();
        if (!existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        session.setAttribute("user", existingUser);
        return ResponseEntity.ok("Login successful");
    }
    
 // Update user profile
    @PutMapping("/update/{id}")
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
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        return ResponseEntity.ok(user);
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
}
