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

    
}