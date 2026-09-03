import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { assessmentService } from "@/services/assessmentService";
import {
  Code2,
  Clock,
  AlertTriangle,
  X,
  Play,
  Terminal,
  ShieldCheck,
  Save,
  Loader2,
} from "lucide-react";
import { Button } from "@/components/ui/button";

interface PreAssessmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  assessment: {
    id: string;
    title: string;
    workspaceName: string;
    difficulty: string;
    durationMinutes: number;
    scheduledStartAt: string;
    scheduledEndAt: string;
    techStack?: string;
  };
}

export const PreAssessmentModal = ({
  isOpen,
  onClose,
  assessment,
}: PreAssessmentModalProps) => {
  const navigate = useNavigate();
  const [isInitializing, setIsInitializing] = useState(false);
  const [confirmedRules, setConfirmedRules] = useState(false);

  if (!isOpen) return null;

  const handleStart = async () => {
    setIsInitializing(true);
    try {
      await assessmentService.startAssessment(assessment.id);
    } catch (err) {
      console.warn("Start assessment offline or already initialized:", err);
    } finally {
      setIsInitializing(false);
      navigate(`/assessment/${assessment.id}`);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl max-w-xl w-full p-6 sm:p-8 shadow-2xl border border-gray-100 space-y-6 animate-in zoom-in-95 duration-200 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-primary-light text-primary flex items-center justify-center font-bold">
              <Code2 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-lg font-extrabold text-gray-900">
                Start Technical Assessment
              </h3>
              <p className="text-xs text-gray-500">
                {assessment.workspaceName}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-gray-500 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Assessment Card Summary */}
        <div className="p-4 bg-gray-50/80 rounded-2xl border border-gray-200 space-y-3">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-extrabold text-gray-900">
              {assessment.title}
            </h4>
            <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-primary-light text-primary border border-primary-border">
              {assessment.difficulty}
            </span>
          </div>

          <div className="grid grid-cols-2 gap-3 text-xs text-gray-600">
            <div className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-gray-400" />
              <span>Duration: <strong>{assessment.durationMinutes} Minutes</strong></span>
            </div>
            <div className="flex items-center gap-2">
              <Terminal className="w-4 h-4 text-gray-400" />
              <span>Stack: <strong>{assessment.techStack || "Java Spring Boot"}</strong></span>
            </div>
          </div>
        </div>

        {/* Instructions & System Environment Rules */}
        <div className="space-y-3">
          <h4 className="text-xs font-bold text-gray-900 uppercase tracking-wider">
            Important Candidate Instructions
          </h4>

          <ul className="space-y-2.5 text-xs text-gray-600">
            <li className="flex items-start gap-2.5">
              <Save className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
              <span>
                <strong>Debounced Autosave (2.5s):</strong> Your code is automatically saved to your isolated workspace on disk as you type.
              </span>
            </li>
            <li className="flex items-start gap-2.5">
              <Terminal className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
              <span>
                <strong>Interactive Run:</strong> Click the "Run" button anytime to recompile and stream live stdout/stderr console logs to verify your implementation.
              </span>
            </li>
            <li className="flex items-start gap-2.5">
              <ShieldCheck className="w-4 h-4 text-purple-600 shrink-0 mt-0.5" />
              <span>
                <strong>Automated Black-Box Evaluation:</strong> When you submit, an automated test suite will execute HTTP requests against your endpoints to compute your score.
              </span>
            </li>
            <li className="flex items-start gap-2.5">
              <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
              <span>
                <strong>Workspace Isolation:</strong> You are provided a clean copy of the project repository. External dependencies outside Maven/Gradle are not permitted.
              </span>
            </li>
          </ul>
        </div>

        {/* Confirmation Checkbox */}
        <div className="p-3 bg-amber-50/70 border border-amber-200 rounded-xl">
          <label className="flex items-center gap-2.5 text-xs text-amber-900 font-semibold cursor-pointer">
            <input
              type="checkbox"
              checked={confirmedRules}
              onChange={(e) => setConfirmedRules(e.target.checked)}
              className="w-4 h-4 rounded text-primary focus:ring-primary border-amber-300"
            />
            <span>I have read the instructions and am ready to start my timed session.</span>
          </label>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-between pt-3 border-t border-gray-100">
          <Button
            variant="outline"
            size="sm"
            onClick={onClose}
            className="text-xs font-semibold text-gray-600"
          >
            Cancel
          </Button>

          <Button
            size="sm"
            disabled={!confirmedRules || isInitializing}
            onClick={handleStart}
            className="bg-primary hover:bg-primary-hover text-white text-xs font-bold gap-2 px-5 py-2.5 shadow-md shadow-[var(--theme-primary-shadow)]"
          >
            {isInitializing ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Initializing Workspace...
              </>
            ) : (
              <>
                <Play className="w-4 h-4 fill-white" />
                Enter Assessment IDE
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};
