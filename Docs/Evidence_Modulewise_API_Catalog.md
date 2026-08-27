# Evidence Platform — Complete Module-Wise API Catalog

> **Base URL:** `/api/v1`  
> **Protocol:** REST / JSON  
> **Database:** PostgreSQL (`evidence_db`)  
> **Authentication Headers:** `X-Recruiter-Id: <UUID>` (for Recruiter endpoints), `X-Candidate-Id: <UUID>` (for Candidate endpoints)

---

## Summary of All System Endpoints (25 Total)

| # | HTTP Method | Endpoint Path | Module | One-Line Purpose |
|---|---|---|---|---|
| 1 | `GET` | `/recruiter/dashboard` | Dashboard | Returns aggregated recruiter hiring metrics (total workspaces, candidate count, active and completed assessments). |
| 2 | `GET` | `/candidate/dashboard` | Dashboard | Returns scheduled upcoming and completed assessment history for the candidate. |
| 3 | `POST` | `/workspaces` | Workspace | Creates a new hiring workspace for a recruiter. |
| 4 | `GET` | `/workspaces` | Workspace | Lists all workspaces owned by the authenticated recruiter. |
| 5 | `GET` | `/workspaces/{workspaceId}` | Workspace | Retrieves detailed metadata and candidate counts for a specific workspace. |
| 6 | `PUT` | `/workspaces/{workspaceId}` | Workspace | Updates title and description of an existing workspace. |
| 7 | `DELETE` | `/workspaces/{workspaceId}` | Workspace | Deletes a workspace and its associated candidate memberships. |
| 8 | `GET` | `/workspaces/{workspaceId}/candidates` | Workspace | Lists all enrolled candidates in a workspace. |
| 9 | `POST` | `/workspaces/{workspaceId}/candidates` | Workspace | Enrolls a registered candidate into a workspace by email. |
| 10 | `DELETE` | `/workspaces/{workspaceId}/candidates/{candidateId}` | Workspace | Removes a candidate's membership from a workspace. |
| 11 | `GET` | `/candidates/search?email={email}` | Candidate | Searches registered candidates by email address for workspace enrollment. |
| 12 | `GET` | `/candidates/{candidateId}/assessments` | Candidate | Retrieves assessment history and scores for a specific candidate. |
| 13 | `POST` | `/workspaces/{workspaceId}/assessments` | Assessment | Creates an assessment and triggers the 5-phase AI generation pipeline (Git clone, Docker validation, AST extraction, Mistral AI feature & test generation). |
| 14 | `GET` | `/assessments/{assessmentId}` | Assessment | Retrieves assessment details (with repository and test case internals safely hidden from candidates). |
| 15 | `GET` | `/assessments/{assessmentId}/processing-status` | Assessment | Returns the preparation and analysis status of the assessment pipeline. |
| 16 | `GET` | `/assessments/{assessmentId}/feature` | Assessment | Fetches the generated AI feature specification and domain requirements. |
| 17 | `GET` | `/assessments/{assessmentId}/status` | Assessment | Polls the current assessment lifecycle state (`CREATING`, `READY`, `IN_PROGRESS`, `EVALUATING`, `COMPLETED`). |
| 18 | `POST` | `/assessments/{assessmentId}/start` | Workspace IDE | Starts the candidate assessment, isolates a clean copy of the original repo into `candidate_workspace/`, and sets status to `IN_PROGRESS`. |
| 19 | `GET` | `/assessments/{assessmentId}/files` | Workspace IDE | Returns the full hierarchical file and folder directory tree of the candidate workspace. |
| 20 | `GET` | `/assessments/{assessmentId}/files/content?path={filePath}` | Workspace IDE | Reads the content of a specific file for the Monaco Code Editor. |
| 21 | `PUT` | `/assessments/{assessmentId}/files/content` | Workspace IDE | Saves file changes to disk with 2.5s debounced autosave support. |
| 22 | `POST` | `/assessments/{assessmentId}/files` | Workspace IDE | Creates a new file or directory in the candidate workspace. |
| 23 | `DELETE` | `/assessments/{assessmentId}/files?path={filePath}` | Workspace IDE | Deletes a file or directory in the candidate workspace. |
| 24 | `POST` | `/assessments/{assessmentId}/files/rename` | Workspace IDE | Renames or moves a file/directory in the candidate workspace. |
| 25 | `POST` | `/assessments/{assessmentId}/run` | Sandbox Run | Compiles modified candidate code, builds a container, and starts the application on a dynamic port. |
| 26 | `GET` | `/assessments/{assessmentId}/execution/status` | Sandbox Run | Returns the current build, container, and application health status. |
| 27 | `GET` | `/assessments/{assessmentId}/execution/logs` | Sandbox Run | Streams buffered stdout/stderr application logs to the frontend terminal console. |
| 28 | `POST` | `/assessments/{assessmentId}/stop` | Sandbox Run | Terminates the running container and frees port bindings. |
| 29 | `POST` | `/assessments/{assessmentId}/submit` | Evaluation | Submits the assessment, launches the evaluation container, sequentially executes all stored AI Black-Box test cases, computes the weighted score, and creates the evaluation report. |
| 30 | `GET` | `/assessments/{assessmentId}/result` | Evaluation | Returns a candidate-safe outcome summary (score, total, passed, and failed test counts) without leaking hidden assertions. |
| 31 | `GET` | `/assessments/{assessmentId}/report` | Evaluation | Returns the detailed evaluation report for recruiters with candidate metrics. |
| 32 | `GET` | `/assessments/{assessmentId}/test-results` | Evaluation | Returns a granular audit of every test case, including status, execution latency, actual status code, and failure reasons. |
| 33 | `GET` | `/reports?workspaceId={workspaceId}&page={page}&size={size}` | Reports | Retrieves a paginated list of completed candidate assessment submissions for a workspace. |

