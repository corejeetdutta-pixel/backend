package com.recruitment.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    private String name;
    private String email;
    private String password;
    private String mobile;
    private String address;
    private String gender;
    private String qualification;
    private String passoutYear;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills = new ArrayList<>();

    private String role = "user";

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profilePicture;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String resume;

    public User() {
        this.userId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    

    public User(Long id, String userId, String name, String email, String password, String mobile, String address,
            String gender, String qualification, String passoutYear, List<String> skills, String role,
            String profilePicture, String resume) {
        super();
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.address = address;
        this.gender = gender;
        this.qualification = qualification;
        this.passoutYear = passoutYear;
        this.skills = skills;
        this.role = role;
        this.profilePicture = profilePicture;
        this.resume = resume;
    }



    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getPassoutYear() { return passoutYear; }
    public void setPassoutYear(String passoutYear) { this.passoutYear = passoutYear; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }
}
