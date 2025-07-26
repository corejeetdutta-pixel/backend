package com.recruitment.dto;



import java.util.List;

public class EvaluationRequest {
    private List<String> questions;
    private List<String> answers;
    private String userId;
    private String jobId;

    public EvaluationRequest() {}

    public List<String> getQuestions() { return questions; }
    public void setQuestions(List<String> questions) { this.questions = questions; }

    public List<String> getAnswers() { return answers; }
    public void setAnswers(List<String> answers) { this.answers = answers; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
}

