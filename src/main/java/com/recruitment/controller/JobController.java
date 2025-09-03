package com.recruitment.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.recruitment.dto.InterviewScheduleRequest;
import com.recruitment.entity.Application;
import com.recruitment.entity.Employee;
import com.recruitment.entity.Job;
import com.recruitment.entity.User;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.JobRepository;
import com.recruitment.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepo;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ApplicationRepository applicationRepository;

    @PostMapping("/add-job")
    public ResponseEntity<?> addJob(@RequestBody Job job, HttpSession session) {
        System.out.println("Session ID: " + session.getId());
        
        // Check if user is logged in as either employee or admin
        Employee emp = (Employee) session.getAttribute("emp");
        String adminId = (String) session.getAttribute("adminId");
        
        if (emp == null && adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }

        // If admin is adding job, we need to handle it differently
        if (adminId != null) {
            // For admin, we might need to set a default employee or handle differently
            // This depends on your business logic
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Admins cannot directly post jobs");
        }
        
        job.setPostedBy(emp);
        jobRepo.save(job);
        return ResponseEntity.ok("Job posted successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobRepo.findAll());
    }
 
    @GetMapping("/posted")
    public ResponseEntity<?> getJobsPostedByEmployee(HttpSession session) {
        System.out.println("posted job fetch is triggered");
        
        // Check if user is logged in as either employee or admin
        Employee emp = (Employee) session.getAttribute("emp");
        String adminId = (String) session.getAttribute("adminId");
        
        if (emp == null && adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        
        List<Job> jobs;
        if (adminId != null) {
            // Admin can see all jobs
            jobs = jobRepo.findAll();
        } else {
            // Employee can only see their own jobs
            jobs = jobRepo.findByPostedBy_EmpId(emp.getEmpId());
        }
        
        return ResponseEntity.ok(jobs);
    }
    
    @GetMapping("/{jobId}/applicants")
    public ResponseEntity<?> getApplicantsForJob(@PathVariable String jobId, HttpSession session) {
        System.out.println("applicants applied job fetch is triggered");
        
        // Check if user is logged in as either employee or admin
        Employee emp = (Employee) session.getAttribute("emp");
        String adminId = (String) session.getAttribute("adminId");
        
        if (emp == null && adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
        
        Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Job job = jobOpt.get();
        
        // If user is employee (not admin), check if they own this job
        if (adminId == null && !job.getPostedBy().getEmpId().equals(emp.getEmpId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to view applicants for this job");
        }
        
        List<Application> applications = applicationRepository.findApplicationsByJobIdWithUser(jobId);
        
        List<Map<String, Object>> applicants = applications.stream().map(app -> {
            User user = app.getUser();
            Map<String, Object> dto = new HashMap<>();
            dto.put("name", user.getName());
            dto.put("email", user.getEmail());
            dto.put("mobile", user.getMobile());
            dto.put("qualification", user.getQualification());
            dto.put("gender", user.getGender());
            dto.put("dob", user.getDob());
            dto.put("address", user.getAddress());
            dto.put("passoutYear", user.getPassoutYear());
            dto.put("experience", user.getExperience());
            dto.put("skills", user.getSkills());
            dto.put("linkedin", user.getLinkedin());
            dto.put("github", user.getGithub());
            dto.put("score", app.getScore());
            dto.put("status", app.getStatus());
            dto.put("appliedAt", app.getAppliedAt());
            dto.put("applicationId", app.getId());
            dto.put("resume", user.getResume()); // Include resume field
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(applicants);
    }
    
    @PostMapping("/schedule-interviews")
    public ResponseEntity<?> scheduleInterviews(@RequestBody InterviewScheduleRequest request, HttpSession session) {
        System.out.println("Schedule interviews triggered for job: " + request.getJobId());
        
        // Check if user is logged in as either employee or admin
        Employee emp = (Employee) session.getAttribute("emp");
        String adminId = (String) session.getAttribute("adminId");
        
        if (emp == null && adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
        
        // Verify the job exists
        Optional<Job> jobOpt = jobRepo.findByJobId(request.getJobId());
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Job job = jobOpt.get();
        
        // If user is employee (not admin), check if they own this job
        if (adminId == null && !job.getPostedBy().getEmpId().equals(emp.getEmpId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to schedule interviews for this job");
        }
        
        try {
            // Get all applications for this job with user data
            List<Application> applications = applicationRepository.findApplicationsByJobIdWithUser(request.getJobId());
            
            List<Application> qualifiedApplications = new ArrayList<>();
            List<Application> nonQualifiedApplications = new ArrayList<>();
            List<Application> alreadyProcessedApplications = new ArrayList<>();
            
            // Determine qualified applicants based on job requirements
            for (Application app : applications) {
                // Skip already processed applications
                if ("Interview Scheduled".equals(app.getStatus()) || "Rejected".equals(app.getStatus())) {
                    alreadyProcessedApplications.add(app);
                    continue;
                }
                
                User user = app.getUser();
                if (isQualifiedApplicant(user, job)) {
                    qualifiedApplications.add(app);
                } else {
                    nonQualifiedApplications.add(app);
                }
            }
            
            // Send interview emails to qualified applicants
            for (Application app : qualifiedApplications) {
                User user = app.getUser();
                // Update status to "Interview Scheduled"
                app.setStatus("Interview Scheduled");
                applicationRepository.save(app);
                
                // Send interview email
                sendInterviewEmail(user, job, request.getInterviewDetails());
            }
            
            // Send regret emails to non-qualified applicants and update their status
            for (Application app : nonQualifiedApplications) {
                User user = app.getUser();
                // Update status to "Rejected"
                app.setStatus("Rejected");
                applicationRepository.save(app);
                
                // Send regret email
                sendRegretEmail(user, job);
            }
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Interview scheduling completed successfully",
                "qualifiedCount", qualifiedApplications.size(),
                "rejectedCount", nonQualifiedApplications.size(),
                "alreadyProcessedCount", alreadyProcessedApplications.size()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to schedule interviews: " + e.getMessage());
        }
    }

    // Helper method to determine if an applicant is qualified
    private boolean isQualifiedApplicant(User user, Job job) {
        // Extract numeric value from experience strings
        int applicantExp = extractYearsFromExperience(user.getExperience());
        int jobExp = extractYearsFromExperience(job.getExperience());
        
        // Check if applicant meets minimum experience requirement
        boolean experienceMatch = applicantExp >= jobExp;
        
        // Check qualification match
        boolean qualificationMatch = user.getQualification() != null && job.getHighestQualification() != null &&
                user.getQualification().toLowerCase().contains(job.getHighestQualification().toLowerCase());
        
        // Check department match
        boolean departmentMatch = user.getDepartment() != null && job.getDepartment() != null &&
                user.getDepartment().toLowerCase().contains(job.getDepartment().toLowerCase());
        
        // Applicant is qualified if they meet any of the criteria
        return experienceMatch || qualificationMatch || departmentMatch;
    }

    // Helper method to extract years from experience string
    private int extractYearsFromExperience(String experience) {
        if (experience == null || experience.isEmpty()) {
            return 0;
        }
        
        // Extract the first number found in the experience string
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(experience);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        return 0;
    }
    private void sendInterviewEmail(User user, Job job, InterviewScheduleRequest.InterviewDetails details) {
        try {
        	System.out.println("qualified method is triggered for mail");
            String subject = "Interview Invitation for " + job.getTitle() + " at " + job.getCompany();
            
            StringBuilder content = new StringBuilder();
            content.append("<h2>Interview Invitation</h2>");
            content.append("<p>Dear ").append(user.getName()).append(",</p>");
            content.append("<p>Congratulations! We are impressed with your qualifications and would like to invite you for an interview for the position of <strong>")
                   .append(job.getTitle()).append("</strong> at <strong>").append(job.getCompany()).append("</strong>.</p>");
            
            content.append("<h3>Interview Details:</h3>");
            content.append("<ul>");
            content.append("<li><strong>Date:</strong> ").append(details.getDate()).append("</li>");
            content.append("<li><strong>Time:</strong> ").append(details.getTime()).append("</li>");
            content.append("<li><strong>Duration:</strong> ").append(details.getDuration()).append(" minutes</li>");
            content.append("<li><strong>Meeting Link:</strong> <a href=\"").append(details.getMeetLink())
                   .append("\">").append(details.getMeetLink()).append("</a></li>");
            if (details.getPassword() != null && !details.getPassword().isEmpty()) {
                content.append("<li><strong>Meeting Password:</strong> ").append(details.getPassword()).append("</li>");
            }
            content.append("<li><strong>Contact Email:</strong> ").append(details.getContactEmail()).append("</li>");
            content.append("</ul>");
            
            content.append("<p>Please ensure you are available at the scheduled time and have a stable internet connection.</p>");
            content.append("<p>Best regards,<br>").append(job.getCompany()).append(" Hiring Team</p>");
            
            // Use your existing email service to send the email
            emailService.sendHtmlEmail(user.getEmail(), subject, content.toString());
            
        } catch (Exception e) {
            System.err.println("Failed to send interview email to " + user.getEmail() + ": " + e.getMessage());
        }
    }

    private void sendRegretEmail(User user, Job job) {
        try {
        	System.out.println("unqualified method is triggered for mail");
            String subject = "Update on Your Application for " + job.getTitle() + " at " + job.getCompany();
            
            StringBuilder content = new StringBuilder();
            content.append("<h2>Application Update</h2>");
            content.append("<p>Dear ").append(user.getName()).append(",</p>");
            content.append("<p>Thank you for your interest in the <strong>").append(job.getTitle())
                   .append("</strong> position at <strong>").append(job.getCompany()).append("</strong>.</p>");
            content.append("<p>After careful consideration, we regret to inform you that we have decided to move forward with other candidates whose qualifications more closely match our current needs.</p>");
            content.append("<p>We appreciate the time you took to apply and encourage you to apply for future positions that may be a better fit for your skills and experience.</p>");
            content.append("<p>Best regards,<br>").append(job.getCompany()).append(" Hiring Team</p>");
            
            // Use your existing email service to send the email
            emailService.sendHtmlEmail(user.getEmail(), subject, content.toString());
            
        } catch (Exception e) {
            System.err.println("Failed to send regret email to " + user.getEmail() + ": " + e.getMessage());
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Job>> filterJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company
    ) {
        List<Job> jobs = jobRepo.filterJobs(location, company);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/view/{jobId}")
    public ResponseEntity<?> getJobByJobId(@PathVariable String jobId) {
        Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jobOpt.get());
    }

    @DeleteMapping("/delete/{jobId}")
    @Transactional
    public ResponseEntity<String> deleteJobByJobId(@PathVariable String jobId, HttpSession session) {
        // Check if user is logged in as either employee or admin
        Employee emp = (Employee) session.getAttribute("emp");
        String adminId = (String) session.getAttribute("adminId");
        
        if (emp == null && adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
        
        Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Job not found");
        }
        
        Job job = jobOpt.get();
        
        // If user is employee (not admin), check if they own this job
        if (adminId == null && !job.getPostedBy().getEmpId().equals(emp.getEmpId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to delete this job");
        }

        jobRepo.delete(job);
        return ResponseEntity.ok("Job deleted successfully");
    }

    @PutMapping("/update/{jobId}")
    public ResponseEntity<?> updateJob(@PathVariable String jobId, @RequestBody Job updatedJob, HttpSession session) {
        // Check if user is logged in as either employee or admin
        Employee emp = (Employee) session.getAttribute("emp");
        String adminId = (String) session.getAttribute("adminId");
        
        if (emp == null && adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
        
        Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Job job = jobOpt.get();
        
        // If user is employee (not admin), check if they own this job
        if (adminId == null && !job.getPostedBy().getEmpId().equals(emp.getEmpId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this job");
        }
        
        job.setTitle(updatedJob.getTitle());
        job.setCompany(updatedJob.getCompany());
        job.setLocation(updatedJob.getLocation());
        job.setMinSalary(updatedJob.getMinSalary());
        job.setMaxSalary(updatedJob.getMaxSalary());
        job.setOpenings(updatedJob.getOpenings());
        job.setLastDate(updatedJob.getLastDate());

        jobRepo.save(job);
        return ResponseEntity.ok("✅ Job updated successfully");
    }
}