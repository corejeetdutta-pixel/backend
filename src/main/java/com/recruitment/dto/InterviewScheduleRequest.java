package com.recruitment.dto;

import java.util.ArrayList;
import java.util.List;

public class InterviewScheduleRequest {
    private String jobId;
    private String jobTitle;
    private String company;
    private InterviewDetails interviewDetails;
    private List<String> qualifiedApplicantIds = new ArrayList<>(); // Initialize with empty list
    
    // Getters and setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public InterviewDetails getInterviewDetails() { return interviewDetails; }
    public void setInterviewDetails(InterviewDetails interviewDetails) { this.interviewDetails = interviewDetails; }
    
    public List<String> getQualifiedApplicantIds() { 
        // Ensure we never return null
        if (qualifiedApplicantIds == null) {
            qualifiedApplicantIds = new ArrayList<>();
        }
        return qualifiedApplicantIds; 
    }
    
    public void setQualifiedApplicantIds(List<String> qualifiedApplicantIds) { 
        // Ensure we never set to null
        this.qualifiedApplicantIds = (qualifiedApplicantIds != null) ? qualifiedApplicantIds : new ArrayList<>();
    }
    
    public static class InterviewDetails {
        private String date;
        private String time;
        private String meetLink;
        private String password;
        private String duration;
        private String contactEmail;
        
        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        
        public String getMeetLink() { return meetLink; }
        public void setMeetLink(String meetLink) { this.meetLink = meetLink; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    }
}