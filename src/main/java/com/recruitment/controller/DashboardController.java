package com.recruitment.controller;

import com.recruitment.dto.DashboardStatsDto;
import com.recruitment.entity.Job;
import com.recruitment.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats/{empId}")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(@PathVariable String empId) {
    	System.out.println("dashboard is triggered");
        DashboardStatsDto stats = dashboardService.getDashboardStats(empId);
        System.out.println("dashboard is ended");
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/employer/{empId}")
    public ResponseEntity<List<Job>> getJobsByEmployer(@PathVariable String empId) {
        List<Job> jobs = dashboardService.getJobsByEmployer(empId);
        return ResponseEntity.ok(jobs);
    }
}