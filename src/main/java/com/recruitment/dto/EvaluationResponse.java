package com.recruitment.dto;



public class EvaluationResponse {
    private int score;
    private boolean qualified;

    public EvaluationResponse() {}

    public EvaluationResponse(int score, boolean qualified) {
        this.score = score;
        this.qualified = qualified;
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isQualified() { return qualified; }
    public void setQualified(boolean qualified) { this.qualified = qualified; }
}

