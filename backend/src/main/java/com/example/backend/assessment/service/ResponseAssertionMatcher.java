package com.example.backend.assessment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResponseAssertionMatcher {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record MatchResult(boolean passed, String failureReason) {}

    public MatchResult match(int expectedStatus, int actualStatus, String expectedResponse, String actualResponse, String assertions) {
        // 1. Check HTTP Status Code
        if (expectedStatus > 0 && expectedStatus != actualStatus) {
            return new MatchResult(false, "HTTP Status mismatch: Expected " + expectedStatus + " but received " + actualStatus);
        }

        // 2. If status matched and no expected response is specified, pass
        if (expectedResponse == null || expectedResponse.trim().isEmpty() || expectedResponse.trim().equals("{}") || expectedResponse.trim().equals("[]")) {
            return new MatchResult(true, null);
        }

        // 3. Match JSON Structure / Content if actualResponse is present
        if (actualResponse == null || actualResponse.trim().isEmpty()) {
            return new MatchResult(false, "Response body was empty; expected: " + expectedResponse);
        }

        try {
            JsonNode expectedNode = objectMapper.readTree(expectedResponse);
            JsonNode actualNode = objectMapper.readTree(actualResponse);

            if (expectedNode.isObject() && actualNode.isObject()) {
                var fieldNames = expectedNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String field = fieldNames.next();
                    if (!actualNode.has(field)) {
                        return new MatchResult(false, "Missing expected JSON field in response: '" + field + "'");
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Notice during JSON assertion matching: {}", ex.getMessage());
            // Fallback to substring matching if not strict JSON
            if (!actualResponse.contains(expectedResponse.trim())) {
                return new MatchResult(false, "Response content mismatch");
            }
        }

        return new MatchResult(true, null);
    }
}
