package com.recruitment.dto;

import com.recruitment.entity.Application;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ApplicantDTO.java (Optional - for better structure)
public class ApplicantDTO {
    private Long applicationId;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private byte[] resume;
    private LocalDate appliedAt;
    private String status;

    // Constructors
    public ApplicantDTO() {}

    public ApplicantDTO(Application application) {
        this.applicationId = application.getId();
        this.userId = application.getUser().getUserId();
        this.name = application.getUser().getName();
        this.email = application.getUser().getEmail();
        this.phone = application.getUser().getMobile();
        this.resume = application.getUser().getResume();
        this.appliedAt = application.getAppliedAt();
        this.status = application.getStatus();
    }

    // Getters and setters

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public byte[] getResume() {
        return resume;
    }

    public void setResume(byte[] resume) {
        this.resume = resume;
    }

    public LocalDate getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDate appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // ...
}
