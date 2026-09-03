import { useState, useEffect } from "react";
import { Loader2, PackageCheck, Server, FlaskConical, Award } from "lucide-react";
import { cn } from "@/lib/utils";

interface SubmissionLoadingOverlayProps {
  isOpen: boolean;
  theme?: "dark" | "light";
}

const EVALUATION_STEPS = [
  {
    id: 1,
    title: "Packaging Source Code",
    desc: "Compiling candidate Java files and building application JAR with Maven...",
    icon: PackageCheck,
  },
  {
    id: 2,
    title: "Booting Sandbox Container",
    desc: "Starting dedicated evaluation container and binding Tomcat network socket...",
    icon: Server,
  },
  {
    id: 3,
    title: "Running Blackbox HTTP Tests",
    desc: "Sending automated HTTP requests and evaluating JSON/status code assertions...",
    icon: FlaskConical,
  },
  {
    id: 4,
    title: "Generating Evaluation Report",
    desc: "Computing category scores, test execution audits, and finalizing report...",
    icon: Award,
  },
];

export const SubmissionLoadingOverlay = ({
  isOpen,
  theme = "dark",
}: SubmissionLoadingOverlayProps) => {
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const isDark = theme === "dark";

  useEffect(() => {
    if (!isOpen) {
      setCurrentStepIndex(0);
      return;
    }

    // Step 1 -> 2 after 3 seconds
    const timer1 = setTimeout(() => setCurrentStepIndex(1), 3000);
    // Step 2 -> 3 after 7 seconds
    const timer2 = setTimeout(() => setCurrentStepIndex(2), 7000);
    // Step 3 -> 4 after 13 seconds
    const timer3 = setTimeout(() => setCurrentStepIndex(3), 13000);

    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
      clearTimeout(timer3);
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 select-none animate-in fade-in duration-300">
      <div
        className={cn(
          "max-w-lg w-full rounded-3xl p-6 sm:p-8 shadow-2xl border space-y-6 animate-in zoom-in-95 duration-200 transition-colors",
          isDark
            ? "bg-slate-900 border-slate-700 text-slate-100"
            : "bg-white border-gray-100 text-gray-900"
        )}
      >
        {/* Top Header & Spinner */}
        <div className="text-center space-y-3">
          <div className="relative inline-block">
            <div className="w-16 h-16 rounded-3xl bg-gradient-to-tr from-primary to-amber-500 p-0.5 shadow-lg shadow-[var(--theme-primary-shadow)] flex items-center justify-center mx-auto">
              <div
                className={cn(
                  "w-full h-full rounded-[22px] flex items-center justify-center",
                  isDark ? "bg-slate-950" : "bg-white"
                )}
              >
                <Loader2 className="w-8 h-8 text-primary animate-spin" />
              </div>
            </div>
          </div>

          <div className="space-y-1">
            <h3 className="text-xl font-extrabold tracking-tight">
              Evaluating Assessment
            </h3>
            <p
              className={cn(
                "text-xs font-medium max-w-sm mx-auto",
                isDark ? "text-slate-400" : "text-gray-500"
              )}
            >
              Please wait while our automated evaluation engine builds your code and executes all blackbox verification tests.
            </p>
          </div>
        </div>

        {/* Step-by-Step Progress Pipeline */}
        <div className="space-y-3 pt-2">
          {EVALUATION_STEPS.map((step, idx) => {
            const Icon = step.icon;
            const isDone = idx < currentStepIndex;
            const isCurrent = idx === currentStepIndex;

            return (
              <div
                key={step.id}
                className={cn(
                  "flex items-start gap-3.5 p-3 rounded-2xl border transition-all duration-300",
                  isCurrent
                    ? isDark
                      ? "bg-slate-800/90 border-primary/60 shadow-md shadow-[var(--theme-primary-shadow)]"
                      : "bg-primary-light/80 border-primary/50 shadow-sm"
                    : isDone
                    ? isDark
                      ? "bg-slate-950/60 border-slate-800/80 opacity-70"
                      : "bg-gray-50/80 border-gray-100 opacity-80"
                    : isDark
                    ? "bg-slate-950/30 border-slate-800/40 opacity-40"
                    : "bg-gray-50/40 border-gray-100/50 opacity-40"
                )}
              >
                <div
                  className={cn(
                    "w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0 mt-0.5 font-bold transition-colors",
                    isCurrent
                      ? "bg-primary text-white shadow-xs"
                      : isDone
                      ? "bg-emerald-500 text-white"
                      : isDark
                      ? "bg-slate-800 text-slate-500"
                      : "bg-gray-200 text-gray-500"
                  )}
                >
                  {isCurrent ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <Icon className="w-4 h-4" />
                  )}
                </div>

                <div className="flex-1 min-w-0 space-y-0.5">
                  <div className="flex items-center justify-between">
                    <h4
                      className={cn(
                        "text-xs font-bold truncate",
                        isCurrent
                          ? isDark
                            ? "text-primary"
                            : "text-primary"
                          : isDone
                          ? "text-emerald-500"
                          : isDark
                          ? "text-slate-400"
                          : "text-gray-600"
                      )}
                    >
                      {step.title}
                    </h4>
                    {isCurrent && (
                      <span className="text-[10px] font-bold text-primary animate-pulse">
                        In Progress...
                      </span>
                    )}
                    {isDone && (
                      <span className="text-[10px] font-bold text-emerald-500">
                        Completed
                      </span>
                    )}
                  </div>
                  <p
                    className={cn(
                      "text-[11px] leading-tight",
                      isDark ? "text-slate-400" : "text-gray-500"
                    )}
                  >
                    {step.desc}
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Informative Footer Note */}
        <div
          className={cn(
            "text-center py-2 px-3 rounded-xl text-[11px] font-medium border",
            isDark
              ? "bg-slate-950/60 border-slate-800 text-slate-400"
              : "bg-gray-50 border-gray-200 text-gray-500"
          )}
        >
          Do not refresh or close this browser tab during test execution.
        </div>
      </div>
    </div>
  );
};
