# EVIDENCE — Updated System Overview, Requirements & End-to-End User Flows

**Software Requirements Specification — MVP**  
**Java Spring Boot Backend Assessment Platform**  
**Version 1.1 — Separate Recruiter/Candidate Accounts & Scheduled Candidate Dashboard**

EVIDENCE

Updated System Overview, Requirements & End-to-End User Flows

Software Requirements Specification — MVP
Java Spring Boot Backend Assessment Platform

Version 1.1 — Separate Recruiter/Candidate Accounts & Scheduled Candidate Dashboard

1. Project Overview

Evidence is a recruiter-focused technical assessment platform designed to verify whether a candidate can genuinely understand and modify a software project. Instead of requiring a recruiter to study a candidate's GitHub repository and manually design project-specific technical questions, Evidence analyzes the repository and generates a practical, project-specific coding task.

For the MVP, Evidence focuses on Java Spring Boot backend repositories. The recruiter creates a workspace, selects an existing candidate by the candidate's unique email address or creates a new candidate account, and creates one or more assessments for that candidate. Each assessment contains repository configuration, difficulty, a duration, and a fixed scheduled start and end time.

The candidate has a separate candidate account and dashboard. The dashboard shows scheduled assessments and completed assessments. When an assessment reaches its scheduled time, the candidate can enter the coding environment and implement the generated feature.

The modified application is built and executed in an isolated Docker environment. Hidden black-box HTTP tests send requests to the running application and compare actual responses against expected responses and assertions. The system then produces an evaluation score out of 100.

1.1 Problem Statement

In a conventional technical interview, verifying whether a candidate genuinely built a project requires the recruiter or interviewer to understand the project first and then design questions around its architecture, implementation, and technical decisions. This is time consuming and difficult to standardize.

Evidence reduces this effort by converting the candidate's own software project into a practical, project-specific coding assessment. The candidate is tested against the actual project rather than a generic coding question.

1.2 Core Value Proposition

2. MVP Scope

Java Spring Boot backend repositories.

GitHub repository-based assessments.

One generated feature per assessment.

Black-box HTTP/API verification.

Browser-based code editing using Monaco Editor.

Docker-based application execution.

Separate recruiter and candidate accounts.

Recruiter-created candidate profiles.

Candidate selection by unique email address.

Multiple assessments for the same candidate.

Workspace-based candidate management.

Fixed scheduled assessment start and end times.

Candidate dashboard showing scheduled and completed assessments.

Outside the current MVP scope:

Frontend repository assessment.

Non-Java backend frameworks.

Generic algorithm/DSA assessments.

Interactive terminal access for candidates.

White-box source-code scoring as the primary evaluation method.

3. System Actors

4. Functional Requirements

5. Non-Functional Requirements

6. Main UI Structure

7. Recruiter User Flow — In Depth

7.1 Enter Evidence

The recruiter visits the landing page and selects the recruiter entry point.

7.2 Recruiter Sign Up / Login

A new recruiter creates a recruiter account. An existing recruiter logs in. Recruiter and candidate authentication are separate.

7.3 Recruiter Dashboard

After authentication, the recruiter sees statistics such as workspaces, assessments, candidates, and assessment processing statuses.

7.4 Create Workspace

The recruiter creates a workspace representing a recruitment or placement context. Example: a specific company placement drive at a specific college.

7.5 Open Workspace

The workspace displays its candidates and assessments.

7.6 Find Existing Candidate

The recruiter selects Add/Select Candidate and types the candidate's email. Email is the unique identifier used to locate the candidate account.

7.7 Candidate Exists

If the email matches an existing candidate account, the recruiter selects that candidate and associates the candidate with the workspace.

If no candidate account exists for the entered email, the recruiter cannot proceed with assessment creation. The candidate must first register a candidate account through the separate candidate sign-up flow. Once the candidate has registered, the recruiter can search for the candidate by email and continue.

If no candidate account exists for the email, the recruiter creates the candidate profile/account and then associates that candidate with the workspace.

7.9 Create Assessment for Candidate

The recruiter selects a candidate from the workspace and can create an assessment. The same candidate can receive multiple assessments.

7.10 Configure Assessment

The recruiter enters repository URL, branch, backend root directory, difficulty, duration, scheduled start time, and scheduled end time.

