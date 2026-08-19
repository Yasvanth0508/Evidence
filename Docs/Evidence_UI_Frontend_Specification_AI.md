# EVIDENCE — UI / FRONTEND SPECIFICATION

> **Purpose:** Define each frontend page, the backend APIs it consumes, request formats, response formats, and frontend behavior.
>
> **Scope:** MVP.
>
> **Base API URL:** `/api/v1`

## AI Implementation Rules

- Recruiter and candidate have separate authentication flows.
- Recruiters select already-registered candidates by unique email.
- Recruiters cannot create candidate accounts.
- A candidate can have multiple assessments.
- Candidate dashboard contains scheduled and completed assessments.
- Assessment access is controlled by `scheduledStartAt` and `scheduledEndAt`.
- There is no exam-ID workflow.
- Protected APIs require an authenticated session.
- Recruiters can access only their own resources.
- Candidates can access only their assigned assessments.
- Hidden test cases, expected responses, evaluator assertions, and internal repository paths must never be exposed to candidates.
- Long-running repository/AI processing is asynchronous.

## Common Response Format

### Success

```json
{
  "success": true,
  "message": "Request completed successfully",
  "data": {},
  "timestamp": "2026-08-19T10:30:00Z"
}
```

### Error

```json
{
  "success": false,
  "message": "Candidate not found",
  "errorCode": "CANDIDATE_NOT_FOUND",
  "timestamp": "2026-08-19T10:30:00Z"
}
```

# EVIDENCE — UI / FRONTEND SPECIFICATION

**Pages • API Calls • Request Formats • Response Formats**

**MVP Version 1.0**

