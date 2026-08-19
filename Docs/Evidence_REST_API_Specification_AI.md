# Evidence — REST API Specification

**Version:** 1.0  
**Status:** MVP API Contract  
**Audience:** Backend engineers, frontend engineers, AI/LLM agents, test engineers  
**Base URL:** `/api/v1`

> This document is a machine-oriented representation of the Evidence REST API specification. It preserves the API contract from the source specification, including endpoint purpose, access rules, request/response examples, authorization boundaries, error codes, and implementation notes. fileciteturn1file0L7-L21

---

# 1. Product/API Context

Evidence is a recruiter and candidate technical assessment platform.

Core workflow:

```text
Recruiter
  → creates workspace
  → finds existing candidate by email
  → adds candidate to workspace
  → creates assessment
  → supplies repository URL + branch + backend root
  → selects difficulty + duration + fixed schedule
  → system asynchronously analyzes repository
  → AI generates feature + hidden HTTP tests
  → assessment becomes ready/scheduled

Candidate
  → registers/logs in separately
  → sees scheduled/completed assessments
  → starts assessment during allowed window
  → opens browser coding environment
  → reads feature specification
  → edits project
  → runs application in Docker
  → views logs/errors
  → submits
  → hidden HTTP tests execute
  → result/score is produced
```

There is **no exam-ID creation or exam-ID sharing workflow**. Candidate access is through the authenticated candidate dashboard and assignment/schedule. fileciteturn1file0L13-L21

---

# 2. Global API Rules

## 2.1 Base URL

```text
/api/v1
```

## 2.2 Content Type

Requests and responses use JSON unless explicitly stated otherwise.

```http
Content-Type: application/json
```

## 2.3 Authentication

Recruiter and candidate have separate authentication flows.

The exact authentication mechanism is left to implementation, but the contract assumes secure authenticated sessions, such as HTTP-only cookies/session authentication. fileciteturn1file0L8-L12

## 2.4 Authorization

### Recruiter

A recruiter can access only:

- their own workspaces
- candidates within their allowed recruiter scope
- assessments belonging to those workspaces/candidates
- reports for their assessments

### Candidate

A candidate can access only:

- their own candidate dashboard
- assessments assigned to them
- their own assessment workspace during the permitted assessment lifecycle/window
- their own completed results

fileciteturn1file0L13-L17

## 2.5 Candidate Account Rule

Candidates register independently.

The recruiter:

- can search for an existing candidate by email
- can add an existing candidate to a workspace
- **cannot create a candidate account**

If the email does not correspond to an existing candidate, the recruiter cannot proceed through the workspace-add API. fileciteturn1file0L15-L17

## 2.6 Assessment Schedule

Assessment access is controlled by:

```text
scheduled_start_at
scheduled_end_at
```

Candidate start is allowed only when the assessment is within its scheduled availability window. fileciteturn1file0L18-L20

## 2.7 Hidden Test Security

Candidate-facing APIs must never expose:

- hidden test cases
- expected responses
- evaluator assertions
- evaluator implementation details

fileciteturn1file0L19-L21

## 2.8 Asynchronous Processing

Repository cloning, repository analysis, AI feature generation, and hidden-test generation are long-running operations and should be processed asynchronously. fileciteturn1file0L20-L21

---

# 3. Common Response Contract

## 3.1 Success

```json
{
  "success": true,
  "message": "Request completed successfully",
  "data": {},
  "timestamp": "2026-08-19T10:30:00Z"
}
```

## 3.2 Error

```json
{
  "success": false,
  "message": "Candidate not found",
  "errorCode": "CANDIDATE_NOT_FOUND",
  "timestamp": "2026-08-19T10:30:00Z"
}
```

These common response shapes are defined in the source API specification. fileciteturn1file0L22-L35

---

# 4. Authentication Module

