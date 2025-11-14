package com.recruitment.service;

import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.recruitment.entity.User;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.frontendUrl:http://localhost:5173}")
    private String frontendUrl;

    public enum EmailType {
        USER_VERIFICATION,
        EMPLOYEE_VERIFICATION,
        LOGIN_NOTIFICATION,
        PASSWORD_RESET,
        APPLICATION_SUBMISSION
    }

    private String extractToken(String tokenOrUrl) {
        if (tokenOrUrl == null) return "";
        String s = tokenOrUrl.trim();

        try {
            if (s.contains("token=")) {
                int idx = s.lastIndexOf("token=");
                String t = s.substring(idx + "token=".length());
                int amp = t.indexOf('&');
                if (amp > -1) t = t.substring(0, amp);
                int hash = t.indexOf('#');
                if (hash > -1) t = t.substring(0, hash);
                return URLDecoder.decode(t, StandardCharsets.UTF_8.name());
            }
            return s;
        } catch (Exception e) {
            System.err.println("⚠️ Token extraction failed for: " + tokenOrUrl + " — " + e.getMessage());
            return s;
        }
    }

    public void sendVerificationEmail(String to, String name, String tokenOrUrl) {
        sendVerificationEmail(to, name, tokenOrUrl, EmailType.USER_VERIFICATION);
    }

    public void sendVerificationEmail(String to, String name, String tokenOrUrl, EmailType emailType) {
        try {
            String token = extractToken(tokenOrUrl);
            String verificationUrl;
            String subject;
            String userType;
            String welcomeMessage;

            switch (emailType) {
                case EMPLOYEE_VERIFICATION:
                    verificationUrl = frontendUrl + "/verify-employee?token=" + token;
                    subject = "Verify Your Employer Account - Recruitment Portal";
                    userType = "Employer";
                    welcomeMessage = "Thank you for registering as an Employer with our Recruitment Portal.";
                    break;
                case USER_VERIFICATION:
                default:
                    verificationUrl = frontendUrl + "/verify-email?token=" + token;
                    subject = "Verify Your Candidate Account - Recruitment Portal";
                    userType = "Candidate";
                    welcomeMessage = "Thank you for registering as a Candidate with our Recruitment Portal.";
                    break;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            String content = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>
                    <h2 style='color: #0260a4;'>%s Email Verification</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>%s</p>
                    <p>Please click the button below to verify your email address:</p>
                    <div style='text-align: center; margin: 30px 0;'>
                        <a href="%s" style="background-color: #4CAF50; color: white; padding: 12px 24px;
                            text-align: center; text-decoration: none; display: inline-block;
                            border-radius: 5px; font-size: 16px;">Verify Email Address</a>
                    </div>
                    <p>This link will expire in 24 hours.</p>
                    <p>If the button doesn't work, copy and paste this URL in your browser:</p>
                    <p style='background-color: #f5f5f5; padding: 10px; border-radius: 4px; word-break: break-all;'>%s</p>
                    <br><p>Regards,<br>Recruitment Portal Team</p>
                </div>
            """.formatted(userType, name, welcomeMessage, verificationUrl, verificationUrl);

            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendLoginNotification(String email, String name) {
        sendLoginNotification(email, name, EmailType.USER_VERIFICATION);
    }

    public void sendLoginNotification(String email, String name, EmailType userType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);

            String accountType = userType == EmailType.EMPLOYEE_VERIFICATION ? "employer" : "candidate";
            helper.setSubject("Login Notification - Recruitment Portal");

            String content = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>
                    <h2 style='color: #0260a4;'>Login Successful</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>You have successfully logged into your %s account.</p>
                    <p>If this wasn't you, please reset your password immediately.</p>
                    <br><p>Regards,<br>Recruitment Portal Team</p>
                </div>
            """.formatted(name, accountType);

            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send login notification: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String name, String resetToken, EmailType userType) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            String accountType = userType == EmailType.EMPLOYEE_VERIFICATION ? "employer" : "candidate";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Password Reset Request - Recruitment Portal");

            String content = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>
                    <h2 style='color: #0260a4;'>Password Reset Request</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>We received a request to reset your %s account password.</p>
                    <p>Please click the button below to reset your password:</p>
                    <div style='text-align: center; margin: 30px 0;'>
                        <a href="%s" style="background-color: #FF6B6B; color: white; padding: 12px 24px;
                            text-align: center; text-decoration: none; display: inline-block;
                            border-radius: 5px; font-size: 16px;">Reset Password</a>
                    </div>
                    <p>This link will expire in 2 hours.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                    <p>If the button doesn't work, copy and paste this URL in your browser:</p>
                    <p style='background-color: #f5f5f5; padding: 10px; border-radius: 4px; word-break: break-all;'>%s</p>
                    <br><p>Regards,<br>Recruitment Portal Team</p>
                </div>
            """.formatted(name, accountType, resetUrl, resetUrl);

            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email: " + e.getMessage());
        }
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send simple email: " + e.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send HTML email: " + e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    // ✅ UPDATED: Application email with User entity instead of UserDto
    public void sendApplicationEmail(
            String employerEmail,
            User user,
            String jobTitle,
            String jobId,
            String jobDescription,
            String answersJson,
            int score
    ) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(employerEmail);
        helper.setSubject("New Application for " + jobTitle);

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Candidate Application Details</h2>")
                .append("<p><strong>Name:</strong> ").append(user.getName()).append("</p>")
                .append("<p><strong>Email:</strong> ").append(user.getEmail()).append("</p>")
                .append("<p><strong>Mobile:</strong> ").append(user.getMobile()).append("</p>")
                .append("<p><strong>Address:</strong> ").append(user.getAddress()).append("</p>")
                .append("<p><strong>Gender:</strong> ").append(user.getGender()).append("</p>")
                .append("<p><strong>Qualification:</strong> ").append(user.getQualification()).append("</p>")
                .append("<p><strong>Passout Year:</strong> ").append(user.getPassoutYear()).append("</p>")
                .append("<p><strong>Date of Birth:</strong> ").append(user.getDob()).append("</p>")
                .append("<p><strong>Experience:</strong> ").append(user.getExperience()).append(" years</p>")
                .append("<p><strong>LinkedIn:</strong> ").append(user.getLinkedin()).append("</p>")
                .append("<p><strong>GitHub:</strong> ").append(user.getGithub()).append("</p>")
                .append("<p><strong>Skills:</strong> ").append(user.getSkills()).append("</p>")
                .append("<p><strong>Job Title:</strong> ").append(jobTitle).append("</p>")
                .append("<p><strong>Job ID:</strong> ").append(jobId).append("</p>")
                .append("<p><strong>Job Description:</strong> ").append(jobDescription).append("</p>")
                .append("<p><strong>Score:</strong> ").append(score).append("</p>")
                .append("<p><strong>Answers:</strong><br><pre>").append(answersJson).append("</pre></p>");

        // ✅ FIXED: Resume attachment handling with byte[]
        byte[] resume = user.getResume();
        boolean attached = false;

        if (resume != null && resume.length > 0) {
            try {
                InputStreamSource attachment = new ByteArrayResource(resume);
                String fileName = user.getResumeFileName() != null ?
                        user.getResumeFileName() : user.getName() + "_resume.pdf";
                helper.addAttachment(fileName, attachment);
                attached = true;
                System.out.println("✅ Resume attached: " + fileName + " (" + resume.length + " bytes)");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to attach resume: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!attached) {
            sb.append("<p style='color: red;'><em>No resume attached to this application.</em></p>");
            System.out.println("⚠️ No resume found for user: " + user.getName());
        }

        helper.setText(sb.toString(), true);
        mailSender.send(message);
        System.out.println("✅ Application email sent to employer: " + employerEmail);
    }

    // ✅ NEW: Overloaded method for backward compatibility with UserDto
    public void sendApplicationEmail(
            String employerEmail,
            com.recruitment.dto.UserDto userDto,
            String jobTitle,
            String jobId,
            String jobDescription,
            String answersJson,
            int score
    ) throws Exception {

        // Convert UserDto to User entity (minimal conversion for email purposes)
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setMobile(userDto.getMobile());
        user.setAddress(userDto.getAddress());
        user.setGender(userDto.getGender());
        user.setQualification(userDto.getQualification());
        user.setPassoutYear(userDto.getPassoutYear());
        user.setDob(userDto.getDob());
        user.setExperience(userDto.getExperience());
        user.setLinkedin(userDto.getLinkedin());
        user.setGithub(userDto.getGithub());
        user.setSkills(userDto.getSkills());

        // Note: Resume cannot be converted from UserDto as it's removed from DTO

        sendApplicationEmail(employerEmail, user, jobTitle, jobId, jobDescription, answersJson, score);
    }

    // ✅ NEW: Method to send application confirmation to candidate
    public void sendApplicationConfirmationToCandidate(
            String candidateEmail,
            String candidateName,
            String jobTitle,
            String companyName
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(candidateEmail);
            helper.setSubject("Application Submitted Successfully - " + jobTitle);

            String content = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>
                    <h2 style='color: #0260a4;'>Application Submitted Successfully!</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Your application for the position of <strong>%s</strong> at <strong>%s</strong> has been submitted successfully.</p>
                    <p>We will review your application and contact you if your profile matches our requirements.</p>
                    <br>
                    <p><strong>Application Details:</strong></p>
                    <ul>
                        <li><strong>Position:</strong> %s</li>
                        <li><strong>Company:</strong> %s</li>
                        <li><strong>Applied On:</strong> %s</li>
                    </ul>
                    <br>
                    <p>You can check your application status in your candidate dashboard.</p>
                    <br>
                    <p>Best regards,<br>Recruitment Portal Team</p>
                </div>
            """.formatted(
                    candidateName,
                    jobTitle,
                    companyName,
                    jobTitle,
                    companyName,
                    java.time.LocalDate.now().toString()
            );

            helper.setText(content, true);
            mailSender.send(message);
            System.out.println("✅ Application confirmation sent to candidate: " + candidateEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send application confirmation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ NEW: Method to send application status update
    public void sendApplicationStatusUpdate(
            String candidateEmail,
            String candidateName,
            String jobTitle,
            String companyName,
            String status,
            String feedback
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(candidateEmail);
            helper.setSubject("Application Status Update - " + jobTitle);

            String statusColor = "blue";
            if ("REJECTED".equalsIgnoreCase(status)) {
                statusColor = "red";
            } else if ("SELECTED".equalsIgnoreCase(status)) {
                statusColor = "green";
            } else if ("SHORTLISTED".equalsIgnoreCase(status)) {
                statusColor = "orange";
            }

            String content = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>
                    <h2 style='color: #0260a4;'>Application Status Update</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Your application for <strong>%s</strong> at <strong>%s</strong> has been updated.</p>
                    <br>
                    <div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 4px solid %s;'>
                        <p style='margin: 0;'><strong>Status:</strong> <span style='color: %s; font-weight: bold;'>%s</span></p>
                    </div>
                    %s
                    <br>
                    <p>Best regards,<br>Recruitment Portal Team</p>
                </div>
            """.formatted(
                    candidateName,
                    jobTitle,
                    companyName,
                    statusColor,
                    statusColor,
                    status,
                    feedback != null ? "<br><p><strong>Feedback:</strong> " + feedback + "</p>" : ""
            );

            helper.setText(content, true);
            mailSender.send(message);
            System.out.println("✅ Application status update sent to: " + candidateEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send application status update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}