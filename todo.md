# Evidence Backend — Development TODO

> **Reference Docs:** [`Docs/`](file:///d:/Evidence_Development/Docs)  
> **Backend Path:** [`backend/`](file:///d:/Evidence_Development/backend)  
> **Stack:** Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL  
> **Base API URL:** `/api/v1`

> [!NOTE]
> AI Processing Pipeline is **not finalized**. Those endpoints are **dummy/stubs**.  
> Build **one module at a time**, test its endpoints, then move to the next.

> [!IMPORTANT]
> **Auth is built LAST.** During development, Spring Security is configured to **permit all requests**.  
> This lets you test every endpoint with plain curl/Postman — no tokens, no 401s.  
> The `User` entity is created early (Module 0) since Workspace needs it for candidate lookup.

---

## Final Folder Structure (Feature-Based)

```
com.example.backend/
│
├── BackendApplication.java
│
├── common/                              ← Shared across all modules
│   ├── entity/
│   │   └── BaseEntity.java              ← @MappedSuperclass (id, createdAt, updatedAt)
│   ├── dto/
│   │   ├── ApiResponse.java             ← Generic success wrapper
│   │   └── ApiErrorResponse.java        ← Error wrapper
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   ├── DuplicateResourceException.java
│   │   ├── ValidationException.java
│   │   ├── UnauthorizedException.java
│   │   ├── ForbiddenException.java
│   │   ├── AssessmentNotAvailableException.java
│   │   ├── AssessmentAlreadySubmittedException.java
│   │   ├── AiProcessingException.java
│   │   ├── ExecutionUnavailableException.java
│   │   └── GlobalExceptionHandler.java  ← @RestControllerAdvice
│   └── enums/
│       ├── Role.java
│       ├── AssessmentStatus.java
│       ├── Difficulty.java
│       ├── WorkspaceStatus.java
│       ├── BuildStatus.java
│       ├── ContainerStatus.java
│       ├── ApplicationStatus.java
│       ├── TestResultStatus.java
│       ├── SubmissionStatus.java
│       └── AnalysisStatus.java
│
├── config/                              ← App-wide configuration
│   ├── SecurityConfig.java              ← permitAll() during dev → locked down in Module 11
│   ├── CorsConfig.java
│   └── AppConfig.java                   ← BCryptPasswordEncoder bean, etc.
│
├── auth/                                ← Authentication module (BUILT LAST — Module 11)
│   ├── entity/
│   │   └── User.java                    ← Created early in Module 0 (other modules depend on it)
│   ├── repository/
│   │   └── UserRepository.java          ← Created early in Module 0
│   ├── dto/
│   │   ├── SignupRequest.java
│   │   ├── LoginRequest.java
│   │   └── AuthResponse.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── CustomUserDetailsService.java
│   ├── controller/
│   │   ├── RecruiterAuthController.java
│   │   └── CandidateAuthController.java
│   └── util/
│       └── AuthUtil.java                ← Get current user from SecurityContext
│
├── workspace/                           ← Workspace management module
│   ├── entity/
│   │   ├── Workspace.java
│   │   └── WorkspaceCandidate.java
│   ├── repository/
│   │   ├── WorkspaceRepository.java
│   │   └── WorkspaceCandidateRepository.java
│   ├── dto/
│   │   ├── CreateWorkspaceRequest.java
│   │   ├── UpdateWorkspaceRequest.java
│   │   ├── AddCandidateToWorkspaceRequest.java
│   │   └── WorkspaceResponse.java
│   ├── service/
│   │   └── WorkspaceService.java
│   └── controller/
│       └── WorkspaceController.java
│
├── candidate/                           ← Candidate management module (recruiter-side)
│   ├── dto/
│   │   ├── CandidateResponse.java
│   │   ├── CandidateWorkspaceDto.java
│   │   └── CandidateAssessmentDto.java
│   ├── service/
│   │   └── CandidateService.java
│   └── controller/
│       └── CandidateController.java
│
├── assessment/                          ← Assessment CRUD & lifecycle module
│   ├── entity/
│   │   └── Assessment.java
│   ├── repository/
│   │   └── AssessmentRepository.java
│   ├── dto/
│   │   ├── CreateAssessmentRequest.java
│   │   ├── AssessmentResponse.java
│   │   ├── AssessmentListItemResponse.java
│   │   ├── ProcessingStatusResponse.java
│   │   └── ProcessingStageDto.java
│   ├── service/
│   │   └── AssessmentService.java
│   └── controller/
│       └── AssessmentController.java
│
├── analysis/                            ← Repository analysis module (DUMMY)
│   ├── entity/
│   │   ├── RepositoryAnalysis.java
│   │   └── RepositoryRecord.java
│   ├── repository/
│   │   ├── RepositoryAnalysisRepository.java
│   │   └── RepositoryRecordRepository.java
│   ├── dto/
│   │   └── RepositoryAnalysisResponse.java
│   ├── service/
│   │   └── RepositoryAnalysisService.java  ← DUMMY: returns mock data
│   └── controller/
│       └── RepositoryAnalysisController.java
│
├── feature/                             ← Feature specification module (DUMMY)
│   ├── entity/
│   │   └── FeatureSpecification.java
│   ├── repository/
│   │   └── FeatureSpecificationRepository.java
│   ├── dto/
│   │   └── FeatureSpecificationResponse.java
│   ├── service/
│   │   └── FeatureSpecificationService.java  ← DUMMY: returns mock data
│   └── controller/
│       └── FeatureController.java
│
├── fileexplorer/                        ← File explorer module (DUMMY)
│   ├── dto/
│   │   ├── FileTreeResponse.java
│   │   ├── FileContentResponse.java
│   │   └── SaveFileRequest.java
│   ├── service/
│   │   └── FileExplorerService.java     ← DUMMY: returns mock file tree/content
│   └── controller/
│       └── FileExplorerController.java
│
├── execution/                           ← Application execution module (DUMMY)
│   ├── entity/
│   │   └── Execution.java
│   ├── repository/
│   │   └── ExecutionRepository.java
│   ├── dto/
│   │   ├── ExecutionResponse.java
│   │   ├── ExecutionStatusResponse.java
│   │   └── ExecutionLogsResponse.java
│   ├── service/
│   │   └── ExecutionService.java        ← DUMMY: returns mock status/logs
│   └── controller/
│       └── ExecutionController.java
│
├── evaluation/                          ← Evaluation, submission & test results (DUMMY)
│   ├── entity/
│   │   ├── Submission.java
│   │   ├── TestCase.java
│   │   └── TestResult.java
│   ├── repository/
│   │   ├── SubmissionRepository.java
│   │   ├── TestCaseRepository.java
│   │   └── TestResultRepository.java
│   ├── dto/
│   │   ├── SubmissionResponse.java
│   │   ├── CandidateResultResponse.java
│   │   ├── AssessmentReportResponse.java
│   │   └── TestResultResponse.java
│   ├── service/
│   │   └── EvaluationService.java       ← DUMMY: returns mock scores/results
│   └── controller/
│       └── EvaluationController.java
│
├── dashboard/                           ← Dashboard module (recruiter + candidate)
│   ├── dto/
│   │   ├── RecruiterDashboardResponse.java
│   │   ├── CandidateDashboardResponse.java
│   │   ├── ScheduledAssessmentDto.java
│   │   └── CompletedAssessmentDto.java
│   ├── service/
│   │   ├── RecruiterDashboardService.java
│   │   └── CandidateDashboardService.java
│   └── controller/
│       ├── RecruiterDashboardController.java
│       └── CandidateDashboardController.java
│
├── report/                              ← Reports module
│   ├── dto/
│   │   ├── ReportListResponse.java
│   │   └── ReportSummaryResponse.java
│   ├── service/
│   │   └── ReportService.java
│   └── controller/
│       └── ReportController.java
│
└── selectedcandidate/                   ← Selected candidates module
    ├── dto/
    │   ├── SelectedCandidateListResponse.java
    │   └── SelectedCandidateDetailResponse.java
    ├── service/
    │   └── SelectedCandidateService.java
    └── controller/
        └── SelectedCandidateController.java
```

---

## Module-by-Module Implementation Plan

---

### Module 0: Common Foundation

> Shared pieces every module depends on — enums, base entity, User entity, response wrappers, exceptions, and a **wide-open** security config for dev.

- [x] **0.1 — Maven Dependencies**
  - Add to [`pom.xml`](file:///d:/Evidence_Development/backend/pom.xml):
    - `spring-boot-starter-security`
    - `spring-boot-starter-validation`
    - `lombok`
    - JWT library (`io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`) — added now, wired in Module 11
  - Existing: `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `postgresql` ✅

- [x] **0.2 — `application.properties`**
  - Add `server.port=8080`
  - Existing PostgreSQL config ✅

- [x] **0.3 — Enums** — Create all 10 enum classes in `common/enums/`

  | Enum | Values |
  |:---|:---|
  | `Role` | `RECRUITER`, `CANDIDATE` |
  | `AssessmentStatus` | `CREATING`, `ANALYZING`, `GENERATING_FEATURE`, `GENERATING_TESTS`, `READY`, `SCHEDULED`, `IN_PROGRESS`, `EVALUATING`, `COMPLETED`, `CANCELLED`, `FAILED`, `EXPIRED` |
  | `Difficulty` | `EASY`, `INTERMEDIATE`, `DIFFICULT` |
  | `WorkspaceStatus` | `ACTIVE` |
  | `BuildStatus` | `SUCCESS`, `FAILED` |
  | `ContainerStatus` | `RUNNING`, `STOPPED` |
  | `ApplicationStatus` | `STARTED`, `FAILED` |
  | `TestResultStatus` | `PASSED`, `FAILED` |
  | `SubmissionStatus` | `EVALUATING`, `COMPLETED`, `FAILED` |
  | `AnalysisStatus` | `PENDING`, `RUNNING`, `COMPLETED`, `FAILED` |

- [x] **0.4 — `BaseEntity`** — `common/entity/BaseEntity.java`
  - `@MappedSuperclass`
  - Fields: `id` (UUID, `@GeneratedValue`), `createdAt` (TIMESTAMP), `updatedAt` (TIMESTAMP)
  - `@PrePersist` sets both; `@PreUpdate` sets `updatedAt`

- [x] **0.5 — `User` entity** — `auth/entity/User.java` *(created early — other modules need it)*
  - Extends `BaseEntity`
  - Fields: `name` (VARCHAR 150), `email` (VARCHAR 255, UNIQUE), `password` (VARCHAR 255), `role` (Role enum)

- [x] **0.6 — `UserRepository`** — `auth/repository/UserRepository.java` *(created early)*
  - `findByEmail(String email)` → `Optional<User>`
  - `findByEmailAndRole(String email, Role role)` → `Optional<User>`
  - `existsByEmail(String email)` → `boolean`

- [x] **0.7 — Response Wrappers** — `common/dto/`
  - `ApiResponse<T>`: `success`, `message`, `data`, `timestamp`
  - `ApiErrorResponse`: `success` (false), `message`, `errorCode`, `timestamp`

- [x] **0.8 — Custom Exceptions** — `common/exception/`
  - `ResourceNotFoundException` (404), `DuplicateResourceException` (409), `ValidationException` (400), `UnauthorizedException` (401), `ForbiddenException` (403), `AssessmentNotAvailableException` (409), `AssessmentAlreadySubmittedException` (409), `AiProcessingException` (502), `ExecutionUnavailableException` (503)

- [x] **0.9 — `GlobalExceptionHandler`** — `common/exception/GlobalExceptionHandler.java`
  - `@RestControllerAdvice`
  - Map each exception → `ApiErrorResponse` with HTTP status + `errorCode`
  - `MethodArgumentNotValidException` → 400 `VALIDATION_ERROR`
  - Generic `Exception` → 500 `INTERNAL_ERROR`

- [x] **0.10 — `AppConfig`** — `config/AppConfig.java`
  - `@Bean BCryptPasswordEncoder passwordEncoder()`

- [x] **0.11 — `CorsConfig`** — `config/CorsConfig.java`
  - Allow origins: `http://localhost`, `http://localhost:5173`
  - Allow methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
  - Allow credentials: `true`

- [x] **0.12 — `SecurityConfig` (DEV MODE — PERMIT ALL)** — `config/SecurityConfig.java`
  ```java
  @Configuration
  public class SecurityConfig {
      @Bean
      public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
          http
              .csrf(csrf -> csrf.disable())
              .cors(Customizer.withDefaults())
              .authorizeHttpRequests(auth -> auth
                  .anyRequest().permitAll()  // ← wide open during dev
              );
          return http.build();
      }
  }
  ```

#### ✅ Checkpoint: `mvn clean compile` passes, app starts, no endpoints yet

---

### Module 1: Workspace

> Workspace CRUD + adding/removing candidates to workspaces.

- [x] **1.1 — `Workspace` entity** — `workspace/entity/Workspace.java`
  - Extends `BaseEntity`
  - Fields: `name` (VARCHAR 200), `description` (TEXT, nullable), `status` (WorkspaceStatus, default `ACTIVE`)
  - `@ManyToOne` → `User` (recruiter via `recruiter_id`)

- [x] **1.2 — `WorkspaceCandidate` entity** — `workspace/entity/WorkspaceCandidate.java`
  - Extends `BaseEntity`
  - `@ManyToOne` → `Workspace`, `@ManyToOne` → `User` (candidate)
  - `@UniqueConstraint(workspace_id, candidate_id)`

- [x] **1.3 — Repositories** — `workspace/repository/`
  - `WorkspaceRepository`:
    - `findAllByRecruiterId(UUID)` → `List<Workspace>`
    - `findByIdAndRecruiterId(UUID id, UUID recruiterId)` → `Optional<Workspace>`
  - `WorkspaceCandidateRepository`:
    - `findAllByWorkspaceId(UUID)` → `List<WorkspaceCandidate>`
    - `findByWorkspaceIdAndCandidateId(UUID, UUID)` → `Optional<WorkspaceCandidate>`
    - `existsByWorkspaceIdAndCandidateId(UUID, UUID)` → `boolean`
    - `findAllByCandidateId(UUID)` → `List<WorkspaceCandidate>`

- [x] **1.4 — DTOs** — `workspace/dto/`
  - `CreateWorkspaceRequest`: `name` (`@NotBlank`), `description`
  - `UpdateWorkspaceRequest`: `name`, `description`
  - `AddCandidateToWorkspaceRequest`: `email` (`@NotBlank`)
  - `WorkspaceResponse`: `id`, `name`, `description`, `status`

- [x] **1.5 — `WorkspaceService`** — `workspace/service/WorkspaceService.java`
  - `createWorkspace(UUID recruiterId, CreateWorkspaceRequest)`
  - `getWorkspaces(UUID recruiterId)` → `List<WorkspaceResponse>`
  - `getWorkspaceById(UUID recruiterId, UUID workspaceId)` → verify ownership
  - `updateWorkspace(UUID recruiterId, UUID workspaceId, UpdateWorkspaceRequest)`
  - `deleteWorkspace(UUID recruiterId, UUID workspaceId)`
  - `getCandidatesInWorkspace(UUID recruiterId, UUID workspaceId)`
  - `addCandidateToWorkspace(UUID recruiterId, UUID workspaceId, String email)` → find by email via `UserRepository`, check not duplicate, create `WorkspaceCandidate`
  - `removeCandidateFromWorkspace(UUID recruiterId, UUID workspaceId, UUID candidateId)`

> [!NOTE]
> **No auth yet** — the controller will accept `recruiterId` as a path param or hardcode a test UUID for now.  
> In Module 11, you'll replace this with `AuthUtil.getCurrentUserId()`.

- [x] **1.6 — `WorkspaceController`** — `workspace/controller/WorkspaceController.java`
  - `POST   /api/v1/workspaces` → create → 201
  - `GET    /api/v1/workspaces` → list → 200
  - `GET    /api/v1/workspaces/{workspaceId}` → get → 200
  - `PUT    /api/v1/workspaces/{workspaceId}` → update → 200
  - `DELETE /api/v1/workspaces/{workspaceId}` → delete → 200
  - `GET    /api/v1/workspaces/{workspaceId}/candidates` → list candidates → 200
  - `POST   /api/v1/workspaces/{workspaceId}/candidates` → add candidate → 201
  - `DELETE /api/v1/workspaces/{workspaceId}/candidates/{candidateId}` → remove → 200

#### ✅ Checkpoint: Test with curl (no auth needed!)
```
# Insert a test recruiter user directly in DB or via a temp endpoint
POST   /workspaces                              → 201 workspace created
GET    /workspaces                              → 200 list
PUT    /workspaces/{id}                         → 200 updated
POST   /workspaces/{id}/candidates (email)      → 201 candidate added
GET    /workspaces/{id}/candidates              → 200 candidate list
DELETE /workspaces/{id}/candidates/{candidateId} → 200 removed
DELETE /workspaces/{id}                         → 200 deleted
```

---

### Module 2: Candidate Management (Recruiter-Side)

> Recruiter searches candidates by email, views their workspaces and assessments.

- [x] **2.1 — DTOs** — `candidate/dto/`
  - `CandidateResponse`: `id`, `name`, `email`, `role`
  - `CandidateWorkspaceDto`: `workspaceId`, `workspaceName`
  - `CandidateAssessmentDto`: `assessmentId`, `difficulty`, `scheduledStartAt`, `scheduledEndAt`, `status`, `score`

- [x] **2.2 — `CandidateService`** — `candidate/service/CandidateService.java`
  - `searchByEmail(String email)` → find candidate user by email, 404 if not found
  - `getCandidateById(UUID candidateId)` → return `CandidateResponse`
  - `getCandidateWorkspaces(UUID candidateId)` → via `WorkspaceCandidateRepository`
  - `getCandidateAssessments(UUID candidateId)` → via `AssessmentRepository` (empty until Module 3)

- [x] **2.3 — `CandidateController`** — `candidate/controller/CandidateController.java`
  - `GET /api/v1/candidates/search?email={email}` → 200 / 404
  - `GET /api/v1/candidates/{candidateId}` → 200
  - `GET /api/v1/candidates/{candidateId}/workspaces` → 200
  - `GET /api/v1/candidates/{candidateId}/assessments` → 200

#### ✅ Checkpoint: Test with curl
```
GET /candidates/search?email=test@example.com → 200 found / 404 not found
GET /candidates/{id}                          → 200 details
GET /candidates/{id}/workspaces               → 200 list
```

---

### Module 3: Assessment

> Assessment creation, listing, update, cancel, start, submit, processing status.

- [x] **3.1 — `Assessment` entity** — `assessment/entity/Assessment.java`
  - Extends `BaseEntity`
  - Fields: `repositoryUrl`, `branchName`, `backendRootDirectory`, `difficulty` (Difficulty), `durationMinutes` (INTEGER), `scheduledStartAt`, `scheduledEndAt`, `status` (AssessmentStatus, default `CREATING`), `score` (DECIMAL 5,2, nullable)
  - `@ManyToOne` → `Workspace` (workspace_id), `@ManyToOne` → `User` (candidate_id)
  - **No `exam_id`**

- [x] **3.2 — `AssessmentRepository`** — `assessment/repository/AssessmentRepository.java`
  - `findAllByWorkspaceId(UUID)` → `List<Assessment>`
  - `findAllByCandidateId(UUID)` → `List<Assessment>`
  - `findByIdAndCandidateId(UUID, UUID)` → `Optional<Assessment>`
  - Count queries for dashboard use later

- [x] **3.3 — DTOs** — `assessment/dto/`
  - `CreateAssessmentRequest`: `candidateId`, `repositoryUrl`, `branchName`, `backendRootDirectory`, `difficulty`, `durationMinutes`, `scheduledStartAt`, `scheduledEndAt` — all validated
  - `AssessmentResponse`: all fields + candidate name
  - `AssessmentListItemResponse`: summary for list views
  - `ProcessingStatusResponse`: `assessmentId`, `status`, `stages` list
  - `ProcessingStageDto`: `name`, `status`

- [x] **3.4 — `AssessmentService`** — `assessment/service/AssessmentService.java`
  - `createAssessment(UUID recruiterId, UUID workspaceId, CreateAssessmentRequest)` → validate workspace ownership, validate candidate in workspace, save with `CREATING`
  - `getAssessmentsByWorkspace(UUID recruiterId, UUID workspaceId)` → verify ownership
  - `getAssessmentById(UUID userId, Role role, UUID assessmentId)` → role-based field filtering
  - `updateAssessment(UUID recruiterId, UUID assessmentId, ...)` → only in certain statuses
  - `cancelAssessment(UUID recruiterId, UUID assessmentId)` → set `CANCELLED`
  - `getProcessingStatus(UUID recruiterId, UUID assessmentId)` → **🟡 DUMMY: hardcoded stages all COMPLETED**
  - `startAssessment(UUID candidateId, UUID assessmentId)` → validate time window `[scheduledStartAt, scheduledEndAt)`, set `IN_PROGRESS`
  - `submitAssessment(UUID candidateId, UUID assessmentId)` → validate not submitted, set `EVALUATING`

- [x] **3.5 — `AssessmentController`** — `assessment/controller/AssessmentController.java`
  - `POST /api/v1/workspaces/{workspaceId}/assessments` → create → 201
  - `GET  /api/v1/workspaces/{workspaceId}/assessments` → list → 200
  - `GET  /api/v1/assessments/{assessmentId}` → get → 200
  - `PUT  /api/v1/assessments/{assessmentId}` → update → 200
  - `POST /api/v1/assessments/{assessmentId}/cancel` → 200
  - `GET  /api/v1/assessments/{assessmentId}/processing-status` → 🟡 dummy → 200
  - `POST /api/v1/assessments/{assessmentId}/start` → 200
  - `POST /api/v1/assessments/{assessmentId}/submit` → 200

#### ✅ Checkpoint: Test with curl
```
POST /workspaces/{id}/assessments            → 201 created
GET  /workspaces/{id}/assessments            → 200 list
GET  /assessments/{id}                       → 200 details
POST /assessments/{id}/cancel                → 200 cancelled
GET  /assessments/{id}/processing-status     → 200 dummy stages
POST /assessments/{id}/start                 → 200 IN_PROGRESS
POST /assessments/{id}/submit                → 200 EVALUATING
```

---

### Module 4: Repository Analysis (DUMMY)

> Stub endpoints returning mock analysis data.

- [x] **4.1 — Entities** — `analysis/entity/`
  - `RepositoryAnalysis`: extends `BaseEntity`, `@OneToOne` → Assessment (UNIQUE), `analysisStatus`, JSON fields
  - `RepositoryRecord`: extends `BaseEntity`, `@OneToOne` → Assessment (UNIQUE), `originalRepositoryPath`, `candidateRepositoryPath`

- [x] **4.2 — Repositories** — `analysis/repository/`
  - `RepositoryAnalysisRepository`: `findByAssessmentId(UUID)`
  - `RepositoryRecordRepository`: `findByAssessmentId(UUID)`

- [x] **4.3 — DTO** — `analysis/dto/RepositoryAnalysisResponse.java`

- [x] **4.4 — `RepositoryAnalysisService`** — 🟡 DUMMY: returns hardcoded mock data

- [x] **4.5 — `RepositoryAnalysisController`**
  - `GET /api/v1/assessments/{assessmentId}/repository-analysis` → 🟡 dummy → 200
  - `GET /api/v1/assessments/{assessmentId}/repository-analysis/status` → 🟡 dummy → 200

#### ✅ Checkpoint: Both return mock JSON

---

### Module 5: Feature Specification (DUMMY)

- [x] **5.1 — Entity** — `feature/entity/FeatureSpecification.java`
  - Extends `BaseEntity`, `@OneToOne` → Assessment (UNIQUE)
  - Fields: `title`, `description`, `requirements` (JSON), `endpoint`, `httpMethod`, `requestSpecification` (JSON), `responseSpecification` (JSON), `constraints` (JSON)

- [x] **5.2 — Repository** — `feature/repository/FeatureSpecificationRepository.java`

- [x] **5.3 — DTO** — `feature/dto/FeatureSpecificationResponse.java`

- [x] **5.4 — `FeatureSpecificationService`** — 🟡 DUMMY: returns hardcoded mock feature

- [x] **5.5 — `FeatureController`**
  - `GET /api/v1/assessments/{assessmentId}/feature` → 🟡 dummy → 200

#### ✅ Checkpoint: Returns mock feature JSON

---

### Module 6: File Explorer (DUMMY)

> No entities — just DTOs, stub service, and controller.

- [x] **6.1 — DTOs** — `fileexplorer/dto/`
  - `FileTreeResponse`, `FileContentResponse`, `SaveFileRequest`

- [x] **6.2 — `FileExplorerService`** — 🟡 DUMMY: mock file tree, content, save

- [x] **6.3 — `FileExplorerController`**
  - `GET  /api/v1/assessments/{assessmentId}/files` → 🟡 dummy → 200
  - `GET  /api/v1/assessments/{assessmentId}/files/content?path=` → 🟡 dummy → 200
  - `PUT  /api/v1/assessments/{assessmentId}/files/content` → 🟡 dummy → 200

#### ✅ Checkpoint: All 3 return mock responses

---

### Module 7: Execution (DUMMY)

- [x] **7.1 — Entity** — `execution/entity/Execution.java`
  - Extends `BaseEntity`, `@ManyToOne` → Assessment, `@ManyToOne` → Submission (nullable)
  - Fields: `containerId`, `buildStatus`, `containerStatus`, `applicationStatus`, `startedAt`, `stoppedAt`

- [x] **7.2 — Repository** — `execution/repository/ExecutionRepository.java`

- [x] **7.3 — DTOs** — `execution/dto/`

- [x] **7.4 — `ExecutionService`** — 🟡 DUMMY: mock run/stop/status/logs

- [x] **7.5 — `ExecutionController`**
  - `POST /api/v1/assessments/{assessmentId}/run` → 🟡 dummy → 200
  - `POST /api/v1/assessments/{assessmentId}/stop` → 🟡 dummy → 200
  - `GET  /api/v1/assessments/{assessmentId}/execution/status` → 🟡 dummy → 200
  - `GET  /api/v1/assessments/{assessmentId}/execution/logs` → 🟡 dummy → 200

#### ✅ Checkpoint: All 4 return mock responses

---

### Module 8: Evaluation & Results (DUMMY)

- [x] **8.1 — Entities** — `evaluation/entity/`
  - `Submission`: `@ManyToOne` → Assessment, `candidateRepositoryPath`, `submittedAt`, `status`
  - `TestCase`: `@ManyToOne` → Assessment, `testCaseNumber`, `httpMethod`, `endpoint`, `requestData`, `expectedStatusCode`, `expectedResponse`, `assertions`. Unique: `(assessment_id, test_case_number)`
  - `TestResult`: `@ManyToOne` → TestCase, `@ManyToOne` → Execution, `status`, `actualStatusCode`, `actualResponse`, `executionTimeMs`, `failureReason`

- [x] **8.2 — Repositories** — `evaluation/repository/`
  - `SubmissionRepository`, `TestCaseRepository`, `TestResultRepository`

- [x] **8.3 — DTOs** — `evaluation/dto/`
  - `SubmissionResponse`, `CandidateResultResponse`, `AssessmentReportResponse`, `TestResultResponse`

- [x] **8.4 — `EvaluationService`** — 🟡 DUMMY: mock scores/results

- [x] **8.5 — `EvaluationController`**
  - `GET /api/v1/assessments/{assessmentId}/result` → candidate → 🟡 dummy → 200
  - `GET /api/v1/assessments/{assessmentId}/report` → recruiter → 🟡 dummy → 200
  - `GET /api/v1/assessments/{assessmentId}/test-results` → recruiter → 🟡 dummy → 200

#### ✅ Checkpoint: All 3 return mock data

---

### Module 9: Dashboard

> Real data — aggregates from workspace, assessment, and candidate tables.

- [x] **9.1 — DTOs** — `dashboard/dto/`
  - `RecruiterDashboardResponse`: `workspaceCount`, `candidateCount`, `assessmentCount`, `activeAssessments`, `completedAssessments`
  - `CandidateDashboardResponse`: `scheduledAssessments` list, `completedAssessments` list
  - `ScheduledAssessmentDto`: `assessmentId`, `workspaceName`, `scheduledStartAt`, `scheduledEndAt`, `difficulty`, `status`
  - `CompletedAssessmentDto`: `assessmentId`, `workspaceName`, `completedAt`, `score`, `status`

- [x] **9.2 — `RecruiterDashboardService`** — count workspaces, candidates, assessments

- [x] **9.3 — `CandidateDashboardService`** — partition assessments into scheduled vs completed

- [x] **9.4 — Controllers** — `dashboard/controller/`
  - `GET /api/v1/recruiter/dashboard` → 200
  - `GET /api/v1/candidate/dashboard` → 200

#### ✅ Checkpoint: Returns real aggregated counts from DB

---

### Module 10: Reports & Selected Candidates

> Real data — query assessments with filters, pagination, and candidate aggregation.

- [x] **10.1 — Report DTOs** — `report/dto/`
  - `ReportListResponse` (paginated), `ReportSummaryResponse`

- [x] **10.2 — `ReportService`**
  - `getReports(UUID recruiterId, filters, pagination)`
  - `getReportById(UUID recruiterId, UUID reportId)`
  - `getReportSummary(UUID recruiterId)`

- [x] **10.3 — `ReportController`**
  - `GET /api/v1/reports?workspaceId=&status=&page=&size=` → 200
  - `GET /api/v1/reports/{reportId}` → 200
  - `GET /api/v1/reports/summary` → 200

- [x] **10.4 — Selected Candidate DTOs & Entity** — `selectedcandidate/`
  - `SelectedCandidate` entity (`selected_candidates` table with unique `(workspace_id, candidate_id)`)
  - `SelectedCandidateRepository`, `SelectCandidateRequest`, `SelectedCandidateItemDto`

- [x] **10.5 — `SelectedCandidateService`**
  - `selectCandidate(SelectCandidateRequest)`
  - `getSelectedCandidates(UUID workspaceId)`
  - `removeSelectedCandidate(UUID id)`

- [x] **10.6 — `SelectedCandidateController`**
  - `POST   /api/v1/selected-candidates` → 201
  - `GET    /api/v1/selected-candidates?workspaceId=` → 200
  - `DELETE /api/v1/selected-candidates/{id}` → 200

#### ✅ Checkpoint: Reports and selected candidates return real DB data

---

### Module 11: Authentication (FINAL MODULE)

> Now lock everything down — JWT, login/signup, role-based access, and wire `AuthUtil` into all controllers.

- [ ] **11.1 — Auth DTOs** — `auth/dto/`
  - `SignupRequest`: `name`, `email`, `password` (all `@NotBlank`)
  - `LoginRequest`: `email`, `password` (all `@NotBlank`)
  - `AuthResponse`: `id`, `name`, `email`, `role`

- [ ] **11.2 — `CustomUserDetailsService`** — `auth/service/CustomUserDetailsService.java`
  - Implements `UserDetailsService`, loads by email from `UserRepository`

- [ ] **11.3 — JWT Implementation** (or Spring Session — pick one)
  - JWT token generation (sign with secret, include user ID + role)
  - JWT token validation filter (`OncePerRequestFilter`)
  - Extract user from token and set `SecurityContext`

- [ ] **11.4 — `AuthUtil`** — `auth/util/AuthUtil.java`
  - `getCurrentUser()` → get `User` from `SecurityContext`
  - `getCurrentUserId()` → `UUID`
  - `getCurrentUserRole()` → `Role`

- [ ] **11.5 — `AuthService`** — `auth/service/AuthService.java`
  - `recruiterSignup(SignupRequest)` → validate unique email, hash password, save `RECRUITER`, return `AuthResponse` + token
  - `recruiterLogin(LoginRequest)` → authenticate, return `AuthResponse` + token
  - `candidateSignup(SignupRequest)` → same for `CANDIDATE`
  - `candidateLogin(LoginRequest)` → same
  - `logout()` → invalidate/blacklist token
  - `getCurrentUser()` → return profile of authenticated user

- [ ] **11.6 — Auth Controllers** — `auth/controller/`
  - `RecruiterAuthController`:
    - `POST /api/v1/auth/recruiter/signup` → 201
    - `POST /api/v1/auth/recruiter/login` → 200
    - `POST /api/v1/auth/recruiter/logout` → 200
    - `GET  /api/v1/auth/recruiter/me` → 200
  - `CandidateAuthController`:
    - `POST /api/v1/auth/candidate/signup` → 201
    - `POST /api/v1/auth/candidate/login` → 200
    - `POST /api/v1/auth/candidate/logout` → 200
    - `GET  /api/v1/auth/candidate/me` → 200

- [ ] **11.7 — Lock Down `SecurityConfig`** — Replace `permitAll()` with real rules:
  ```java
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/v1/auth/**").permitAll()
      .requestMatchers("/api/v1/recruiter/**").hasRole("RECRUITER")
      .requestMatchers("/api/v1/workspaces/**").hasRole("RECRUITER")
      .requestMatchers("/api/v1/candidates/**").hasRole("RECRUITER")
      .requestMatchers("/api/v1/reports/**").hasRole("RECRUITER")
      .requestMatchers("/api/v1/selected-candidates/**").hasRole("RECRUITER")
      .requestMatchers("/api/v1/candidate/**").hasRole("CANDIDATE")
      .requestMatchers("/api/v1/assessments/*/start").hasRole("CANDIDATE")
      .requestMatchers("/api/v1/assessments/*/submit").hasRole("CANDIDATE")
      .requestMatchers("/api/v1/assessments/*/files/**").hasRole("CANDIDATE")
      .requestMatchers("/api/v1/assessments/*/run").hasRole("CANDIDATE")
      .requestMatchers("/api/v1/assessments/*/stop").hasRole("CANDIDATE")
      .requestMatchers("/api/v1/assessments/*/execution/**").hasRole("CANDIDATE")
      .requestMatchers("/api/v1/assessments/*/result").hasRole("CANDIDATE")
      .anyRequest().authenticated()
  )
  ```

- [ ] **11.8 — Refactor All Controllers** — Replace hardcoded/param user IDs with `AuthUtil.getCurrentUserId()`
  - Update `WorkspaceController`, `CandidateController`, `AssessmentController`, `DashboardControllers`, `ReportController`, `SelectedCandidateController`

#### ✅ Checkpoint: Full auth flow works
```
POST /auth/recruiter/signup   → 201 + token
POST /auth/recruiter/login    → 200 + token
GET  /auth/recruiter/me       → 200 profile (with token)
GET  /workspaces (no token)   → 401 Unauthorized
GET  /workspaces (with token) → 200 list
POST /auth/candidate/signup   → 201 + token
POST /auth/candidate/login    → 200 + token
GET  /workspaces (candidate token) → 403 Forbidden
```

---

## Final Verification

- [ ] **`mvn clean package -DskipTests`** — JAR builds
- [ ] **`docker compose up --build`** — all 3 containers start
- [ ] **Hibernate auto-creates all 11 tables** in PostgreSQL
- [ ] **Full end-to-end smoke test:**
  1. Recruiter signup → login → create workspace → search candidate → add to workspace → create assessment → view processing status → view dashboard → view reports
  2. Candidate signup → login → view dashboard → start assessment → view feature → browse files → run → view logs → submit → view result

---

## Build Order Summary

| Order | Module | Endpoints | Auth? |
|:---|:---|:---|:---|
| **0** | Common Foundation | 0 | ❌ `permitAll()` |
| **1** | Workspace | 8 | ❌ No auth |
| **2** | Candidate Management | 4 | ❌ No auth |
| **3** | Assessment | 8 | ❌ No auth |
| **4** | Repository Analysis | 2 🟡 | ❌ No auth |
| **5** | Feature Specification | 1 🟡 | ❌ No auth |
| **6** | File Explorer | 3 🟡 | ❌ No auth |
| **7** | Execution | 4 🟡 | ❌ No auth |
| **8** | Evaluation & Results | 3 🟡 | ❌ No auth |
| **9** | Dashboard | 2 | ❌ No auth |
| **10** | Reports & Selected Candidates | 5 | ❌ No auth |
| **11** | **Authentication** | 8 | ✅ **Lock down everything** |
| | **Total** | **~48** | |