7.11 Create Assessment

The system validates the configuration and creates the assessment record. The recruiter receives an immediate response while the preparation pipeline continues asynchronously.

7.12 Repository Processing

Evidence clones the repository, prepares the original assessment workspace, and extracts the Spring Boot project structure.

7.13 AI Processing

The structured repository information is passed to the AI pipeline to select an appropriate feature and generate the feature specification.

7.14 Hidden Tests

Hidden HTTP test cases are generated for the feature, including request data, expected status codes, expected responses, and assertions.

7.15 Processing Status

The recruiter can see the assessment moving through repository cloning, analysis, feature generation, test generation, and ready/failed states.

7.16 Assessment Ready

Once preparation is successful, the assessment becomes available according to its scheduled start/end window. There is no separate exam ID creation or exam-ID sharing step.

7.17 Candidate Notification / Communication

The candidate can see the assessment from the candidate dashboard. Any additional recruiter-to-candidate communication is outside the core assessment workflow.

7.18 Review Results

After completion, the recruiter opens the report to view score, passed/failed tests, build/application status, and assessment outcome.

7.19 Recruiter Flow — Visual Summary

Landing Page → Recruiter Login/Sign Up → Recruiter Dashboard → Create/Open Workspace → Search Candidate by Email → Select Existing Candidate OR Create Candidate → Add Candidate to Workspace → Create Assessment → Repository Configuration → Async Repository Analysis → AI Feature Generation → Hidden Test Generation → Assessment Ready → Candidate Dashboard → Candidate Completes Assessment → Recruiter Views Report

8. Candidate User Flow — In Depth

8.1 Candidate Sign Up / Login

The candidate uses the separate candidate authentication flow to create or access the candidate account.

8.2 Candidate Dashboard

After login, the candidate sees two primary assessment groups: Scheduled Assessments and Completed Assessments.

8.3 Scheduled Assessments

Scheduled Assessments contain assessments assigned to the candidate that are ready or scheduled for a future time. Each assessment displays relevant information such as assessment name/context, scheduled start, scheduled end, duration, and status.

8.4 Waiting for Scheduled Time

If an assessment has a future scheduled start time, the candidate can see it but cannot begin the coding session before the permitted start time.

8.5 Assessment Becomes Available

When the fixed scheduled start time is reached, the assessment becomes available for the candidate to take, subject to its scheduled end time and assessment status.

8.6 Start Assessment

The candidate selects the available scheduled assessment directly from the dashboard. There is no exam ID entry step.

8.7 Assessment Environment

The candidate enters the browser-based coding environment containing the file explorer, Monaco Editor, feature view, run/stop controls, and log/error area.

8.8 View Feature

The candidate reads the generated feature specification, including the requested functionality, endpoint, HTTP method, request format, response contract, and requirements.

8.9 Explore Existing Project

The candidate navigates the prepared repository and understands the existing Spring Boot architecture.

8.10 Implement Feature

The candidate edits the project using Monaco Editor and implements the requested feature inside the existing application.

8.11 Run Application

The candidate selects Run. Evidence builds the modified project and executes it inside an isolated Docker environment.

8.12 Inspect Logs

Build, startup, and runtime logs are displayed in the log/error section. This is a terminal-style output viewer, not an arbitrary terminal.

8.13 Fix Errors and Re-run

If compilation, build, configuration, or startup fails, the candidate reads the logs, modifies the code, and runs the application again.

8.14 Application Ready

When the application starts successfully, the evaluation engine can execute the hidden HTTP tests.

8.15 Hidden Verification

The evaluator sends hidden requests to the candidate's running application and compares actual responses against expected responses and assertions.

8.16 Submit

When the candidate finishes the implementation, the candidate submits the assessment.

8.17 Evaluation

The system aggregates hidden test outcomes and calculates the score out of 100.

8.18 Completed Assessments

The completed assessment moves from the candidate's scheduled/in-progress view to Completed Assessments. The candidate can review the result information made available to candidates.

8.19 Candidate Dashboard — Conceptual View

8.20 Candidate Flow — Visual Summary

