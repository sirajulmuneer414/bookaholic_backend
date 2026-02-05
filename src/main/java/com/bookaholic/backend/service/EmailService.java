package com.bookaholic.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Bookaholic Library");

            String htmlContent = buildEmailTemplate(resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendOtpEmail(String toEmail, String otp, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email - Bookaholic Library");

            String htmlContent = buildOtpEmailTemplate(otp, userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP verification email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailTemplate(String otp, String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #10b981 0%, #047857 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
                +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".otp-box { background: white; border: 2px dashed #10b981; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }"
                +
                ".otp-code { font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #10b981; font-family: 'Courier New', monospace; }"
                +
                ".warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px; margin: 20px 0; border-radius: 4px; }"
                +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Welcome to Bookaholic!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>Thank you for registering with Bookaholic Library. Please verify your email address using the One-Time Password (OTP) below:</p>"
                +
                "<div class='otp-box'>" +
                "<p style='margin: 0; color: #666; font-size: 14px;'>Your Verification Code</p>" +
                "<div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "<p style='margin: 0;'><strong>⏰ This code will expire in 10 minutes.</strong></p>" +
                "</div>" +
                "<p>Enter this code on the verification page to complete your registration.</p>" +
                "<p>If you didn't create an account with Bookaholic, you can safely ignore this email.</p>" +
                "<p>Best regards,<br>The Bookaholic Team</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>This is an automated email, please do not reply.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildEmailTemplate(String resetLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #10b981 0%, #047857 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }"
                +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".button { display: inline-block; padding: 12px 30px; background: #10b981; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }"
                +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Password Reset Request</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello,</p>" +
                "<p>We received a request to reset your password for your Bookaholic Library account.</p>" +
                "<p>Click the button below to reset your password:</p>" +
                "<p style='text-align: center;'>" +
                "<a href='" + resetLink + "' class='button'>Reset Password</a>" +
                "</p>" +
                "<p>Or copy and paste this link into your browser:</p>" +
                "<p style='word-break: break-all; background: white; padding: 10px; border-radius: 5px;'>" + resetLink
                + "</p>" +
                "<p><strong>This link will expire in 1 hour.</strong></p>" +
                "<p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>"
                +
                "<p>Best regards,<br>Bookaholic Team</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>This is an automated email, please do not reply.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
