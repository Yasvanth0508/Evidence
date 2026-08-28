import { apiClient } from "@/lib/apiClient";

export interface ReportItem {
  assessmentId: string;
  workspaceId: string;
  workspaceName: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  difficulty: string;
  status: string;
  score: number;
  completedAt: string;
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
  assessmentId?: string;
  score?: number;
  selectionNotes?: string;
  selectedAt: string;
}

export interface AssessmentReportResponse {
  assessmentId: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  overallScore: number;
  scoreRating: string;
  bugsFixedCount: number;
  totalBugsCount: number;
  passedTestsCount: number;
  totalTestsCount: number;
  totalTimeTakenMinutes: number;
  aiSummary: string;
  strengths: string[];
  improvements: string[];
}

export interface TestResultItem {
  testCaseId: string;
  testCaseNumber: number;
  endpoint: string;
  httpMethod: string;
  status: string;
  expectedStatusCode: number;
  actualStatusCode: number;
  executionTimeMs: number;
  failureReason?: string;
}

export const reportService = {
  getReports: async (params?: { page?: number; size?: number; workspaceId?: string }): Promise<ReportListResponse> => {
    return await apiClient.get("/reports", { params });
  },

  getReportById: async (assessmentId: string): Promise<AssessmentReportResponse> => {
    return await apiClient.get(`/assessments/${assessmentId}/report`);
  },

  getReportSummary: async (): Promise<ReportSummaryResponse> => {
    return await apiClient.get("/reports/summary");
  },

  getSelectedCandidates: async (workspaceId?: string): Promise<SelectedCandidateItem[]> => {
    return await apiClient.get("/selected-candidates", { params: { workspaceId } });
  },

  selectCandidate: async (data: {
    workspaceId: string;
    candidateId: string;
    assessmentId?: string;
    selectionNotes?: string;
  }): Promise<SelectedCandidateItem> => {
    return await apiClient.post("/selected-candidates", data);
  },

  removeSelectedCandidate: async (id: string): Promise<any> => {
    return await apiClient.delete(`/selected-candidates/${id}`);
  },

  getTestResults: async (assessmentId: string): Promise<{ assessmentId: string; testResults: TestResultItem[] }> => {
    return await apiClient.get(`/assessments/${assessmentId}/test-results`);
  },
};
