package com.recruitment.service;

import java.util.Base64;
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

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ====================================
    // Extract the actual token from URL or raw string
    // ====================================
    private String extractToken(String tokenOrUrl) {
        if (tokenOrUrl == null) return "";
        String s = tokenOrUrl.trim();

        try {
            // If full backend URL was passed
            if (s.contains("token=")) {
                int idx = s.lastIndexOf("token=");
                String t = s.substring(idx + "token=".length());
                // Strip off any trailing params or anchors
                int amp = t.indexOf('&');
                if (amp > -1) t = t.substring(0, amp);
                int hash = t.indexOf('#');
                if (hash > -1) t = t.substring(0, hash);
                return URLDecoder.decode(t, StandardCharsets.UTF_8.name());
            }
            // Otherwise treat as plain token
            return s;
        } catch (Exception e) {
            System.err.println("⚠️ Token extraction failed for: " + tokenOrUrl + " — " + e.getMessage());
            return s;
        }
    }

    // ====================================
    // Simple text email
    // ====================================
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("📧 Simple email sent to " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send simple email: " + e.getMessage());
        }
    }

    // ====================================
    // Verification email
    // ====================================
    public void sendVerificationEmail(String to, String name, String tokenOrUrl) {
        try {
            String token = extractToken(tokenOrUrl);
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            System.out.println("📨 Sending verification email to: " + to);
            System.out.println("   raw input: " + tokenOrUrl);
            System.out.println("   extracted token: " + token);
            System.out.println("🔗 Final verification URL: " + verificationUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Email - Recruitment Portal");

            String content = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>
                    <h2 style='color: #0260a4;'>Email Verification</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Thank you for registering with our Recruitment Portal.</p>
                    <p>Please click the button below to verify your email address:</p>
                    <div style='text-align: center; margin: 30px 0;'>
                        <a href="%s" style="background-color: #4CAF50; color: white; padding: 12px 24px;
                            text-align: center; text-decoration: none; display: inline-block;
                            border-radius: 5px; font-size: 16px;">Verify Email Address</a>
                    </div>
                    <p>This link will expire in 24 hours.</p>
                    <p>If the button doesn’t work, copy and paste this URL in your browser:</p>
                    <p style='background-color: #f5f5f5; padding: 10px; border-radius: 4px; word-break: break-all;'>%s</p>
                    <br><p>Regards,<br>Recruitment Portal Team</p>
                </div>
            """.formatted(name, tokenOrUrl, tokenOrUrl);

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("✅ Verification email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
        }
    }

    // ====================================
    // Login notification email
    // ====================================
    public void sendLoginNotification(String email, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Login Notification - Recruitment Portal");

            String content = """
                <div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto;'>
                    <h2 style='color: #0260a4;'>Login Successful</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>You have successfully logged into your account.</p>
                    <p>If this wasn’t you, please reset your password immediately.</p>
                    <br><p>Regards,<br>Recruitment Portal Team</p>
                </div>
            """.formatted(name);

            helper.setText(content, true);
            mailSender.send(message);
            System.out.println("✅ Login notification email sent to " + email);
        } catch (Exception e) {
            System.err.println("❌ Failed to send login notification: " + e.getMessage());
        }
    }

    // ====================================
    // Generic HTML email (for JobController)
    // ====================================
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ HTML email sent to " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send HTML email: " + e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    // ====================================
    // Application submission email
    // ====================================
    public void sendApplicationEmail(
            String employerEmail,
            com.recruitment.dto.UserDto user,
            String jobTitle,
            String jobId,
            String jobDescription,
            String answersJson,
            int score
    ) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

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
                .append("<p><strong>Skills:</strong> ").append(user.getSkills()).append("</p>")
                .append("<p><strong>Job Title:</strong> ").append(jobTitle).append("</p>")
                .append("<p><strong>Job ID:</strong> ").append(jobId).append("</p>")
                .append("<p><strong>Job Description:</strong> ").append(jobDescription).append("</p>")
                .append("<p><strong>Score:</strong> ").append(score).append("</p>")
                .append("<p><strong>Answers:</strong><br><pre>").append(answersJson).append("</pre></p>");

        String resumeString = user.getResume();
        boolean attached = false;

        if (resumeString != null && !resumeString.isBlank()) {
            String base64Part = resumeString;
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
                    System.out.println("⚠️ Invalid Base64 resume: " + e.getMessage());
                }
            }
        }

        if (!attached && resumeString != null && !resumeString.isBlank()) {
            sb.append("<p><strong>Resume URL:</strong> <a href=\"")
                    .append(resumeString)
                    .append("\">")
                    .append(resumeString)
                    .append("</a></p>");
        }

        helper.setText(sb.toString(), true);
        mailSender.send(message);
        System.out.println("✅ Application email sent to employer: " + employerEmail);
    }

    private boolean isProbablyBase64(String input) {
        return input.matches("^[A-Za-z0-9+/=\\r\\n]+$") && input.length() % 4 == 0;
    }
}
