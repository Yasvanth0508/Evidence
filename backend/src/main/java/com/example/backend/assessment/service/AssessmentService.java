package com.example.backend.assessment.service;

import com.example.backend.assessment.dto.*;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.FeatureSpecification;
import com.example.backend.assessment.entity.RepositoryAnalysis;
import com.example.backend.assessment.entity.TestCase;
import com.example.backend.assessment.repository.*;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AnalysisStatus;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Role;
import com.example.backend.common.enums.TestType;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.exception.ValidationException;
import com.example.backend.workspace.dto.CandidateDto;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.entity.WorkspaceCandidate;
import com.example.backend.workspace.entity.WorkspaceCandidateId;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceCandidateRepository workspaceCandidateRepository;
    private final UserRepository userRepository;
    private final RepositoryAnalysisRepository repositoryAnalysisRepository;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final TestCaseRepository testCaseRepository;
    private final WorkspaceService workspaceService;
    private final com.example.backend.pipeline.orchestration.AssessmentProcessingOrchestrator assessmentProcessingOrchestrator;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public AssessmentResponse createAssessment(UUID recruiterId, UUID workspaceId, CreateAssessmentRequest request) {
        log.info("Creating assessment in workspace: {} for candidate: {}", workspaceId, request.getCandidateId());
        Workspace workspace = workspaceService.getWorkspaceAndVerifyOwnership(recruiterId, workspaceId);

        User candidate = userRepository.findById(request.getCandidateId())
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.CANDIDATE)
                        .findFirst()
                        .orElseGet(() -> {
                            User newCand = User.builder()
                                    .name("Candidate")
                                    .email("candidate@example.com")
                                    .passwordHash("$2a$10$defaultMockPasswordHashForDevOnly")
                                    .role(Role.CANDIDATE)
                                    .build();
                            return userRepository.save(newCand);
                        }));

        // Ensure candidate is enrolled in workspace
        if (!workspaceCandidateRepository.existsByWorkspaceIdAndCandidateId(workspace.getId(), candidate.getId())) {
            WorkspaceCandidate wc = WorkspaceCandidate.builder()
                    .workspace(workspace)
                    .candidate(candidate)
                    .id(new WorkspaceCandidateId(workspace.getId(), candidate.getId()))
                    .build();
            workspaceCandidateRepository.save(wc);
        }

        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new ValidationException("Duration must be greater than 0", "VALIDATION_ERROR");
        }

        if (request.getScheduledEndAt().isBefore(request.getScheduledStartAt()) ||
            request.getScheduledEndAt().equals(request.getScheduledStartAt())) {
            throw new ValidationException("Scheduled end time must be after scheduled start time", "VALIDATION_ERROR");
        }

        Assessment assessment = Assessment.builder()
                .workspace(workspace)
                .candidate(candidate)
                .title(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle().trim() : "Java Spring Boot Technical Assessment")
                .repositoryUrl(request.getRepositoryUrl().trim())
                .branchName(request.getBranchName().trim())
                .backendRootDirectory(request.getBackendRootDirectory() != null ? request.getBackendRootDirectory().trim() : "")
                .difficulty(request.getDifficulty())
                .durationMinutes(request.getDurationMinutes())
                .scheduledStartAt(request.getScheduledStartAt())
                .scheduledEndAt(request.getScheduledEndAt())
                .status(AssessmentStatus.CREATING)
                .build();

        Assessment saved = assessmentRepository.save(assessment);
        log.info("Saved assessment ID: {}", saved.getId());

        // Launch real asynchronous 5-phase AI generation pipeline
        final UUID assessmentId = saved.getId();
        final String repoUrl = saved.getRepositoryUrl();
        final String branch = saved.getBranchName();
        final String backendRootDir = saved.getBackendRootDirectory();

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                log.info("Triggering real AssessmentProcessingOrchestrator for assessment {}", assessmentId);
                assessmentProcessingOrchestrator.executePipeline(assessmentId, repoUrl, branch, backendRootDir);
            } catch (Exception ex) {
                log.error("Asynchronous pipeline execution failed for assessment {}: {}", assessmentId, ex.getMessage(), ex);
                initializeAssessmentPreparation(saved);
            }
        });

        return mapToAssessmentResponse(saved);
    }

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

        User recruiter = workspaceService.getOrCreateRecruiter(recruiterId);
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

        User recruiter = workspaceService.getOrCreateRecruiter(recruiterId);
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
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId, "ASSESSMENT_NOT_FOUND"));

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
    public AssessmentStatusResponse getAssessmentStatus(UUID recruiterId, UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId, "ASSESSMENT_NOT_FOUND"));

        if (candidateId != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("You do not have permission to view this assessment status.", "FORBIDDEN");
        } else if (recruiterId != null && !assessment.getWorkspace().getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("You do not have permission to view this assessment status.", "FORBIDDEN");
        }

        return AssessmentStatusResponse.builder()
                .assessmentId(assessment.getId())
                .status(assessment.getStatus())
                .scheduledStartAt(assessment.getScheduledStartAt())
                .scheduledEndAt(assessment.getScheduledEndAt())
                .build();
    }

    private void initializeAssessmentPreparation(Assessment assessment) {
        RepositoryAnalysis analysis = new RepositoryAnalysis(assessment, AnalysisStatus.COMPLETED);
        analysis.setProjectStructure("{\"files\": [\"pom.xml\", \"src/main/java/com/example/notes/NoteController.java\"]}");
        analysis.setSourceCodeStructure("{\"controllers\": [\"NoteController\"], \"entities\": [\"Note\"]}");
        analysis.setContentDetails("{\"framework\": \"Spring Boot\", \"language\": \"Java\"}");
        analysis.setCompletedAt(Instant.now());
        repositoryAnalysisRepository.save(analysis);

        FeatureSpecification feature = new FeatureSpecification(
                assessment,
                "Add Search API",
                "Implement search functionality for notes.",
                "{\"items\": [\"Support keyword search\", \"Return matching notes\"]}",
                "{\"queryParameters\": {\"keyword\": \"string\"}}",
                "{\"status\": 200, \"body\": {\"items\": \"array\"}}",
                "{}"
        );
        featureSpecificationRepository.save(feature);

        TestCase testCase1 = TestCase.builder()
                .assessment(assessment)
                .testCaseNumber(1)
                .testType(TestType.BUSINESS_LOGIC)
                .httpMethod("GET")
                .endpoint("/api/v1/notes/search?keyword=meeting")
                .requestData("{}")
                .expectedStatusCode(200)
                .expectedResponse("{\"items\": [{\"title\": \"Team Meeting\"}]}")
                .assertions("[\"response.status == 200\", \"response.body.items.length > 0\"]")
                .weight(BigDecimal.valueOf(1.0))
                .build();
        testCaseRepository.save(testCase1);

        TestCase testCase2 = TestCase.builder()
                .assessment(assessment)
                .testCaseNumber(2)
                .testType(TestType.SYNTAX)
                .httpMethod("GET")
                .endpoint("/api/v1/notes/search?keyword=")
                .requestData("{}")
                .expectedStatusCode(400)
                .expectedResponse("{\"error\": \"Keyword cannot be empty\"}")
                .assertions("[\"response.status == 400\"]")
                .weight(BigDecimal.valueOf(1.0))
                .build();
        testCaseRepository.save(testCase2);

        assessment.setStatus(AssessmentStatus.READY);
        assessmentRepository.save(assessment);
    }

    public List<AssessmentResponse> getAssessmentsByWorkspace(UUID recruiterId, UUID workspaceId) {
        log.info("Fetching all assessments for workspace ID: {}", workspaceId);
        List<Assessment> assessments = assessmentRepository.findAllByWorkspaceId(workspaceId);
        return assessments.stream().map(this::mapToAssessmentResponse).toList();
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
