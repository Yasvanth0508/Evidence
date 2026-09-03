import { useState, useEffect } from "react";
import {
  GitBranch,
  Box,
  Binary,
  Sparkles,
  CheckCircle2,
  Loader2,
  X,
  FileCode,
  Terminal,
  Clock,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { assessmentService } from "@/services/assessmentService";

interface PipelineStageInfo {
  id: string;
  name: string;
  description: string;
  icon: typeof GitBranch;
  status: "PENDING" | "RUNNING" | "COMPLETED" | "FAILED";
  durationSeconds?: number;
}

interface AssessmentProcessingModalProps {
  isOpen: boolean;
  onClose: () => void;
  assessmentTitle: string;
  repositoryUrl: string;
  assessmentId?: string;
  onComplete?: () => void;
}

export const AssessmentProcessingModal = ({
  isOpen,
  onClose,
  assessmentTitle,
  repositoryUrl,
  assessmentId,
  onComplete,
}: AssessmentProcessingModalProps) => {
  const [stages, setStages] = useState<PipelineStageInfo[]>([
    {
      id: "stage-1",
      name: "1. Native Git Clone",
      description: "Cloning repository template and verifying branch integrity",
      icon: GitBranch,
      status: "PENDING",
    },
    {
      id: "stage-2",
      name: "2. Docker & Container Validation",
      description: "Compiling code in isolated Alpine container and testing runtime port",
      icon: Box,
      status: "PENDING",
    },
    {
      id: "stage-3",
      name: "3. AST Codebase Architecture Extraction",
      description: "Parsing Java AST for @RestController, @Service, @Entity, JPA repos and endpoints",
      icon: Binary,
      status: "PENDING",
    },
    {
      id: "stage-4",
      name: "4. Mistral AI Feature Specification Synthesis",
      description: "Analyzing domain semantics and generating architectural coding requirement",
      icon: Sparkles,
      status: "PENDING",
    },
    {
      id: "stage-5",
      name: "5. Black-Box Test Cases Suite Generation",
      description: "Synthesizing automated HTTP evaluation assertions and status verification suite",
      icon: FileCode,
      status: "PENDING",
    },
  ]);

  const [isCompleted, setIsCompleted] = useState(false);
  const [showLogs, setShowLogs] = useState(false);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [logMessages, setLogMessages] = useState<string[]>([]);

  // Timer for elapsed seconds
  useEffect(() => {
    if (!isOpen || isCompleted) return;
    const interval = setInterval(() => {
      setElapsedSeconds((prev) => prev + 1);
    }, 1000);
    return () => clearInterval(interval);
  }, [isOpen, isCompleted]);

  // Real backend polling when assessmentId is available, with graceful animated progression
  useEffect(() => {
    if (!isOpen) {
      setIsCompleted(false);
      setElapsedSeconds(0);
      setLogMessages([]);
      return;
    }

    let isMounted = true;
    let pollInterval: ReturnType<typeof setInterval> | null = null;
    let consecutiveErrors = 0;

    const isUuid = (str?: string) =>
      Boolean(str && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str));

    setLogMessages([
      `[INFO] Initiating asynchronous AI assessment pipeline for: ${assessmentTitle}`,
      `[INFO] Repository source target: ${repositoryUrl || "configured git template"}`,
      `[INFO] Submitting background orchestrator job to Spring Boot backend...`,
    ]);

    // Initial progressive feedback while waiting for first poll response
    const t1 = setTimeout(() => {
      if (!isMounted) return;
      setStages((prev) =>
        prev.map((s, i) => (i === 0 ? { ...s, status: "RUNNING" } : s))
      );
      setLogMessages((prev) => [
        ...prev,
        `[INFO] Phase 1: Native Git clone worker active (git clone ${repositoryUrl || "template"} --depth 1)`,
      ]);
    }, 500);

    const pollBackend = async () => {
      if (!assessmentId || !isUuid(assessmentId)) return false;
      try {
        const res = await assessmentService.getProcessingStatus(assessmentId);
        if (!isMounted || !res) return false;
        consecutiveErrors = 0;

        const status = res.assessmentStatus;
        const isAstDone = res?.repositoryAnalysis?.status === "COMPLETED";
        const isFeatureDone = res?.featureSpecification?.available || res?.featureSpecification?.status === "COMPLETED";
        const isTestCasesDone = (res?.testCases?.generatedCount || 0) > 0 || res?.testCases?.status === "COMPLETED";
        const isAllReady = status === "READY" || status === "SCHEDULED" || status === "IN_PROGRESS" || status === "COMPLETED";
        const isFailed = status === "FAILED";

        if (isFailed) {
          setStages((prev) =>
            prev.map((s) => (s.status === "RUNNING" ? { ...s, status: "FAILED" } : s))
          );
          setLogMessages((prev) => {
            if (!prev.some((m) => m.includes("failure during execution"))) {
              return [
                ...prev,
                `[ERROR] Assessment processing pipeline encountered a failure during execution.`,
              ];
            }
            return prev;
          });
          return true;
        }

        setStages((prev) =>
          prev.map((s, idx) => {
            if (isAllReady) return { ...s, status: "COMPLETED" };
            if (status === "CREATING") {
              if (idx === 0) return { ...s, status: "RUNNING" };
              return { ...s, status: "PENDING" };
            }
            if (status === "ANALYZING") {
              if (idx === 0) return { ...s, status: "COMPLETED" };
              if (idx === 1) return { ...s, status: isAstDone ? "COMPLETED" : "RUNNING" };
              if (idx === 2) return { ...s, status: isAstDone ? "COMPLETED" : "RUNNING" };
              return { ...s, status: "PENDING" };
            }
            if (status === "GENERATING_FEATURE") {
              if (idx <= 2) return { ...s, status: "COMPLETED" };
              if (idx === 3) return { ...s, status: isFeatureDone ? "COMPLETED" : "RUNNING" };
              return { ...s, status: "PENDING" };
            }
            if (status === "GENERATING_TESTS") {
              if (idx <= 3) return { ...s, status: "COMPLETED" };
              if (idx === 4) return { ...s, status: isTestCasesDone ? "COMPLETED" : "RUNNING" };
              return { ...s, status: "PENDING" };
            }
            // Default fallback
            if (idx === 0) return { ...s, status: "COMPLETED" };
            if (idx === 1) return { ...s, status: isAstDone ? "COMPLETED" : "RUNNING" };
            if (idx === 2) return { ...s, status: isAstDone ? "COMPLETED" : "RUNNING" };
            if (idx === 3) return { ...s, status: isFeatureDone ? "COMPLETED" : "PENDING" };
            if (idx === 4) return { ...s, status: isTestCasesDone ? "COMPLETED" : "PENDING" };
            return s;
          })
        );

        if (status === "ANALYZING") {
          setLogMessages((prev) => {
            if (!prev.some((m) => m.includes("Phase 3: AST Codebase Architecture"))) {
              return [
                ...prev,
                `[INFO] Phase 1 & 2 Completed: Cloned and verified build.`,
                `[INFO] Phase 3: AST Codebase Architecture Extraction in progress...`,
              ];
            }
            return prev;
          });
        } else if (status === "GENERATING_FEATURE") {
          setLogMessages((prev) => {
            if (!prev.some((m) => m.includes("Phase 4: Mistral AI Feature"))) {
              return [
                ...prev,
                `[INFO] Phase 3 Completed: Extracted controllers, JPA entities, and endpoints.`,
                `[INFO] Phase 4: Mistral AI Feature Specification Synthesis active...`,
              ];
            }
            return prev;
          });
        } else if (status === "GENERATING_TESTS") {
          setLogMessages((prev) => {
            if (!prev.some((m) => m.includes("Phase 5: Black-Box Test Cases"))) {
              return [
                ...prev,
                `[INFO] Phase 4 Completed: Domain feature specification synthesized.`,
                `[INFO] Phase 5: Black-Box Test Cases Suite Generation in progress...`,
              ];
            }
            return prev;
          });
        }

        if (isAllReady) {
          setStages((prev) => prev.map((s) => ({ ...s, status: "COMPLETED" })));
          setIsCompleted(true);
          setLogMessages((prev) => {
            if (!prev.some((m) => m.includes("status transitioned to READY"))) {
              return [
                ...prev,
                `[SUCCESS] Phase 3: AST codebase extraction completed.`,
                `[SUCCESS] Phase 4: Mistral AI feature specification synthesized.`,
                `[SUCCESS] Phase 5: Automated Black-Box verification suite generated.`,
                `[SUCCESS] Assessment pipeline status transitioned to READY.`,
              ];
            }
            return prev;
          });
          if (onComplete) onComplete();
          return true;
        }
      } catch (err) {
        console.debug("Assessment processing poll:", err);
        consecutiveErrors++;
        if (consecutiveErrors >= 6) {
          setLogMessages((prev) => [
            ...prev,
            `[WARN] Backend processing status polling timed out. Please check backend server console.`,
          ]);
          return true;
        }
      }
      return false;
    };

    // Poll backend every 2s while modal is active and valid UUID is present
    if (assessmentId && isUuid(assessmentId)) {
      pollBackend();
      pollInterval = setInterval(async () => {
        const done = await pollBackend();
        if (done && pollInterval) {
          clearInterval(pollInterval);
        }
      }, 2000);
    }

    return () => {
      isMounted = false;
      clearTimeout(t1);
      if (pollInterval) clearInterval(pollInterval);
    };
  }, [isOpen, assessmentId, onComplete]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white dark:bg-slate-900 rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl border border-gray-100 dark:border-slate-800 space-y-6 animate-in zoom-in-95 duration-200 transition-colors">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-gray-100 dark:border-slate-800">
          <div className="flex items-center gap-3">
            <div
              className={`w-12 h-12 rounded-2xl flex items-center justify-center font-bold transition-colors ${
                isCompleted
                  ? "bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400"
                  : "bg-primary-light dark:bg-primary/25 text-primary dark:text-primary"
              }`}
            >
              {isCompleted ? (
                <CheckCircle2 className="w-6 h-6 animate-in zoom-in-50" />
              ) : (
                <Sparkles className="w-6 h-6 animate-spin" />
              )}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-lg font-extrabold text-gray-900 dark:text-white">
                  {isCompleted
                    ? "Assessment Pipeline Ready!"
                    : "Preparing Assessment Pipeline"}
                </h3>
                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    isCompleted
                      ? "bg-emerald-100 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-300"
                      : "bg-blue-100 dark:bg-blue-950/60 text-blue-800 dark:text-blue-300 animate-pulse"
                  }`}
                >
                  {isCompleted ? "READY" : "PROCESSING"}
                </span>
              </div>
              <p className="text-xs text-gray-500 dark:text-slate-400 font-mono truncate max-w-md">
                {assessmentTitle} • {repositoryUrl}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <div className="flex items-center gap-1 text-xs font-mono text-gray-500 dark:text-slate-400 bg-gray-50 dark:bg-slate-800 px-2.5 py-1 rounded-lg border border-gray-200 dark:border-slate-700">
              <Clock className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
              <span>{elapsedSeconds}s</span>
            </div>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full bg-gray-100 dark:bg-slate-800 hover:bg-gray-200 dark:hover:bg-slate-700 flex items-center justify-center text-gray-500 dark:text-slate-400 transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* 5-Phase Timeline Progress List */}
        <div className="space-y-3">
          {stages.map((stage) => {
            const Icon = stage.icon;
            const isDone = stage.status === "COMPLETED";
            const isRunning = stage.status === "RUNNING";

            return (
              <div
                key={stage.id}
                className={`p-3.5 rounded-2xl border transition-all duration-300 flex items-center justify-between ${
                  isDone
                    ? "bg-emerald-50/40 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-800/60"
                    : isRunning
                    ? "bg-primary-light/50 dark:bg-primary/15 border-primary/50 shadow-xs"
                    : "bg-gray-50/60 dark:bg-slate-800/40 border-gray-200 dark:border-slate-700/60 opacity-60"
                }`}
              >
                <div className="flex items-center gap-3">
                  <div
                    className={`w-9 h-9 rounded-xl flex items-center justify-center ${
                      isDone
                        ? "bg-emerald-100 dark:bg-emerald-900/60 text-emerald-700 dark:text-emerald-300"
                        : isRunning
                        ? "bg-primary text-white animate-pulse"
                        : "bg-gray-200 dark:bg-slate-700 text-gray-400 dark:text-slate-400"
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="text-xs font-extrabold text-gray-900 dark:text-white">
                      {stage.name}
                    </h4>
                    <p className="text-[11px] text-gray-500 dark:text-slate-400">{stage.description}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  {isDone && (
                    <span className="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-700 dark:text-emerald-300 bg-emerald-100 dark:bg-emerald-950/60 px-2.5 py-0.5 rounded-full">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      Done
                    </span>
                  )}
                  {isRunning && (
                    <span className="inline-flex items-center gap-1 text-[11px] font-bold text-primary dark:text-primary bg-primary-light dark:bg-primary/25 px-2.5 py-0.5 rounded-full">
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      Running
                    </span>
                  )}
                  {!isDone && !isRunning && (
                    <span className="text-[11px] font-medium text-gray-400 dark:text-slate-500">
                      Pending
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Live Logs Toggle */}
        <div>
          <button
            onClick={() => setShowLogs(!showLogs)}
            className="text-xs font-semibold text-gray-600 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white inline-flex items-center gap-1.5 cursor-pointer"
          >
            <Terminal className="w-3.5 h-3.5" />
            <span>{showLogs ? "Hide" : "Show"} Pipeline Console Stream</span>
          </button>

          {showLogs && (
            <div className="mt-2 p-3 bg-gray-950 text-emerald-400 font-mono text-[11px] rounded-xl overflow-x-auto max-h-36 border border-gray-800 space-y-1">
              {logMessages.length > 0 ? (
                logMessages.map((msg, i) => <div key={i}>{msg}</div>)
              ) : (
                <>
                  <div>[INFO] git clone {repositoryUrl || "template"} --depth 1</div>
                  <div>[INFO] Docker build context verified.</div>
                  <div>[INFO] AST Parser inspecting classes, repositories, and endpoints...</div>
                  {isCompleted && (
                    <div className="text-white font-bold">[SUCCESS] Assessment status transitioned to READY.</div>
                  )}
                </>
              )}
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-3 border-t border-gray-100 dark:border-slate-800">
          <p className="text-xs text-gray-500 dark:text-slate-400">
            {isCompleted
              ? "All 5 pipeline stages finished. Codebase and tests are ready."
              : "Asynchronous background orchestrator active."}
          </p>

          <div className="flex items-center gap-2">
            <Button
              onClick={onClose}
              className={`text-xs font-bold ${
                isCompleted
                  ? "bg-gray-900 hover:bg-black dark:bg-slate-800 dark:hover:bg-slate-700 text-white"
                  : "bg-primary hover:bg-primary-hover text-white"
              }`}
            >
              {isCompleted ? "Done" : "Keep Running in Background"}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};
