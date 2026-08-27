# EVIDENCE — MVP REST API Specification

## Database-Aligned API Contract

This API specification contains **only the requested endpoints**.

The request and response contracts are aligned with the finalized EVIDENCE PostgreSQL database design:

- `USER`
- `WORKSPACE`
- `WORKSPACE_CANDIDATE`
- `ASSESSMENT`
- `REPOSITORY_ANALYSIS`
- `ASSESSMENT_WORKSPACE`
- `FEATURE_SPECIFICATION`
- `TEST_CASE`
- `SUBMISSION`
- `EVALUATION_REPORT`
- `TEST_RESULT`

### API conventions

- **Base URL:** `/api/v1`
- Request and response bodies use JSON.
- Authentication is assumed to be handled by the selected authentication mechanism.
- The authenticated recruiter can access only resources within their ownership scope.
- The authenticated candidate can access only assessments assigned to them.
- Candidate accounts must already exist before they can be added to a workspace.
- Candidate email is unique in `USER.email` and is used to find an existing candidate.
- `ASSESSMENT` has no `exam_id`.
- Long-running assessment preparation is asynchronous.
- Hidden test cases, expected responses, and evaluator assertions must not be returned by candidate-facing endpoints.

---

# Common Response Format

## Success

```json
{
  "success": true,
  "message": "Request completed successfully",
  "data": {},
  "timestamp": "2026-08-25T10:30:00Z"
}
```

## Error

```json
{
  "success": false,
  "message": "Resource not found",
  "errorCode": "RESOURCE_NOT_FOUND",
  "timestamp": "2026-08-25T10:30:00Z"
}
```

---

# Module 2 — Dashboard

| Method | Endpoint | Access | Use Case |
|---|---|---|---|
| GET | `/recruiter/dashboard` | Recruiter | Get recruiter statistics |
| GET | `/candidate/dashboard` | Candidate | Get candidate's scheduled and completed assessments |

---

## 2.1 Get Recruiter Dashboard

### Endpoint

```http
GET /api/v1/recruiter/dashboard
```

### Authentication

`RECRUITER`

### Database sources

The response is derived from:

- `WORKSPACE`
- `WORKSPACE_CANDIDATE`
- `ASSESSMENT`

The recruiter scope is determined from the authenticated user's `USER.id`, which matches `WORKSPACE.recruiter_id`.

### Request

No request body.

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
  },
  "timestamp": "2026-08-25T10:30:00Z"
}
```

### Database mapping

```text
workspaceCount
    → COUNT(WORKSPACE.id)
      WHERE WORKSPACE.recruiter_id = authenticatedUser.id

candidateCount
    → COUNT(DISTINCT WORKSPACE_CANDIDATE.candidate_id)
      through recruiter-owned WORKSPACE rows

assessmentCount
    → COUNT(ASSESSMENT.id)
      through recruiter-owned WORKSPACE rows

activeAssessments
    → ASSESSMENT rows in the application's active lifecycle statuses

completedAssessments
    → ASSESSMENT rows in the application's completed lifecycle status
```

> The exact ENUM values for `WORKSPACE.status` and `ASSESSMENT.status` are intentionally not fixed by the database design and must be defined during implementation.

---

## 2.2 Get Candidate Dashboard

### Endpoint

```http
GET /api/v1/candidate/dashboard
```

### Authentication

`CANDIDATE`

### Database sources

- `USER`
- `WORKSPACE`
- `ASSESSMENT`
- `SUBMISSION`
- `EVALUATION_REPORT`

### Request

No request body.

The candidate is identified from the authenticated `USER.id`.

### Response

```json
{
  "success": true,
  "data": {
    "scheduledAssessments": [
      {
        "assessmentId": "uuid",
        "workspaceId": "uuid",
        "workspaceName": "TCS - Backend Hiring",
        "scheduledStartAt": "2026-08-25T10:00:00+05:30",
        "scheduledEndAt": "2026-08-25T11:30:00+05:30",
        "difficulty": "INTERMEDIATE",
        "status": "SCHEDULED"
      }
    ],
    "completedAssessments": [
      {
        "assessmentId": "uuid",
        "workspaceId": "uuid",
        "workspaceName": "ABC Hiring Drive",
        "submittedAt": "2026-08-18T12:15:00+05:30",
        "timeTakenSeconds": 4200,
        "score": 82.5,
        "status": "COMPLETED"
      }
    ]
  },
  "timestamp": "2026-08-25T10:30:00Z"
}
```

### Database mapping

```text
assessmentId
    → ASSESSMENT.id

