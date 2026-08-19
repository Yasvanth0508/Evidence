import { useQuery } from "@tanstack/react-query";
import { mockDashboardService } from "@/mocks/services/mockDashboardService";

export const useRecruiterDashboard = () => {
  return useQuery({
    queryKey: ["recruiter-dashboard"],
    queryFn: async () => {
      const response = await mockDashboardService.getRecruiterDashboard();
      return response.data;
    },
  });
};
