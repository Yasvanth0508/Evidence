import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import {
  Copy,
  Cpu,
  CheckCircle2,
  Clock,
  ShieldAlert,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface IntegrityData {
  overallRiskBadge: "LOW" | "MEDIUM" | "HIGH";
  behaviorSummary: {
    copyPasteEvents: number;
    buildRuns: number;
    testRuns: number;
    idleTimeMinutes: number;
  };
  riskAnalysis: string;
}

interface IntegrityBehaviorTabsProps {
  integrity: IntegrityData;
}

const TABS = [
  "Overview",
  "Copy-Paste Events",
  "Build Runs",
  "Test Runs",
  "Idle Time",
];

export const IntegrityBehaviorTabs = ({
  integrity,
}: IntegrityBehaviorTabsProps) => {
  const [activeTab, setActiveTab] = useState("Overview");

  return (
    <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-2xl p-6 shadow-sm space-y-6 transition-colors">
      {/* Title */}
      <h3 className="font-bold text-gray-900 dark:text-white text-base">
        Integrity & Behavior
      </h3>

      {/* Tabs List */}
      <div className="flex border-b border-gray-100 dark:border-slate-800 space-x-6 text-xs font-semibold overflow-x-auto">
        {TABS.map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={cn(
              "pb-3 relative transition-colors whitespace-nowrap cursor-pointer",
              activeTab === tab
                ? "text-primary dark:text-primary font-bold border-b-2 border-primary dark:border-primary"
                : "text-gray-500 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white"
            )}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Tab 1: Overview */}
      {activeTab === "Overview" && (
        <div className="space-y-6">
          {/* Overall Risk Badge */}
          <div className="flex items-center gap-3">
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">
              Overall Risk Badge
            </span>
            <Badge variant="riskMedium">
              {integrity.overallRiskBadge}
            </Badge>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-12 gap-8 items-start">
            {/* Behavior Summary Column */}
            <div className="md:col-span-5 space-y-3">
              <span className="text-xs font-bold text-gray-700 dark:text-slate-300 block">
                Behavior Summary
              </span>

              <div className="space-y-2.5 text-xs text-gray-600 dark:text-slate-300">
                <div className="flex items-center justify-between py-1 border-b border-gray-50 dark:border-slate-800">
                  <span className="flex items-center gap-2">
                    <Copy className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
                    Copy-Paste Events
                  </span>
                  <span className="font-mono font-bold text-gray-900 dark:text-white">
                    {integrity.behaviorSummary.copyPasteEvents}
                  </span>
                </div>

                <div className="flex items-center justify-between py-1 border-b border-gray-50 dark:border-slate-800">
                  <span className="flex items-center gap-2">
                    <Cpu className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
                    Build Runs
                  </span>
                  <span className="font-mono font-bold text-gray-900 dark:text-white">
                    {integrity.behaviorSummary.buildRuns}
                  </span>
                </div>

                <div className="flex items-center justify-between py-1 border-b border-gray-50 dark:border-slate-800">
                  <span className="flex items-center gap-2">
                    <CheckCircle2 className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
                    Test Runs
                  </span>
                  <span className="font-mono font-bold text-gray-900 dark:text-white">
                    {integrity.behaviorSummary.testRuns}
                  </span>
                </div>

                <div className="flex items-center justify-between py-1 border-b border-gray-50 dark:border-slate-800">
                  <span className="flex items-center gap-2">
                    <Clock className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
                    Idle Time
                  </span>
                  <span className="font-mono font-bold text-gray-900 dark:text-white">
                    {integrity.behaviorSummary.idleTimeMinutes} mins
                  </span>
                </div>
              </div>
            </div>

            {/* Risk Analysis Card */}
            <div className="md:col-span-7 bg-amber-50/60 dark:bg-amber-950/30 border border-amber-200/70 dark:border-amber-800/50 rounded-2xl p-5 space-y-2">
              <div className="flex items-center gap-2 text-amber-900 dark:text-amber-300">
                <ShieldAlert className="w-4 h-4 text-amber-600 dark:text-amber-400" />
                <h4 className="font-bold text-xs">Risk Analysis</h4>
              </div>
              <p className="text-xs text-amber-900/90 dark:text-amber-200/90 leading-relaxed">
                {integrity.riskAnalysis}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Other Tabs Placeholder */}
      {activeTab !== "Overview" && (
        <div className="py-8 text-center text-xs text-gray-400 dark:text-slate-500 space-y-1">
          <p className="font-medium text-gray-600 dark:text-slate-300">
            Telemetry logs recorded for {activeTab}
          </p>
          <p>All recorded telemetry events verified within normal operational ranges.</p>
        </div>
      )}
    </div>
  );
};
