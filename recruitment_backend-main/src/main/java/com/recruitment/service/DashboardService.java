package com.recruitment.service;

import com.recruitment.dto.DashboardStatsDto;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    public DashboardStatsDto getDashboardStats(String empId) {
        long jobsPosted = jobRepository.countByPostedBy_EmpId(empId);
        long applications = applicationRepository.countByJob_PostedBy_EmpId(empId);
        long hired = applicationRepository.countByJob_PostedBy_EmpIdAndStatus(empId, "HIRED");
        long interviews = applicationRepository.countByJob_PostedBy_EmpIdAndStatus(empId, "INTERVIEW");
        System.out.println("jobPosted : "+jobsPosted+ "applications : "+ applications);
        return new DashboardStatsDto(jobsPosted, applications, hired, interviews);
    }
}
