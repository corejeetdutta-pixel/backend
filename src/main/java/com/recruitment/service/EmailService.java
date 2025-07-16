package com.recruitment.service;

import java.util.Base64;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.recruitment.dto.UserDto;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send application email to employer.
     */
    public void sendApplicationEmail(
            String employerEmail,
            UserDto user,
            String jobTitle,
            String jobDescription,
            String answersJson,
            int score
    ) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(employerEmail);
        helper.setSubject("New Application for " + jobTitle);

        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Applicant Name:</strong> ").append(user.getName()).append("</p>");
        sb.append("<p><strong>Email:</strong> ").append(user.getEmail()).append("</p>");
        sb.append("<p><strong>Mobile:</strong> ").append(user.getMobile()).append("</p>");
        sb.append("<p><strong>Address:</strong> ").append(user.getAddress()).append("</p>");
        sb.append("<p><strong>Gender:</strong> ").append(user.getGender()).append("</p>");
        sb.append("<p><strong>Qualification:</strong> ").append(user.getQualification()).append("</p>");
        sb.append("<p><strong>Passout Year:</strong> ").append(user.getPassoutYear()).append("</p>");
        sb.append("<p><strong>Skills:</strong> ").append(user.getSkills()).append("</p>");
        sb.append("<p><strong>Job Title:</strong> ").append(jobTitle).append("</p>");
        sb.append("<p><strong>Job Description:</strong> ").append(jobDescription).append("</p>");
        sb.append("<p><strong>Score:</strong> ").append(score).append("</p>");
        sb.append("<p><strong>Answers:</strong><br><pre>").append(answersJson).append("</pre></p>");

        helper.setText(sb.toString(), true);

        // Handle resume (attachment)
        String resumeString = user.getResume();
        if (resumeString != null && !resumeString.isBlank()) {
            String base64Part = resumeString;

            // Check if it's a data URI
            if (resumeString.startsWith("data:application/pdf;base64,")) {
                base64Part = resumeString.substring("data:application/pdf;base64,".length());
            }

            if (isProbablyBase64(base64Part)) {
                try {
                    byte[] resumeBytes = Base64.getDecoder().decode(base64Part);
                    InputStreamSource attachment = new ByteArrayResource(resumeBytes);
                    helper.addAttachment("resume.pdf", attachment);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid Base64 resume, skipping attachment: " + e.getMessage());
                }
            } else {
                // Treat as URL
                sb.append("<p><strong>Resume URL:</strong> <a href=\"")
                  .append(resumeString)
                  .append("\">")
                  .append(resumeString)
                  .append("</a></p>");
                helper.setText(sb.toString(), true);
            }
        }

        mailSender.send(message);
    }

    /**
     * Heuristic to guess if a string looks like Base64.
     */
    private boolean isProbablyBase64(String input) {
        // Basic check: only contains base64 chars and has reasonable length
        return input.matches("^[A-Za-z0-9+/=\\r\\n]+$") && input.length() % 4 == 0;
    }
}
