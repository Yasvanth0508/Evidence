package com.example.backend.workspace.service;

import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AuthProvider;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.workspace.dto.AddCandidateRequest;
import com.example.backend.workspace.dto.CandidateDto;
import com.example.backend.workspace.dto.WorkspaceCandidateItemResponse;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.entity.WorkspaceCandidateId;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceCandidateService {

    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<WorkspaceCandidateItemResponse> getCandidatesInWorkspace(UUID recruiterId, UUID workspaceId) {
        Workspace workspace = workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        return workspaceCandidateRepository.findAllByWorkspaceId(workspace.getId()).stream()
                .map(wc -> WorkspaceCandidateItemResponse.builder()
                        .workspaceId(workspace.getId())
                        .candidate(CandidateDto.builder()
                                .id(wc.getCandidate().getId())
                                .name(wc.getCandidate().getName())
                                .email(wc.getCandidate().getEmail())
                                .role(wc.getCandidate().getRole())
                                .build())
                        .createdAt(wc.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public WorkspaceCandidateItemResponse addCandidateToWorkspace(UUID recruiterId, UUID workspaceId, AddCandidateRequest request) {
        Workspace workspace = workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        String email = request.getEmail().trim().toLowerCase();
        log.info("Enrolling candidate '{}' into workspace ID: {}", email, workspaceId);

        User candidate = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String candidateName = request.getName() != null && !request.getName().trim().isEmpty()
                            ? request.getName().trim()
                            : email.split("@")[0];
                    String secureInitPassword = passwordEncoder.encode(UUID.randomUUID().toString());
                    User newCandidate = User.builder()
                            .name(candidateName)
                            .email(email)
                            .passwordHash(secureInitPassword)
                            .role(Role.CANDIDATE)
                            .authProvider(AuthProvider.LOCAL)
                            .build();
                    return userRepository.save(newCandidate);
                });

        if (workspaceCandidateRepository.existsByWorkspaceIdAndCandidateId(workspace.getId(), candidate.getId())) {
            return WorkspaceCandidateItemResponse.builder()
                    .workspaceId(workspace.getId())
                    .candidate(CandidateDto.builder()
                            .id(candidate.getId())
                            .name(candidate.getName())
                            .email(candidate.getEmail())
                            .role(candidate.getRole())
                            .build())
                    .createdAt(workspace.getCreatedAt())
                    .build();
        }

        WorkspaceCandidate workspaceCandidate = WorkspaceCandidate.builder()
                .workspace(workspace)
                .candidate(candidate)
                .id(new WorkspaceCandidateId(workspace.getId(), candidate.getId()))
                .build();
        WorkspaceCandidate saved = workspaceCandidateRepository.save(workspaceCandidate);

        return WorkspaceCandidateItemResponse.builder()
                .workspaceId(workspace.getId())
                .candidate(CandidateDto.builder()
                        .id(candidate.getId())
                        .name(candidate.getName())
                        .email(candidate.getEmail())
                        .role(candidate.getRole())
                        .build())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public void removeCandidateFromWorkspace(UUID recruiterId, UUID workspaceId, UUID candidateId) {
        log.info("Removing candidate ID: {} from workspace ID: {}", candidateId, workspaceId);
        Workspace workspace = workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        WorkspaceCandidate workspaceCandidate = workspaceCandidateRepository
                .findByWorkspaceIdAndCandidateId(workspace.getId(), candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate is not enrolled in this workspace.",
                        "CANDIDATE_NOT_FOUND"
                ));

        workspaceCandidateRepository.delete(workspaceCandidate);
    }
}
