package com.example.backend.pipeline.testcase;

import com.example.backend.pipeline.feature.dto.FeatureGenerationResult;
import org.springframework.stereotype.Component;

@Component
public class TestCasePromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are a Lead QA Automation Architect specializing in API Testing and Automated Evaluation.
                Your task is to generate a comprehensive suite of Black-Box HTTP Test Cases for evaluating a candidate's implementation of a Spring Boot feature.
                
                CRITICAL INSTRUCTIONS:
                1. Test cases must strictly target the endpoints, request schemas, and response expectations specified in the feature requirements.
                2. Generate at least 5-8 distinct test cases covering:
                   - Happy Path creation (POST)
                   - Happy Path retrieval (GET)
                   - Happy Path update/calculation (PUT/POST)
                   - Validation edge cases (invalid/negative inputs -> 400 Bad Request)
                   - Not Found errors (non-existent IDs -> 404 Not Found)
                3. Each test case must have concrete assertions, expected status codes, and request bodies.
                
                You must return a valid JSON object with a "testCases" array adhering to this schema:
                {
                  "testCases": [
                    {
                      "testCaseNumber": 1,
                      "testType": "BUSINESS_LOGIC", // Options: BUSINESS_LOGIC, DATA_FLOW, INTEGRATION, SYNTAX, UNIT
                      "httpMethod": "POST", // Options: GET, POST, PUT, DELETE, PATCH
                      "endpoint": "/orders/checkout",
                      "requestData": "{\\"userId\\": 1, \\"productId\\": 2, \\"quantity\\": 3}",
                      "expectedStatusCode": 201,
                      "expectedResponse": "{\\"id\\": 101, \\"totalAmount\\": 240.0}",
                      "assertions": "[\\"response.statusCode == 201\\", \\"response.body.id != null\\"]",
                      "weight": 1.0,
                      "description": "Happy Path: Successfully process order checkout with valid promotional discount"
                    }
                  ]
                }
                
                Return ONLY the valid JSON object. Do not include markdown code block formatting (```json) or extra text.
                """;
    }

    public String buildUserPrompt(FeatureGenerationResult featureResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TARGET FEATURE SPECIFICATION ===\n\n");
        sb.append("Feature Title: ").append(featureResult.getFeatureName()).append("\n\n");
        sb.append("Feature Description:\n").append(featureResult.getDescription()).append("\n\n");
        sb.append("Technical Requirements:\n").append(featureResult.getRequirements()).append("\n\n");
        sb.append("Request Specification:\n").append(featureResult.getRequestSpecification()).append("\n\n");
        sb.append("Response Specification:\n").append(featureResult.getResponseSpecification()).append("\n\n");
        sb.append("Constraints:\n").append(featureResult.getConstraints()).append("\n\n");

        sb.append("=== TASK ===\n");
        sb.append("Generate a complete, executable suite of Black-Box HTTP Test Cases strictly matching the above feature requirements. Output as valid JSON.");
        return sb.toString();
    }
}
