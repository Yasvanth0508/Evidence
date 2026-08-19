# Evidence — Backend Service Documentation

> **Tech Stack:** Java 21, Spring Boot 4.1.0, Spring Data JPA, Hibernate, PostgreSQL, Docker  
> **Base API Path:** `/api/v1`

---

## 📌 Table of Contents

- [Overview & Architecture](#-overview--architecture)
- [Project Package Structure](#-project-package-structure)
- [Database Entities & Relationships](#-database-entities--relationships)
- [Implemented Modules & Endpoints](#-implemented-modules--endpoints)
  - [Module 0: Common Foundation](#module-0-common-foundation)
  - [Module 1: Workspace Management](#module-1-workspace-management)
  - [Module 2: Candidate Management (Recruiter-Side)](#module-2-candidate-management-recruiter-side)
  - [Module 3: Assessment Lifecycle & Scheduling](#module-3-assessment-lifecycle--scheduling)
- [API Conventions & Error Handling](#-api-conventions--error-handling)
- [Testing & Verification](#-testing--verification)
- [How to Run](#-how-to-run)

---

## 🏗 Overview & Architecture

The Evidence Backend provides RESTful APIs for recruiter workspace management, candidate lookup, assessment scheduling, and automated grading for Java Spring Boot repositories.

It is designed following a **feature-based folder structure** where each business domain (`workspace`, `candidate`, `assessment`, `auth`, etc.) is self-contained with its own entities, repositories, DTOs, services, and controllers.

---

## 📂 Project Package Structure

```text
com.example.backend/
│
├── BackendApplication.java
│
├── common/                              # Shared foundation
│   ├── entity/BaseEntity.java           # MappedSuperclass (UUID id, createdAt, updatedAt)
│   ├── dto/
│   │   ├── ApiResponse.java             # Generic success envelope
│   │   └── ApiErrorResponse.java        # Standardized error envelope
│   ├── enums/                           # Role, AssessmentStatus, Difficulty, etc.
│   └── exception/                       # Custom domain exceptions & GlobalExceptionHandler
│
├── config/                              # App-level configs
│   ├── AppConfig.java                   # BCrypt password encoder
│   ├── CorsConfig.java                  # CORS origin mapping
│   ├── SecurityConfig.java              # Spring Security filter chain (permitAll for dev)
│   └── DataInitializer.java            # Automatic dev data seeding on startup
│
├── auth/                                # User entity & accounts
│   ├── entity/User.java
│   └── repository/UserRepository.java
│
├── workspace/                           # Workspace CRUD & candidate enrollment
│   ├── entity/
│   │   ├── Workspace.java
│   │   └── WorkspaceCandidate.java      # Unique constraint (workspace_id, candidate_id)
│   ├── repository/
│   │   ├── WorkspaceRepository.java
│   │   └── WorkspaceCandidateRepository.java
│   ├── dto/                             # Create, Update, AddCandidate, Response DTOs
│   ├── service/WorkspaceService.java
│   └── controller/WorkspaceController.java
│
├── candidate/                           # Recruiter-side candidate search & details
│   ├── dto/                             # CandidateResponse, CandidateWorkspaceDto, etc.
│   ├── service/CandidateService.java
│   └── controller/CandidateController.java
│
└── assessment/                          # Assessment scheduling, start, submit, cancel
    ├── entity/Assessment.java           # No exam_id invariant; strict schedule window
    ├── repository/AssessmentRepository.java
    ├── dto/                             # CreateAssessment, ProcessingStatus, Response DTOs
    ├── service/AssessmentService.java
    └── controller/AssessmentController.java
```

---

## 🗄 Database Entities & Relationships

```text
users (id, name, email [UNIQUE], password, role)
  │
  ├── workspaces (id, recruiter_id -> users.id, name, description, status)
  │     │
  │     ├── workspace_candidates (id, workspace_id, candidate_id -> users.id) [UNIQUE(workspace_id, candidate_id)]
  │     │
  │     └── assessments (id, workspace_id, candidate_id -> users.id, repo_url, branch, backend_dir, difficulty, duration_minutes, scheduled_start_at, scheduled_end_at, status, score)
```

- **BaseEntity**: Generates UUID primary keys and maintains `created_at` and `updated_at` timestamps.
- **SRS Invariant**: There is **no exam-ID** concept. All assessments are identified strictly by their UUID.

---

## 🚀 Implemented Modules & Endpoints

### Module 0: Common Foundation
- Standardized `ApiResponse<T>` wrapper for all successful responses.
- Standardized `ApiErrorResponse` with custom `errorCode` for all errors.
- `GlobalExceptionHandler` handling validation, resource conflicts (409), not found (404), forbidden (403), type mismatch, and internal errors.
- Seeded dev accounts automatically created on startup:
  - Recruiter: `recruiter@example.com` (password: `password123`)
  - Candidate 1: `rahul@example.com` (password: `password123`)
  - Candidate 2: `priya@example.com` (password: `password123`)

---

### Module 1: Workspace Management

| Method | Endpoint | Description | Status Code |
|:---|:---|:---|:---|
| `POST` | `/api/v1/workspaces` | Create a new recruiter workspace | `201 Created` |
| `GET` | `/api/v1/workspaces` | List all workspaces | `200 OK` |
| `GET` | `/api/v1/workspaces/{workspaceId}` | Get workspace details | `200 OK` |
| `PUT` | `/api/v1/workspaces/{workspaceId}` | Update workspace name / description | `200 OK` |
| `DELETE` | `/api/v1/workspaces/{workspaceId}` | Delete workspace (cascades candidates & assessments) | `200 OK` |
| `GET` | `/api/v1/workspaces/{workspaceId}/candidates` | List candidates enrolled in workspace | `200 OK` |
| `POST` | `/api/v1/workspaces/{workspaceId}/candidates` | Add registered candidate by email | `201 Created` |
| `DELETE` | `/api/v1/workspaces/{workspaceId}/candidates/{candidateId}` | Remove candidate from workspace | `200 OK` |

---

### Module 2: Candidate Management (Recruiter-Side)

| Method | Endpoint | Description | Status Code |
|:---|:---|:---|:---|
| `GET` | `/api/v1/candidates/search?email={email}` | Search candidate by unique email | `200 OK` / `404` |
| `GET` | `/api/v1/candidates/{candidateId}` | Get candidate profile | `200 OK` |
| `GET` | `/api/v1/candidates/{candidateId}/workspaces` | List all workspaces candidate belongs to | `200 OK` |
| `GET` | `/api/v1/candidates/{candidateId}/assessments` | List candidate's assessment history & scores | `200 OK` |

---

### Module 3: Assessment Lifecycle & Scheduling

| Method | Endpoint | Description | Status Code |
|:---|:---|:---|:---|
| `POST` | `/api/v1/workspaces/{workspaceId}/assessments` | Create assessment (repo config + schedule) | `201 Created` |
| `GET` | `/api/v1/workspaces/{workspaceId}/assessments` | List assessments in a workspace | `200 OK` |
| `GET` | `/api/v1/assessments/{assessmentId}` | Get assessment configuration details | `200 OK` |
| `PUT` | `/api/v1/assessments/{assessmentId}` | Update assessment config (before started) | `200 OK` |
| `POST` | `/api/v1/assessments/{assessmentId}/cancel` | Cancel an assessment | `200 OK` |
| `GET` | `/api/v1/assessments/{assessmentId}/processing-status` | Get AI pipeline stage progress | `200 OK` |
| `POST` | `/api/v1/assessments/{assessmentId}/start` | Candidate starts assessment (validates time window) | `200 OK` |
| `POST` | `/api/v1/assessments/{assessmentId}/submit` | Candidate submits assessment (transitions to `EVALUATING`) | `200 OK` |

---

## 📋 API Conventions & Error Handling

### 1. Success Envelope Format
```json
{
  "success": true,
  "message": "Request completed successfully",
  "data": { ... },
  "timestamp": "2026-08-19T12:00:00Z"
}
```

### 2. Error Envelope Format
```json
{
  "success": false,
  "message": "Candidate has not registered yet. Ask the candidate to create an account.",
  "errorCode": "CANDIDATE_NOT_FOUND",
  "timestamp": "2026-08-19T12:00:00Z"
}
```

### 3. Common Error Codes
- `VALIDATION_ERROR` (400) — Malformed request body or invalid parameters
- `CANDIDATE_NOT_FOUND` (404) — Searched candidate email does not exist
- `WORKSPACE_NOT_FOUND` (404) — Workspace does not exist
- `ASSESSMENT_NOT_FOUND` (404) — Assessment does not exist
- `DUPLICATE_RESOURCE` (409) — Candidate already added to workspace
- `ASSESSMENT_NOT_AVAILABLE` (409) — Attempted to start assessment outside scheduled start/end window
- `ASSESSMENT_ALREADY_SUBMITTED` (409) — Attempted to start/submit an already completed assessment
- `FORBIDDEN` (403) — Resource access violation

---

## 🧪 Testing & Verification

An automated PowerShell test script is included in the project root to run a complete end-to-end verification of all endpoints against the live database:

```powershell
# From the project root
powershell -ExecutionPolicy Bypass -File .\test_api.ps1
```

**Results:** 22/22 Automated Tests Passing (100% pass rate).

---

## ⚙️ How to Run

### Option 1: Docker (Recommended)
```bash
# From project root
docker compose up --build
```
- Backend will be available at: `http://localhost:8080`
- PostgreSQL will be running on: `localhost:5432`

### Option 2: Local Development
Ensure PostgreSQL is running locally on port 5432 (`evidence_db`), then:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
