# EVIDENCE --- Final Database Design

## Document Metadata

-   **Project:** EVIDENCE --- AI-Powered Technical Assessment Platform
-   **Database Design Version:** Final
-   **Database:** PostgreSQL
-   **Primary Key Strategy:** UUID-based identifiers with composite keys
    for relationship / 1:1 tables

## 1. Purpose

This document defines the finalized relational database structure for
the current EVIDENCE system.

It focuses on:

-   Entities
-   Primary keys
-   Foreign keys
-   Important constraints
-   Relationships
-   Finalized key-structure decisions

The design retains the existing architecture while applying the
finalized key-structure changes.

## 2. Important Finalized Decisions

1.  `USER` uses a UUID `id` as its primary key.
2.  `USER.email` remains `UNIQUE`.
3.  `WORKSPACE_CANDIDATE` uses a composite primary key:
    -   `(workspace_id, candidate_id)`
4.  The following 1:1 child tables use their foreign key as their
    primary key:
    -   `REPOSITORY_ANALYSIS`
    -   `ASSESSMENT_WORKSPACE`
    -   `FEATURE_SPECIFICATION`
    -   `SUBMISSION`
    -   `EVALUATION_REPORT`
5.  `ASSESSMENT` does **not** contain `exam_id`.
6.  `TEST_RESULT` uses `test_case_id` as its primary key.
7.  `TEST_RESULT` does **not** contain `report_id`.

------------------------------------------------------------------------

# 3. Final Database Overview

The final design contains **11 core tables**.

UUIDs are the default identifiers for independent domain entities.

Composite keys are used where a row represents a unique relationship.

One-to-one child tables use the parent's foreign key as their primary
key.

  ----------------------------------------------------------------------------------
  Table                     Primary Key                      Purpose
  ------------------------- -------------------------------- -----------------------
  `USER`                    `id` (UUID)                      System users:
                                                             recruiters and
                                                             candidates

  `WORKSPACE`               `id` (UUID)                      Recruiter-owned
                                                             assessment workspace

  `WORKSPACE_CANDIDATE`     `(workspace_id, candidate_id)`   Candidate membership in
                                                             a workspace

  `ASSESSMENT`              `id` (UUID)                      Assessment assigned to
                                                             a workspace candidate

  `REPOSITORY_ANALYSIS`     `assessment_id`                  One repository analysis
                                                             per assessment

  `ASSESSMENT_WORKSPACE`    `assessment_id`                  One execution workspace
                                                             per assessment

  `FEATURE_SPECIFICATION`   `assessment_id`                  One generated feature
                                                             specification per
                                                             assessment

  `TEST_CASE`               `id` (UUID)                      Generated black-box
                                                             test case

  `SUBMISSION`              `assessment_id`                  One final submission
                                                             per assessment

  `EVALUATION_REPORT`       `submission_id`                  One evaluation report
                                                             per submission

  `TEST_RESULT`             `test_case_id`                   Final result for a test
                                                             case
  ----------------------------------------------------------------------------------

------------------------------------------------------------------------

# 4. Entity Relationship Structure

## 4.1 Core Hierarchy

``` text
USER
  └── WORKSPACE
        └── WORKSPACE_CANDIDATE
              └── ASSESSMENT
```

## 4.2 Assessment Preparation

``` text
ASSESSMENT
  ├── REPOSITORY_ANALYSIS
  ├── ASSESSMENT_WORKSPACE
  └── FEATURE_SPECIFICATION
        └── TEST_CASE
              └── TEST_RESULT
```

## 4.3 Submission and Evaluation

``` text
ASSESSMENT
  └── SUBMISSION
        └── EVALUATION_REPORT
```

## 4.4 Complete Relationship Map

``` text
USER
├── WORKSPACE
│   └── WORKSPACE_CANDIDATE
│       └── ASSESSMENT
│           ├── REPOSITORY_ANALYSIS
│           ├── ASSESSMENT_WORKSPACE
│           └── FEATURE_SPECIFICATION
│               └── TEST_CASE
│                   └── TEST_RESULT
│
└── WORKSPACE_CANDIDATE
    └── ASSESSMENT
        └── SUBMISSION
            └── EVALUATION_REPORT
```

------------------------------------------------------------------------

# 5. Table Definitions

## 5.1 USER

