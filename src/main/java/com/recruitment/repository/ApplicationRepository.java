package com.recruitment.repository;

import com.recruitment.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUserId(String userId);
    boolean existsByUserIdAndJobId(String userId, String jobId);
    long countByJob_PostedBy_EmpId(String empId);
    long countByJob_PostedBy_EmpIdAndStatus(String empId, String status);
    
    @Query("SELECT a FROM Application a JOIN a.job j WHERE j.postedBy.empId = :empId")
    List<Application> findApplicationsByEmployerId(@Param("empId") String empId);
    
    @Query("SELECT a FROM Application a JOIN FETCH a.user WHERE a.jobId = :jobId")
    List<Application> findApplicationsByJobIdWithUser(@Param("jobId") String jobId);
    
    // Fixed method: use 'In' suffix for collection parameter
    List<Application> findByJobIdIn(List<String> jobIds);  // ✅ Corrected method name
    
    
	 // Add this method to your ApplicationRepository interface
	 @Query("SELECT a FROM Application a WHERE a.jobId = :jobId")
	 List<Application> findByJobId(@Param("jobId") String jobId);
}