## 4.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/auth/recruiter/signup` | Public | Create recruiter account |
| `POST` | `/auth/recruiter/login` | Public | Authenticate recruiter |
| `POST` | `/auth/recruiter/logout` | Recruiter | End recruiter session |
| `GET` | `/auth/recruiter/me` | Recruiter | Return authenticated recruiter profile |
| `POST` | `/auth/candidate/signup` | Public | Create candidate account |
| `POST` | `/auth/candidate/login` | Public | Authenticate candidate |
| `POST` | `/auth/candidate/logout` | Candidate | End candidate session |
| `GET` | `/auth/candidate/me` | Candidate | Return authenticated candidate profile |

fileciteturn1file0L37-L49

## 4.2 Recruiter Sign Up

```http
POST /api/v1/auth/recruiter/signup
```

### Request

```json
{
  "name": "John Recruiter",
  "email": "john@company.com",
  "password": "StrongPassword123"
}
```

### Response

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "John Recruiter",
    "email": "john@company.com",
    "role": "RECRUITER"
  }
}
```

fileciteturn1file0L50-L66

## 4.3 Candidate Sign Up

```http
POST /api/v1/auth/candidate/signup
```

### Request

```json
{
  "name": "Rahul Kumar",
  "email": "rahul@example.com",
  "password": "StrongPassword123"
}
```

### Response

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "Rahul Kumar",
    "email": "rahul@example.com",
    "role": "CANDIDATE"
  }
}
```

fileciteturn1file0L67-L83

---

# 5. Dashboard Module

## 5.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/recruiter/dashboard` | Recruiter | Recruiter statistics |
| `GET` | `/candidate/dashboard` | Candidate | Scheduled and completed assessments |

fileciteturn1file0L84-L88

## 5.2 Recruiter Dashboard

```http
GET /api/v1/recruiter/dashboard
```

### Response

```json
{
  "success": true,
  "data": {
    "workspaceCount": 5,
    "candidateCount": 120,
    "assessmentCount": 240,
    "activeAssessments": 12,
    "completedAssessments": 190
  }
}
```

fileciteturn1file0L89-L102

## 5.3 Candidate Dashboard

```http
GET /api/v1/candidate/dashboard
```

### Response

```json
{
  "success": true,
  "data": {
    "scheduledAssessments": [
      {
        "assessmentId": "uuid",
        "workspaceName": "TCS - REC Placement Drive",
        "scheduledStartAt": "2026-08-25T10:00:00+05:30",
        "scheduledEndAt": "2026-08-25T11:30:00+05:30",
        "difficulty": "INTERMEDIATE",
        "status": "SCHEDULED"
      }
    ],
    "completedAssessments": [
      {
        "assessmentId": "uuid",
        "workspaceName": "ABC Hiring Drive",
        "completedAt": "2026-08-18T12:15:00+05:30",
        "score": 82.5,
        "status": "COMPLETED"
      }
    ]
  }
}
```

The candidate dashboard separates scheduled assessments from completed assessments. fileciteturn1file0L103-L129

---

# 6. Workspace Module

## 6.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/workspaces` | Recruiter | Create workspace |
| `GET` | `/workspaces` | Recruiter | List recruiter's workspaces |
| `GET` | `/workspaces/{workspaceId}` | Recruiter | Get workspace details |
| `PUT` | `/workspaces/{workspaceId}` | Recruiter | Update workspace |
| `DELETE` | `/workspaces/{workspaceId}` | Recruiter | Archive/delete workspace according to lifecycle policy |
| `GET` | `/workspaces/{workspaceId}/candidates` | Recruiter | List candidates in workspace |
| `POST` | `/workspaces/{workspaceId}/candidates` | Recruiter | Add existing candidate by email |
| `DELETE` | `/workspaces/{workspaceId}/candidates/{candidateId}` | Recruiter | Remove candidate from workspace |

fileciteturn1file0L130-L160

## 6.2 Create Workspace

```http
POST /api/v1/workspaces
```

### Request

```json
{
  "name": "TCS - REC Placement Drive",
  "description": "Backend developer placement assessment"
}
```