workspaceId
    → ASSESSMENT.workspace_id

workspaceName
    → WORKSPACE.name

scheduledStartAt
    → ASSESSMENT.scheduled_start_at

scheduledEndAt
    → ASSESSMENT.scheduled_end_at

difficulty
    → ASSESSMENT.difficulty

assessment status
    → ASSESSMENT.status

submittedAt
    → SUBMISSION.submitted_at

timeTakenSeconds
    → SUBMISSION.time_taken_seconds

score
    → EVALUATION_REPORT.score
```

Completed assessment information should be joined through:

```text
ASSESSMENT
    → SUBMISSION
    → EVALUATION_REPORT
```

---

# Module 3 — Workspace

| Method | Endpoint | Access | Use Case |
|---|---|---|---|
| POST | `/workspaces` | Recruiter | Create workspace |
| GET | `/workspaces` | Recruiter | List recruiter's workspaces |
| GET | `/workspaces/{workspaceId}` | Recruiter | Get workspace details |
| PUT | `/workspaces/{workspaceId}` | Recruiter | Update workspace |
| DELETE | `/workspaces/{workspaceId}` | Recruiter | Archive/delete workspace |
| GET | `/workspaces/{workspaceId}/candidates` | Recruiter | List candidates in workspace |
| POST | `/workspaces/{workspaceId}/candidates` | Recruiter | Add existing candidate by email |
| DELETE | `/workspaces/{workspaceId}/candidates/{candidateId}` | Recruiter | Remove candidate from workspace |

---

## 3.1 Create Workspace

### Endpoint

```http
POST /api/v1/workspaces
```

### Authentication

`RECRUITER`

### Database table

`WORKSPACE`

### Request

```json
{
  "name": "TCS - Backend Hiring",
  "description": "Backend developer technical assessment"
}
```

### Server-controlled fields

The API must derive these fields rather than accept them from the client:

```text
id
recruiter_id
status
created_at
updated_at
```

`recruiter_id` comes from the authenticated `USER.id`.

### Response

```json
{
  "success": true,
  "data": {
    "id": "workspace-uuid",
    "recruiterId": "recruiter-uuid",
    "name": "TCS - Backend Hiring",
    "description": "Backend developer technical assessment",
    "status": "ACTIVE",
    "createdAt": "2026-08-25T10:30:00Z",
    "updatedAt": "2026-08-25T10:30:00Z"
  },
  "timestamp": "2026-08-25T10:30:00Z"
}
```

---

## 3.2 List Recruiter's Workspaces

### Endpoint

```http
GET /api/v1/workspaces
```

### Authentication

`RECRUITER`

### Database table

`WORKSPACE`

### Request

No request body.

### Response

```json
{
  "success": true,
  "data": [
    {
      "id": "workspace-uuid",
      "recruiterId": "recruiter-uuid",
      "name": "TCS - Backend Hiring",
      "description": "Backend developer technical assessment",
      "status": "ACTIVE",
      "createdAt": "2026-08-25T10:30:00Z",
      "updatedAt": "2026-08-25T10:30:00Z"
    }
  ],
  "timestamp": "2026-08-25T10:30:00Z"
}
```

Only rows satisfying:

```text
WORKSPACE.recruiter_id = authenticatedUser.id
```

are returned.

---

## 3.3 Get Workspace Details

### Endpoint

```http
GET /api/v1/workspaces/{workspaceId}
```

### Authentication

`RECRUITER`

### Database sources

- `WORKSPACE`
- `WORKSPACE_CANDIDATE`
- `USER`
- `ASSESSMENT`

### Request

No request body.

### Response

```json
{
  "success": true,
  "data": {
    "id": "workspace-uuid",
    "recruiterId": "recruiter-uuid",
    "name": "TCS - Backend Hiring",
    "description": "Backend developer technical assessment",
    "status": "ACTIVE",
    "createdAt": "2026-08-25T10:30:00Z",
    "updatedAt": "2026-08-25T10:30:00Z",
    "candidates": [
      {
        "id": "candidate-uuid",
        "name": "Rahul Kumar",
        "email": "rahul@example.com",
        "addedAt": "2026-08-25T10:35:00Z"
      }
    ],
    "assessmentCount": 3
  },
  "timestamp": "2026-08-25T10:30:00Z"
}
```

### Relationship traversal

```text
WORKSPACE
    ↓
