# EVIDENCE — REST API Endpoints Specification & Status Matrix

> **Base URL:** `/api/v1`  
> **API Version:** 1.0 (MVP)  
> **Technology Stack:** Java 21, Spring Boot 4.1, Spring Data JPA, PostgreSQL  
> **Last Updated:** August 20, 2026

---

## 1. Overall Implementation Summary

| Category | Count | Description |
|:---|:---:|:---|
| **Total API Endpoints** | **49** | Complete platform API surface across 11 modules |
| ✅ **Real DB Implemented** | **29** | Endpoints wired to PostgreSQL with real JPA queries, validations, and cascades |
| 🟡 **Dummy / Mock Stubs** | **12** | Controller endpoints return simulated mock data (awaiting real AI/Docker pipelines) |
| ❌ **Not Yet Implemented** | **8** | Auth & security endpoints (scheduled for Phase 1 of backend roadmap) |

---

## 2. Master API Endpoint Matrix

| # | Module | Method | Endpoint Path | Description | Current Status |
|:---:|:---|:---:|:---|:---|:---:|
| **1** | **Workspace** | `POST` | `/api/v1/workspaces` | Creates a new recruiter workspace | ✅ Real DB |
| **2** | **Workspace** | `GET` | `/api/v1/workspaces` | Lists all workspaces owned by recruiter | ✅ Real DB |
| **3** | **Workspace** | `GET` | `/api/v1/workspaces/{workspaceId}` | Gets single workspace details by ID | ✅ Real DB |
| **4** | **Workspace** | `PUT` | `/api/v1/workspaces/{workspaceId}` | Updates workspace name & description | ✅ Real DB |
| **5** | **Workspace** | `DELETE` | `/api/v1/workspaces/{workspaceId}` | Cascades & deletes workspace and children | ✅ Real DB |
| **6** | **Workspace** | `GET` | `/api/v1/workspaces/{workspaceId}/candidates` | Lists all candidates enrolled in workspace | ✅ Real DB |
| **7** | **Workspace** | `POST` | `/api/v1/workspaces/{workspaceId}/candidates` | Enrolls candidate by email into workspace | ✅ Real DB |
| **8** | **Workspace** | `DELETE` | `/api/v1/workspaces/{workspaceId}/candidates/{candidateId}` | Removes a candidate from workspace | ✅ Real DB |
| **9** | **Candidate** | `GET` | `/api/v1/candidates/search?email={email}` | Finds existing candidate by email address | ✅ Real DB |
| **10** | **Candidate** | `GET` | `/api/v1/candidates/{candidateId}` | Gets candidate profile information | ✅ Real DB |
| **11** | **Candidate** | `GET` | `/api/v1/candidates/{candidateId}/workspaces` | Lists all workspaces candidate belongs to | ✅ Real DB |
| **12** | **Candidate** | `GET` | `/api/v1/candidates/{candidateId}/assessments` | Lists all assessments assigned to candidate | ✅ Real DB |
| **13** | **Assessment** | `POST` | `/api/v1/workspaces/{workspaceId}/assessments` | Creates technical assessment with repo details | ✅ Real DB |
| **14** | **Assessment** | `GET` | `/api/v1/workspaces/{workspaceId}/assessments` | Lists all assessments within a workspace | ✅ Real DB |
| **15** | **Assessment** | `GET` | `/api/v1/assessments/{assessmentId}` | Gets assessment details (role-filtered) | ✅ Real DB |
| **16** | **Assessment** | `PUT` | `/api/v1/assessments/{assessmentId}` | Updates assessment schedule / difficulty | ✅ Real DB |
| **17** | **Assessment** | `POST` | `/api/v1/assessments/{assessmentId}/cancel` | Cancels an ongoing/scheduled assessment | ✅ Real DB |
| **18** | **Assessment** | `GET` | `/api/v1/assessments/{assessmentId}/processing-status` | Polls async pipeline preparation progress | 🟡 Dummy Stub |
| **19** | **Assessment** | `POST` | `/api/v1/assessments/{assessmentId}/start` | Starts assessment & transitions to IN_PROGRESS | ✅ Real DB |
| **20** | **Assessment** | `POST` | `/api/v1/assessments/{assessmentId}/submit` | Submits candidate code for AI evaluation | ✅ Real DB |
| **21** | **Analysis** | `GET` | `/api/v1/assessments/{assessmentId}/repository-analysis` | Gets AST repository analysis data | 🟡 Dummy Stub |
| **22** | **Analysis** | `GET` | `/api/v1/assessments/{assessmentId}/repository-analysis/status` | Gets repository analysis job status | 🟡 Dummy Stub |
| **23** | **Feature** | `GET` | `/api/v1/assessments/{assessmentId}/feature` | Gets AI-generated feature specification task | 🟡 Dummy Stub |
| **24** | **File Explorer** | `GET` | `/api/v1/assessments/{assessmentId}/files` | Returns recursive repository file tree | 🟡 Dummy Stub |
| **25** | **File Explorer** | `GET` | `/api/v1/assessments/{assessmentId}/files/content?path={path}` | Reads source file contents for IDE editor | 🟡 Dummy Stub |
| **26** | **File Explorer** | `PUT` | `/api/v1/assessments/{assessmentId}/files/content` | Saves edited file content from IDE editor | 🟡 Dummy Stub |
| **27** | **Execution** | `POST` | `/api/v1/assessments/{assessmentId}/run` | Starts Docker container runtime for project | 🟡 Dummy Stub |
| **28** | **Execution** | `POST` | `/api/v1/assessments/{assessmentId}/stop` | Stops running Docker execution container | 🟡 Dummy Stub |
| **29** | **Execution** | `GET` | `/api/v1/assessments/{assessmentId}/execution/status` | Checks container and application health | 🟡 Dummy Stub |
| **30** | **Execution** | `GET` | `/api/v1/assessments/{assessmentId}/execution/logs` | Fetches stdout/stderr logs from container | 🟡 Dummy Stub |
| **31** | **Evaluation** | `GET` | `/api/v1/assessments/{assessmentId}/result` | Gets candidate scorecard & evaluation summary | 🟡 Dummy Stub |
| **32** | **Evaluation** | `GET` | `/api/v1/assessments/{assessmentId}/report` | Gets full recruiter diagnostic report | 🟡 Dummy Stub |
| **33** | **Evaluation** | `GET` | `/api/v1/assessments/{assessmentId}/test-results` | Gets detailed test case assertion outcomes | 🟡 Dummy Stub |
| **34** | **Dashboard** | `GET` | `/api/v1/recruiter/dashboard` | Aggregated recruiter stats (counts, metrics) | ✅ Real DB |
| **35** | **Dashboard** | `GET` | `/api/v1/candidate/dashboard` | Scheduled vs completed candidate assessments | ✅ Real DB |
| **36** | **Report** | `GET` | `/api/v1/reports` | Paginated assessment report listing with filters | ✅ Real DB |
| **37** | **Report** | `GET` | `/api/v1/reports/{reportId}` | Detailed assessment report by ID | ✅ Real DB |
| **38** | **Report** | `GET` | `/api/v1/reports/summary` | Global assessment performance summary metrics | ✅ Real DB |
| **39** | **Selected Candidate** | `POST` | `/api/v1/selected-candidates` | Marks/selects candidate for hire | ✅ Real DB |
| **40** | **Selected Candidate** | `GET` | `/api/v1/selected-candidates?workspaceId={id}` | Lists all selected candidates in workspace | ✅ Real DB |
| **41** | **Selected Candidate** | `DELETE` | `/api/v1/selected-candidates/{id}` | Removes candidate from selected list | ✅ Real DB |
| **42** | **Auth (Recruiter)** | `POST` | `/api/v1/auth/recruiter/signup` | Recruiter registration + JWT generation | ❌ Not Implemented |
| **43** | **Auth (Recruiter)** | `POST` | `/api/v1/auth/recruiter/login` | Recruiter authentication + JWT token return | ❌ Not Implemented |
| **44** | **Auth (Recruiter)** | `POST` | `/api/v1/auth/recruiter/logout` | Recruiter logout & session invalidation | ❌ Not Implemented |
| **45** | **Auth (Recruiter)** | `GET` | `/api/v1/auth/recruiter/me` | Current authenticated recruiter profile | ❌ Not Implemented |
| **46** | **Auth (Candidate)** | `POST` | `/api/v1/auth/candidate/signup` | Candidate independent registration | ❌ Not Implemented |
| **47** | **Auth (Candidate)** | `POST` | `/api/v1/auth/candidate/login` | Candidate authentication + JWT token return | ❌ Not Implemented |
| **48** | **Auth (Candidate)** | `POST` | `/api/v1/auth/candidate/logout` | Candidate logout & session invalidation | ❌ Not Implemented |
| **49** | **Auth (Candidate)** | `GET` | `/api/v1/auth/candidate/me` | Current authenticated candidate profile | ❌ Not Implemented |

