package com.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.recruitment.entity.Employee;
import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    Optional<Employee> findByEmail(String email); // ✅ required for login
    Optional<Employee> findByEmpId(String empId);
    // ✅ Add this method to check if GST number exists
    boolean existsByGstNumber(String gstNumber);
    

}
