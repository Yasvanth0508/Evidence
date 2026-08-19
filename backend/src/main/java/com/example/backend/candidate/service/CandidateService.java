package com.example.backend.candidate.service;

import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.candidate.dto.CandidateAssessmentDto;
import com.example.backend.candidate.dto.CandidateResponse;
import com.example.backend.candidate.dto.CandidateWorkspaceDto;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CandidateService {

    private final UserRepository userRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final AssessmentRepository assessmentRepository;

    public CandidateService(UserRepository userRepository,
                            WorkspaceCandidateRepository workspaceCandidateRepository,
                            AssessmentRepository assessmentRepository) {
        this.userRepository = userRepository;
        this.workspaceCandidateRepository = workspaceCandidateRepository;
        this.assessmentRepository = assessmentRepository;
    }

    public CandidateResponse searchByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResourceNotFoundException("Candidate email must not be empty", "CANDIDATE_NOT_FOUND");
        }

        String normalizedEmail = email.trim().toLowerCase();
        User candidate = userRepository.findByEmailAndRole(normalizedEmail, Role.CANDIDATE)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found", "CANDIDATE_NOT_FOUND"));

        return new CandidateResponse(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                candidate.getRole()
        );
    }

    public CandidateResponse getCandidateById(UUID candidateId) {
        User candidate = userRepository.findById(candidateId)
                .filter(u -> u.getRole() == Role.CANDIDATE)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found", "CANDIDATE_NOT_FOUND"));

        return new CandidateResponse(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                candidate.getRole()
        );
    }

    public List<CandidateWorkspaceDto> getCandidateWorkspaces(UUID candidateId) {
        // Verify candidate exists
        getCandidateById(candidateId);

        return workspaceCandidateRepository.findAllByCandidateId(candidateId).stream()
                .map(wc -> new CandidateWorkspaceDto(
                        wc.getWorkspace().getId(),
                        wc.getWorkspace().getName()
                ))
                .collect(Collectors.toList());
    }

    public List<CandidateAssessmentDto> getCandidateAssessments(UUID candidateId) {
        // Verify candidate exists
        getCandidateById(candidateId);

        return assessmentRepository.findAllByCandidateId(candidateId).stream()
                .map(a -> new CandidateAssessmentDto(
                        a.getId(),
                        a.getDifficulty(),
                        a.getScheduledStartAt(),
                        a.getScheduledEndAt(),
                        a.getStatus(),
                        a.getScore()
                ))
                .collect(Collectors.toList());
    }
}
