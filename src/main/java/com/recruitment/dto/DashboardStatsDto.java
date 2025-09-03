package com.recruitment.dto;

public class DashboardStatsDto {
    private long jobsPosted;
    private long applications;
    private long hired = 0;  // Default to 0
    private long interviews = 0;  // Default to 0

    public DashboardStatsDto() {}

    public DashboardStatsDto(long jobsPosted, long applications) {
        this.jobsPosted = jobsPosted;
        this.applications = applications;
        // hired and interviews will remain 0
    }

    public long getJobsPosted() {
        return jobsPosted;
    }

    public void setJobsPosted(long jobsPosted) {
        this.jobsPosted = jobsPosted;
    }

    public long getApplications() {
        return applications;
    }

    public void setApplications(long applications) {
        this.applications = applications;
    }

    public long getHired() {
        return hired;
    }

    public void setHired(long hired) {
        this.hired = hired;
    }

    public long getInterviews() {
        return interviews;
    }

    public void setInterviews(long interviews) {
        this.interviews = interviews;
    }
}