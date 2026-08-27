import { apiClient } from "@/lib/apiClient";

export interface RecruiterDashboardData {
  totalWorkspaces: number;
  totalCandidates: number;
  totalAssessments: number;
  activeAssessments: number;
  completedAssessments: number;
}

export interface CandidateDashboardData {
  scheduledAssessments: Array<{
    assessmentId: string;
    workspaceName: string;
    difficulty: string;
    scheduledStartAt: string;
    scheduledEndAt: string;
    durationMinutes: number;
  }>;
  completedAssessments: Array<{
    assessmentId: string;
    workspaceName: string;
    difficulty: string;
    score: number;
    completedAt: string;
  }>;
}

export const dashboardService = {
  getRecruiterDashboard: async (): Promise<RecruiterDashboardData> => {
    return await apiClient.get("/recruiter/dashboard");
  },

  getCandidateDashboard: async (): Promise<CandidateDashboardData> => {
    return await apiClient.get("/candidate/dashboard");
  },
};
