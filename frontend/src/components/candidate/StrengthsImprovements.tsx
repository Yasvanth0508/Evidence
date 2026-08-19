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
      <h3 className="font-bold text-gray-900 text-base">
        Strengths & Improvements
      </h3>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Strengths Card */}
        <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <ThumbsUp className="w-4 h-4" />
            </div>
            <h4 className="font-bold text-emerald-800 text-sm">Strengths</h4>
          </div>

          <ul className="space-y-2 text-xs text-gray-600">
            {strengths.map((item, index) => (
              <li key={index} className="flex items-start gap-2.5">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 mt-1.5 flex-shrink-0" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* Improvements Card */}
        <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-orange-50 text-[#F05323] flex items-center justify-center">
              <TrendingUp className="w-4 h-4" />
            </div>
            <h4 className="font-bold text-[#F05323] text-sm">Improvements</h4>
          </div>

          <ul className="space-y-2 text-xs text-gray-600">
            {improvements.map((item, index) => (
              <li key={index} className="flex items-start gap-2.5">
                <span className="w-1.5 h-1.5 rounded-full bg-[#F05323] mt-1.5 flex-shrink-0" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};