**Purpose:** Stores system users, including recruiters and candidates.

  -------------------------------------------------------------------------------
  Column            Type           Null           Key /          Description
                                                  Constraint     
  ----------------- -------------- -------------- -------------- ----------------
  `id`              UUID           NO             PK             Stable internal
                                                                 user identifier

  `name`            VARCHAR(150)   NO             ---            User
                                                                 display/name

  `email`           VARCHAR(255)   NO             UNIQUE         Login/business
                                                                 email

  `role`            ENUM           NO             ---            `RECRUITER` or
                                                                 `CANDIDATE`

  `password_hash`   VARCHAR / TEXT NO             ---            BCrypt or
                                                                 configured
                                                                 password hash

  `auth_provider`   ENUM           NO             ---            Authentication
                                                                 provider

  `created_at`      TIMESTAMPTZ    NO             DEFAULT        Creation
                                                                 timestamp

  `updated_at`      TIMESTAMPTZ    NO             DEFAULT        Last update
                                                                 timestamp
  -------------------------------------------------------------------------------

------------------------------------------------------------------------

## 5.2 WORKSPACE

**Purpose:** Represents a recruiter-owned assessment workspace.

  Column           Type           Null   Key / Constraint   Description
  ---------------- -------------- ------ ------------------ ----------------------------
  `id`             UUID           NO     PK                 Workspace identifier
  `recruiter_id`   UUID           NO     FK → `USER.id`     Owner/recruiter
  `name`           VARCHAR(200)   NO     ---                Workspace name
  `description`    TEXT           YES    ---                Workspace description
  `status`         ENUM           NO     ---                Workspace lifecycle status
  `created_at`     TIMESTAMPTZ    NO     DEFAULT            Creation timestamp
  `updated_at`     TIMESTAMPTZ    NO     DEFAULT            Last update timestamp

------------------------------------------------------------------------

## 5.3 WORKSPACE_CANDIDATE

**Purpose:** Represents candidate membership in a workspace.

  ------------------------------------------------------------------------------
  Column           Type           Null           Key / Constraint Description
  ---------------- -------------- -------------- ---------------- --------------
  `workspace_id`   UUID           NO             PK, FK →         Workspace
                                                 `WORKSPACE.id`   

  `candidate_id`   UUID           NO             PK, FK →         Candidate
                                                 `USER.id`        

  `created_at`     TIMESTAMPTZ    NO             DEFAULT          Membership
                                                                  creation
                                                                  timestamp
  ------------------------------------------------------------------------------

**Primary Key:**

``` text
(workspace_id, candidate_id)
```

------------------------------------------------------------------------

## 5.4 ASSESSMENT

**Purpose:** Represents an assessment assigned to a workspace candidate.

  ---------------------------------------------------------------------------------------
  Column                     Type           Null           Key /          Description
                                                           Constraint     
  -------------------------- -------------- -------------- -------------- ---------------
  `id`                       UUID           NO             PK             Assessment
                                                                          identifier

  `workspace_id`             UUID           NO             Composite FK   Workspace
                                                                          portion of
                                                                          assignment

  `candidate_id`             UUID           NO             Composite FK   Candidate
                                                                          portion of
                                                                          assignment

  `repository_url`           TEXT           NO             ---            Repository
                                                                          source

  `branch_name`              VARCHAR(255)   NO             ---            Branch to
                                                                          clone/analyze

  `backend_root_directory`   TEXT           NO             ---            Backend project
                                                                          root

  `difficulty`               ENUM           NO             ---            Assessment
                                                                          difficulty

  `duration_minutes`         INTEGER        NO             CHECK \> 0     Allowed
                                                                          duration

  `scheduled_start_at`       TIMESTAMPTZ    NO             CHECK with end Assessment
                                                                          start

  `scheduled_end_at`         TIMESTAMPTZ    NO             CHECK \> start Assessment end

  `status`                   ENUM           NO             ---            Assessment
                                                                          lifecycle
                                                                          status

  `created_at`               TIMESTAMPTZ    NO             DEFAULT        Creation
                                                                          timestamp

  `updated_at`               TIMESTAMPTZ    NO             DEFAULT        Last update
                                                                          timestamp
  ---------------------------------------------------------------------------------------

**Composite Foreign Key:**

``` text
(workspace_id, candidate_id)
    → WORKSPACE_CANDIDATE(workspace_id, candidate_id)
```

