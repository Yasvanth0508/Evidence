# Evidence — Database Design

**Version:** 1.1  
**Status:** Finalized / Corrected  
**Project:** Evidence — Recruiter & Candidate Technical Assessment Platform

---

## 1. Database Design Principles

The database supports the following finalized requirements:

1. Evidence has two user roles:
   - `RECRUITER`
   - `CANDIDATE`
2. Recruiters create and manage workspaces.
3. Candidates register independently through the candidate authentication flow.
4. A recruiter cannot create a candidate account.
5. Recruiters find existing candidates using the candidate's unique email address.
6. A candidate can belong to multiple workspaces.
7. A candidate can take multiple assessments.
8. An assessment belongs to one candidate and one workspace.
9. An assessment is based on a repository supplied by the recruiter.
10. Assessment creation stores:
    - Repository URL
    - Branch
    - Backend root directory
    - Difficulty
    - Duration
    - Scheduled start time
    - Scheduled end time
11. Repository structure analysis is stored separately from the assessment's core configuration.
12. AI-generated feature specification and hidden test cases are stored separately.
13. Hidden test cases and expected responses are never exposed through candidate-facing APIs.
14. There is **no exam-ID concept** in the system.

---

# 2. Entity Relationship Overview

```text
USER
 ├── RECRUITER
 │      │
 │      └──< WORKSPACE
 │               │
 │               └──< WORKSPACE_CANDIDATE >── CANDIDATE(USER)
 │                                              │
 │                                              └──< ASSESSMENT
 │                                                       │
 │                                                       ├── REPOSITORY_ANALYSIS
 │                                                       ├── FEATURE_SPECIFICATION
 │                                                       ├── TEST_CASE
 │                                                       ├── SUBMISSION
 │                                                       ├── EXECUTION
 │                                                       └── TEST_RESULT
 │
 └── CANDIDATE
```

A candidate can therefore have:

```text
Candidate
   ├── Assessment 1
   ├── Assessment 2
   ├── Assessment 3
   └── ...
```

---

# 3. Tables

## 3.1 USER

Stores authentication and common identity information for recruiters and candidates.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | User identifier |
| `name` | VARCHAR(150) | NOT NULL | User name |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Unique login/search identifier |
| `password` | VARCHAR(255) | NOT NULL | BCrypt/password hash |
| `role` | ENUM | NOT NULL | `RECRUITER` or `CANDIDATE` |
| `created_at` | TIMESTAMP | NOT NULL | Account creation time |
| `updated_at` | TIMESTAMP | NOT NULL | Last update time |

### Important constraint

`email` is globally unique.

```sql
UNIQUE(email)
```

This supports the requirement that recruiters search for an existing candidate using the candidate's email.

A recruiter cannot create another user with the same email.

---

# 4. WORKSPACE

A workspace represents one recruiter-managed hiring/placement drive.

Example:

```text
TCS - Savita Engineering College Placement Drive
TCS - Rajalakshmi Engineering College Placement Drive
```

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Workspace identifier |
| `recruiter_id` | UUID | FK → USER.id, NOT NULL | Owner recruiter |
| `name` | VARCHAR(200) | NOT NULL | Workspace name |
| `description` | TEXT | NULL | Workspace description |
| `status` | ENUM | NOT NULL | Workspace lifecycle status |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |
| `updated_at` | TIMESTAMP | NOT NULL | Last update time |

### Relationship

```text
USER (RECRUITER)
       1
       |
       | creates
       |
       N
WORKSPACE
```

A recruiter can create many workspaces.

---

# 5. WORKSPACE_CANDIDATE

Associates an existing candidate with a workspace.

This table is required because a candidate can participate in multiple workspaces.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Membership identifier |
| `workspace_id` | UUID | FK → WORKSPACE.id, NOT NULL | Workspace |
| `candidate_id` | UUID | FK → USER.id, NOT NULL | Candidate |
| `created_at` | TIMESTAMP | NOT NULL | Time candidate was added |

### Constraint

```sql
UNIQUE(workspace_id, candidate_id)
```

This prevents the same candidate from being added to the same workspace more than once.

### Relationship

```text
WORKSPACE
    1
    |
    N
WORKSPACE_CANDIDATE
    N
    |
    1
CANDIDATE
```

---

# 6. ASSESSMENT

Represents one technical assessment assigned to one candidate.

An assessment is based on the candidate's project/repository.

