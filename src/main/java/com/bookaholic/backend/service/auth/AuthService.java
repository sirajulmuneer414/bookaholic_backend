package com.bookaholic.backend.service.auth;

import com.bookaholic.backend.DTO.auth.AuthenticationRequest;
import com.bookaholic.backend.DTO.auth.AuthenticationResponse;
import com.bookaholic.backend.DTO.auth.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse authenticateGoogle(String idTokenString);

    // Password Reset
    void processForgotPassword(String email);

    void resetPassword(String token, String newPassword);

    // OTP Verification
    void verifyOtp(String email, String otp);
    void resendOtp(String email);
}
