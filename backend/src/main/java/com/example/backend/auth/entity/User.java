package com.example.backend.auth.entity;

import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.AuthProvider;
import com.example.backend.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 50)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    public User(String name, String email, String passwordHash, Role role) {
        this(name, email, passwordHash, role, AuthProvider.LOCAL);
    }

    public User(String name, String email, String passwordHash, Role role, AuthProvider authProvider) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.authProvider = authProvider != null ? authProvider : AuthProvider.LOCAL;
    }

    // Convenience method for security / legacy callers
    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }
}
