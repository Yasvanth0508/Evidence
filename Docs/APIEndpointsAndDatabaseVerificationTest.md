# API Endpoints & Database Verification Specification

> **Base URL:** `/api/v1`  
> **Database:** PostgreSQL (`evidence_db`)  
> **Target Audience:** Candidate Assessment Lifecycle, Real-Time File Explorer IDE, Live Sandbox Execution, Automated Evaluation Engine & Recruiter Reporting.

---

## 1. Candidate Assessment & Workspace File Explorer Endpoints

### 1.1 Start Assessment & Initialize Candidate Workspace
- **Method & Path:** `POST /api/v1/assessments/{assessmentId}/start`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Purpose:** Verifies the schedule, creates an isolated candidate copy from `original/` into `candidate_workspace/`, registers the paths in `ASSESSMENT_WORKSPACES`, and transitions assessment status to `IN_PROGRESS`.
- **Database Tables:** `ASSESSMENT`, `ASSESSMENT_WORKSPACE`
- **Request Body:** None
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "message": "Assessment workspace initialized successfully",
    "data": {
      "assessmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "status": "IN_PROGRESS",
      "workspacePath": "storage/assessments/3fa85f64.../candidate_workspace",
      "startedAt": "2026-08-26T11:00:00Z"
    },
    "timestamp": "2026-08-26T11:00:00Z"
  }
  ```
- **Database Verification:**
  - `ASSESSMENT.status` = `'IN_PROGRESS'`
  - `ASSESSMENT_WORKSPACE.candidate_workspace_path` is populated.

---

### 1.2 Fetch Workspace Directory Tree (File Explorer)
- **Method & Path:** `GET /api/v1/assessments/{assessmentId}/files`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Purpose:** Recursively scans candidate workspace directory and returns clean hierarchical file/directory tree (excluding `.git`, `target`, `.idea`, `node_modules`, `.class`).
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": {
      "name": "root",
      "type": "DIRECTORY",
      "path": "",
      "children": [
        {
          "name": "pom.xml",
          "type": "FILE",
          "path": "pom.xml",
          "extension": "xml"
        },
        {
          "name": "src",
          "type": "DIRECTORY",
          "path": "src",
          "children": [
            {
              "name": "main",
              "type": "DIRECTORY",
              "path": "src/main",
              "children": []
            }
          ]
        }
      ]
    },
    "timestamp": "2026-08-26T11:00:05Z"
  }
  ```

---

### 1.3 Read File Content
- **Method & Path:** `GET /api/v1/assessments/{assessmentId}/files/content?path={filePath}`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Query Params:** `path` (e.g. `src/main/java/com/example/controller/NoteController.java`)
- **Purpose:** Safely reads text content with path traversal protection.
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": {
      "path": "src/main/java/com/example/controller/NoteController.java",
      "content": "package com.example.controller;\n\nimport org.springframework.web.bind.annotation.*;...",
      "language": "java",
      "sizeBytes": 1420
    },
    "timestamp": "2026-08-26T11:00:10Z"
  }
  ```

---

### 1.4 Save / Debounced Autosave File Content
- **Method & Path:** `PUT /api/v1/assessments/{assessmentId}/files/content`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Debounce Window:** 2500 ms (Frontend autosave debounce)
- **Request Body:**
  ```json
  {
    "path": "src/main/java/com/example/controller/NoteController.java",
    "content": "package com.example.controller;\n\n// modified source code..."
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "message": "File saved successfully",
    "data": {
      "path": "src/main/java/com/example/controller/NoteController.java",
      "savedAt": "2026-08-26T11:02:15Z"
    },
    "timestamp": "2026-08-26T11:02:15Z"
  }
  ```
- **Verification:** Target file on disk in `candidate_workspace` reflects new content immediately.

---

### 1.5 Create New File or Directory
- **Method & Path:** `POST /api/v1/assessments/{assessmentId}/files`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Request Body:**
  ```json
  {
    "path": "src/main/java/com/example/dto/SearchRequest.java",
    "type": "FILE",
    "initialContent": "package com.example.dto;\n\npublic class SearchRequest {}"
  }
  ```
- **Response (`201 CREATED`):**
  ```json
  {
    "success": true,
    "message": "File created successfully",
    "timestamp": "2026-08-26T11:03:00Z"
  }
  ```

---

### 1.6 Delete File or Directory
- **Method & Path:** `DELETE /api/v1/assessments/{assessmentId}/files?path={filePath}`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "message": "File deleted successfully",
    "timestamp": "2026-08-26T11:03:30Z"
  }
  ```

---

### 1.7 Rename File or Directory
- **Method & Path:** `POST /api/v1/assessments/{assessmentId}/files/rename`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Request Body:**
  ```json
  {
    "oldPath": "src/main/java/com/example/OldName.java",
    "newPath": "src/main/java/com/example/NewName.java"
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "message": "File renamed successfully",
    "timestamp": "2026-08-26T11:04:00Z"
  }
  ```

---

## 2. Live Sandbox Execution Endpoints ("Run Code" Console)

### 2.1 Trigger Live Application Run
- **Method & Path:** `POST /api/v1/assessments/{assessmentId}/run`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Purpose:** Compiles updated candidate code, builds candidate Docker image, and starts ephemeral container on dynamic port.
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "message": "Application compilation and execution started",
    "data": {
      "executionId": "e1f2a3b4-5678-90ab-cdef-1234567890ab",
      "status": "STARTING",
      "port": 18452
    },
    "timestamp": "2026-08-26T11:05:00Z"
  }
  ```

---

### 2.2 Get Application Execution Status
- **Method & Path:** `GET /api/v1/assessments/{assessmentId}/execution/status`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": {
      "executionId": "e1f2a3b4-5678-90ab-cdef-1234567890ab",
      "buildStatus": "SUCCESS",
      "containerStatus": "RUNNING",
      "applicationStatus": "STARTED",
      "port": 18452,
      "uptimeSeconds": 14
    },
    "timestamp": "2026-08-26T11:05:15Z"
  }
  ```

