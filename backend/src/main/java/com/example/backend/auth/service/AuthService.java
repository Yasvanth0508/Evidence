package com.example.backend.auth.service;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.GoogleAuthRequest;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.SignupRequest;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.auth.security.JwtTokenProvider;
import com.example.backend.common.enums.AuthProvider;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.DuplicateResourceException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing user authentication, registration, password verification,
 * JWT token issuance, and Google OAuth2 integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Value("${google.client-id:}")
    private String googleClientId;

    /**
     * Authenticates a user by email and password, issuing a signed JWT token.
     *
     * @param request      DTO with email and password.
     * @param expectedRole Optional role filter (null for unified login across all roles).
     * @return AuthResponse with generated JWT and user profile.
     */
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

        String token = jwtTokenProvider.generateToken(user);
        log.info("User {} logged in successfully as {}", email, user.getRole());

        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
    }

    /**
     * Registers a new user account with role, password hash, and issues a signed JWT token.
     *
     * @param request DTO with name, email, and password.
     * @param role    The user role (RECRUITER or CANDIDATE).
     * @return AuthResponse with generated JWT and user profile.
     */
    @Transactional
    public AuthResponse signup(SignupRequest request, Role role) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Registering new user with email: {} and role: {}", email, role);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A user with email '" + email + "' already exists", "EMAIL_ALREADY_EXISTS");
        }

        Role assignedRole = role != null ? role : Role.CANDIDATE;

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .authProvider(AuthProvider.LOCAL)
                .build();

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved);
        log.info("User {} registered successfully with ID {}", email, saved.getId());

        return AuthResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .authProvider(saved.getAuthProvider())
                .avatarUrl(saved.getAvatarUrl())
                .token(token)
                .build();
    }

    /**
     * Authenticates or registers a user via Google OAuth2 ID token credential.
     *
     * @param request DTO containing Google ID token and optional role for new signups.
     * @return AuthResponse with generated application JWT.
     */
    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        String credential = request.getCredential();
        if (credential == null || credential.trim().isEmpty()) {
            throw new UnauthorizedException("Google credential is required", "MISSING_GOOGLE_CREDENTIAL");
        }

        GoogleTokenVerifier.GoogleProfile profile = verifyGoogleIdToken(credential.trim());
        if (profile == null || profile.email() == null) {
            throw new UnauthorizedException("Invalid or unverified Google token", "INVALID_GOOGLE_TOKEN");
        }

        String email = profile.email().trim().toLowerCase();
        String name = profile.name() != null && !profile.name().trim().isEmpty() ? profile.name().trim() : email.split("@")[0];
        String picture = profile.picture();

        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            log.info("Google OAuth login for existing user: {} (Role: {})", email, user.getRole());
            boolean modified = false;
            if (user.getAuthProvider() == null || user.getAuthProvider() == AuthProvider.LOCAL) {
                user.setAuthProvider(AuthProvider.GOOGLE);
                modified = true;
            }
            if (picture != null && !picture.isBlank() && (user.getAvatarUrl() == null || !user.getAvatarUrl().equals(picture))) {
                user.setAvatarUrl(picture);
                modified = true;
            }
            if (modified) {
                user = userRepository.save(user);
            }
        } else {
            if (request.getRole() == null) {
                log.info("Google OAuth signup attempted without a role for email {}", email);
                throw new UnauthorizedException("Please select whether you are a Candidate or Recruiter before continuing with Google.", "ROLE_REQUIRED");
            }
            Role assignedRole = request.getRole();
            log.info("Google OAuth signup for new user: {} with role {}", email, assignedRole);
            User newUser = User.builder()
                    .name(name)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(assignedRole)
                    .authProvider(AuthProvider.GOOGLE)
                    .avatarUrl(picture)
                    .build();
            user = userRepository.save(newUser);
        }

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
    }

    /**
     * Retrieves the profile of the currently authenticated user by UUID.
     *
     * @param userId UUID of the user.
     * @return AuthResponse containing profile details.
     */
    @Transactional(readOnly = true)
    public AuthResponse getAuthenticatedUser(UUID userId) {
        if (userId == null) {
            throw new UnauthorizedException("Authentication token required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .avatarUrl(user.getAvatarUrl())
                .token(jwtTokenProvider.generateToken(user))
                .build();
    }

    private GoogleTokenVerifier.GoogleProfile verifyGoogleIdToken(String idTokenString) {
        return googleTokenVerifier.verifyGoogleIdToken(idTokenString);
    }
}
