import { useParams, Link, useNavigate } from "react-router-dom";
import { useCandidatePersonalReport } from "@/hooks/useCandidates";
import { useAuthStore } from "@/store/authStore";
import { ScoreProgressCircle } from "@/components/candidate/ScoreProgressCircle";
import { AiSummaryCard } from "@/components/candidate/AiSummaryCard";
import { StrengthsImprovements } from "@/components/candidate/StrengthsImprovements";
import { ThemeToggle } from "@/components/common/ThemeToggle";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Code2,
  CheckCircle2,
  Clock,
  LogOut,
  ArrowLeft,
  Loader2,
  Download,
  ShieldCheck,
  Layers,
  AlertCircle,
  Award,
} from "lucide-react";

export const CandidatePerformanceReportPage = () => {
  const { id = "" } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const { data: report, isLoading, error, refetch } = useCandidatePersonalReport(id);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#F9FAFB] dark:bg-[#0B0F19] flex flex-col items-center justify-center space-y-3 font-sans">
        <Loader2 className="w-9 h-9 text-primary animate-spin" />
        <span className="text-sm font-semibold text-gray-500 dark:text-slate-400">
          Loading Your Performance Report...
        </span>
      </div>
    );
  }

  if (error || !report) {
    return (
      <div className="min-h-screen bg-[#F9FAFB] dark:bg-[#0B0F19] p-6 flex flex-col items-center justify-center font-sans">
        <div className="p-10 text-center bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 shadow-sm max-w-md w-full space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-amber-50 dark:bg-amber-950/50 text-amber-600 dark:text-amber-400 flex items-center justify-center mx-auto">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div className="space-y-1.5">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Report Not Available Yet
            </h3>
            <p className="text-xs text-gray-500 dark:text-slate-400 leading-relaxed">
              Your assessment evaluation may still be processing or requires a brief moment to finalize.
            </p>
          </div>
          <div className="pt-2 flex items-center justify-center gap-3">
            <Button size="sm" variant="outline" className="text-xs font-semibold" onClick={() => refetch()}>
              Retry Loading
            </Button>
            <Link to="/candidate/dashboard">
              <Button size="sm" className="text-xs font-semibold bg-primary hover:bg-primary-hover text-white">
                Back to Dashboard
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const rawScore = typeof report.score === "number" ? Math.round(report.score) : 0;
  const rating = report.scoreRating || (rawScore >= 80 ? "Excellent" : rawScore >= 60 ? "Good Performance" : "Needs Improvement");
  const candidateName = user?.name || "Candidate";
  const candidateEmail = user?.email || "";
  const initials =
    candidateName
      .split(" ")
      .map((n: string) => n[0])
      .join("")
      .toUpperCase()
      .slice(0, 2) || "CD";

  const timeTaken = report.timeTakenMinutes || Math.max(1, Math.round((report.timeTakenSeconds || 0) / 60));

  return (
    <div className="min-h-screen bg-[#F9FAFB] dark:bg-[#0B0F19] text-gray-900 dark:text-slate-100 flex flex-col font-sans transition-colors duration-200">
      {/* 1. Candidate Portal Header Topbar */}
      <header className="sticky top-0 z-50 bg-white/90 dark:bg-slate-950/90 backdrop-blur-md border-b border-gray-100 dark:border-slate-800 shadow-2xs transition-colors">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/candidate/dashboard" className="flex items-center gap-2">
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

          <div className="flex items-center gap-3">
            <ThemeToggle size="sm" />
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

      {/* 2. Main Report Container */}
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Navigation Breadcrumb & Page Title */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <Link
              to="/candidate/dashboard"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-gray-500 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors mb-2"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Back to Candidate Dashboard</span>
            </Link>
            <h1 className="text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight">
              Assessment Performance Report
            </h1>
          </div>

          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              className="gap-2 text-xs font-semibold border-gray-300 dark:border-slate-700 shadow-2xs"
              onClick={() => window.print()}
            >
              <Download className="w-3.5 h-3.5" />
              <span>Download PDF</span>
            </Button>
          </div>
        </div>

        {/* Candidate & Assessment Meta Card */}
        <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-3xl p-6 sm:p-7 shadow-2xs transition-colors">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-primary-light/80 dark:bg-primary/25 border border-primary-border dark:border-primary/30 text-primary dark:text-primary flex items-center justify-center font-black text-lg shadow-sm">
                {initials}
              </div>
              <div>
                <div className="flex items-center gap-2.5">
                  <h2 className="text-lg font-extrabold text-gray-900 dark:text-white leading-tight">
                    {candidateName}
                  </h2>
                  <Badge variant="completed" dot>
                    Completed & Scored
                  </Badge>
                </div>
                <p className="text-xs text-gray-500 dark:text-slate-400 font-mono mt-0.5">
                  {candidateEmail}
                </p>
              </div>
            </div>

            {/* Assessment Meta Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 pt-4 md:pt-0 border-t md:border-t-0 border-gray-100 dark:border-slate-800 text-xs">
              <div>
                <span className="text-gray-400 dark:text-slate-500 font-medium block">Assessment</span>
                <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5">
                  {report.title || "Spring Boot REST API"}
                </span>
              </div>
              <div>
                <span className="text-gray-400 dark:text-slate-500 font-medium block">Workspace</span>
                <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5">
                  {report.workspaceName || "General Drive"}
                </span>
              </div>
              <div>
                <span className="text-gray-400 dark:text-slate-500 font-medium block">Tech Stack</span>
                <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5">
                  {report.techStack || "Java 21, Spring Boot, Maven, PostgreSQL"}
                </span>
              </div>
              <div>
                <span className="text-gray-400 dark:text-slate-500 font-medium block">Time Taken</span>
                <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5 font-mono">
                  {timeTaken} mins
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Score Overview Grid */}
        <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-3xl p-6 shadow-2xs space-y-6 transition-colors">
          <h3 className="font-bold text-gray-900 dark:text-white text-base">Performance Overview</h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 items-center">
            {/* Radial Score Circle */}
            <div className="flex justify-center p-2">
              <ScoreProgressCircle score={rawScore} rating={rating} />
            </div>

            {/* Test Cases Passed */}
            <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 dark:bg-slate-800/60 border border-gray-100 dark:border-slate-700/60 rounded-2xl text-center space-y-2 h-36">
              <div className="w-9 h-9 rounded-full bg-emerald-100/70 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <div className="text-xs font-medium text-gray-500 dark:text-slate-400">Tests Passed</div>
              <div className="text-2xl font-extrabold text-gray-900 dark:text-white font-mono">
                <span className="text-emerald-600 dark:text-emerald-400">{report.passedTests}</span>
                <span className="text-gray-400 dark:text-slate-500 text-lg"> / {report.totalTests}</span>
              </div>
            </div>

            {/* Total Duration */}
            <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 dark:bg-slate-800/60 border border-gray-100 dark:border-slate-700/60 rounded-2xl text-center space-y-2 h-36">
              <div className="w-9 h-9 rounded-full bg-purple-100/70 dark:bg-purple-950/60 text-purple-600 dark:text-purple-400 flex items-center justify-center">
                <Clock className="w-4 h-4" />
              </div>
              <div className="text-xs font-medium text-gray-500 dark:text-slate-400">Total Duration</div>
              <div className="text-2xl font-extrabold text-gray-900 dark:text-white font-mono">
                {timeTaken} <span className="text-sm font-normal text-gray-500 dark:text-slate-400">mins</span>
              </div>
            </div>

            {/* Benchmark Status */}
            <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 dark:bg-slate-800/60 border border-gray-100 dark:border-slate-700/60 rounded-2xl text-center space-y-2 h-36">
              <div className="w-9 h-9 rounded-full bg-primary-light/70 dark:bg-primary/25 text-primary dark:text-primary flex items-center justify-center">
                <Award className="w-4 h-4" />
              </div>
              <div className="text-xs font-medium text-gray-500 dark:text-slate-400">Engineering Benchmark</div>
              <div className="text-sm font-extrabold text-gray-900 dark:text-white">
                {rawScore >= 70 ? (
                  <span className="text-emerald-600 dark:text-emerald-400 font-bold">Benchmark Qualified (≥ 70%)</span>
                ) : (
                  <span className="text-amber-600 dark:text-amber-400 font-bold">Needs Review (&lt; 70%)</span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Domain Category Breakdown */}
        {report.categoryBreakdown && report.categoryBreakdown.length > 0 && (
          <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-3xl p-6 shadow-2xs space-y-4 transition-colors">
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400 flex items-center justify-center font-bold">
                <Layers className="w-4 h-4" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900 dark:text-white text-base">Requirement Category Breakdown</h3>
                <p className="text-xs text-gray-500 dark:text-slate-400">
                  Performance across domain logic, request/response syntax, and persistence flow.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
              {report.categoryBreakdown.map((cat, idx) => (
                <div
                  key={idx}
                  className="p-4 rounded-2xl bg-gray-50/70 dark:bg-slate-800/50 border border-gray-100 dark:border-slate-700/60 space-y-2"
                >
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-bold text-gray-800 dark:text-slate-200">{cat.category}</span>
                    <span className="font-mono font-bold text-primary dark:text-primary">{cat.score}%</span>
                  </div>
                  <div className="w-full bg-gray-200 dark:bg-slate-700 rounded-full h-2 overflow-hidden">
                    <div
                      className="bg-primary h-2 rounded-full transition-all"
                      style={{ width: `${Math.min(100, Math.max(0, cat.score))}%` }}
                    />
                  </div>
                  <div className="text-[11px] text-gray-500 dark:text-slate-400">
                    {cat.passed} of {cat.total} tests passed
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* AI-Generated Evaluation Summary */}
        <AiSummaryCard summary={report.aiSummary || "Evaluation completed successfully via automated test runner."} />

        {/* Strengths & Improvements */}
        <StrengthsImprovements
          strengths={report.strengths || ["Clean REST API controller mappings", "Spring Boot standard architecture"]}
          improvements={report.improvements || ["Edge case HTTP error handling", "Strict request validation"]}
        />

        {/* Session Verification Card */}
        <div className="bg-emerald-50/50 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/40 rounded-3xl p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-emerald-100 dark:bg-emerald-900/50 text-emerald-700 dark:text-emerald-300 flex items-center justify-center">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-extrabold text-sm text-emerald-900 dark:text-emerald-200">
                Verified Proctored Assessment
              </h4>
              <p className="text-xs text-emerald-700 dark:text-emerald-400">
                Session verified against Evidence automated sandbox evaluation engine.
              </p>
            </div>
          </div>

          <Link to="/candidate/dashboard">
            <Button size="sm" className="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs gap-1.5 shadow-2xs">
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Done • Back to Dashboard</span>
            </Button>
          </Link>
        </div>
      </main>
    </div>
  );
};
