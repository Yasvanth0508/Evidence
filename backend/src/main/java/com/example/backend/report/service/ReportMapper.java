package com.example.backend.report.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.Submission;
import com.example.backend.assessment.repository.EvaluationReportRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.report.dto.ReportItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportMapper {

    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;

    public ReportItemDto toDto(Assessment a) {
        Optional<Submission> submissionOpt = submissionRepository.findByAssessmentId(a.getId());
        Instant submittedAt = submissionOpt.map(Submission::getSubmittedAt).orElse(a.getUpdatedAt());
        BigDecimal score = submissionOpt.flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()))
                .map(r -> r.getScore())
                .orElse(BigDecimal.ZERO);

        return ReportItemDto.builder()
                .assessmentId(a.getId())
                .candidateId(a.getCandidate() != null ? a.getCandidate().getId() : null)
                .candidateName(a.getCandidate() != null ? a.getCandidate().getName() : "Unknown")
                .candidateEmail(a.getCandidate() != null ? a.getCandidate().getEmail() : "Unknown")
                .workspaceId(a.getWorkspace() != null ? a.getWorkspace().getId() : null)
                .workspaceName(a.getWorkspace() != null ? a.getWorkspace().getName() : "General")
                .difficulty(a.getDifficulty())
                .score(score)
                .status(a.getStatus())
                .submittedAt(submittedAt)
                .build();
    }
}
