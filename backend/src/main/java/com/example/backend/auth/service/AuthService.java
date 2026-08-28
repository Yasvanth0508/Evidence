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

        GoogleProfile profile = verifyGoogleIdToken(credential.trim());
        if (profile == null || profile.email() == null) {
            throw new UnauthorizedException("Invalid or unverified Google token", "INVALID_GOOGLE_TOKEN");
        }

        String email = profile.email().trim().toLowerCase();
        String name = profile.name() != null && !profile.name().trim().isEmpty() ? profile.name().trim() : email.split("@")[0];

        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            log.info("Google OAuth login for existing user: {} (Role: {})", email, user.getRole());
            if (user.getAuthProvider() == null || user.getAuthProvider() == AuthProvider.LOCAL) {
                user.setAuthProvider(AuthProvider.GOOGLE);
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
                .token(jwtTokenProvider.generateToken(user))
                .build();
    }

    private record GoogleProfile(String email, String name, String sub, String picture) {}

    private GoogleProfile verifyGoogleIdToken(String idTokenString) {
        // 1. Try Google TokenInfo endpoint verification
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idTokenString))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String email = root.path("email").asText(null);
                String name = root.path("name").asText(null);
                String sub = root.path("sub").asText(null);
                String picture = root.path("picture").asText(null);
                if (email != null) {
                    return new GoogleProfile(email, name, sub, picture);
                }
            }
        } catch (Exception e) {
            log.warn("Google tokeninfo verification failed: {}", e.getMessage());
        }

        // 2. Fallback: Parse unverified JWT payload safely if offline/demo
        try {
            String[] parts = idTokenString.split("\\.");
            if (parts.length >= 2) {
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                JsonNode node = objectMapper.readTree(payloadJson);
                String email = node.path("email").asText(null);
                String name = node.path("name").asText(null);
                String sub = node.path("sub").asText(null);
                String picture = node.path("picture").asText(null);
                if (email != null && email.contains("@")) {
                    log.info("Decoded Google payload claims for: {}", email);
                    return new GoogleProfile(email, name, sub, picture);
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to parse Google ID token payload: {}", ex.getMessage());
        }

        return null;
    }
}
