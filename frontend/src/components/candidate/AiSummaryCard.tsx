import { Sparkles } from "lucide-react";

interface AiSummaryCardProps {
  summary: string;
}

export const AiSummaryCard = ({ summary }: AiSummaryCardProps) => {
  return (
    <div className="bg-purple-50/40 dark:bg-purple-950/20 border border-purple-200/70 dark:border-purple-900/40 rounded-2xl p-6 shadow-sm space-y-3 transition-colors">
      {/* Header */}
      <div className="flex items-center gap-2 text-purple-700 dark:text-purple-300">
        <div className="w-7 h-7 rounded-lg bg-purple-100 dark:bg-purple-900/60 flex items-center justify-center text-purple-600 dark:text-purple-300">
          <Sparkles className="w-4 h-4" />
        </div>
        <h3 className="font-bold text-sm text-purple-900 dark:text-purple-200 tracking-tight">
          AI-Generated Summary
        </h3>
      </div>

      {/* Summary Content */}
      <p className="text-xs sm:text-sm text-gray-700 dark:text-slate-300 leading-relaxed pl-1">
        {summary}
      </p>
    </div>
  );
};
