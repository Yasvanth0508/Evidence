import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/ui/logo";
import {
  Play,
  Square,
  Sparkles,
  Send,
  Loader2,
  Clock,
  LogOut,
  Sun,
  Moon,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface IdeHeaderProps {
  projectName: string;
  initialDurationMinutes?: number;
  isRunningBuild: boolean;
  isApplicationRunning?: boolean;
  theme?: "dark" | "light";
  onToggleTheme?: () => void;
  onRunBuild: () => void;
  onStopApplication?: () => void;
  onSubmitAssessment: () => void;
  onToggleFeatureSpec: () => void;
}

export const IdeHeader = ({
  projectName,
  initialDurationMinutes = 90,
  isRunningBuild,
  isApplicationRunning = false,
  theme = "dark",
  onToggleTheme,
  onRunBuild,
  onStopApplication,
  onSubmitAssessment,
  onToggleFeatureSpec,
}: IdeHeaderProps) => {
  const [secondsRemaining, setSecondsRemaining] = useState(
    initialDurationMinutes * 60
  );

  const isDark = theme === "dark";

  useEffect(() => {
    const interval = setInterval(() => {
      setSecondsRemaining((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const formatTimer = (totalSeconds: number) => {
    const hrs = Math.floor(totalSeconds / 3600);
    const mins = Math.floor((totalSeconds % 3600) / 60);
    const secs = totalSeconds % 60;
    return `${String(hrs).padStart(2, "0")}:${String(mins).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
  };

  return (
    <header
      className={cn(
        "h-14 px-4 flex items-center justify-between z-30 select-none border-b transition-colors",
        isDark
          ? "bg-[#0F172A] border-slate-800 text-slate-100"
          : "bg-white border-gray-200 text-gray-900"
      )}
    >
      {/* Left: Window Controls & Project Name */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-1.5">
          <span className="w-3 h-3 rounded-full bg-rose-400"></span>
          <span className="w-3 h-3 rounded-full bg-amber-400"></span>
          <span className="w-3 h-3 rounded-full bg-emerald-400"></span>
        </div>

        <div className={cn("h-4 w-px mx-1", isDark ? "bg-slate-700" : "bg-gray-200")} />

        <Logo size="sm" showSubtitle={false} to="/dashboard" />

        <div className={cn("h-4 w-px mx-1", isDark ? "bg-slate-700" : "bg-gray-200")} />

        <span
          className={cn(
            "text-xs font-bold flex items-center gap-1 truncate max-w-[260px] md:max-w-md",
            isDark ? "text-slate-200" : "text-gray-800"
          )}
        >
          {projectName}
        </span>
      </div>

      {/* Center: Live Countdown Timer */}
      <div
        className={cn(
          "flex items-center gap-2 border px-3 py-1 rounded-xl transition-colors",
          isDark
            ? "bg-slate-800/90 border-slate-700"
            : "bg-gray-100/80 border-gray-200"
        )}
      >
        <Clock className={cn("w-3.5 h-3.5", isDark ? "text-slate-400" : "text-gray-500")} />
        <span
          className={cn(
            "text-xs font-mono font-bold",
            isDark ? "text-slate-100" : "text-gray-800"
          )}
        >
          {formatTimer(secondsRemaining)}
        </span>
        <span className={cn("text-[10px] font-medium", isDark ? "text-slate-400" : "text-gray-400")}>
          Time Left
        </span>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-2">
        {/* Theme Toggle Button */}
        {onToggleTheme && (
          <button
            type="button"
            onClick={onToggleTheme}
            className={cn(
              "w-8 h-8 rounded-lg border flex items-center justify-center transition-colors shadow-2xs",
              isDark
                ? "bg-slate-800 border-slate-700 text-amber-300 hover:bg-slate-700 hover:text-amber-200"
                : "bg-white border-gray-200 text-slate-700 hover:bg-gray-100 hover:text-slate-900"
            )}
            title={`Switch to ${isDark ? "Light" : "Dark"} Mode`}
          >
            {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
          </button>
        )}

        {/* Feature Spec Toggle Button */}
        <Button
          variant="outline"
          size="sm"
          onClick={onToggleFeatureSpec}
          className={cn(
            "h-8 gap-1.5 text-xs font-semibold transition-colors",
            isDark
              ? "text-purple-300 bg-purple-950/60 border-purple-800 hover:bg-purple-900/60 hover:text-purple-200"
              : "text-purple-700 bg-purple-50/70 border-purple-200 hover:bg-purple-100"
          )}
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>Task Spec</span>
        </Button>

        {/* Run / Stop Button */}
        {isApplicationRunning ? (
          <Button
            size="sm"
            onClick={onStopApplication}
            className="h-8 gap-1.5 text-xs font-semibold shadow-xs bg-rose-600 hover:bg-rose-700 text-white"
          >
            <Square className="w-3.5 h-3.5 fill-white" />
            <span>Stop Application</span>
          </Button>
        ) : (
          <Button
            size="sm"
            disabled={isRunningBuild}
            onClick={onRunBuild}
            className="h-8 gap-1.5 text-xs font-semibold shadow-xs"
          >
            {isRunningBuild ? (
              <>
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
                <span>Building...</span>
              </>
            ) : (
              <>
                <Play className="w-3.5 h-3.5 fill-white" />
                <span>Run Build</span>
              </>
            )}
          </Button>
        )}

        {/* Submit Assessment Button */}
        <Button
          variant="outline"
          size="sm"
          onClick={onSubmitAssessment}
          className={cn(
            "h-8 gap-1.5 text-xs font-semibold transition-colors",
            isDark
              ? "text-emerald-300 bg-emerald-950/60 border-emerald-800 hover:bg-emerald-900/60 hover:text-emerald-200"
              : "text-emerald-700 bg-emerald-50/60 border-emerald-200 hover:bg-emerald-100"
          )}
        >
          <Send className="w-3.5 h-3.5" />
          <span>Submit</span>
        </Button>

        <Link
          to="/dashboard"
          className={cn(
            "w-8 h-8 rounded-lg border flex items-center justify-center transition-colors ml-1",
            isDark
              ? "border-slate-700 text-slate-400 hover:text-white hover:bg-slate-800"
              : "border-gray-200 text-gray-400 hover:text-gray-700 hover:bg-gray-50"
          )}
          title="Exit to Dashboard"
        >
          <LogOut className="w-3.5 h-3.5" />
        </Link>
      </div>
    </header>
  );
};
