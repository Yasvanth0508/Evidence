package com.example.backend.auth.dto;

import com.example.backend.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String token;
    private com.example.backend.common.enums.AuthProvider authProvider;
    private String avatarUrl;
}