---

## 3. Detailed Module-by-Module Breakdown

### Module 1: Workspace Management (`/api/v1/workspaces`)
*Allows recruiters to create isolated workspaces, manage candidate rosters, and configure testing scopes.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `POST` | `/api/v1/workspaces` | `{ name, description }` | `WorkspaceResponse` (201) | Creates a new workspace owned by the current recruiter. | ✅ Real DB |
| `GET` | `/api/v1/workspaces` | *None* | `List<WorkspaceResponse>` | Lists all workspaces belonging to the recruiter. | ✅ Real DB |
| `GET` | `/api/v1/workspaces/{workspaceId}` | *None* | `WorkspaceResponse` | Returns detailed metadata of a specific workspace. | ✅ Real DB |
| `PUT` | `/api/v1/workspaces/{workspaceId}` | `{ name, description }` | `WorkspaceResponse` | Updates workspace name and description. | ✅ Real DB |
| `DELETE` | `/api/v1/workspaces/{workspaceId}` | *None* | `ApiResponse<Void>` | Cascades and deletes workspace, selected candidates, and assessments. | ✅ Real DB |
| `GET` | `/api/v1/workspaces/{workspaceId}/candidates` | *None* | `List<CandidateResponse>` | Lists all candidates currently enrolled in the workspace. | ✅ Real DB |
| `POST` | `/api/v1/workspaces/{workspaceId}/candidates` | `{ email }` | `CandidateResponse` (201) | Enrolls an existing registered candidate by email into the workspace. | ✅ Real DB |
| `DELETE` | `/api/v1/workspaces/{workspaceId}/candidates/{candidateId}` | *None* | `ApiResponse<Void>` | Removes a candidate from the workspace. | ✅ Real DB |

