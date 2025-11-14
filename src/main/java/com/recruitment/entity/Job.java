package com.recruitment.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.recruitment.util.ShortIdGenerator;
import jakarta.persistence.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "job_id", nullable = false, updatable = false, unique = true)
    private String jobId;

    @Column(name = "short_id", nullable = false, updatable = false, unique = true)
    private String shortId;

    private String title;
    private String company;
    private String location;
    private String highestQualification;
    private String minSalary;
    private String maxSalary;
    private String experience;

    @Column(name = "job_type")
    private String jobType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_email")
    private String contactEmail;

    private String department;

    @Column(name = "employment_type")
    private String employmentType;

    private int openings;

    @Column(name = "open_date")
    private LocalDate openingDate;

    @Column(name = "close_date")
    private LocalDate lastDate;

    private String mode;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String perks;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id")
    @JsonIgnore
    private Employee postedBy;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Application> applications;

    // Many-to-Many relationship with User (inverse side)
    @ManyToMany(mappedBy = "appliedJobs", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"appliedJobs", "password", "verificationToken"}) // Avoid sensitive data and circular references
    private Set<User> applicants = new HashSet<>();

    public Job() {
    }

    // Constructor (update to include applicants)


    public Job(long id, String jobId, String title, String company, String location, String highestQualification, String minSalary, String maxSalary, String experience, String jobType, String description, String contactEmail, String department, String employmentType, String shortId, int openings, LocalDate openingDate, LocalDate lastDate, String mode, String requirements, String perks, String responsibilities, Employee postedBy, Set<Application> applications, Set<User> applicants) {
        this.id = id;
        this.jobId = jobId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.highestQualification = highestQualification;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.experience = experience;
        this.jobType = jobType;
        this.description = description;
        this.contactEmail = contactEmail;
        this.department = department;
        this.employmentType = employmentType;
        this.shortId = shortId;
        this.openings = openings;
        this.openingDate = openingDate;
        this.lastDate = lastDate;
        this.mode = mode;
        this.requirements = requirements;
        this.perks = perks;
        this.responsibilities = responsibilities;
        this.postedBy = postedBy;
        this.applications = applications;
        this.applicants = applicants;
    }

    @PrePersist
    public void prePersist() {
        if (this.jobId == null) {
            this.jobId = UUID.randomUUID().toString();
        }
        if (this.shortId == null || this.shortId.isEmpty()) {
            this.shortId = ShortIdGenerator.generateShortId();
        }
    }

    // Getters and Setters (add getters/setters for applicants)


    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getHighestQualification() { return highestQualification; }
    public void setHighestQualification(String highestQualification) { this.highestQualification = highestQualification; }

    public String getMinSalary() { return minSalary; }
    public void setMinSalary(String minSalary) { this.minSalary = minSalary; }

    public String getMaxSalary() { return maxSalary; }
    public void setMaxSalary(String maxSalary) { this.maxSalary = maxSalary; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public int getOpenings() { return openings; }
    public void setOpenings(int openings) { this.openings = openings; }

    public LocalDate getOpeningDate() { return openingDate; }
    public void setOpeningDate(LocalDate openingDate) { this.openingDate = openingDate; }

    public LocalDate getLastDate() { return lastDate; }
    public void setLastDate(LocalDate lastDate) { this.lastDate = lastDate; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getPerks() { return perks; }
    public void setPerks(String perks) { this.perks = perks; }

    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

    public Employee getPostedBy() { return postedBy; }
    public void setPostedBy(Employee postedBy) { this.postedBy = postedBy; }

    public Set<Application> getApplications() { return applications; }
    public void setApplications(Set<Application> applications) { this.applications = applications; }

    public Set<User> getApplicants() { return applicants; }
    public void setApplicants(Set<User> applicants) { this.applicants = applicants; }
}