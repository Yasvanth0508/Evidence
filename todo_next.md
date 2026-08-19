# Evidence Backend — Complete Implementation Roadmap

> **Generated:** 2026-08-19  
> **Context:** Modules 0–10 endpoints are built and verified (44/44 tests passing). Core AI pipeline, Docker execution, evaluation engine, file system management, and authentication are still **dummy/mock stubs**. This document provides the step-by-step roadmap to make every module production-real.

---

## Current State Summary

| Layer | Status | Details |
|:---|:---:|:---|
| CRUD Endpoints (Modules 1, 2, 3, 9, 10) | ✅ Real | Full DB-backed CRUD, cascade deletes, real aggregations |
| Repository Analysis (Module 4) | 🟡 Dummy | Hardcoded project structure & status |
| Feature Specification (Module 5) | 🟡 Dummy | Hardcoded feature spec JSON |
| File Explorer (Module 6) | 🟡 Dummy | Hardcoded file tree & content, no disk I/O |
| Application Execution (Module 7) | 🟡 Dummy | Saves Execution rows but fake container IDs, no Docker |
| Evaluation & Results (Module 8) | 🟡 Dummy | Hardcoded 5 test results, score always 85.00 |
| Assessment Submit (Module 3) | 🟡 Partial | No real Submission row created, no evaluation triggered |
| Processing Pipeline (Module 3) | 🟡 Dummy | Hardcoded 4-stage "all COMPLETED" response |
| Authentication (Module 11) | ❌ Missing | `permitAll()`, hardcoded user emails, no JWT |

---

## Phase 1: Authentication & Security Foundation

> **Why first?** Every subsequent phase needs `SecurityContext` to resolve the authenticated user. Currently all services use hardcoded `recruiter@example.com` / `rahul@example.com` fallbacks.

### Step 1.1 — Add JWT Dependencies

- [ ] Add to `pom.xml`:
  - `io.jsonwebtoken:jjwt-api:0.12.6`
  - `io.jsonwebtoken:jjwt-impl:0.12.6` (runtime)
  - `io.jsonwebtoken:jjwt-jackson:0.12.6` (runtime)
- [ ] Add `application.properties` JWT config:
  - `jwt.secret` (256-bit Base64 key)
  - `jwt.expiration-ms` (e.g., 86400000 = 24h)

### Step 1.2 — JWT Token Service

- [ ] Create `com.example.backend.auth.service.JwtService`
  - `generateToken(User user) → String` — Claims: `sub=userId`, `email`, `role`, `iat`, `exp`
  - `extractUserId(String token) → UUID`
  - `extractRole(String token) → Role`
  - `validateToken(String token) → boolean`

### Step 1.3 — JWT Authentication Filter

- [ ] Create `com.example.backend.auth.filter.JwtAuthenticationFilter extends OncePerRequestFilter`
  - Extract `Authorization: Bearer <token>` header
  - Validate token, load `User` from DB
  - Set `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`
  - Skip filter for `/api/v1/auth/**` paths

### Step 1.4 — Auth DTOs

- [ ] Create request/response DTOs:
  - `SignupRequest` — `name`, `email`, `password`
  - `LoginRequest` — `email`, `password`
  - `AuthResponse` — `token`, `userId`, `name`, `email`, `role`

### Step 1.5 — Auth Controllers

- [ ] Create `RecruiterAuthController` (`/api/v1/auth/recruiter`)
  - `POST /signup` — Register new recruiter, return JWT
  - `POST /login` — Authenticate recruiter, return JWT
  - `POST /logout` — Client-side token discard (stateless)
  - `GET /me` — Return current recruiter profile from JWT
- [ ] Create `CandidateAuthController` (`/api/v1/auth/candidate`)
  - `POST /signup` — Register new candidate, return JWT
  - `POST /login` — Authenticate candidate, return JWT
  - `POST /logout` — Client-side token discard
  - `GET /me` — Return current candidate profile from JWT

### Step 1.6 — Update SecurityConfig

