package com.example.backend.auth.domain;

import com.example.backend.common.enums.Role;
import java.util.UUID;

public record CurrentUser(
    UUID id,
    String email,
    Role role,
    String name
) {}
