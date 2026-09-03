package com.example.backend.dashboard.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.EvaluationReport;
import com.example.backend.assessment.entity.Submission;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.EvaluationReportRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.dashboard.dto.CandidateDashboardResponse;
import com.example.backend.dashboard.dto.CompletedAssessmentDto;
import com.example.backend.dashboard.dto.ScheduledAssessmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateDashboardService {

    private final AssessmentRepository assessmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;
    private final UserRepository userRepository;

    public CandidateDashboardResponse getCandidateDashboard(UUID candidateId) {
        log.info("Calculating candidate dashboard for candidateId: {}", candidateId);
        User candidate = getCandidate(candidateId);

        List<Assessment> assessments = assessmentRepository.findAllByCandidateIdOrderByCreatedAtDesc(candidate.getId());

        List<ScheduledAssessmentDto> scheduledList = new ArrayList<>();
        List<CompletedAssessmentDto> completedList = new ArrayList<>();

        for (Assessment a : assessments) {
            if (a.getStatus() == AssessmentStatus.COMPLETED) {
                Optional<Submission> subOpt = submissionRepository.findByAssessmentId(a.getId());
                Long timeTaken = subOpt.map(Submission::getTimeTakenSeconds).orElse(0L);
                var submittedAt = subOpt.map(Submission::getSubmittedAt).orElse(a.getScheduledEndAt());

                BigDecimal score = BigDecimal.ZERO;
                Integer totalTests = 0;
                Integer passedTests = 0;
                Integer failedTests = 0;
                if (subOpt.isPresent()) {
                    Optional<EvaluationReport> evalOpt = evaluationReportRepository.findBySubmissionId(subOpt.get().getId());
                    if (evalOpt.isPresent()) {
                        EvaluationReport r = evalOpt.get();
                        score = r.getScore() != null ? r.getScore() : BigDecimal.ZERO;
                        totalTests = r.getTotalTests() != null ? r.getTotalTests() : 0;
                        passedTests = r.getPassedTests() != null ? r.getPassedTests() : 0;
                        failedTests = r.getFailedTests() != null ? r.getFailedTests() : 0;
                    }
                }

                completedList.add(CompletedAssessmentDto.builder()
                        .assessmentId(a.getId())
                        .title(a.getTitle() != null && !a.getTitle().isBlank() ? a.getTitle() : a.getWorkspace().getName() + " Assessment")
                        .workspaceId(a.getWorkspace().getId())
                        .workspaceName(a.getWorkspace().getName())
                        .difficulty(a.getDifficulty())
                        .durationMinutes(a.getDurationMinutes())
                        .submittedAt(submittedAt)
                        .timeTakenSeconds(timeTaken)
                        .score(score)
                        .totalTests(totalTests)
                        .passedTests(passedTests)
                        .failedTests(failedTests)
                        .status(a.getStatus())
                        .build());
            } else {
                scheduledList.add(ScheduledAssessmentDto.builder()
                        .assessmentId(a.getId())
                        .title(a.getTitle() != null && !a.getTitle().isBlank() ? a.getTitle() : a.getWorkspace().getName() + " Assessment")
                        .workspaceId(a.getWorkspace().getId())
                        .workspaceName(a.getWorkspace().getName())
                        .scheduledStartAt(a.getScheduledStartAt())
                        .scheduledEndAt(a.getScheduledEndAt())
                        .difficulty(a.getDifficulty())
                        .durationMinutes(a.getDurationMinutes())
                        .status(a.getStatus())
                        .build());
            }
        }

        return CandidateDashboardResponse.builder()
                .scheduledAssessments(scheduledList)
                .completedAssessments(completedList)
                .build();
    }

    private User getCandidate(UUID candidateId) {
        if (candidateId == null) {
            throw new com.example.backend.common.exception.UnauthorizedException("Authentication required.");
        }
        return userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId, "CANDIDATE_NOT_FOUND"));
    }
}
