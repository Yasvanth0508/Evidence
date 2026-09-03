// ==========================================
// EVIDENCE PLATFORM — CORE TYPE DEFINITIONS
// ==========================================

export type UserRole = "RECRUITER" | "CANDIDATE";

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  avatarUrl?: string;
  company?: string;
}

export interface AuthResponse {
  user: User;
  token: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

// ------------------------------------------
// Workspace Types
// ------------------------------------------
export interface Workspace {
  id: string;
  name: string;
  description: string;
  status: "ACTIVE" | "ARCHIVED";
  recruiterId: string;
  candidateCount: number;
  assessmentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface WorkspaceCandidate {
  id: string;
  workspaceId: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  enrolledAt: string;
}

// ------------------------------------------
// Assessment Types
// ------------------------------------------
export type AssessmentStatus =
  | "CREATING"
  | "ANALYZING"
  | "GENERATING_FEATURE"
  | "GENERATING_TESTS"
  | "READY"
  | "SCHEDULED"
  | "IN_PROGRESS"
  | "EVALUATING"
  | "COMPLETED"
  | "CANCELLED"
  | "FAILED"
  | "EXPIRED";

export type DifficultyLevel = "EASY" | "INTERMEDIATE" | "DIFFICULT";

export interface Assessment {
  id: string;
  workspaceId: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  projectName?: string;
  techStack?: string;
  repositoryUrl: string;
  branchName: string;
  backendRootDirectory: string;
  difficulty: DifficultyLevel;
  durationMinutes: number;
  scheduledStartAt: string;
  scheduledEndAt: string;
  status: AssessmentStatus;
  score: number | null;
  timeTakenMinutes?: number;
  integrityScore?: number;
  createdAt: string;
  updatedAt: string;
}

export interface PipelineStage {
  id: string;
  name: string;
  status: "PENDING" | "IN_PROGRESS" | "COMPLETED" | "FAILED";
  progress: number;
  errorMessage?: string;
}

export interface ProcessingStatusResponse {
  assessmentId: string;
  status: AssessmentStatus;
  stages: PipelineStage[];
}

// ------------------------------------------
// Recruiter Dashboard Types
// ------------------------------------------
export interface MetricTrend {
  value: number | string;
  changePercentage: number;
  isPositive: boolean;
  sparkline: { val: number }[];
}

export interface AssessmentStatusSlice {
  name: string;
  count: number;
  percentage: number;
  color: string;
}

export interface BugCategoryStat {
  category: "Business Logic Bugs" | "Syntax / Structural Bugs" | "Data Flow Bugs";
  failureRate: number;
  count: number;
  color: string;
}

export interface RecentAssessmentItem {
  id: string;
  candidateName: string;
  candidateEmail: string;
  project: string;
  techStack: string;
  status: AssessmentStatus;
  timeTaken: string;
  integrity: number; // Percentage, e.g. 98
}

export interface TopPerformerItem {
  rank: number;
  candidateName: string;
  score: number;
  avgFixTime: string;
  avatarUrl?: string;
}

export interface RecruiterDashboardData {
  totalCandidates: MetricTrend;
  totalAssessments: MetricTrend;
  completionRate: MetricTrend;
  avgFixTime: MetricTrend;
  assessmentStatusDistribution: AssessmentStatusSlice[];
  mostFailedBugCategories: BugCategoryStat[];
  recentAssessments: RecentAssessmentItem[];
  topPerformers: TopPerformerItem[];
}

// ------------------------------------------
// Candidate Report Types
// ------------------------------------------
export interface BugBreakdownItem {
  id: number;
  category: "Data Flow" | "Syntax" | "Business Logic";
  issueType: string;
  status: "FIXED" | "NOT_FIXED";
  score: number;
  maxScore: number;
}

export interface CandidateReportData {
  id: string;
  workspaceId?: string;
  candidate: {
    id: string;
    name: string;
    email: string;
    avatarInitials: string;
    status: AssessmentStatus;
  };
  project: {
    name: string;
    techStack: string;
    date: string;
    totalTimeTaken: string;
  };
  scoreOverview: {
    overallScore: number;
    scoreRating: "Excellent" | "Good Performance" | "Needs Improvement";
    bugsFixed: { fixed: number; total: number };
    testCasesPassed: { passed: number; total: number };
    totalTimeTakenMinutes: number;
  };
  bugBreakdown: BugBreakdownItem[];
  testCases?: TestCaseAuditItem[];
  aiSummary: string;
  strengths: string[];
  improvements: string[];
  integrity: {
    overallRiskBadge: "LOW" | "MEDIUM" | "HIGH";
    behaviorSummary: {
      copyPasteEvents: number;
      buildRuns: number;
      testRuns: number;
      idleTimeMinutes: number;
    };
    riskAnalysis: string;
  };
}

// ------------------------------------------
// IDE & Execution Types
// ------------------------------------------
export interface FileNode {
  id: string;
  name: string;
  type: "FILE" | "DIRECTORY";
  path: string;
  children?: FileNode[];
}

export interface FeatureSpecification {
  title: string;
  description: string;
  requirements: string[];
  endpoint: string;
  httpMethod: "GET" | "POST" | "PUT" | "DELETE" | "PATCH";
  requestSpecification?: string;
  responseSpecification?: string;
  constraints: string[];
}

export interface ExecutionState {
  buildStatus: "IDLE" | "BUILDING" | "SUCCESS" | "FAILED";
  containerStatus: "STOPPED" | "STARTING" | "RUNNING" | "FAILED";
  applicationStatus: "STOPPED" | "STARTING" | "STARTED" | "FAILED";
  logs: string[];
  startedAt?: string;
  stoppedAt?: string;
}

// ------------------------------------------
// Test Case Audit & Codebase Analysis Types
// ------------------------------------------
export interface TestCaseAuditItem {
  id: string;
  testCaseNumber: number;
  testType: "BUSINESS_LOGIC" | "INTEGRATION" | "SYNTAX" | "DATA_FLOW";
  httpMethod: string;
  endpoint: string;
  requestData?: string;
  expectedStatusCode: number;
  expectedResponse?: string;
  actualStatusCode?: number;
  actualResponse?: string;
  assertions?: string;
  weight: number;
  executionTimeMs?: number;
  status: "PASSED" | "FAILED" | "PENDING";
  failureReason?: string;
}

export interface AstCodebaseAnalysis {
  analysisStatus: "COMPLETED" | "RUNNING" | "FAILED";
  projectStructure: {
    folders: string[];
    files: string[];
  };
  sourceCodeStructure: {
    controllers: string[];
    services: string[];
    repositories: string[];
    entities: string[];
  };
  contentDetails: {
    endpoints: Array<{ method: string; path: string; controllerMethod: string }>;
    entityFields: Array<{ entity: string; fields: string[] }>;
    serviceMethods: Array<{ service: string; methods: string[] }>;
  };
}

