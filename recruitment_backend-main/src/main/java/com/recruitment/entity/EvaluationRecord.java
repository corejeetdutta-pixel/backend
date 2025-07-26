//package com.recruitment.entity;
//
//
//
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//public class EvaluationRecord {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String userId;
//    private String jobId;
//
//    @Lob
//    private String questionsJson;
//
//    @Lob
//    private String answersJson;
//
//    private int totalScore;
//    private boolean qualified;
//
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    public EvaluationRecord() {}
//
//    public EvaluationRecord(String userId, String jobId, String questionsJson, String answersJson, int totalScore, boolean qualified) {
//        this.userId = userId;
//        this.jobId = jobId;
//        this.questionsJson = questionsJson;
//        this.answersJson = answersJson;
//        this.totalScore = totalScore;
//        this.qualified = qualified;
//    }
//
//    // Getters and Setters
//
//    public Long getId() { return id; }
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//    public String getJobId() { return jobId; }
//    public void setJobId(String jobId) { this.jobId = jobId; }
//    public String getQuestionsJson() { return questionsJson; }
//    public void setQuestionsJson(String questionsJson) { this.questionsJson = questionsJson; }
//    public String getAnswersJson() { return answersJson; }
//    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
//    public int getTotalScore() { return totalScore; }
//    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
//    public boolean isQualified() { return qualified; }
//    public void setQualified(boolean qualified) { this.qualified = qualified; }
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//}
