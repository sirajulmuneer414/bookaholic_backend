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

import java.util.Collections;

    // Auth service implementation for user registration and authentication

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImp implements AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;


    @Value("${admin.registration.secret}")
    private String adminSecret;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        // 1. Check if user wants to be Admin
        if (request.getRole() == Role.ADMIN) {
            if (request.getAdminCode() == null || !request.getAdminCode().equals(adminSecret)) {
                throw new RuntimeException("Invalid Admin Registration Code");
            }
        }

        var user = User.builder()
                .fullName(request.getLastname() + " " + request.getFirstname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        try {
            repository.save(user);
        }catch (Exception e) {
            log.error("User Registration Failed: " + e.getMessage());
            throw new UnableToRegisterException("User Registration Failed: " + e.getMessage());
        }

        return "User Registered Successfully";
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthenticationResponse authenticateGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
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
                            .fullName(firstName + lastName)
                            .email(email)
                            .role(Role.USER)
                            .authProvider(AuthProvider.GOOGLE)
                            .build();
                    return repository.save(newUser);
                });

                var jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder().token(jwtToken).build();
            } else {
                throw new RuntimeException("Invalid Google Token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Google Authentication Failed: " + e.getMessage());
        }
    }
}