### Response

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "TCS - REC Placement Drive",
    "description": "Backend developer placement assessment",
    "status": "ACTIVE"
  }
}
```

fileciteturn1file0L161-L176

## 6.3 Add Existing Candidate by Email

```http
POST /api/v1/workspaces/{workspaceId}/candidates
```

### Request

```json
{
  "email": "rahul@example.com"
}
```

### Response

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "workspaceId": "uuid",
    "candidate": {
      "id": "uuid",
      "name": "Rahul Kumar",
      "email": "rahul@example.com"
    }
  }
}
```

### Candidate-not-found behavior

If the candidate does not exist:

```http
404
```

```text
CANDIDATE_NOT_FOUND
```

The recruiter cannot create the candidate account from this API. fileciteturn1file0L177-L195

---

# 7. Candidate Management Module

## 7.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/candidates/search?email={email}` | Recruiter | Find existing candidate by unique email |
| `GET` | `/candidates/{candidateId}` | Recruiter | Get candidate profile and recruiter-visible assessment summary |
| `GET` | `/candidates/{candidateId}/workspaces` | Recruiter | List workspaces containing candidate |
| `GET` | `/candidates/{candidateId}/assessments` | Recruiter | List candidate assessments within recruiter scope |

fileciteturn1file0L197-L219

## 7.2 Search Candidate

```http
GET /api/v1/candidates/search?email=rahul@example.com
```

### Response

```http
200 OK
```

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "Rahul Kumar",
    "email": "rahul@example.com",
    "role": "CANDIDATE"
  }
}
```

No recruiter-side candidate-creation endpoint exists. fileciteturn1file0L220-L232

---

# 8. Assessment Module

## 8.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/workspaces/{workspaceId}/assessments` | Recruiter | Create assessment for existing workspace candidate |
| `GET` | `/workspaces/{workspaceId}/assessments` | Recruiter | List workspace assessments |
| `GET` | `/assessments/{assessmentId}` | Recruiter/Candidate | Get assessment details subject to ownership/assignment |
| `PUT` | `/assessments/{assessmentId}` | Recruiter | Update assessment configuration before lock |
| `POST` | `/assessments/{assessmentId}/cancel` | Recruiter | Cancel assessment before candidate completion |
| `GET` | `/assessments/{assessmentId}/processing-status` | Recruiter | View asynchronous preparation status |
| `POST` | `/assessments/{assessmentId}/start` | Candidate | Start assessment when within scheduled window |
| `POST` | `/assessments/{assessmentId}/run` | Candidate | Build/run current workspace |
| `POST` | `/assessments/{assessmentId}/stop` | Candidate | Stop running application |
| `POST` | `/assessments/{assessmentId}/submit` | Candidate | Submit final implementation |

fileciteturn1file0L233-L284

## 8.2 Create Assessment

```http
POST /api/v1/workspaces/{workspaceId}/assessments
```

### Request

```json
{
  "candidateId": "candidate-uuid",
  "repositoryUrl": "https://github.com/example/notes-app.git",
  "branchName": "main",
  "backendRootDirectory": "backend",
  "difficulty": "INTERMEDIATE",
  "durationMinutes": 90,
  "scheduledStartAt": "2026-08-25T10:00:00+05:30",
  "scheduledEndAt": "2026-08-25T11:30:00+05:30"
}
```

### Response

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "assessmentId": "uuid",
    "candidateId": "uuid",
    "workspaceId": "uuid",
    "status": "CREATING",
    "scheduledStartAt": "2026-08-25T10:00:00+05:30",
    "scheduledEndAt": "2026-08-25T11:30:00+05:30"
  }
}
```

fileciteturn1file0L285-L308

## 8.3 Processing Status

```http
GET /api/v1/assessments/{assessmentId}/processing-status
```

### Response

```json
{
  "success": true,
  "data": {
    "assessmentId": "uuid",
    "status": "GENERATING_TESTS",
    "stages": [
      {
        "name": "CLONING",
        "status": "COMPLETED"
      },
      {
        "name": "REPOSITORY_ANALYSIS",
        "status": "COMPLETED"
      },
      {
        "name": "FEATURE_GENERATION",
        "status": "COMPLETED"
      },
      {
        "name": "TEST_GENERATION",
        "status": "RUNNING"
      }
    ]
  }
}
```

fileciteturn1file0L309-L325

---

# 9. Repository Analysis Module

## 9.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/assessments/{assessmentId}/repository-analysis` | Recruiter | View structured repository analysis |
| `GET` | `/assessments/{assessmentId}/repository-analysis/status` | Recruiter | View analysis status |

