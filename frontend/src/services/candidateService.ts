import { apiClient } from "@/lib/apiClient";

export interface CandidateProfile {
  id: string;
  name: string;
  email: string;
  role: string;
  createdAt: string;
}

export interface CandidateWorkspaceDto {
  workspaceId: string;
  workspaceName: string;
  enrolledAt: string;
}

export interface CandidateAssessmentDto {
  assessmentId: string;
  workspaceId: string;
  workspaceName: string;
  difficulty: string;
  status: string;
  score?: number;
  scheduledStartAt: string;
  scheduledEndAt: string;
}

export const candidateService = {
  getAllCandidates: async (query?: string): Promise<CandidateProfile[]> => {
    return await apiClient.get("/candidates", { params: { query } });
  },

  searchCandidateByEmail: async (email: string): Promise<CandidateProfile> => {
    return await apiClient.get("/candidates/search", { params: { email } });
  },

  getCandidateById: async (candidateId: string): Promise<CandidateProfile> => {
    return await apiClient.get(`/candidates/${candidateId}`);
  },

  getCandidateWorkspaces: async (candidateId: string): Promise<CandidateWorkspaceDto[]> => {
    return await apiClient.get(`/candidates/${candidateId}/workspaces`);
  },

  getCandidateAssessments: async (candidateId: string): Promise<CandidateAssessmentDto[]> => {
    return await apiClient.get(`/candidates/${candidateId}/assessments`);
  },
};
