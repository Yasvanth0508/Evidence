package com.example.backend.candidate.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.EvaluationReport;
import com.example.backend.assessment.entity.Submission;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.EvaluationReportRepository;
import com.example.backend.assessment.repository.SubmissionRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.candidate.dto.CandidateAssessmentItemResponse;
import com.example.backend.candidate.dto.CandidateEvaluationDto;
import com.example.backend.candidate.dto.CandidateSearchResponse;
import com.example.backend.candidate.dto.CandidateSubmissionDto;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateService {

    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationReportRepository evaluationReportRepository;
    private final WorkspaceService workspaceService;

    public List<CandidateSearchResponse> getAllCandidates(String query) {
        List<User> candidates;
        if (query != null && !query.trim().isEmpty()) {
            candidates = userRepository.searchCandidatesByRoleAndQuery(Role.CANDIDATE, query.trim());
        } else {
            candidates = userRepository.findAllByRole(Role.CANDIDATE);
        }

        return candidates.stream()
                .map(c -> CandidateSearchResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .email(c.getEmail())
                        .role(c.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    public CandidateSearchResponse searchCandidateByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResourceNotFoundException("Email query parameter is required", "CANDIDATE_NOT_FOUND");
        }

        User candidate = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with email: " + email, "CANDIDATE_NOT_FOUND"));

        if (candidate.getRole() != Role.CANDIDATE) {
            throw new ResourceNotFoundException("User is not registered as a candidate", "CANDIDATE_NOT_FOUND");
        }

        return CandidateSearchResponse.builder()
                .id(candidate.getId())
                .name(candidate.getName())
                .email(candidate.getEmail())
                .role(candidate.getRole())
                .build();
    }

    public List<CandidateAssessmentItemResponse> getCandidateAssessmentsForRecruiter(UUID recruiterId, UUID candidateId) {
        User recruiter = workspaceService.getOrCreateRecruiter(recruiterId);

        userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId, "CANDIDATE_NOT_FOUND"));

        List<Assessment> assessments = assessmentRepository.findAllByCandidateIdAndRecruiterId(candidateId, recruiter.getId());

        return assessments.stream().map(a -> {
            Optional<Submission> submissionOpt = submissionRepository.findByAssessmentId(a.getId());
            CandidateSubmissionDto submissionDto = submissionOpt.map(s -> CandidateSubmissionDto.builder()
                    .submittedAt(s.getSubmittedAt())
                    .timeTakenSeconds(s.getTimeTakenSeconds())
                    .status(s.getStatus())
                    .build()).orElse(null);

            CandidateEvaluationDto evalDto = null;
            if (submissionOpt.isPresent()) {
                Optional<EvaluationReport> evalOpt = evaluationReportRepository.findBySubmissionId(submissionOpt.get().getAssessmentId());
                evalDto = evalOpt.map(e -> CandidateEvaluationDto.builder()
                        .score(e.getScore())
                        .totalTests(e.getTotalTests())
                        .passedTests(e.getPassedTests())
                        .failedTests(e.getFailedTests())
                        .buildStatus(e.getBuildStatus())
                        .applicationStatus(e.getApplicationStatus())
                        .timeTakenSeconds(e.getTimeTakenSeconds())
                        .status(e.getStatus())
                        .evaluatedAt(e.getEvaluatedAt())
                        .build()).orElse(null);
            }

            return CandidateAssessmentItemResponse.builder()
                    .id(a.getId())
                    .workspaceId(a.getWorkspace().getId())
                    .workspaceName(a.getWorkspace().getName())
                    .repositoryUrl(a.getRepositoryUrl())
                    .branchName(a.getBranchName())
                    .backendRootDirectory(a.getBackendRootDirectory())
                    .difficulty(a.getDifficulty())
                    .durationMinutes(a.getDurationMinutes())
                    .scheduledStartAt(a.getScheduledStartAt())
                    .scheduledEndAt(a.getScheduledEndAt())
                    .status(a.getStatus())
                    .submission(submissionDto)
                    .evaluation(evalDto)
                    .build();
        }).collect(Collectors.toList());
    }
}
