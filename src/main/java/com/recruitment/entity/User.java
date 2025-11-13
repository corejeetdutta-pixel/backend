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


	@Column(name = "user_id", nullable = false, updatable = false, unique = true)
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
	private String dob;
	private String experience;
	private String linkedin;
	private String github;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> skills = new ArrayList<>();

	private String role = "USER";

	@Lob
	@Column(columnDefinition = "TEXT")
	private String profilePicture;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "resume")
	private byte[] resume;

	@Column(name = "resume_content_type")
	private String resumeContentType;

	@Column(name = "resume_file_name")
	private String resumeFileName;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified = false;

	@Column(name = "verification_token")
	private String verificationToken;

	@Column(name = "token_expiry_date")
	private LocalDateTime tokenExpiryDate;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "user_applied_jobs",
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "job_id", referencedColumnName = "id")
	)
	@JsonIgnoreProperties({"applications", "postedBy"})
	private Set<Job> appliedJobs = new HashSet<>();

	public User() {
		this.userId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	public void applyForJob(Job job) {
		this.appliedJobs.add(job);
		job.getApplicants().add(this);
	}

	public void removeJob(Job job) {
		this.appliedJobs.remove(job);
		job.getApplicants().remove(this);
	}

	// Getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

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

	public String getQualification() { return qualification; }
	public void setQualification(String qualification) { this.qualification = qualification; }

	public String getDepartment() { return department; }
	public void setDepartment(String department) { this.department = department; }

	public String getPassoutYear() { return passoutYear; }
	public void setPassoutYear(String passoutYear) { this.passoutYear = passoutYear; }

	public String getDob() { return dob; }
	public void setDob(String dob) { this.dob = dob; }

	public String getExperience() { return experience; }
	public void setExperience(String experience) { this.experience = experience; }

	public String getLinkedin() { return linkedin; }
	public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

	public String getGithub() { return github; }
	public void setGithub(String github) { this.github = github; }

	public List<String> getSkills() { return skills; }
	public void setSkills(List<String> skills) { this.skills = skills; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }

	public String getProfilePicture() { return profilePicture; }
	public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

	public byte[] getResume() { return resume; }
	public void setResume(byte[] resume) { this.resume = resume; }

	public String getResumeContentType() { return resumeContentType; }
	public void setResumeContentType(String resumeContentType) { this.resumeContentType = resumeContentType; }

	public String getResumeFileName() { return resumeFileName; }
	public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

	public boolean isEmailVerified() { return emailVerified; }
	public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

	public String getVerificationToken() { return verificationToken; }
	public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

	public LocalDateTime getTokenExpiryDate() { return tokenExpiryDate; }
	public void setTokenExpiryDate(LocalDateTime tokenExpiryDate) { this.tokenExpiryDate = tokenExpiryDate; }

	public Set<Job> getAppliedJobs() { return appliedJobs; }
	public void setAppliedJobs(Set<Job> appliedJobs) { this.appliedJobs = appliedJobs; }
}