WORKSPACE_CANDIDATE
    ↓
USER

WORKSPACE
    ↓
ASSESSMENT
```

---

## 3.4 Update Workspace

### Endpoint

```http
PUT /api/v1/workspaces/{workspaceId}
```

### Authentication

`RECRUITER`

### Database table

`WORKSPACE`

### Request

Only mutable workspace fields should be accepted.

```json
{
  "name": "TCS - Backend Developer Hiring",
  "description": "Updated backend technical assessment"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": "workspace-uuid",
    "recruiterId": "recruiter-uuid",
    "name": "TCS - Backend Developer Hiring",
    "description": "Updated backend technical assessment",
    "status": "ACTIVE",
    "createdAt": "2026-08-25T10:30:00Z",
    "updatedAt": "2026-08-25T11:00:00Z"
  },
  "timestamp": "2026-08-25T11:00:00Z"
}
```

The client must not modify:

```text
id
recruiter_id
created_at
```

---

## 3.5 Archive/Delete Workspace

### Endpoint

```http
DELETE /api/v1/workspaces/{workspaceId}
```

### Authentication

`RECRUITER`

### Database consideration

The database design contains `WORKSPACE.status` but does not define a specific deletion/archival ENUM value.

Therefore the implementation must choose one lifecycle policy:

1. Soft archive by changing `WORKSPACE.status`, or
2. Hard delete the workspace and apply the configured foreign-key delete actions.

### Request

No request body.

### Response — lifecycle archive

```json
{
  "success": true,
  "message": "Workspace archived",
  "data": {
    "id": "workspace-uuid",
    "status": "ARCHIVED"
  },
  "timestamp": "2026-08-25T11:30:00Z"
}
```

> `ARCHIVED` is an example application ENUM value. The finalized database document explicitly leaves exact ENUM values for implementation.

---

## 3.6 List Candidates in Workspace

### Endpoint

```http
GET /api/v1/workspaces/{workspaceId}/candidates
```

### Authentication

`RECRUITER`

### Database sources

- `WORKSPACE_CANDIDATE`
- `USER`

### Response

```json
{
  "success": true,
  "data": [
    {
      "workspaceId": "workspace-uuid",
      "candidate": {
        "id": "candidate-uuid",
        "name": "Rahul Kumar",
        "email": "rahul@example.com",
        "role": "CANDIDATE"
      },
      "createdAt": "2026-08-25T10:35:00Z"
    }
  ],
  "timestamp": "2026-08-25T10:30:00Z"
}
```

### Database relationship

```text
WORKSPACE_CANDIDATE.workspace_id
    → WORKSPACE.id

WORKSPACE_CANDIDATE.candidate_id
    → USER.id
