package com.example.backend.assessment.application;

import com.example.backend.assessment.dto.*;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.FeatureSpecification;
import com.example.backend.assessment.entity.RepositoryAnalysis;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.FeatureSpecificationRepository;
import com.example.backend.assessment.repository.RepositoryAnalysisRepository;
import com.example.backend.assessment.repository.TestCaseRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.common.enums.AnalysisStatus;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.workspace.dto.CandidateDto;
import com.example.backend.workspace.service.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentQueryService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceService workspaceService;
    private final RepositoryAnalysisRepository repositoryAnalysisRepository;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final TestCaseRepository testCaseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Object getAssessmentDetails(UUID recruiterId, UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId, "ASSESSMENT_NOT_FOUND"));

        if (candidateId != null && assessment.getCandidate().getId().equals(candidateId)) {
            return CandidateSafeAssessmentResponse.builder()
                    .id(assessment.getId())
                    .workspaceId(assessment.getWorkspace().getId())
                    .difficulty(assessment.getDifficulty())
                    .durationMinutes(assessment.getDurationMinutes())
                    .scheduledStartAt(assessment.getScheduledStartAt())
                    .scheduledEndAt(assessment.getScheduledEndAt())
                    .status(assessment.getStatus())
                    .build();
        }

        User recruiter = workspaceService.getRecruiterOrThrow(recruiterId);
        if (!assessment.getWorkspace().getRecruiter().getId().equals(recruiter.getId())) {
            throw new ForbiddenException("You do not have permission to access this assessment.", "FORBIDDEN");
        }

        return AssessmentDetailResponse.builder()
                .id(assessment.getId())
                .workspaceId(assessment.getWorkspace().getId())
                .candidate(CandidateDto.builder()
                        .id(assessment.getCandidate().getId())
                        .name(assessment.getCandidate().getName())
                        .email(assessment.getCandidate().getEmail())
                        .role(assessment.getCandidate().getRole())
                        .build())
                .repositoryUrl(assessment.getRepositoryUrl())
                .branchName(assessment.getBranchName())
                .backendRootDirectory(assessment.getBackendRootDirectory())
                .difficulty(assessment.getDifficulty())
                .durationMinutes(assessment.getDurationMinutes())
                .scheduledStartAt(assessment.getScheduledStartAt())
                .scheduledEndAt(assessment.getScheduledEndAt())
                .status(assessment.getStatus())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ProcessingStatusResponse getProcessingStatus(UUID recruiterId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId, "ASSESSMENT_NOT_FOUND"));

        User recruiter = workspaceService.getRecruiterOrThrow(recruiterId);
        if (!assessment.getWorkspace().getRecruiter().getId().equals(recruiter.getId())) {
            throw new ForbiddenException("You do not have permission to access this assessment.", "FORBIDDEN");
        }

        Optional<RepositoryAnalysis> analysisOpt = repositoryAnalysisRepository.findByAssessmentId(assessmentId);
        RepositoryAnalysisProcessingDto analysisDto = analysisOpt
                .map(a -> RepositoryAnalysisProcessingDto.builder()
                        .status(a.getAnalysisStatus())
                        .completedAt(a.getCompletedAt())
                        .build())
                .orElse(RepositoryAnalysisProcessingDto.builder()
                        .status(AnalysisStatus.PENDING)
                        .build());

        Optional<FeatureSpecification> featureOpt = featureSpecificationRepository.findByAssessmentId(assessmentId);
        FeatureSpecificationProcessingDto featureDto = FeatureSpecificationProcessingDto.builder()
                .status(featureOpt.isPresent() ? AnalysisStatus.COMPLETED : AnalysisStatus.PENDING)
                .available(featureOpt.isPresent())
                .build();

        long testCaseCount = testCaseRepository.countByAssessmentId(assessmentId);
        TestCaseProcessingDto testCaseDto = TestCaseProcessingDto.builder()
                .generatedCount(testCaseCount)
                .build();

        return ProcessingStatusResponse.builder()
                .assessmentId(assessment.getId())
                .assessmentStatus(assessment.getStatus())
                .repositoryAnalysis(analysisDto)
                .featureSpecification(featureDto)
                .testCases(testCaseDto)
                .build();
    }

    @Transactional(readOnly = true)
    public FeatureSpecificationResponse getFeatureSpecification(UUID recruiterId, UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId, "ASSESSMENT_NOT_FOUND"));

        if (candidateId != null) {
            if (!assessment.getCandidate().getId().equals(candidateId)) {
                throw new ForbiddenException("You do not have permission to access this assessment feature.", "FORBIDDEN");
            }
        } else if (recruiterId != null) {
            if (!assessment.getWorkspace().getRecruiter().getId().equals(recruiterId)) {
                throw new ForbiddenException("You do not have permission to access this assessment feature.", "FORBIDDEN");
            }
        }

        FeatureSpecification feature = featureSpecificationRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature specification is not available yet", "FEATURE_NOT_FOUND"));

        String endpoint = feature.getEndpoint() != null && !feature.getEndpoint().trim().isEmpty()
                ? feature.getEndpoint() : "/api/v1/resource";
        String httpMethod = feature.getHttpMethod() != null && !feature.getHttpMethod().trim().isEmpty()
                ? feature.getHttpMethod() : "POST";

        return FeatureSpecificationResponse.builder()
                .assessmentId(feature.getAssessmentId())
                .title(feature.getFeatureName())
                .featureName(feature.getFeatureName())
                .description(feature.getDescription())
                .endpoint(endpoint)
                .httpMethod(httpMethod)
                .requirements(parseJsonToMap(feature.getRequirements(), "items"))
                .requestSpecification(parseJsonToMap(feature.getRequestSpecification(), "request"))
                .responseSpecification(parseJsonToMap(feature.getResponseSpecification(), "response"))
                .constraints(parseJsonToMap(feature.getConstraints(), "constraints"))
                .createdAt(feature.getCreatedAt())
                .updatedAt(feature.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRepositoryAnalysis(UUID recruiterId, UUID candidateId, UUID assessmentId) {
        if (!assessmentRepository.existsById(assessmentId)) {
            throw new ResourceNotFoundException("Assessment not found with id: " + assessmentId, "ASSESSMENT_NOT_FOUND");
        }

        Optional<RepositoryAnalysis> analysisOpt = repositoryAnalysisRepository.findByAssessmentId(assessmentId);
        if (analysisOpt.isEmpty()) {
            return Map.of(
                    "status", "PENDING",
                    "projectStructure", Map.of("files", List.of()),
                    "sourceCodeStructure", Map.of("controllers", List.of(), "services", List.of(), "entities", List.of(), "repositories", List.of()),
                    "contentDetails", Map.of("endpoints", List.of())
            );
        }

        RepositoryAnalysis a = analysisOpt.get();
        Object projStruct = parseJsonOrRaw(a.getProjectStructure());
        Object srcStruct = parseJsonOrRaw(a.getSourceCodeStructure());
        Object contentDetails = parseJsonOrRaw(a.getContentDetails());

        return Map.of(
                "assessmentId", a.getAssessmentId(),
                "analysisStatus", a.getAnalysisStatus(),
                "projectStructure", projStruct != null ? projStruct : Map.of("files", List.of()),
                "sourceCodeStructure", srcStruct != null ? srcStruct : Map.of("controllers", List.of(), "services", List.of(), "entities", List.of(), "repositories", List.of()),
                "contentDetails", contentDetails != null ? contentDetails : Map.of("endpoints", List.of()),
                "completedAt", a.getCompletedAt() != null ? a.getCompletedAt().toString() : Instant.now().toString()
        );
    }

    private Object parseJsonOrRaw(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json, String fallbackKey) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            if (parsed instanceof Map) {
                return (Map<String, Object>) parsed;
            } else if (parsed instanceof List) {
                return Map.of(fallbackKey, parsed);
            } else {
                return Map.of(fallbackKey, parsed.toString());
            }
        } catch (Exception e) {
            return Map.of(fallbackKey, json);
        }
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAssessmentsByWorkspace(UUID recruiterId, UUID workspaceId) {
        log.info("Fetching all assessments for workspace ID: {}", workspaceId);
        List<Assessment> assessments = assessmentRepository.findAllByWorkspaceId(workspaceId);
        return assessments.stream().map(this::mapToAssessmentResponse).collect(Collectors.toList());
    }

    private AssessmentResponse mapToAssessmentResponse(Assessment assessment) {
        return AssessmentResponse.builder()
                .assessmentId(assessment.getId())
                .workspaceId(assessment.getWorkspace().getId())
                .candidateId(assessment.getCandidate().getId())
                .title(assessment.getTitle() != null ? assessment.getTitle() : "Java Spring Boot Technical Assessment")
                .repositoryUrl(assessment.getRepositoryUrl())
                .branchName(assessment.getBranchName())
                .backendRootDirectory(assessment.getBackendRootDirectory())
                .difficulty(assessment.getDifficulty())
                .durationMinutes(assessment.getDurationMinutes())
                .scheduledStartAt(assessment.getScheduledStartAt())
                .scheduledEndAt(assessment.getScheduledEndAt())
                .status(assessment.getStatus())
                .build();
    }
}