---

### Module 2: Candidate Management (`/api/v1/candidates`)
*Recruiter-side candidate exploration, roster lookup, and history inspection.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `GET` | `/api/v1/candidates/search?email={email}` | *None* | `CandidateResponse` | Searches for registered candidates by email (404 if not found). | ✅ Real DB |
| `GET` | `/api/v1/candidates/{candidateId}` | *None* | `CandidateResponse` | Gets profile details of a specific candidate. | ✅ Real DB |
| `GET` | `/api/v1/candidates/{candidateId}/workspaces` | *None* | `List<CandidateWorkspaceDto>` | Lists all workspaces a candidate has been enrolled in. | ✅ Real DB |
| `GET` | `/api/v1/candidates/{candidateId}/assessments` | *None* | `List<CandidateAssessmentDto>` | Lists all past and active assessments assigned to the candidate. | ✅ Real DB |

---

### Module 3: Assessment Lifecycle (`/api/v1/assessments`)
*Core evaluation assignment creation, scheduling, candidate start, and submission.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `POST` | `/api/v1/workspaces/{workspaceId}/assessments` | `CreateAssessmentRequest` | `AssessmentResponse` (201) | Creates assessment with repository URL, branch, difficulty, and schedule window. | ✅ Real DB |
| `GET` | `/api/v1/workspaces/{workspaceId}/assessments` | *None* | `List<AssessmentListItemResponse>` | Lists all assessments within a workspace. | ✅ Real DB |
| `GET` | `/api/v1/assessments/{assessmentId}` | *None* | `AssessmentResponse` | Gets assessment details (hides hidden tests from candidate). | ✅ Real DB |
| `PUT` | `/api/v1/assessments/{assessmentId}` | `UpdateAssessmentRequest` | `AssessmentResponse` | Updates schedule window or difficulty if not yet started. | ✅ Real DB |
| `POST` | `/api/v1/assessments/{assessmentId}/cancel` | *None* | `AssessmentResponse` | Cancels assessment and sets status to `CANCELLED`. | ✅ Real DB |
| `GET` | `/api/v1/assessments/{assessmentId}/processing-status` | *None* | `ProcessingStatusResponse` | Polls asynchronous AI preparation stages (Cloning, Analysis, Feature Gen, Test Gen). | 🟡 Dummy Stub |
| `POST` | `/api/v1/assessments/{assessmentId}/start` | *None* | `AssessmentResponse` | Validates time window and marks assessment `IN_PROGRESS`. | ✅ Real DB |
| `POST` | `/api/v1/assessments/{assessmentId}/submit` | *None* | `AssessmentResponse` | Submits candidate code and transitions to `EVALUATING`. | ✅ Real DB |

---

