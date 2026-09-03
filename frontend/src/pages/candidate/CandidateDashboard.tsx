import { useState, useMemo, useEffect, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";
import { HRAssessment } from "@/store/hrStore";
import { dashboardService } from "@/services/dashboardService";
import { ScheduledAssessmentCard } from "@/components/candidate/ScheduledAssessmentCard";
import { CompletedAssessmentCard } from "@/components/candidate/CompletedAssessmentCard";
import { ThemeToggle } from "@/components/common/ThemeToggle";
import { Button } from "@/components/ui/button";
import {
  Code2,
  CheckCircle2,
  Clock,
  LogOut,
  Loader2,
  AlertCircle,
  RefreshCw,
} from "lucide-react";

type CandidateAssessmentItem = HRAssessment & { workspaceName?: string };

const mapScheduledAssessment = (asmt: any, userId: string): CandidateAssessmentItem => {
  const id = asmt.id || asmt.assessmentId || "";
  const startDate = asmt.scheduledStartAt ? new Date(asmt.scheduledStartAt) : null;
  const isInvalidDate = !startDate || isNaN(startDate.getTime());

  // Use the assessment's genuine title, only falling back to workspace name if title is truly missing
  const assessmentTitle =
    asmt.title && asmt.title.trim()
      ? asmt.title.trim()
      : `${asmt.workspaceName || "Technical"} Assessment`;

  return {
    id,
    workspaceId: asmt.workspaceId || "",
    candidateId: userId,
    title: assessmentTitle,
    workspaceName: asmt.workspaceName || "Placement Drive",
    category: asmt.category || "Spring Boot REST API",
    repositoryUrl: asmt.repositoryUrl || "https://github.com/scanurag/FoodFrenzy.git",
    branchName: asmt.branchName || "master",
    backendRootDirectory: asmt.backendRootDirectory || "",
    difficulty: (asmt.difficulty as any) || "INTERMEDIATE",
    durationMinutes: asmt.durationMinutes || 90,
    scheduledDate: !isInvalidDate
      ? startDate.toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" })
      : "Today",
    scheduledTime: !isInvalidDate
      ? startDate.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
      : "10:00 AM",
    scheduledStartAt: asmt.scheduledStartAt,
    scheduledEndAt: asmt.scheduledEndAt,
    status: (asmt.status as any) || "SCHEDULED",
  };
};

const mapCompletedAssessment = (asmt: any, userId: string): CandidateAssessmentItem => {
  const id = asmt.id || asmt.assessmentId || "";
  const completedDate = asmt.completedAt || asmt.submittedAt;
  const parsedDate = completedDate ? new Date(completedDate) : null;
  const isInvalidDate = !parsedDate || isNaN(parsedDate.getTime());

  const assessmentTitle =
    asmt.title && asmt.title.trim()
      ? asmt.title.trim()
      : `${asmt.workspaceName || "Technical"} Assessment`;

  return {
    id,
    workspaceId: asmt.workspaceId || "",
    candidateId: userId,
    title: assessmentTitle,
    workspaceName: asmt.workspaceName || "Placement Drive",
    category: asmt.category || "Spring Boot REST API",
    repositoryUrl: asmt.repositoryUrl || "https://github.com/scanurag/FoodFrenzy.git",
    branchName: asmt.branchName || "master",
    backendRootDirectory: asmt.backendRootDirectory || "",
    difficulty: (asmt.difficulty as any) || "INTERMEDIATE",
    durationMinutes: asmt.durationMinutes || 90,
    scheduledDate: !isInvalidDate
      ? parsedDate.toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" })
      : "Recently",
    scheduledTime: !isInvalidDate
      ? parsedDate.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
      : "",
    completedAt: !isInvalidDate
      ? `${parsedDate.toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" })} at ${parsedDate.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`
      : "Recently",
    status: "COMPLETED",
    score: typeof asmt.score === "number" ? asmt.score : null,
    totalTests: asmt.totalTests ?? 10,
    passedTests: asmt.passedTests ?? (typeof asmt.score === "number" ? Math.round((asmt.score / 100) * 10) : 8),
    failedTests: asmt.failedTests ?? 0,
  };
};

const deduplicateAssessments = (items: CandidateAssessmentItem[]): CandidateAssessmentItem[] => {
  const seen = new Set<string>();
  const result: CandidateAssessmentItem[] = [];
  for (const item of items) {
    if (item.id && !seen.has(item.id)) {
      seen.add(item.id);
      result.push(item);
    }
  }
  return result;
};

export const CandidateDashboard = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const [scheduled, setScheduled] = useState<CandidateAssessmentItem[]>([]);
  const [completed, setCompleted] = useState<CandidateAssessmentItem[]>([]);
  const [activeTab, setActiveTab] = useState<"ALL" | "SCHEDULED" | "COMPLETED">("ALL");
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboardData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await dashboardService.getCandidateDashboard();
      if (data) {
        const rawScheduled = (data.scheduledAssessments || []).map((a) =>
          mapScheduledAssessment(a, user?.id || "")
        );
        const rawCompleted = (data.completedAssessments || []).map((a) =>
          mapCompletedAssessment(a, user?.id || "")
        );

        setScheduled(deduplicateAssessments(rawScheduled));
        setCompleted(deduplicateAssessments(rawCompleted));
      }
    } catch (err: any) {
      console.error("Candidate dashboard fetch error:", err);
      setError(err?.message || "Failed to load assessments from server.");
    } finally {
      setIsLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  const activeCandidate = useMemo(() => {
    return {
      id: user?.id || "",
      name: user?.name || "Candidate",
      email: user?.email || "",
      role: user?.role || "CANDIDATE",
      avatarUrl: user?.avatarUrl || "",
    };
  }, [user]);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] dark:bg-[#0B0F19] text-gray-900 dark:text-slate-100 flex flex-col font-sans transition-colors duration-200">
      {/* 1. Candidate Portal Topbar */}
      <header className="sticky top-0 z-50 bg-white/90 dark:bg-slate-950/90 backdrop-blur-md border-b border-gray-100 dark:border-slate-800 shadow-2xs transition-colors">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-xl bg-primary-light dark:bg-primary/20 text-primary dark:text-primary flex items-center justify-center font-bold">
                <Code2 className="w-5 h-5" />
              </div>
              <span className="font-extrabold text-xl text-gray-900 dark:text-white tracking-tight">
                EVIDENCE
              </span>
            </Link>
            <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-50 dark:bg-emerald-950/50 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
              Candidate Portal
            </span>
          </div>

          {/* User Profile & Actions */}
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={fetchDashboardData}
              disabled={isLoading}
              className="text-xs font-semibold text-gray-600 dark:text-slate-300 border-gray-200 dark:border-slate-700 hover:bg-gray-50 dark:hover:bg-slate-800 h-8 gap-1.5"
              title="Refresh assessments"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? "animate-spin" : ""}`} />
              <span className="hidden sm:inline">Refresh</span>
            </Button>

            {/* Dark Mode Switcher */}
            <ThemeToggle size="sm" />

            {/* Logout button */}
            <Button
              variant="outline"
              size="sm"
              onClick={handleLogout}
              className="text-xs font-semibold text-gray-700 dark:text-slate-300 border-gray-200 dark:border-slate-700 hover:bg-rose-50 dark:hover:bg-rose-950/40 hover:text-rose-600 dark:hover:text-rose-400 h-8"
            >
              <LogOut className="w-3.5 h-3.5 mr-1" /> Sign Out
            </Button>
          </div>
        </div>
      </header>

      {/* 2. Main Content Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Candidate Profile Header Card */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-2xs transition-colors">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div className="flex items-center gap-4">
              <img
                src={
                  activeCandidate.avatarUrl ||
                  "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80"
                }
                alt={activeCandidate.name}
                className="w-16 h-16 rounded-2xl object-cover border-2 border-primary-border/60 dark:border-primary/30 shadow-2xs flex-shrink-0"
              />
              <div className="space-y-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <h1 className="text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight">
                    Welcome, {activeCandidate.name}
                  </h1>
                  <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-primary-light dark:bg-primary/20 text-primary dark:text-primary border border-primary-border dark:border-primary/30">
                    Java Candidate
                  </span>
                </div>
                <p className="text-xs text-gray-500 dark:text-slate-400 font-medium font-mono">
                  {activeCandidate.email} • {activeCandidate.role}
                </p>
              </div>
            </div>

            {/* Quick Metrics */}
            <div className="flex items-center gap-4 border-t md:border-t-0 md:border-l border-gray-100 dark:border-slate-800 pt-4 md:pt-0 md:pl-6">
              <div>
                <span className="text-[10px] uppercase font-bold text-gray-400 dark:text-slate-500 block tracking-wider">
                  Scheduled
                </span>
                <span className="text-2xl font-black text-primary dark:text-primary block">
                  {isLoading ? "-" : scheduled.length}
                </span>
              </div>
              <div className="h-8 w-px bg-gray-100 dark:bg-slate-800"></div>
              <div>
                <span className="text-[10px] uppercase font-bold text-gray-400 dark:text-slate-500 block tracking-wider">
                  Completed
                </span>
                <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400 block">
                  {isLoading ? "-" : completed.length}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Error Alert with Retry */}
        {error && (
          <div className="bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-900/50 rounded-2xl p-4 flex items-center justify-between text-xs text-amber-800 dark:text-amber-300">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-4 h-4 text-amber-600 dark:text-amber-400 flex-shrink-0" />
              <span>{error}</span>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={fetchDashboardData}
              className="h-7 text-xs font-semibold hover:bg-amber-100 dark:hover:bg-amber-900/40"
            >
              <RefreshCw className="w-3 h-3 mr-1" /> Retry
            </Button>
          </div>
        )}

        {/* Section Navigation Tabs */}
        <div className="flex items-center gap-2 border-b border-gray-200 dark:border-slate-800 pb-2">
          <button
            onClick={() => setActiveTab("ALL")}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              activeTab === "ALL"
                ? "bg-[#0F172A] dark:bg-slate-800 text-white shadow-xs"
                : "text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-slate-850"
            }`}
          >
            All Assessments ({isLoading ? "-" : scheduled.length + completed.length})
          </button>
          <button
            onClick={() => setActiveTab("SCHEDULED")}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer ${
              activeTab === "SCHEDULED"
                ? "bg-primary text-white shadow-xs"
                : "text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-slate-850"
            }`}
          >
            <Clock className="w-3.5 h-3.5" />
            Scheduled Assessments ({isLoading ? "-" : scheduled.length})
          </button>
          <button
            onClick={() => setActiveTab("COMPLETED")}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer ${
              activeTab === "COMPLETED"
                ? "bg-emerald-600 text-white shadow-xs"
                : "text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-slate-850"
            }`}
          >
            <CheckCircle2 className="w-3.5 h-3.5" />
            Completed Assessments ({isLoading ? "-" : completed.length})
          </button>
        </div>

        {/* Loading State */}
        {isLoading && (
          <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-100 dark:border-slate-800 p-12 text-center space-y-3 shadow-2xs">
            <Loader2 className="w-8 h-8 text-primary animate-spin mx-auto" />
            <p className="text-sm font-semibold text-gray-700 dark:text-slate-200">Loading your assessments...</p>
            <p className="text-xs text-gray-400 dark:text-slate-500">Fetching technical evaluations from server</p>
          </div>
        )}

        {/* ---------------------------------------------------- */}
        {/* 3. Scheduled Assessments Section (Requirement 3)     */}
        {/* ---------------------------------------------------- */}
        {!isLoading && (activeTab === "ALL" || activeTab === "SCHEDULED") && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-lg bg-primary-light dark:bg-primary/25 text-primary dark:text-primary flex items-center justify-center font-bold text-xs">
                  <Clock className="w-4 h-4" />
                </div>
                <div>
                  <h2 className="text-lg font-extrabold text-gray-900 dark:text-white tracking-tight">
                    Scheduled Assessments
                  </h2>
                  <p className="text-xs text-gray-500 dark:text-slate-400">
                    Live second-by-second countdown for upcoming technical evaluations.
                  </p>
                </div>
              </div>

              <span className="text-xs font-bold text-gray-400 dark:text-slate-500">
                {scheduled.length} Scheduled
              </span>
            </div>

            {scheduled.length === 0 ? (
              <div className="bg-white dark:bg-slate-900 rounded-3xl border border-dashed border-gray-300 dark:border-slate-700 p-10 text-center space-y-2">
                <Clock className="w-10 h-10 text-gray-300 dark:text-slate-600 mx-auto" />
                <h4 className="text-sm font-bold text-gray-800 dark:text-slate-200">
                  No Pending Scheduled Assessments
                </h4>
                <p className="text-xs text-gray-500 dark:text-slate-400 max-w-sm mx-auto">
                  When recruiters schedule an assessment for your email, it will appear here with a real-time countdown timer.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {scheduled.map((asmt) => (
                  <ScheduledAssessmentCard key={asmt.id} assessment={asmt} />
                ))}
              </div>
            )}
          </div>
        )}

        {/* ---------------------------------------------------- */}
        {/* 4. Completed Assessments Section (Requirement 2)     */}
        {/* ---------------------------------------------------- */}
        {!isLoading && (activeTab === "ALL" || activeTab === "COMPLETED") && (
          <div className="space-y-4 pt-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-lg bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-400 flex items-center justify-center font-bold text-xs">
                  <CheckCircle2 className="w-4 h-4" />
                </div>
                <div>
                  <h2 className="text-lg font-extrabold text-gray-900 dark:text-white tracking-tight">
                    Completed Assessments
                  </h2>
                  <p className="text-xs text-gray-500 dark:text-slate-400">
                    Review your completed assessment results and evaluation scores.
                  </p>
                </div>
              </div>

              <span className="text-xs font-bold text-gray-400 dark:text-slate-500">
                {completed.length} Completed
              </span>
            </div>

            {completed.length === 0 ? (
              <div className="bg-white dark:bg-slate-900 rounded-3xl border border-dashed border-gray-300 dark:border-slate-700 p-10 text-center space-y-2">
                <CheckCircle2 className="w-10 h-10 text-gray-300 dark:text-slate-600 mx-auto" />
                <h4 className="text-sm font-bold text-gray-800 dark:text-slate-200">
                  No Completed Assessments Yet
                </h4>
                <p className="text-xs text-gray-500 dark:text-slate-400 max-w-sm mx-auto">
                  Once you take and submit an assessment in the browser Monaco IDE, your score and evaluation details will appear here.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {completed.map((asmt) => (
                  <CompletedAssessmentCard key={asmt.id} assessment={asmt} />
                ))}
              </div>
            )}
          </div>
        )}
      </main>

      {/* 5. Footer */}
      <footer className="bg-white dark:bg-slate-950 border-t border-gray-200 dark:border-slate-800 py-6 mt-12 transition-colors">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 dark:text-slate-500 gap-4">
          <p>© 2026 EVIDENCE. Recruiter & Candidate Technical Assessment Platform.</p>
          <div className="flex space-x-6">
            <Link to="/#overview" className="hover:text-gray-600 dark:hover:text-slate-300">Home</Link>
            <Link to="/#how-it-works" className="hover:text-gray-600 dark:hover:text-slate-300">How It Works</Link>
          </div>
        </div>
      </footer>
    </div>
  );
};
