# Evidence Platform — Phased Implementation TODO

> **Reference Docs:** [`Docs/APIEndpointsAndDatabaseVerificationTest.md`](file:///d:/Evidence_Development/Docs/APIEndpointsAndDatabaseVerificationTest.md)  
> **Backend Path:** [`backend/`](file:///d:/Evidence_Development/backend)  
> **Base API URL:** `/api/v1`  
> **Target Database:** PostgreSQL (`evidence_db`)

---

## 🚀 Active Pipeline: Take Assessment, Live Sandbox Execution & Automated Evaluation

---

### 📦 Phase A: Candidate Workspace Isolation & Real-Time File Explorer IDE
> **Goal:** Allow the candidate to start an assessment, isolate a copy of the original repo into `candidate_workspace/`, browse directory tree, view file contents in Monaco Editor, and perform debounced autosave (2.5s) / file operations.

- [x] **A.1 — Assessment Start & Workspace Isolation Service**
  - Path: `com.example.backend.assessment.service.CandidateWorkspaceService.java`
  - Logic:
    - Validate schedule window (`scheduledStartAt <= now < scheduledEndAt`).
    - Copy folder `storage/assessments/{id}/original` to `storage/assessments/{id}/candidate_workspace` (excluding `.git`, `target`, `.idea`, `node_modules`).
    - Persist `candidate_workspace_path` into `ASSESSMENT_WORKSPACES` entity.
    - Transition `ASSESSMENT.status` to `IN_PROGRESS`.
  - Endpoint: `POST /api/v1/assessments/{assessmentId}/start`

- [x] **A.2 — Hierarchical File Explorer Directory Tree API**
  - Path: `com.example.backend.assessment.service.CandidateWorkspaceService.java` & DTOs
  - Logic:
    - Recursively scan `candidate_workspace/` and generate nested `FileNodeDto` JSON tree.
    - Exclude binary and system files (`.class`, `.jar`, `.git`, `.idea`, `target`, `node_modules`).
  - Endpoint: `GET /api/v1/assessments/{assessmentId}/files`

- [x] **A.3 — File Content Read & Path Sanitization**
  - Logic:
    - Sanitize `path` parameter against directory traversal (`../` protection).
    - Read file string with character encoding and auto-detect language (`java`, `xml`, `properties`, `json`, `yaml`).
  - Endpoint: `GET /api/v1/assessments/{assessmentId}/files/content?path={filePath}`

- [x] **A.4 — Debounced Autosave (2500ms) & File Update API**
  - Logic:
    - Accept modified file content from frontend Monaco Editor.
    - Write directly to candidate workspace file on disk.
    - Maintain atomicity and return updated timestamp.
  - Endpoint: `PUT /api/v1/assessments/{assessmentId}/files/content`

- [x] **A.5 — Workspace File Management (Create, Delete, Rename)**
  - Logic:
    - Create new file or folder (`POST /api/v1/assessments/{assessmentId}/files`).
    - Delete file or folder (`DELETE /api/v1/assessments/{assessmentId}/files?path=...`).
    - Rename or move file (`POST /api/v1/assessments/{assessmentId}/files/rename`).

---

### ⚡ Phase B: Interactive Sandbox Compilation, Docker Run & Terminal Log Streaming
> **Goal:** Allow the candidate to click "Run" in the frontend, compile their updated code, launch a test container on a dynamic port, and stream live stdout/stderr logs.

- [x] **B.1 — Sandbox Execution Engine & Compilation Validation**
  - Path: `com.example.backend.assessment.service.CandidateExecutionService.java`
  - Logic:
    - Scan modified Java files and verify compilation with Maven or JDK compiler.
    - If compilation fails, return parsed compiler errors (file, line number, message) to the UI immediately.
    - Package updated `.jar` into `target/app.jar`.
  - Endpoint: `POST /api/v1/assessments/{assessmentId}/run`

- [x] **B.2 — Ephemeral Docker Container Lifecycle & Port Binding**
  - Logic:
    - If a previous container exists for this candidate assessment, gracefully stop and remove it.
    - Build Docker image: `candidate-assessment-{id}:run`.
    - Run container detached on a non-conflicting dynamic port (e.g. `18080-19080`).
    - Check container uptime status (`STARTING`, `RUNNING`, `FAILED`).
  - Endpoint: `GET /api/v1/assessments/{assessmentId}/execution/status`

- [x] **B.3 — Live Terminal & Output Log Streaming**
  - Path: `com.example.backend.assessment.service.ProcessLogBuffer.java`
  - Logic:
    - Buffer `docker logs --tail 200` output.
    - Return plain formatted terminal text for Xterm.js / frontend terminal console.
  - Endpoint: `GET /api/v1/assessments/{assessmentId}/execution/logs`

- [x] **B.4 — Stop Application Container**
  - Logic:
    - Terminate container process and release port resources.
  - Endpoint: `POST /api/v1/assessments/{assessmentId}/stop`

---

### 🧪 Phase C: Final Submission & Automated Black-Box Evaluation Engine
> **Goal:** When the candidate clicks "Submit", lock the workspace, launch the evaluation container, sequentially execute all stored test cases, match assertions, calculate weighted score, and persist test results and evaluation reports.

- [x] **C.1 — Assessment Submission & State Locking**
  - Path: `com.example.backend.assessment.service.CandidateEvaluationService.java`
  - Logic:
    - Verify assessment is `IN_PROGRESS` and within schedule.
    - Transition `ASSESSMENT.status` to `EVALUATING`.
    - Create and persist `SUBMISSION` entity (`status=SUBMITTED`, `timeTakenSeconds = now - startedAt`).
  - Endpoint: `POST /api/v1/assessments/{assessmentId}/submit`

- [x] **C.2 — Automated Black-Box Test Runner Service**
  - Path: `com.example.backend.assessment.service.BlackBoxTestRunnerService.java`
  - Logic:
    - Spin up candidate Docker container on evaluation port.
    - Fetch all `TEST_CASE` records for this assessment from PostgreSQL ordered by `test_case_number`.
    - Loop through test cases and execute sequential HTTP requests (`GET`, `POST`, `PUT`, `DELETE`).
    - Capture response status code, response body, and latency (`execution_time_ms`).

- [x] **C.3 — Response Assertion Matcher & Test Result Persistence**
  - Path: `com.example.backend.assessment.service.ResponseAssertionMatcher.java`
  - Logic:
    - Validate `actualStatusCode == expectedStatusCode`.
    - Compare JSON response schema / expected fields using Jackson.
    - Record individual `TEST_RESULT` rows (`test_case_id`, `status: PASSED/FAILED`, `actual_status_code`, `actual_response`, `execution_time_ms`, `failure_reason`).

- [x] **C.4 — Scoring Engine & Evaluation Report Generation**
  - Logic:
    - Calculate: $\text{Weighted Score} = \left(\frac{\sum \text{weight of passed tests}}{\sum \text{total test weights}}\right) \times 100$.
    - Persist `EVALUATION_REPORT` entity (`submission_id`, `score`, `total_tests`, `passed_tests`, `failed_tests`, `build_status`, `application_status`, `time_taken_seconds`, `status=COMPLETED`, `evaluated_at=now`).
    - Transition `ASSESSMENT.status` to `COMPLETED`.
    - Clean up evaluation Docker container.

- [x] **C.5 — Safe Candidate Result View**
  - Logic:
    - Return score, total/passed/failed test counts, duration (without leaking hidden assertions or test payloads).
  - Endpoint: `GET /api/v1/assessments/{assessmentId}/result`

---

### 📊 Phase D: Recruiter Evaluation Reports & Audit Breakdown
> **Goal:** Provide recruiters with detailed audit reports, paginated submission views, and granular test result breakdowns.

- [x] **D.1 — Paginated Workspace Reports API**
  - Endpoint: `GET /api/v1/reports?workspaceId={id}&status=COMPLETED&page=0&size=20`

- [x] **D.2 — Recruiter Detailed Assessment Report API**
  - Endpoint: `GET /api/v1/assessments/{assessmentId}/report`

- [x] **D.3 — Recruiter Granular Test Results Breakdown API**
  - Endpoint: `GET /api/v1/assessments/{assessmentId}/test-results`

---

### 🛡️ Phase E: End-to-End Integration Tests & PostgreSQL Verification
> **Goal:** Ensure 100% automated test coverage across all new endpoints and database transactions.

- [x] **E.1 — Integration Test Suite Updates**
  - File: `backend/src/test/java/com/example/backend/ApiEndpointsAndDatabaseVerificationTest.java`
  - MockMvc integration tests for all 15 endpoints verified against PostgreSQL.
- [x] **E.2 — Verification & Full Suite Pass**
  - Ran `mvn test` — 13 tests passed, 0 failures, 0 errors (`BUILD SUCCESS`).

---

## 🎨 Phase F: Frontend Complete Mock & UI Verification
> **Goal:** Complete all missing frontend views and components with rich interactive mock data for manual review.

- [x] **F.1 — Assessment Pipeline Processing Visualizer Modal**
  - Path: `frontend/src/components/dashboard/AssessmentProcessingModal.tsx`
  - 5-Phase pipeline timeline (`Cloning` $\rightarrow$ `Docker Validation` $\rightarrow$ `AST Architecture` $\rightarrow$ `Mistral AI Feature` $\rightarrow$ `Black-Box Test Cases`).
- [x] **F.2 — Candidate Pre-Assessment Instructions & System Check Screen**
  - Path: `frontend/src/components/candidate/PreAssessmentModal.tsx`
  - Assessment guidelines, duration, 2.5s debounced autosave notice, and workspace entry trigger.
- [x] **F.3 — Recruiter AST Codebase & AI Feature Spec Inspector**
  - Path: `frontend/src/components/dashboard/AstAnalysisDrawer.tsx`
  - AST architecture overview, endpoints list, and full AI Feature Specification contract.
- [x] **F.4 — Granular Recruiter Test Case Audit Breakdown**
  - Path: `frontend/src/components/candidate/BugBreakdownTable.tsx`
  - Detailed test cases tab with HTTP method, endpoint, expected vs actual status code, latency, and assertions.
- [x] **F.5 — Frontend TypeScript & Vite Build Verification**
  - Ran `npm run build` — 1981 modules transformed with 0 errors (`Exit code 0`).

---

## 🔌 Phase G: Real API Integration (Upcoming)
> **Goal:** Connect frontend services and React Query hooks to real backend `/api/v1` endpoints.
- [ ] G.1 — Connect Auth & Session Headers (`X-Recruiter-Id`, `X-Candidate-Id`)
- [ ] G.2 — Connect Recruiter Dashboard & Workspace Endpoints
- [ ] G.3 — Connect Candidate Search & Assessment Creation with Real Pipeline
- [ ] G.4 — Connect Monaco IDE (`/files`, `/files/content`, `/run`, `/logs`, `/stop`, `/submit`)
- [ ] G.5 — Connect Candidate & Recruiter Evaluation Reports

