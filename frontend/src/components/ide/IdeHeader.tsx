import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/ui/logo";
import {
  Play,
  Sparkles,
  Send,
  Loader2,
  Clock,
  LogOut,
} from "lucide-react";

interface IdeHeaderProps {
  projectName: string;
  initialDurationMinutes?: number;
  isRunningBuild: boolean;
  onRunBuild: () => void;
  onSubmitAssessment: () => void;
  onToggleFeatureSpec: () => void;
}

export const IdeHeader = ({
  projectName,
  initialDurationMinutes = 90,
  isRunningBuild,
  onRunBuild,
  onSubmitAssessment,
  onToggleFeatureSpec,
}: IdeHeaderProps) => {
  const [secondsRemaining, setSecondsRemaining] = useState(
    initialDurationMinutes * 60
  );

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
    <header className="h-14 bg-white border-b border-gray-200 px-4 flex items-center justify-between z-30 select-none">
      {/* Left: Window Controls & Project Name */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-1.5">
          <span className="w-3 h-3 rounded-full bg-rose-400"></span>
          <span className="w-3 h-3 rounded-full bg-amber-400"></span>
          <span className="w-3 h-3 rounded-full bg-emerald-400"></span>
        </div>

        <div className="h-4 w-px bg-gray-200 mx-1"></div>

        <Logo size="sm" showSubtitle={false} to="/dashboard" />

        <div className="h-4 w-px bg-gray-200 mx-1"></div>

        <span className="text-xs font-bold text-gray-800 flex items-center gap-1">
          {projectName}
        </span>
      </div>

      {/* Center: Live Countdown Timer */}
      <div className="flex items-center gap-2 bg-gray-100/80 border border-gray-200 px-3 py-1 rounded-xl">
        <Clock className="w-3.5 h-3.5 text-gray-500" />
        <span className="text-xs font-mono font-bold text-gray-800">
          {formatTimer(secondsRemaining)}
        </span>
        <span className="text-[10px] text-gray-400 font-medium">Time Left</span>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-2.5">
        {/* Feature Spec Toggle Button */}
        <Button
          variant="outline"
          size="sm"
          onClick={onToggleFeatureSpec}
          className="h-8 gap-1.5 text-xs font-semibold text-purple-700 bg-purple-50/70 border-purple-200 hover:bg-purple-100"
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>Task Spec</span>
        </Button>

        {/* Run Build Button */}
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

        {/* Submit Assessment Button */}
        <Button
          variant="outline"
          size="sm"
          onClick={onSubmitAssessment}
          className="h-8 gap-1.5 text-xs font-semibold text-emerald-700 bg-emerald-50/60 border-emerald-200 hover:bg-emerald-100"
        >
          <Send className="w-3.5 h-3.5" />
          <span>Submit</span>
        </Button>

        <Link
          to="/dashboard"
          className="w-8 h-8 rounded-lg border border-gray-200 text-gray-400 hover:text-gray-700 hover:bg-gray-50 flex items-center justify-center transition-colors ml-1"
          title="Exit to Dashboard"
        >
          <LogOut className="w-3.5 h-3.5" />
        </Link>
      </div>
    </header>
  );
};
