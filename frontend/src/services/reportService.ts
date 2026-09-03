import { apiClient } from "@/lib/apiClient";

export interface CategoryScoreItem {
  category: string;
  total: number;
  passed: number;
  score: number;
}

export interface ReportItem {
  assessmentId: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  workspaceId?: string;
  workspaceName: string;
  difficulty?: string;
  score: number;
  status: string;
  submittedAt: string;
  completedAt?: string;
}

export interface ReportListResponse {
  content?: ReportItem[];
  reports?: ReportItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ReportSummaryResponse {
  totalCandidates?: number;
  completedAssessments?: number;
  scheduledAssessments?: number;
  participationRate?: number;
  passedAssessments?: number;
  passRate?: number;
  averageScore?: number;
  totalEvaluated?: number;
  highestScore?: number;
  lowestScore?: number;
}

export interface SelectedCandidateItem {
  id: string;
  workspaceId: string;
  workspaceName: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  candidateRole?: string;
  assessmentId?: string;
  score?: number;
  scoreRating?: string;
  passedTests?: number;
  totalTests?: number;
  timeTakenMinutes?: number;
  selectionNotes?: string;
  selectionStatus?: string;
  selectedAt: string;
}

export interface CandidateResultResponse {
  assessmentId: string;
  title: string;
  workspaceName: string;
  difficulty: string;
  techStack: string;
  score: number;
  scoreRating: string;
  status: string;
  totalTests: number;
  passedTests: number;
  failedTests: number;
  buildStatus: string;
  applicationStatus: string;
  timeTakenSeconds: number;
  timeTakenMinutes: number;
  evaluatedAt: string;
  submittedAt: string;
  categoryBreakdown: CategoryScoreItem[];
  aiSummary: string;
  strengths: string[];
  improvements: string[];
}

export interface RecruiterReportResponse {
  assessmentId: string;
  title: string;
  workspaceId: string;
  workspaceName: string;
  difficulty: string;
  techStack: string;
  candidate: {
    id: string;
    name: string;
    email: string;
    avatarInitials?: string;
    addedAt?: string;
  };
  score: number;
  scoreRating: string;
  totalTests: number;
  passedTests: number;
  failedTests: number;
  buildStatus: string;
  applicationStatus: string;
  timeTakenSeconds: number;
  timeTakenMinutes: number;
  status: string;
  evaluatedAt: string;
  submittedAt: string;
  categoryBreakdown: CategoryScoreItem[];
  aiSummary: string;
  strengths: string[];
  improvements: string[];
  integrity?: {
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

export type AssessmentReportResponse = RecruiterReportResponse;

export interface TestResultItem {
  testCaseId: string;
  testCaseNumber: number;
  testType: string;
  status: string;
  httpMethod: string;
  endpoint: string;
  expectedStatusCode: number;
  actualStatusCode: number;
  expectedResponse?: string;
  actualResponse?: string;
  assertions?: string;
  executionTimeMs: number;
  weight: number;
  failureReason?: string;
}

export const reportService = {
  getReports: async (params?: { page?: number; size?: number; workspaceId?: string; status?: string }): Promise<ReportListResponse> => {
    return await apiClient.get("/reports", { params });
  },

  getReportById: async (assessmentId: string): Promise<RecruiterReportResponse> => {
    return await apiClient.get(`/assessments/${assessmentId}/report`);
  },

  getCandidateResult: async (assessmentId: string): Promise<CandidateResultResponse> => {
    return await apiClient.get(`/assessments/${assessmentId}/result`);
  },

  getReportSummary: async (workspaceId?: string): Promise<ReportSummaryResponse> => {
    return await apiClient.get("/reports/summary", { params: { workspaceId } });
  },

  getSelectedCandidates: async (workspaceId?: string): Promise<SelectedCandidateItem[]> => {
    return await apiClient.get("/selected-candidates", { params: { workspaceId } });
  },

  selectCandidate: async (data: {
    workspaceId: string;
    candidateId: string;
    assessmentId?: string;
    selectionNotes?: string;
    selectionStatus?: string;
  }): Promise<SelectedCandidateItem> => {
    return await apiClient.post("/selected-candidates", data);
  },

  removeSelectedCandidate: async (id: string): Promise<any> => {
    return await apiClient.delete(`/selected-candidates/${id}`);
  },

  removeSelectedCandidateByWorkspaceAndCandidate: async (workspaceId: string, candidateId: string): Promise<any> => {
    return await apiClient.delete("/selected-candidates", { params: { workspaceId, candidateId } });
  },

  updateSelectionStatus: async (_id: string, data: {
    workspaceId: string;
    candidateId: string;
    selectionStatus: string;
    selectionNotes?: string;
  }): Promise<SelectedCandidateItem> => {
    return await apiClient.post("/selected-candidates", data);
  },

  getTestResults: async (assessmentId: string): Promise<TestResultItem[]> => {
    return await apiClient.get(`/assessments/${assessmentId}/test-results`);
  },
};
