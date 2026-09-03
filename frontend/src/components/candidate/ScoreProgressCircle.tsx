import { cn } from "@/lib/utils";

interface ScoreProgressCircleProps {
  score: number; // 0 to 100
  rating?: string;
  size?: number;
  strokeWidth?: number;
  className?: string;
}

export const ScoreProgressCircle = ({
  score,
  rating = "Good Performance",
  size = 140,
  strokeWidth = 10,
  className,
}: ScoreProgressCircleProps) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  // Arc calculation for smooth partial circle (e.g. 260 deg arc or full circle)
  const strokeDashoffset = circumference - (score / 100) * circumference;

  const getScoreColor = (val: number) => {
    if (val >= 80) return "#10B981"; // Emerald Green
    if (val >= 60) return "#F59E0B"; // Amber
    return "#EF4444"; // Red
  };

  const scoreColor = getScoreColor(score);

  return (
    <div className={cn("flex flex-col items-center justify-center", className)}>
      <div className="relative" style={{ width: size, height: size }}>
        <svg width={size} height={size} className="transform -rotate-90">
          {/* Background Track Circle */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="currentColor"
            className="text-gray-200 dark:text-slate-700"
            strokeWidth={strokeWidth}
            fill="transparent"
          />
          {/* Animated Progress Circle */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke={scoreColor}
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            className="transition-all duration-1000 ease-out"
          />
        </svg>

        {/* Center Text */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span className="text-[10px] font-semibold uppercase tracking-wider text-gray-400 dark:text-slate-500">
            Overall Score
          </span>
          <div className="flex items-baseline gap-0.5">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white leading-none">
              {score}
            </span>
            <span className="text-xs text-gray-400 dark:text-slate-500 font-semibold">/100</span>
          </div>
          <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400 mt-1">
            {rating}
          </span>
        </div>
      </div>
    </div>
  );
};
