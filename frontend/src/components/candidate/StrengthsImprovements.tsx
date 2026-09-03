import { ThumbsUp, TrendingUp } from "lucide-react";

interface StrengthsImprovementsProps {
  strengths: string[];
  improvements: string[];
}

export const StrengthsImprovements = ({
  strengths,
  improvements,
}: StrengthsImprovementsProps) => {
  return (
    <div className="space-y-3">
      <h3 className="font-bold text-gray-900 dark:text-white text-base">
        Strengths & Improvements
      </h3>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Strengths Card */}
        <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-2xl p-6 shadow-sm space-y-4 transition-colors">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <ThumbsUp className="w-4 h-4" />
            </div>
            <h4 className="font-bold text-emerald-800 dark:text-emerald-300 text-sm">Strengths</h4>
          </div>

          <ul className="space-y-2 text-xs text-gray-600 dark:text-slate-300">
            {strengths.map((item, index) => (
              <li key={index} className="flex items-start gap-2.5">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 mt-1.5 flex-shrink-0" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* Improvements Card */}
        <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-2xl p-6 shadow-sm space-y-4 transition-colors">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-primary-light dark:bg-primary/25 text-primary dark:text-primary flex items-center justify-center">
              <TrendingUp className="w-4 h-4" />
            </div>
            <h4 className="font-bold text-primary dark:text-primary text-sm">Improvements</h4>
          </div>

          <ul className="space-y-2 text-xs text-gray-600 dark:text-slate-300">
            {improvements.map((item, index) => (
              <li key={index} className="flex items-start gap-2.5">
                <span className="w-1.5 h-1.5 rounded-full bg-primary mt-1.5 flex-shrink-0" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};
