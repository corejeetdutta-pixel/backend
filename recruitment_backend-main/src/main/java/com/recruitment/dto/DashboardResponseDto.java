// DashboardResponseDto.java
package com.recruitment.dto;

import com.recruitment.entity.Job;
import java.util.List;

public class DashboardResponseDto {
    private DashboardStatsDto stats;
    private List<Job> jobs;

    public DashboardResponseDto() {}

    public DashboardResponseDto(DashboardStatsDto stats, List<Job> jobs) {
        this.stats = stats;
        this.jobs = jobs;
    }

    public DashboardStatsDto getStats() {
        return stats;
    }

    public void setStats(DashboardStatsDto stats) {
        this.stats = stats;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
