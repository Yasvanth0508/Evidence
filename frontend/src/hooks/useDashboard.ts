import { useQuery } from "@tanstack/react-query";
import { dashboardService } from "@/services/dashboardService";
import { reportService } from "@/services/reportService";
import { RecruiterDashboardData, AssessmentStatus } from "@/types";

export const useRecruiterDashboard = () => {
  return useQuery<RecruiterDashboardData>({
    queryKey: ["recruiter-dashboard"],
    queryFn: async () => {
      const stats = await dashboardService.getRecruiterDashboard();
      const reportsRes = await reportService.getReports({ page: 0, size: 5 });

      return {
        totalCandidates: {
          value: stats.totalCandidates ?? 0,
          changePercentage: 0,
          isPositive: true,
          sparkline: [{ val: 10 }, { val: 20 }, { val: stats.totalCandidates ?? 0 }],
        },
        totalAssessments: {
          value: stats.totalAssessments ?? 0,
          changePercentage: 0,
          isPositive: true,
          sparkline: [{ val: 5 }, { val: 10 }, { val: stats.totalAssessments ?? 0 }],
        },
        completionRate: {
          value: stats.totalAssessments ? `${Math.round(((stats.completedAssessments || 0) / stats.totalAssessments) * 100)}%` : "0%",
          changePercentage: 0,
          isPositive: true,
          sparkline: [{ val: 50 }, { val: 75 }, { val: 100 }],
        },
        avgFixTime: {
          value: "45m",
          changePercentage: 0,
          isPositive: true,
          sparkline: [{ val: 50 }, { val: 45 }, { val: 40 }],
        },
        assessmentStatusDistribution: [
          { name: "Completed", count: stats.completedAssessments || 0, percentage: stats.totalAssessments ? Math.round(((stats.completedAssessments || 0) / stats.totalAssessments) * 100) : 0, color: "#10B981" },
          { name: "Active", count: stats.activeAssessments || 0, percentage: stats.totalAssessments ? Math.round(((stats.activeAssessments || 0) / stats.totalAssessments) * 100) : 0, color: "var(--theme-primary)" },
          { name: "Scheduled", count: Math.max(0, (stats.totalAssessments || 0) - (stats.completedAssessments || 0) - (stats.activeAssessments || 0)), percentage: 0, color: "#3B82F6" },
        ],
        mostFailedBugCategories: [
          { category: "Business Logic Bugs", failureRate: 25, count: 12, color: "#EF4444" },
          { category: "Data Flow Bugs", failureRate: 15, count: 8, color: "#3B82F6" },
          { category: "Syntax / Structural Bugs", failureRate: 5, count: 2, color: "#F59E0B" },
        ],
        recentAssessments: ((reportsRes?.content || reportsRes?.reports) && (reportsRes?.content || reportsRes?.reports)!.length > 0)
          ? (reportsRes.content || reportsRes.reports)!.map((r) => ({
              id: r.assessmentId,
              candidateName: r.candidateName,
              candidateEmail: r.candidateEmail,
              project: `${r.workspaceName} - Assessment`,
              techStack: "Java, Spring Boot",
              status: (r.status as AssessmentStatus) || "COMPLETED",
              timeTaken: "90 mins",
              integrity: 100,
            }))
          : [],
        topPerformers: [],
      };
    },
  });
};

export const useCandidateDashboard = () => {
  return useQuery({
    queryKey: ["candidate-dashboard"],
    queryFn: async () => {
      return await dashboardService.getCandidateDashboard();
    },
  });
};
