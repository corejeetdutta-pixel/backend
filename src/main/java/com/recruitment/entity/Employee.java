package com.recruitment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "employees") // Added table name for consistency
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, updatable = false)
    private String empId;
    
    private boolean verified = false;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name can only contain letters, spaces, apostrophes, hyphens, and dots")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Column(length = 100) // Increased length for encoded passwords
    private String password;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please provide a valid 10-digit mobile number starting with 6-9")
    private String mobile;
    
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    private String address;
    
    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Aadhar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhar number must be exactly 12 digits")
    private String aadharNumber;
    
    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number must be in format ABCDE1234F")
    private String panNumber;
    
    @Column(columnDefinition = "TEXT")
    private String profilePicture;
    
    private String role = "EMPLOYER";
    
    @NotNull(message = "You must agree to terms and conditions")
    private Boolean agreedToTerms = false;
    
    @OneToMany(mappedBy = "postedBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Job> postedJobs = new ArrayList<>();

    // Auto-generate EmpId before persisting
    @PrePersist
    public void generateEmpId() {
        if (this.empId == null) {
            this.empId = "EMP" + System.currentTimeMillis() + 
                (int)(Math.random() * 1000);
        }
    }
    
    // Custom validation method
    public void validate() {
        // Age validation (must be at least 18 years old)
        if (dateOfBirth != null) {
            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            if (age < 18) {
                throw new IllegalArgumentException("Employee must be at least 18 years old");
            }
            if (age > 100) {
                throw new IllegalArgumentException("Please provide a valid date of birth");
            }
        }
        
        // Aadhar validation (basic checksum validation)
        if (aadharNumber != null && aadharNumber.length() == 12) {
            try {
                Long.parseLong(aadharNumber);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Aadhar number must contain only digits");
            }
        }
    }

    // Constructors
    public Employee() {}

    public Employee(Long id, String empId, boolean verified, String name, String email, 
                   String password, String mobile, String address, String gender, 
                   LocalDate dateOfBirth, String aadharNumber, String panNumber, 
                   String profilePicture, String role, Boolean agreedToTerms, List<Job> postedJobs) {
        super();
        this.id = id;
        this.empId = empId;
        this.verified = verified;
        this.name = name;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.address = address;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.aadharNumber = aadharNumber;
        this.panNumber = panNumber;
        this.profilePicture = profilePicture;
        this.role = role;
        this.agreedToTerms = agreedToTerms;
        this.postedJobs = postedJobs;
    }

    // Getters and Setters (same as before)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

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

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getAgreedToTerms() { return agreedToTerms; }
    public void setAgreedToTerms(Boolean agreedToTerms) { this.agreedToTerms = agreedToTerms; }

    public List<Job> getPostedJobs() { return postedJobs; }
    public void setPostedJobs(List<Job> postedJobs) { this.postedJobs = postedJobs; }

    public static long getSerialversionuid() { return serialVersionUID; }
}