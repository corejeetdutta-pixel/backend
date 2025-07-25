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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
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
    public String applyToJob(@RequestBody ApplicationRequest request) throws Exception {
        System.out.println("apply is triggered");

        boolean alreadyApplied = repository.existsByUserIdAndJobId(request.getUserId(), request.getJobId());
        if (alreadyApplied) {
            return "⚠️ You have already applied to this job.";
        }

        ObjectMapper mapper = new ObjectMapper();
        String answersJson = mapper.writeValueAsString(request.getAnswers());

        // 1. Get job entity first (required to set in Application)
        var job = jobRepository.findByJobId(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // 2. Save application with new fields
        Application application = new Application();
        application.setJobId(request.getJobId());
        application.setUserId(request.getUserId());
        application.setAnswers(answersJson);
        application.setScore(request.getScore());
        application.setQualified(request.getQualified());
        application.setStatus("Pending");      // ✅ default status
        application.setJob(job);               // ✅ set job reference
        repository.save(application);
        System.out.println("apply part 1");

        // 3. Get employer email
        String employerEmail = job.getPostedBy().getEmail();
        System.out.println("apply part 2");

        // 4. Get full user details
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

        // 5. Send email to employer
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
            return "✅ Application saved, but failed to email employer.";
        }

        return "✅ Application submitted successfully.";
    }
    
    @GetMapping("/employer/{empId}")
    public ResponseEntity<List<Application>> getEmployerApplications(@PathVariable String empId) {
        List<Application> apps = applicationService.getApplicationsByEmployer(empId);
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/count")
    public long getTotalApplications() {
        return repository.count();
    }

    @GetMapping("/all")
    public List<Application> getAllApplications() {
        return repository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Application> getApplicationsByUser(@PathVariable String userId) {
        return repository.findByUserId(userId);
    }

    @GetMapping("/check")
    public boolean hasUserApplied(@RequestParam String userId, @RequestParam String jobId) {
        return repository.existsByUserIdAndJobId(userId, jobId);
    }
}
