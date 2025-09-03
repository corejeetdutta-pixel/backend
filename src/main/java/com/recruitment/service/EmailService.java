package com.recruitment.service;

import java.util.Base64;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.recruitment.dto.UserDto;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    
    /**
     * Sends login success notification email to employer
     */
    public void sendLoginNotification(String email, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Login Notification - Recruitment Portal");

            String content = "<h2>Login Successful</h2>" +
                             "<p>Hello <strong>" + name + "</strong>,</p>" +
                             "<p>You have successfully logged into your account.</p>" +
                             "<p>If this wasn’t you, please reset your password immediately.</p>" +
                             "<br><p>Regards,<br>Recruitment Portal Team</p>";

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("Login email sent to " + email);
        } catch (Exception e) {
            System.err.println("Failed to send login email: " + e.getMessage());
        }
    }

    

	 // Add this method to your EmailService
 // In EmailService.java
    public void sendVerificationEmail(String to, String name, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(to);
            helper.setSubject("Verify Your Email - Recruitment Portal");
            
            // Use environment variable for frontend URL or a config property
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                frontendUrl = "https://backend-n4w7.onrender.com"; // default for development
            }
            
            String verificationUrl = frontendUrl + "auth/user/verify-email?token=" + token;
            
            String content = "<h2>Email Verification</h2>" +
                             "<p>Hello <strong>" + name + "</strong>,</p>" +
                             "<p>Thank you for registering with our Recruitment Portal. " +
                             "Please click the link below to verify your email address:</p>" +
                             "<p><a href=\"" + verificationUrl + "\" style=\"background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block; border-radius: 5px;\">Verify Email</a></p>" +
                             "<p>This link will expire in 24 hours.</p>" +
                             "<p>If the button doesn't work, copy and paste this URL in your browser:</p>" +
                             "<p>" + verificationUrl + "</p>" +
                             "<br><p>Regards,<br>Recruitment Portal Team</p>";
            
            helper.setText(content, true);
            mailSender.send(message);
            
            System.out.println("Verification email sent to " + to);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    /**
     * Sends an application email to the employer with applicant details and resume.
     */
    public void sendApplicationEmail(
            String employerEmail,
            UserDto user,
            String jobTitle,
            String jobId,
            String jobDescription,
            String answersJson,
            int score
    ) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(employerEmail);
        helper.setSubject("New Application for " + jobTitle);

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Candidate Application Details</h2>");
        sb.append("<p><strong>Name:</strong> ").append(user.getName()).append("</p>");
        sb.append("<p><strong>Email:</strong> ").append(user.getEmail()).append("</p>");
        sb.append("<p><strong>Mobile:</strong> ").append(user.getMobile()).append("</p>");
        sb.append("<p><strong>Address:</strong> ").append(user.getAddress()).append("</p>");
        sb.append("<p><strong>Gender:</strong> ").append(user.getGender()).append("</p>");
        sb.append("<p><strong>Qualification:</strong> ").append(user.getQualification()).append("</p>");
        sb.append("<p><strong>Passout Year:</strong> ").append(user.getPassoutYear()).append("</p>");
        sb.append("<p><strong>Skills:</strong> ").append(user.getSkills()).append("</p>");
        sb.append("<p><strong>Job Title:</strong> ").append(jobTitle).append("</p>");
        sb.append("<p><strong>Job ID:</strong> ").append(jobId).append("</p>");
        sb.append("<p><strong>Job Description:</strong> ").append(jobDescription).append("</p>");
        sb.append("<p><strong>Score:</strong> ").append(score).append("</p>");
        sb.append("<p><strong>Answers:</strong><br><pre>").append(answersJson).append("</pre></p>");

        // Handle resume (either as attachment or link)
        String resumeString = user.getResume();
        boolean attached = false;

        if (resumeString != null && !resumeString.isBlank()) {
            String base64Part = resumeString;

            // If it starts with data URI
            if (resumeString.startsWith("data:application/pdf;base64,")) {
                base64Part = resumeString.substring("data:application/pdf;base64,".length());
            }

            if (isProbablyBase64(base64Part)) {
                try {
                    byte[] resumeBytes = Base64.getDecoder().decode(base64Part);
                    InputStreamSource attachment = new ByteArrayResource(resumeBytes);
                    helper.addAttachment("resume.pdf", attachment);
                    attached = true;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid Base64 resume, skipping attachment: " + e.getMessage());
                }
            }
        }

        if (!attached && resumeString != null && !resumeString.isBlank()) {
            // If not attached, send as URL in email
            sb.append("<p><strong>Resume URL:</strong> <a href=\"")
              .append(resumeString)
              .append("\">")
              .append(resumeString)
              .append("</a></p>");
        }

        helper.setText(sb.toString(), true);
        mailSender.send(message);
    }
    
 // Add to EmailService class
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML
            
            mailSender.send(message);
            System.out.println("HTML email sent to " + to);
        } catch (Exception e) {
            System.err.println("Failed to send HTML email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Heuristic to check if a string is likely Base64-encoded.
     */
    private boolean isProbablyBase64(String input) {
        return input.matches("^[A-Za-z0-9+/=\\r\\n]+$") && input.length() % 4 == 0;
    }
}
