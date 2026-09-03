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
    id?: string;
    assessmentId: string;
    title: string;
    workspaceId?: string;
    workspaceName: string;
    difficulty: string;
    scheduledStartAt: string;
    scheduledEndAt: string;
    durationMinutes: number;
    status?: string;
  }>;
  completedAssessments: Array<{
    id?: string;
    assessmentId: string;
    title: string;
    workspaceId?: string;
    workspaceName: string;
    difficulty?: string;
    score: number;
    completedAt?: string;
    submittedAt?: string;
    durationMinutes?: number;
    timeTakenSeconds?: number;
    status?: string;
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
