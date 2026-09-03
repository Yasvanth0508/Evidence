# Evidence Backend — AI Refactoring & Clean Architecture Implementation Plan

## 0. Purpose

This document is an implementation specification for restructuring the Evidence backend.

The backend was largely AI-generated and currently contains large services, mixed responsibilities, infrastructure code inside business services, duplicated authorization logic, and inconsistent abstractions.

The objective is **not to rewrite the application**.

The objective is to refactor the existing backend into a clear, maintainable **Clean Architecture Lite** structure while preserving the externally visible behavior.

---

# 1. Non-Negotiable Constraints

The following are protected contracts.

## 1.1 API Contract

Do NOT break or intentionally change:

- Existing endpoint paths
- HTTP methods
- Path variables
- Query parameters
- Request JSON structures
- Response JSON structures
- HTTP status codes
- Existing frontend-facing behavior
- Authentication endpoints used by the frontend

If an internal class must be renamed or moved, the controller/API contract must remain unchanged.

## 1.2 Database Contract

Do NOT initially change:

- Existing database table names
- Existing column names
- Existing foreign keys
- Existing relationships
- UUID strategy
- Existing persisted enum values
- Existing data
- Existing schema semantics

Do not perform a database redesign as part of this refactoring.

## 1.3 Functional Contract

The following functionality must continue to work:

- Authentication
- Workspace management
- Candidate management
- Assessment lifecycle
- Repository ingestion
- Repository/source analysis
- Feature generation
- Test-case generation
- Candidate workspace/file operations
- Application build/run/stop
- Evaluation
- Scoring
- Reports
- Dashboards
- Existing frontend integration

---

# 2. Target Architecture

Use **Clean Architecture Lite**.

Do NOT introduce unnecessary enterprise abstractions.

The target dependency direction is:

```text
Web / Controllers
        |
        v
Application / Use Cases
        |
        v
Domain
        |
        v
Ports / Repository Interfaces
        |
        v
Infrastructure Adapters
        |
        +---- PostgreSQL
        +---- Git
        +---- Docker
        +---- Filesystem
        +---- Mistral / AI
        +---- Google OAuth
```

Infrastructure details must not leak into business logic.

---

# 3. Target Package Structure

Move toward the following structure gradually:

```text
backend/src/main/java/com/example/backend/

├── BackendApplication.java
│
├── common/
│   ├── dto/
│   ├── entity/
│   ├── enums/
│   ├── exception/
│   └── util/
│
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   ├── AppConfig.java
│   └── DataInitializer.java
│
├── auth/
│   ├── domain/
│   │   └── User.java
│   │
│   ├── application/
│   │   ├── AuthenticationService.java
│   │   ├── LocalAuthenticationService.java
│   │   ├── GoogleAuthenticationService.java
│   │   └── TokenService.java
│   │
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   └── UserRepository.java
│   │   └── google/
│   │       └── GoogleTokenVerifier.java
│   │
│   └── web/
│       ├── AuthController.java
│       └── dto/
│
├── workspace/
│   ├── domain/
│   │   └── ...
│   │
│   ├── application/
│   │   ├── WorkspaceService.java
│   │   ├── WorkspaceCandidateService.java
│   │   └── WorkspaceDeletionService.java
│   │
│   ├── infrastructure/
│   │   └── persistence/
│   │
│   └── web/
│       ├── WorkspaceController.java
│       └── dto/
│
├── candidate/
│   ├── application/
│   ├── infrastructure/
│   └── web/
│
├── assessment/
│   ├── domain/
│   │   ├── Assessment.java
│   │   ├── AssessmentWorkspace.java
│   │   ├── FeatureSpecification.java
│   │   ├── RepositoryAnalysis.java
│   │   ├── TestCase.java
│   │   ├── TestResult.java
│   │   ├── Submission.java
│   │   └── EvaluationReport.java
│   │
│   ├── application/
│   │   ├── AssessmentCommandService.java
│   │   ├── AssessmentQueryService.java
│   │   ├── AssessmentLifecycleService.java
│   │   ├── AssessmentProcessingService.java
│   │   ├── CandidateWorkspaceService.java
│   │   ├── CandidateExecutionService.java
│   │   └── CandidateEvaluationService.java
│   │
│   ├── infrastructure/
│   │   └── persistence/
│   │
│   └── web/
│       ├── AssessmentController.java
│       ├── CandidateWorkspaceController.java
│       ├── CandidateExecutionController.java
│       └── CandidateEvaluationController.java
│
├── pipeline/
│   ├── application/
│   │   ├── AssessmentPipeline.java
│   │   └── AssessmentProcessingLauncher.java
│   │
│   ├── git/
│   │   ├── GitRepositoryClient.java
│   │   └── JGitRepositoryClient.java
│   │
│   ├── analysis/
│   │   ├── SourceCodeAnalyzer.java
│   │   └── SpringBootSourceAnalyzer.java
│   │
│   ├── docker/
│   │   ├── ApplicationRunner.java
│   │   ├── DockerApplicationRunner.java
│   │   └── DockerCommandExecutor.java
│   │
│   ├── feature/
│   │   ├── FeatureGenerator.java
│   │   └── MistralFeatureGenerator.java
│   │
│   └── testcase/
│       ├── TestGenerator.java
│       └── MistralTestGenerator.java
│
├── dashboard/
│   ├── application/
│   └── web/
│
└── report/
    ├── application/
    └── web/
```

