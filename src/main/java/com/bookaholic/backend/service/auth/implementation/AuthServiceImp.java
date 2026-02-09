package com.bookaholic.backend.service.auth.implementation;

import com.bookaholic.backend.DTO.auth.AuthenticationRequest;
import com.bookaholic.backend.DTO.auth.AuthenticationResponse;
import com.bookaholic.backend.DTO.auth.RegisterRequest;
import com.bookaholic.backend.config.JwtService;
import com.bookaholic.backend.entity.User;
import com.bookaholic.backend.entity.enums.AuthProvider;
import com.bookaholic.backend.entity.enums.Role;
import com.bookaholic.backend.exceptions.UnableToRegisterException;
import com.bookaholic.backend.repository.UserRepository;
import com.bookaholic.backend.service.auth.AuthService;
import com.bookaholic.backend.service.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Auth service implementation for user registration and authentication

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImp implements AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${admin.registration.secret}")
    private String adminSecret;

    @Value("${password.reset.token.expiry}")
    private long tokenExpiryMinutes;

    @Value("${otp.expiry.minutes}")
    private long otpExpiryMinutes;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        // 1. Check if user wants to be Admin
        if (request.getRole() == Role.ADMIN) {
            if (request.getAdminCode() == null || !request.getAdminCode().equals(adminSecret)) {
                throw new RuntimeException("Invalid Admin Registration Code");
            }
        }

        // 2. Generate 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        // 3. Create user with isVerified=false
        var user = User.builder()
                .fullName(request.getFirstname() + " " + request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .authProvider(AuthProvider.LOCAL)
                .otpCode(otp)
                .otpExpiry(otpExpiry)
                .isVerified(false) // Email not verified yet
                .build();

        try {
            repository.save(user);
        } catch (Exception e) {
            log.error("User Registration Failed: " + e.getMessage());
            throw new UnableToRegisterException("User Registration Failed: " + e.getMessage());
        }

        // 4. Send OTP email
        try {
            emailService.sendOtpEmail(request.getEmail(), otp, request.getFirstname());
            log.info("OTP sent to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
            // User is created but email failed - they can request resend
        }

        return "Registration successful! Please check your email for OTP.";
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is verified
        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified. Please check your email for OTP.");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().toString());

        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthenticationResponse authenticateGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String firstName = (String) payload.get("given_name");
                String lastName = (String) payload.get("family_name");

                // Check if user exists, if not create them
                var user = repository.findByEmail(email).orElseGet(() -> {
                    // ENFORCE RULE: Google Auth users are always USER role
                    User newUser = User.builder()
                            .fullName(firstName + " " + lastName)
                            .email(email)
                            .role(Role.USER)
                            .authProvider(AuthProvider.GOOGLE)
                            .isVerified(true) // Google emails are pre-verified
                            .build();
                    return repository.save(newUser);
                });

                Map<String, Object> claims = new HashMap<>();
                claims.put("role", user.getRole().toString());

                var jwtToken = jwtService.generateToken(claims, user);
                return AuthenticationResponse.builder().token(jwtToken).build();
            } else {
                throw new RuntimeException("Invalid Google Token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Google Authentication Failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void processForgotPassword(String email) {
        // Find user by email
        var user = repository.findByEmail(email).orElse(null);

        // Don't reveal whether email exists (security best practice)
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return; // Silently return to prevent user enumeration
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();

        // Set token expiry
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        // Save token to user
        user.setResetPasswordToken(resetToken);
        user.setResetTokenExpiry(expiry);
        repository.save(user);

        // Send email
        try {
            emailService.sendPasswordResetEmail(email, resetToken);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find user by reset token
        var user = repository.findAll().stream()
                .filter(u -> token.equals(u.getResetPasswordToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is expired
        if (user.getResetTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear reset token fields
        user.setResetPasswordToken(null);
        user.setResetTokenExpiry(null);

        repository.save(user);
        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {
        // Find user by email
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already verified
        if (user.isVerified()) {
            log.info("User already verified: {}", email);
            return; // Already verified, no error
        }

        // Validate OTP
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid OTP code");
        }

        // Check if OTP is expired
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // Mark as verified and clear OTP
        user.setVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);

        repository.save(user);
        log.info("Email verified successfully for: {}", email);
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        // Find user by email
        var user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already verified
        if (user.isVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        // Generate new OTP
        String newOtp = String.format("%06d", new java.util.Random().nextInt(999999));
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        // Update user with new OTP
        user.setOtpCode(newOtp);
        user.setOtpExpiry(newExpiry);
        repository.save(user);

        // Send email
        try {
            emailService.sendOtpEmail(email, newOtp, user.getFullName());
            log.info("OTP resent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to resend OTP email: {}", e.getMessage());
            throw new RuntimeException("Failed to resend OTP email");
        }
    }
}
