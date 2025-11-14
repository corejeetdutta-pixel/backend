package com.recruitment.service;

import com.recruitment.entity.Application;
import com.recruitment.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getApplicantsByJobId(String jobId) {
        try {
            System.out.println("Service: Fetching applicants for job ID: " + jobId);

            // Use the simplest working method first
            List<Object[]> applicationData = applicationRepository.findSimpleApplicantsByJobId(jobId);

            System.out.println("Found " + applicationData.size() + " applications for job: " + jobId);

            // Convert to applicant DTOs
            List<Map<String, Object>> applicants = applicationData.stream().map(data -> {
                Map<String, Object> applicant = new HashMap<>();

                // Map the data based on SELECT clause order
                applicant.put("applicationId", data[0]); // a.id
                applicant.put("name", data[1]);          // u.name
                applicant.put("email", data[2]);         // u.email
                applicant.put("phone", data[3]);         // u.mobile
                applicant.put("appliedAt", data[4]);     // a.appliedAt
                applicant.put("status", data[5]);        // a.status

                return applicant;
            }).collect(Collectors.toList());

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", applicants);
            response.put("count", applicants.size());
            response.put("jobId", jobId);
            response.put("message", "Successfully retrieved " + applicants.size() + " applicants");

            return response;

        } catch (Exception e) {
            System.err.println("Service error fetching applicants for job " + jobId + ": " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Service error while fetching applicants");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("jobId", jobId);

            return errorResponse;
        }
    }

    // Alternative method using the projection interface
    @Transactional(readOnly = true)
    public Map<String, Object> getApplicantsByJobIdWithProjection(String jobId) {
        try {
            List<ApplicationRepository.ApplicationProjection> projections =
                    applicationRepository.findApplicationsByJobIdWithoutLob(jobId);

            List<Map<String, Object>> applicants = projections.stream().map(proj -> {
                Map<String, Object> applicant = new HashMap<>();
                applicant.put("applicationId", proj.getApplicationId());
                applicant.put("score", proj.getScore());
                applicant.put("status", proj.getStatus());
                applicant.put("appliedAt", proj.getAppliedAt());
                applicant.put("userId", proj.getUserId());
                applicant.put("name", proj.getName());
                applicant.put("email", proj.getEmail());
                applicant.put("phone", proj.getMobile());
                applicant.put("qualification", proj.getQualification());
                applicant.put("gender", proj.getGender());
                applicant.put("dob", proj.getDob());
                applicant.put("address", proj.getAddress());
                applicant.put("passoutYear", proj.getPassoutYear());
                applicant.put("experience", proj.getExperience());
                applicant.put("skills", proj.getSkills());
                applicant.put("linkedin", proj.getLinkedin());
                applicant.put("github", proj.getGithub());
                applicant.put("department", proj.getDepartment());

                return applicant;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", applicants);
            response.put("count", applicants.size());
            response.put("jobId", jobId);

            return response;

        } catch (Exception e) {
            System.err.println("Projection method error: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error using projection method");
            errorResponse.put("error", e.getMessage());

            return errorResponse;
        }
    }
    public List<Application> getApplicationsByEmployer(String empId) {
        return applicationRepository.findApplicationsByEmployerId(empId);
    }

    @Transactional // Add this annotation
    public List<Application> getApplicationsByUser(String userId) {
        return applicationRepository.findByUserId(userId);
    }
}