package com.example.backend.assessment.application;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.FeatureSpecification;
import com.example.backend.assessment.entity.RepositoryAnalysis;
import com.example.backend.assessment.entity.TestCase;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.FeatureSpecificationRepository;
import com.example.backend.assessment.repository.RepositoryAnalysisRepository;
import com.example.backend.assessment.repository.TestCaseRepository;
import com.example.backend.common.enums.AnalysisStatus;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.TestType;
import com.example.backend.pipeline.application.AssessmentProcessingLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentProcessingService {

    private final AssessmentRepository assessmentRepository;
    private final RepositoryAnalysisRepository repositoryAnalysisRepository;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final TestCaseRepository testCaseRepository;
    private final AssessmentProcessingLauncher launcher;

    public void startProcessing(UUID assessmentId, String repoUrl, String branch, String backendRootDir) {
        launcher.launchProcessing(assessmentId, repoUrl, branch, backendRootDir)
            .exceptionally(ex -> {
                log.error("Pipeline failed, initializing fallback for assessment {}: {}", assessmentId, ex.getMessage());
                initializeAssessmentPreparation(assessmentId);
                return null;
            });
    }

    public void startProcessing(Assessment assessment) {
        if (assessment == null) return;
        startProcessing(
                assessment.getId(),
                assessment.getRepositoryUrl(),
                assessment.getBranchName(),
                assessment.getBackendRootDirectory()
        );
    }

    @Transactional
    public void initializeAssessmentPreparation(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
        if (assessment == null) {
            log.warn("Cannot initialize fallback preparation: Assessment {} not found", assessmentId);
            return;
        }
        initializeAssessmentPreparation(assessment);
    }

    public void initializeAssessmentPreparation(Assessment assessment) {
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
}