## 6.1 Columns

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Assessment identifier |
| `workspace_id` | UUID | FK → WORKSPACE.id, NOT NULL | Workspace |
| `candidate_id` | UUID | FK → USER.id, NOT NULL | Candidate |
| `repository_url` | VARCHAR(2048) | NOT NULL | Git repository URL |
| `branch_name` | VARCHAR(255) | NOT NULL | Branch to analyze |
| `backend_root_directory` | VARCHAR(1000) | NOT NULL | Spring Boot backend root directory |
| `difficulty` | ENUM | NOT NULL | Difficulty level |
| `duration_minutes` | INTEGER | NOT NULL | Assessment duration |
| `scheduled_start_at` | TIMESTAMP | NOT NULL | Fixed assessment start |
| `scheduled_end_at` | TIMESTAMP | NOT NULL | Fixed assessment end |
| `status` | ENUM | NOT NULL | Assessment lifecycle status |
| `score` | DECIMAL(5,2) | NULL | Final score out of 100 |
| `created_at` | TIMESTAMP | NOT NULL | Assessment creation time |
| `updated_at` | TIMESTAMP | NOT NULL | Last update time |

## 6.2 Important: No `exam_id`

There is **no `exam_id` column** in the `ASSESSMENT` table.

The system does not have an exam-ID workflow.

The primary identifier of an assessment is:

```text
assessment.id
```

The candidate does not receive or enter an exam ID.

The candidate accesses assessments through:

```text
Candidate Dashboard
        ↓
Scheduled Assessments
        ↓
Assessment
```

Therefore:

```sql
-- DO NOT HAVE THIS
exam_id VARCHAR(...) UNIQUE NOT NULL
```

---

# 7. Assessment Status

The assessment status enum must match the API/UI contract.

```text
CREATING
ANALYZING
GENERATING_FEATURE
GENERATING_TESTS
READY
SCHEDULED
IN_PROGRESS
EVALUATING
COMPLETED
CANCELLED
FAILED
EXPIRED
```

## Status meanings

| Status | Meaning |
|---|---|
| `CREATING` | Assessment record has been created and preparation has started |
| `ANALYZING` | Repository/project analysis is running |
| `GENERATING_FEATURE` | AI is generating the feature specification |
| `GENERATING_TESTS` | Hidden test cases are being generated |
| `READY` | AI preparation is complete and assessment is ready |
| `SCHEDULED` | Assessment is ready and waiting for its scheduled start time |
| `IN_PROGRESS` | Candidate is currently taking the assessment |
| `EVALUATING` | Candidate submitted and hidden tests are running |
| `COMPLETED` | Evaluation is complete |
| `CANCELLED` | Recruiter/system cancelled the assessment |
| `FAILED` | Assessment preparation or processing failed |
| `EXPIRED` | Scheduled end time passed without valid completion |

### Important correction

`SCHEDULED` **must exist** in the database enum because the API and UI use:

```json
{
  "status": "SCHEDULED"
}
```

### Important correction

`CANCELLED` **must exist** because the API supports:

```text
POST /assessments/{assessmentId}/cancel
```

---

# 8. Assessment Scheduling Rules

An assessment has fixed scheduling fields:

```text
scheduled_start_at
scheduled_end_at
```

There are no separate:

```text
started_at
ended_at
```

fields in the finalized assessment design.

The candidate can start only when:

```text
scheduled_start_at <= current_time < scheduled_end_at
```

The backend remains responsible for enforcing this rule.

`duration_minutes` represents the configured assessment duration.

The scheduled start/end window is the authoritative access window.

---

# 9. ASSESSMENT Relationship

An assessment belongs to:

```text
ONE workspace
ONE candidate
```

Therefore:

```text
WORKSPACE
    1
    |
    N
ASSESSMENT
    N
    |
    1
CANDIDATE
```

A candidate can have many assessments:

```text
Candidate
   |
   +── Assessment A
   +── Assessment B
   +── Assessment C
   +── Assessment D
```

This directly supports the requirement that one candidate can take multiple assessments created by recruiters.

---

# 10. REPOSITORY_ANALYSIS

Repository analysis is intentionally separated from the core `ASSESSMENT` table.

It stores the structured information extracted from the repository before the information is provided to the LLM.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Analysis identifier |
| `assessment_id` | UUID | FK → ASSESSMENT.id, UNIQUE, NOT NULL | Related assessment |
| `analysis_status` | ENUM | NOT NULL | Analysis lifecycle |
| `repository_structure` | JSON | NOT NULL | Folder/file structure |
| `controllers` | JSON | NULL | Controller metadata |
| `services` | JSON | NULL | Service metadata |
| `repositories` | JSON | NULL | Repository metadata |
| `entities` | JSON | NULL | Entity metadata |
| `endpoints` | JSON | NULL | API endpoint metadata |
| `entity_fields` | JSON | NULL | Entity field metadata |
| `service_methods` | JSON | NULL | Service method metadata |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |
| `updated_at` | TIMESTAMP | NOT NULL | Last update time |

