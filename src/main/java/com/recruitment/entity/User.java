package com.recruitment.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name="users")
public class User implements Serializable {

    private static final long serialVersionUID = 2L;
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
    private String department;
    private String passoutYear;
    private String dob; // New field
    private String experience; // New field
    private String linkedin; // New field
    private String github; // New field

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills = new ArrayList<>();

    private String role = "user";

    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String resume;
    
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;
    
    @Column(name = "verification_token")
    private String verificationToken;
    
    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

    public User() {
        this.userId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Updated constructor with new fields
   
    
    
    
 // Getters and setters for new fields
    public String getDob() { return dob; }
	public User(Long id, String userId, String name, String email, String password, String mobile, String address,
			String gender, String qualification, String department, String passoutYear, String dob, String experience,
			String linkedin, String github, List<String> skills, String role, String profilePicture, String resume,
			boolean emailVerified, String verificationToken, LocalDateTime tokenExpiryDate) {
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
		this.department = department;
		this.passoutYear = passoutYear;
		this.dob = dob;
		this.experience = experience;
		this.linkedin = linkedin;
		this.github = github;
		this.skills = skills;
		this.role = role;
		this.profilePicture = profilePicture;
		this.resume = resume;
		this.emailVerified = emailVerified;
		this.verificationToken = verificationToken;
		this.tokenExpiryDate = tokenExpiryDate;
	}

	public void setDob(String dob) { this.dob = dob; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public String getPassoutYear() {
		return passoutYear;
	}

	public void setPassoutYear(String passoutYear) {
		this.passoutYear = passoutYear;
	}

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public LocalDateTime getTokenExpiryDate() {
		return tokenExpiryDate;
	}

	public void setTokenExpiryDate(LocalDateTime tokenExpiryDate) {
		this.tokenExpiryDate = tokenExpiryDate;
	}
	
	
	
	
    
    

    // Existing getters and setters...
    // [Keep all existing getters/setters and add toString if needed]
}