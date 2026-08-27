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
} from "lucide-react";
import { AstAnalysisDrawer } from "@/components/dashboard/AstAnalysisDrawer";

export const CandidateReport = () => {
  const { id = "asmt-001" } = useParams<{ id: string }>();
  const { data, isLoading, error, refetch } = useCandidateReport(id);
  const [isAstDrawerOpen, setIsAstDrawerOpen] = useState(false);

  if (isLoading) {
    return (
      <div className="h-96 flex flex-col items-center justify-center space-y-3">
        <Loader2 className="w-8 h-8 text-[#F05323] animate-spin" />
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
              className="text-xs font-semibold bg-[#F05323] hover:bg-[#d94417] text-white"
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
      {/* 1. Header with Breadcrumb and Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Link
            to="/dashboard"
            className="text-xs font-semibold text-gray-500 hover:text-gray-900 inline-flex items-center gap-1.5 mb-1 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Candidates</span>
          </Link>
          <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight">
            Candidate Report
          </h1>
          <p className="text-xs text-gray-500">
            Detailed assessment results and performance analysis.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            className="gap-1.5 text-xs font-bold text-[#F05323] border-orange-200 hover:bg-orange-50"
            onClick={() => setIsAstDrawerOpen(true)}
          >
            <Sparkles className="w-3.5 h-3.5 text-[#F05323]" />
            <span>Inspect AST & AI Feature Spec</span>
          </Button>

          <Button
            variant="outline"
            size="sm"
            className="gap-1.5 text-xs font-semibold text-gray-700"
            onClick={() => alert("Share link copied to clipboard!")}
          >
            <Users className="w-3.5 h-3.5" />
            <span>Share with Team</span>
          </Button>
        </div>
      </div>

      {/* 2. Candidate Meta Info Card */}
      <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          {/* Avatar & Candidate Name */}
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-full bg-purple-100/80 border border-purple-200 text-purple-700 flex items-center justify-center font-bold text-lg shadow-sm">
              {data.candidate.avatarInitials}
            </div>
            <div>
              <div className="flex items-center gap-2.5">
                <h2 className="text-lg font-extrabold text-gray-900 leading-tight">
                  {data.candidate.name}
                </h2>
                <Badge variant="completed" dot>
                  {data.candidate.status}
                </Badge>
              </div>
              <p className="text-xs text-gray-500 mt-0.5">
                {data.candidate.email}
              </p>
            </div>
          </div>

          {/* Project Details Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 pt-4 md:pt-0 border-t md:border-t-0 border-gray-100 text-xs">
            <div>
              <span className="text-gray-400 font-medium block">Project</span>
              <span className="font-semibold text-gray-800 block mt-0.5">
                {data.project.name}
              </span>
            </div>
            <div>
              <span className="text-gray-400 font-medium block">Tech Stack</span>
              <span className="font-semibold text-gray-800 block mt-0.5">
                {data.project.techStack}
              </span>
            </div>
            <div>
              <span className="text-gray-400 font-medium block">Date</span>
              <span className="font-semibold text-gray-800 block mt-0.5">
                {data.project.date}
              </span>
            </div>
            <div>
              <span className="text-gray-400 font-medium block">Total Time Taken</span>
              <span className="font-semibold text-gray-800 block mt-0.5 font-mono">
                {data.project.totalTimeTaken}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 3. Score Overview Grid (Radial Circle + 3 Metric Blocks) */}
      <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm">
        <h3 className="font-bold text-gray-900 text-base mb-6">Score Overview</h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 items-center">
          {/* Block 1: Radial Progress Meter */}
          <div className="flex justify-center p-2">
            <ScoreProgressCircle
              score={data.scoreOverview.overallScore}
              rating={data.scoreOverview.scoreRating}
            />
          </div>

          {/* Block 2: Bugs Fixed */}
          <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 border border-gray-100 rounded-2xl text-center space-y-2 h-36">
            <div className="w-9 h-9 rounded-full bg-rose-100/70 text-rose-600 flex items-center justify-center">
              <Bug className="w-4 h-4" />
            </div>
            <div className="text-xs font-medium text-gray-500">Bugs Fixed</div>
            <div className="text-2xl font-extrabold text-gray-900 font-mono">
              <span className="text-rose-600">{data.scoreOverview.bugsFixed.fixed}</span>
              <span className="text-gray-400 text-lg"> / {data.scoreOverview.bugsFixed.total}</span>
            </div>
          </div>

          {/* Block 3: Test Cases Passed */}
          <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 border border-gray-100 rounded-2xl text-center space-y-2 h-36">
            <div className="w-9 h-9 rounded-full bg-emerald-100/70 text-emerald-600 flex items-center justify-center">
              <ShieldCheck className="w-4 h-4" />
            </div>
            <div className="text-xs font-medium text-gray-500">Test Cases Passed</div>
            <div className="text-2xl font-extrabold text-gray-900 font-mono">
              <span className="text-emerald-600">{data.scoreOverview.testCasesPassed.passed}</span>
              <span className="text-gray-400 text-lg"> / {data.scoreOverview.testCasesPassed.total}</span>
            </div>
          </div>

          {/* Block 4: Total Time Taken */}
          <div className="flex flex-col items-center justify-center p-4 bg-gray-50/70 border border-gray-100 rounded-2xl text-center space-y-2 h-36">
            <div className="w-9 h-9 rounded-full bg-purple-100/70 text-purple-600 flex items-center justify-center">
              <Clock className="w-4 h-4" />
            </div>
            <div className="text-xs font-medium text-gray-500">Total Time Taken</div>
            <div className="text-2xl font-extrabold text-gray-900 font-mono">
              {data.scoreOverview.totalTimeTakenMinutes} <span className="text-sm font-normal text-gray-500">mins</span>
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