Candidate Sign Up/Login → Candidate Dashboard → Scheduled Assessments → Wait Until Scheduled Start → Select Available Assessment → Coding IDE → View Feature → Explore Repository → Implement Feature → Run → Docker Build/Start → View Logs → Fix/Re-run → Application Ready → Hidden HTTP Tests → Submit → Evaluation → Completed Assessments

9. AI Processing Pipeline

The AI pipeline converts a Spring Boot repository into a project-specific assessment. The repository is first analyzed into structured information so that the feature-generation step can reason about the existing application architecture.

### 10. Candidate Execution and Evaluation Pipeline

10.1 Black-Box Evaluation Model

The hidden evaluator treats the candidate application as an HTTP service. It sends requests to the running application and evaluates observable behavior rather than requiring a particular internal implementation.

Example conceptual test:
Request: POST /api/notes
Body: { title, description }
Expected status: 201
Expected response: required fields and values
Assertions: response structure/content rules
Result: PASS or FAIL

### 11. Assessment Lifecycle

CREATING → ANALYZING → GENERATING_TESTS → SCHEDULED/READY → IN_PROGRESS → SUBMITTED → EVALUATING → COMPLETED

The assessment is associated with a fixed scheduled_start_at and scheduled_end_at. The candidate dashboard uses these timestamps to determine whether an assessment is upcoming, currently available, or no longer available. Candidate access is controlled by the application according to the assessment schedule and lifecycle status.

### 12. Security and Isolation Model

Recruiter and candidate authentication are separate.

Role-specific authorization must prevent candidates from accessing recruiter functionality.

A candidate may access only assessments assigned to that candidate.

A recruiter may access only their own workspaces, candidates, and assessments.

Candidate email is the unique identifier used by recruiters to locate existing candidate accounts.

Hidden test cases, expected responses, and evaluator logic must never be exposed through candidate-facing APIs.

Candidate code must execute inside isolated Docker environments.

Resource limits should be applied to candidate containers to prevent excessive CPU, memory, process, and execution-time usage.

Repository cloning and execution must be treated as untrusted-code operations.

### 13. High-Level System View

Public Landing Page
       ↓
Role Selection
   ↙             ↘
Recruiter Auth     Candidate Auth
   ↓                  ↓
Recruiter UI       Candidate Dashboard
   ↓                  ↓
Workspace → Candidate Selection → Scheduled Assessment
   ↓                  ↓
Assessment Creation   Coding IDE
   ↓                  ↓
Repository Analysis   Candidate Code Changes
   ↓                  ↓
AI Feature + Hidden Tests
   ↓                  ↓
Assessment Ready → Docker Execution → Hidden HTTP Tests
                                      ↓
                               Evaluation Report
                                      ↓
                                Recruiter Reports

### 14. Core Data Model Summary

### 15. End-to-End Example

Assume a recruiter is conducting a placement drive. The recruiter creates a workspace for the drive and needs to assess Candidate A, whose Spring Boot project is hosted on GitHub.

The recruiter logs into the recruiter account.

The recruiter opens the relevant workspace.

The recruiter searches for Candidate A by entering Candidate A's unique email address.

The recruiter searches for Candidate A using Candidate A's unique email address. Candidate A must already have a registered candidate account. If no account exists, the recruiter cannot proceed with the assessment.

The recruiter adds Candidate A to the workspace.

The recruiter creates Assessment 1 for Candidate A with the repository URL, branch, backend root directory, difficulty, duration, scheduled start, and scheduled end.

Evidence processes the repository asynchronously and generates the feature and hidden tests.

The assessment becomes scheduled/ready. There is no exam ID generation and no exam ID sharing step.

Candidate A logs into the separate candidate account and sees the assessment under Scheduled Assessments.

Before the scheduled start time, Candidate A can see the assessment but cannot start it.

At the scheduled start time, Candidate A can select the assessment and enter the coding environment.

Candidate A implements the feature, runs the application, fixes errors using the displayed logs, and re-runs as required.

Evidence executes hidden HTTP tests against the running application.

Candidate A submits the assessment.

The assessment moves to Completed Assessments on the candidate dashboard.

The recruiter can open the assessment report and view the score and test outcomes.

The recruiter can later create Assessment 2, Assessment 3, and additional assessments for the same Candidate A if required.

### 16. Important Terminology

### 17. Current Design Decisions

There is no exam ID creation or exam ID sharing concept in the current design.