---

### 2.3 Fetch Execution Logs & Terminal Output
- **Method & Path:** `GET /api/v1/assessments/{assessmentId}/execution/logs`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": {
      "logs": "Tomcat started on port 8080 (http)\nStarted Application in 2.314 seconds",
      "isTerminal": false
    },
    "timestamp": "2026-08-26T11:05:20Z"
  }
  ```

---

### 2.4 Stop Running Container
- **Method & Path:** `POST /api/v1/assessments/{assessmentId}/stop`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "message": "Container stopped successfully",
    "data": {
      "status": "STOPPED"
    },
    "timestamp": "2026-08-26T11:06:00Z"
  }
  ```

---

## 3. Final Submission & Automated Evaluation Endpoints

### 3.1 Submit Assessment
- **Method & Path:** `POST /api/v1/assessments/{assessmentId}/submit`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Purpose:** Locks workspace, creates `SUBMISSIONS` entity, starts evaluation container, sequentially executes all stored `TEST_CASES`, verifies status codes & JSON assertions, computes score, and generates `EVALUATION_REPORTS`.
- **Database Tables:** `ASSESSMENT`, `SUBMISSION`, `TEST_CASE`, `TEST_RESULT`, `EVALUATION_REPORT`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "message": "Assessment submitted successfully. Evaluation in progress.",
    "data": {
      "submissionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "status": "EVALUATING",
      "submittedAt": "2026-08-26T11:10:00Z"
    },
    "timestamp": "2026-08-26T11:10:00Z"
  }
  ```

---

### 3.2 Candidate Result View (Safe View — No Test Leaks)
- **Method & Path:** `GET /api/v1/assessments/{assessmentId}/result`
- **Headers:** `X-Candidate-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": {
      "assessmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "score": 85.00,
      "status": "COMPLETED",
      "totalTests": 10,
      "passedTests": 8,
      "failedTests": 2,
      "buildStatus": "SUCCESS",
      "applicationStatus": "STARTED",
      "timeTakenSeconds": 2700,
      "evaluatedAt": "2026-08-26T11:11:15Z"
    },
    "timestamp": "2026-08-26T11:11:15Z"
  }
  ```
- **Database Mapping:**
  - `score` $\rightarrow$ `EVALUATION_REPORT.score`
  - `totalTests` $\rightarrow$ `EVALUATION_REPORT.total_tests`
  - `passedTests` $\rightarrow$ `EVALUATION_REPORT.passed_tests`
  - `failedTests` $\rightarrow$ `EVALUATION_REPORT.failed_tests`
  - `status` $\rightarrow$ `ASSESSMENT.status`

---

## 4. Recruiter Reports & Detailed Audit Endpoints

### 4.1 List Workspace Reports with Pagination
- **Method & Path:** `GET /api/v1/reports?workspaceId={workspaceId}&status=COMPLETED&page=0&size=20`
- **Headers:** `X-Recruiter-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": {
      "content": [
        {
          "assessmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "candidateId": "ca78aa07-e9ca-4343-86bc-d285125216e9",
          "candidateName": "Rahul Kumar",
          "candidateEmail": "rahul@example.com",
          "workspaceName": "Enterprise Java Hiring",
          "score": 85.00,
          "status": "COMPLETED",
          "submittedAt": "2026-08-26T11:10:00Z"
        }
      ],
      "page": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1
    },
    "timestamp": "2026-08-26T11:15:00Z"
  }
  ```

---

### 4.2 Recruiter Detailed Assessment Report
- **Method & Path:** `GET /api/v1/assessments/{assessmentId}/report`
- **Headers:** `X-Recruiter-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": {
      "assessmentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "candidate": {
        "id": "ca78aa07-e9ca-4343-86bc-d285125216e9",
        "name": "Rahul Kumar",
        "email": "rahul@example.com"
      },
      "score": 85.00,
      "totalTests": 10,
      "passedTests": 8,
      "failedTests": 2,
      "buildStatus": "SUCCESS",
      "applicationStatus": "STARTED",
      "timeTakenSeconds": 2700,
      "status": "COMPLETED",
      "evaluatedAt": "2026-08-26T11:11:15Z"
    },
    "timestamp": "2026-08-26T11:15:10Z"
  }
  ```

---

### 4.3 Recruiter Granular Test Results Breakdown
- **Method & Path:** `GET /api/v1/assessments/{assessmentId}/test-results`
- **Headers:** `X-Recruiter-Id: <UUID>`
- **Response (`200 OK`):**
  ```json
  {
    "success": true,
    "data": [
      {
        "testCaseId": "11111111-0000-0000-0000-000000000001",
        "testCaseNumber": 1,
        "testType": "BUSINESS_LOGIC",
        "status": "PASSED",
        "expectedStatusCode": 201,
        "actualStatusCode": 201,
        "executionTimeMs": 48,
        "weight": 1.00,
        "failureReason": null
      },
      {
        "testCaseId": "11111111-0000-0000-0000-000000000002",
        "testCaseNumber": 2,
        "testType": "SYNTAX",
        "status": "FAILED",
        "expectedStatusCode": 400,
        "actualStatusCode": 500,
        "executionTimeMs": 62,
        "weight": 1.00,
        "failureReason": "Expected status 400 Bad Request, but received 500 Internal Server Error"
      }
    ],
    "timestamp": "2026-08-26T11:15:20Z"
  }
  ```
