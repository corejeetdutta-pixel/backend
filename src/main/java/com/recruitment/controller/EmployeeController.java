package com.recruitment.controller;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "https://1c.atract.in", allowCredentials = "true")
@RequestMapping("/auth/employee")
public class EmployeeController {

    @Autowired
    private EmployeeRepo repo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository tokenRepo;

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
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character.");
        }

        emp.setAgreedToTerms(true);
        emp.setRole("Employer");
        emp.setVerified(false);
        repo.save(emp);

        // ✅ Create and save verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setEmployee(emp);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepo.save(verificationToken);

        // ✅ Send verification email
        String link = "https://recruitment-backend-beta-test.onrender.com/auth/employee/verify?token=" + token;
        emailService.sendSimpleMessage(emp.getEmail(), "Email Verification", "Please verify your email by clicking the link: " + link);

        return ResponseEntity.ok("Registration successful. Please check your email to verify your account.");
    }

    // ✅ Verify Email
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<VerificationToken> opt = tokenRepo.findByToken(token);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }

        VerificationToken vt = opt.get();
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Verification token has expired.");
        }

        Employee employee = vt.getEmployee();
        if (employee.isVerified()) {
            return ResponseEntity.ok("Email already verified.");
        }

        employee.setVerified(true);
        repo.save(employee);

        vt.setVerified(true);
        tokenRepo.save(vt);

        return ResponseEntity.ok("Email verified successfully. You can now login.");
    }

    // ✅ Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Employee emp, HttpSession session) {
        Optional<Employee> optionalUser = repo.findByEmail(emp.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Employee not found.");
        }

        Employee existingEmp = optionalUser.get();

        if (!existingEmp.isVerified()) {
            return ResponseEntity.status(401).body("Email not verified. Please check your inbox.");
        }

        if (!existingEmp.getPassword().equals(emp.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password.");
        }

        // ✅ Session setup
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
        session.setAttribute("emp", existingEmp);

        // ✅ Send login email
        emailService.sendLoginNotification(existingEmp.getEmail(), existingEmp.getName());

        // ✅ Return response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("user", existingEmp);
        response.put("Access-Control-Allow-Credentials", "true");
        response.put("Access-Control-Expose-Headers", "Set-Cookie");

        return ResponseEntity.ok(response);
    }

    // ✅ Get current logged-in employee
    @GetMapping("/current-employee")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Employee emp = (Employee) session.getAttribute("emp");
        if (emp == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        return ResponseEntity.ok(emp);
    }

    // ✅ Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
}
