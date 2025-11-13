package com.recruitment.repository;

import com.recruitment.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByLocationIgnoreCase(String location);
    List<Job> findByCompanyIgnoreCase(String company);

    @Query("SELECT j FROM Job j WHERE " +
           "(:location IS NULL OR LOWER(j.location) = LOWER(:location)) AND " +
           "(:company IS NULL OR LOWER(j.company) = LOWER(:company))")
    List<Job> filterJobs(String location, String company);

    Optional<Job> findByJobId(String jobId);
    Optional<Job> findByShortId(String shortId);
    long countByPostedBy_EmpId(String empId);
    List<Job> findByPostedBy_EmpId(String empId);

    @Query("SELECT j FROM Job j JOIN j.applicants u WHERE u.userId = :userId")
    List<Job> findJobsByUserId(@Param("userId") String userId);
}