Do not force every module to have every directory. Create abstractions only when they improve separation.

---

# 4. Refactoring Principles

## Rule 1 — Preserve Behavior First

Every refactor must preserve behavior.

Before changing a class:

1. Understand what it currently does.
2. Identify callers.
3. Identify database operations.
4. Identify API behavior.
5. Add tests where necessary.
6. Refactor.
7. Run the test suite.
8. Verify affected endpoints.

## Rule 2 — One Responsibility Per Class

Avoid classes that simultaneously:

- Validate requests
- Authorize users
- Query repositories
- Execute Docker
- Manipulate files
- Call external APIs
- Calculate scores
- Build DTOs

Split those responsibilities.

## Rule 3 — Do Not Over-Abstract

Do NOT create an interface simply because Clean Architecture diagrams contain interfaces.

Introduce an interface when:

- The application needs to hide infrastructure.
- There are multiple implementations.
- Testing becomes significantly easier.
- The dependency represents an external capability.

## Rule 4 — Controllers Stay Thin

Controllers should primarily:

1. Receive HTTP input.
2. Convert authentication context.
3. Call an application service/use case.
4. Return the result.

Avoid business rules, database operations, Docker calls, filesystem operations, and complex authorization inside controllers.

---

# 5. Phase 0 — Establish a Baseline

Before refactoring anything:

```bash
./mvnw test
```

Record:

- Passing tests
- Failing tests
- Application startup behavior
- Existing API behavior
- Existing database behavior

Create a baseline commit:

```text
refactor: establish backend baseline
```

Do not begin architectural changes until the baseline is understood.

---

# 6. Phase 1 — Understand the Domain

The main domain model is:

```text
User
 |
 +-- Recruiter
 |      |
 |      +-- Workspace
 |             |
 |             +-- WorkspaceCandidate
 |             |
 |             +-- Assessment
 |
 +-- Candidate
        |
        +-- Assessment
```

An assessment is associated with:

```text
Assessment
 |
 +-- RepositoryAnalysis
 +-- FeatureSpecification
 +-- AssessmentWorkspace
 +-- TestCase
 +-- Submission
       |
       +-- TestResult
       +-- EvaluationReport
```

Do not change these relationships.

Create a simple domain diagram/documentation file if useful.

---

# 7. Phase 2 — Clean Common Infrastructure

Review:

```text
common/
```

Keep:

```text
common/
├── dto/
├── entity/
├── enums/
├── exception/
└── util/
```

Responsibilities:

### `common.entity`

Shared persistence behavior such as:

- UUID IDs
- timestamps

### `common.dto`

Shared API envelopes only.

### `common.exception`

- Domain exceptions
- Resource-not-found exceptions
- Conflict exceptions
- Forbidden exceptions
- Validation exceptions
- Global exception handler

### `common.util`

Only genuinely generic utilities.

Do not place business logic here.

---

# 8. Phase 3 — Authentication Refactor

Current authentication combines too many concerns.

Separate:

```text
AuthenticationService
    |
    +-- LocalAuthenticationService
    |
    +-- GoogleAuthenticationService
    |
    +-- TokenService
```

Google HTTP communication must be moved out of the core authentication service.

Create an abstraction such as:

```text
GoogleTokenVerifier
```

The authentication application service should ask:

```text
"Is this Google credential valid?"
```

It should not know how HTTP communication with Google works.

---

# 9. Phase 4 — Introduce CurrentUser

Create a simple application-level authentication representation:

