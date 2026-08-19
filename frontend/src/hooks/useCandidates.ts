import { useQuery } from "@tanstack/react-query";
import { mockCandidateService } from "@/mocks/services/mockCandidateService";

export const useCandidates = () => {
  return useQuery({
    queryKey: ["candidates"],
    queryFn: async () => {
      const response = await mockCandidateService.getCandidates();
      return response.data;
    },
  });
};

export const useCandidateReport = (assessmentId: string) => {
  return useQuery({
    queryKey: ["candidate-report", assessmentId],
    queryFn: async () => {
      const response = await mockCandidateService.getCandidateReport(assessmentId);
      return response.data;
    },
    enabled: Boolean(assessmentId),
  });
};
