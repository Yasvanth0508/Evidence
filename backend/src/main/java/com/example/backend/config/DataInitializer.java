package com.example.backend.config;

import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AuthProvider;
import com.example.backend.common.enums.Role;
import com.example.backend.common.enums.WorkspaceStatus;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initTestData(
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceCandidateRepository workspaceCandidateRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            User recruiter = userRepository.findByEmail("recruiter@example.com")
                    .orElseGet(() -> {
                        User r = new User(
                                "Demo Recruiter",
                                "recruiter@example.com",
                                passwordEncoder.encode("password123"),
                                Role.RECRUITER,
                                AuthProvider.LOCAL
                        );
                        return userRepository.save(r);
                    });

            User candidate1 = userRepository.findByEmail("rahul@example.com")
                    .orElseGet(() -> {
                        User c = new User(
                                "Rahul Kumar",
                                "rahul@example.com",
                                passwordEncoder.encode("password123"),
                                Role.CANDIDATE,
                                AuthProvider.LOCAL
                        );
                        return userRepository.save(c);
                    });

            User candidate2 = userRepository.findByEmail("priya@example.com")
                    .orElseGet(() -> {
                        User c = new User(
                                "Priya Sharma",
                                "priya@example.com",
                                passwordEncoder.encode("password123"),
                                Role.CANDIDATE,
                                AuthProvider.LOCAL
                        );
                        return userRepository.save(c);
                    });

            User candidate3 = userRepository.findByEmail("arun@gmail.com")
                    .orElseGet(() -> {
                        User c = new User(
                                "Arun Kumar",
                                "arun@gmail.com",
                                passwordEncoder.encode("password123"),
                                Role.CANDIDATE,
                                AuthProvider.LOCAL
                        );
                        return userRepository.save(c);
                    });

            User candidate4 = userRepository.findByEmail("sneha@gmail.com")
                    .orElseGet(() -> {
                        User c = new User(
                                "Sneha Rao",
                                "sneha@gmail.com",
                                passwordEncoder.encode("password123"),
                                Role.CANDIDATE,
                                AuthProvider.LOCAL
                        );
                        return userRepository.save(c);
                    });

            if (workspaceRepository.count() == 0) {
                Workspace ws1 = Workspace.builder()
                        .recruiter(recruiter)
                        .name("IIT Bombay - Campus Placement 2026")
                        .description("Computer Science & Engineering Campus Placement Drive for Java Spring Boot Developers")
                        .status(WorkspaceStatus.ACTIVE)
                        .build();
                ws1 = workspaceRepository.save(ws1);

                Workspace ws2 = Workspace.builder()
                        .recruiter(recruiter)
                        .name("NIT Trichy - Backend Engineering")
                        .description("Backend Systems & Microservices Architecture Placement Assessment")
                        .status(WorkspaceStatus.ACTIVE)
                        .build();
                ws2 = workspaceRepository.save(ws2);

                Workspace ws3 = Workspace.builder()
                        .recruiter(recruiter)
                        .name("VIT Vellore - SDE-1 Hiring Drive")
                        .description("Software Development Engineer (SDE-1) Java REST API technical evaluation")
                        .status(WorkspaceStatus.ACTIVE)
                        .build();
                ws3 = workspaceRepository.save(ws3);

                // Enroll candidates
                workspaceCandidateRepository.save(new WorkspaceCandidate(ws1, candidate1));
                workspaceCandidateRepository.save(new WorkspaceCandidate(ws1, candidate2));
                workspaceCandidateRepository.save(new WorkspaceCandidate(ws1, candidate3));
                workspaceCandidateRepository.save(new WorkspaceCandidate(ws2, candidate4));
                workspaceCandidateRepository.save(new WorkspaceCandidate(ws2, candidate1));
            }
        };
    }
}