```text
CurrentUser
----------------
UUID id
String email
Role role
String name
```

Controllers should not repeatedly extract IDs and roles manually.

Application services should receive:

```text
CurrentUser currentUser
```

instead of multiple nullable parameters such as:

```text
UUID recruiterId
UUID candidateId
```

---

# 10. Phase 5 — Centralize Authorization

Create:

```text
AuthorizationService
```

Potential operations:

```text
requireAuthenticated()
requireRecruiter()
requireCandidate()
requireWorkspaceOwner()
requireAssessmentOwner()
requireAssessmentCandidate()
```

The exact API can be chosen based on existing code.

Do not duplicate authorization rules across controllers and services.

Important:

Authorization must remain behaviorally equivalent to the current system.

Do not accidentally broaden access while refactoring.

---

# 11. Phase 6 — Refactor Workspace

Current `WorkspaceService` contains several unrelated responsibilities.

Split into:

```text
WorkspaceService
WorkspaceCandidateService
WorkspaceDeletionService
```

## WorkspaceService

Responsible for:

```text
create
get
list
update
archive
```

## WorkspaceCandidateService

Responsible for:

```text
add candidate
remove candidate
list candidates
```

## WorkspaceDeletionService

Responsible for:

```text
delete workspace
cleanup related assessment data
cleanup related records
```

Keep database behavior unchanged.

If deletion requires a transaction, preserve or improve transaction boundaries without changing semantics.

---

# 12. Phase 7 — Refactor Candidate Module

Keep this module simple.

Target:

```text
CandidateController
       |
       v
CandidateService
       |
       v
Repositories
```

Separate:

- Candidate lookup
- Candidate profile
- Candidate workspace queries
- Candidate assessment history

Only split classes when their responsibilities become meaningfully different.

---

# 13. Phase 8 — Major Assessment Refactor

This is the largest application-level refactor.

Do not modify API paths.

Split the current assessment service into:

```text
AssessmentCommandService
AssessmentQueryService
AssessmentLifecycleService
AssessmentProcessingService
```

## Command Service

Responsible for:

```text
create assessment
update assessment
cancel assessment
```

## Query Service

Responsible for:

```text
get assessment
list assessments
get repository analysis
get feature specification
get processing status
```

## Lifecycle Service

Responsible for:

```text
start assessment
submit assessment
validate assessment state
validate scheduling window
```

## Processing Service

Responsible for:

```text
start asynchronous processing
track processing
coordinate pipeline execution
```

Avoid putting Docker/Git/AI implementation details here.

---

# 14. Phase 9 — Refactor Asynchronous Processing

Replace direct:

```text
CompletableFuture.runAsync(...)
```

from business services with:

```text
AssessmentProcessingLauncher
```

Conceptually:

```text
AssessmentCommandService
        |
        v
AssessmentProcessingLauncher
        |
        v
Spring Task Executor
        |
        v
AssessmentProcessingOrchestrator
```

Do not introduce Kafka/RabbitMQ merely for architecture.

Use the existing asynchronous model initially.

---

# 15. Phase 10 — Refactor Assessment Pipeline

The existing five-stage pipeline should remain logically unchanged:

```text
1. Repository ingestion
2. Project validation
3. Source analysis
4. Feature generation
5. Test generation
```

Target:

```text
AssessmentPipeline
      |
      +-- RepositoryIngestionPort
      +-- ProjectValidationPort
      +-- SourceCodeAnalyzer
      +-- FeatureGenerator
      +-- TestGenerator
```

The pipeline orchestrator should coordinate stages.

It should not contain low-level Git/Docker/HTTP code.

---

# 16. Phase 11 — Isolate Git

Current Git logic should be separated.

Target:

```text
GitRepositoryClient
        |
        +-- JGitRepositoryClient
        +-- Optional native Git adapter
```

Application code should call something conceptually like:

```text
gitRepositoryClient.cloneRepository(...)
```

It should not directly know:

- JGit implementation details
- `ProcessBuilder`
- native git commands
- filesystem cleanup details

Create a separate repository storage component for filesystem locations.

---

# 17. Phase 12 — Isolate Repository Storage

Create:

```text
RepositoryStorageService
```

Responsibilities:

- Determine assessment workspace path
- Create directories
- Remove directories
- Resolve original repository location
- Resolve candidate workspace location

Do not scatter hard-coded paths throughout the application.

For example, avoid repeatedly embedding:

```text
storage/assessments/{id}/original
```

in unrelated classes.

---

