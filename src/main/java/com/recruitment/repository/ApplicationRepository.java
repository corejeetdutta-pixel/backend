package com.recruitment.repository;

import com.recruitment.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByUserIdAndJobId(String userId, String jobId);
    long countByJob_PostedBy_EmpId(String empId);
    long countByJob_PostedBy_EmpIdAndStatus(String empId, String status);

    @Query("SELECT a FROM Application a JOIN a.job j WHERE j.postedBy.empId = :empId")
    List<Application> findApplicationsByEmployerId(@Param("empId") String empId);

    // FIXED: Use correct field names based on your Application entity
    @Query("SELECT a.id as applicationId, a.score as score, a.status as status, a.appliedAt as appliedAt, " +
            "u.id as userId, u.name as name, u.email as email, u.mobile as mobile, " +
            "u.qualification as qualification, u.gender as gender, u.dob as dob, u.address as address, " +
            "u.passoutYear as passoutYear, u.experience as experience, u.skills as skills, " +
            "u.linkedin as linkedin, u.github as github, u.department as department " +
            "FROM Application a " +
            "JOIN a.user u " +
            "JOIN a.job j " +
            "WHERE j.jobId = :jobId " +
            "ORDER BY a.appliedAt DESC")
    List<ApplicationProjection> findApplicationsByJobIdWithoutLob(@Param("jobId") String jobId);

    // Projection interface to avoid LOB loading
    interface ApplicationProjection {
        Long getApplicationId();
        Double getScore();
        String getStatus();
        java.time.LocalDateTime getAppliedAt();
        String getUserId();
        String getName();
        String getEmail();
        String getMobile();
        String getQualification();
        String getGender();
        String getDob();
        String getAddress();
        String getPassoutYear();
        String getExperience();
        List<String> getSkills();
        String getLinkedin();
        String getGithub();
        String getDepartment();
    }

    @Query(value = "SELECT " +
            "a.id as application_id, a.score as score, a.status as status, " +
            "u.id as user_id, u.name as name, u.email as email, u.mobile as mobile, " +
            "u.qualification as qualification, u.gender as gender, u.dob as dob, u.address as address, " +
            "u.passout_year as passout_year, u.experience as experience, u.linkedin as linkedin, " +
            "u.github as github, u.department as department, " +
            "CASE WHEN u.resume IS NOT NULL AND LENGTH(u.resume) > 0 THEN true ELSE false END as has_resume, " +
            "CASE WHEN u.profile_picture IS NOT NULL AND LENGTH(u.profile_picture) > 0 THEN true ELSE false END as has_profile_picture " +
            "FROM application a " +
            "JOIN users u ON a.user_id = u.id::text " +    // CAST u.id (Long) to TEXT
            "JOIN job j ON a.job_id = j.id::text " +       // CAST j.id (Long) to TEXT
            "WHERE j.job_id = :jobId " +
            "ORDER BY a.id DESC", nativeQuery = true)      // Removed applied_at, using id instead
    List<Object[]> findApplicationsByJobIdNative(@Param("jobId") String jobId);

    // Fixed method: use 'In' suffix for collection parameter
    List<Application> findByJobIdIn(List<String> jobIds);

    // Add this method to your ApplicationRepository interface
    @Query("SELECT a FROM Application a WHERE a.job.jobId = :jobId")
    List<Application> findByJobId(@Param("jobId") String jobId);

    @Transactional
    List<Application> findByUserId(String userId);

    // FIXED: Simple count query with correct field name
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.jobId = :jobId")
    int countByJobId(@Param("jobId") String jobId);

    // FIXED: Check if job exists and has applications
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Application a WHERE a.job.jobId = :jobId")
    boolean existsByJobId(@Param("jobId") String jobId);

    // FIXED: Use correct field names - removed problematic fields
    @Query("SELECT a.id, a.appliedAt, a.status, " +
            "u.id, u.name, u.email, u.mobile, u.resume " +
            "FROM Application a " +
            "JOIN a.user u " +
            "JOIN a.job j " +
            "WHERE j.jobId = :jobId " +
            "ORDER BY a.appliedAt DESC")
    List<Object[]> findApplicantsByJobId(@Param("jobId") String jobId);

    // FIXED: Alternative with correct field names
    @Query("SELECT a FROM Application a " +
            "JOIN FETCH a.user u " +
            "JOIN FETCH a.job j " +
            "WHERE j.jobId = :jobId " +
            "ORDER BY a.appliedAt DESC")
    List<Application> findApplicationsByJobId(@Param("jobId") String jobId);

    // SIMPLEST WORKING VERSION - Use this one first
    @Query("SELECT a.id, u.name, u.email, u.mobile, a.appliedAt, a.status " +
            "FROM Application a " +
            "JOIN a.user u " +
            "JOIN a.job j " +
            "WHERE j.jobId = :jobId " +
            "ORDER BY a.appliedAt DESC")
    List<Object[]> findSimpleApplicantsByJobId(@Param("jobId") String jobId);
}