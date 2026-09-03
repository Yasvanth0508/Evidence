package com.example.backend.pipeline.testcase;

import com.example.backend.common.enums.TestType;
import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseGenerationResult;
import com.example.backend.pipeline.testcase.dto.TestCaseItemDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DynamicTestGenerator implements TestGenerator {

    @Override
    public TestCaseGenerationResult generateTestCases(UUID assessmentId, FeatureGenerationResult featureResult) {
        List<TestCaseItemDto> suite = new ArrayList<>();

        String featureName = featureResult != null && featureResult.getFeatureName() != null ? featureResult.getFeatureName() : "Feature";
        String reqSpec = featureResult != null && featureResult.getRequestSpecification() != null ? featureResult.getRequestSpecification() : "";

        // Extract primary endpoint from request specification (e.g. POST /orders, POST /enrollments, etc.)
        String primaryEndpoint = "/items";
        String sampleBody = "{\"name\": \"Test Item\", \"status\": \"ACTIVE\"}";

        for (String line : reqSpec.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("POST /") || trimmed.startsWith("PUT /") || trimmed.startsWith("GET /")) {
                String[] parts = trimmed.split(" ");
                if (parts.length >= 2) {
                    primaryEndpoint = parts[1].split("\\\\?")[0].trim();
                    break;
                }
            }
        }

        // Test 1: Happy Path Creation
        suite.add(new TestCaseItemDto(
                1,
                TestType.BUSINESS_LOGIC,
                "POST",
                primaryEndpoint,
                sampleBody,
                201,
                "{\"id\": 1, \"status\": \"ACTIVE\"}",
                "[\"response.statusCode == 201\", \"response.body.id != null\"]",
                BigDecimal.valueOf(1.00),
                "Happy Path: Successfully create and register new " + featureName + " resource"
        ));

        // Test 2: Happy Path Retrieval
        String getEndpoint = primaryEndpoint.contains("{") ? primaryEndpoint.replaceAll("\\\\{[^}]+\\\\}", "1") : primaryEndpoint + "/1";
        suite.add(new TestCaseItemDto(
                2,
                TestType.INTEGRATION,
                "GET",
                getEndpoint,
                null,
                200,
                "{\"id\": 1}",
                "[\"response.statusCode == 200\", \"response.body.id != null\"]",
                BigDecimal.valueOf(1.00),
                "Happy Path: Retrieve resource details by ID"
        ));

        // Test 3: Status Transition / Update
        suite.add(new TestCaseItemDto(
                3,
                TestType.BUSINESS_LOGIC,
                "PUT",
                getEndpoint + "/status",
                "{\"status\": \"COMPLETED\"}",
                200,
                "{\"id\": 1, \"status\": \"COMPLETED\"}",
                "[\"response.statusCode == 200\", \"response.body.status == 'COMPLETED'\"]",
                BigDecimal.valueOf(1.00),
                "Happy Path: Transition status to COMPLETED"
        ));

        // Test 4: Validation Edge Case
        suite.add(new TestCaseItemDto(
                4,
                TestType.SYNTAX,
                "POST",
                primaryEndpoint,
                "{}",
                400,
                "{\"error\": \"Validation failed\"}",
                "[\"response.statusCode == 400\"]",
                BigDecimal.valueOf(1.00),
                "Validation Edge Case: Reject empty or invalid request body with 400 Bad Request"
        ));

        // Test 5: Not Found Handling
        suite.add(new TestCaseItemDto(
                5,
                TestType.DATA_FLOW,
                "GET",
                primaryEndpoint.replaceAll("/$", "") + "/999999",
                null,
                404,
                "{\"error\": \"Resource not found\"}",
                "[\"response.statusCode == 404\"]",
                BigDecimal.valueOf(1.00),
                "Error Handling: Return 404 Not Found for non-existent resource identifier"
        ));

        return TestCaseGenerationResult.ok(assessmentId, suite, "dynamic-ast-test-generator-v2");
    }
}
