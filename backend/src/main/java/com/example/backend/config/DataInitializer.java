package com.example.backend.config;

import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initTestData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("recruiter@example.com")) {
                User recruiter = new User(
                        "Demo Recruiter",
                        "recruiter@example.com",
                        passwordEncoder.encode("password123"),
                        Role.RECRUITER
                );
                userRepository.save(recruiter);
            }

            if (!userRepository.existsByEmail("rahul@example.com")) {
                User candidate1 = new User(
                        "Rahul Kumar",
                        "rahul@example.com",
                        passwordEncoder.encode("password123"),
                        Role.CANDIDATE
                );
                userRepository.save(candidate1);
            }

            if (!userRepository.existsByEmail("priya@example.com")) {
                User candidate2 = new User(
                        "Priya Sharma",
                        "priya@example.com",
                        passwordEncoder.encode("password123"),
                        Role.CANDIDATE
                );
                userRepository.save(candidate2);
            }
        };
    }
}