fileciteturn1file0L326-L338

## 9.2 Get Repository Analysis

```http
GET /api/v1/assessments/{assessmentId}/repository-analysis
```

### Response

```json
{
  "success": true,
  "data": {
    "analysisStatus": "COMPLETED",
    "projectStructure": {
      "folders": [],
      "files": []
    },
    "sourceCodeStructure": {
      "controllers": [],
      "services": [],
      "repositories": [],
      "entities": []
    },
    "contentDetails": {
      "endpoints": [],
      "entityFields": [],
      "serviceMethods": []
    }
  }
}
```

fileciteturn1file0L339-L358

---

# 10. Feature Specification Module

## 10.1 Endpoint

```http
GET /api/v1/assessments/{assessmentId}/feature
```

### Access

- Recruiter: own assessment
- Candidate: assigned assessment

### Response

```json
{
  "success": true,
  "data": {
    "title": "Add Search API",
    "description": "Implement search functionality for notes.",
    "requirements": [
      "Support keyword search",
      "Return matching notes"
    ],
    "endpoint": "/api/notes/search",
    "httpMethod": "GET",
    "requestSpecification": {
      "queryParameters": {
        "keyword": "string"
      }
    },
    "responseSpecification": {
      "status": 200,
      "body": {
        "items": "array"
      }
    },
    "constraints": []
  }
}
```

The feature specification may be shown to the candidate.

The following must not be returned:

```text
hidden test cases
expected responses
evaluator assertions
```

fileciteturn1file0L359-L387

---

# 11. Assessment Workspace / File Explorer Module

## 11.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/assessments/{assessmentId}/files` | Candidate | Get candidate workspace file tree |
| `GET` | `/assessments/{assessmentId}/files/content?path={path}` | Candidate | Read a file |
| `PUT` | `/assessments/{assessmentId}/files/content` | Candidate | Save modified file |
| `GET` | `/assessments/{assessmentId}/logs` | Candidate | Get build/application logs |

fileciteturn1file0L388-L405

## 11.2 File Tree

```http
GET /api/v1/assessments/{assessmentId}/files
```

### Response

```json
{
  "success": true,
  "data": {
    "root": {
      "name": "backend",
      "type": "DIRECTORY",
      "children": [
        {
          "name": "src",
          "type": "DIRECTORY",
          "children": []
        },
        {
          "name": "pom.xml",
          "type": "FILE"
        }
      ]
    }
  }
}
```

fileciteturn1file0L406-L421

## 11.3 Read File

```http
GET /api/v1/assessments/{assessmentId}/files/content?path=src/main/java/com/example/NotesController.java
```

### Response

```json
{
  "success": true,
  "data": {
    "path": "src/main/java/com/example/NotesController.java",
    "content": "package com.example; ..."
  }
}
```

fileciteturn1file0L422-L432

## 11.4 Save File

```http
PUT /api/v1/assessments/{assessmentId}/files/content
```

### Request

```json
{
  "path": "src/main/java/com/example/NotesController.java",
  "content": "package com.example; ...modified source..."
}
```

### Response

```http
200 OK
```

```json
{
  "success": true,
  "message": "File saved"
}
```

fileciteturn1file0L433-L443

---

# 12. Application Execution Module

## 12.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/assessments/{assessmentId}/run` | Candidate | Build/run candidate workspace |
| `POST` | `/assessments/{assessmentId}/stop` | Candidate | Stop candidate application |
| `GET` | `/assessments/{assessmentId}/execution/status` | Candidate | Get build/container/application status |
| `GET` | `/assessments/{assessmentId}/execution/logs` | Candidate | Stream/read execution logs |

fileciteturn1file0L444-L465

## 12.2 Run

```http
POST /api/v1/assessments/{assessmentId}/run
```

### Response

