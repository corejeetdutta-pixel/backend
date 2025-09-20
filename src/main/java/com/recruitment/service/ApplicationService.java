package com.recruitment.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.entity.Application;
import com.recruitment.repository.ApplicationRepository;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    public List<Application> getApplicationsByEmployer(String empId) {
        return applicationRepository.findApplicationsByEmployerId(empId);
    }
    
    @Transactional // Add this annotation
    public List<Application> getApplicationsByUser(String userId) {
        return applicationRepository.findByUserId(userId);
    }
}

