//package com.recruitment.controller;
//
//import com.recruitment.entity.DashboardStats;
//import com.recruitment.repository.ApplicationRepository;
//import com.recruitment.repository.JobRepository;
//
////import com.recruitment.repository.JobRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/dashboard")
//@CrossOrigin(origins = "http://localhost:5173")
//public class DashboardController {
//
//    @Autowired private JobRepository jobRepo;
//    @Autowired private ApplicationRepository appRepo;
//
//    @GetMapping("/stats")
//    public DashboardStats getStats() {
//        long jobsPosted = jobRepo.count();
//        long applications = appRepo.count();
//        System.out.println("total number of application + "+ applications);
//        long hired = 4;        // You can fetch from DB if needed
//        long interviews = 16;  // You can fetch from DB if needed
//
//        return new DashboardStats(jobsPosted, applications, hired, interviews);
//    }
//}
