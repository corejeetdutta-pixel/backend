package com.recruitment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity(name="payments")
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private String userId;

    @NotNull
    @Column(nullable = false, name = "plan_type")
    private String planType;

    @NotNull
    @Column(nullable = false, name = "session_id")
    private String sessionId;

    @Column(name = "amount")
    private int amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status")
    private String status;

    @Column(nullable = false)
    private LocalDate created_at;

    @Column(nullable = false)
    private LocalDate updated_at;

    public Payments(){}

    public Payments(Long id, String userId, String planType, String sessionId, int amount, String currency, String status, LocalDate created_at, LocalDate updated_at) {
        this.id = id;
        this.userId = userId;
        this.planType = planType;
        this.sessionId = sessionId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull String getUserId() {
        return userId;
    }

    public void setUserId(@NotNull String userId) {
        this.userId = userId;
    }

    public @NotNull String getPlanType() {
        return planType;
    }

    public void setPlanType(@NotNull String planType) {
        this.planType = planType;
    }

    public @NotNull String getSessionId() {
        return sessionId;
    }

    public void setSessionId(@NotNull String sessionId) {
        this.sessionId = sessionId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDate created_at) {
        this.created_at = created_at;
    }

    public LocalDate getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDate updated_at) {
        this.updated_at = updated_at;
    }
}
