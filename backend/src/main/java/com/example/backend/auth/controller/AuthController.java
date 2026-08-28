package com.example.backend.auth.controller;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.GoogleAuthRequest;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.SignupRequest;
import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling user authentication, registration, Google OAuth2,
 * and current session profile queries.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Unified Login endpoint for all users (Recruiters and Candidates).
     *
     * @param request DTO containing email and password.
     * @return ApiResponse containing AuthResponse with signed JWT and user profile.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST: User login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request, null);
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", response));
    }

    /**
     * Unified Signup endpoint allowing role selection (RECRUITER or CANDIDATE).
     *
     * @param role    Optional query param or defaulted in payload.
     * @param request DTO containing name, email, password.
     * @return ApiResponse with HTTP 201 Created and AuthResponse.
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @RequestParam(value = "role", required = false, defaultValue = "CANDIDATE") Role role,
            @Valid @RequestBody SignupRequest request) {
        log.info("REST: User signup request for email: {} with role: {}", request.getEmail(), role);
        AuthResponse response = authService.signup(request, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account registered successfully", response));
    }

    /**
     * Google OAuth2 Login / Signup endpoint verifying Google ID token.
     *
     * @param request DTO containing Google ID token credential and optional role.
     * @return ApiResponse containing AuthResponse with application JWT.
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        log.info("REST: Google OAuth2 authentication request");
        AuthResponse response = authService.googleAuth(request);
        return ResponseEntity.ok(ApiResponse.success("Google authentication successful", response));
    }

    /**
     * Retrieves the profile of the currently authenticated user from JWT token.
     *
     * @param principal Injected authenticated user principal.
     * @return ApiResponse containing current user profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        AuthResponse response = authService.getAuthenticatedUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // --- Legacy / Specific Compatibility Endpoints ---

    @PostMapping("/recruiter/login")
    public ResponseEntity<ApiResponse<AuthResponse>> recruiterLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request, Role.RECRUITER);
        return ResponseEntity.ok(ApiResponse.success("Recruiter logged in successfully", response));
    }

    @PostMapping("/recruiter/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> recruiterSignup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request, Role.RECRUITER);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recruiter registered successfully", response));
    }

    @PostMapping("/candidate/login")
    public ResponseEntity<ApiResponse<AuthResponse>> candidateLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request, Role.CANDIDATE);
        return ResponseEntity.ok(ApiResponse.success("Candidate logged in successfully", response));
    }

    @PostMapping("/candidate/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> candidateSignup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request, Role.CANDIDATE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate registered successfully", response));
    }
}