---

## Detailed Module-by-Module Breakdown

### 1. Dashboard Module
- **Controller:** [`DashboardController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/dashboard/controller/DashboardController.java)
- **Service:** [`DashboardService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/dashboard/service/DashboardService.java)
- **Endpoints:**
  - `GET /api/v1/recruiter/dashboard` — Aggregates and returns recruiter metrics including total workspaces, candidates enrolled, total assessments, and active/completed counts.
  - `GET /api/v1/candidate/dashboard` — Aggregates and returns scheduled upcoming and completed assessment history for the candidate.

---

### 2. Workspace Module
- **Controller:** [`WorkspaceController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/workspace/controller/WorkspaceController.java)
- **Service:** [`WorkspaceService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/workspace/service/WorkspaceService.java)
- **Endpoints:**
  - `POST /api/v1/workspaces` — Creates a new recruitment workspace.
  - `GET /api/v1/workspaces` — Lists all workspaces for the recruiter.
  - `GET /api/v1/workspaces/{workspaceId}` — Returns full workspace metadata.
  - `PUT /api/v1/workspaces/{workspaceId}` — Modifies workspace name/description.
  - `DELETE /api/v1/workspaces/{workspaceId}` — Removes a workspace.
  - `GET /api/v1/workspaces/{workspaceId}/candidates` — Returns all candidates in the workspace.
  - `POST /api/v1/workspaces/{workspaceId}/candidates` — Adds a candidate to the workspace by email.
  - `DELETE /api/v1/workspaces/{workspaceId}/candidates/{candidateId}` — Removes candidate from workspace.

---

### 3. Candidate Management Module
- **Controller:** [`CandidateController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/candidate/controller/CandidateController.java)
- **Service:** [`CandidateService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/candidate/service/CandidateService.java)
- **Endpoints:**
  - `GET /api/v1/candidates/search?email={email}` — Looks up candidate by email.
  - `GET /api/v1/candidates/{candidateId}/assessments` — Retrieves assessment performance and history for a candidate.

---

