package com.recruitment.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class UserRegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String mobile;
    private String address;
    private String gender;
    private String qualification;
    private String passoutYear;
    private List<String> skills;
    private MultipartFile profilePicture;
    private MultipartFile resume;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getPassoutYear() { return passoutYear; }
    public void setPassoutYear(String passoutYear) { this.passoutYear = passoutYear; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public MultipartFile getProfilePicture() { return profilePicture; }
    public void setProfilePicture(MultipartFile profilePicture) { this.profilePicture = profilePicture; }

    public MultipartFile getResume() { return resume; }
    public void setResume(MultipartFile resume) { this.resume = resume; }
}

