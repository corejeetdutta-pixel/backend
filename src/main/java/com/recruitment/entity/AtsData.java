package com.recruitment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "atsdata",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "job_id"})
)
public class AtsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private String userId; // ✅ Stored as UUID (text/UUID)

    @NotNull
    @Column(name = "job_id", nullable = false, columnDefinition = "UUID")
    private String jobId; // ✅ Stored as UUID (text/UUID)

    @NotNull
    @Column(name = "ats_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal ats_score;

    @Column(name = "created_at", nullable = false)
    private LocalDate created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updated_at;

    // Constructors
    public AtsData() {}

    public AtsData(String userId, String jobId, BigDecimal ats_score, LocalDate created_at, LocalDate updated_at) {
        this.userId = userId;
        this.jobId = jobId;
        this.ats_score = ats_score;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public BigDecimal getAts_score() { return ats_score; }
    public void setAts_score(BigDecimal ats_score) { this.ats_score = ats_score; }

    public LocalDate getCreated_at() { return created_at; }
    public void setCreated_at(LocalDate created_at) { this.created_at = created_at; }

    public LocalDate getUpdated_at() { return updated_at; }
    public void setUpdated_at(LocalDate updated_at) { this.updated_at = updated_at; }
}
