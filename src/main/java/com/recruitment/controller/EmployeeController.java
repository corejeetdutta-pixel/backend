package com.recruitment.controller;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import com.recruitment.entity.Employee;
import com.recruitment.repository.EmployeeRepo;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth/employee")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EmployeeController {

    @Autowired
    private EmployeeRepo repo;

    // ✅ Password regex: min 8 chars, 1 upper, 1 lower, 1 digit, 1 special char
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
            return ResponseEntity.badRequest().body(
                "Password must be at least 8 characters long and include " +
                "an uppercase letter, a lowercase letter, a digit, and a special character."
            );
        }

        // Set default value for agreedToTerms if not provided
        if (emp.getAgreedToTerms() == null) {
            emp.setAgreedToTerms(true); // Default to true for registration
        }

        repo.save(emp);
        return ResponseEntity.ok("Employee registered successfully");
    }

    // ✅ Login employee and set session
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Employee emp, HttpSession session) {
        System.out.println("Login is triggered");

        Optional<Employee> optionalUser = repo.findByEmail(emp.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Employee not found");
        }

        Employee existingEmp = optionalUser.get();
        if (!existingEmp.getPassword().equals(emp.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        // Set session timeout to 30 minutes
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("emp", existingEmp); // Set session key
        
        // Create response with CORS headers
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("user", existingEmp);
        response.put("Access-Control-Allow-Credentials", "true");
        response.put("Access-Control-Expose-Headers", "Set-Cookie");
        
        return ResponseEntity.ok(response);    // Return response with CORS headers
    }

    // ✅ Get current logged-in employee
    @GetMapping("/current-employee")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Employee emp = (Employee) session.getAttribute("emp");
        if (emp == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        System.out.println("EmpId : " + emp.getEmpId());
        return ResponseEntity.ok(emp);
    }

    // ✅ Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
}