EVIDENCE
UI / FRONTEND SPECIFICATION
Pages • API Calls • Request Formats • Response Formats
MVP Version 1.0
1. Document Purpose
This document defines the frontend pages for Evidence and the backend APIs consumed by each page. It follows the finalized product decisions: separate recruiter/candidate authentication, recruiter selection of already-registered candidates by unique email, no recruiter-side candidate creation, multiple assessments per candidate, candidate dashboard with scheduled/completed assessments, fixed assessment start/end times, and no exam-ID workflow.
2. Common API Rules
Base URL: /api/v1
Requests and responses use JSON unless otherwise specified.
Protected APIs require the authenticated user's session.
Recruiters can access only their own resources; candidates can access only their assigned assessments.
Hidden test cases, expected responses, evaluator assertions, and internal repository paths are never exposed to candidates.
Long-running repository/AI processing is asynchronous.
3. Page Map
4. Public & Authentication Pages
4.1 Landing Page
Purpose: explain Evidence and route users to the appropriate portal.
API: none.
4.2 Recruiter Sign Up
POST /api/v1/auth/recruiter/signup
Request:
{
  "name": "John Recruiter",
  "email": "john@company.com",
  "password": "StrongPassword123"
}
Response 201:
{
  "success": true,
  "data": {
    "id": "uuid", "name": "John Recruiter",
    "email": "john@company.com", "role": "RECRUITER"
  }
}
4.3 Recruiter Login
POST /api/v1/auth/recruiter/login
Request:
{
  "email": "john@company.com",
  "password": "StrongPassword123"
}
Response 200:
{
  "success": true,
  "data": {
    "id": "uuid", "name": "John Recruiter",
    "email": "john@company.com", "role": "RECRUITER"
  }
}
4.4 Candidate Sign Up
POST /api/v1/auth/candidate/signup
Request:
{
  "name": "Rahul Kumar",
  "email": "rahul@example.com",
  "password": "StrongPassword123"
}
Response 201:
{
  "success": true,
  "data": {
    "id": "uuid", "name": "Rahul Kumar",
    "email": "rahul@example.com", "role": "CANDIDATE"
  }
}
This is the only candidate-account creation flow.
4.5 Candidate Login
POST /api/v1/auth/candidate/login
Request:
{
  "email": "rahul@example.com",
  "password": "StrongPassword123"
}
Response 200:
{
  "success": true,
  "data": {
    "id": "uuid", "name": "Rahul Kumar",
    "email": "rahul@example.com", "role": "CANDIDATE"
  }
}
5. Recruiter Dashboard
Purpose: show high-level recruiter statistics and navigation.
GET /api/v1/recruiter/dashboard
Response:
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
6. Workspace Pages
6.1 Workspace List
GET /api/v1/workspaces
Response:
{
  "success": true,
  "data": [
    {"id":"uuid","name":"TCS - REC Placement Drive","description":"Backend assessment","status":"ACTIVE"}
  ]
}
Create workspace:
POST /api/v1/workspaces
Request:
{"name":"TCS - REC Placement Drive","description":"Backend assessment"}
Response 201:
{"success":true,"data":{"id":"uuid","name":"TCS - REC Placement Drive","status":"ACTIVE"}}
6.2 Workspace Detail
GET /api/v1/workspaces/{workspaceId}
Response:
{"success":true,"data":{"id":"uuid","name":"TCS - REC Placement Drive","status":"ACTIVE"}}
GET /api/v1/workspaces/{workspaceId}/candidates
Response:
{"success":true,"data":[{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com"}]}
GET /api/v1/workspaces/{workspaceId}/assessments
Response:
{"success":true,"data":[{"assessmentId":"uuid","candidateName":"Rahul Kumar","difficulty":"INTERMEDIATE","scheduledStartAt":"2026-08-25T10:00:00+05:30","scheduledEndAt":"2026-08-25T11:30:00+05:30","status":"SCHEDULED"}]}
7. Candidate Search & Workspace Membership
Recruiter searches by the candidate's unique email. If the candidate does not exist, the recruiter cannot continue.
GET /api/v1/candidates/search?email=rahul@example.com
Response 200:
{"success":true,"data":{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com","role":"CANDIDATE"}}
Add the existing candidate to a workspace:
POST /api/v1/workspaces/{workspaceId}/candidates
Request:
{"email":"rahul@example.com"}
Response 201:
{"success":true,"data":{"workspaceId":"uuid","candidate":{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com"}}}
If no account exists: 404 CANDIDATE_NOT_FOUND. Show: 'Candidate has not registered yet. Ask the candidate to create an account.'
There is deliberately no POST /candidates endpoint for recruiter-created candidate accounts.
8. Recruiter Candidate Detail
GET /api/v1/candidates/{candidateId}
Response:
{"success":true,"data":{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com","role":"CANDIDATE"}}
GET /api/v1/candidates/{candidateId}/workspaces
Response:
{"success":true,"data":[{"workspaceId":"uuid","workspaceName":"TCS - REC Placement Drive"}]}
GET /api/v1/candidates/{candidateId}/assessments
Response:
{"success":true,"data":[{"assessmentId":"uuid","difficulty":"INTERMEDIATE","scheduledStartAt":"2026-08-25T10:00:00+05:30","scheduledEndAt":"2026-08-25T11:30:00+05:30","status":"COMPLETED","score":82.5}]}
9. Create Assessment Page
The recruiter selects a candidate already belonging to the workspace.
GET /api/v1/workspaces/{workspaceId}/candidates
Response:
{"success":true,"data":[{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com"}]}
POST /api/v1/workspaces/{workspaceId}/assessments
Request:
{
  "candidateId":"candidate-uuid",
  "repositoryUrl":"https://github.com/example/notes-app.git",
  "branchName":"main",
  "backendRootDirectory":"backend",
  "difficulty":"INTERMEDIATE",
  "durationMinutes":90,
  "scheduledStartAt":"2026-08-25T10:00:00+05:30",
  "scheduledEndAt":"2026-08-25T11:30:00+05:30"
}
Response 201:
{
  "success":true,
  "data":{
    "assessmentId":"uuid","candidateId":"uuid","workspaceId":"uuid",
    "status":"CREATING",
    "scheduledStartAt":"2026-08-25T10:00:00+05:30",
    "scheduledEndAt":"2026-08-25T11:30:00+05:30"
  }
}
After creation, navigate to the Assessment Processing page. Do not wait synchronously for AI processing.

## 10. Assessment Processing Page
GET /api/v1/assessments/{assessmentId}/processing-status
Response:
{
  "success":true,
  "data":{
    "assessmentId":"uuid","status":"GENERATING_TESTS",
    "stages":[
      {"name":"CLONING","status":"COMPLETED"},
      {"name":"REPOSITORY_ANALYSIS","status":"COMPLETED"},
      {"name":"FEATURE_GENERATION","status":"COMPLETED"},
      {"name":"TEST_GENERATION","status":"RUNNING"}
    ]
  }
}
UI stages: Creating → Cloning → Repository Analysis → Feature Generation → Test Generation → Ready / Failed.

## 11. Recruiter Assessment Detail
GET /api/v1/assessments/{assessmentId}
Response:
{
  "success":true,
  "data":{
    "assessmentId":"uuid",
    "candidate":{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com"},
    "repositoryUrl":"https://github.com/example/notes-app.git",
    "branchName":"main","backendRootDirectory":"backend",
    "difficulty":"INTERMEDIATE","durationMinutes":90,
    "scheduledStartAt":"2026-08-25T10:00:00+05:30",
    "scheduledEndAt":"2026-08-25T11:30:00+05:30","status":"SCHEDULED"
  }
}
GET /api/v1/assessments/{assessmentId}/repository-analysis
Response:
{"success":true,"data":{"analysisStatus":"COMPLETED","projectStructure":{"folders":[],"files":[]},"sourceCodeStructure":{"controllers":[],"services":[],"repositories":[],"entities":[]},"contentDetails":{"endpoints":[],"entityFields":[],"serviceMethods":[]}}}

## 12. Feature Specification View
GET /api/v1/assessments/{assessmentId}/feature
Response:
{
  "success":true,
  "data":{
    "title":"Add Search API",
    "description":"Implement search functionality for notes.",
    "requirements":["Support keyword search","Return matching notes"],
    "endpoint":"/api/notes/search","httpMethod":"GET",
    "requestSpecification":{"queryParameters":{"keyword":"string"}},
    "responseSpecification":{"status":200,"body":{"items":"array"}},
    "constraints":[]
  }
}
Candidate can see the feature contract. Hidden TEST_CASE data and expected responses remain server-side.

## 13. Candidate Dashboard
GET /api/v1/candidate/dashboard
Response:
{
  "success":true,
  "data":{
    "scheduledAssessments":[
      {"assessmentId":"uuid","workspaceName":"TCS - REC Placement Drive","scheduledStartAt":"2026-08-25T10:00:00+05:30","scheduledEndAt":"2026-08-25T11:30:00+05:30","difficulty":"INTERMEDIATE","status":"SCHEDULED"}
    ],
    "completedAssessments":[
      {"assessmentId":"uuid","workspaceName":"ABC Hiring Drive","completedAt":"2026-08-18T12:15:00+05:30","score":82.5,"status":"COMPLETED"}
    ]
  }
}
Scheduled UI:
Future assessment: show schedule; Start disabled.
Within scheduled window: show Start Assessment.
Past/expired assessment: show unavailable/expired state.
Completed assessment: show score/result action.

## 14. Candidate Assessment Overview
GET /api/v1/assessments/{assessmentId}
Response:
{"success":true,"data":{"assessmentId":"uuid","workspaceName":"TCS - REC Placement Drive","difficulty":"INTERMEDIATE","durationMinutes":90,"scheduledStartAt":"2026-08-25T10:00:00+05:30","scheduledEndAt":"2026-08-25T11:30:00+05:30","status":"SCHEDULED"}}
When the fixed schedule permits access:
POST /api/v1/assessments/{assessmentId}/start
Response:
{"success":true,"data":{"assessmentId":"uuid","status":"IN_PROGRESS"}}

## 15. Candidate Coding IDE

## 15.1 File Tree
GET /api/v1/assessments/{assessmentId}/files
Response:
{"success":true,"data":{"root":{"name":"backend","type":"DIRECTORY","children":[{"name":"src","type":"DIRECTORY","children":[]},{"name":"pom.xml","type":"FILE"}]}}}

## 15.2 Read File
GET /api/v1/assessments/{assessmentId}/files/content?path=src/main/java/com/example/NotesController.java
Response:
{"success":true,"data":{"path":"src/main/java/com/example/NotesController.java","content":"package com.example; ..."}}

## 15.3 Save File
PUT /api/v1/assessments/{assessmentId}/files/content
Request:
{"path":"src/main/java/com/example/NotesController.java","content":"package com.example; ...modified source..."}
Response:
{"success":true,"message":"File saved"}

## 15.4 Run
POST /api/v1/assessments/{assessmentId}/run
Response:
{"success":true,"data":{"executionId":"uuid","status":"STARTING"}}

## 15.5 Execution Status
GET /api/v1/assessments/{assessmentId}/execution/status
Response:
{"success":true,"data":{"executionId":"uuid","buildStatus":"SUCCESS","containerStatus":"RUNNING","applicationStatus":"STARTED"}}

## 15.6 Logs
GET /api/v1/assessments/{assessmentId}/execution/logs
Response:
{"success":true,"data":{"logs":"Started DemoApplication... Tomcat started on port 8080..."}}
SSE/WebSocket log streaming can replace polling in the implementation.

## 15.7 Stop
POST /api/v1/assessments/{assessmentId}/stop
Response:
{"success":true,"data":{"status":"STOPPED"}}

## 16. Candidate Submission & Result
POST /api/v1/assessments/{assessmentId}/submit
Response:
{"success":true,"data":{"submissionId":"uuid","status":"EVALUATING"}}
After submission: lock editor/actions and show evaluation progress.
GET /api/v1/assessments/{assessmentId}/result
Response:
{"success":true,"data":{"assessmentId":"uuid","score":82.5,"status":"COMPLETED","totalTests":10,"passedTests":8,"failedTests":2}}
Candidate result must not reveal hidden expected responses or assertions.

## 17. Recruiter Reports
GET /api/v1/reports?workspaceId=uuid&status=COMPLETED&page=0&size=20
Response:
{"success":true,"data":{"content":[{"assessmentId":"uuid","candidateName":"Rahul Kumar","score":82.5,"status":"COMPLETED"}],"page":0,"size":20,"totalElements":1}}
Assessment report:
GET /api/v1/assessments/{assessmentId}/report
Response:
{"success":true,"data":{"assessmentId":"uuid","candidate":{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com"},"score":82.5,"totalTests":10,"passedTests":8,"failedTests":2,"buildStatus":"SUCCESS","applicationStatus":"STARTED","timeTakenSeconds":4200,"status":"COMPLETED"}}
Test results:
GET /api/v1/assessments/{assessmentId}/test-results
Response:
{"success":true,"data":[{"testCaseNumber":1,"status":"PASSED","actualStatusCode":200,"executionTimeMs":85},{"testCaseNumber":2,"status":"FAILED","actualStatusCode":400,"failureReason":"Expected status 200"}]}

## 18. Selected Candidates
GET /api/v1/selected-candidates
Response:
{"success":true,"data":[{"candidateId":"uuid","candidateName":"Rahul Kumar","email":"rahul@example.com","assessmentCount":3,"completedCount":2,"latestScore":82.5}]}
GET /api/v1/selected-candidates/{candidateId}
Response:
{"success":true,"data":{"candidate":{"id":"uuid","name":"Rahul Kumar","email":"rahul@example.com"},"workspaces":[{"workspaceId":"uuid","workspaceName":"TCS - REC Placement Drive","assessmentCount":3,"completedCount":2}]}}

## 19. Authentication/Profile Actions
GET /api/v1/auth/recruiter/me
Response:
{"success":true,"data":{"id":"uuid","name":"John Recruiter","email":"john@company.com","role":"RECRUITER"}}

## 20. Standard Frontend Error Handling

## 21. Page-to-API Matrix

## 22. Complete UI Flow
RECRUITER
Landing → Recruiter Login/Sign Up → Dashboard → Workspaces → Workspace Detail
→ Search Candidate by Email → Candidate Found → Add to Workspace
→ Create Assessment → Processing → Ready/Scheduled → Reports

CANDIDATE
Landing → Candidate Login/Sign Up → Candidate Dashboard
→ Scheduled Assessments → Wait for scheduled start → Start Assessment
→ Coding IDE → Feature → File Explorer/Monaco → Run → Logs → Fix/Re-run
→ Submit → Evaluation → Completed Assessments → Result

## 23. Security Rules for UI
Never request hidden TEST_CASE data from candidate APIs.
Never expose expected_response, hidden assertions, original_repository_path, or candidate_workspace_path.
File paths must be validated server-side; the frontend must not allow path traversal.
Candidate assessment pages must verify the assessment belongs to the authenticated candidate.
Run/stop/submit buttons must be disabled when the assessment is outside its allowed lifecycle.
After submission, the editor must become read-only.
Evidence — UI / Frontend Specification v1.0
Role | Page | Primary APIs
Public | Landing | None
Recruiter | Login / Sign Up | POST /auth/recruiter/login, POST /auth/recruiter/signup
Recruiter | Dashboard | GET /recruiter/dashboard
Recruiter | Workspaces | GET /workspaces, POST /workspaces
Recruiter | Workspace Detail | GET /workspaces/{id}, GET /workspaces/{id}/candidates, GET /workspaces/{id}/assessments
Recruiter | Candidate Search | GET /candidates/search, POST /workspaces/{id}/candidates
Recruiter | Candidate Detail | GET /candidates/{id}, GET /candidates/{id}/assessments
Recruiter | Create Assessment | GET /workspaces/{id}/candidates, POST /workspaces/{id}/assessments
Recruiter | Assessment Processing | GET /assessments/{id}/processing-status
Recruiter | Assessment Detail | GET /assessments/{id}, GET /assessments/{id}/feature, GET /assessments/{id}/repository-analysis
Recruiter | Reports | GET /reports, GET /assessments/{id}/report, GET /assessments/{id}/test-results
Recruiter | Selected Candidates | GET /selected-candidates, GET /selected-candidates/{candidateId}
Candidate | Login / Sign Up | POST /auth/candidate/login, POST /auth/candidate/signup
Candidate | Dashboard | GET /candidate/dashboard
Candidate | Assessment Overview | GET /assessments/{id}, POST /assessments/{id}/start
Candidate | Coding IDE | GET /files, GET /files/content, PUT /files/content, GET /feature, POST /run, POST /stop, GET /execution/status, GET /execution/logs
Candidate | Completed Result | GET /assessments/{id}/result
Form Field | Type | Required
candidateId | UUID | Yes
repositoryUrl | String | Yes
branchName | String | Yes
backendRootDirectory | String | Yes
difficulty | EASY | INTERMEDIATE | DIFFICULT | Yes
durationMinutes | Integer | Yes
scheduledStartAt | DateTime | Yes
scheduledEndAt | DateTime | Yes
UI Area | Purpose | API Calls
File Explorer | Show project tree | GET /assessments/{id}/files
Monaco Editor | Read and save files | GET /files/content, PUT /files/content
Feature Panel | Show feature | GET /feature
Run Button | Build/run application | POST /run
Stop Button | Stop application | POST /stop
Logs Panel | Show execution output | GET /execution/logs
Execution Status | Show build/container/application state | GET /execution/status
Submit | Submit implementation | POST /submit
Action | API
Load recruiter profile | GET /auth/recruiter/me
Recruiter logout | POST /auth/recruiter/logout
Load candidate profile | GET /auth/candidate/me
Candidate logout | POST /auth/candidate/logout
HTTP | Error Code | UI Behavior
400 | VALIDATION_ERROR | Show form/field validation.
401 | UNAUTHENTICATED | Redirect to role login.
403 | FORBIDDEN | Show access denied.
404 | CANDIDATE_NOT_FOUND | Tell recruiter candidate must register first.
404 | ASSESSMENT_NOT_FOUND | Show assessment unavailable.
409 | ASSESSMENT_NOT_AVAILABLE | Show scheduled window and disable Start.
409 | ASSESSMENT_ALREADY_SUBMITTED | Lock assessment and show result.
502 | AI_PROCESSING_ERROR | Show preparation failure.
503 | EXECUTION_UNAVAILABLE | Show execution unavailable/retry state.
Page | APIs
Landing | None
Recruiter Login | POST /auth/recruiter/login
Recruiter Sign Up | POST /auth/recruiter/signup
Candidate Login | POST /auth/candidate/login
Candidate Sign Up | POST /auth/candidate/signup
Recruiter Dashboard | GET /recruiter/dashboard
Workspaces | GET /workspaces, POST /workspaces
Workspace Detail | GET /workspaces/{id}, GET /workspaces/{id}/candidates, GET /workspaces/{id}/assessments
Candidate Search | GET /candidates/search, POST /workspaces/{id}/candidates
Candidate Detail | GET /candidates/{id}, GET /candidates/{id}/workspaces, GET /candidates/{id}/assessments
Create Assessment | GET /workspaces/{id}/candidates, POST /workspaces/{id}/assessments
Assessment Processing | GET /assessments/{id}/processing-status
Candidate Dashboard | GET /candidate/dashboard
Assessment Overview | GET /assessments/{id}, POST /assessments/{id}/start
Coding IDE | GET /files, GET /files/content, PUT /files/content, GET /feature, POST /run, POST /stop, GET /execution/status, GET /execution/logs
Result | GET /assessments/{id}/result
Reports | GET /reports, GET /assessments/{id}/report, GET /assessments/{id}/test-results
Selected Candidates | GET /selected-candidates, GET /selected-candidates/{candidateId}