Recruiter and candidate have separate authentication flows and accounts.

Recruiters locate existing candidates by unique email address.

If a candidate is not found, the recruiter can create the candidate account/profile.

Candidates are associated with recruiter workspaces.

One candidate can have many assessments.

A recruiter can create many assessments for the same candidate.

The candidate dashboard is the primary entry point for taking assigned assessments.

The candidate dashboard separates scheduled/upcoming assessments from completed assessments.

Assessment access is governed by fixed scheduled start and end timestamps.

The MVP is limited to Java Spring Boot backend projects.

One assessment generates one feature and its hidden HTTP test suite.

Evaluation is primarily black-box HTTP/API testing.

The final score is normalized to 100.

Evidence — Updated MVP System Requirements & User Flow

Candidate Account Prerequisite

A recruiter cannot create a candidate account. Candidate registration and authentication are handled through the separate candidate sign-up/login flow. The recruiter can only search for an existing candidate using the candidate's unique email address, select that candidate, associate the candidate with a workspace, and then create assessments for that candidate. If the candidate has not registered yet, the recruiter must wait until the candidate registers.


| Current Recruitment Process | Evidence |
|---|---|
| Recruiter studies candidate project manually. | System analyzes the repository automatically. |
| Recruiter designs project-specific questions. | AI generates a feature specification based on project structure. |
| Candidate explains or demonstrates the project. | Candidate implements a new feature in the actual project. |
| Verification can be subjective. | Hidden HTTP tests provide objective verification. |
| Assessment creation requires significant effort. | Recruiter creates an assessment from repository configuration. |
| Results depend heavily on interviewer judgment. | Automated test results contribute to a score out of 100. |


| Actor | Purpose | Main Capabilities |
|---|---|---|
| Recruiter | Creates and manages technical assessments. | Separate recruiter account; create workspaces; find/create candidates; create assessments; monitor processing; review reports. |
| Candidate | Completes assigned project-specific coding assessments. | Separate candidate account; view scheduled/completed assessments; enter available assessment; edit repository; run application; inspect logs; submit. |
| Evidence AI Pipeline | Analyzes the repository and generates the assessment. | Extract project metadata; generate feature specification; generate hidden test cases. |
| Assessment Execution Engine | Runs and verifies candidate implementation. | Build Docker image; run application; execute hidden HTTP tests; collect results. |


| ID | Requirement | Description |
|---|---|---|
| FR-01 | Landing Page | The system shall provide a public landing page explaining Evidence, its use cases, and recruiter benefits. |
| FR-02 | Role Selection | The system shall provide separate entry paths for recruiter and candidate users. |
| FR-03 | Recruiter Authentication | The system shall provide separate recruiter sign-up and login. |
| FR-04 | Candidate Authentication | The system shall provide separate candidate sign-up and login. |
| FR-05 | Recruiter Dashboard | The recruiter dashboard shall show workspace, assessment, candidate, and status statistics. |
| FR-06 | Candidate Dashboard | The candidate dashboard shall show scheduled assessments and completed assessments. |
| FR-07 | Workspace Management | A recruiter shall be able to create and manage multiple workspaces. |
| FR-08 | Candidate Search | A recruiter shall be able to find an existing candidate by typing the candidate's unique email address. |
| FR-09 | Candidate Creation | If the candidate does not exist, the recruiter shall be able to create a candidate account/profile. |
| FR-10 | Workspace Membership | A recruiter shall be able to associate a candidate with a workspace. |
| FR-11 | Multiple Assessments | A recruiter shall be able to create multiple assessments for the same candidate. |
| FR-12 | Assessment Configuration | The recruiter shall provide repository URL, branch, backend root directory, difficulty, duration, scheduled start time, and scheduled end time. |
| FR-13 | Assessment Creation | The system shall acknowledge assessment creation immediately and process repository analysis asynchronously. |
| FR-14 | Repository Cloning | The system shall clone the configured repository and prepare an assessment-specific workspace. |
| FR-15 | Repository Analysis | The system shall extract structured information from the Spring Boot project, including project structure, controllers, endpoints, services, repositories, entities, fields, parameters, and related metadata. |
| FR-16 | AI Feature Generation | The system shall generate one project-specific feature based on the repository analysis and recruiter-selected difficulty. |
| FR-17 | Hidden Test Generation | The system shall generate hidden HTTP test cases for the generated feature. |
| FR-18 | Assessment Processing Status | The recruiter shall be able to see the assessment processing stages and final readiness/failure state. |
| FR-19 | Candidate Assessment Listing | The candidate dashboard shall list assessments assigned to the candidate as scheduled or completed based on their lifecycle. |
| FR-20 | Scheduled Access | The candidate shall be allowed to start an assessment according to its fixed scheduled start and end time. |
| FR-21 | Feature Display | The candidate shall be able to view the generated feature specification inside the coding environment. |
| FR-22 | Browser IDE | The candidate shall be provided with a file explorer and Monaco-based code editor. |
| FR-23 | Application Execution | The candidate shall be able to run and stop the application from the coding environment. |
| FR-24 | Docker Isolation | The system shall build and execute the candidate's modified project in an isolated Docker environment. |
| FR-25 | Runtime Logs | The system shall display build, startup, and runtime logs/errors in the candidate UI. |
| FR-26 | Automated Verification | The system shall execute hidden HTTP requests against the running candidate application. |
| FR-27 | Response Comparison | The system shall compare actual status codes, response bodies, and configured assertions against hidden expected results. |
| FR-28 | Scoring | The system shall calculate a score out of 100 from the hidden test results. |
| FR-29 | Submission | The candidate shall be able to submit the assessment. |
| FR-30 | Recruiter Reports | The recruiter shall be able to view assessment reports and candidate performance. |
| FR-31 | Completed Assessment History | The candidate shall be able to view completed assessments and their available result information. |


| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Security | Repository execution and candidate code shall run in isolated execution environments. Hidden tests and expected responses shall not be exposed to candidates. |
| NFR-02 | Authentication & Authorization | Recruiter and candidate accounts shall have separate access boundaries and role-specific permissions. |
| NFR-03 | Candidate Isolation | A candidate shall only access assessments assigned to that candidate. |
| NFR-04 | Workspace Isolation | A recruiter shall only access workspaces and candidates belonging to that recruiter. |
| NFR-05 | Performance | Assessment creation shall acknowledge the recruiter quickly and perform repository analysis and AI generation asynchronously. |
| NFR-06 | Scalability | Repository analysis, AI generation, Docker execution, and test execution should be independently scalable workloads. |
| NFR-07 | Reliability | Failures during cloning, analysis, AI generation, build, startup, or testing shall be represented as explicit states without corrupting assessment data. |
| NFR-08 | Data Integrity | Database constraints shall maintain valid recruiter, candidate, workspace, assessment, submission, and evaluation relationships. |
| NFR-09 | Auditability | Assessment configuration, processing status, submission, and evaluation results shall be persisted. |
| NFR-10 | Confidentiality | Hidden test cases, expected responses, and evaluator logic shall remain server-side. |
| NFR-11 | Usability | The candidate dashboard shall make upcoming/scheduled assessments clearly distinguishable from completed assessments. |
| NFR-12 | Usability | The coding environment shall provide a familiar file explorer, editor, feature view, run/stop controls, and readable logs. |
| NFR-13 | Observability | Long-running assessment preparation and execution stages shall expose meaningful processing states and errors. |
| NFR-14 | Maintainability | Repository analysis, AI generation, assessment execution, and evaluation shall be separated into maintainable services/components. |
| NFR-15 | Extensibility | The architecture should allow additional languages/frameworks and evaluation methods after the MVP. |


| User | Page / View | Purpose |
|---|---|---|
| Public | Landing Page | Explain Evidence and provide recruiter/candidate entry points. |
| Recruiter | Sign Up / Login | Create or access the recruiter's account. |
| Recruiter | Dashboard | Show workspace, assessment, candidate, and processing statistics. |
| Recruiter | Workspaces | Create and manage recruitment/placement workspaces. |
| Recruiter | Workspace Candidates | View candidates associated with the workspace and search/create candidates. |
| Recruiter | Assessment Creation | Configure repository, candidate, difficulty, duration, and fixed schedule. |
| Recruiter | Assessment Processing | Show repository analysis and AI generation progress. |
| Recruiter | Reports | View scores, test results, and candidate performance. |
| Recruiter | Selected Candidates | Review selected candidates and their assessment outcomes. |
| Candidate | Sign Up / Login | Create or access the candidate's account. |
| Candidate | Dashboard | Show scheduled assessments and completed assessments. |
| Candidate | Assessment Environment | Open an available assessment and use the browser-based coding IDE. |