```json
{
  "success": true,
  "data": {
    "executionId": "uuid",
    "status": "STARTING"
  }
}
```

## 12.3 Execution Status

```http
GET /api/v1/assessments/{assessmentId}/execution/status
```

### Response

```json
{
  "success": true,
  "data": {
    "executionId": "uuid",
    "buildStatus": "SUCCESS",
    "containerStatus": "RUNNING",
    "applicationStatus": "STARTED"
  }
}
```

fileciteturn1file0L466-L486

---

# 13. Evaluation Module

## 13.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/assessments/{assessmentId}/submit` | Candidate | Submit final implementation |
| `GET` | `/assessments/{assessmentId}/result` | Candidate | Get candidate-visible result |
| `GET` | `/assessments/{assessmentId}/report` | Recruiter | Get complete recruiter report |
| `GET` | `/assessments/{assessmentId}/test-results` | Recruiter | Get individual test results |

fileciteturn1file0L487-L502

## 13.2 Submit Assessment

```http
POST /api/v1/assessments/{assessmentId}/submit
```

### Response

```json
{
  "success": true,
  "data": {
    "submissionId": "uuid",
    "status": "EVALUATING"
  }
}
```

## 13.3 Candidate Result

```http
GET /api/v1/assessments/{assessmentId}/result
```

### Response

```json
{
  "success": true,
  "data": {
    "assessmentId": "uuid",
    "score": 82.5,
    "status": "COMPLETED",
    "totalTests": 10,
    "passedTests": 8,
    "failedTests": 2
  }
}
```

## 13.4 Recruiter Report

```http
GET /api/v1/assessments/{assessmentId}/report
```

### Response

```json
{
  "success": true,
  "data": {
    "assessmentId": "uuid",
    "candidate": {
      "id": "uuid",
      "name": "Rahul Kumar",
      "email": "rahul@example.com"
    },
    "score": 82.5,
    "totalTests": 10,
    "passedTests": 8,
    "failedTests": 2,
    "buildStatus": "SUCCESS",
    "applicationStatus": "STARTED",
    "timeTakenSeconds": 4200,
    "status": "COMPLETED"
  }
}
```

## 13.5 Recruiter Test Results

```http
GET /api/v1/assessments/{assessmentId}/test-results
```

### Response

```json
{
  "success": true,
  "data": [
    {
      "testCaseNumber": 1,
      "status": "PASSED",
      "actualStatusCode": 200,
      "executionTimeMs": 85
    },
    {
      "testCaseNumber": 2,
      "status": "FAILED",
      "actualStatusCode": 400,
      "failureReason": "Expected status 200"
    }
  ]
}
```

The source document notes that visibility of `expected_response` to recruiter-facing UI is a policy decision and may be restricted to internal/admin tooling. It must not be exposed to candidate-facing APIs. fileciteturn1file0L503-L571

---

# 14. Reports Module

## 14.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/reports` | Recruiter | List recruiter assessment reports with filters |
| `GET` | `/reports/{reportId}` | Recruiter | Get a specific report |
| `GET` | `/reports/summary` | Recruiter | Aggregate recruiter report statistics |

fileciteturn1file0L572-L578

## 14.2 List Reports

```http
GET /api/v1/reports?workspaceId=uuid&status=COMPLETED&page=0&size=20
```

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "assessmentId": "uuid",
        "candidateName": "Rahul Kumar",
        "score": 82.5,
        "status": "COMPLETED"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  }
}
```

fileciteturn1file0L579-L597

---

# 15. Selected Candidates Module

## 15.1 Endpoint Registry

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/selected-candidates` | Recruiter | List candidates selected/managed by recruiter |
| `GET` | `/selected-candidates/{candidateId}` | Recruiter | Get candidate assessment summary |

fileciteturn1file0L598-L606

## 15.2 Candidate Summary

```http
GET /api/v1/selected-candidates/{candidateId}
```

### Response

```json
{
  "success": true,
  "data": {
    "candidate": {
      "id": "uuid",
      "name": "Rahul Kumar",
      "email": "rahul@example.com"
    },
    "workspaces": [
      {
        "workspaceId": "uuid",
        "workspaceName": "TCS - REC Placement Drive",
        "assessmentCount": 3,
        "completedCount": 2
      }
    ]
  }
}
```