**Important:** `ASSESSMENT` does not contain `exam_id`.

------------------------------------------------------------------------

## 5.5 REPOSITORY_ANALYSIS

**Purpose:** Stores repository analysis results for an assessment.

**Relationship:** One repository analysis per assessment.

  ----------------------------------------------------------------------------------------
  Column                    Type           Null           Key / Constraint  Description
  ------------------------- -------------- -------------- ----------------- --------------
  `assessment_id`           UUID           NO             PK, FK →          One analysis
                                                          `ASSESSMENT.id`   per assessment

  `project_structure`       JSONB          NO             ---               Detected
                                                                            project
                                                                            structure

  `source_code_structure`   JSONB          NO             ---               Detected
                                                                            source-code
                                                                            structure

  `content_details`         JSONB          NO             ---               Relevant
                                                                            repository
                                                                            content

  `created_at`              TIMESTAMPTZ    NO             DEFAULT           Creation
                                                                            timestamp

  `completed_at`            TIMESTAMPTZ    YES            ---               Completion
                                                                            timestamp
  ----------------------------------------------------------------------------------------

**Primary Key:**

``` text
assessment_id
```

------------------------------------------------------------------------

## 5.6 ASSESSMENT_WORKSPACE

**Purpose:** Stores the filesystem/workspace information used to execute
an assessment.

**Relationship:** One execution workspace per assessment.

  -------------------------------------------------------------------------------------------------
  Column                       Type           Null           Key / Constraint  Description
  ---------------------------- -------------- -------------- ----------------- --------------------
  `assessment_id`              UUID           NO             PK, FK →          One workspace per
                                                             `ASSESSMENT.id`   assessment

  `original_repository_path`   TEXT           NO             ---               Original cloned
                                                                               repository path

  `candidate_workspace_path`   TEXT           YES            ---               Candidate-specific
                                                                               working path

  `created_at`                 TIMESTAMPTZ    NO             DEFAULT           Creation timestamp

  `updated_at`                 TIMESTAMPTZ    NO             DEFAULT           Last update
                                                                               timestamp
  -------------------------------------------------------------------------------------------------

**Primary Key:**

``` text
assessment_id
```

------------------------------------------------------------------------

## 5.7 FEATURE_SPECIFICATION

**Purpose:** Stores the generated feature specification for an
assessment.

**Relationship:** One feature specification per assessment.

  ------------------------------------------------------------------------------------------
  Column                     Type           Null           Key / Constraint  Description
  -------------------------- -------------- -------------- ----------------- ---------------
  `assessment_id`            UUID           NO             PK, FK →          One feature
                                                           `ASSESSMENT.id`   specification
                                                                             per assessment

  `feature_name`             VARCHAR(255)   NO             ---               Feature name

  `description`              TEXT           NO             ---               Feature
                                                                             description

  `requirements`             JSONB          YES            ---               Functional
                                                                             requirements

  `request_specification`    JSONB          YES            ---               Request
                                                                             contract

  `response_specification`   JSONB          YES            ---               Response
                                                                             contract

  `constraints`              JSONB          YES            ---               Feature
                                                                             constraints

  `created_at`               TIMESTAMPTZ    NO             DEFAULT           Creation
                                                                             timestamp

  `updated_at`               TIMESTAMPTZ    NO             DEFAULT           Last update
                                                                             timestamp
  ------------------------------------------------------------------------------------------

**Primary Key:**

``` text
assessment_id
```

------------------------------------------------------------------------

## 5.8 TEST_CASE

**Purpose:** Stores generated black-box test cases.

  ----------------------------------------------------------------------------------------------------------------
  Column                   Type           Null           Key / Constraint                        Description
  ------------------------ -------------- -------------- --------------------------------------- -----------------
  `id`                     UUID           NO             PK                                      Test case
                                                                                                 identifier

  `assessment_id`          UUID           NO             FK →                                    Assessment
                                                         `FEATURE_SPECIFICATION.assessment_id`   context

  `test_case_number`       INTEGER        NO             UNIQUE with `assessment_id`             Stable test
                                                                                                 ordering/number

  `test_type`              ENUM           NO             ---                                     Test category

  `http_method`            VARCHAR(20)    NO             ---                                     HTTP method

  `endpoint`               VARCHAR(500)   NO             ---                                     Endpoint under
                                                                                                 test

  `request_data`           JSONB          YES            ---                                     Request
                                                                                                 payload/data

  `expected_status_code`   INTEGER        NO             ---                                     Expected HTTP
                                                                                                 status

  `expected_response`      JSONB          YES            ---                                     Expected response

  `assertions`             JSONB          NO             ---                                     Validation
                                                                                                 assertions

  `weight`                 DECIMAL(5,2)   NO             CHECK ≥ 0                               Scoring weight

  `created_at`             TIMESTAMPTZ    NO             DEFAULT                                 Creation
                                                                                                 timestamp
  ----------------------------------------------------------------------------------------------------------------

