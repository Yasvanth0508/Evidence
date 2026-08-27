package com.example.backend.assessment.service;

import com.example.backend.assessment.entity.TestCase;
import com.example.backend.assessment.entity.TestResult;
import com.example.backend.common.enums.TestResultStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlackBoxTestRunnerService {

    private final ResponseAssertionMatcher assertionMatcher;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Executes black-box test cases against the candidate container running on targetPort.
     */
    public List<TestResult> runTestCases(int targetPort, List<TestCase> testCases) {
        List<TestResult> results = new ArrayList<>();
        String baseUrl = "http://localhost:" + targetPort;

        for (TestCase tc : testCases) {
            String endpoint = tc.getEndpoint() != null ? tc.getEndpoint() : "/";
            if (!endpoint.startsWith("/")) {
                endpoint = "/" + endpoint;
            }
            String url = baseUrl + endpoint;
            String method = tc.getHttpMethod() != null ? tc.getHttpMethod().toUpperCase() : "GET";

            log.info("Executing Test #{} [{} {}] -> {}", tc.getTestCaseNumber(), method, endpoint, url);

            long start = System.currentTimeMillis();
            try {
                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json");

                String reqBody = tc.getRequestData() != null ? tc.getRequestData() : "";
                switch (method) {
                    case "POST" -> reqBuilder.POST(HttpRequest.BodyPublishers.ofString(reqBody));
                    case "PUT" -> reqBuilder.PUT(HttpRequest.BodyPublishers.ofString(reqBody));
                    case "DELETE" -> reqBuilder.DELETE();
                    default -> reqBuilder.GET();
                }

                HttpResponse<String> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
                long latency = System.currentTimeMillis() - start;

                int actualStatus = response.statusCode();
                String actualBody = response.body();

                int expectedStatus = tc.getExpectedStatusCode() != null ? tc.getExpectedStatusCode() : 200;
                ResponseAssertionMatcher.MatchResult matchResult = assertionMatcher.match(
                        expectedStatus, actualStatus, tc.getExpectedResponse(), actualBody, tc.getAssertions()
                );

                TestResultStatus status = matchResult.passed() ? TestResultStatus.PASSED : TestResultStatus.FAILED;

                TestResult testResult = new TestResult(
                        tc, status, actualStatus, actualBody, latency, matchResult.failureReason()
                );
                results.add(testResult);

            } catch (Exception ex) {
                long latency = System.currentTimeMillis() - start;
                String errMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                    errMsg += " (" + ex.getCause().getMessage() + ")";
                }
                log.warn("Test #{} failed to execute against {}: {}", tc.getTestCaseNumber(), url, errMsg);

                TestResult testResult = new TestResult(
                        tc, TestResultStatus.FAILED, 500, null, latency,
                        "HTTP Request Error: " + errMsg
                );
                results.add(testResult);
            }
        }

        return results;
    }
}
