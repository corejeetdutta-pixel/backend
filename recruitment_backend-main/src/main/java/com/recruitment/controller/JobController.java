package com.recruitment.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.recruitment.entity.Employee;
import com.recruitment.entity.Job;
import com.recruitment.repository.JobRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = {"http://localhost:5173", "https://1c.atract.in"}, allowCredentials = "true")
public class JobController {

    @Autowired
    private JobRepository jobRepo;

    @PostMapping("/add-job")
    public ResponseEntity<?> addJob(@RequestBody Job job, HttpSession session) {
        System.out.println("Session ID: " + session.getId());
        System.out.println("Employee from session: " + session.getAttribute("emp"));

        Employee emp = (Employee) session.getAttribute("emp");
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }

        job.setPostedBy(emp);
        jobRepo.save(job);
        return ResponseEntity.ok("Job posted successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobRepo.findAll());
    }

//    @PostMapping("/by-ids")
//    public ResponseEntity<List<Job>> getJobsByIds(@RequestBody Map<String, List<String>> body) {
//        List<String> jobIds = body.get("jobIds");
//        List<Long> ids = jobIds.stream()
//                               .map(Long::valueOf)
//                               .toList();
//        List<Job> jobs = jobRepo.findAllById(ids);
//        return ResponseEntity.ok(jobs);
//    }

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
    public ResponseEntity<String> deleteJobByJobId(@PathVariable String jobId) {
        Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Job not found");
        }

        jobRepo.delete(jobOpt.get());
        return ResponseEntity.ok("Job deleted successfully");
    }

    @PutMapping("/update/{jobId}")
    public ResponseEntity<?> updateJob(@PathVariable String jobId, @RequestBody Job updatedJob) {
        Optional<Job> jobOpt = jobRepo.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Job job = jobOpt.get();
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