### Module 4: Repository Analysis (`/api/v1/assessments/{id}/repository-analysis`)
*Parses candidate's Git repository, extracts AST components, endpoints, and framework details.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `GET` | `/api/v1/assessments/{assessmentId}/repository-analysis` | *None* | `RepositoryAnalysisResponse` | Returns parsed routes, controllers, tech stack, and structure. | 🟡 Dummy Stub *(Returns hardcoded Spring Boot AST)* |
| `GET` | `/api/v1/assessments/{assessmentId}/repository-analysis/status` | *None* | `AnalysisStatusResponse` | Returns async repo analysis background job status. | 🟡 Dummy Stub |

*Yet to Implement:* JGit repository cloning, Tree-sitter / JavaParser AST analyzer, and DB persistence to `repository_analyses` table.

---

### Module 5: Feature Specification (`/api/v1/assessments/{id}/feature`)
*Provides the candidate with the AI-generated task requirements, contract, and constraints.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `GET` | `/api/v1/assessments/{assessmentId}/feature` | *None* | `FeatureSpecificationResponse` | Returns feature title, description, endpoint, and requirements. | 🟡 Dummy Stub *(Returns mock Note Search task)* |

*Yet to Implement:* LLM prompt pipeline generating contextual features matching the candidate's repository AST.

---

### Module 6: File Explorer (`/api/v1/assessments/{id}/files`)
*Enables the browser IDE to explore files, read source code, and save changes.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `GET` | `/api/v1/assessments/{assessmentId}/files` | *None* | `FileTreeResponse` | Returns directory tree of candidate repository. | 🟡 Dummy Stub *(Returns mock Java file tree)* |
| `GET` | `/api/v1/assessments/{assessmentId}/files/content?path={p}` | *None* | `FileContentResponse` | Reads source code content of a specific file. | 🟡 Dummy Stub *(Returns mock Java code)* |
| `PUT` | `/api/v1/assessments/{assessmentId}/files/content` | `SaveFileRequest` | `ApiResponse<Void>` | Saves edited source code to filesystem. | 🟡 Dummy Stub *(Simulates file save)* |

*Yet to Implement:* Real filesystem I/O operations targeting the candidate's isolated cloned repository folder.

---

### Module 7: Isolated Container Execution (`/api/v1/assessments/{id}/execution`)
*Manages Docker container lifecycle for running candidate application and streaming build logs.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `POST` | `/api/v1/assessments/{assessmentId}/run` | *None* | `ExecutionResponse` | Spawns Docker container and compiles application (`./mvnw clean spring-boot:run`). | 🟡 Dummy Stub *(Returns mock container ID)* |
| `POST` | `/api/v1/assessments/{assessmentId}/stop` | *None* | `ExecutionResponse` | Stops and tears down active Docker container. | 🟡 Dummy Stub |
| `GET` | `/api/v1/assessments/{assessmentId}/execution/status` | *None* | `ExecutionStatusResponse` | Checks build status (`SUCCESS`/`FAILED`) and container health. | 🟡 Dummy Stub |
| `GET` | `/api/v1/assessments/{assessmentId}/execution/logs` | *None* | `ExecutionLogsResponse` | Streams stdout and stderr build logs to the IDE terminal. | 🟡 Dummy Stub *(Returns mock Spring Boot logs)* |

*Yet to Implement:* Docker Java SDK / Testcontainers integration, ephemeral container provisioning, port mapping, and live log buffer streaming.

---

### Module 8: Evaluation & Test Results (`/api/v1/assessments/{id}/evaluation`)
*Executes hidden automated HTTP test suites and computes scores.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `GET` | `/api/v1/assessments/{assessmentId}/result` | *None* | `CandidateResultResponse` | Returns final score and high-level completion status to candidate. | 🟡 Dummy Stub *(Returns 82.00 score)* |
| `GET` | `/api/v1/assessments/{assessmentId}/report` | *None* | `AssessmentReportResponse` | Returns comprehensive diagnostic evaluation report to recruiter. | 🟡 Dummy Stub *(Returns Aarav Patel report)* |
| `GET` | `/api/v1/assessments/{assessmentId}/test-results` | *None* | `List<TestResultResponse>` | Returns hidden test assertion pass/fail details (recruiter only). | 🟡 Dummy Stub *(Returns 24 test cases)* |

*Yet to Implement:* Hidden HTTP test runner (RestAssured / WebClient against Docker container), LLM feedback generator, and score calculation engine.

---