```

---

## 3.7 Add Existing Candidate by Email

### Endpoint

```http
POST /api/v1/workspaces/{workspaceId}/candidates
```

### Authentication

`RECRUITER`

### Database sources

- `USER`
- `WORKSPACE_CANDIDATE`

### Request

```json
{
  "email": "rahul@example.com"
}
```

### Processing

1. Find `USER` using the unique `email`.
2. Verify `USER.role = CANDIDATE`.
3. Verify the candidate is not already a member of the workspace.
4. Insert:

```text
WORKSPACE_CANDIDATE(
    workspace_id,
    candidate_id,
    created_at
)
```

### Response

```json
{
  "success": true,
  "data": {
    "workspaceId": "workspace-uuid",
    "candidate": {
      "id": "candidate-uuid",
      "name": "Rahul Kumar",
      "email": "rahul@example.com",
      "role": "CANDIDATE"
    },
    "createdAt": "2026-08-25T10:35:00Z"
  },
  "timestamp": "2026-08-25T10:35:00Z"
}
```

### Errors

```text
404 CANDIDATE_NOT_FOUND
409 DUPLICATE_RESOURCE
```

The recruiter cannot create a candidate through this endpoint.

---

## 3.8 Remove Candidate from Workspace

### Endpoint

```http
DELETE /api/v1/workspaces/{workspaceId}/candidates/{candidateId}
```

### Authentication

`RECRUITER`

### Database table

`WORKSPACE_CANDIDATE`

### Request

No request body.

### Response

```json
{
  "success": true,
  "message": "Candidate removed from workspace",
  "data": {
    "workspaceId": "workspace-uuid",
    "candidateId": "candidate-uuid"
  },
  "timestamp": "2026-08-25T11:00:00Z"
}
```

### Important relationship constraint

The membership row is identified by the composite primary key:

```text
(workspace_id, candidate_id)
```

If the candidate already has an `ASSESSMENT` referencing this membership, the implementation must enforce the configured foreign-key deletion policy rather than silently breaking the relationship.

---

# Module 4 — Candidate Management

| Method | Endpoint | Access | Use Case |
|---|---|---|---|
| GET | `/candidates/search?email={email}` | Recruiter | Find existing candidate by email |
| GET | `/candidates/{candidateId}/assessments` | Recruiter | List candidate's assessments within recruiter scope |

---

## 4.1 Search Candidate by Email

### Endpoint

```http
GET /api/v1/candidates/search?email=rahul@example.com
```

### Authentication

`RECRUITER`

### Database table

`USER`

### Request

No request body.

Query parameter:

```text
email
```

### Response

```json
{
  "success": true,
  "data": {
    "id": "candidate-uuid",
    "name": "Rahul Kumar",
    "email": "rahul@example.com",
    "role": "CANDIDATE"
  },
  "timestamp": "2026-08-25T10:30:00Z"
}
```

### Database rules

Search using:

```text
USER.email = requested email
```

Then require:

```text
USER.role = CANDIDATE
```

No candidate is created by this endpoint.

---

## 4.2 List Candidate's Assessments Within Recruiter Scope

### Endpoint

```http
GET /api/v1/candidates/{candidateId}/assessments
```

### Authentication

`RECRUITER`

### Database sources

- `USER`
- `WORKSPACE`
- `WORKSPACE_CANDIDATE`
- `ASSESSMENT`
- `SUBMISSION`
- `EVALUATION_REPORT`

### Scope rule

The recruiter must only receive assessments where:

```text
ASSESSMENT.candidate_id = candidateId
```

and the assessment belongs to a workspace where:

```text
WORKSPACE.recruiter_id = authenticatedUser.id
```

### Response

```json
{
  "success": true,
  "data": [
    {
      "assessmentId": "assessment-uuid",
      "workspaceId": "workspace-uuid",
      "workspaceName": "TCS - Backend Hiring",
      "repositoryUrl": "https://github.com/example/notes-app.git",
      "branchName": "main",
      "backendRootDirectory": "backend",
      "difficulty": "INTERMEDIATE",
      "durationMinutes": 90,
      "scheduledStartAt": "2026-08-25T10:00:00+05:30",
      "scheduledEndAt": "2026-08-25T11:30:00+05:30",
      "status": "COMPLETED",
      "submission": {
        "submittedAt": "2026-08-25T11:20:00+05:30",
        "timeTakenSeconds": 4800,
        "status": "SUBMITTED"
      },
      "evaluation": {
        "score": 82.5,
        "totalTests": 10,
        "passedTests": 8,
        "failedTests": 2,
        "buildStatus": "SUCCESS",
        "applicationStatus": "STARTED",
        "timeTakenSeconds": 25,
        "status": "COMPLETED",
        "evaluatedAt": "2026-08-25T11:22:00+05:30"
      }
    }
  ],
  "timestamp": "2026-08-25T11:30:00Z"
}
```

### Database relationship

```text
USER
  ↑
  │ candidate_id
