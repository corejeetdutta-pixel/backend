package com.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.recruitment.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByLocationIgnoreCase(String location);
    List<Job> findByCompanyIgnoreCase(String company);

    @Query("SELECT j FROM Job j WHERE " +
           "(:location IS NULL OR LOWER(j.location) = LOWER(:location)) AND " +
           "(:company IS NULL OR LOWER(j.company) = LOWER(:company))")
    List<Job> filterJobs(String location, String company);

    Optional<Job> findByJobId(String jobId); // ✅ Returns Optional
    long countByPostedBy_EmpId(String empId);
    
    
 // ✅ Custom method to find jobs by employer ID (assumes postedBy is of type Employee)
    List<Job> findByPostedBy_EmpId(String empId);
}