# 18. Phase 13 — Refactor Source Analysis

Current source analysis must continue working.

First extract:

```text
SourceCodeAnalyzer
```

Then retain the current implementation behind it:

```text
SpringBootSourceAnalyzer
```

Important:

The current analyzer appears to rely substantially on regex/string matching rather than a true Java AST.

Do NOT replace it during the first refactor.

Later, a real implementation can be introduced:

```text
SourceCodeAnalyzer
    |
    +-- RegexSpringBootSourceAnalyzer
    |
    +-- JavaAstSourceAnalyzer
```

This makes future improvement safe.

---

# 19. Phase 14 — Refactor AI Feature Generation

Separate:

```text
FeatureGenerationService
        |
        v
FeatureGenerator
        |
        +-- MistralFeatureGenerator
        +-- DynamicFeatureGenerator
```

Responsibilities:

### FeatureGenerationService

Business orchestration and persistence.

### FeatureGenerator

Abstraction for generating a feature specification.

### MistralFeatureGenerator

External AI API communication and prompt execution.

### DynamicFeatureGenerator

Fallback/deterministic generation.

Do not allow HTTP client details to leak into the application service.

---

# 20. Phase 15 — Refactor Test Generation

Use the same pattern:

```text
TestGenerationService
        |
        v
TestGenerator
        |
        +-- MistralTestGenerator
        +-- DynamicTestGenerator
```

Separate:

- Prompt construction
- AI communication
- JSON parsing
- Fallback generation
- Database persistence

Do not change generated test behavior during structural refactoring.

---

# 21. Phase 16 — Refactor Candidate Workspace

Current candidate workspace logic contains too many filesystem responsibilities.

Split into:

```text
CandidateWorkspaceService
FileService
WorkspacePathService
RepositoryWorkspaceInitializer
```

## `FileService`

Handles:

```text
read
write
create
delete
rename
tree
```

## `WorkspacePathService`

Handles:

```text
path resolution
path normalization
path traversal protection
allowed workspace boundaries
```

This is security-sensitive.

Do not weaken path validation during refactoring.

## `RepositoryWorkspaceInitializer`

Handles:

```text
clone/copy repository
initialize candidate workspace
create required files/directories
```

---

# 22. Phase 17 — Refactor Docker

Current Docker/process details should be hidden.

Introduce:

```text
ApplicationRunner
```

Implementations:

```text
DockerApplicationRunner
LocalApplicationRunner
```

Potential additional abstraction:

```text
ApplicationBuilder
```

Implementation:

```text
MavenApplicationBuilder
```

Target flow:

```text
CandidateExecutionService
        |
        +-- ApplicationBuilder
        |
        +-- ApplicationRunner
```

The application service must not directly contain:

```text
docker build
docker run
ProcessBuilder
container IDs
image names
port polling
```

Those belong to infrastructure adapters.

---

# 23. Phase 18 — Refactor DockerCommandExecutor

Keep the low-level process execution code isolated.

Responsibilities:

```text
execute operating-system process
capture stdout
capture stderr
handle timeout
return process result
```

It should not know business concepts such as:

```text
assessment
candidate
score
test case
submission
```

Do not mix process execution with evaluation.

---

# 24. Phase 19 — Refactor Candidate Evaluation

Break the current large evaluation flow into:

```text
CandidateEvaluationService
SubmissionService
ApplicationBuildService
ApplicationRunner
TestExecutionService
ScoreCalculator
EvaluationReportService
```

Desired flow:

```text
submit
  |
  v
create submission
  |
  v
build application
  |
  v
start application
  |
  v
execute tests
  |
  v
calculate score
  |
  v
persist test results
  |
  v
generate evaluation report
  |
  v
complete assessment
```

The orchestration service coordinates these operations.

---

# 25. Phase 20 — Extract ScoreCalculator

This is a high-priority extraction.

Create:

```text
ScoreCalculator
```

It should contain only scoring rules.

Input:

```text
Test cases
Test results
```

Output:

```text
ScoreResult
```

Potential fields:

```text
totalWeight
passedWeight
totalTests
passedTests
failedTests
finalScore
```

The calculator must be testable without:

- Spring
- PostgreSQL
- Docker
- HTTP
- filesystem

This should become one of the first strong unit-test examples.

---

# 26. Phase 21 — Refactor Dashboards

Separate:

```text
RecruiterDashboardService
CandidateDashboardService
```

Keep aggregation behavior unchanged.

Review every repository query carefully.