ASSESSMENT
  │
  ├── WORKSPACE
  │
  └── SUBMISSION
        └── EVALUATION_REPORT
```

---

# Module 5 — Assessment

| Method | Endpoint | Access | Use Case |
|---|---|---|---|
| POST | `/workspaces/{workspaceId}/assessments` | Recruiter | Create assessment for a workspace candidate |
| GET | `/assessments/{assessmentId}` | Recruiter/Candidate | Get assessment details subject to ownership/assignment |
| GET | `/assessments/{assessmentId}/processing-status` | Recruiter | Get asynchronous assessment preparation status |
| GET | `/assessments/{assessmentId}/feature` | Recruiter/Candidate | Get feature specification |
| GET | `/assessments/{assessmentId}/status` | Recruiter/Candidate | Poll current assessment status |

---

## 5.1 Create Assessment

### Endpoint

```http
POST /api/v1/workspaces/{workspaceId}/assessments
```

### Authentication

`RECRUITER`

### Database tables

Primary table:

```text
ASSESSMENT
```

Related table:

```text
WORKSPACE_CANDIDATE
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

### Validation

The API must verify that:

```text
WORKSPACE.id = {workspaceId}

WORKSPACE.recruiter_id = authenticatedUser.id

WORKSPACE_CANDIDATE.workspace_id = {workspaceId}

WORKSPACE_CANDIDATE.candidate_id = request.candidateId
```

This prevents creating an assessment for a candidate who is not a member of the workspace.

### Database insert

```text
ASSESSMENT
--------------------------------
id
workspace_id
candidate_id
repository_url
branch_name
backend_root_directory
difficulty
duration_minutes
scheduled_start_at
scheduled_end_at
status
created_at
updated_at
```

### Server-controlled fields

The client does not supply:

```text
id
status
created_at
updated_at
```

### Response

```json
{
  "success": true,
  "message": "Assessment creation started",
  "data": {
    "assessmentId": "assessment-uuid",
    "workspaceId": "workspace-uuid",
    "candidateId": "candidate-uuid",
    "repositoryUrl": "https://github.com/example/notes-app.git",
    "branchName": "main",
    "backendRootDirectory": "backend",
    "difficulty": "INTERMEDIATE",
    "durationMinutes": 90,
    "scheduledStartAt": "2026-08-25T10:00:00+05:30",
    "scheduledEndAt": "2026-08-25T11:30:00+05:30",
    "status": "CREATING"
  },
  "timestamp": "2026-08-25T09:00:00+05:30"
}
```

### Asynchronous behavior

The endpoint should return after the `ASSESSMENT` row has been created.

The following processing should happen asynchronously:

```text
Repository cloning
        ↓
Repository analysis
        ↓
Feature specification generation
        ↓
Test-case generation
```

The database currently persists:

```text
REPOSITORY_ANALYSIS.analysis_status
REPOSITORY_ANALYSIS.created_at
REPOSITORY_ANALYSIS.completed_at

FEATURE_SPECIFICATION.*
TEST_CASE.*
ASSESSMENT.status
```

The finalized database design does **not** define a separate pipeline-stage table.

---

## 5.2 Get Assessment Details

### Endpoint

```http
GET /api/v1/assessments/{assessmentId}
```

### Authentication

`RECRUITER` or assigned `CANDIDATE`

### Database sources

- `ASSESSMENT`
- `WORKSPACE`
- `USER`
- `SUBMISSION`
- `EVALUATION_REPORT`

### Authorization

For recruiter:

```text
ASSESSMENT
    → WORKSPACE
    → WORKSPACE.recruiter_id = authenticatedUser.id
```

For candidate:

```text
ASSESSMENT.candidate_id = authenticatedUser.id
```

### Response — Recruiter

