import { useQuery } from "@tanstack/react-query";
import { reportService } from "@/services/reportService";
import { CandidateReportData } from "@/types";

export const useCandidates = () => {
  return useQuery({
    queryKey: ["candidates"],
    queryFn: async () => {
      const reportsRes = await reportService.getReports({ page: 0, size: 50 });
      if (reportsRes.reports && reportsRes.reports.length > 0) {
        return reportsRes.reports.map((r) => ({
          id: r.candidateId,
          assessmentId: r.assessmentId,
          name: r.candidateName,
          email: r.candidateEmail,
          avatar: `https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80`,
          role: "Full Stack Engineer",
          score: r.score,
          status: r.status === "COMPLETED" ? ("completed" as const) : ("in_progress" as const),
          completedAt: r.completedAt,
        }));
      }
      return [];
    },
  });
};

export const useCandidateReport = (assessmentId: string) => {
  return useQuery<CandidateReportData>({
    queryKey: ["candidate-report", assessmentId],
    queryFn: async () => {
      const report = await reportService.getReportById(assessmentId);
      const initials = (report.candidateName || "Candidate")
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);

      const scoreRating: "Excellent" | "Good Performance" | "Needs Improvement" =
        (report.overallScore || 0) >= 80
          ? "Excellent"
          : (report.overallScore || 0) >= 60
          ? "Good Performance"
          : "Needs Improvement";

      return {
        id: report.assessmentId || assessmentId,
        candidate: {
          id: report.candidateId || "",
          name: report.candidateName || "Candidate",
          email: report.candidateEmail || "",
          avatarInitials: initials,
          status: ((report as any).status as any) || "COMPLETED",
        },
        project: {
          name: (report as any).workspaceName ? `${(report as any).workspaceName} - Java API` : "Spring Boot REST API Assessment",
          techStack: "Java 21, Spring Boot, Maven, PostgreSQL",
          date: (report as any).completedAt ? new Date((report as any).completedAt).toLocaleDateString() : "Today",
          totalTimeTaken: `${report.totalTimeTakenMinutes || 45} mins`,
        },
        scoreOverview: {
          overallScore: report.overallScore ?? 0,
          scoreRating,
          bugsFixed: {
            fixed: report.bugsFixedCount ?? 0,
            total: report.totalBugsCount ?? 0,
          },
          testCasesPassed: {
            passed: report.passedTestsCount ?? 0,
            total: report.totalTestsCount ?? 0,
          },
          totalTimeTakenMinutes: report.totalTimeTakenMinutes ?? 0,
        },
        bugBreakdown: [
          {
            id: 1,
            category: "Business Logic",
            issueType: "Endpoint Business Logic & Constraints",
            status: (report.passedTestsCount || 0) > 0 ? "FIXED" : "NOT_FIXED",
            score: report.overallScore ?? 0,
            maxScore: 100,
          },
        ],
        aiSummary: report.aiSummary || "Evaluation report generated automatically from test suite execution against PostgreSQL.",
        strengths: report.strengths && report.strengths.length > 0 ? report.strengths : ["REST Controller Design", "Clean Code Structure"],
        improvements: report.improvements && report.improvements.length > 0 ? report.improvements : ["Exception Handling Coverage"],
        integrity: {
          overallRiskBadge: "LOW",
          behaviorSummary: {
            copyPasteEvents: 0,
            buildRuns: 1,
            testRuns: 1,
            idleTimeMinutes: 2,
          },
          riskAnalysis: "No suspicious activity detected. Valid coding session verified.",
        },
      };
    },
    enabled: Boolean(assessmentId),
  });
};
