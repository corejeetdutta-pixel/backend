package com.recruitment.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;

@Entity
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id 
    @GeneratedValue
    private Long id;
    private String empId;
    private boolean verified = false;
    private String name;
    private String email;
    private String password;
    private String mobile;
    private String address;
    private String gender;
    private LocalDate dateOfBirth;
    private String aadharNumber;
    private String panNumber;
    private String profilePicture;
    private String role = "EMPLOYER";
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getAgreedToTerms() {
        return agreedToTerms;
    }

    public void setAgreedToTerms(Boolean agreedToTerms) {
        this.agreedToTerms = agreedToTerms;
    }

    public List<Job> getPostedJobs() {
        return postedJobs;
    }

    public void setPostedJobs(List<Job> postedJobs) {
        this.postedJobs = postedJobs;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}