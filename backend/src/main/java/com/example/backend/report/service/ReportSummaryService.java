package com.example.backend.report.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.EvaluationReportRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.report.dto.ReportSummaryResponse;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportSummaryService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;

    @Transactional(readOnly = true)
    public ReportSummaryResponse getReportSummary(UUID recruiterId, UUID workspaceId) {
        log.info("Calculating report summary for recruiter={}, workspaceId={}", recruiterId, workspaceId);

        List<Assessment> assessments;
        long totalCandidates;

        if (workspaceId != null) {
            assessments = assessmentRepository.findAllByWorkspaceId(workspaceId);
            totalCandidates = workspaceCandidateRepository.findAllByWorkspaceId(workspaceId).size();
        } else if (recruiterId != null) {
            var workspaces = workspaceRepository.findAllByRecruiterId(recruiterId);
            List<UUID> wsIds = workspaces.stream().map(Workspace::getId).toList();
            assessments = new ArrayList<>();
            for (UUID wsId : wsIds) {
                assessments.addAll(assessmentRepository.findAllByWorkspaceId(wsId));
            }
            totalCandidates = workspaceCandidateRepository.countDistinctCandidatesByRecruiterId(recruiterId);
        } else {
            assessments = assessmentRepository.findAll();
            totalCandidates = workspaceCandidateRepository.count();
        }

        long completedCount = assessments.stream().filter(a -> a.getStatus() == AssessmentStatus.COMPLETED).count();
        long scheduledCount = assessments.stream().filter(a -> a.getStatus() == AssessmentStatus.SCHEDULED || a.getStatus() == AssessmentStatus.READY).count();

        int participationRate = totalCandidates > 0
                ? (int) Math.round(((double) (completedCount + scheduledCount) / totalCandidates) * 100)
                : 0;

        List<BigDecimal> scores = new ArrayList<>();
        long passedCount = 0;

        for (Assessment a : assessments) {
            if (a.getStatus() == AssessmentStatus.COMPLETED) {
                var scoreOpt = submissionRepository.findByAssessmentId(a.getId())
                        .flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()))
                        .map(r -> r.getScore());
                if (scoreOpt.isPresent()) {
                    BigDecimal score = scoreOpt.get();
                    scores.add(score);
                    if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
                        passedCount++;
                    }
                }
            }
        }

        int passRate = completedCount > 0
                ? (int) Math.round(((double) passedCount / completedCount) * 100)
                : 0;

        BigDecimal avgScore = BigDecimal.ZERO;
        if (!scores.isEmpty()) {
            BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            avgScore = sum.divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);
        }

        return ReportSummaryResponse.builder()
                .totalCandidates(totalCandidates)
                .completedAssessments(completedCount)
                .scheduledAssessments(scheduledCount)
                .participationRate(participationRate)
                .passedAssessments(passedCount)
                .passRate(passRate)
                .averageScore(avgScore)
                .build();
    }
}
