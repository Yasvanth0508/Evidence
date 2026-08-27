package com.example.backend.assessment.service;

import com.example.backend.assessment.dto.evaluation.CandidateResultResponse;
import com.example.backend.assessment.dto.evaluation.RecruiterReportResponse;
import com.example.backend.assessment.dto.evaluation.RecruiterTestResultItemDto;
import com.example.backend.assessment.dto.evaluation.SubmissionResponse;
import com.example.backend.assessment.entity.*;
import com.example.backend.assessment.repository.*;
import com.example.backend.common.enums.*;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.pipeline.docker.DockerCommandExecutor;
import com.example.backend.workspace.dto.CandidateSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final DockerCommandExecutor dockerExecutor;

    /**
     * Phase C.1 - C.4: Submit assessment, run automated test suite, compute score, persist evaluation report.
     */
    @Transactional
    public SubmissionResponse submitAssessment(UUID candidateId, UUID assessmentId) {
        log.info("Candidate {} is submitting assessment {}", candidateId, assessmentId);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (candidateId != null && assessment.getCandidate() != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        // Stop any currently running candidate test container
        executionService.stopExistingExecution(assessmentId);

        Instant now = Instant.now();
        long timeTakenSeconds = 0L;
        if (assessment.getScheduledStartAt() != null) {
            timeTakenSeconds = Math.max(0, DurationBetween(assessment.getScheduledStartAt(), now));
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
                    copyDirectorySimple(origMvn, mvnDir);
                } else if (Files.exists(backendMvn)) {
                    copyDirectorySimple(backendMvn, mvnDir);
                }
            } catch (Exception ex) {
                log.warn("Could not copy fallback .mvn wrapper during evaluation: {}", ex.getMessage());
            }
        }

        String mvnCmd = isWindows() ? "mvn.cmd" : "mvn";
        boolean hasWrapperProps = Files.exists(mvnDir.resolve("wrapper").resolve("maven-wrapper.properties"));
        if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw.cmd"))) {
            mvnCmd = isWindows() ? "mvnw.cmd" : "./mvnw";
        } else if (hasWrapperProps && Files.exists(workspaceDir.resolve("mvnw"))) {
            mvnCmd = "./mvnw";
        }

        DockerCommandExecutor.ProcessResult packageResult = dockerExecutor.executeCommand(
                workingDir, 120, mvnCmd, "package", "-DskipTests", "-Dcheckstyle.skip=true"
        );

        boolean jarExists = findJarFile(targetDir).isPresent();
        if (!jarExists) {
            Path classesDir = targetDir.resolve("classes");
            if (Files.exists(classesDir)) {
                try {
                    Files.createDirectories(targetDir);
                    String javaHome = System.getProperty("java.home");
                    String jarExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (isWindows() ? "jar.exe" : "jar") : "jar";
                    dockerExecutor.executeCommand(workingDir, 30, jarExe, "-cf", "target/app.jar", "-C", "target/classes", ".");
                    jarExists = findJarFile(targetDir).isPresent();
                } catch (Exception ignored) {}
            }
        }

        BuildStatus buildStatus = jarExists ? BuildStatus.SUCCESS : BuildStatus.FAILED;
        ApplicationStatus appStatus = ApplicationStatus.STARTED;

        int evalPort = 19080 + Math.abs(assessmentId.hashCode() % 500);
        String evalTag = "evidence-eval-" + assessmentId.toString().substring(0, 8) + ":eval";
        String evalContainer = "evidence-eval-" + assessmentId.toString().substring(0, 8);

        Process evalProcess = null;
        boolean dockerAvailable = isDockerDaemonRunning();

        // 2. Launch Container / Process for Black-Box HTTP Testing
        if (jarExists) {
            if (dockerAvailable) {
                dockerExecutor.executeCommand(workingDir, 120, "docker", "build", "-t", evalTag, ".");
                dockerExecutor.executeCommand(workingDir, 30, "docker", "run", "-d", "--name", evalContainer, "-p", evalPort + ":8080", evalTag);
            } else {
                Optional<Path> jarPath = findJarFile(targetDir);
                if (jarPath.isPresent()) {
                    try {
                        String javaHome = System.getProperty("java.home");
                        String javaExe = javaHome != null ? javaHome + File.separator + "bin" + File.separator + (isWindows() ? "java.exe" : "java") : "java";
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

        // 6. Calculate Weighted Score & Summary Counts
        int totalTests = testCases.size();
        int passedTests = 0;
        int failedTests = 0;
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal passedWeight = BigDecimal.ZERO;

        for (TestResult tr : testResults) {
            TestCase tc = tr.getTestCase();
            BigDecimal weight = tc.getWeight() != null ? tc.getWeight() : BigDecimal.ONE;
            totalWeight = totalWeight.add(weight);

            if (tr.getStatus() == TestResultStatus.PASSED) {
                passedTests++;
                passedWeight = passedWeight.add(weight);
            } else {
                failedTests++;
            }
        }

        BigDecimal finalScore = BigDecimal.ZERO;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            finalScore = passedWeight.divide(totalWeight, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

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
     * Phase C.5: Safe Candidate Result View.
     */
    @Transactional(readOnly = true)
    public CandidateResultResponse getCandidateResult(UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (candidateId != null && assessment.getCandidate() != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("Candidate is not authorized for this assessment");
        }

        Submission submission = submissionRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for assessment: " + assessmentId));

        EvaluationReport report = evaluationReportRepository.findBySubmissionId(submission.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation report not yet generated"));

        return CandidateResultResponse.builder()
                .assessmentId(assessmentId)
                .score(report.getScore())
                .status(assessment.getStatus())
                .totalTests(report.getTotalTests())
                .passedTests(report.getPassedTests())
                .failedTests(report.getFailedTests())
                .buildStatus(report.getBuildStatus())
                .applicationStatus(report.getApplicationStatus())
                .timeTakenSeconds(report.getTimeTakenSeconds())
                .evaluatedAt(report.getEvaluatedAt())
                .build();
    }

    /**
     * Phase D.2: Recruiter Detailed Report View.
     */
    @Transactional(readOnly = true)
    public RecruiterReportResponse getRecruiterReport(UUID recruiterId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (recruiterId != null && assessment.getWorkspace() != null && assessment.getWorkspace().getRecruiter() != null
                && !assessment.getWorkspace().getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("Recruiter is not authorized for this assessment");
        }

        Submission submission = submissionRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for assessment: " + assessmentId));

        EvaluationReport report = evaluationReportRepository.findBySubmissionId(submission.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation report not found"));

        CandidateSummaryDto candidateDto = null;
        if (assessment.getCandidate() != null) {
            candidateDto = CandidateSummaryDto.builder()
                    .id(assessment.getCandidate().getId())
                    .name(assessment.getCandidate().getName())
                    .email(assessment.getCandidate().getEmail())
                    .build();
        }

        return RecruiterReportResponse.builder()
                .assessmentId(assessmentId)
                .candidate(candidateDto)
                .score(report.getScore())
                .totalTests(report.getTotalTests())
                .passedTests(report.getPassedTests())
                .failedTests(report.getFailedTests())
                .buildStatus(report.getBuildStatus())
                .applicationStatus(report.getApplicationStatus())
                .timeTakenSeconds(report.getTimeTakenSeconds())
                .status(assessment.getStatus())
                .evaluatedAt(report.getEvaluatedAt())
                .build();
    }

    /**
     * Phase D.3: Recruiter Granular Test Results Breakdown.
     */
    @Transactional(readOnly = true)
    public List<RecruiterTestResultItemDto> getRecruiterTestResults(UUID recruiterId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));

        if (recruiterId != null && assessment.getWorkspace() != null && assessment.getWorkspace().getRecruiter() != null
                && !assessment.getWorkspace().getRecruiter().getId().equals(recruiterId)) {
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
                .executionTimeMs(tr.getExecutionTimeMs())
                .weight(tr.getTestCase().getWeight())
                .failureReason(tr.getFailureReason())
                .build()
        ).collect(Collectors.toList());
    }

    private void copyDirectorySimple(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new java.nio.file.SimpleFileVisitor<>() {
            @Override
            public java.nio.file.FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Path targetDir = destination.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @Override
            public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Path targetFile = destination.resolve(source.relativize(file));
                Files.copy(file, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
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

    private boolean isDockerDaemonRunning() {
        DockerCommandExecutor.ProcessResult result = dockerExecutor.executeCommand(null, 5, "docker", "info");
        return result.isSuccess();
    }

    private Optional<Path> findJarFile(Path targetDir) {
        if (!Files.exists(targetDir)) return Optional.empty();
        try (Stream<Path> stream = Files.list(targetDir)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".jar") && !p.getFileName().toString().startsWith("original-"))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private long DurationBetween(Instant start, Instant end) {
        return java.time.Duration.between(start, end).getSeconds();
    }
}