### 4. Assessment Creation & AI Generation Module
- **Controller:** [`AssessmentController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/controller/AssessmentController.java)
- **Service:** [`AssessmentService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/service/AssessmentService.java)
- **Orchestrator:** [`AssessmentProcessingOrchestrator.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/pipeline/orchestration/AssessmentProcessingOrchestrator.java)
- **Endpoints:**
  - `POST /api/v1/workspaces/{workspaceId}/assessments` — Triggers 5-phase automated pipeline (Cloning $\rightarrow$ Docker validation $\rightarrow$ AST extraction $\rightarrow$ Mistral AI feature generation $\rightarrow$ Black-Box test generation).
  - `GET /api/v1/assessments/{assessmentId}` — Fetches assessment configuration details.
  - `GET /api/v1/assessments/{assessmentId}/processing-status` — Returns progress status of pipeline stages.
  - `GET /api/v1/assessments/{assessmentId}/feature` — Returns the generated AI feature specification.
  - `GET /api/v1/assessments/{assessmentId}/status` — Polls the current lifecycle state of the assessment.

---

### 5. Candidate Workspace & Monaco File Explorer IDE Module
- **Controller:** [`CandidateWorkspaceController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/controller/CandidateWorkspaceController.java)
- **Service:** [`CandidateWorkspaceService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/service/CandidateWorkspaceService.java)
- **Endpoints:**
  - `POST /api/v1/assessments/{assessmentId}/start` — Copies template repo into candidate isolated workspace and sets status to `IN_PROGRESS`.
  - `GET /api/v1/assessments/{assessmentId}/files` — Returns hierarchical directory tree JSON structure.
  - `GET /api/v1/assessments/{assessmentId}/files/content?path={filePath}` — Reads file text for editor.
  - `PUT /api/v1/assessments/{assessmentId}/files/content` — Saves file changes (with 2500ms debounced autosave).
  - `POST /api/v1/assessments/{assessmentId}/files` — Creates new file/directory.
  - `DELETE /api/v1/assessments/{assessmentId}/files?path={filePath}` — Deletes file/directory.
  - `POST /api/v1/assessments/{assessmentId}/files/rename` — Renames or moves file/directory.

---

### 6. Interactive Sandbox Execution Module
- **Controller:** [`CandidateExecutionController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/controller/CandidateExecutionController.java)
- **Service:** [`CandidateExecutionService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/service/CandidateExecutionService.java)
- **Endpoints:**
  - `POST /api/v1/assessments/{assessmentId}/run` — Recompiles modified candidate code and spins up ephemeral container on dynamic port.
  - `GET /api/v1/assessments/{assessmentId}/execution/status` — Checks build and container health.
  - `GET /api/v1/assessments/{assessmentId}/execution/logs` — Streams application console logs.
  - `POST /api/v1/assessments/{assessmentId}/stop` — Terminates container process.

---

### 7. Evaluation & Submission Module
- **Controller:** [`CandidateEvaluationController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/controller/CandidateEvaluationController.java)
- **Services:** [`CandidateEvaluationService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/service/CandidateEvaluationService.java), [`BlackBoxTestRunnerService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/service/BlackBoxTestRunnerService.java), [`ResponseAssertionMatcher.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/assessment/service/ResponseAssertionMatcher.java)
- **Endpoints:**
  - `POST /api/v1/assessments/{assessmentId}/submit` — Triggers automated HTTP Black-Box evaluation, computes weighted score, and generates `EvaluationReport`.
  - `GET /api/v1/assessments/{assessmentId}/result` — Safe candidate result view (score, tests passed/failed).
  - `GET /api/v1/assessments/{assessmentId}/report` — Detailed recruiter assessment evaluation report.
  - `GET /api/v1/assessments/{assessmentId}/test-results` — Granular breakdown of each test case execution, latency, and status.

---

### 8. Recruiter Reports Module
- **Controller:** [`ReportController.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/report/controller/ReportController.java)
- **Service:** [`ReportService.java`](file:///d:/Evidence_Development/backend/src/main/java/com/example/backend/report/service/ReportService.java)
- **Endpoints:**
  - `GET /api/v1/reports?workspaceId={workspaceId}&status=COMPLETED&page=0&size=20` — Retrieves paginated candidate submission reports for a recruiter's workspace.
