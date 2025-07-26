package com.recruitment.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {
	
	@Id @GeneratedValue
    private Long id;
	private String empId;
    private String name;
    private String email;
    private String password;
    private String role = "Employer";
    private Boolean agreedToTerms = false;
    @OneToMany(mappedBy = "postedBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Job> postedJobs = new ArrayList<>();
    
    
	
    public Employee(Long id, String empId, String name, String email, String password, String role,
			Boolean agreedToTerms, List<Job> postedJobs) {
		super();
		this.id = id;
		this.empId = empId;
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.agreedToTerms = agreedToTerms;
		this.postedJobs = postedJobs;
	}

	public Employee() {
    	
    }

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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public List<Job> getPostedJobs() {
		return postedJobs;
	}

	public void setPostedJobs(List<Job> postedJobs) {
		this.postedJobs = postedJobs;
	}

	public Boolean getAgreedToTerms() {
		return agreedToTerms;
	}

	public void setAgreedToTerms(Boolean agreedToTerms) {
		this.agreedToTerms = agreedToTerms;
	}
}
