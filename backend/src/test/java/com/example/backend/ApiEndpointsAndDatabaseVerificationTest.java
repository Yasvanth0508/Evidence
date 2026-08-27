package com.example.backend;

import com.example.backend.assessment.dto.workspace.CreateFileRequest;
import com.example.backend.assessment.dto.workspace.RenameFileRequest;
import com.example.backend.assessment.dto.workspace.SaveFileRequest;
import com.example.backend.assessment.entity.*;
import com.example.backend.assessment.repository.*;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.enums.Difficulty;
import com.example.backend.common.enums.Role;
import com.example.backend.common.enums.TestType;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.WorkspaceCandidateRepository;
import com.example.backend.workspace.repository.WorkspaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ApiEndpointsAndDatabaseVerificationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceCandidateRepository workspaceCandidateRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentWorkspaceRepository assessmentWorkspaceRepository;

    @Autowired
    private RepositoryAnalysisRepository repositoryAnalysisRepository;

    @Autowired
    private FeatureSpecificationRepository featureSpecificationRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private EvaluationReportRepository evaluationReportRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    private User recruiter;
    private User candidate;
    private Workspace testWorkspace;
    private Assessment testAssessment;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Find or create recruiter
        recruiter = userRepository.findByEmail("recruiter@example.com").orElseGet(() ->
                userRepository.save(new User("Test Recruiter", "recruiter@example.com", "password", Role.RECRUITER))
        );

        // Find or create candidate
        candidate = userRepository.findByEmail("rahul@example.com").orElseGet(() ->
                userRepository.save(new User("Rahul Kumar", "rahul@example.com", "password", Role.CANDIDATE))
        );

        testWorkspace = workspaceRepository.findAllByRecruiterId(recruiter.getId()).stream().findFirst().orElseGet(() -> {
            Workspace ws = Workspace.builder()
                    .name("Enterprise Java Hiring")
                    .description("Test workspace")
                    .recruiter(recruiter)
                    .build();
            return workspaceRepository.save(ws);
        });

        testAssessment = assessmentRepository.findAllByCandidateId(candidate.getId()).stream().findFirst().orElseGet(() -> {
            Assessment a = Assessment.builder()
                    .workspace(testWorkspace)
                    .candidate(candidate)
                    .repositoryUrl("https://github.com/scanurag/FoodFrenzy.git")
                    .branchName("master")
                    .backendRootDirectory("")
                    .difficulty(Difficulty.INTERMEDIATE)
                    .durationMinutes(90)
                    .scheduledStartAt(Instant.now().minus(10, ChronoUnit.MINUTES))
                    .scheduledEndAt(Instant.now().plus(4, ChronoUnit.HOURS))
                    .status(AssessmentStatus.READY)
                    .build();
            return assessmentRepository.save(a);
        });

        testAssessment.setScheduledStartAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        testAssessment.setScheduledEndAt(Instant.now().plus(4, ChronoUnit.HOURS));
        testAssessment = assessmentRepository.save(testAssessment);
    }

    @Test
    @DisplayName("1. Recruiter & Candidate Dashboard Endpoints")
    void testDashboardEndpoints() throws Exception {
        // Recruiter Dashboard
        mockMvc.perform(get("/api/v1/recruiter/dashboard")
                        .header("X-Recruiter-Id", recruiter.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workspaceCount").exists())
                .andExpect(jsonPath("$.data.candidateCount").exists())
                .andExpect(jsonPath("$.data.assessmentCount").exists());

        // Candidate Dashboard
        mockMvc.perform(get("/api/v1/candidate/dashboard")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduledAssessments").isArray())
                .andExpect(jsonPath("$.data.completedAssessments").isArray());
    }

    @Test
    @DisplayName("2. Workspace Lifecycle & Candidate Assessment Creation")
    void testWorkspaceAndAssessmentCreation() throws Exception {
        // Create Workspace
        String createJson = """
                {
                  "name": "Fullstack Java Workspace",
                  "description": "Evaluating Java Candidates"
                }
                """;
        MvcResult createResult = mockMvc.perform(post("/api/v1/workspaces")
                        .header("X-Recruiter-Id", recruiter.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Fullstack Java Workspace"))
                .andReturn();

        String workspaceIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asText();
        UUID workspaceId = UUID.fromString(workspaceIdStr);

        // Add candidate to workspace
        String addCandJson = """
                {
                  "email": "rahul@example.com"
                }
                """;
        mockMvc.perform(post("/api/v1/workspaces/" + workspaceId + "/candidates")
                        .header("X-Recruiter-Id", recruiter.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addCandJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Create Assessment
        String assessJson = String.format("""
                {
                  "candidateId": "%s",
                  "repositoryUrl": "https://github.com/scanurag/FoodFrenzy.git",
                  "branchName": "master",
                  "backendRootDirectory": "",
                  "difficulty": "INTERMEDIATE",
                  "durationMinutes": 90,
                  "scheduledStartAt": "%s",
                  "scheduledEndAt": "%s"
                }
                """,
                candidate.getId(),
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(2, ChronoUnit.HOURS)
        );

        MvcResult assessResult = mockMvc.perform(post("/api/v1/workspaces/" + workspaceId + "/assessments")
                        .header("X-Recruiter-Id", recruiter.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assessJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String assessmentIdStr = objectMapper.readTree(assessResult.getResponse().getContentAsString())
                .path("data").path("assessmentId").asText();
        UUID assessmentId = UUID.fromString(assessmentIdStr);

        assertThat(assessmentRepository.findById(assessmentId)).isPresent();
    }

    @Test
    @DisplayName("3. Phase A: Candidate Workspace Isolation & Real-Time File Explorer IDE")
    void testPhaseACandidateWorkspaceFileExplorer() throws Exception {
        UUID assessmentId = testAssessment.getId();

        // 3.1 Start Assessment & Initialize Candidate Workspace
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/start")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.workspacePath").exists());

        // Verify Assessment & AssessmentWorkspace in DB
        Assessment dbAssessment = assessmentRepository.findById(assessmentId).orElseThrow();
        assertThat(dbAssessment.getStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);

        AssessmentWorkspace dbWs = assessmentWorkspaceRepository.findByAssessmentId(assessmentId).orElse(null);
        assertThat(dbWs).isNotNull();
        assertThat(dbWs.getCandidateWorkspacePath()).isNotEmpty();

        // 3.2 Fetch Workspace Directory Tree
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/files")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("root"))
                .andExpect(jsonPath("$.data.children").isArray());

        // 3.3 Read File Content
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/files/content?path=pom.xml")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.language").value("xml"))
                .andExpect(jsonPath("$.data.content").exists());

        // 3.4 Save / Debounced Autosave File Content
        SaveFileRequest saveReq = new SaveFileRequest("pom.xml", "<project><!-- autosaved --></project>");
        mockMvc.perform(put("/api/v1/assessments/" + assessmentId + "/files/content")
                        .header("X-Candidate-Id", candidate.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("File saved successfully"));

        // 3.5 Create New File
        CreateFileRequest createReq = new CreateFileRequest("src/main/java/com/example/TestNote.java", "FILE", "package com.example;\npublic class TestNote {}");
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/files")
                        .header("X-Candidate-Id", candidate.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 3.6 Rename File
        RenameFileRequest renameReq = new RenameFileRequest("src/main/java/com/example/TestNote.java", "src/main/java/com/example/RenamedNote.java");
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/files/rename")
                        .header("X-Candidate-Id", candidate.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renameReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3.7 Delete File
        mockMvc.perform(delete("/api/v1/assessments/" + assessmentId + "/files?path=src/main/java/com/example/RenamedNote.java")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("4. Phase B: Sandbox Execution, Logs, Status and Stop")
    void testPhaseBSandboxExecution() throws Exception {
        UUID assessmentId = testAssessment.getId();

        // 4.1 Trigger Run
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/run")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.executionId").exists())
                .andExpect(jsonPath("$.data.port").isNumber());

        // 4.2 Check Execution Status
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/execution/status")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.buildStatus").exists())
                .andExpect(jsonPath("$.data.containerStatus").exists());

        // 4.3 Check Execution Logs
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/execution/logs")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.logs").exists());

        // 4.4 Stop Execution
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/stop")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("STOPPED"));
    }

    @Test
    @DisplayName("5. Phase C & D: Final Submission, Evaluation, Scoring, and Recruiter Reports")
    void testPhaseCEvaluationAndReports() throws Exception {
        UUID assessmentId = testAssessment.getId();

        // Ensure at least one test case exists for assessment
        if (testCaseRepository.countByAssessmentId(assessmentId) == 0) {
            TestCase tc = TestCase.builder()
                    .assessment(testAssessment)
                    .testCaseNumber(1)
                    .testType(TestType.BUSINESS_LOGIC)
                    .httpMethod("GET")
                    .endpoint("/actuator/health")
                    .expectedStatusCode(200)
                    .assertions("[]")
                    .weight(BigDecimal.ONE)
                    .build();
            testCaseRepository.save(tc);
        }

        // 5.1 Submit Assessment
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/submit")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // Verify Assessment & Submission & EvaluationReport in DB
        Assessment evaluatedAssessment = assessmentRepository.findById(assessmentId).orElseThrow();
        assertThat(evaluatedAssessment.getStatus()).isEqualTo(AssessmentStatus.COMPLETED);

        Submission dbSubmission = submissionRepository.findByAssessmentId(assessmentId).orElse(null);
        assertThat(dbSubmission).isNotNull();

        EvaluationReport dbReport = evaluationReportRepository.findBySubmissionId(dbSubmission.getId()).orElse(null);
        assertThat(dbReport).isNotNull();
        assertThat(dbReport.getScore()).isNotNull();
        assertThat(dbReport).isNotNull();
        assertThat(dbReport.getTotalTests()).isGreaterThanOrEqualTo(1);

        // 5.2 Safe Candidate Result View
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/result")
                        .header("X-Candidate-Id", candidate.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").exists())
                .andExpect(jsonPath("$.data.totalTests").isNumber())
                .andExpect(jsonPath("$.data.passedTests").isNumber())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        // 5.3 Recruiter Detailed Report View
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/report")
                        .header("X-Recruiter-Id", recruiter.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").exists())
                .andExpect(jsonPath("$.data.candidate.email").value("rahul@example.com"));

        // 5.4 Recruiter Granular Test Results View
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/test-results")
                        .header("X-Recruiter-Id", recruiter.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 5.5 Recruiter Paginated Workspace Reports
        mockMvc.perform(get("/api/v1/reports?workspaceId=" + testWorkspace.getId() + "&page=0&size=10")
                        .header("X-Recruiter-Id", recruiter.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }
}