```json
{
  "success": true,
  "data": {
    "id": "assessment-uuid",
    "workspaceId": "workspace-uuid",
    "candidate": {
      "id": "candidate-uuid",
      "name": "Rahul Kumar",
      "email": "rahul@example.com"
    },
    "repositoryUrl": "https://github.com/example/notes-app.git",
    "branchName": "main",
    "backendRootDirectory": "backend",
    "difficulty": "INTERMEDIATE",
    "durationMinutes": 90,
    "scheduledStartAt": "2026-08-25T10:00:00+05:30",
    "scheduledEndAt": "2026-08-25T11:30:00+05:30",
    "status": "COMPLETED",
    "createdAt": "2026-08-25T09:00:00+05:30",
    "updatedAt": "2026-08-25T11:30:00+05:30"
  },
  "timestamp": "2026-08-25T11:30:00+05:30"
}
```

### Candidate response

Candidate-facing responses must not expose recruiter/internal data such as:

```text
repositoryUrl
branchName
backendRootDirectory
hidden test information
expected responses
evaluator assertions
```

A candidate-safe response can be:

```json
{
  "success": true,
  "data": {
    "id": "assessment-uuid",
    "workspaceId": "workspace-uuid",
    "difficulty": "INTERMEDIATE",
    "durationMinutes": 90,
    "scheduledStartAt": "2026-08-25T10:00:00+05:30",
    "scheduledEndAt": "2026-08-25T11:30:00+05:30",
    "status": "SCHEDULED"
  },
  "timestamp": "2026-08-25T09:30:00+05:30"
}
```

---

## 5.3 Get Assessment Processing Status

### Endpoint

```http
GET /api/v1/assessments/{assessmentId}/processing-status
```

### Authentication

`RECRUITER`

### Database sources

- `ASSESSMENT`
- `REPOSITORY_ANALYSIS`
- `FEATURE_SPECIFICATION`
- `TEST_CASE`

### Important database limitation

The finalized database schema does not contain a dedicated `assessment_processing_stage` table.

Therefore, the API must derive the processing status from persisted assessment and preparation records.

### Response

```json
{
  "success": true,
  "data": {
    "assessmentId": "assessment-uuid",
    "assessmentStatus": "GENERATING_TESTS",
    "repositoryAnalysis": {
      "status": "COMPLETED",
      "completedAt": "2026-08-25T09:15:00+05:30"
    },
    "featureSpecification": {
      "status": "COMPLETED",
      "available": true
    },
    "testCases": {
      "generatedCount": 7
    }
  },
  "timestamp": "2026-08-25T09:20:00+05:30"
}
```

### Database mapping

```text
assessmentStatus
    → ASSESSMENT.status

repositoryAnalysis.status
    → REPOSITORY_ANALYSIS.analysis_status

repositoryAnalysis.completedAt
    → REPOSITORY_ANALYSIS.completed_at

featureSpecification.available
    → existence of FEATURE_SPECIFICATION row

testCases.generatedCount
    → COUNT(TEST_CASE.id)
      WHERE TEST_CASE.assessment_id =
            FEATURE_SPECIFICATION.assessment_id
```

### Important implementation rule

Statuses such as:

```text
CLONING
FEATURE_GENERATION
TEST_GENERATION
```

are not individually persisted by the finalized schema.

If the frontend requires exact stage-level progress, the database design would need an additional processing/job entity or equivalent persistence mechanism. This API must not pretend those stages are database-backed when they are not.

---

## 5.4 Get Feature Specification

### Endpoint

```http
GET /api/v1/assessments/{assessmentId}/feature
```

### Authentication

`RECRUITER` or assigned `CANDIDATE`

### Database table

`FEATURE_SPECIFICATION`

### Relationship

```text
FEATURE_SPECIFICATION.assessment_id
    → ASSESSMENT.id
```

### Response

```json
{
  "success": true,
  "data": {
    "assessmentId": "assessment-uuid",
    "featureName": "Add Search API",
    "description": "Implement search functionality for notes.",
    "requirements": {
      "items": [
        "Support keyword search",
        "Return matching notes"
      ]
    },
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
    "constraints": {},
    "createdAt": "2026-08-25T09:20:00+05:30",
    "updatedAt": "2026-08-25T09:20:00+05:30"
  },
  "timestamp": "2026-08-25T09:20:00+05:30"
}
```

