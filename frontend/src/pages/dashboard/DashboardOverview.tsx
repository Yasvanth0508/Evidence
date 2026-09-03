import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useHRStore } from "@/store/hrStore";
import { workspaceService } from "@/services/workspaceService";
import { dashboardService, RecruiterDashboardData } from "@/services/dashboardService";
import { Button } from "@/components/ui/button";
import {
  Folder,
  Users2,
  FileCheck2,
  CheckCircle2,
  UserCheck2,
  FolderPlus,
  ArrowRight,
  TrendingUp,
  Calendar,
  Clock,
  Sparkles,
  Award,
} from "lucide-react";

export const DashboardOverview = () => {
  const navigate = useNavigate();
  const {
    workspaces,
    candidates,
    assessments,
    setWorkspaces,
    getDashboardMetrics,
  } = useHRStore();

  const [backendMetrics, setBackendMetrics] = useState<RecruiterDashboardData | null>(null);

  // Sync backend workspaces & metrics on load
  useEffect(() => {
    let isMounted = true;
    workspaceService
      .getWorkspaces()
      .then((backendWsList) => {
        if (isMounted && Array.isArray(backendWsList)) {
          const mapped = backendWsList.map((bWs) => ({
            id: bWs.id,
            name: bWs.name,
            description: bWs.description || "",
            track: "Java Spring Boot Backend",
            defaultDurationMinutes: 90,
            status: (bWs.status as any) || "ACTIVE",
            createdAt: new Date().toISOString().split("T")[0],
            candidateIds: [],
          }));
          setWorkspaces(mapped);
        }
      })
      .catch((err) => {
        console.debug("Backend workspace sync skipped in dashboard:", err.message);
      });

    dashboardService
      .getRecruiterDashboard()
      .then((data) => {
        if (isMounted && data) {
          setBackendMetrics(data);
        }
      })
      .catch((err) => {
        console.debug("Backend dashboard metrics fetch skipped:", err.message);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const localMetrics = getDashboardMetrics();
  const metrics = {
    totalWorkspaces: backendMetrics?.totalWorkspaces ?? localMetrics.totalWorkspaces,
    totalCandidates: backendMetrics?.totalCandidates ?? localMetrics.totalCandidates,
    candidatesAssigned: backendMetrics?.totalAssessments ?? localMetrics.candidatesAssigned,
    completedAssessments: backendMetrics?.completedAssessments ?? localMetrics.completedAssessments,
    selectedCandidates: localMetrics.selectedCandidates,
    avgScore: localMetrics.avgScore,
  };

  const recentAssessments = assessments.map((asmt) => {
    const cand = candidates.find((c) => c.id === asmt.candidateId);
    const ws = workspaces.find((w) => w.id === asmt.workspaceId);
    return {
      ...asmt,
      candidateName: cand ? cand.name : "Candidate",
      candidateEmail: cand ? cand.email : "",
      workspaceName: ws ? ws.name : "Workspace",
      role: cand ? cand.role : "Java Developer",
    };
  });

  return (
    <div className="space-y-8">
      {/* 1. Welcome & HR Platform Summary Banner */}
      <div className="bg-white rounded-3xl border border-gray-200/90 p-6 sm:p-8 shadow-2xs flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="space-y-1.5">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-primary-light text-primary border border-primary-border/60">
            <Sparkles className="w-3 h-3 text-primary" />
            HR Assessment Command Center
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
            Recruitment Dashboard
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 max-w-xl leading-relaxed">
            Monitor real-time placement workspaces, candidate assessment attendance, and automated code evaluation benchmarks.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <Button
            size="default"
            onClick={() => navigate("/dashboard/workspaces")}
            className="font-semibold gap-2 bg-primary hover:bg-primary-hover text-white shadow-xs text-xs"
          >
            <FolderPlus className="w-4 h-4" />
            Manage Workspaces
          </Button>
          <Button
            variant="outline"
            size="default"
            onClick={() => navigate("/dashboard/selected-candidates")}
            className="font-semibold gap-2 border-gray-300 text-gray-700 hover:bg-gray-50 shadow-2xs text-xs"
          >
            <UserCheck2 className="w-4 h-4 text-emerald-600" />
            View Selected
          </Button>
        </div>
      </div>

      {/* 2. Key Metrics Row (6 Metric Cards Matching Requirement 2) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        {/* 1. Total Workspaces */}
        <Link
          to="/dashboard/workspaces"
          className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-5 shadow-2xs hover:shadow-md hover:border-primary-border dark:hover:border-primary/50 transition-all space-y-2 group"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-500 dark:text-slate-400 group-hover:text-gray-900 dark:group-hover:text-white transition-colors">
              Workspaces
            </span>
            <div className="w-8 h-8 rounded-xl bg-primary-light dark:bg-primary/20 text-primary dark:text-primary flex items-center justify-center">
              <Folder className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-black text-gray-900 dark:text-white">{metrics.totalWorkspaces}</span>
            <span className="text-[11px] font-bold text-emerald-600 dark:text-emerald-400 flex items-center">
              <TrendingUp className="w-3 h-3 mr-0.5" /> Active
            </span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">Campus & Hiring Hubs</p>
        </Link>

        {/* 2. Total Candidates */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-5 shadow-2xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-500 dark:text-slate-400">Total Candidates</span>
            <div className="w-8 h-8 rounded-xl bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 flex items-center justify-center">
              <Users2 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-black text-gray-900 dark:text-white">{metrics.totalCandidates}</span>
            <span className="text-[11px] font-bold text-blue-600 dark:text-blue-400">Registered</span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">In Global Candidate Pool</p>
        </div>

        {/* 3. Candidates Assigned */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-5 shadow-2xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-500 dark:text-slate-400">Assigned</span>
            <div className="w-8 h-8 rounded-xl bg-purple-50 dark:bg-purple-950/40 text-purple-600 dark:text-purple-400 flex items-center justify-center">
              <FileCheck2 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-black text-purple-700 dark:text-purple-300">{metrics.candidatesAssigned}</span>
            <span className="text-[11px] font-bold text-purple-600 dark:text-purple-400">Candidates</span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">Assigned to Assessments</p>
        </div>

        {/* 4. Completed Assessments */}
        <Link
          to="/dashboard/reports"
          className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-5 shadow-2xs hover:shadow-md hover:border-emerald-300 dark:hover:border-emerald-500/50 transition-all space-y-2 group"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-500 dark:text-slate-400 group-hover:text-gray-900 dark:group-hover:text-white transition-colors">
              Completed
            </span>
            <div className="w-8 h-8 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-black text-emerald-700 dark:text-emerald-400">{metrics.completedAssessments}</span>
            <span className="text-[11px] font-bold text-emerald-600 dark:text-emerald-400">Evaluated</span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">Automated Scoring Done</p>
        </Link>

        {/* 5. Selected Candidates */}
        <Link
          to="/dashboard/selected-candidates"
          className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-5 shadow-2xs hover:shadow-md hover:border-emerald-300 dark:hover:border-emerald-500/50 transition-all space-y-2 group"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-500 dark:text-slate-400 group-hover:text-gray-900 dark:group-hover:text-white transition-colors">
              Selected
            </span>
            <div className="w-8 h-8 rounded-xl bg-emerald-100/70 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-400 flex items-center justify-center">
              <UserCheck2 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-black text-emerald-800 dark:text-emerald-400">{metrics.selectedCandidates}</span>
            <span className="text-[11px] font-bold text-emerald-600 dark:text-emerald-400">For Hiring</span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">Offer / Shortlist Stage</p>
        </Link>

        {/* 6. Avg Score */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-5 shadow-2xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-500 dark:text-slate-400">Average Score</span>
            <div className="w-8 h-8 rounded-xl bg-primary-light dark:bg-primary/20 text-primary dark:text-primary flex items-center justify-center">
              <Award className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-black text-gray-900 dark:text-white">{metrics.avgScore}%</span>
            <span className="text-[11px] font-bold text-gray-500 dark:text-slate-400">0–100 Scale</span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">Spring Boot Verification</p>
        </div>
      </div>

      {/* 3. Workspaces Directory Preview (Folder Style) */}
      <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-2xs space-y-6 transition-colors">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-gray-100 dark:border-slate-800">
          <div>
            <h2 className="text-lg font-bold text-gray-900 dark:text-white">
              Active Placement Workspaces
            </h2>
            <p className="text-xs text-gray-500 dark:text-slate-400">
              Click any workspace folder to inspect candidates, add profiles, or schedule assessments.
            </p>
          </div>

          <Link
            to="/dashboard/workspaces"
            className="inline-flex items-center gap-1 text-xs font-bold text-primary dark:text-primary hover:underline"
          >
            <span>View All Workspaces ({workspaces.length})</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {workspaces.slice(0, 3).map((ws) => (
            <Link
              key={ws.id}
              to={`/dashboard/workspaces/${ws.id}`}
              className="group bg-gray-50/70 dark:bg-slate-800/60 hover:bg-white dark:hover:bg-slate-800 rounded-2xl border border-gray-200/80 dark:border-slate-700 p-5 shadow-2xs hover:shadow-md hover:border-primary-border dark:hover:border-primary/50 transition-all flex flex-col justify-between"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="w-10 h-10 rounded-xl bg-primary-light dark:bg-primary/25 text-primary dark:text-primary flex items-center justify-center group-hover:scale-105 transition-transform">
                    <Folder className="w-5 h-5 fill-primary/20 dark:dark:fill-primary/30" />
                  </div>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                    {ws.status}
                  </span>
                </div>

                <div>
                  <h3 className="font-bold text-sm text-gray-900 dark:text-white group-hover:text-primary dark:group-hover:text-primary transition-colors">
                    {ws.name}
                  </h3>
                  <p className="text-[11px] text-gray-500 dark:text-slate-400 line-clamp-1 mt-0.5">
                    {ws.description}
                  </p>
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-gray-200/60 dark:border-slate-700 flex items-center justify-between text-xs text-gray-600 dark:text-slate-300">
                <span className="flex items-center gap-1 font-semibold">
                  <Users2 className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
                  {ws.candidateIds.length} Candidates
                </span>
                <span className="text-[11px] text-primary dark:text-primary font-bold flex items-center gap-0.5">
                  Open <ArrowRight className="w-3 h-3" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* 4. Recent Assessment Activity */}
      <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 shadow-2xs overflow-hidden transition-colors">
        <div className="p-6 border-b border-gray-100 dark:border-slate-800 flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-gray-900 dark:text-white">Recent Candidate Activity</h2>
            <p className="text-xs text-gray-500 dark:text-slate-400">Live view of candidates, schedules, and evaluation status.</p>
          </div>

          <Link
            to="/dashboard/reports"
            className="inline-flex items-center gap-1 text-xs font-bold text-primary dark:text-primary hover:underline"
          >
            <span>View Full Reports</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-gray-50/80 dark:bg-slate-800/80 text-gray-500 dark:text-slate-400 font-semibold uppercase tracking-wider border-b border-gray-100 dark:border-slate-800">
              <tr>
                <th className="px-6 py-3.5">Candidate</th>
                <th className="px-6 py-3.5">Workspace</th>
                <th className="px-6 py-3.5">Assessment Details</th>
                <th className="px-6 py-3.5">Scheduled Window</th>
                <th className="px-6 py-3.5">Status</th>
                <th className="px-6 py-3.5 text-right">Score</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-slate-800">
              {recentAssessments.map((asmt) => (
                <tr key={asmt.id} className="hover:bg-primary-light/30 dark:hover:bg-slate-800/50 transition-colors">
                  <td className="px-6 py-4">
                    <span className="font-bold text-gray-900 dark:text-white block">{asmt.candidateName}</span>
                    <span className="text-[11px] text-gray-500 dark:text-slate-400 font-mono">{asmt.candidateEmail}</span>
                  </td>
                  <td className="px-6 py-4 font-semibold text-gray-800 dark:text-slate-200">
                    {asmt.workspaceName}
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-gray-900 dark:text-white font-medium block truncate max-w-[220px]">
                      {asmt.title}
                    </span>
                    <span className="text-[10px] text-gray-400 dark:text-slate-500 font-mono">
                      {asmt.difficulty} • {asmt.durationMinutes}m
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-600 dark:text-slate-400">
                    <div className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
                      <span>{asmt.scheduledDate}</span>
                    </div>
                    <span className="text-[11px] text-gray-400 dark:text-slate-500 pl-4">{asmt.scheduledTime}</span>
                  </td>
                  <td className="px-6 py-4">
                    {asmt.status === "COMPLETED" ? (
                      <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/50 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                        <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
                        Completed
                      </span>
                    ) : asmt.status === "SCHEDULED" ? (
                      <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-blue-50 dark:bg-blue-950/50 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800">
                        <Clock className="w-3 h-3 text-blue-600 dark:text-blue-400" />
                        Scheduled
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-amber-50 dark:bg-amber-950/50 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800">
                        Assigned
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-right">
                    {typeof asmt.score === "number" ? (
                      <span className="font-extrabold text-sm text-emerald-600 dark:text-emerald-400">
                        {asmt.score}%
                      </span>
                    ) : (
                      <span className="text-gray-400 dark:text-slate-500 font-mono">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