Pay special attention to ID semantics.

For example, verify that methods expecting:

```text
submissionId
```

are never accidentally called with:

```text
assessmentId
```

The goal is to eliminate semantic bugs, not merely rearrange code.

---

# 27. Phase 22 — Refactor Reports

Target:

```text
ReportQueryService
ReportSummaryService
ReportMapper
```

Preserve:

- Pagination
- Workspace filtering
- Status filtering
- Existing report JSON
- Existing score calculations

Avoid unnecessary query rewrites during this phase.

---

# 28. Phase 23 — Clean DTOs

Keep DTOs close to their feature.

Prefer:

```text
assessment/web/dto/
workspace/web/dto/
auth/web/dto/
```

Do not create a huge global DTO folder.

Separate request and response DTOs only where they actually represent different contracts.

Do not create unnecessary DTO layers.

---

# 29. Phase 24 — Clean JPA Entities

Review JPA entities after behavior is protected.

Avoid blindly using Lombok `@Data` on entities.

Prefer:

```text
@Getter
@Setter
```

with deliberate:

```text
equals()
hashCode()
toString()
```

Especially review entities containing relationships.

Do not change entity relationships or schema mappings without explicit evidence that the current mapping is incorrect.

---

# 30. Phase 25 — Repository Cleanup

Keep Spring Data repositories simple.

If Spring Data already provides the required behavior:

```text
JpaRepository<Entity, UUID>
```

do not create unnecessary repository implementations.

Only add repository abstractions/adapters where they create a useful architecture boundary.

---

# 31. Phase 26 — Configuration Cleanup

After the structural refactor, separate environment configuration:

```text
application.properties
application-dev.properties
application-test.properties
application-prod.properties
```

Use environment variables for:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
JWT_SECRET
MISTRAL_API_KEY
GOOGLE_CLIENT_ID
```

Do not commit real secrets.

Development defaults may remain temporarily if required for local development, but production configuration must not depend on insecure fallback secrets.

For production, prefer:

```text
ddl-auto=validate
```

rather than schema mutation.

---

# 32. Phase 27 — Testing Strategy

Use three testing levels.

## Unit Tests

Prioritize:

```text
ScoreCalculator
AuthorizationService
WorkspacePathService
SourceCodeAnalyzer
Feature generators
Test generators
Lifecycle rules
```

These should run without infrastructure whenever possible.

## Service Tests

Test application services using controlled dependencies.

Examples:

```text
AssessmentCommandService
AssessmentLifecycleService
WorkspaceService
CandidateService
ReportService
```

## Integration Tests

Preserve and improve:

```text
HTTP
  -> Controller
  -> Application Service
  -> Repository
  -> PostgreSQL