### Database mapping

```text
assessmentId
    → FEATURE_SPECIFICATION.assessment_id

featureName
    → FEATURE_SPECIFICATION.feature_name

description
    → FEATURE_SPECIFICATION.description

requirements
    → FEATURE_SPECIFICATION.requirements

requestSpecification
    → FEATURE_SPECIFICATION.request_specification

responseSpecification
    → FEATURE_SPECIFICATION.response_specification

constraints
    → FEATURE_SPECIFICATION.constraints

createdAt
    → FEATURE_SPECIFICATION.created_at

updatedAt
    → FEATURE_SPECIFICATION.updated_at
```

### Candidate visibility

The feature specification may be returned to the assigned candidate.

The following must never be exposed through this endpoint:

```text
TEST_CASE.request_data
TEST_CASE.expected_status_code
TEST_CASE.expected_response
TEST_CASE.assertions
```

---

## 5.5 Poll Current Assessment Status

### Endpoint

```http
GET /api/v1/assessments/{assessmentId}/status
```

### Authentication

`RECRUITER` or assigned `CANDIDATE`

### Purpose

This endpoint is intended for frontend polling of the current persisted assessment lifecycle state.

### Database source

`ASSESSMENT`

### Response

```json
{
  "success": true,
  "data": {
    "assessmentId": "assessment-uuid",
    "status": "CREATING",
    "scheduledStartAt": "2026-08-25T10:00:00+05:30",
    "scheduledEndAt": "2026-08-25T11:30:00+05:30"
  },
  "timestamp": "2026-08-25T09:10:00+05:30"
}
```

### Database mapping

```text
assessmentId
    → ASSESSMENT.id

status
    → ASSESSMENT.status

scheduledStartAt
    → ASSESSMENT.scheduled_start_at

scheduledEndAt
    → ASSESSMENT.scheduled_end_at
```

### Difference between `/status` and `/processing-status`

#### `/status`

Returns the current persisted lifecycle status of the assessment.

```text
ASSESSMENT.status
```

Use this for simple frontend polling.

#### `/processing-status`

Returns the available preparation information derived from:

```text
ASSESSMENT
REPOSITORY_ANALYSIS
FEATURE_SPECIFICATION
TEST_CASE
```

Use this when the frontend needs more information about assessment preparation.

---

# Standard Error Cases

| HTTP Status | Error Code | Meaning |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Request data is invalid |
| 401 | `UNAUTHENTICATED` | Authentication is required or session is invalid |
| 403 | `FORBIDDEN` | User does not have permission |
| 404 | `CANDIDATE_NOT_FOUND` | Candidate does not exist |
| 404 | `WORKSPACE_NOT_FOUND` | Workspace does not exist or is inaccessible |
| 404 | `ASSESSMENT_NOT_FOUND` | Assessment does not exist or is inaccessible |
| 404 | `FEATURE_NOT_FOUND` | Feature specification is not available |
| 409 | `DUPLICATE_RESOURCE` | Resource already exists or candidate is already in workspace |
| 409 | `ASSESSMENT_NOT_AVAILABLE` | Assessment is outside its allowed schedule or unavailable |
| 422 | `INVALID_ASSESSMENT_CONFIGURATION` | Assessment configuration cannot be processed |
| 500 | `INTERNAL_ERROR` | Unexpected server error |
| 502 | `AI_PROCESSING_ERROR` | AI feature/test generation failed |

---

# Database-to-API Relationship Summary

```text
USER
│
├── WORKSPACE
│   │
│   ├── WORKSPACE_CANDIDATE
│   │       │
│   │       └── USER (candidate)
│   │
│   └── ASSESSMENT
│       │
│       ├── REPOSITORY_ANALYSIS
│       │
│       ├── ASSESSMENT_WORKSPACE
│       │
│       ├── FEATURE_SPECIFICATION
│       │       │
│       │       └── TEST_CASE
│       │               │
│       │               └── TEST_RESULT
│       │
│       └── SUBMISSION
│               │
│               └── EVALUATION_REPORT
```

