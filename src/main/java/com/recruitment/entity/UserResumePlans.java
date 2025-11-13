package com.recruitment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "user_resume_plans")
public class UserResumePlans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private String userId;

    @NotNull
    @Column(nullable = false, name = "plan_type")
    private String planType;


    @Column(name = "payment_ids")
    private List<Integer> paymentIds;

    @NotNull
    @Column(nullable = false, name = "creations_remaining")
    private int creationsRemaining;

    @NotNull
    @Column(nullable = false, name = "enhancements_remaining")
    private int enhancementsRemaining;

    @NotNull
    @Column(nullable = false, name = "downloads_remaining")
    private int downloadsRemaining;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "resumes")
    private List<Map<String, Object>> resumes;

    @Column(nullable = false, name = "created_at")
    private LocalDate createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDate updatedAt;

    public UserResumePlans(){}

    public UserResumePlans(Long id, String userId, String planType, List<Integer> paymentIds, int creationsRemaining, int enhancementsRemaining, int downloadsRemaining, List<Map<String, Object>> resumes, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.userId = userId;
        this.planType = planType;
        this.paymentIds = paymentIds;
        this.creationsRemaining = creationsRemaining;
        this.enhancementsRemaining = enhancementsRemaining;
        this.downloadsRemaining = downloadsRemaining;
        this.resumes = resumes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
// ---- Getters and Setters ----


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

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public List<Integer> getPaymentIds() {
        return paymentIds;
    }

    public void setPaymentIds(List<Integer> paymentIds) {
        this.paymentIds = paymentIds;
    }

    public int getCreationsRemaining() {
        return creationsRemaining;
    }

    public void setCreationsRemaining(int creationsRemaining) {
        this.creationsRemaining = creationsRemaining;
    }

    public int getEnhancementsRemaining() {
        return enhancementsRemaining;
    }

    public void setEnhancementsRemaining(int enhancementsRemaining) {
        this.enhancementsRemaining = enhancementsRemaining;
    }

    public int getDownloadsRemaining() {
        return downloadsRemaining;
    }

    public void setDownloadsRemaining(int downloadsRemaining) {
        this.downloadsRemaining = downloadsRemaining;
    }

    public List<Map<String, Object>> getResumes() {
        return resumes;
    }

    public void setResumes(List<Map<String, Object>> resumes) {
        this.resumes = resumes;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }
}