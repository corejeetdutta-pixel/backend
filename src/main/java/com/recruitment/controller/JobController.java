package com.recruitment.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.*;
import java.util.stream.Collectors;

import com.recruitment.dto.JobWithSchemaResponse;
import com.recruitment.util.jobSchemaGenerator;
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
import com.recruitment.repository.EmployeeRepo;
import com.recruitment.repository.JobRepository;
import com.recruitment.service.EmailService;
import com.recruitment.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class JobController {

    @Autowired
    private JobRepository jobRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ApplicationRepository applicationRepository;

    // List of admin emails
    private static final List<String> ADMIN_EMAILS = List.of(
            "admin@example.com",
            "bursana@example.com",
            "admin@attract.in"
    );

    // Helper method to extract user from JWT token
    private String getUserEmailFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                String email = jwtUtil.extractUsername(jwt);
                System.out.println("Extracted email from token: " + email);
                return email;
            } catch (Exception e) {
                System.err.println("Error extracting email from token: " + e.getMessage());
                return null;
            }
        }

        // Also check for token in cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    try {
                        String email = jwtUtil.extractUsername(cookie.getValue());
                        System.out.println("Extracted email from cookie: " + email);
                        return email;
                    } catch (Exception e) {
                        System.err.println("Error extracting email from cookie: " + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    // Helper method to check if user is admin
    private boolean isAdmin(HttpServletRequest request) {
        String email = getUserEmailFromRequest(request);
        boolean isAdmin = email != null && ADMIN_EMAILS.contains(email);
        System.out.println("Is admin check for " + email + ": " + isAdmin);
        return isAdmin;
    }

    // Helper method to get employee from request
    private Employee getEmployeeFromRequest(HttpServletRequest request) {
        String email = getUserEmailFromRequest(request);
        System.out.println("Getting employee for email: " + email);

        if (email != null) {
            Optional<Employee> employeeOpt = employeeRepo.findByEmail(email);
            if (employeeOpt.isPresent()) {
                System.out.println("Employee found: " + employeeOpt.get().getEmpId());
            } else {
                System.out.println("No employee found for email: " + email);
            }
            return employeeOpt.orElse(null);
        }
        return null;
    }

    @PostMapping("/add-job")
    public ResponseEntity<?> addJob(@RequestBody Job job, HttpServletRequest request) {
        System.out.println("Adding job: " + job.getTitle() + " for company: " + job.getCompany());

        // Check if user is logged in as employee using JWT
        Employee emp = getEmployeeFromRequest(request);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in or invalid token");
        }

        try {
            // Validate required fields
            if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Job title is required");
            }
            if (job.getCompany() == null || job.getCompany().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Company name is required");
            }
            if (job.getLocation() == null || job.getLocation().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Location is required");
            }
            if (job.getExperience() == null || job.getExperience().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Experience is required");
            }
            if (job.getOpenings() <= 0) {
                return ResponseEntity.badRequest().body("Number of openings must be greater than 0");
            }
            if (job.getOpeningDate() == null) {
                return ResponseEntity.badRequest().body("Opening date is required");
            }
            if (job.getLastDate() == null) {
                return ResponseEntity.badRequest().body("Closing date is required");
            }
            if (job.getLastDate().isBefore(job.getOpeningDate())) {
                return ResponseEntity.badRequest().body("Closing date must be after opening date");
            }
            if (job.getContactEmail() == null || job.getContactEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Contact email is required");
            }
            if (job.getMinSalary() == null || job.getMinSalary().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Minimum salary is required");
            }
            if (job.getMaxSalary() == null || job.getMaxSalary().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Maximum salary is required");
            }

            // Set the employee who posted the job
            job.setPostedBy(emp);

            // Ensure jobId is set
            if (job.getJobId() == null || job.getJobId().isEmpty()) {
                job.setJobId(java.util.UUID.randomUUID().toString());
            }

            // Set default values for optional fields if they are null
            if (job.getJobType() == null || job.getJobType().isEmpty()) {
                job.setJobType("Full-time");
            }
            if (job.getEmploymentType() == null || job.getEmploymentType().isEmpty()) {
                job.setEmploymentType("Permanent");
            }
            if (job.getDescription() == null || job.getDescription().isEmpty()) {
                job.setDescription("No description provided");
            }

            Job savedJob = jobRepo.save(job);
            return ResponseEntity.ok("Job posted successfully");
        } catch (Exception e) {
            System.err.println("Error saving job: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to post job: " + e.getMessage());
        }
    }

    /*@GetMapping("/all")
    public ResponseEntity<List<Job>> getAllJobs() {
        try {
            List<Job> jobs = jobRepo.findAll();
            String schema = JobSchemaGenerator.generateJobPostingSchema(job);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.err.println("Error fetching all jobs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }*/

    @GetMapping("/all")
    public ResponseEntity<List<JobWithSchemaResponse>> getAllJobs() {
        try {
            List<Job> jobs = jobRepo.findAll();

            List<JobWithSchemaResponse> jobResponses = jobs.stream()
                    .map(job -> {
                        String schema = jobSchemaGenerator.generateJobPostingSchema(job);
                        return new JobWithSchemaResponse(job, schema);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(jobResponses);

        } catch (Exception e) {
            System.err.println("Error fetching all jobs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // New endpoint for admin to get all jobs
    @GetMapping("/admin/all-jobs")
    public ResponseEntity<?> getAllJobsForAdmin(HttpServletRequest request) {
        // Check if user is admin
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized as admin");
        }

        try {
            List<Job> jobs = jobRepo.findAll();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.err.println("Error fetching all jobs for admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching jobs");
        }
    }

    @GetMapping("/posted")
    public ResponseEntity<?> getJobsPostedByEmployee(HttpServletRequest request) {
        System.out.println("posted job fetch is triggered");

        // Check if user is logged in as employee using JWT
        Employee emp = getEmployeeFromRequest(request);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in or invalid token");
        }

        try {
            List<Job> jobs = jobRepo.findByPostedBy_EmpId(emp.getEmpId());
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.err.println("Error fetching posted jobs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching jobs");
        }
    }

    // Fixed applicants endpoint for regular users
    @GetMapping("/{jobId}/applicants")
    public ResponseEntity<?> getApplicantsForJob(@PathVariable String jobId, HttpServletRequest request) {
        System.out.println("Applicants endpoint called for job: " + jobId);

        // Check if user is logged in as employee using JWT
        Employee emp = getEmployeeFromRequest(request);
        if (emp == null && !isAdmin(request)) {
            System.out.println("Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        try {
            Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
            if (jobOpt.isEmpty()) {
                System.out.println("Job not found: " + jobId);
                return ResponseEntity.ok(new ArrayList<>());
            }

            Job job = jobOpt.get();
            // Check if the job has a poster
            if (job.getPostedBy() == null) {
                System.out.println("Job has no postedBy employee: " + jobId);
                // Only admin can view applicants for a job with no poster
                if (!isAdmin(request)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to view applicants for this job");
                }
            } else {
                System.out.println("Job found: " + job.getTitle() + " posted by: " + job.getPostedBy().getEmpId());
                // Check if the employee owns this job or is admin
                if (emp != null && !job.getPostedBy().getEmpId().equals(emp.getEmpId()) && !isAdmin(request)) {
                    System.out.println("Forbidden: Employee " + emp.getEmpId() + " does not own job " + jobId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to view applicants for this job");
                }
            }

            List<Application> applications = applicationRepository.findApplicationsByJobIdWithUser(jobId);
            System.out.println("Found " + (applications != null ? applications.size() : 0) + " applications");

            // Ensure we always return an array, even if null
            List<Map<String, Object>> applicants = new ArrayList<>();
            if (applications != null) {
                applicants = applications.stream().map(app -> {
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
                    dto.put("resume", user.getResume());
                    return dto;
                }).collect(Collectors.toList());
            }

            System.out.println("Returning " + applicants.size() + " applicants");
            return ResponseEntity.ok(applicants);
        } catch (Exception e) {
            System.err.println("Error fetching applicants: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching applicants: " + e.getMessage());
        }
    }

    // Fixed admin applicants endpoint
    @GetMapping("/admin/{jobId}/applicants")
    public ResponseEntity<?> getApplicantsForJobAdmin(@PathVariable String jobId, HttpServletRequest request) {
        System.out.println("Admin applicants endpoint called for job: " + jobId);

        // Check if user is admin
        if (!isAdmin(request)) {
            System.out.println("Non-admin attempt to access admin endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized as admin");
        }

        try {
            Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
            if (jobOpt.isEmpty()) {
                System.out.println("Job not found: " + jobId);
                return ResponseEntity.ok(new ArrayList<>());
            }

            Job job = jobOpt.get();
            if (job.getPostedBy() != null) {
                System.out.println("Job found: " + job.getTitle() + " posted by: " + job.getPostedBy().getEmpId());
            } else {
                System.out.println("Job found: " + job.getTitle() + " but has no postedBy");
            }

            List<Application> applications = applicationRepository.findApplicationsByJobIdWithUser(jobId);
            System.out.println("Found " + (applications != null ? applications.size() : 0) + " applications");

            // Ensure we always return an array, even if null
            List<Map<String, Object>> applicants = new ArrayList<>();
            if (applications != null) {
                applicants = applications.stream().map(app -> {
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
                    dto.put("resume", user.getResume());
                    return dto;
                }).collect(Collectors.toList());
            }

            System.out.println("Returning " + applicants.size() + " applicants for admin");
            return ResponseEntity.ok(applicants);
        } catch (Exception e) {
            System.err.println("Error fetching applicants for admin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching applicants: " + e.getMessage());
        }
    }

    @PostMapping("/schedule-interviews")
    public ResponseEntity<?> scheduleInterviews(@RequestBody InterviewScheduleRequest request, HttpServletRequest httpRequest) {
        System.out.println("Schedule interviews triggered for job: " + request.getJobId());

        // Check if user is logged in as employee using JWT
        Employee emp = getEmployeeFromRequest(httpRequest);
        if (emp == null && !isAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        try {
            // Verify the job exists
            Optional<Job> jobOpt = jobRepo.findByJobId(request.getJobId());
            if (jobOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Job job = jobOpt.get();

            // Check if the employee owns this job or is admin
            if (emp != null && !job.getPostedBy().getEmpId().equals(emp.getEmpId()) && !isAdmin(httpRequest)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to schedule interviews for this job");
            }

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
        try {
            List<Job> jobs = jobRepo.filterJobs(location, company);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.err.println("Error filtering jobs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @GetMapping("/view/{jobId}")
    public ResponseEntity<?> getJobByJobId(@PathVariable String jobId) {
        try {
            Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
            if (jobOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Job job = jobOpt.get();

            // Create a simplified DTO for public view (optional)
            Map<String, Object> publicJob = new HashMap<>();
            publicJob.put("jobId", job.getJobId());
            publicJob.put("title", job.getTitle());
            publicJob.put("company", job.getCompany());
            publicJob.put("location", job.getLocation());
            publicJob.put("minSalary", job.getMinSalary());
            publicJob.put("maxSalary", job.getMaxSalary());
            publicJob.put("experience", job.getExperience());
            publicJob.put("openings", job.getOpenings());
            publicJob.put("openingDate", job.getOpeningDate());
            publicJob.put("lastDate", job.getLastDate());
            publicJob.put("jobType", job.getJobType());
            publicJob.put("employmentType", job.getEmploymentType());
            publicJob.put("department", job.getDepartment());
            publicJob.put("mode", job.getMode());
            publicJob.put("description", job.getDescription());
            publicJob.put("requirements", job.getRequirements());
            publicJob.put("perks", job.getPerks());
            publicJob.put("contactEmail", job.getContactEmail());

            return ResponseEntity.ok(publicJob);
        } catch (Exception e) {
            System.err.println("Error fetching job: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching job");
        }
    }

    @DeleteMapping("/delete/{jobId}")
    @Transactional
    public ResponseEntity<String> deleteJobByJobId(@PathVariable String jobId, HttpServletRequest request) {
        // Check if user is logged in
        String email = getUserEmailFromRequest(request);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        boolean isAdminUser = isAdmin(request);
        Employee emp = getEmployeeFromRequest(request);

        try {
            Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
            if (jobOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Job not found");
            }

            Job job = jobOpt.get();

            // Check if the employee owns this job or is admin
            if (emp != null && !job.getPostedBy().getEmpId().equals(emp.getEmpId()) && !isAdminUser) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to delete this job");
            }

            jobRepo.delete(job);
            return ResponseEntity.ok("Job deleted successfully");
        } catch (Exception e) {
            System.err.println("Error deleting job: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting job");
        }
    }

    @PutMapping("/update/{jobId}")
    public ResponseEntity<?> updateJob(@PathVariable String jobId, @RequestBody Job updatedJob, HttpServletRequest request) {
        // Check if user is logged in
        String email = getUserEmailFromRequest(request);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        boolean isAdminUser = isAdmin(request);
        Employee emp = getEmployeeFromRequest(request);

        try {
            Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
            if (jobOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Job job = jobOpt.get();

            // Check if the employee owns this job or is admin
            if (emp != null && !job.getPostedBy().getEmpId().equals(emp.getEmpId()) && !isAdminUser) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this job");
            }

            // Update only the allowed fields
            job.setTitle(updatedJob.getTitle());
            job.setCompany(updatedJob.getCompany());
            job.setLocation(updatedJob.getLocation());
            job.setHighestQualification(updatedJob.getHighestQualification());
            job.setExperience(updatedJob.getExperience());
            job.setMinSalary(updatedJob.getMinSalary());
            job.setMaxSalary(updatedJob.getMaxSalary());
            job.setOpenings(updatedJob.getOpenings());
            job.setOpeningDate(updatedJob.getOpeningDate());
            job.setLastDate(updatedJob.getLastDate());
            job.setMode(updatedJob.getMode());
            job.setDepartment(updatedJob.getDepartment());
            job.setJobType(updatedJob.getJobType());
            job.setEmploymentType(updatedJob.getEmploymentType());
            job.setDescription(updatedJob.getDescription());
            job.setResponsibilities(updatedJob.getResponsibilities());
            job.setRequirements(updatedJob.getRequirements());
            job.setPerks(updatedJob.getPerks());
            job.setContactEmail(updatedJob.getContactEmail());

            jobRepo.save(job);
            return ResponseEntity.ok("✅ Job updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating job: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating job");
        }
    }
}