**Unique Constraint:**

``` text
UNIQUE(assessment_id, test_case_number)
```

------------------------------------------------------------------------

## 5.9 SUBMISSION

**Purpose:** Stores the final submission for an assessment.

**Relationship:** One final submission per assessment.

  -------------------------------------------------------------------------------------
  Column                 Type           Null           Key / Constraint  Description
  ---------------------- -------------- -------------- ----------------- --------------
  `assessment_id`        UUID           NO             PK, FK →          One final
                                                       `ASSESSMENT.id`   submission per
                                                                         assessment

  `submitted_at`         TIMESTAMPTZ    NO             ---               Submission
                                                                         time

  `time_taken_seconds`   BIGINT         NO             CHECK ≥ 0         Candidate
                                                                         duration

  `status`               ENUM           NO             ---               Submission
                                                                         status
  -------------------------------------------------------------------------------------

**Primary Key:**

``` text
assessment_id
```

------------------------------------------------------------------------

## 5.10 EVALUATION_REPORT

**Purpose:** Stores the evaluation result for a submission.

**Relationship:** One evaluation report per submission.

  ------------------------------------------------------------------------------------------------
  Column                 Type           Null           Key / Constraint             Description
  ---------------------- -------------- -------------- ---------------------------- --------------
  `submission_id`        UUID           NO             PK, FK →                     One report per
                                                       `SUBMISSION.assessment_id`   submission

  `score`                DECIMAL(5,2)   NO             CHECK 0--100                 Final score

  `total_tests`          INTEGER        NO             CHECK ≥ 0                    Total tests

  `passed_tests`         INTEGER        NO             CHECK ≥ 0                    Passed tests

  `failed_tests`         INTEGER        NO             CHECK ≥ 0                    Failed tests

  `build_status`         ENUM           NO             ---                          Build outcome

  `application_status`   ENUM           NO             ---                          Application
                                                                                    startup
                                                                                    outcome

  `time_taken_seconds`   BIGINT         NO             CHECK ≥ 0                    Evaluation
                                                                                    duration

  `status`               ENUM           NO             ---                          Evaluation
                                                                                    status

  `evaluated_at`         TIMESTAMPTZ    YES            ---                          Evaluation
                                                                                    completion
                                                                                    time
  ------------------------------------------------------------------------------------------------

**Primary Key:**

``` text
submission_id
```

------------------------------------------------------------------------

## 5.11 TEST_RESULT

**Purpose:** Stores the final execution result for a generated test
case.

  ------------------------------------------------------------------------------------
  Column                 Type           Null           Key / Constraint Description
  ---------------------- -------------- -------------- ---------------- --------------
  `test_case_id`         UUID           NO             PK, FK →         Uniquely
                                                       `TEST_CASE.id`   identifies
                                                                        final test
                                                                        result

  `status`               ENUM           NO             ---              `PASS`,
                                                                        `FAIL`, etc.

  `actual_status_code`   INTEGER        YES            ---              Observed HTTP
                                                                        status

  `actual_response`      JSONB          YES            ---              Observed
                                                                        response

  `failure_reason`       TEXT           YES            ---              Reason for
                                                                        failure

  `execution_time_ms`    BIGINT         YES            CHECK ≥ 0        Test execution
                                                                        duration
  ------------------------------------------------------------------------------------

**Primary Key:**

``` text
test_case_id
```

**Important:** `TEST_RESULT` does not contain `report_id`.

------------------------------------------------------------------------

# 6. Primary Key Rules

## Independent Entities

Independent entities use UUID primary keys:

-   `USER.id`
-   `WORKSPACE.id`
-   `ASSESSMENT.id`
-   `TEST_CASE.id`

