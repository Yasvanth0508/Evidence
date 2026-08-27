package com.example.backend.auth.controller;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.SignupRequest;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class AuthController {

    private final AuthService authService;

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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> genericLogin(
            @RequestParam(value = "role", required = false) Role role,
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request, role);
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> genericSignup(
            @RequestParam(value = "role", required = false, defaultValue = "CANDIDATE") Role role,
            @Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", response));
    }
}