fileciteturn1file0L607-L626

---

# 16. Standard Error Contract

| HTTP | Error Code | Meaning |
|---|---|---|
| `400` | `VALIDATION_ERROR` | Request data is invalid |
| `401` | `UNAUTHENTICATED` | Authentication required or session invalid |
| `403` | `FORBIDDEN` | Authenticated user lacks permission |
| `404` | `CANDIDATE_NOT_FOUND` | No candidate exists for provided email |
| `404` | `ASSESSMENT_NOT_FOUND` | Assessment does not exist or is inaccessible |
| `409` | `DUPLICATE_RESOURCE` | Resource already exists / duplicate workspace membership |
| `409` | `ASSESSMENT_NOT_AVAILABLE` | Assessment is outside scheduled window or unavailable |
| `409` | `ASSESSMENT_ALREADY_SUBMITTED` | Assessment already submitted |
| `422` | `INVALID_ASSESSMENT_CONFIGURATION` | Assessment configuration cannot be processed |
| `500` | `INTERNAL_ERROR` | Unexpected server error |
| `502` | `AI_PROCESSING_ERROR` | AI feature/test generation failed |
| `503` | `EXECUTION_UNAVAILABLE` | Docker execution environment unavailable |

fileciteturn1file0L627-L654

---

# 17. Authorization Matrix

| Module | Recruiter | Candidate |
|---|---|---|
| Recruiter Authentication | Own account | No |
| Candidate Authentication | No | Own account |
| Recruiter Dashboard | Yes | No |
| Candidate Dashboard | No | Own dashboard |
| Workspace Management | Own workspaces | No |
| Candidate Search | Own recruiter scope | No |
| Assessment Creation | Yes, for workspace candidates | No |
| Repository Analysis | Own assessments | No |
| Feature Specification | Own assessments | Assigned assessment |
| File Explorer / Editor | No | Assigned assessment during allowed window |
| Run/Stop | No | Assigned assessment |
| Submit | No | Assigned assessment |
| Candidate Result | No | Own completed assessment |
| Recruiter Report | Own assessments | No |

fileciteturn1file0L655-L671

---

# 18. End-to-End API Sequences

## 18.1 Recruiter Flow

```text
POST /auth/recruiter/login
        ↓
GET /recruiter/dashboard
        ↓
POST /workspaces
        ↓
GET /candidates/search?email=...
        ↓
POST /workspaces/{workspaceId}/candidates
        ↓
POST /workspaces/{workspaceId}/assessments
        ↓
GET /assessments/{assessmentId}/processing-status
        ↓
Assessment becomes ready/scheduled
        ↓
Candidate completes assessment
        ↓
GET /assessments/{assessmentId}/report
```

## 18.2 Candidate Flow

```text
POST /auth/candidate/login
        ↓
GET /candidate/dashboard
        ↓
POST /assessments/{assessmentId}/start
        ↓
GET /assessments/{assessmentId}/files
        ↓
GET /assessments/{assessmentId}/files/content?path=...
        ↓
PUT /assessments/{assessmentId}/files/content
        ↓
GET /assessments/{assessmentId}/feature
        ↓
POST /assessments/{assessmentId}/run
        ↓
GET /assessments/{assessmentId}/execution/status
        ↓
GET /assessments/{assessmentId}/execution/logs
        ↓
POST /assessments/{assessmentId}/stop
        ↓
POST /assessments/{assessmentId}/submit
        ↓
GET /assessments/{assessmentId}/result
```

The source document defines these sequences explicitly. fileciteturn1file0L672-L696

---

# 19. Implementation Rules

## 19.1 Authentication

The exact session/authentication technology is implementation-specific. The API contract assumes authenticated sessions. fileciteturn1file0L697-L699

## 19.2 Candidate Dashboard

The frontend should derive scheduled vs completed assessment views using:

```text
assessment.status
scheduled_start_at
scheduled_end_at
```

