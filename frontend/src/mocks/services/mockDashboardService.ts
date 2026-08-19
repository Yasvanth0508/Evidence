import { ApiResponse, RecruiterDashboardData } from "@/types";
import { mockDashboardData } from "../data/dashboard.mock";

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const mockDashboardService = {
  getRecruiterDashboard: async (): Promise<ApiResponse<RecruiterDashboardData>> => {
    await delay(400);
    return {
      success: true,
      message: "Recruiter dashboard statistics fetched successfully",
      data: mockDashboardData,
      timestamp: new Date().toISOString(),
    };
  },
};