```

Existing API/database verification tests should remain as regression protection.

---

# 33. Refactoring Order

Execute in exactly this order unless an actual dependency requires otherwise:

```text
1. Baseline tests
2. Domain/entity understanding
3. Common foundation
4. Authentication
5. CurrentUser + authorization
6. Workspace
7. Candidate
8. Assessment
9. Async processing
10. Pipeline
11. Git
12. Repository storage
13. Source analysis
14. AI feature generation
15. AI test generation
16. Candidate workspace
17. Docker
18. Evaluation
19. Score calculation
20. Dashboard
21. Reports
22. DTO cleanup
23. Entity cleanup
24. Repository cleanup
25. Configuration
26. Test expansion
27. Final cleanup
```

---

# 34. Commit Strategy

Never perform the entire refactor in one commit.

Use small commits such as:

```text
refactor: establish backend baseline
refactor: clean common foundation
refactor: introduce current user abstraction
refactor: centralize authorization
refactor: simplify authentication
refactor: split workspace responsibilities
refactor: split assessment commands and queries
refactor: isolate assessment lifecycle
refactor: isolate assessment processing
refactor: introduce pipeline ports
refactor: isolate git infrastructure
refactor: isolate repository storage
refactor: isolate source analysis
refactor: isolate AI feature generation
refactor: isolate AI test generation
refactor: split candidate workspace responsibilities
refactor: isolate docker execution
refactor: split candidate evaluation
refactor: extract score calculator
refactor: simplify dashboards
refactor: simplify reports
refactor: clean DTOs
refactor: clean JPA entities
refactor: clean configuration
test: expand unit coverage
```

Run:

```bash
./mvnw test
```

after each meaningful refactor.

---

# 35. AI-Agent Instructions

When using Antigravity or another coding agent, do NOT ask it to blindly rewrite the whole backend.

Use this operating procedure:

## Before Editing

The agent must:

1. Inspect the current implementation.
2. Identify all callers.
3. Identify database dependencies.
4. Identify API dependencies.
5. Identify tests.
6. Explain the proposed change.
7. Confirm that API/database/frontend contracts will remain unchanged.

## During Editing

The agent must:

- Make small changes.
- Preserve behavior.
- Avoid unnecessary dependencies.
- Avoid introducing frameworks without a clear need.
- Avoid changing API contracts.
- Avoid changing database schema.
- Avoid deleting functionality merely because it appears unused.
- Avoid replacing working logic with speculative implementations.
- Keep infrastructure behind appropriate boundaries.

## After Editing

The agent must:

1. Compile the project.
2. Run tests.
3. Report failures.
4. Explain files changed.
5. Explain architectural improvements.
6. Identify any behavior that could not be verified.

---

# 36. Anti-Patterns to Avoid

Do NOT create:

```text
BaseService
BaseController
BaseRepository
GenericService<T>
GenericController<T>
```

unless there is a real repeated abstraction.

Do NOT create:

```text
10 interfaces for 10 services
```

just to satisfy a Clean Architecture diagram.

Do NOT introduce:

```text
Kafka
RabbitMQ
Redis
Kubernetes
CQRS framework
Event sourcing
Microservices
```

as part of this refactor.

The objective is maintainability, not architectural complexity.

---

# 37. Definition of Done

The refactor is successful when:

## API

```text
Existing endpoints still work.
Existing request/response contracts remain compatible.
Frontend requires no architectural changes.
```

## Database

```text
Existing schema remains compatible.
Existing data remains intact.
```

## Architecture

```text
Controllers are thin.
Application services contain use-case orchestration.
Business rules are isolated.
Infrastructure details are isolated.
Git/Docker/AI/filesystem code is not scattered through business services.
```

## Readability

A developer should be able to answer:

```text
Where is assessment creation?
Where is assessment starting?
Where is scoring?
Where is Docker execution?
Where is Git cloning?
Where is AI feature generation?
Where is file manipulation?
Where is authorization?
```

without searching through a huge service.

## Testing

Important business rules can be tested without requiring:

```text
PostgreSQL
Docker
Git
Mistral
HTTP
```

---

# 38. Final Target Mental Model

The final system should be understandable as:

```text
                  HTTP
                   |
                   v
             Controllers
                   |
                   v
             Use Cases
                   |
          +--------+--------+
          |        |        |
          v        v        v
       Domain   Repositories  Ports
                    |          |
                    v          v
                 Database   Infrastructure
                              |
              +---------------+---------------+
              |               |               |
             Git            Docker           AI
```

For assessment processing:

```text
Create Assessment
       |
       v
Assessment Command
       |
       v
Processing Launcher
       |
       v
Assessment Pipeline
       |
       +--> Repository Ingestion
       |
       +--> Project Validation
       |
       +--> Source Analysis
       |
       +--> Feature Generation
       |
       +--> Test Generation
```

For candidate evaluation:

```text
Submit Assessment
       |
       v
Create Submission
       |
       v
Build Application
       |
       v
Run Application
       |
       v
Execute Tests
       |
       v
Calculate Score
       |
       v
Persist Results
       |
       v
Generate Report
```

---

# 39. Most Important Refactoring Principle

Do not optimize for:

```text
fewer files
```

Optimize for:

```text
one responsibility
        |
        v
clear dependency
        |
        v
testable behavior
        |
        v
understandable code
```

The purpose of this refactor is not simply to make the repository look like Clean Architecture.

The purpose is that **you should be able to open any class and understand why it exists, what it owns, what it depends on, and what it is allowed to do.**

---

# 40. Recommended First Task

Start with the smallest useful Clean Architecture extraction:

```text
CandidateEvaluationService
        |
        +--> ScoreCalculator
```

Do not touch the API.

Do not touch the database.

Do not touch Docker.

Do not touch AI.

Extract only the scoring rules, add unit tests, verify the existing evaluation flow, and commit.

Then proceed to:

```text
WorkspaceService
        |
        +--> WorkspaceCandidateService
        +--> WorkspaceDeletionService
```

Then:

```text
AssessmentService
        |
        +--> Command
        +--> Query
        +--> Lifecycle
        +--> Processing
```

Then refactor the pipeline and infrastructure.

This sequence minimizes risk while progressively teaching the architecture.
