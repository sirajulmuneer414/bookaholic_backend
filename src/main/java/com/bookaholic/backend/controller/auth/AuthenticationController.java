package com.bookaholic.backend.controller.auth;

import com.bookaholic.backend.DTO.auth.AuthenticationRequest;
import com.bookaholic.backend.DTO.auth.AuthenticationResponse;
import com.bookaholic.backend.DTO.auth.RegisterRequest;
import com.bookaholic.backend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> googleAuth(
            @RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        return ResponseEntity.ok(service.authenticateGoogle(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        service.processForgotPassword(email);
        return ResponseEntity.ok("If the email exists, a password reset link has been sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        service.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestBody com.bookaholic.backend.DTO.auth.VerifyOtpRequest request) {
        service.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(
            @RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        service.resendOtp(email);
        return ResponseEntity.ok("OTP has been resent to your email");
    }
}
