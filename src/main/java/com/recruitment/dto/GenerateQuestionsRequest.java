package com.recruitment.dto;

public class GenerateQuestionsRequest {
    private String title;
    private String description;
    private String requirements;
    private String experienceLevel;
    private int count;
    
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    
    public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRequirements() {
		return requirements;
	}
	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}
	public String getExperienceLevel() {
		return experienceLevel;
	}
	public void setExperienceLevel(String experienceLevel) {
		this.experienceLevel = experienceLevel;
	}
	public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}