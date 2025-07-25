package com.recruitment.controller;

import com.recruitment.dto.DashboardStatsDto;
import com.recruitment.entity.Application;
import com.recruitment.entity.Job;
import com.recruitment.repository.JobRepository;
import com.recruitment.service.DashboardService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private JobRepository jobRepository; // ✅ Inject job repo

    @GetMapping("/stats/{empId}")
    public DashboardStatsDto getDashboardStats(@PathVariable String empId) {
    	System.out.println("dashboard is triggered" +empId);
        return dashboardService.getDashboardStats(empId);
    }
    
 // ✅ New endpoint to fetch jobs posted by a specific employer
    @GetMapping("/employer/{empId}")
    public List<Job> getJobsByEmployer(@PathVariable String empId) {
        System.out.println("Fetching jobs for employer: " + empId);
        return jobRepository.findByPostedBy_EmpId(empId);
    }
}
