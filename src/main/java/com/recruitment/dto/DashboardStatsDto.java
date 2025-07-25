package com.recruitment.dto;

public class DashboardStatsDto {
    private long jobsPosted;
    private long applications;
    private long hired;
    private long interviews;

    // Getters and setters

    public DashboardStatsDto() {}

    public DashboardStatsDto(long jobsPosted, long applications, long hired, long interviews) {
        this.jobsPosted = jobsPosted;
        this.applications = applications;
        this.hired = hired;
        this.interviews = interviews;
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
