package com.example.backend.auth.repository;

import com.example.backend.auth.entity.User;
import com.example.backend.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndRole(String email, Role role);
    boolean existsByEmail(String email);
}
