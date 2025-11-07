package com.recruitment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, message = "Job title must be at least 3 characters long")
    private String title;

    @NotBlank(message = "Company name is required")
    private String company;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @NotBlank(message = "Experience is required")
    private String experience;

    @NotBlank(message = "Highest qualification is required")
    private String highestQualification;

    @NotBlank(message = "Mode is required")
    private String mode;

    @Positive(message = "Openings must be greater than 0")
    private int openings;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotNull(message = "Opening date is required")
    private LocalDate openingDate;

    @NotNull(message = "Closing date is required")
    private LocalDate lastDate;

    @Email(message = "Invalid contact email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    @Pattern(regexp = "\\d+(\\.\\d+)?[L]?", message = "Invalid min salary format")
    private String minSalary;

    @Pattern(regexp = "\\d+(\\.\\d+)?[L]?", message = "Invalid max salary format")
    private String maxSalary;

    private String responsibilities;
    private String requirements;
    private String perks;
}
