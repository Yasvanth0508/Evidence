package com.example.backend.workspace.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.EvaluationReport;
import com.example.backend.assessment.entity.Submission;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.EvaluationReportRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.workspace.dto.SelectedCandidateRequest;
import com.example.backend.workspace.dto.SelectedCandidateResponse;
import com.example.backend.workspace.entity.SelectedCandidate;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.SelectedCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelectedCandidateService {

    private final SelectedCandidateRepository selectedCandidateRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;

    @Transactional(readOnly = true)
    public List<SelectedCandidateResponse> getSelectedCandidates(UUID recruiterId, UUID workspaceId) {
        log.info("Fetching selected candidates for recruiter={}, workspace={}", recruiterId, workspaceId);

        List<SelectedCandidate> candidates;
        if (workspaceId != null) {
            candidates = selectedCandidateRepository.findAllByRecruiterIdAndWorkspaceIdOrderBySelectedAtDesc(recruiterId, workspaceId);
        } else {
            candidates = selectedCandidateRepository.findAllByRecruiterIdOrderBySelectedAtDesc(recruiterId);
        }

        List<SelectedCandidateResponse> results = new ArrayList<>();
        for (SelectedCandidate sc : candidates) {
            BigDecimal score = BigDecimal.ZERO;
            String scoreRating = null;
            Integer passedTests = null;
            Integer totalTests = null;
            Long timeTakenMinutes = null;
            UUID assessmentId = sc.getAssessment() != null ? sc.getAssessment().getId() : null;

            if (assessmentId == null) {
                // Try finding an assessment for candidate in workspace
                List<Assessment> asmts = assessmentRepository.findAllByWorkspaceId(sc.getWorkspace().getId());
                for (Assessment a : asmts) {
                    if (a.getCandidate() != null && a.getCandidate().getId().equals(sc.getCandidate().getId())) {
                        assessmentId = a.getId();
                        break;
                    }
                }
            }

            if (assessmentId != null) {
                Optional<Submission> subOpt = submissionRepository.findByAssessmentId(assessmentId);
                Optional<EvaluationReport> evalOpt = subOpt.flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()));
                if (evalOpt.isPresent()) {
                    EvaluationReport report = evalOpt.get();
                    score = report.getScore() != null ? report.getScore() : BigDecimal.ZERO;
                    scoreRating = report.getScoreRating();
                    passedTests = report.getPassedTests();
                    totalTests = report.getTotalTests();
                    if (report.getTimeTakenSeconds() != null && report.getTimeTakenSeconds() > 0) {
                        timeTakenMinutes = (report.getTimeTakenSeconds() + 59) / 60;
                    }
                }
            }

            results.add(SelectedCandidateResponse.builder()
                    .id(sc.getId())
                    .workspaceId(sc.getWorkspace().getId())
                    .workspaceName(sc.getWorkspace().getName())
                    .candidateId(sc.getCandidate().getId())
                    .candidateName(sc.getCandidate().getName())
                    .candidateEmail(sc.getCandidate().getEmail())
                    .candidateRole("Java Backend Engineer")
                    .assessmentId(assessmentId)
                    .score(score)
                    .scoreRating(scoreRating)
                    .passedTests(passedTests)
                    .totalTests(totalTests)
                    .timeTakenMinutes(timeTakenMinutes)
                    .selectionNotes(sc.getSelectionNotes())
                    .selectionStatus(sc.getSelectionStatus())
                    .selectedAt(sc.getSelectedAt())
                    .build());
        }

        return results;
    }

    @Transactional
    public SelectedCandidateResponse selectCandidate(UUID recruiterId, SelectedCandidateRequest request) {
        log.info("Recruiter {} selecting candidate {} in workspace {}", recruiterId, request.getCandidateId(), request.getWorkspaceId());

        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + request.getWorkspaceId()));

        if (workspace.getRecruiter() == null || !workspace.getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("Recruiter does not own this workspace");
        }

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found: " + recruiterId));

        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + request.getCandidateId()));

        Assessment assessment = null;
        if (request.getAssessmentId() != null) {
            assessment = assessmentRepository.findById(request.getAssessmentId()).orElse(null);
        } else {
            // Auto-resolve candidate's assessment in this workspace if exists
            List<Assessment> asmts = assessmentRepository.findAllByWorkspaceId(workspace.getId());
            for (Assessment a : asmts) {
                if (a.getCandidate() != null && a.getCandidate().getId().equals(candidate.getId())) {
                    assessment = a;
                    break;
                }
            }
        }

        String status = request.getSelectionStatus() != null && !request.getSelectionStatus().isBlank()
                ? request.getSelectionStatus()
                : "SELECTED";

        SelectedCandidate sc = selectedCandidateRepository
                .findByWorkspaceIdAndCandidateId(request.getWorkspaceId(), request.getCandidateId())
                .orElseGet(() -> SelectedCandidate.builder()
                        .recruiter(recruiter)
                        .workspace(workspace)
                        .candidate(candidate)
                        .build());

        sc.setAssessment(assessment);
        sc.setSelectionNotes(request.getSelectionNotes());
        sc.setSelectionStatus(status);
        sc.setSelectedAt(Instant.now());

        SelectedCandidate saved = selectedCandidateRepository.save(sc);

        BigDecimal score = BigDecimal.ZERO;
        String scoreRating = null;
        Integer passedTests = null;
        Integer totalTests = null;
        Long timeTakenMinutes = null;

        if (assessment != null) {
            Optional<Submission> subOpt = submissionRepository.findByAssessmentId(assessment.getId());
            Optional<EvaluationReport> evalOpt = subOpt.flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()));
            if (evalOpt.isPresent()) {
                EvaluationReport report = evalOpt.get();
                score = report.getScore() != null ? report.getScore() : BigDecimal.ZERO;
                scoreRating = report.getScoreRating();
                passedTests = report.getPassedTests();
                totalTests = report.getTotalTests();
                if (report.getTimeTakenSeconds() != null && report.getTimeTakenSeconds() > 0) {
                    timeTakenMinutes = (report.getTimeTakenSeconds() + 59) / 60;
                }
            }
        }

        return SelectedCandidateResponse.builder()
                .id(saved.getId())
                .workspaceId(workspace.getId())
                .workspaceName(workspace.getName())
                .candidateId(candidate.getId())
                .candidateName(candidate.getName())
                .candidateEmail(candidate.getEmail())
                .candidateRole("Java Backend Engineer")
                .assessmentId(assessment != null ? assessment.getId() : null)
                .score(score)
                .scoreRating(scoreRating)
                .passedTests(passedTests)
                .totalTests(totalTests)
                .timeTakenMinutes(timeTakenMinutes)
                .selectionNotes(saved.getSelectionNotes())
                .selectionStatus(saved.getSelectionStatus())
                .selectedAt(saved.getSelectedAt())
                .build();
    }

    @Transactional
    public void removeSelectedCandidate(UUID recruiterId, UUID id) {
        log.info("Recruiter {} removing selected candidate record {}", recruiterId, id);

        SelectedCandidate sc = selectedCandidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Selected candidate entry not found: " + id));

        if (sc.getRecruiter() == null || !sc.getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("Recruiter does not own this entry");
        }

        selectedCandidateRepository.delete(sc);
    }

    @Transactional
    public void removeSelectedCandidateByWorkspaceAndCandidate(UUID recruiterId, UUID workspaceId, UUID candidateId) {
        log.info("Recruiter {} removing selected candidate {} in workspace {}", recruiterId, candidateId, workspaceId);
        selectedCandidateRepository.deleteByRecruiterIdAndWorkspaceIdAndCandidateId(recruiterId, workspaceId, candidateId);
    }
}
