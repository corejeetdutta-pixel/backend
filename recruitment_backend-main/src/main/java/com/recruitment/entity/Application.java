package com.recruitment.entity;

import jakarta.persistence.*;

@Entity
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobId;
    private String userId;

    @Lob
    private String questions;

    @Lob
    private String answers;

    private Integer score;
    private Boolean qualified;

    private String status; // ✅ Add this line

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_ref_id")
    private Job job;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "job_posted_by")
//    private Job empId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getQuestions() {
		return questions;
	}

	public void setQuestions(String questions) {
		this.questions = questions;
	}

	public String getAnswers() {
		return answers;
	}

	public void setAnswers(String answers) {
		this.answers = answers;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Boolean getQualified() {
		return qualified;
	}

	public void setQualified(Boolean qualified) {
		this.qualified = qualified;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

    // Getters and setters...
    
}
