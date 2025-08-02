package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class VerificationToken {
    @Id @GeneratedValue
    private Long id;

    private String token;

    private LocalDateTime expiryDate;

    private boolean verified = false;

    @OneToOne
    private Employee employee;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

    // Getters and setters
    
}
