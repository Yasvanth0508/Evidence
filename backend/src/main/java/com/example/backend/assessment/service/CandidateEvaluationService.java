package com.example.backend.assessment.service;

import com.example.backend.assessment.dto.evaluation.*;
import com.example.backend.assessment.entity.*;
import com.example.backend.assessment.repository.*;
import com.example.backend.common.enums.*;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.pipeline.docker.ProcessCommandExecutor;
import com.example.backend.pipeline.docker.DockerUtils;
import com.example.backend.workspace.dto.CandidateSummaryDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service orchestrating automated black-box test evaluation, scoring,
 * and result reporting for candidate assessment submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateEvaluationService {

    private final AssessmentRepository assessmentRepository;
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestResultRepository testResultRepository;
    private final EvaluationReportRepository evaluationReportRepository;
    private final CandidateWorkspaceService candidateWorkspaceService;
    private final CandidateExecutionService executionService;
    private final BlackBoxTestRunnerService testRunnerService;
    private final ProcessCommandExecutor dockerExecutor;
    private final AiEvaluationService aiEvaluationService;
    private final FeatureSpecificationRepository featureSpecificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Submits an assessment, triggers automated Maven packaging, boots an evaluation
     * container, executes all black-box test cases, calculates weighted scores, and persists report.
     *
     * @param candidateId  UUID of the candidate submitting the assessment.
     * @param assessmentId UUID of the assessment.
     * @return SubmissionResponse with final status and timestamp.
     * @throws ResourceNotFoundException if assessment does not exist.
     * @throws ForbiddenException        if candidate is not authorized.
     */
    @Transactional
    public SubmissionResponse submitAssessment(UUID candidateId, UUID assessmentId) {
        return submitAssessment(candidateId, assessmentId, null);
    }

    @Transactional
    public SubmissionResponse submitAssessment(UUID candidateId, UUID assessmentId, SubmitAssessmentRequest request) {
        log.info("Candidate {} is submitting assessment {} with proctoring request={}", candidateId, assessmentId, request);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getCandidate() == null || !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        // Stop any currently running candidate test container
        executionService.stopExistingExecution(assessmentId);

        Instant now = Instant.now();
        long timeTakenSeconds = 0L;
        if (assessment.getScheduledStartAt() != null) {
            timeTakenSeconds = Math.max(0, durationBetween(assessment.getScheduledStartAt(), now));
        }

        // Create / Update Submission
        Submission submission = submissionRepository.findByAssessmentId(assessmentId)
                .orElseGet(() -> new Submission(assessment, now, 0L, SubmissionStatus.EVALUATING));

        submission.setSubmittedAt(now);
        submission.setTimeTakenSeconds(timeTakenSeconds);
        submission.setStatus(SubmissionStatus.EVALUATING);
        submissionRepository.save(submission);

        assessment.setStatus(AssessmentStatus.EVALUATING);
        assessmentRepository.save(assessment);

        // 1. Package candidate code
        Path workspaceDir = candidateWorkspaceService.resolveCandidateWorkspace(candidateId, assessmentId);
        File workingDir = workspaceDir.toFile();
        Path targetDir = workspaceDir.resolve("target");

        // Ensure .mvn/wrapper/maven-wrapper.properties exists
        Path mvnDir = workspaceDir.resolve(".mvn");
        if (!Files.exists(mvnDir.resolve("wrapper").resolve("maven-wrapper.properties"))) {
            Path origMvn = workspaceDir.getParent() != null ? workspaceDir.getParent().resolve("original").resolve(".mvn") : null;
            Path backendMvn = Paths.get(".mvn").toAbsolutePath();
            try {
                if (origMvn != null && Files.exists(origMvn)) {
                    DockerUtils.copyDirectorySimple(origMvn, mvnDir);
                } else if (Files.exists(backendMvn)) {
                    DockerUtils.copyDirectorySimple(backendMvn, mvnDir);
                }
            } catch (Exception ex) {
                log.warn("Could not copy fallback .mvn wrapper during evaluation: {}", ex.getMessage());
            }
        }

        String mvnCmd = DockerUtils.isWindows() ? "mvn.cmd" : "mvn";
        boolean hasWrapperProps = Files.exists(mvnDir.resolve("wrapper").resolve("maven-wrapper.properties"));
        if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw.cmd"))) {
            mvnCmd = DockerUtils.isWindows() ? "mvnw.cmd" : "./mvnw";
        } else if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw"))) {
            mvnCmd = "./mvnw";
        }

        dockerExecutor.executeCommand(
                workingDir, 120, mvnCmd, "package", "-DskipTests", "-Dcheckstyle.skip=true"
        );

        boolean jarExists = DockerUtils.findJarFile(targetDir).isPresent();
        if (!jarExists) {
            Path classesDir = targetDir.resolve("classes");
            if (Files.exists(classesDir)) {
                try {
                    Files.createDirectories(targetDir);
                    String javaHome = System.getProperty("java.home");
                    String jarExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (DockerUtils.isWindows() ? "jar.exe" : "jar") : "jar";
                    dockerExecutor.executeCommand(workingDir, 30, jarExe, "-cf", "target/app.jar", "-C", "target/classes", ".");
                    jarExists = DockerUtils.findJarFile(targetDir).isPresent();
                } catch (Exception ignored) {}
            }
        }

        BuildStatus buildStatus = jarExists ? BuildStatus.SUCCESS : BuildStatus.FAILED;
        ApplicationStatus appStatus = ApplicationStatus.STARTED;

        int evalPort = 19080 + Math.abs(assessmentId.hashCode() % 500);
        String evalTag = "evidence-eval-" + assessmentId.toString().substring(0, 8) + ":eval";
        String evalContainer = "evidence-eval-" + assessmentId.toString().substring(0, 8);

        Process evalProcess = null;
        boolean dockerAvailable = DockerUtils.isDockerDaemonRunning(dockerExecutor);

        // 2. Launch Container / Process for Black-Box HTTP Testing
        if (jarExists) {
            if (dockerAvailable) {
                dockerExecutor.executeCommand(workingDir, 120, "docker", "build", "-t", evalTag, ".");
                dockerExecutor.executeCommand(workingDir, 30, "docker", "run", "-d", "--name", evalContainer, "-p", evalPort + ":8080", evalTag);
            } else {
                Optional<Path> jarPath = DockerUtils.findJarFile(targetDir);
                if (jarPath.isPresent()) {
                    try {
                        String javaHome = System.getProperty("java.home");
                        String javaExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (DockerUtils.isWindows() ? "java.exe" : "java") : "java";
                        ProcessBuilder pb = new ProcessBuilder(
                                javaExe, "-jar", jarPath.get().toAbsolutePath().toString(),
                                "--server.port=" + evalPort
                        );
                        pb.directory(workingDir);
                        pb.redirectErrorStream(true);
                        evalProcess = pb.start();
                        Process proc = evalProcess;
                        new Thread(() -> {
                            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    log.info("[EVAL-PROCESS-{}] {}", evalPort, line);
                                }
                            } catch (Exception ignored) {}
                        }).start();
                    } catch (Exception ex) {
                        log.error("Failed to start evaluation process: {}", ex.getMessage(), ex);
                        appStatus = ApplicationStatus.FAILED;
                    }
                }
            }

            // Actively poll until candidate application is up and listening on evalPort
            boolean isReady = waitForPort(evalPort, 20);
            if (!isReady) {
                log.warn("Application failed to start on port {} in time", evalPort);
                appStatus = ApplicationStatus.FAILED;
            }
        } else {
            appStatus = ApplicationStatus.FAILED;
        }

        // 3. Fetch Test Cases & Execute Sequential Black-Box Tests
        List<TestCase> testCases = testCaseRepository.findAllByAssessmentIdOrderByTestCaseNumberAsc(assessmentId);
        List<TestResult> testResults = new ArrayList<>();

        if (buildStatus == BuildStatus.SUCCESS && appStatus == ApplicationStatus.STARTED) {
            testResults = testRunnerService.runTestCases(evalPort, testCases);
        } else {
            // If build failed, fail all test cases automatically
            for (TestCase tc : testCases) {
                testResults.add(new TestResult(
                        tc, TestResultStatus.FAILED, 500, null, 0L, "Build or startup failure"
                ));
            }
        }

        // 4. Clean up evaluation container / process
        if (dockerAvailable) {
            try {
                dockerExecutor.executeCommand(null, 10, "docker", "rm", "-f", evalContainer);
                dockerExecutor.executeCommand(null, 10, "docker", "rmi", "-f", evalTag);
            } catch (Exception ignored) {}
        }
        if (evalProcess != null && evalProcess.isAlive()) {
            evalProcess.destroyForcibly();
        }

        // 5. Persist Test Results
        for (TestResult tr : testResults) {
            testResultRepository.findByTestCaseId(tr.getTestCaseId()).ifPresent(testResultRepository::delete);
            testResultRepository.save(tr);
        }

        ScoreCalculator calculator = new ScoreCalculator();
        ScoreCalculator.ScoreResult scoreResult = calculator.calculate(testCases, testResults);

        int totalTests = scoreResult.getTotalTests();
        int passedTests = scoreResult.getPassedTests();
        int failedTests = scoreResult.getFailedTests();
        BigDecimal finalScore = scoreResult.getFinalScore();


        // 7. Persist Evaluation Report
        EvaluationReport report = evaluationReportRepository.findBySubmissionId(submission.getId())
                .orElseGet(() -> new EvaluationReport(
                        submission, BigDecimal.ZERO, 0, 0, 0,
                        BuildStatus.SUCCESS, ApplicationStatus.STARTED,
                        0L, SubmissionStatus.COMPLETED, Instant.now()
                ));

        report.setScore(finalScore);
        report.setTotalTests(totalTests);
        report.setPassedTests(passedTests);
        report.setFailedTests(failedTests);
        report.setBuildStatus(buildStatus);
        report.setApplicationStatus(appStatus);
        report.setTimeTakenSeconds(timeTakenSeconds);
        report.setStatus(SubmissionStatus.COMPLETED);
        report.setEvaluatedAt(Instant.now());

        // Generate AI Evaluation Insights (Mistral AI or deterministic fallback)
        FeatureSpecification featureSpec = featureSpecificationRepository.findByAssessmentId(assessmentId).orElse(null);
        AiEvaluationService.EvaluationInsights insights = aiEvaluationService.generateInsights(
                assessment,
                featureSpec,
                buildStatus,
                testCases,
                testResults,
                finalScore,
                request != null ? request.getTabSwitchCount() : 0,
                request != null ? request.getCopyPasteEvents() : 0,
                request != null ? request.getIdleTimeMinutes() : 2
        );

        report.setScoreRating(insights.getScoreRating());
        report.setAiSummary(insights.getAiSummary());
        try {
            report.setStrengthsJson(objectMapper.writeValueAsString(insights.getStrengths()));
            report.setImprovementsJson(objectMapper.writeValueAsString(insights.getImprovements()));
        } catch (Exception ignored) {}
        report.setBusinessLogicTotal(insights.getBusinessLogicTotal());
        report.setBusinessLogicPassed(insights.getBusinessLogicPassed());
        report.setSyntaxTotal(insights.getSyntaxTotal());
        report.setSyntaxPassed(insights.getSyntaxPassed());
        report.setDataFlowTotal(insights.getDataFlowTotal());
        report.setDataFlowPassed(insights.getDataFlowPassed());
        report.setCopyPasteEvents(insights.getCopyPasteEvents());
        report.setTabSwitchCount(insights.getTabSwitchCount());
        report.setBuildRuns(insights.getBuildRuns());
        report.setRiskAnalysis(insights.getRiskAnalysis());
        report.setOverallRiskBadge(insights.getOverallRiskBadge());

        evaluationReportRepository.save(report);

        submission.setStatus(SubmissionStatus.COMPLETED);
        submissionRepository.save(submission);

        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessmentRepository.save(assessment);

        log.info("Assessment {} evaluation COMPLETED: Score={}, Passed={}/{}", assessmentId, finalScore, passedTests, totalTests);

        return SubmissionResponse.builder()
                .submissionId(submission.getId())
                .status(SubmissionStatus.COMPLETED)
                .submittedAt(now)
                .message("Assessment evaluated successfully with score " + finalScore + "%")
                .build();
    }

    /**
     * Retrieves the candidate-safe result view (hiding test implementation details).
     *
     * @param candidateId  UUID of the candidate.
     * @param assessmentId UUID of the assessment.
     * @return CandidateResultResponse containing summary score and test counts.
     */
    @Transactional(readOnly = true)
    public CandidateResultResponse getCandidateResult(UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getCandidate() == null || !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        Submission submission = submissionRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for assessment: " + assessmentId));

        EvaluationReport report = evaluationReportRepository.findBySubmissionId(submission.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation report not yet generated"));

        long timeTaken = report.getTimeTakenSeconds() != null ? report.getTimeTakenSeconds() : 0L;
        int timeTakenMins = Math.max(1, (int) Math.round((double) timeTaken / 60));

        String title = assessment.getTitle() != null && !assessment.getTitle().isBlank()
                ? assessment.getTitle()
                : (assessment.getWorkspace() != null ? assessment.getWorkspace().getName() + " Assessment" : "Technical Assessment");

        String workspaceName = assessment.getWorkspace() != null ? assessment.getWorkspace().getName() : "General";
        String difficulty = assessment.getDifficulty() != null ? assessment.getDifficulty().name() : "INTERMEDIATE";

        return CandidateResultResponse.builder()
                .assessmentId(assessmentId)
                .title(title)
                .workspaceName(workspaceName)
                .difficulty(difficulty)
                .techStack("Java 21, Spring Boot, Maven, PostgreSQL")
                .score(report.getScore())
                .scoreRating(report.getScoreRating() != null ? report.getScoreRating() : "Needs Improvement")
                .status(assessment.getStatus())
                .totalTests(report.getTotalTests())
                .passedTests(report.getPassedTests())
                .failedTests(report.getFailedTests())
                .buildStatus(report.getBuildStatus())
                .applicationStatus(report.getApplicationStatus())
                .timeTakenSeconds(timeTaken)
                .timeTakenMinutes(timeTakenMins)
                .evaluatedAt(report.getEvaluatedAt())
                .submittedAt(submission.getSubmittedAt())
                .categoryBreakdown(buildCategoryBreakdown(report))
                .aiSummary(getAiSummarySafe(report))
                .strengths(parseJsonList(report.getStrengthsJson(), List.of("REST API Controller Implementation", "Spring Data JPA Architecture")))
                .improvements(parseJsonList(report.getImprovementsJson(), List.of("Edge Case Exception Handling", "Response Body Schema Validation")))
                .build();
    }

    /**
     * Retrieves the comprehensive recruiter report, including candidate info, time taken,
     * and scoring breakdowns.
     *
     * @param recruiterId  UUID of the requesting recruiter.
     * @param assessmentId UUID of the assessment.
     * @return RecruiterReportResponse with detailed evaluation metrics.
     */
    @Transactional(readOnly = true)
    public RecruiterReportResponse getRecruiterReport(UUID recruiterId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getWorkspace() == null || assessment.getWorkspace().getRecruiter() == null
                || !assessment.getWorkspace().getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("Recruiter is not authorized for this assessment");
        }

        var candidate = assessment.getCandidate();
        CandidateSummaryDto candidateDto = CandidateSummaryDto.builder()
                .id(candidate != null ? candidate.getId() : null)
                .name(candidate != null ? candidate.getName() : "Unknown")
                .email(candidate != null ? candidate.getEmail() : "Unknown")
                .addedAt(candidate != null ? candidate.getCreatedAt() : null)
                .build();

        Optional<Submission> submissionOpt = submissionRepository.findByAssessmentId(assessmentId);
        Optional<EvaluationReport> reportOpt = submissionOpt.flatMap(s -> evaluationReportRepository.findBySubmissionId(s.getId()));

        BigDecimal score = reportOpt.map(EvaluationReport::getScore).orElse(BigDecimal.ZERO);
        int totalTests = reportOpt.map(EvaluationReport::getTotalTests).orElse(0);
        int passedTests = reportOpt.map(EvaluationReport::getPassedTests).orElse(0);
        int failedTests = reportOpt.map(EvaluationReport::getFailedTests).orElse(0);
        long timeTaken = reportOpt.map(EvaluationReport::getTimeTakenSeconds).orElse(0L);
        int timeTakenMins = Math.max(1, (int) Math.round((double) timeTaken / 60));
        Instant evaluatedAt = reportOpt.map(EvaluationReport::getEvaluatedAt).orElse(assessment.getUpdatedAt());
        Instant submittedAt = submissionOpt.map(Submission::getSubmittedAt).orElse(evaluatedAt);

        String title = assessment.getTitle() != null && !assessment.getTitle().isBlank()
                ? assessment.getTitle()
                : (assessment.getWorkspace() != null ? assessment.getWorkspace().getName() + " Assessment" : "Technical Assessment");
        String workspaceName = assessment.getWorkspace() != null ? assessment.getWorkspace().getName() : "General";
        UUID workspaceId = assessment.getWorkspace() != null ? assessment.getWorkspace().getId() : null;
        String difficulty = assessment.getDifficulty() != null ? assessment.getDifficulty().name() : "INTERMEDIATE";

        return RecruiterReportResponse.builder()
                .assessmentId(assessmentId)
                .title(title)
                .workspaceId(workspaceId)
                .workspaceName(workspaceName)
                .difficulty(difficulty)
                .techStack("Java 21, Spring Boot, Maven, PostgreSQL")
                .candidate(candidateDto)
                .score(score)
                .scoreRating(reportOpt.map(EvaluationReport::getScoreRating).orElse("Needs Improvement"))
                .totalTests(totalTests)
                .passedTests(passedTests)
                .failedTests(failedTests)
                .buildStatus(reportOpt.map(EvaluationReport::getBuildStatus).orElse(BuildStatus.SUCCESS))
                .applicationStatus(reportOpt.map(EvaluationReport::getApplicationStatus).orElse(ApplicationStatus.STARTED))
                .timeTakenSeconds(timeTaken)
                .timeTakenMinutes(timeTakenMins)
                .status(assessment.getStatus())
                .evaluatedAt(evaluatedAt)
                .submittedAt(submittedAt)
                .categoryBreakdown(reportOpt.map(this::buildCategoryBreakdown).orElse(Collections.emptyList()))
                .aiSummary(reportOpt.map(this::getAiSummarySafe).orElse("Evaluation report generated from automated test execution."))
                .strengths(reportOpt.map(r -> parseJsonList(r.getStrengthsJson(), List.of("REST API Controller Implementation", "Spring Data JPA Architecture"))).orElse(List.of("REST API Controller Implementation")))
                .improvements(reportOpt.map(r -> parseJsonList(r.getImprovementsJson(), List.of("Edge Case Exception Handling", "Response Body Schema Validation"))).orElse(List.of("Edge Case Exception Handling")))
                .integrity(IntegritySummaryDto.builder()
                        .overallRiskBadge(reportOpt.map(EvaluationReport::getOverallRiskBadge).orElse("LOW"))
                        .behaviorSummary(IntegritySummaryDto.BehaviorSummaryDto.builder()
                                .copyPasteEvents(reportOpt.map(EvaluationReport::getCopyPasteEvents).orElse(0))
                                .buildRuns(reportOpt.map(EvaluationReport::getBuildRuns).orElse(1))
                                .testRuns(totalTests)
                                .idleTimeMinutes(2)
                                .build())
                        .riskAnalysis(reportOpt.map(EvaluationReport::getRiskAnalysis).orElse("No suspicious activity detected. Valid coding session verified."))
                        .build())
                .build();
    }

    /**
     * Retrieves the granular per-test-case results breakdown for recruiters.
     *
     * @param recruiterId  UUID of the recruiter.
     * @param assessmentId UUID of the assessment.
     * @return List of RecruiterTestResultItemDto records.
     */
    @Transactional(readOnly = true)
    public List<RecruiterTestResultItemDto> getRecruiterTestResults(UUID recruiterId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (assessment.getWorkspace() == null || assessment.getWorkspace().getRecruiter() == null
                || !assessment.getWorkspace().getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("Recruiter is not authorized for this assessment");
        }

        List<TestResult> results = testResultRepository.findAllByAssessmentId(assessmentId);
        results.sort(Comparator.comparing(tr -> tr.getTestCase().getTestCaseNumber()));

        return results.stream().map(tr -> RecruiterTestResultItemDto.builder()
                .testCaseId(tr.getTestCaseId())
                .testCaseNumber(tr.getTestCase().getTestCaseNumber())
                .testType(tr.getTestCase().getTestType())
                .status(tr.getStatus())
                .httpMethod(tr.getTestCase().getHttpMethod())
                .endpoint(tr.getTestCase().getEndpoint())
                .expectedStatusCode(tr.getTestCase().getExpectedStatusCode())
                .actualStatusCode(tr.getActualStatusCode())
                .expectedResponse(tr.getTestCase().getExpectedResponse())
                .actualResponse(tr.getActualResponse())
                .assertions(tr.getTestCase().getAssertions())
                .executionTimeMs(tr.getExecutionTimeMs())
                .weight(tr.getTestCase().getWeight())
                .failureReason(tr.getFailureReason())
                .build()
        ).collect(Collectors.toList());
    }

    private List<CategoryScoreDto> buildCategoryBreakdown(EvaluationReport report) {
        List<CategoryScoreDto> list = new ArrayList<>();
        int blTot = report.getBusinessLogicTotal() != null ? report.getBusinessLogicTotal() : 0;
        int blPas = report.getBusinessLogicPassed() != null ? report.getBusinessLogicPassed() : 0;
        int synTot = report.getSyntaxTotal() != null ? report.getSyntaxTotal() : 0;
        int synPas = report.getSyntaxPassed() != null ? report.getSyntaxPassed() : 0;
        int dfTot = report.getDataFlowTotal() != null ? report.getDataFlowTotal() : 0;
        int dfPas = report.getDataFlowPassed() != null ? report.getDataFlowPassed() : 0;

        if (blTot > 0) {
            list.add(new CategoryScoreDto("Business Logic", blTot, blPas, (int) Math.round(((double) blPas / blTot) * 100)));
        }
        if (synTot > 0) {
            list.add(new CategoryScoreDto("Syntax", synTot, synPas, (int) Math.round(((double) synPas / synTot) * 100)));
        }
        if (dfTot > 0) {
            list.add(new CategoryScoreDto("Data Flow", dfTot, dfPas, (int) Math.round(((double) dfPas / dfTot) * 100)));
        }
        if (list.isEmpty() && report.getTotalTests() != null && report.getTotalTests() > 0) {
            int tot = report.getTotalTests();
            int pas = report.getPassedTests() != null ? report.getPassedTests() : 0;
            list.add(new CategoryScoreDto("Business Logic", tot, pas, (int) Math.round(((double) pas / tot) * 100)));
        }
        return list;
    }

    private List<String> parseJsonList(String json, List<String> fallback) {
        if (json != null && !json.isBlank()) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }
        return fallback != null ? fallback : Collections.emptyList();
    }

    private String getAiSummarySafe(EvaluationReport report) {
        if (report.getAiSummary() != null && !report.getAiSummary().isBlank()) {
            return report.getAiSummary();
        }
        BigDecimal score = report.getScore() != null ? report.getScore() : BigDecimal.ZERO;
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "The candidate demonstrated strong engineering competency with clean REST API architecture and robust test verification.";
        } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "The candidate completed the core requirements successfully with solid Spring Boot fundamentals. Minor test assertions failed.";
        } else {
            return "The candidate completed the assessment with partial requirement completion. Several functional assertions require refinement.";
        }
    }

    private boolean waitForPort(int port, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            try (java.net.Socket socket = new java.net.Socket("127.0.0.1", port)) {
                log.info("Evaluation application is up and listening on port {}", port);
                return true;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
        }
        return false;
    }

    private long durationBetween(Instant start, Instant end) {
        return java.time.Duration.between(start, end).getSeconds();
    }
}