### Module 9: Dashboard Aggregations (`/api/v1/recruiter/dashboard` & `/api/v1/candidate/dashboard`)
*Aggregates high-level metrics for recruiter and candidate home views.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `GET` | `/api/v1/recruiter/dashboard` | *None* | `RecruiterDashboardResponse` | Aggregates workspace count, candidate count, assessment totals, active vs completed. | ✅ Real DB |
| `GET` | `/api/v1/candidate/dashboard` | *None* | `CandidateDashboardResponse` | Returns scheduled upcoming assessments and completed score history. | ✅ Real DB |

---

### Module 10: Reports & Selected Candidates (`/api/v1/reports` & `/api/v1/selected-candidates`)
*Recruiter reporting, filtering, pagination, and candidate hiring shortlist.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `GET` | `/api/v1/reports` | *Query params: `workspaceId`, `status`, `page`, `size`* | `Page<ReportListItemDto>` | Paginated listing of candidate reports with filters. | ✅ Real DB |
| `GET` | `/api/v1/reports/{reportId}` | *None* | `ReportDetailResponse` | Detailed evaluation report with metrics by ID. | ✅ Real DB |
| `GET` | `/api/v1/reports/summary` | *None* | `ReportSummaryResponse` | Global average score, completion rate, and time statistics. | ✅ Real DB |
| `POST` | `/api/v1/selected-candidates` | `{ workspaceId, candidateId, notes }` | `SelectedCandidateResponse` (201) | Adds a high-performing candidate to the selection shortlist. | ✅ Real DB |
| `GET` | `/api/v1/selected-candidates?workspaceId={id}` | *None* | `List<SelectedCandidateResponse>` | Lists all shortlisted candidates in a workspace. | ✅ Real DB |
| `DELETE` | `/api/v1/selected-candidates/{id}` | *None* | `ApiResponse<Void>` | Removes candidate from selection shortlist. | ✅ Real DB |

---

### Module 11: Authentication & Security (`/api/v1/auth`)
*JWT authentication, user registration, role enforcement, and session guards.*

| Method | Endpoint | Request Body | Response Body | What It Does | Status |
|:---:|:---|:---|:---|:---|:---:|
| `POST` | `/api/v1/auth/recruiter/signup` | `{ name, email, password }` | `AuthResponse` (201) + JWT Token | Registers a new recruiter account and returns JWT token. | ❌ Not Implemented |
| `POST` | `/api/v1/auth/recruiter/login` | `{ email, password }` | `AuthResponse` + JWT Token | Authenticates recruiter and returns JWT token. | ❌ Not Implemented |
| `POST` | `/api/v1/auth/recruiter/logout` | *None* | `ApiResponse<Void>` | Invalidates active recruiter JWT token / session. | ❌ Not Implemented |
| `GET` | `/api/v1/auth/recruiter/me` | *None* | `UserResponse` | Returns profile of currently authenticated recruiter. | ❌ Not Implemented |
| `POST` | `/api/v1/auth/candidate/signup` | `{ name, email, password }` | `AuthResponse` (201) + JWT Token | Registers a new candidate account and returns JWT token. | ❌ Not Implemented |
| `POST` | `/api/v1/auth/candidate/login` | `{ email, password }` | `AuthResponse` + JWT Token | Authenticates candidate and returns JWT token. | ❌ Not Implemented |
| `POST` | `/api/v1/auth/candidate/logout` | *None* | `ApiResponse<Void>` | Invalidates active candidate JWT token / session. | ❌ Not Implemented |
| `GET` | `/api/v1/auth/candidate/me` | *None* | `UserResponse` | Returns profile of currently authenticated candidate. | ❌ Not Implemented |

---

## 4. Next Implementation Priorities (from `todo_next.md`)

```
Phase 1: Real Auth & JWT Security (Module 11)
  └── Lock down all endpoints, implement JWT filter, replace hardcoded user IDs with AuthUtil.
Phase 2: JGit Repository Ingestion & Cloning
  └── Real Git clone into isolated workspace volume.
Phase 3: AST Repository Analysis Pipeline (Tree-sitter / JavaParser)
  └── Extract real routes, controllers, and models.
Phase 4: LLM Feature & Bug Generation Engine
  └── Inject 3-tier bugs (Data Flow, Syntax, Business Logic).
Phase 5: Hidden HTTP Test Suite Generator
  └── Create automated assertion suite.
Phase 6: Docker Sandbox Execution Engine
  └── Real ephemeral container execution, health checks, log streaming.
Phase 7: Automated Evaluation & Scoring Engine
  └── Execute test suite against container, compute scores, generate AI summary.
```
