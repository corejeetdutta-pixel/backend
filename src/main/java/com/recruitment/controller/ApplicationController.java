package com.recruitment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.dto.ApplicationRequest;
import com.recruitment.dto.UserDto;
import com.recruitment.entity.Application;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.UserRepo;
import com.recruitment.service.ApplicationService;
import com.recruitment.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationRepository repository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/apply")
    public ResponseEntity<String> applyToJob(@RequestBody ApplicationRequest request) {
        try {
            System.out.println("apply is triggered");

            boolean alreadyApplied = repository.existsByUserIdAndJobId(request.getUserId(), request.getJobId());
            if (alreadyApplied) {
                System.out.println("isApplied triggered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body("⚠️ You have already applied to this job.");
            }

            ObjectMapper mapper = new ObjectMapper();
            String answersJson = mapper.writeValueAsString(request.getAnswers());
            String questionsJson = mapper.writeValueAsString(request.getQuestions());

            // Get job entity first (required to set in Application)
            var job = jobRepository.findByJobId(request.getJobId())
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            var user = userRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Save application with new fields
            Application application = new Application();
            application.setJobId(request.getJobId());
            application.setUserId(request.getUserId());
            application.setAnswers(answersJson);
            application.setQuestions(questionsJson);
            application.setScore(request.getScore());
            application.setQualified(request.getQualified());
            application.setStatus("Pending");
            application.setJob(job);
            application.setUser(user);
            repository.save(application);
            System.out.println("apply part 1");

            // Get employer email
            String employerEmail = job.getPostedBy().getEmail();
            System.out.println("apply part 2");

            // Get full user details
            var userEntity = userRepository.findByUserId(request.getUserId()).orElseThrow();
            var userDto = new UserDto();
            userDto.setName(userEntity.getName());
            userDto.setEmail(userEntity.getEmail());
            userDto.setMobile(userEntity.getMobile());
            userDto.setAddress(userEntity.getAddress());
            userDto.setGender(userEntity.getGender());
            userDto.setQualification(userEntity.getQualification());
            userDto.setPassoutYear(userEntity.getPassoutYear());
            userDto.setSkills(userEntity.getSkills());
            userDto.setResume(userEntity.getResume());
            System.out.println("apply part 3");

            // Send email to employer
            try {
                emailService.sendApplicationEmail(
                        employerEmail,
                        userDto,
                        job.getTitle(),
                        job.getJobId(),
                        job.getDescription(),
                        answersJson,
                        request.getScore()
                );
                System.out.println("mail is triggered");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("exception part");
                return ResponseEntity.status(HttpStatus.OK).body("✅ Application saved, but failed to email employer.");
            }

            return ResponseEntity.ok("✅ Application submitted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error submitting application.");
        }
    }
    
    @GetMapping("/employer/{empId}")
    public ResponseEntity<List<Application>> getEmployerApplications(@PathVariable String empId) {
        try {
            List<Application> apps = applicationService.getApplicationsByEmployer(empId);
            return ResponseEntity.ok(apps);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalApplications() {
        try {
            long count = repository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Application>> getAllApplications() {
        try {
            List<Application> applications = repository.findAll();
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Application>> getApplicationsByUser(@PathVariable String userId) {
        try {
            List<Application> applications = repository.findByUserId(userId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> hasUserApplied(@RequestParam String userId, @RequestParam String jobId) {
        try {
            boolean hasApplied = repository.existsByUserIdAndJobId(userId, jobId);
            return ResponseEntity.ok(hasApplied);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}