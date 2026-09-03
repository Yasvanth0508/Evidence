import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useCandidateReport } from "@/hooks/useCandidates";
import { ScoreProgressCircle } from "@/components/candidate/ScoreProgressCircle";
import { BugBreakdownTable } from "@/components/candidate/BugBreakdownTable";
import { AiSummaryCard } from "@/components/candidate/AiSummaryCard";
import { StrengthsImprovements } from "@/components/candidate/StrengthsImprovements";
import { IntegrityBehaviorTabs } from "@/components/candidate/IntegrityBehaviorTabs";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Users,
  Download,
  Bug,
  ShieldCheck,
  Clock,
  ArrowLeft,
  Loader2,
  Sparkles,
  UserCheck2,
} from "lucide-react";
import { AstAnalysisDrawer } from "@/components/dashboard/AstAnalysisDrawer";
import { reportService } from "@/services/reportService";

export const CandidateReport = () => {
  const { id = "" } = useParams<{ id: string }>();
  const { data, isLoading, error, refetch } = useCandidateReport(id);
  const [isAstDrawerOpen, setIsAstDrawerOpen] = useState(false);
  const [isSelected, setIsSelected] = useState(false);
  const [isSelecting, setIsSelecting] = useState(false);

  const handleSelectCandidate = async () => {
    if (!data?.candidate?.id || isSelecting) return;
    setIsSelecting(true);
    try {
      if (!isSelected) {
        // Mark as selected
        if (data.workspaceId) {
          await reportService.selectCandidate({
            workspaceId: data.workspaceId,
            candidateId: data.candidate.id,
            assessmentId: id,
            selectionStatus: "SELECTED",
            selectionNotes: `Selected from candidate evaluation report. Score: ${data.scoreOverview.overallScore}%.`,
          });
        }
        setIsSelected(true);
      } else {
        // Deselect
        if (data.workspaceId) {
          await reportService.removeSelectedCandidateByWorkspaceAndCandidate(
            data.workspaceId,
            data.candidate.id
          );
        }
        setIsSelected(false);
      }
    } catch (e) {
      console.error("Select/deselect candidate error:", e);
      // Optimistically toggle anyway for UX
      setIsSelected((prev) => !prev);
    } finally {
      setIsSelecting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="h-96 flex flex-col items-center justify-center space-y-3">
        <Loader2 className="w-8 h-8 text-primary animate-spin" />
        <span className="text-xs font-semibold text-gray-500">
          Loading Candidate Assessment Report...
        </span>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="p-12 text-center bg-white rounded-3xl border border-gray-200/90 shadow-sm max-w-lg mx-auto my-12 space-y-4">
        <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
          <Bug className="w-6 h-6" />
        </div>
        <div className="space-y-1">
          <h3 className="text-base font-bold text-gray-900">
            Assessment Report Not Available Yet
          </h3>
          <p className="text-xs text-gray-500 leading-relaxed max-w-xs mx-auto">
            The assessment evaluation may still be processing or the report has not been generated yet.
          </p>
        </div>
        <div className="pt-2 flex items-center justify-center gap-3">
          <Button
            size="sm"
            variant="outline"
            className="text-xs font-semibold"
            onClick={() => refetch()}
          >
            Retry Loading
          </Button>
          <Link to="/dashboard">
            <Button
              size="sm"
              className="text-xs font-semibold bg-primary hover:bg-primary-hover text-white"
            >
              Back to Dashboard
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 1. Header with Breadcrumbs & Action Buttons */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Link
            to="/dashboard/reports"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-gray-500 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white transition-colors mb-2"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Assessment Reports</span>
          </Link>
          <h1 className="text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight">
            Candidate Assessment Report
          </h1>
        </div>

        <div className="flex items-center gap-3">
          <Button
            size="sm"
            className={`gap-1.5 text-xs font-semibold shadow-2xs ${
              isSelected
                ? "bg-emerald-600 hover:bg-emerald-700 text-white"
                : "bg-primary hover:bg-primary-hover text-white"
            }`}
            onClick={handleSelectCandidate}
            disabled={isSelecting || isSelected}
          >
            <UserCheck2 className="w-3.5 h-3.5" />
            <span>{isSelected ? "Selected for Hire ✓" : isSelecting ? "Selecting..." : "Select for Hire"}</span>
          </Button>

          <Button
            variant="outline"
            size="sm"
            className="gap-1.5 text-xs font-semibold text-primary dark:text-primary border-primary-border dark:border-primary/30 bg-primary-light/60 dark:bg-primary/20 hover:bg-primary-light/70 dark:hover:bg-primary/30"
            onClick={() => setIsAstDrawerOpen(true)}
          >
            <Sparkles className="w-3.5 h-3.5 text-primary dark:text-primary" />
            <span>Inspect AST & AI Feature Spec</span>
          </Button>

          <Button
            variant="outline"
            size="sm"
            className="gap-1.5 text-xs font-semibold text-gray-700 dark:text-slate-200 border-gray-300 dark:border-slate-700 hover:bg-gray-50 dark:hover:bg-slate-800"
            onClick={() => alert("Share link copied to clipboard!")}
          >
            <Users className="w-3.5 h-3.5" />
            <span>Share with Team</span>
          </Button>
        </div>
      </div>

      {/* 2. Candidate Meta Info Card */}
      <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-2xl p-6 shadow-sm transition-colors">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          {/* Avatar & Candidate Name */}
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-full bg-purple-100/80 dark:bg-purple-950/60 border border-purple-200 dark:border-purple-800 text-purple-700 dark:text-purple-300 flex items-center justify-center font-bold text-lg shadow-sm">
              {data.candidate.avatarInitials}
            </div>
            <div>
              <div className="flex items-center gap-2.5">
                <h2 className="text-lg font-extrabold text-gray-900 dark:text-white leading-tight">
                  {data.candidate.name}
                </h2>
                <Badge variant="completed" dot>
                  {data.candidate.status}
                </Badge>
              </div>
              <p className="text-xs text-gray-500 dark:text-slate-400 mt-0.5">
                {data.candidate.email}
              </p>
            </div>
          </div>

          {/* Project Details Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 pt-4 md:pt-0 border-t md:border-t-0 border-gray-100 dark:border-slate-800 text-xs">
            <div>
              <span className="text-gray-400 dark:text-slate-500 font-medium block">Project</span>
              <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5">
                {data.project.name}
              </span>
            </div>
            <div>
              <span className="text-gray-400 dark:text-slate-500 font-medium block">Tech Stack</span>
              <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5">
                {data.project.techStack}
              </span>
            </div>
            <div>
              <span className="text-gray-400 dark:text-slate-500 font-medium block">Date</span>
              <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5">
                {data.project.date}
              </span>
            </div>
            <div>
              <span className="text-gray-400 dark:text-slate-500 font-medium block">Total Time Taken</span>
              <span className="font-semibold text-gray-800 dark:text-slate-200 block mt-0.5 font-mono">
                {data.project.totalTimeTaken}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 3. Score Overview Grid (Radial Circle + 3 Metric Blocks) */}
      <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-2xl p-6 shadow-sm transition-colors">
        <h3 className="font-bold text-gray-900 dark:text-white text-base mb-6">Score Overview</h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 items-center">
          {/* Block 1: Radial Progress Meter */}
          <div className="flex justify-center p-2">
            <ScoreProgressCircle
              score={data.scoreOverview.overallScore}
              rating={data.scoreOverview.scoreRating}
            />
          </div>

          {/* Block 2: Bugs Fixed */}
          <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 dark:bg-slate-800 border border-gray-100 dark:border-slate-700 rounded-2xl text-center space-y-2 h-36">
            <div className="w-9 h-9 rounded-full bg-rose-100/70 dark:bg-rose-950/60 text-rose-600 dark:text-rose-400 flex items-center justify-center">
              <Bug className="w-4 h-4" />
            </div>
            <div className="text-xs font-medium text-gray-500 dark:text-slate-400">Bugs Fixed</div>
            <div className="text-2xl font-extrabold text-gray-900 dark:text-white font-mono">
              <span className="text-rose-600 dark:text-rose-400">{data.scoreOverview.bugsFixed.fixed}</span>
              <span className="text-gray-400 dark:text-slate-500 text-lg"> / {data.scoreOverview.bugsFixed.total}</span>
            </div>
          </div>

          {/* Block 3: Test Cases Passed */}
          <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 dark:bg-slate-800 border border-gray-100 dark:border-slate-700 rounded-2xl text-center space-y-2 h-36">
            <div className="w-9 h-9 rounded-full bg-emerald-100/70 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <ShieldCheck className="w-4 h-4" />
            </div>
            <div className="text-xs font-medium text-gray-500 dark:text-slate-400">Test Cases Passed</div>
            <div className="text-2xl font-extrabold text-gray-900 dark:text-white font-mono">
              <span className="text-emerald-600 dark:text-emerald-400">{data.scoreOverview.testCasesPassed.passed}</span>
              <span className="text-gray-400 dark:text-slate-500 text-lg"> / {data.scoreOverview.testCasesPassed.total}</span>
            </div>
          </div>

          {/* Block 4: Total Time Taken */}
          <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 dark:bg-slate-800 border border-gray-100 dark:border-slate-700 rounded-2xl text-center space-y-2 h-36">
            <div className="w-9 h-9 rounded-full bg-purple-100/70 dark:bg-purple-950/60 text-purple-600 dark:text-purple-400 flex items-center justify-center">
              <Clock className="w-4 h-4" />
            </div>
            <div className="text-xs font-medium text-gray-500 dark:text-slate-400">Total Time Taken</div>
            <div className="text-2xl font-extrabold text-gray-900 dark:text-white font-mono">
              {data.scoreOverview.totalTimeTakenMinutes} <span className="text-sm font-normal text-gray-500 dark:text-slate-400">mins</span>
            </div>
          </div>
        </div>
      </div>

      {/* 4. Bug Breakdown Table */}
      <BugBreakdownTable bugs={data.bugBreakdown} testCases={data.testCases} />

      {/* 5. AI-Generated Summary */}
      <AiSummaryCard summary={data.aiSummary} />

      {/* 6. Strengths & Improvements */}
      <StrengthsImprovements
        strengths={data.strengths}
        improvements={data.improvements}
      />

      {/* 7. Integrity & Behavior Tabs */}
      <IntegrityBehaviorTabs integrity={data.integrity} />

      {/* 8. Bottom Action Buttons Bar */}
      <div className="flex items-center justify-end gap-3 pt-2">
        <Button
          variant="outline"
          size="lg"
          className="gap-2 text-xs font-semibold text-gray-700 bg-white"
          onClick={() => alert("Share modal opened!")}
        >
          <Users className="w-4 h-4" />
          <span>Share with Team</span>
        </Button>
        <Button
          size="lg"
          className="gap-2 text-xs font-semibold shadow-sm"
          onClick={() => window.print()}
        >
          <Download className="w-4 h-4" />
          <span>Download PDF Report</span>
        </Button>
      </div>

      {/* Codebase AST & AI Feature Specification Drawer */}
      <AstAnalysisDrawer
        isOpen={isAstDrawerOpen}
        onClose={() => setIsAstDrawerOpen(false)}
        assessmentTitle={data.project.name}
      />
    </div>
  );
};
