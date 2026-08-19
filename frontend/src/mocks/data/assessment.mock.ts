import {
  Assessment,
  FeatureSpecification,
  FileNode,
  ProcessingStatusResponse,
  Workspace,
} from "@/types";

export const mockWorkspaces: Workspace[] = [
  {
    id: "ws-001",
    name: "Backend Java Assessment Hub",
    description: "Spring Boot Microservices and REST API evaluation workspace",
    status: "ACTIVE",
    recruiterId: "recruiter-001",
    candidateCount: 12,
    assessmentCount: 18,
    createdAt: "2026-05-01T10:00:00Z",
    updatedAt: "2026-05-18T14:30:00Z",
  },
  {
    id: "ws-002",
    name: "Frontend React Engineer Evaluation",
    description: "React 19, TypeScript, and Tailwind state management challenges",
    status: "ACTIVE",
    recruiterId: "recruiter-001",
    candidateCount: 8,
    assessmentCount: 11,
    createdAt: "2026-05-05T09:00:00Z",
    updatedAt: "2026-05-17T11:00:00Z",
  },
];

export const mockAssessmentsList: Assessment[] = [
  {
    id: "asmt-001",
    workspaceId: "ws-001",
    candidateId: "cand-001",
    candidateName: "Aarav Patel",
    candidateEmail: "aarav.patel@example.com",
    projectName: "E-Commerce API",
    techStack: "Java, Spring Boot",
    repositoryUrl: "https://github.com/example/ecommerce-api.git",
    branchName: "main",
    backendRootDirectory: "/",
    difficulty: "INTERMEDIATE",
    durationMinutes: 90,
    scheduledStartAt: "2026-05-15T10:00:00Z",
    scheduledEndAt: "2026-05-15T12:00:00Z",
    status: "COMPLETED",
    score: 82,
    timeTakenMinutes: 42,
    integrityScore: 98,
    createdAt: "2026-05-14T08:00:00Z",
    updatedAt: "2026-05-15T10:42:00Z",
  },
  {
    id: "asmt-002",
    workspaceId: "ws-001",
    candidateId: "cand-002",
    candidateName: "Neha Singh",
    candidateEmail: "neha.singh@example.com",
    projectName: "Task Manager",
    techStack: "React, Node.js",
    repositoryUrl: "https://github.com/example/task-manager.git",
    branchName: "main",
    backendRootDirectory: "/",
    difficulty: "EASY",
    durationMinutes: 60,
    scheduledStartAt: "2026-05-18T14:00:00Z",
    scheduledEndAt: "2026-05-18T16:00:00Z",
    status: "IN_PROGRESS",
    score: null,
    integrityScore: 96,
    createdAt: "2026-05-17T09:00:00Z",
    updatedAt: "2026-05-18T14:15:00Z",
  },
  {
    id: "asmt-004",
    workspaceId: "ws-001",
    candidateId: "cand-004",
    candidateName: "Priya Nair",
    candidateEmail: "priya.nair@example.com",
    projectName: "Chat Application",
    techStack: "React, Socket.io",
    repositoryUrl: "https://github.com/example/chat-app.git",
    branchName: "master",
    backendRootDirectory: "/",
    difficulty: "DIFFICULT",
    durationMinutes: 120,
    scheduledStartAt: "2026-05-20T10:00:00Z",
    scheduledEndAt: "2026-05-20T13:00:00Z",
    status: "SCHEDULED",
    score: null,
    createdAt: "2026-05-18T11:00:00Z",
    updatedAt: "2026-05-18T11:05:00Z",
  },
];

export const mockProcessingStatus: ProcessingStatusResponse = {
  assessmentId: "asmt-004",
  status: "READY",
  stages: [
    { id: "stage-1", name: "Cloning Repository", status: "COMPLETED", progress: 100 },
    { id: "stage-2", name: "Structural Repository Analysis", status: "COMPLETED", progress: 100 },
    { id: "stage-3", name: "AI Feature Specification Generation", status: "COMPLETED", progress: 100 },
    { id: "stage-4", name: "Hidden Test Case Suite Generation", status: "COMPLETED", progress: 100 },
  ],
};

export const mockFeatureSpec: FeatureSpecification = {
  title: "Note Search Endpoint with Keyword Filtering",
  description:
    "Implement a search endpoint that enables filtering notes by keyword across title and content with case-insensitive matching and pagination.",
  requirements: [
    "Create GET /api/notes/search endpoint in NoteController",
    "Support query parameters: 'q' (search keyword), 'page' (0-indexed, default 0), 'size' (default 10)",
    "Search must be case-insensitive across both 'title' and 'content' fields",
    "Return Spring Data Page response envelope or structured array with matching notes",
    "Handle empty query 'q' by returning all notes paginated",
  ],
  endpoint: "/api/notes/search",
  httpMethod: "GET",
  requestSpecification: "Query Parameters: q (string, optional), page (int, optional), size (int, optional)",
  responseSpecification: "200 OK: Page<NoteDto> containing id, title, content, tags, createdAt, updatedAt",
  constraints: [
    "Must preserve existing JPA entity constraints",
    "Do not break existing GET /api/notes endpoints",
    "Query execution time must remain under 200ms",
  ],
};

export const mockFileTree: FileNode[] = [
  {
    id: "root-1",
    name: "src",
    type: "DIRECTORY",
    path: "/src",
    children: [
      {
        id: "main-1",
        name: "main",
        type: "DIRECTORY",
        path: "/src/main",
        children: [
          {
            id: "java-1",
            name: "java",
            type: "DIRECTORY",
            path: "/src/main/java",
            children: [
              {
                id: "pkg-1",
                name: "com.example.notes",
                type: "DIRECTORY",
                path: "/src/main/java/com/example/notes",
                children: [
                  {
                    id: "file-ctrl",
                    name: "NoteController.java",
                    type: "FILE",
                    path: "/src/main/java/com/example/notes/NoteController.java",
                  },
                  {
                    id: "file-svc",
                    name: "NoteService.java",
                    type: "FILE",
                    path: "/src/main/java/com/example/notes/NoteService.java",
                  },
                  {
                    id: "file-repo",
                    name: "NoteRepository.java",
                    type: "FILE",
                    path: "/src/main/java/com/example/notes/NoteRepository.java",
                  },
                  {
                    id: "file-ent",
                    name: "Note.java",
                    type: "FILE",
                    path: "/src/main/java/com/example/notes/Note.java",
                  },
                ],
              },
            ],
          },
          {
            id: "res-1",
            name: "resources",
            type: "DIRECTORY",
            path: "/src/main/resources",
            children: [
              {
                id: "file-props",
                name: "application.properties",
                type: "FILE",
                path: "/src/main/resources/application.properties",
              },
            ],
          },
        ],
      },
    ],
  },
  {
    id: "file-pom",
    name: "pom.xml",
    type: "FILE",
    path: "/pom.xml",
  },
  {
    id: "file-readme",
    name: "README.md",
    type: "FILE",
    path: "/README.md",
  },
];
