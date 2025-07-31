package com.recruitment.service;

import java.util.Base64;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import com.recruitment.dto.UserDto;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

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
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(employerEmail);
        helper.setSubject("New Application for " + HtmlUtils.htmlEscape(jobTitle));

        StringBuilder sb = new StringBuilder();
        appendEscaped(sb, "Applicant Name", user.getName());
        appendEscaped(sb, "Email", user.getEmail());
        appendEscaped(sb, "Mobile", user.getMobile());
        appendEscaped(sb, "Address", user.getAddress());
        appendEscaped(sb, "Gender", user.getGender());
        appendEscaped(sb, "Qualification", user.getQualification());
        appendEscaped(sb, "Passout Year", user.getPassoutYear());
        appendEscaped(sb, "Skills", user.getSkills());
        appendEscaped(sb, "Job Title", jobTitle);
        appendEscaped(sb, "Job Id", jobId);
        appendEscaped(sb, "Job Description", jobDescription);
        sb.append("<p><strong>Score:</strong> ").append(score).append("</p>");
        sb.append("<p><strong>Answers:</strong><br><pre>")
          .append(HtmlUtils.htmlEscape(answersJson))
          .append("</pre></p>");

        // Initial set of email body
        helper.setText(sb.toString(), true);

        // Handle resume
        String resumeString = user.getResume();
        if (resumeString != null && !resumeString.isBlank()) {
            String base64Part = extractBase64Part(resumeString);
            String cleanBase64 = cleanBase64String(base64Part);

            if (isValidBase64(cleanBase64)) {
                try {
                    byte[] resumeBytes = Base64.getDecoder().decode(cleanBase64);
                    InputStreamSource attachment = new ByteArrayResource(resumeBytes);
                    helper.addAttachment("resume.pdf", attachment);
                } catch (IllegalArgumentException e) {
                    appendResumeLink(sb, resumeString);
                    helper.setText(sb.toString(), true);
                }
            } else {
                appendResumeLink(sb, resumeString);
                helper.setText(sb.toString(), true);
            }
        }

        mailSender.send(message);
    }

    private void appendEscaped(StringBuilder sb, String label, String value) {
        sb.append("<p><strong>")
          .append(HtmlUtils.htmlEscape(label))
          .append(":</strong> ")
          .append(HtmlUtils.htmlEscape(value))
          .append("</p>");
    }

    private String extractBase64Part(String resumeString) {
        if (resumeString.startsWith("data:") && resumeString.contains("base64,")) {
            return resumeString.split("base64,", 2)[1];
        }
        return resumeString;
    }

    private String cleanBase64String(String base64) {
        return base64.replaceAll("\\s", ""); // Remove all whitespace
    }

    private boolean isValidBase64(String input) {
        return input.matches("^[A-Za-z0-9+/=]+$") && input.length() % 4 == 0;
    }

    private void appendResumeLink(StringBuilder sb, String resumeString) {
        String escapedUrl = HtmlUtils.htmlEscape(resumeString);
        sb.append("<p><strong>Resume:</strong> <a href=\"")
          .append(escapedUrl)
          .append("\">")
          .append(escapedUrl)
          .append("</a></p>");
    }
}