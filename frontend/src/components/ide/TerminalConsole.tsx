import { useState } from "react";
import { Trash2, Terminal as TerminalIcon, ChevronDown, ChevronUp } from "lucide-react";
import { cn } from "@/lib/utils";

interface TerminalConsoleProps {
  logs: string[];
  buildStatus: "IDLE" | "BUILDING" | "SUCCESS" | "FAILED";
  onClearLogs?: () => void;
  isCollapsed?: boolean;
  onToggleCollapse?: () => void;
}

export const TerminalConsole = ({
  logs,
  buildStatus,
  onClearLogs,
  isCollapsed = false,
  onToggleCollapse,
}: TerminalConsoleProps) => {
  const [activeTab, setActiveTab] = useState<"TERMINAL" | "BUILD_OUTPUT">(
    "TERMINAL"
  );

  return (
    <div className="h-full flex flex-col bg-[#0F172A] text-gray-200 font-mono text-xs border-t border-gray-800">
      {/* Console Header / Tabs */}
      <div className="h-10 px-4 bg-[#1E293B] border-b border-gray-800 flex items-center justify-between select-none flex-shrink-0">
        <div className="flex items-center space-x-6 text-[11px] font-bold">
          <button
            onClick={() => {
              setActiveTab("TERMINAL");
              if (isCollapsed && onToggleCollapse) onToggleCollapse();
            }}
            className={cn(
              "flex items-center gap-1.5 py-2.5 transition-colors border-b-2",
              activeTab === "TERMINAL"
                ? "text-white border-primary"
                : "text-gray-400 border-transparent hover:text-gray-200"
            )}
          >
            <TerminalIcon className="w-3.5 h-3.5" />
            <span>TERMINAL</span>
          </button>

          <button
            onClick={() => {
              setActiveTab("BUILD_OUTPUT");
              if (isCollapsed && onToggleCollapse) onToggleCollapse();
            }}
            className={cn(
              "flex items-center gap-1.5 py-2.5 transition-colors border-b-2",
              activeTab === "BUILD_OUTPUT"
                ? "text-white border-primary"
                : "text-gray-400 border-transparent hover:text-gray-200"
            )}
          >
            <span>BUILD OUTPUT</span>
            {buildStatus === "FAILED" && (
              <span className="w-2 h-2 rounded-full bg-rose-500" />
            )}
          </button>
        </div>

        <div className="flex items-center gap-3 text-[11px]">
          {/* Status Badge */}
          {buildStatus === "BUILDING" && (
            <span className="text-amber-400 font-semibold animate-pulse">
              ● Building container...
            </span>
          )}
          {buildStatus === "SUCCESS" && (
            <span className="text-emerald-400 font-semibold">
              ✔ Build Succeeded
            </span>
          )}
          {buildStatus === "FAILED" && (
            <span className="text-rose-400 font-semibold">
              ✗ Build failed
            </span>
          )}

          {onClearLogs && (
            <button
              onClick={onClearLogs}
              className="text-gray-400 hover:text-white p-1 transition-colors rounded hover:bg-slate-700/50"
              title="Clear Console"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          )}

          {onToggleCollapse && (
            <button
              onClick={onToggleCollapse}
              className="text-gray-400 hover:text-white p-1 transition-colors rounded hover:bg-slate-700/50"
              title={isCollapsed ? "Expand Terminal" : "Collapse Terminal"}
            >
              {isCollapsed ? (
                <ChevronUp className="w-4 h-4" />
              ) : (
                <ChevronDown className="w-4 h-4" />
              )}
            </button>
          )}
        </div>
      </div>

      {/* Console Output Log Stream (hidden if collapsed) */}
      {!isCollapsed && (
        <div className="flex-1 p-4 overflow-y-auto space-y-1 text-xs leading-relaxed selection:bg-primary selection:text-white">
          {logs.length === 0 ? (
            <div className="text-gray-500 italic">
              Click "Run Build" in the top bar to compile and start your Spring Boot application...
            </div>
          ) : (
            logs.map((line, idx) => {
              const isStep = line.includes(">>> [") || line.includes("Step ");
              const isError =
                line.includes("[ERROR]") ||
                line.includes("Error:") ||
                line.includes("Exception") ||
                line.includes("Build failed") ||
                line.includes("FAILURE") ||
                line.includes("COMPILATION ERROR") ||
                line.startsWith("✗");
              const isSuccess =
                line.includes("BUILD SUCCESS") ||
                line.includes("Started Application") ||
                line.includes("Tomcat started") ||
                line.startsWith("✔");
              const isWarning =
                line.includes("[WARNING]") ||
                line.includes("WARN") ||
                line.includes("[INTEGRITY_WARNING]");
              const isInfo = line.includes("[INFO]");

              return (
                <div
                  key={idx}
                  className={cn(
                    "font-mono whitespace-pre-wrap leading-5",
                    isStep
                      ? "text-purple-400 font-bold"
                      : isError
                      ? "text-rose-400 font-semibold"
                      : isSuccess
                      ? "text-emerald-400 font-semibold"
                      : isWarning
                      ? "text-amber-400"
                      : isInfo
                      ? "text-sky-300"
                      : "text-gray-300"
                  )}
                >
                  {line}
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
};
