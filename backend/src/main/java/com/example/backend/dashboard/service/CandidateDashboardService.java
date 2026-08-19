package com.example.backend.dashboard.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Role;
import com.example.backend.dashboard.dto.CandidateDashboardResponse;
import com.example.backend.dashboard.dto.CompletedAssessmentDto;
import com.example.backend.dashboard.dto.ScheduledAssessmentDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CandidateDashboardService {

    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;

    public CandidateDashboardService(AssessmentRepository assessmentRepository,
                                     UserRepository userRepository) {
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
    }

    public CandidateDashboardResponse getDashboard() {
        // Resolve default candidate (rahul@example.com) for pre-auth development
        UUID candidateId = userRepository.findByEmail("rahul@example.com")
                .map(User::getId)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.CANDIDATE)
                        .findFirst()
                        .map(User::getId)
                        .orElse(UUID.randomUUID()));

        List<Assessment> assessments = assessmentRepository.findAllByCandidateIdOrderByCreatedAtDesc(candidateId);

        List<ScheduledAssessmentDto> scheduled = new ArrayList<>();
        List<CompletedAssessmentDto> completed = new ArrayList<>();

        for (Assessment a : assessments) {
            if (a.getStatus() == AssessmentStatus.COMPLETED || a.getStatus() == AssessmentStatus.EVALUATING) {
                completed.add(new CompletedAssessmentDto(
                        a.getId(),
                        a.getWorkspace().getName(),
                        a.getUpdatedAt(),
                        a.getScore(),
                        a.getStatus()
                ));
            } else if (a.getStatus() != AssessmentStatus.CANCELLED) {
                scheduled.add(new ScheduledAssessmentDto(
                        a.getId(),
                        a.getWorkspace().getName(),
                        a.getScheduledStartAt(),
                        a.getScheduledEndAt(),
                        a.getDifficulty(),
                        a.getStatus()
                ));
            }
        }

        return new CandidateDashboardResponse(scheduled, completed);
    }
}