| Section | What Candidate Sees | Action |
|---|---|---|
| Scheduled Assessments | Upcoming and currently available assessments with fixed start/end times. | View details / Start when available. |
| Completed Assessments | Assessments already submitted/completed and their available result information. | View result/details. |


| Stage | System Behavior |
|---|---|
| 1. Clone | Clone the configured repository and selected branch into an assessment-specific workspace. |
| 2. Project Discovery | Identify the configured backend root and inspect project directories/files. |
| 3. Structural Extraction | Extract controllers, endpoints, HTTP methods, parameters, services, repositories, entities, fields, and relevant project relationships. |
| 4. Repository Analysis Storage | Persist the structured analysis separately from the assessment configuration. |
| 5. Feature Selection | Use structured project information and selected difficulty to select one suitable feature. |
| 6. Feature Specification | Generate feature title, description, requirements, endpoint, request/response contract, constraints, and examples. |
| 7. Hidden Test Generation | Generate black-box HTTP tests containing requests, expected status codes, expected responses, and assertions. |
| 8. Validation | Validate generated feature/test artifacts before marking the assessment ready. |
| 9. Ready | The assessment is ready for the candidate's scheduled window. |


| Stage | Input | Output |
|---|---|---|
| Workspace Preparation | Original assessment repository | Candidate-editable workspace |
| Code Editing | Candidate workspace | Modified source code |
| Build | Modified source code | Build success/failure |
| Docker Execution | Built project | Running isolated application or startup failure |
| HTTP Test | Hidden request | Actual HTTP response |
| Comparison | Expected response + assertions + actual response | Pass/fail/error |
| Aggregation | All test results | Score and evaluation report |


| Entity | Purpose |
|---|---|
| USER | Stores separate recruiter and candidate accounts; email identifies a user account uniquely. |
| WORKSPACE | Represents a recruitment/placement context managed by a recruiter. |
| WORKSPACE_CANDIDATE | Associates candidates with workspaces. |
| ASSESSMENT | Stores candidate-specific repository configuration, difficulty, duration, fixed schedule, and lifecycle. |
| REPOSITORY_ANALYSIS | Stores structured analysis of the repository. |
| ASSESSMENT_WORKSPACE | Stores original and candidate-modified repository paths. |
| FEATURE_SPECIFICATION | Stores the AI-generated feature to implement. |
| TEST_CASE | Stores hidden HTTP tests and expected responses/assertions. |
| SUBMISSION | Stores the candidate's final submission information. |
| EVALUATION_REPORT | Stores aggregate score and evaluation information. |
| TEST_RESULT | Stores actual responses and individual test outcomes. |


| Term | Meaning |
|---|---|
| Workspace | A recruiter-managed grouping such as a placement drive or hiring context. |
| Candidate Profile | A separate candidate user account that can participate in multiple assessments. |
| Assessment | One project-specific coding test created for one candidate. |
| Scheduled Assessment | An assigned assessment with a fixed scheduled start/end window that appears on the candidate dashboard. |
| Completed Assessment | An assessment that the candidate has submitted and that has reached its completed state. |
| Repository Analysis | Structured representation of the Spring Boot project's architecture and API/code metadata. |
| Feature Specification | The project-specific feature the candidate must implement. |
| Hidden Test Case | A server-side test not exposed to the candidate. |
| Expected Response | The response the evaluator expects for a hidden request. |
| Actual Response | The response produced by the candidate's application. |
| Assessment Workspace | The storage locations for the original repository and candidate-modified project. |
| Evaluation Report | Aggregate result containing score and assessment execution information. |

## Candidate Account Prerequisite

A recruiter cannot create a candidate account. Candidate registration and authentication are handled through the separate candidate sign-up/login flow. The recruiter can only search for an existing candidate using the candidate's unique email address, select that candidate, associate the candidate with a workspace, and then create assessments for that candidate. If the candidate has not registered yet, the recruiter must wait until the candidate registers.
