package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ CRITICAL FIX
	private Long id;

	@Column(unique = true, nullable = false)
	private String token;

	@Column(name = "expiry_date", nullable = false)
	private LocalDateTime expiryDate;

	@Column(nullable = false)
	private boolean verified = false;

	// ✅ FIXED: ManyToOne relationship
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Constructors
	public VerificationToken() {}

	public VerificationToken(String token, Employee employee, LocalDateTime expiryDate) {
		this.token = token;
		this.employee = employee;
		this.expiryDate = expiryDate;
		this.verified = false;
	}

	// Utility method
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiryDate);
	}

	// Getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }

	public LocalDateTime getExpiryDate() { return expiryDate; }
	public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

	public boolean isVerified() { return verified; }
	public void setVerified(boolean verified) { this.verified = verified; }

	public Employee getEmployee() { return employee; }
	public void setEmployee(Employee employee) { this.employee = employee; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	@Override
	public String toString() {
		return "VerificationToken{" +
				"id=" + id +
				", token='" + token + '\'' +
				", expiryDate=" + expiryDate +
				", verified=" + verified +
				", employee=" + (employee != null ? employee.getEmail() : "null") +
				'}';
	}
}