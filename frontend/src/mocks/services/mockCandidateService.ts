import { ApiResponse, CandidateReportData, User } from "@/types";
import { mockAaravPatelReport, mockCandidatesList } from "../data/candidates.mock";

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const mockCandidateService = {
  getCandidates: async (): Promise<ApiResponse<User[]>> => {
    await delay(350);
    return {
      success: true,
      message: "Candidates retrieved successfully",
      data: mockCandidatesList,
      timestamp: new Date().toISOString(),
    };
  },

  getCandidateById: async (id: string): Promise<ApiResponse<User>> => {
    await delay(300);
    const candidate = mockCandidatesList.find((c) => c.id === id) || mockCandidatesList[0];
    return {
      success: true,
      message: "Candidate found",
      data: candidate,
      timestamp: new Date().toISOString(),
    };
  },

  getCandidateReport: async (_assessmentId: string): Promise<ApiResponse<CandidateReportData>> => {
    await delay(500);
    return {
      success: true,
      message: "Candidate evaluation report retrieved successfully",
      data: mockAaravPatelReport,
      timestamp: new Date().toISOString(),
    };
  },
};