## Relationship / 1:1 Entities

Relationship or 1:1 entities use relational keys:

-   `WORKSPACE_CANDIDATE`
-   `REPOSITORY_ANALYSIS`
-   `ASSESSMENT_WORKSPACE`
-   `FEATURE_SPECIFICATION`
-   `SUBMISSION`
-   `EVALUATION_REPORT`

## TEST_RESULT

`TEST_RESULT.test_case_id` is the primary key because each generated
test case has one final result.

------------------------------------------------------------------------

# 7. Foreign Key Rules

The finalized foreign-key relationships are:

``` text
WORKSPACE.recruiter_id
    → USER.id

WORKSPACE_CANDIDATE.workspace_id
    → WORKSPACE.id

WORKSPACE_CANDIDATE.candidate_id
    → USER.id

ASSESSMENT(workspace_id, candidate_id)
    → WORKSPACE_CANDIDATE(workspace_id, candidate_id)

REPOSITORY_ANALYSIS.assessment_id
    → ASSESSMENT.id

ASSESSMENT_WORKSPACE.assessment_id
    → ASSESSMENT.id

FEATURE_SPECIFICATION.assessment_id
    → ASSESSMENT.id

TEST_CASE.assessment_id
    → FEATURE_SPECIFICATION.assessment_id

SUBMISSION.assessment_id
    → ASSESSMENT.id

EVALUATION_REPORT.submission_id
    → SUBMISSION.assessment_id

TEST_RESULT.test_case_id
    → TEST_CASE.id
```

------------------------------------------------------------------------

# 8. Recommended Database Constraints

The following constraints are recommended.

## USER

``` text
USER.email UNIQUE
USER.email NOT NULL
```

## WORKSPACE_CANDIDATE

``` text
PRIMARY KEY(workspace_id, candidate_id)
```

## TEST_CASE

``` text
UNIQUE(assessment_id, test_case_number)
```

## ASSESSMENT

``` text
duration_minutes > 0
scheduled_end_at > scheduled_start_at
```

## TEST_RESULT

``` text
execution_time_ms >= 0
```

When `execution_time_ms` is present.

## EVALUATION_REPORT

``` text
score BETWEEN 0 AND 100

passed_tests >= 0
failed_tests >= 0

passed_tests + failed_tests <= total_tests
```

## TEST_CASE

``` text
weight >= 0
```

------------------------------------------------------------------------

# 9. Recommended Indexes

Create indexes for frequent lookup paths, especially:

``` text
WORKSPACE(recruiter_id)

WORKSPACE_CANDIDATE(candidate_id)

ASSESSMENT(workspace_id, candidate_id)

ASSESSMENT(scheduled_start_at, scheduled_end_at)

TEST_CASE(assessment_id)
```

Foreign-key indexes should also be created where PostgreSQL query
patterns and delete/update operations benefit from them.

------------------------------------------------------------------------

# 10. Final Design Decisions

  Decision                Final Choice
  ----------------------- ---------------------------------------------
  User identity           UUID `user.id` is the primary key
  User email              Unique, not a foreign-key identifier
  Workspace identity      UUID `workspace.id`
  Workspace owner         `workspace.recruiter_id → user.id`
  Workspace membership    Composite PK `(workspace_id, candidate_id)`
  Assessment identity     UUID `assessment.id`
  Assessment identifier   No `exam_id` attribute
  Repository analysis     `assessment_id` is PK
  Assessment workspace    `assessment_id` is PK
  Feature specification   `assessment_id` is PK
  Submission              `assessment_id` is PK
  Evaluation report       `submission_id` is PK
  Test result             `test_case_id` is PK; no `report_id`

------------------------------------------------------------------------

# 11. Implementation Finalization Note

This schema represents the finalized relational structure agreed during
the design discussion.

Before implementing production DDL, the following items should be
explicitly encoded:

1.  Application-specific ENUM values
2.  Exact `VARCHAR` lengths
3.  UUID generation defaults
4.  `ON DELETE` actions
5.  `ON UPDATE` actions
6.  Final indexing strategy

## Source Scope

This Markdown document is a structured conversion of the provided
**EVIDENCE --- Final Database Design** PDF. It preserves the database
entities, relationships, constraints, and final design decisions
contained in the source document without adding external database design
decisions.
