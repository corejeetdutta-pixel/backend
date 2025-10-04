//package com.recruitment.service;
//
//import com.sendgrid.*;
//import com.sendgrid.helpers.mail.Mail;
//import com.sendgrid.helpers.mail.objects.Content;
//import com.sendgrid.helpers.mail.objects.Email;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//
//@Service
//public class SendGridEmailService {
//
//    @Value("${sendgrid.api.key:}")
//    private String sendGridApiKey;
//
//    @Value("${spring.mail.username:recruitment@yourdomain.com}")
//    private String fromEmail;
//
//    public void sendVerificationEmail(String to, String name, String verificationUrl) {
//        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
//            System.err.println("SendGrid API key not configured");
//            return;
//        }
//
//        try {
//            Email from = new Email(fromEmail);
//            String subject = "Verify Your Email - Recruitment Portal";
//            Email toEmail = new Email(to);
//            
//            String contentText = "Hello " + name + ",\n\n" +
//                    "Please verify your email by clicking: " + verificationUrl + "\n\n" +
//                    "This link expires in 24 hours.\n\n" +
//                    "Regards,\nRecruitment Portal Team";
//
//            String contentHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
//                    "<h2 style='color: #0260a4;'>Email Verification</h2>" +
//                    "<p>Hello <strong>" + name + "</strong>,</p>" +
//                    "<p>Thank you for registering with our Recruitment Portal. " +
//                    "Please click the link below to verify your email address:</p>" +
//                    "<div style='text-align: center; margin: 30px 0;'>" +
//                    "<a href=\"" + verificationUrl + "\" style=\"background-color: #4CAF50; color: white; padding: 12px 24px; text-align: center; text-decoration: none; display: inline-block; border-radius: 5px; font-size: 16px;\">Verify Email Address</a>" +
//                    "</div>" +
//                    "<p>This link will expire in 24 hours.</p>" +
//                    "<p>If the button doesn't work, copy and paste this URL in your browser:</p>" +
//                    "<p style='background-color: #f5f5f5; padding: 10px; border-radius: 4px; word-break: break-all;'>" + verificationUrl + "</p>" +
//                    "<br><p>Regards,<br>Recruitment Portal Team</p>" +
//                    "</div>";
//
//            Content content = new Content("text/html", contentHtml);
//            Mail mail = new Mail(from, subject, toEmail, content);
//
//            SendGrid sg = new SendGrid(sendGridApiKey);
//            Request request = new Request();
//            
//            request.setMethod(Method.POST);
//            request.setEndpoint("mail/send");
//            request.setBody(mail.build());
//            
//            Response response = sg.api(request);
//            
//            System.out.println("SendGrid Response Status: " + response.getStatusCode());
//            System.out.println("SendGrid Response Body: " + response.getBody());
//            System.out.println("SendGrid Response Headers: " + response.getHeaders());
//
//            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
//                System.out.println("✅ Verification email sent via SendGrid to: " + to);
//            } else {
//                System.err.println("❌ SendGrid email failed: " + response.getStatusCode() + " - " + response.getBody());
//            }
//            
//        } catch (IOException e) {
//            System.err.println("❌ SendGrid email error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public void sendLoginNotification(String email, String name) {
//        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
//            System.err.println("SendGrid API key not configured");
//            return;
//        }
//
//        try {
//            Email from = new Email(fromEmail);
//            String subject = "Login Notification - Recruitment Portal";
//            Email toEmail = new Email(email);
//            
//            String contentHtml = "<h2>Login Successful</h2>" +
//                    "<p>Hello <strong>" + name + "</strong>,</p>" +
//                    "<p>You have successfully logged into your account.</p>" +
//                    "<p>If this wasn't you, please reset your password immediately.</p>" +
//                    "<br><p>Regards,<br>Recruitment Portal Team</p>";
//
//            Content content = new Content("text/html", contentHtml);
//            Mail mail = new Mail(from, subject, toEmail, content);
//
//            SendGrid sg = new SendGrid(sendGridApiKey);
//            Request request = new Request();
//            request.setMethod(Method.POST);
//            request.setEndpoint("mail/send");
//            request.setBody(mail.build());
//            
//            Response response = sg.api(request);
//            
//            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
//                System.out.println("Login notification sent to " + email);
//            } else {
//                System.err.println("SendGrid login notification failed: " + response.getStatusCode());
//            }
//            
//        } catch (IOException e) {
//            System.err.println("SendGrid login notification error: " + e.getMessage());
//        }
//    }
//}