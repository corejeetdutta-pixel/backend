package com.recruitment.dto;

import java.util.List;

public class UserDto {
    private String name;
    private String email;
    private String password;
    private String mobile;
    private String address;
    private String gender;
    private String qualification;
    private String passoutYear;
    private List<String> skills;
    private String profilePicture; // base64
    private String resume; // base64

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
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }
}
