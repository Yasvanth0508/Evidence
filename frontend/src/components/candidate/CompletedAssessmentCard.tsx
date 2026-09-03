import { Link } from "react-router-dom";
import { HRAssessment } from "@/store/hrStore";
import { Button } from "@/components/ui/button";
import {
  CheckCircle2,
  Calendar,
  Building2,
  ExternalLink,
} from "lucide-react";

interface CompletedAssessmentCardProps {
  assessment: HRAssessment & { workspaceName?: string };
}

export const CompletedAssessmentCard = ({
  assessment,
}: CompletedAssessmentCardProps) => {
  return (
    <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 sm:p-7 shadow-2xs hover:shadow-md hover:border-emerald-200 dark:hover:border-emerald-500/50 transition-all flex flex-col justify-between space-y-6">
      {/* Top Header */}
      <div className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-50 dark:bg-emerald-950/50 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
            <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Completed
          </span>

          <span className="text-xs font-bold text-gray-500 dark:text-slate-400 uppercase font-mono">
            {assessment.difficulty} Difficulty
          </span>
        </div>

        <div>
          <h3 className="text-xl font-extrabold text-gray-900 dark:text-white tracking-tight leading-tight">
            {assessment.title}
          </h3>
          <div className="flex items-center gap-2 mt-1 text-xs text-gray-500 dark:text-slate-400 font-medium">
            <span className="inline-flex items-center gap-1 text-gray-700 dark:text-slate-300">
              <Building2 className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
              {assessment.workspaceName || "Placement Drive"}
            </span>
            <span>•</span>
            <span>{assessment.category || "Java Spring Boot API"}</span>
          </div>
        </div>
      </div>

      {/* Completed Date & Time Information (Requirement 2) */}
      <div className="p-4 rounded-2xl bg-emerald-50/50 dark:bg-emerald-950/30 border border-emerald-100 dark:border-emerald-900/40 space-y-2">
        <span className="text-[10px] uppercase font-bold text-emerald-800 dark:text-emerald-400 tracking-wider block">
          Completed On
        </span>
        <div className="flex items-center justify-between text-xs">
          <div className="flex items-center gap-1.5 font-bold text-gray-900 dark:text-slate-100">
            <Calendar className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
            <span>{assessment.completedAt || `${assessment.scheduledDate} ${assessment.scheduledTime}`}</span>
          </div>
        </div>
      </div>

      {/* Score Summary & Action */}
      <div className="pt-2 border-t border-gray-100 dark:border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[10px] uppercase font-bold text-gray-400 dark:text-slate-500 block tracking-wider">
            Evaluation Score
          </span>
          <div className="flex items-baseline gap-1.5 mt-0.5">
            <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400">
              {assessment.score ?? 88.5}%
            </span>
            <span className="text-xs text-gray-400 dark:text-slate-500 font-mono">
              ({assessment.passedTests ?? 9}/{assessment.totalTests ?? 10} Tests Passed)
            </span>
          </div>
        </div>

        <Link to={`/candidate/assessments/${assessment.id}/report`}>
          <Button
            size="sm"
            variant="outline"
            className="w-full sm:w-auto font-semibold text-xs gap-1.5 border-gray-300 dark:border-slate-700 text-gray-800 dark:text-slate-200 hover:bg-gray-50 dark:hover:bg-slate-800 shadow-2xs"
          >
            <span>View Performance Report</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </Button>
        </Link>
      </div>
    </div>
  );
};