fileciteturn1file0L700-L701

## 19.3 Assessment Creation

Assessment creation should return before the full repository/AI pipeline completes.

Recommended architecture:

```text
POST assessment
      ↓
201 / immediate acknowledgement
      ↓
background job
      ├── clone repository
      ├── analyze repository
      ├── generate feature
      └── generate hidden tests
```

fileciteturn1file0L702-L703

## 19.4 File Security

File APIs must:

- validate paths
- prevent path traversal
- restrict access to the candidate's assessment workspace

fileciteturn1file0L704-L705

## 19.5 Run / Stop

Run/stop operations should be idempotent where practical to avoid conflicting execution states caused by repeated UI requests. fileciteturn1file0L706-L707

## 19.6 Hidden Test Security

Evaluation uses server-side `TEST_CASE` data.

Candidate-facing APIs must never retrieve hidden expected responses. fileciteturn1file0L708-L709

## 19.7 Repository Paths

Recruiter-visible repository configuration:

```text
repositoryUrl
branchName
backendRootDirectory
```

Physical repository paths are internal execution/storage data and should not be exposed to the candidate. fileciteturn1file0L710-L711

## 19.8 Future API Extensions

The source specification identifies these as possible later additions:

- pagination
- filtering
- sorting
- rate limits
- WebSocket/SSE log streaming

fileciteturn1file0L712-L713

---

# 20. Machine-Readable API Rules for AI Agents

An AI agent implementing or reviewing this system should follow these rules:

```text
RULE 1:
Base path = /api/v1

RULE 2:
Recruiter and candidate authentication are separate.

RULE 3:
Candidate accounts must already exist before recruiter selection.

RULE 4:
Recruiter searches candidate by email.

RULE 5:
Recruiter cannot create candidate accounts.

RULE 6:
Candidate can belong to assigned workspaces/assessments only.

RULE 7:
Candidate can have multiple assessments.

RULE 8:
There is no exam ID.

RULE 9:
Assessment availability is controlled by scheduled_start_at and scheduled_end_at.

RULE 10:
Repository analysis is asynchronous.

RULE 11:
AI feature/test generation is asynchronous.

RULE 12:
Candidate may see feature specification.

RULE 13:
Candidate must never see hidden tests, expected responses, or evaluator assertions.

RULE 14:
File APIs are restricted to the candidate's assessment workspace.

RULE 15:
Run/stop controls execute the candidate application in the assessment environment.

RULE 16:
Submit starts evaluation.

RULE 17:
Candidate result contains aggregate score/result information.

RULE 18:
Recruiter report contains assessment/candidate performance data.

RULE 19:
Repository physical paths are internal only.

RULE 20:
All endpoint authorization must be enforced server-side; frontend visibility is not a security boundary.
```

---

# 21. API Surface Summary

```text
AUTH
├── recruiter signup/login/logout/me
└── candidate signup/login/logout/me

DASHBOARD
├── recruiter dashboard
└── candidate dashboard

WORKSPACE
├── create
├── list
├── get
├── update
├── delete/archive
├── list candidates
├── add candidate
└── remove candidate

CANDIDATE
├── search by email
├── get profile
├── list workspaces
└── list assessments

ASSESSMENT
├── create
├── list
├── get
├── update
├── cancel
├── processing status
├── start
├── run
├── stop
└── submit

REPOSITORY ANALYSIS
├── get analysis
└── get analysis status

FEATURE
└── get feature specification

WORKSPACE/FILES
├── file tree
├── read file
├── save file
└── logs

EXECUTION
├── run
├── stop
├── execution status
└── execution logs

EVALUATION
├── submit
├── candidate result
├── recruiter report
└── test results

REPORTS
├── list
├── get
└── summary

SELECTED CANDIDATES
├── list
└── candidate summary
```

---

# 22. Source-of-Truth Note

This Markdown document is a conversion and restructuring of the uploaded **Evidence REST API Specification v1.0**. It preserves the source API's terminology, endpoint set, access model, request/response examples, authorization rules, error contract, and implementation notes rather than introducing a new API design. fileciteturn1file0L2-L5