### Why it is separate

The assessment contains the configuration of the assessment.

The repository analysis contains the result of analyzing the supplied repository.

This separation allows repository analysis to be processed independently from the assessment configuration.

---

# 11. REPOSITORY

Stores repository execution/storage information.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Repository record |
| `assessment_id` | UUID | FK → ASSESSMENT.id, UNIQUE, NOT NULL | Assessment |
| `original_repository_path` | VARCHAR(2048) | NOT NULL | Internal path of cloned original repository |
| `candidate_repository_path` | VARCHAR(2048) | NULL | Internal path of candidate-modified repository |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |
| `updated_at` | TIMESTAMP | NOT NULL | Last update time |

### Important

These paths are internal backend/execution data.

They must never be returned to the candidate frontend.

---

# 12. FEATURE_SPECIFICATION

Stores the AI-generated feature that the candidate must implement.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Feature identifier |
| `assessment_id` | UUID | FK → ASSESSMENT.id, UNIQUE, NOT NULL | Assessment |
| `title` | VARCHAR(255) | NOT NULL | Feature title |
| `description` | TEXT | NOT NULL | Feature description |
| `requirements` | JSON | NOT NULL | Functional requirements |
| `endpoint` | VARCHAR(500) | NULL | Suggested/new API endpoint |
| `http_method` | VARCHAR(20) | NULL | HTTP method |
| `request_specification` | JSON | NULL | Request format |
| `response_specification` | JSON | NULL | Expected API contract |
| `constraints` | JSON | NULL | Implementation constraints |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |
| `updated_at` | TIMESTAMP | NOT NULL | Last update time |

### Response contract

The feature specification contains the **API response specification** that defines what the candidate should implement.

The hidden evaluator's actual expected response for individual test cases is stored separately in `TEST_CASE`.

---

# 13. TEST_CASE

Stores hidden test cases generated for the assessment.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Test case identifier |
| `assessment_id` | UUID | FK → ASSESSMENT.id, NOT NULL | Assessment |
| `test_case_number` | INTEGER | NOT NULL | Test case sequence |
| `http_method` | VARCHAR(20) | NOT NULL | HTTP method |
| `endpoint` | VARCHAR(500) | NOT NULL | Endpoint under test |
| `request_data` | JSON | NULL | Request/query/body |
| `expected_status_code` | INTEGER | NOT NULL | Expected HTTP status |
| `expected_response` | JSON | NOT NULL | Hidden expected response |
| `assertions` | JSON | NULL | Additional evaluator assertions |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |

### Constraint

```sql
UNIQUE(assessment_id, test_case_number)
```

### Security

`expected_response` and `assertions` are server-side hidden data.

They must never be returned through candidate-facing APIs.

---

# 14. SUBMISSION

Stores the candidate's final submission.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Submission identifier |
| `assessment_id` | UUID | FK → ASSESSMENT.id, NOT NULL | Assessment |
| `candidate_repository_path` | VARCHAR(2048) | NOT NULL | Internal submitted repository path |
| `submitted_at` | TIMESTAMP | NOT NULL | Submission time |
| `status` | ENUM | NOT NULL | Submission status |

A candidate may have multiple submission attempts if the product rules later support it.

---

# 15. EXECUTION

Stores Docker/application execution information.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Execution identifier |
| `assessment_id` | UUID | FK → ASSESSMENT.id, NOT NULL | Assessment |
| `submission_id` | UUID | FK → SUBMISSION.id, NULL | Related submission |
| `container_id` | VARCHAR(255) | NULL | Docker container identifier |
| `build_status` | ENUM | NOT NULL | Build state |
| `container_status` | ENUM | NOT NULL | Container state |
| `application_status` | ENUM | NOT NULL | Application state |
| `started_at` | TIMESTAMP | NULL | Execution start |
| `stopped_at` | TIMESTAMP | NULL | Execution stop |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |

---

# 16. TEST_RESULT

Stores the result of executing each hidden test case.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PK, NOT NULL | Result identifier |
| `test_case_id` | UUID | FK → TEST_CASE.id, NOT NULL | Test case |
| `execution_id` | UUID | FK → EXECUTION.id, NOT NULL | Execution |
| `status` | ENUM | NOT NULL | `PASSED` / `FAILED` |
| `actual_status_code` | INTEGER | NULL | Actual HTTP status |
| `actual_response` | JSON | NULL | Actual response returned by candidate application |
| `execution_time_ms` | BIGINT | NULL | Test execution duration |
| `failure_reason` | TEXT | NULL | Failure explanation |
| `created_at` | TIMESTAMP | NOT NULL | Creation time |

The recruiter can use these records to calculate:

