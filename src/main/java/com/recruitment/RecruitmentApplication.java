package com.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.recruitment")
@EntityScan(basePackages = "com.recruitment.entity")
@EnableJpaRepositories(basePackages = "com.recruitment.repository")
public class RecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentApplication.class, args);
        System.out.println("✅ Recruitment backend started successfully!");
    }
}
