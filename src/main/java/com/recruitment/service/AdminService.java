package com.recruitment.service;

import com.recruitment.entity.Admin;
import com.recruitment.repository.AdminRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Value("${system.admin.key}")
    private String systemAdminKey;

    @Autowired
    private AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Admin registerAdmin(Admin admin) {
        // validate system-level key before registering
        if (!systemAdminKey.equals(admin.getAdminKey())) {
            throw new RuntimeException("Invalid Admin Key");
        }

        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setAdminKey(passwordEncoder.encode(admin.getAdminKey()));

        return adminRepository.save(admin);
    }
    
 // AdminService.java
    public Optional<Admin> getAdminById(String adminId) {
        return adminRepository.findById(adminId);
    }

    public Admin loginAdmin(String email, String rawPassword, String rawAdminKey) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // check password
        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // check admin key
        if (!passwordEncoder.matches(rawAdminKey, admin.getAdminKey())) {
            throw new RuntimeException("Invalid admin key");
        }

        return admin;
    }
}