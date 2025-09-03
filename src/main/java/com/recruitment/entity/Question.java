package com.recruitment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String questionText;
    
    private String jobTitle;
    private String difficulty;
    
    // Constructors, getters, and setters
    public Question() {}
    
    public Question(String questionText, String jobTitle, String difficulty) {
        this.questionText = questionText;
        this.jobTitle = jobTitle;
        this.difficulty = difficulty;
    }
    
    // Getters and setters...
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}