---

# Endpoint-to-Database Mapping

| Endpoint | Main Database Tables |
|---|---|
| `GET /recruiter/dashboard` | `WORKSPACE`, `WORKSPACE_CANDIDATE`, `ASSESSMENT` |
| `GET /candidate/dashboard` | `ASSESSMENT`, `WORKSPACE`, `SUBMISSION`, `EVALUATION_REPORT` |
| `POST /workspaces` | `WORKSPACE` |
| `GET /workspaces` | `WORKSPACE` |
| `GET /workspaces/{workspaceId}` | `WORKSPACE`, `WORKSPACE_CANDIDATE`, `USER`, `ASSESSMENT` |
| `PUT /workspaces/{workspaceId}` | `WORKSPACE` |
| `DELETE /workspaces/{workspaceId}` | `WORKSPACE` |
| `GET /workspaces/{workspaceId}/candidates` | `WORKSPACE_CANDIDATE`, `USER` |
| `POST /workspaces/{workspaceId}/candidates` | `USER`, `WORKSPACE_CANDIDATE` |
| `DELETE /workspaces/{workspaceId}/candidates/{candidateId}` | `WORKSPACE_CANDIDATE` |
| `GET /candidates/search?email={email}` | `USER` |
| `GET /candidates/{candidateId}/assessments` | `ASSESSMENT`, `WORKSPACE`, `SUBMISSION`, `EVALUATION_REPORT` |
| `POST /workspaces/{workspaceId}/assessments` | `WORKSPACE_CANDIDATE`, `ASSESSMENT` |
| `GET /assessments/{assessmentId}` | `ASSESSMENT`, `WORKSPACE`, `USER` |
| `GET /assessments/{assessmentId}/processing-status` | `ASSESSMENT`, `REPOSITORY_ANALYSIS`, `FEATURE_SPECIFICATION`, `TEST_CASE` |
| `GET /assessments/{assessmentId}/feature` | `FEATURE_SPECIFICATION`, `ASSESSMENT` |
| `GET /assessments/{assessmentId}/status` | `ASSESSMENT` |

---

# Scope and Authorization Rules

## Recruiter

A recruiter may access:

```text
WORKSPACE
    WHERE WORKSPACE.recruiter_id = authenticatedUser.id
```

All nested workspace resources must inherit this ownership check.

For candidate assessments, the recruiter must have a valid ownership path:

```text
authenticated recruiter
    → WORKSPACE.recruiter_id
    → WORKSPACE.id
    → ASSESSMENT.workspace_id
```

## Candidate

A candidate may access an assessment only when:

```text
ASSESSMENT.candidate_id = authenticatedUser.id
```

Candidate-facing APIs must never expose:

```text
TEST_CASE.expected_response
TEST_CASE.assertions
TEST_CASE.request_data
other hidden evaluator information
```

---

# Implementation Notes

1. Assessment creation should return before repository analysis and AI generation finish.
2. A background job/event-driven mechanism should perform long-running preparation.
3. `ASSESSMENT.status` is the authoritative persisted assessment lifecycle state.
4. `REPOSITORY_ANALYSIS.analysis_status` represents repository-analysis processing state.
5. `FEATURE_SPECIFICATION` existence indicates that a feature specification has been generated.
6. `TEST_CASE` rows indicate generated test cases.
7. The current database design does not persist individual asynchronous pipeline stages such as cloning, feature generation, and test generation as separate entities.
8. Exact ENUM values are intentionally left for the implementation layer.
9. `ON DELETE` and `ON UPDATE` behavior must be explicitly selected before production DDL.
10. UUID generation defaults must be explicitly configured in PostgreSQL/application code.
11. Physical repository paths from `ASSESSMENT_WORKSPACE` are internal execution/storage data and should not be exposed through these endpoints.
12. All authorization checks must be performed server-side.
13. The API must not rely on a client-supplied `recruiterId`; recruiter scope must come from the authenticated user.
