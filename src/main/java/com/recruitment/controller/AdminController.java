package com.recruitment.controller;

import com.recruitment.entity.Admin;
import com.recruitment.entity.Employee;
import com.recruitment.entity.User;
import com.recruitment.repository.EmployeeRepo;
import com.recruitment.repository.UserRepo;
import com.recruitment.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private EmployeeRepo employeeRepo;

    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody Admin admin) {
        try {
            Admin savedAdmin = adminService.registerAdmin(admin);
            return ResponseEntity.ok(savedAdmin);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            String adminKey = loginRequest.get("adminKey");

            Admin admin = adminService.loginAdmin(email, password, adminKey);
            
            // Store admin ID in session
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("adminId", admin.getAdminId());
            
            // Create response with admin data (excluding sensitive information)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("admin", Map.of(
                "id", admin.getAdminId(),
                "name", admin.getName(),
                "email", admin.getEmail()
            ));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    @GetMapping("/current-admin")
    public ResponseEntity<?> currentAdmin(HttpSession session) {
        String adminId = (String) session.getAttribute("adminId");
        if (adminId == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        
        // Fetch admin from database using the ID stored in session
        Optional<Admin> adminOptional = adminService.getAdminById(adminId);
        if (!adminOptional.isPresent()) {
            return ResponseEntity.status(401).body("Admin not found");
        }
        
        Admin admin = adminOptional.get();
        
        // Return admin data without sensitive information
        Map<String, Object> response = new HashMap<>();
        response.put("id", admin.getAdminId());
        response.put("name", admin.getName());
        response.put("email", admin.getEmail());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutAdmin(HttpSession session) {
    	System.out.println("admin log out is triggered");
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
    
 // Add this to your AdminController.java
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
        	System.out.println("admin user fetch kar raha hai...");
            List<User> users = userRepo.findAll();
            // Remove sensitive information
            users.forEach(user -> {
                
            });
            System.out.println("admin fetch ho gya");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
        	System.out.println("admin me user fetch nhi ho raha hai");
            return ResponseEntity.status(500).body(null);
        }
    }
    
 // Add this to your AdminController.java
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            Optional<User> userOptional = userRepo.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            userRepo.deleteById(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting user: " + e.getMessage());
        }
    }
    
 // Add these to your AdminController.java
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
        	System.out.println("admin emp fetch kar raha hai...");
            List<Employee> employees = employeeRepo.findAll();
            // Remove sensitive information
            employees.forEach(employee -> {
                
            });
            System.out.println("admin emp fetch ho gya...");
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
        	System.out.println("admin me emp fetch nhi ho raha hai");
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long employeeId) {
        try {
            Optional<Employee> employeeOptional = employeeRepo.findById(employeeId);
            if (employeeOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            employeeRepo.deleteById(employeeId);
            return ResponseEntity.ok("Employee deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting employee: " + e.getMessage());
        }
    }
}