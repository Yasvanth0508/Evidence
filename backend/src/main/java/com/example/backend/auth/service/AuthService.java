package com.example.backend.auth.service;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.SignupRequest;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AuthProvider;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.DuplicateResourceException;
import com.example.backend.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, Role expectedRole) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Attempting login for email: {} with expected role: {}", email, expectedRole);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS"));

        if (expectedRole != null && user.getRole() != expectedRole) {
            log.warn("Role mismatch for user {}: expected {}, actual {}", email, expectedRole, user.getRole());
            throw new UnauthorizedException("User is not registered as a " + expectedRole, "INVALID_ROLE");
        }

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash())
                || request.getPassword().equals(user.getPasswordHash());

        if (!matches) {
            log.warn("Password mismatch for email: {}", email);
            throw new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS");
        }

        log.info("User {} logged in successfully as {}", email, user.getRole());
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token("jwt-session-token-" + user.getId())
                .build();
    }

    @Transactional
    public AuthResponse signup(SignupRequest request, Role role) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Registering new user with email: {} and role: {}", email, role);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A user with email '" + email + "' already exists", "EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role != null ? role : Role.CANDIDATE)
                .authProvider(AuthProvider.LOCAL)
                .build();

        User saved = userRepository.save(user);
        log.info("User {} registered successfully with ID {}", email, saved.getId());

        return AuthResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .token("jwt-session-token-" + saved.getId())
                .build();
    }
}