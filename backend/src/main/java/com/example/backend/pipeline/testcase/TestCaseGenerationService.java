package com.example.backend.pipeline.testcase;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.entity.TestCase;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.assessment.repository.TestCaseRepository;
import com.example.backend.common.enums.TestType;
import com.example.backend.pipeline.feature.client.MistralAiClient;
import com.example.backend.pipeline.feature.config.MistralAiConfig;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseItemDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TestCaseGenerationService {

    private static final Logger log = LoggerFactory.getLogger(TestCaseGenerationService.class);

    private final TestGenerator mistralTestGenerator;
    private final TestGenerator dynamicTestGenerator;
    private final TestCaseRepository testCaseRepository;
    private final AssessmentRepository assessmentRepository;

    public TestCaseGenerationService(MistralTestGenerator mistralTestGenerator,
                                     DynamicTestGenerator dynamicTestGenerator,
                                     TestCaseRepository testCaseRepository,
                                     AssessmentRepository assessmentRepository) {
        this.mistralTestGenerator = mistralTestGenerator;
        this.dynamicTestGenerator = dynamicTestGenerator;
        this.testCaseRepository = testCaseRepository;
        this.assessmentRepository = assessmentRepository;
    }

    /**
     * Executes Phase 5: Generates Black-Box Test Cases using Mistral AI (or dynamic domain test generator).
     */
    @Transactional
    public TestCaseGenerationResult generateTestCases(UUID assessmentId, FeatureGenerationResult featureResult) {
        log.info("Phase 5: Starting AI Black-Box Test Case Generation for Assessment {}", assessmentId);

        TestCaseGenerationResult result = mistralTestGenerator.generateTestCases(assessmentId, featureResult);

        if (result == null) {
            log.info("Mistral AI test case generation returned null. Falling back to dynamic test generator.");
            result = dynamicTestGenerator.generateTestCases(assessmentId, featureResult);
        }

        // Persist to PostgreSQL Database
        persistTestCases(assessmentId, result.getTestCases());

        log.info("Phase 5: Test Case Generation COMPLETED for Assessment {} (Generated {} test cases)", assessmentId, result.getTestCases().size());

        return result;
    }

    private void persistTestCases(UUID assessmentId, List<TestCaseItemDto> dtoList) {
        if (testCaseRepository == null || assessmentId == null) {
            return;
        }

        try {
            Assessment assessment = assessmentRepository != null ? assessmentRepository.findById(assessmentId).orElse(null) : null;
            if (assessment == null) {
                return;
            }

            testCaseRepository.deleteByAssessmentId(assessmentId);

            List<TestCase> entities = new ArrayList<>();
            for (TestCaseItemDto dto : dtoList) {
                TestCase entity = new TestCase(
                        assessment,
                        dto.getTestCaseNumber(),
                        dto.getTestType(),
                        dto.getHttpMethod(),
                        dto.getEndpoint(),
                        dto.getRequestData(),
                        dto.getExpectedStatusCode(),
                        dto.getExpectedResponse(),
                        dto.getAssertions(),
                        dto.getWeight()
                );
                entities.add(entity);
            }

            testCaseRepository.saveAll(entities);
            log.info("Phase 5: Saved {} TEST_CASE records to DB for assessment {}", entities.size(), assessmentId);
        } catch (Exception ex) {
            log.warn("Could not persist TEST_CASE entities to DB: {}", ex.getMessage());
        }
    }
}
