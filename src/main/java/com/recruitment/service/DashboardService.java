package com.recruitment.service;

import com.recruitment.dto.DashboardStatsDto;
import com.recruitment.entity.Job;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public DashboardService(JobRepository jobRepository,
                           ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public DashboardStatsDto getDashboardStats(String empId) {
        long jobsPosted = jobRepository.countByPostedBy_EmpId(empId);
        long applications = applicationRepository.countByJob_PostedBy_EmpId(empId);
        long interviews = applicationRepository.countByJob_PostedBy_EmpIdAndStatus(empId, "Interview Scheduled");
        long hired = applicationRepository.countByJob_PostedBy_EmpIdAndStatus(empId, "Hired");
        
        DashboardStatsDto stats = new DashboardStatsDto(jobsPosted, applications);
        stats.setInterviews(interviews);
        stats.setHired(hired);
        
        return stats;
    }
    
    public List<Job> getJobsByEmployer(String empId) {
        return jobRepository.findByPostedBy_EmpId(empId);
    }
}