- [ ] Update `SecurityConfig.java`:
  - Register `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
  - Permit: `/api/v1/auth/**`
  - Require authentication for all other routes
  - Add `BCryptPasswordEncoder` bean

### Step 1.7 — Auth Utility Helper

- [ ] Create `com.example.backend.auth.util.AuthUtil`
  - `getCurrentUserId() → UUID` — Extract from `SecurityContextHolder`
  - `getCurrentUserRole() → Role`
  - `requireRecruiter()` — Throws `ForbiddenException` if not RECRUITER
  - `requireCandidate()` — Throws `ForbiddenException` if not CANDIDATE

### Step 1.8 — Refactor All Services to Use AuthUtil

- [ ] Replace **every** hardcoded `recruiter@example.com` / `rahul@example.com` lookup with `AuthUtil.getCurrentUserId()`
- [ ] Affected services:
  - `WorkspaceController` / `WorkspaceService`
  - `AssessmentController` / `AssessmentService`
  - `CandidateController` / `CandidateService`
  - `RecruiterDashboardService`
  - `CandidateDashboardService`
  - `ReportService`
  - `SelectedCandidateService`
  - `EvaluationController`
  - `ExecutionController`
  - `FileExplorerController`
  - `FeatureController`
  - `RepositoryAnalysisController`

### Step 1.9 — Role-Based Endpoint Authorization

- [ ] Add `@PreAuthorize` or manual `AuthUtil.requireRecruiter()` / `AuthUtil.requireCandidate()` checks:
  - **Recruiter-only:** Workspace CRUD, candidate search/add, assessment create/update/cancel, reports, selected-candidates, recruiter dashboard, assessment report/test-results
  - **Candidate-only:** Start assessment, submit assessment, file explorer, run/stop, candidate result, candidate dashboard
  - **Assessment ownership:** Verify `assessment.candidateId == currentUserId` for candidate endpoints, verify workspace ownership for recruiter endpoints

### Step 1.10 — Update DataInitializer

- [ ] Keep seeded users for development but ensure passwords are BCrypt-hashed (already done)
- [ ] Make seed data conditional on profile (`@Profile("dev")`)

---

## Phase 2: Repository Cloning & Workspace Provisioning

> **Why?** This is the first stage of the AI pipeline. Everything else (analysis, feature gen, test gen, file explorer, execution) depends on having real cloned files on disk.

### Step 2.1 — Git Clone Service

- [ ] Create `com.example.backend.git.service.GitCloneService`
  - `cloneRepository(String repoUrl, String branch, Path targetDir) → Path`
  - Uses JGit library (`org.eclipse.jgit`) or Process-based `git clone --depth 1 --branch <branch> <url> <targetDir>`
  - Shallow clone for performance
  - Error handling: invalid URL, private repo, branch not found, timeout
- [ ] Add JGit dependency to `pom.xml`:
  - `org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r`

### Step 2.2 — Workspace Directory Manager

- [ ] Create `com.example.backend.workspace.service.WorkspaceDirectoryService`
  - `provisionWorkspace(UUID assessmentId, String repoUrl, String branch) → WorkspacePaths`
    - Creates directory structure: `<base>/assessments/<assessmentId>/original/` and `<base>/assessments/<assessmentId>/candidate/`
    - Clones into `original/`
    - Deep-copies `original/` → `candidate/` (candidate's editable copy)
  - `getOriginalPath(UUID assessmentId) → Path`
  - `getCandidatePath(UUID assessmentId) → Path`
  - `cleanupWorkspace(UUID assessmentId)` — Deletes workspace directory on assessment deletion
- [ ] Add `application.properties` config:
  - `evidence.workspace.base-dir=/data/workspaces` (Docker volume mount)

### Step 2.3 — Persist Repository Record

- [ ] Update clone flow to save `RepositoryRecord` entity:
  - `original_repository_path` = absolute path to cloned original
  - `candidate_repository_path` = absolute path to candidate copy
  - Linked via `assessment_id`

---

## Phase 3: Repository Analysis Engine (Replace Dummy Module 4)

> **Why?** The analysis output feeds into AI feature generation and provides the candidate with project context.

### Step 3.1 — Java Source Code Parser

- [ ] Create `com.example.backend.analysis.service.JavaParserService`
  - Uses JavaParser library (`com.github.javaparser:javaparser-core:3.26.3`) or regex-based extraction
  - Methods:
    - `extractControllers(Path projectRoot) → List<ControllerInfo>`
      - Scan for `@RestController` / `@Controller` classes
      - Extract class name, base path (`@RequestMapping`)
    - `extractEndpoints(List<ControllerInfo>) → List<EndpointInfo>`
      - Scan handler methods (`@GetMapping`, `@PostMapping`, etc.)
      - Extract HTTP method, path, path variables, query params, request body type, return type
    - `extractServices(Path projectRoot) → List<ServiceInfo>`
      - Scan `@Service` classes, method signatures
    - `extractRepositories(Path projectRoot) → List<RepositoryInfo>`
      - Scan `JpaRepository` / `CrudRepository` interfaces, entity bindings, custom query methods
    - `extractEntities(Path projectRoot) → List<EntityInfo>`
      - Scan `@Entity` classes, `@Table` names, fields, types, relationships
    - `extractProjectStructure(Path projectRoot) → ProjectStructureDto`
      - Directory tree, file listing, identify `backendRootDirectory` (location of `pom.xml` or `build.gradle`)
- [ ] Add JavaParser dependency to `pom.xml`:
  - `com.github.javaparser:javaparser-core:3.26.3`

### Step 3.2 — Update RepositoryAnalysisService (Replace Dummy)

- [ ] Rewrite `RepositoryAnalysisService`:
  - `analyzeRepository(UUID assessmentId)`:
    1. Get `RepositoryRecord.originalRepositoryPath` from DB
    2. Identify backend root directory
    3. Run all JavaParser extractions
    4. Persist results as JSON in `RepositoryAnalysis` entity
    5. Update `RepositoryAnalysis.analysisStatus` = `COMPLETED`
  - `getAnalysis(UUID assessmentId)` → Read from DB (no more hardcoded data)
  - `getAnalysisStatus(UUID assessmentId)` → Read real status from DB

---

## Phase 4: AI Integration — Feature & Test Generation (Replace Dummy Modules 5 & 8)

> **Why?** This is the core intelligence of Evidence — generating project-specific features and hidden test suites.

### Step 4.1 — LLM Client Service

- [ ] Create `com.example.backend.ai.service.LlmClientService`
  - Abstraction layer over LLM API (Gemini / Claude / OpenAI)
  - `generateCompletion(String systemPrompt, String userPrompt) → String`
  - `generateStructuredOutput(String systemPrompt, String userPrompt, Class<T> schema) → T`
  - Configurable via `application.properties`:
    - `evidence.ai.provider` = `gemini` | `openai` | `claude`
    - `evidence.ai.api-key`
    - `evidence.ai.model` (e.g., `gemini-2.5-flash`)
    - `evidence.ai.temperature` = `0.7`
    - `evidence.ai.max-tokens` = `4096`
  - Rate limiting, retry with exponential backoff, timeout handling
- [ ] Add HTTP client dependency (Spring WebClient or OkHttp)

### Step 4.2 — Feature Generation Service (Replace Dummy Module 5)

- [ ] Create `com.example.backend.ai.service.FeatureGenerationService`
  - `generateFeature(UUID assessmentId) → FeatureSpecification`
    1. Load `RepositoryAnalysis` JSON from DB
    2. Load assessment `difficulty` level
    3. Construct LLM prompt:
       - System prompt: "You are an expert Spring Boot assessor..."
       - User prompt: Include extracted controllers, endpoints, entities, services + difficulty level
       - Output schema: `{ title, description, requirements[], endpoint, httpMethod, requestSpecification, responseSpecification, constraints[] }`
    4. Parse LLM JSON response
    5. Validate schema completeness
    6. Persist `FeatureSpecification` entity to DB
- [ ] Update `FeatureSpecificationService.getFeature()` to read from DB instead of returning hardcoded data

### Step 4.3 — Hidden Test Case Generation Service

- [ ] Create `com.example.backend.ai.service.TestGenerationService`
  - `generateTestCases(UUID assessmentId) → List<TestCase>`
    1. Load `FeatureSpecification` from DB
    2. Load `RepositoryAnalysis` for context
    3. Construct LLM prompt:
       - System prompt: "Generate black-box HTTP test cases for the following feature..."
       - Include: feature endpoint, method, request/response specs, constraints
       - Require: happy path tests, edge cases, error handling, boundary conditions
       - Output: Array of `{ testCaseNumber, httpMethod, endpoint, requestData, expectedStatusCode, expectedResponse, assertions }`
    4. Parse and validate test case array
    5. Persist each `TestCase` entity to DB with `UNIQUE(assessment_id, test_case_number)`
  - Generate 5–10 test cases based on difficulty:
    - `EASY`: 5 tests (mostly happy path)
    - `INTERMEDIATE`: 7 tests (happy path + edge cases)
    - `DIFFICULT`: 10 tests (happy path + edge cases + error handling + constraints)

### Step 4.4 — Prompt Templates

- [ ] Create `src/main/resources/prompts/` directory:
  - `feature_generation_system.txt` — System prompt for feature generation
  - `feature_generation_user.txt` — Template with `{{controllers}}`, `{{entities}}`, `{{endpoints}}`, `{{difficulty}}`
  - `test_generation_system.txt` — System prompt for test generation
  - `test_generation_user.txt` — Template with `{{feature}}`, `{{constraints}}`, `{{difficulty}}`
- [ ] Design prompts to enforce JSON output format and prevent hallucination

---

## Phase 5: Asynchronous Assessment Pipeline Orchestrator

> **Why?** When a recruiter creates an assessment, the backend must asynchronously run: Clone → Analyze → Generate Feature → Generate Tests → Mark READY. Currently this is entirely missing.

### Step 5.1 — Async Pipeline Service

- [ ] Create `com.example.backend.pipeline.service.AssessmentPipelineService`
  - `@Async` method: `processAssessment(UUID assessmentId)`
    1. Update status → `CREATING`
    2. **Clone Stage:** Call `WorkspaceDirectoryService.provisionWorkspace()`, update status → `ANALYZING`
    3. **Analysis Stage:** Call `RepositoryAnalysisService.analyzeRepository()`, update status → `GENERATING_FEATURE`
    4. **Feature Gen Stage:** Call `FeatureGenerationService.generateFeature()`, update status → `GENERATING_TESTS`
    5. **Test Gen Stage:** Call `TestGenerationService.generateTestCases()`, update status → `READY` (then `SCHEDULED` if within window)
    6. **Error Handling:** On any exception, update status → `FAILED`, log error details
  - Each stage updates a `pipeline_stages` tracking structure for `GET /processing-status`

### Step 5.2 — Enable Spring Async

- [ ] Create `com.example.backend.config.AsyncConfig`
  - `@EnableAsync`
  - Configure `ThreadPoolTaskExecutor` with bounded queue and thread pool
  - Error handler for uncaught async exceptions

### Step 5.3 — Pipeline Status Tracking

- [ ] Create `com.example.backend.pipeline.entity.PipelineStage` or use JSON column on `Assessment`
  - Track per-stage: `stageName`, `status` (`PENDING` | `IN_PROGRESS` | `COMPLETED` | `FAILED`), `startedAt`, `completedAt`, `errorMessage`
- [ ] Update `AssessmentService.getProcessingStatus()` to read real stage data from DB (replace dummy)

### Step 5.4 — Wire Pipeline to Assessment Creation

- [ ] In `AssessmentService.createAssessment()`:
  - After persisting the assessment, call `assessmentPipelineService.processAssessment(assessmentId)` asynchronously
  - Return `201 Created` immediately with assessment ID and `CREATING` status

---

## Phase 6: Real File System Management (Replace Dummy Module 6)

> **Why?** Candidates need to read and edit real files in their workspace via the Monaco editor.

### Step 6.1 — Rewrite FileExplorerService (Replace Dummy)

- [ ] Rewrite `FileExplorerService`:
  - `getFileTree(UUID assessmentId) → List<FileNodeDto>`
    1. Get `candidateRepositoryPath` from `RepositoryRecord`
    2. Recursively walk directory using `java.nio.file.Files.walkFileTree()`
    3. Filter out: `.git/`, `target/`, `build/`, `.idea/`, `node_modules/`, binary files
    4. Build hierarchical `FileNodeDto` tree (name, type, path, children)
  - `getFileContent(UUID assessmentId, String relativePath) → FileContentResponse`
    1. **Path Traversal Protection:**
       - Resolve `candidateRepositoryPath.resolve(relativePath).normalize()`
       - Verify resolved path starts with `candidateRepositoryPath` (prevent `../` escape)
       - Reject absolute paths, symlinks outside workspace
    2. Read file content as UTF-8 string
    3. Return path, content, file size, last modified timestamp
  - `saveFile(UUID assessmentId, SaveFileRequest request) → SaveFileResponse`
    1. Same path traversal validation
    2. Verify assessment status is `IN_PROGRESS`
    3. Write content to file on disk
    4. Return saved path and timestamp

### Step 6.2 — File Size & Binary Detection

- [ ] Add guards:
  - Max file size for reading: 1MB (reject large binaries)
  - Detect binary files by extension or content inspection
  - Return `400 VALIDATION_ERROR` for unsupported files

---

## Phase 7: Docker Execution Engine (Replace Dummy Module 7)

> **Why?** This is the sandbox where candidate code is built and run. Critical for security (untrusted code execution) and for the evaluation engine.

### Step 7.1 — Docker Client Integration

- [ ] Add Docker Java client dependency to `pom.xml`:
  - `com.github.docker-java:docker-java-core:3.4.1`
  - `com.github.docker-java:docker-java-transport-httpclient5:3.4.1`
- [ ] Create `com.example.backend.docker.service.DockerClientService`
  - Initialize `DockerClient` connected to Docker daemon socket
  - Configuration in `application.properties`:
    - `evidence.docker.host` = `unix:///var/run/docker.sock` or `tcp://localhost:2375`
    - `evidence.docker.image` = `evidence-candidate-runner:latest` (pre-built base image with JDK + Maven)
    - `evidence.docker.cpu-limit` = `1.0`
    - `evidence.docker.memory-limit` = `1g`
    - `evidence.docker.network` = `evidence-sandbox`
    - `evidence.docker.execution-timeout-seconds` = `300`

### Step 7.2 — Candidate Runner Base Image

- [ ] Create `docker/candidate-runner/Dockerfile`:
  ```dockerfile
  FROM eclipse-temurin:21-jdk-alpine
  RUN apk add --no-cache maven git
  WORKDIR /workspace
  ENTRYPOINT ["/bin/sh"]
  ```
- [ ] Build and tag: `docker build -t evidence-candidate-runner:latest .`

### Step 7.3 — Container Lifecycle Manager

- [ ] Create `com.example.backend.docker.service.ContainerExecutionService`
  - `buildAndRun(UUID assessmentId) → ExecutionResult`:
    1. Get `candidateRepositoryPath` from DB
    2. Create Docker container:
       - Image: `evidence-candidate-runner:latest`
       - Bind mount: `candidateRepositoryPath:/workspace`
       - CPU/memory limits from config
       - Network: isolated bridge or `none` (prevent outbound)
       - Read-only root FS with tmpfs for `/tmp`
    3. Start container
    4. Execute build command inside container:
       - `mvn clean package -DskipTests -q` (or detect Gradle)
       - Capture build stdout/stderr
       - Determine `build_status`: `SUCCESS` if exit code 0, `FAILED` otherwise
    5. If build succeeded, execute Spring Boot app:
       - `java -jar target/*.jar --server.port=8080`
       - Poll for startup (health check or log pattern: `Started .* in .* seconds`)
       - Determine `application_status`: `STARTED` or `FAILED`
    6. Save `Execution` entity with real `container_id`, statuses, timestamps
    7. Return execution result
  - `stopContainer(UUID assessmentId)`:
    1. Find running container from `Execution` record
    2. `dockerClient.stopContainerCmd(containerId).exec()`
    3. `dockerClient.removeContainerCmd(containerId).exec()`
    4. Update `Execution`: `container_status = STOPPED`, `stopped_at = now`
  - `getContainerLogs(UUID assessmentId) → String`:
    1. Get container ID from latest `Execution`
    2. `dockerClient.logContainerCmd(containerId).withStdOut(true).withStdErr(true)`
    3. Return combined log output
  - `getContainerStatus(UUID assessmentId) → ExecutionStatusResponse`:
    1. Inspect container state via Docker API
    2. Map to `build_status`, `container_status`, `application_status`

### Step 7.4 — Rewrite ExecutionService (Replace Dummy)

- [ ] Rewrite `ExecutionService` to delegate to `ContainerExecutionService`
- [ ] Replace hardcoded container IDs and statuses with real Docker data
- [ ] Replace hardcoded logs with real container log streaming

### Step 7.5 — Execution Timeout & Cleanup

- [ ] Implement timeout enforcement:
  - If container runs longer than `execution-timeout-seconds`, force kill
  - If assessment time window expires (`scheduledEndAt`), force stop container
- [ ] Implement orphan container cleanup:
  - Scheduled task to find and kill containers from expired/cancelled assessments

---

## Phase 8: Evaluation Engine (Replace Dummy Module 8)

> **Why?** This is the scoring engine that runs hidden test cases against the candidate's running application.

### Step 8.1 — HTTP Test Runner

- [ ] Create `com.example.backend.evaluation.service.TestRunnerService`
  - `runTestSuite(UUID assessmentId) → EvaluationResult`:
    1. Ensure candidate's container is running and healthy
    2. Determine container's internal IP/port (or mapped host port)
    3. Load all `TestCase` records for this assessment from DB
    4. For each test case (ordered by `test_case_number`):
       a. Build HTTP request: method, URL (`http://<container>:<port>` + endpoint), headers, body (`request_data`)
       b. Send request using `RestTemplate` / `WebClient`
       c. Record: `actual_status_code`, `actual_response`, `execution_time_ms`
       d. Compare:
          - `actual_status_code == expected_status_code`
          - `actual_response` matches `expected_response` (JSON deep comparison or schema match)
          - All `assertions` pass (JSONPath checks, array length, field values)
       e. Determine `status`: `PASSED` or `FAILED`
       f. If failed, build `failure_reason` string describing mismatch
       g. Persist `TestResult` entity to DB
    5. Calculate score: `(passedCount / totalCount) * 100.0`
    6. Update `Assessment.score` and `Assessment.status = COMPLETED`
    7. Stop and cleanup the container

### Step 8.2 — JSON Assertion Engine

- [ ] Create `com.example.backend.evaluation.service.AssertionEngine`
  - `compareStatusCode(int actual, int expected) → boolean`
  - `compareResponseBody(JsonNode actual, JsonNode expected) → ComparisonResult`
    - Support exact match, partial match (subset), and structural match
  - `evaluateAssertions(JsonNode actualResponse, List<Assertion> assertions) → List<AssertionResult>`
    - JSONPath evaluation (e.g., `$.data.length() == 3`, `$.data[0].name == "Note 1"`)
    - Field existence checks
    - Type checks
  - Use Jackson for JSON processing, Jayway JsonPath for path evaluation
- [ ] Add JsonPath dependency:
  - `com.jayway.jsonpath:json-path:2.9.0`

### Step 8.3 — Rewrite EvaluationService (Replace Dummy)

- [ ] Rewrite `EvaluationService`:
  - `getCandidateResult(UUID assessmentId)` → Read real score, count real `TestResult` rows (PASSED vs FAILED)
  - `getAssessmentReport(UUID assessmentId)` → Read real data: score, build status from `Execution`, candidate info, time taken
  - `getTestResults(UUID assessmentId)` → Query real `TestResult` + `TestCase` joins, return per-test breakdown

---

## Phase 9: Real Submission Flow

> **Why?** Currently `AssessmentService.submitAssessment()` assigns a fake score of 85.00 and doesn't create a `Submission` record or trigger evaluation. This needs to be the trigger for the full evaluation pipeline.

### Step 9.1 — Rewrite submitAssessment

- [ ] Update `AssessmentService.submitAssessment()`:
  1. Validate: assessment belongs to candidate, status is `IN_PROGRESS`, within time window
  2. Transition status → `EVALUATING`
  3. Lock file editor (status check in `FileExplorerService.saveFile()`)
  4. Create real `Submission` entity:
     - `candidate_repository_path` = path to candidate's modified files
     - `submitted_at` = now
     - `status` = `EVALUATING`
  5. Trigger evaluation pipeline asynchronously:
     - Build & start candidate's application in Docker container
     - Wait for application healthy
     - Run hidden test suite via `TestRunnerService`
     - Calculate score, persist results
     - Update assessment status → `COMPLETED`
  6. Return submission ID immediately

### Step 9.2 — Post-Submission Lockdown

- [ ] Add status guards across all candidate-facing endpoints:
  - `FileExplorerService.saveFile()` → Reject if status != `IN_PROGRESS`
  - `ExecutionService.runApplication()` → Reject if status != `IN_PROGRESS`
  - `ExecutionService.stopApplication()` → Reject if status != `IN_PROGRESS`
  - Return `409 ASSESSMENT_ALREADY_SUBMITTED` for locked assessments

---

## Phase 10: Background Workers & Scheduled Tasks

### Step 10.1 — Assessment Expiration Worker

- [ ] Create `com.example.backend.scheduler.AssessmentExpirationScheduler`
  - `@Scheduled(fixedRate = 60000)` — Runs every 1 minute
  - Query: Find assessments where `status = SCHEDULED` AND `scheduled_end_at < NOW()`
  - Transition each to `EXPIRED`

### Step 10.2 — Auto-Submit on Time Expiry

- [ ] Create `com.example.backend.scheduler.AutoSubmitScheduler`
  - Query: Find assessments where `status = IN_PROGRESS` AND `scheduled_end_at < NOW()`
  - Auto-submit each (trigger evaluation with whatever code state exists)

### Step 10.3 — Orphan Container Cleanup

- [ ] Create `com.example.backend.scheduler.ContainerCleanupScheduler`
  - `@Scheduled(fixedRate = 300000)` — Every 5 minutes
  - List all Docker containers with `evidence-` label
  - Kill containers whose assessment is `COMPLETED`, `CANCELLED`, `EXPIRED`, or `FAILED`

---

## Phase 11: Data Model Refinements

### Step 11.1 — Assessment Entity Enhancements

- [ ] Add missing fields to `Assessment` entity:
  - `started_at` TIMESTAMP — When candidate actually started
  - `submitted_at` TIMESTAMP — When candidate submitted
  - `time_taken_seconds` LONG — Calculated duration
- [ ] Add full status enum values (verify all 12 exist):
  - `CREATING`, `ANALYZING`, `GENERATING_FEATURE`, `GENERATING_TESTS`, `READY`, `SCHEDULED`, `IN_PROGRESS`, `EVALUATING`, `COMPLETED`, `CANCELLED`, `FAILED`, `EXPIRED`

### Step 11.2 — Execution Entity Enhancements

- [ ] Add `submission_id` FK (nullable) to `Execution` entity
- [ ] Add `assessment_id` index for query performance

### Step 11.3 — TestResult Entity Enhancements

- [ ] Ensure `TestResult` has proper FK to `TestCase` (not just `assessment_id`)
- [ ] Add `execution_id` FK to link results to specific execution run

### Step 11.4 — Repository Entity Corrections

- [ ] Verify `RepositoryRecord` entity matches DB spec:
  - `assessment_id` (UNIQUE)
  - `original_repository_path`
  - `candidate_repository_path`

---

## Phase 12: Error Handling, Validation & Security Hardening

### Step 12.1 — Path Traversal Protection

- [ ] In `FileExplorerService`:
  - Canonicalize all paths with `.toRealPath()` or `.normalize()`
  - Reject paths containing `..`, absolute paths, or symlinks escaping workspace root
  - Return `403 FORBIDDEN` on traversal attempt

### Step 12.2 — Assessment Status Guards

- [ ] Add comprehensive status transition validation:
  - `start` only from `SCHEDULED` / `READY`
  - `submit` only from `IN_PROGRESS`
  - `cancel` only from non-terminal states (not `COMPLETED`, `EVALUATING`)
  - `run/stop` only during `IN_PROGRESS`
  - `files/edit` only during `IN_PROGRESS`

### Step 12.3 — Input Validation

- [ ] Add `@Valid` + Jakarta Bean Validation annotations on all request DTOs
- [ ] Validate:
  - Repository URL format (HTTPS or SSH Git URL pattern)
  - Branch name format (no special chars)
  - `scheduledEndAt > scheduledStartAt`
  - `durationMinutes > 0`
  - File paths don't contain null bytes

### Step 12.4 — Docker Security

- [ ] Container sandboxing:
  - CPU limits: `--cpus=1.0`
  - Memory limits: `--memory=1g --memory-swap=1g`
  - No network egress (or restricted to assessment container network only)
  - Read-only root filesystem with ephemeral `/tmp` tmpfs
  - No privileged mode, drop all capabilities
  - Execution timeout enforcement (kill after N seconds)

### Step 12.5 — Data Confidentiality

- [ ] Never expose in API responses:
  - `original_repository_path`, `candidate_repository_path` (server-internal paths)
  - `TestCase.expected_response`, `TestCase.assertions` (hidden from candidate)
  - `User.password`
- [ ] Candidate `/result` endpoint: Only score + pass/fail counts (no test details)
- [ ] Recruiter `/report` + `/test-results`: Full details allowed

---

## Phase 13: Testing & Verification

### Step 13.1 — Update Automated Test Suite

- [ ] Update `test_api.ps1` with authentication flow:
  - Register/login to get JWT tokens
  - Pass `Authorization: Bearer <token>` headers in all requests
  - Test role-based access (recruiter can't access candidate endpoints and vice versa)
- [ ] Add tests for real pipeline:
  - Create assessment with real GitHub repo URL
  - Poll processing status until `READY`
  - Start, edit files, run, submit
  - Verify real score and test results

### Step 13.2 — Integration Tests

- [ ] Write JUnit integration tests with `@SpringBootTest` + Testcontainers:
  - PostgreSQL test container
  - Docker-in-Docker for execution tests
  - End-to-end pipeline: create → clone → analyze → generate → evaluate

### Step 13.3 — Security Tests

- [ ] Test JWT validation (expired tokens, malformed tokens, wrong role)
- [ ] Test path traversal attacks on file explorer
- [ ] Test cross-tenant access (recruiter A accessing recruiter B's workspace)

---

## Dependency Graph & Recommended Execution Order

```
Phase 1 (Auth)
    │
    ├──► Phase 2 (Git Clone & Workspace)
    │         │
    │         ├──► Phase 3 (Repo Analysis Engine)
    │         │         │
    │         │         ├──► Phase 4 (AI Feature + Test Gen)
    │         │         │         │
    │         │         │         ├──► Phase 5 (Async Pipeline Orchestrator)
    │         │         │         │
    │         │         │         └──► Phase 8 (Evaluation Engine)
    │         │         │                   │
    │         │         │                   └──► Phase 9 (Real Submission Flow)
    │         │         │
    │         │         └──► Phase 6 (Real File Explorer)
    │         │
    │         └──► Phase 7 (Docker Execution Engine)
    │
    ├──► Phase 10 (Background Workers)
    │
    ├──► Phase 11 (Data Model Refinements)
    │
    └──► Phase 12 (Security Hardening)
              │
              └──► Phase 13 (Testing)
```

### Suggested Sprint Plan

| Sprint | Phases | Estimated Effort | Focus |
|:---:|:---|:---:|:---|
| **Sprint 1** | Phase 1 (Auth) | 2–3 days | JWT, login/signup, refactor all services |
| **Sprint 2** | Phase 2 + 3 | 2–3 days | Git clone, workspace provisioning, Java parser |
| **Sprint 3** | Phase 4 + 5 | 3–4 days | LLM integration, prompt engineering, async pipeline |
| **Sprint 4** | Phase 6 + 7 | 3–4 days | Real file I/O, Docker execution engine |
| **Sprint 5** | Phase 8 + 9 | 2–3 days | Test runner, assertion engine, real submission |
| **Sprint 6** | Phase 10 + 11 + 12 | 2–3 days | Background workers, data model fixes, security |
| **Sprint 7** | Phase 13 | 2–3 days | Comprehensive testing and verification |

---

## New Dependencies Summary

| Dependency | Version | Purpose |
|:---|:---|:---|
| `io.jsonwebtoken:jjwt-api` | `0.12.6` | JWT token generation & validation |
| `io.jsonwebtoken:jjwt-impl` | `0.12.6` | JWT implementation (runtime) |
| `io.jsonwebtoken:jjwt-jackson` | `0.12.6` | JWT JSON serialization (runtime) |
| `org.eclipse.jgit:org.eclipse.jgit` | `7.1.0` | Git repository cloning |
| `com.github.javaparser:javaparser-core` | `3.26.3` | Java source code AST parsing |
| `com.github.docker-java:docker-java-core` | `3.4.1` | Docker daemon API client |
| `com.github.docker-java:docker-java-transport-httpclient5` | `3.4.1` | Docker HTTP transport |
| `com.jayway.jsonpath:json-path` | `2.9.0` | JSONPath assertion evaluation |

---

## New Package Structure (To Be Created)

```
com.example.backend
├── ai/
│   └── service/
│       ├── LlmClientService.java          ← LLM API abstraction
│       ├── FeatureGenerationService.java   ← AI feature spec generator
│       └── TestGenerationService.java      ← AI hidden test generator
├── auth/
│   ├── controller/
│   │   ├── RecruiterAuthController.java    ← Recruiter auth endpoints
│   │   └── CandidateAuthController.java    ← Candidate auth endpoints
│   ├── dto/
│   │   ├── SignupRequest.java
│   │   ├── LoginRequest.java
│   │   └── AuthResponse.java
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java    ← JWT request filter
│   ├── service/
│   │   └── JwtService.java                 ← Token generation/validation
│   └── util/
│       └── AuthUtil.java                   ← SecurityContext helper
├── docker/
│   └── service/
│       ├── DockerClientService.java        ← Docker daemon connection
│       └── ContainerExecutionService.java  ← Container lifecycle manager
├── git/
│   └── service/
│       └── GitCloneService.java            ← Repository cloning
├── pipeline/
│   └── service/
│       └── AssessmentPipelineService.java  ← Async pipeline orchestrator
├── scheduler/
│   ├── AssessmentExpirationScheduler.java  ← Expire overdue assessments
│   ├── AutoSubmitScheduler.java            ← Auto-submit on time expiry
│   └── ContainerCleanupScheduler.java      ← Kill orphan containers
└── config/
    └── AsyncConfig.java                    ← Spring @Async configuration
```