```text
score = passed_tests / total_tests × 100
```

---

# 17. Final Database Relationship

```text
USER
│
├── RECRUITER
│      │
│      └────< WORKSPACE
│                    │
│                    └────< WORKSPACE_CANDIDATE >──── CANDIDATE(USER)
│                                                         │
│                                                         └────< ASSESSMENT
│                                                                  │
│                                                                  ├──── 1 REPOSITORY
│                                                                  ├──── 1 REPOSITORY_ANALYSIS
│                                                                  ├──── 1 FEATURE_SPECIFICATION
│                                                                  ├────< TEST_CASE
│                                                                  │        │
│                                                                  │        └────< TEST_RESULT
│                                                                  │
│                                                                  └────< SUBMISSION
│                                                                           │
│                                                                           └────< EXECUTION
│                                                                                   │
│                                                                                   └────< TEST_RESULT
```

---

# 18. Critical Corrections Applied

## Correction 1 — Remove `exam_id`

### Previous problem

```text
ASSESSMENT.exam_id
UNIQUE
NOT NULL
```

The requirements explicitly removed the exam-ID concept, but the database still required it.

### Final design

`ASSESSMENT.exam_id` is completely removed.

The assessment primary key is:

```text
ASSESSMENT.id
```

No exam ID is generated, stored, shared, or entered by the candidate.

---

## Correction 2 — Add `CANCELLED`

### Previous problem

The API contained:

```text
POST /assessments/{assessmentId}/cancel
```

but the database enum did not contain `CANCELLED`.

### Final design

```text
AssessmentStatus:
    CREATING
    ANALYZING
    GENERATING_FEATURE
    GENERATING_TESTS
    READY
    SCHEDULED
    IN_PROGRESS
    EVALUATING
    COMPLETED
    CANCELLED
    FAILED
    EXPIRED
```

---

## Correction 3 — Make `USER.email` unique

### Previous problem

The application treated email as a unique identifier, but the database did not enforce uniqueness.

### Final design

```sql
email VARCHAR(255) NOT NULL UNIQUE
```

This makes email uniqueness a database-level invariant rather than only an application-level assumption.

---

## Correction 4 — Add `SCHEDULED`

### Previous problem

API/UI returned:

```json
"status": "SCHEDULED"
```

but the database enum did not contain `SCHEDULED`.

### Final design

`SCHEDULED` is now part of `AssessmentStatus`.

The lifecycle can therefore transition conceptually as:

```text
CREATING
   ↓
ANALYZING
   ↓
GENERATING_FEATURE
   ↓
GENERATING_TESTS
   ↓
READY
   ↓
SCHEDULED
   ↓
IN_PROGRESS
   ↓
EVALUATING
   ↓
COMPLETED
```

Alternative terminal paths include:

```text
CREATING / ANALYZING / GENERATING_*
        ↓
      FAILED
```

and:

```text
SCHEDULED
   ↓
CANCELLED
```

or:

```text
SCHEDULED
   ↓
EXPIRED
```

---

# 19. Database Invariants

The following must be enforced by the database/backend:

| Rule | Enforcement |
|---|---|
| User email cannot be duplicated | `UNIQUE(USER.email)` |
| Every workspace belongs to a recruiter | FK |
| Candidate must already exist before workspace association | FK + application validation |
| Candidate cannot be duplicated in same workspace | `UNIQUE(workspace_id, candidate_id)` |
| Assessment belongs to one candidate | FK |
| Assessment belongs to one workspace | FK |
| One candidate can have many assessments | No unique constraint on `candidate_id` in ASSESSMENT |
| One workspace can have many assessments | No unique constraint on `workspace_id` in ASSESSMENT |
| Assessment has fixed schedule | `scheduled_start_at`, `scheduled_end_at` NOT NULL |
| Assessment status matches API/UI | `SCHEDULED` included in enum |
| Assessment can be cancelled | `CANCELLED` included in enum |
| No exam-ID workflow | No `exam_id` column |
| Repository analysis is separated | Dedicated `REPOSITORY_ANALYSIS` table |
| Feature belongs to assessment | FK + unique `assessment_id` |
| Hidden tests belong to assessment | FK |
| Hidden expected responses stay server-side | Candidate API authorization rule |

---

# 20. Important Implementation Note

The database, API specification, and UI specification must use exactly the same enum values.

In particular:

```text
AssessmentStatus
    READY
    SCHEDULED
    IN_PROGRESS
    EVALUATING
    COMPLETED
    CANCELLED
    FAILED
    EXPIRED
```

must not be independently redefined in the frontend, backend, and database.

The database schema should be treated as the source of truth for persisted assessment state, while the backend controls valid state transitions.
