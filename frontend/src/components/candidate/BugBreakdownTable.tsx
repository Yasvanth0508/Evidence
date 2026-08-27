import { useState } from "react";
import { BugBreakdownItem, TestCaseAuditItem } from "@/types";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import {
  Layers,
  Code2,
  Bug,
  CheckCircle2,
  XCircle,
  ChevronDown,
  ChevronUp,
  Clock,
} from "lucide-react";

interface BugBreakdownTableProps {
  bugs: BugBreakdownItem[];
  testCases?: TestCaseAuditItem[];
}

export const BugBreakdownTable = ({ bugs, testCases }: BugBreakdownTableProps) => {
  const [viewMode, setViewMode] = useState<"SUMMARY" | "TEST_AUDIT">("SUMMARY");
  const [expandedTestId, setExpandedTestId] = useState<string | null>(null);

  const totalScore = bugs.reduce((sum, b) => sum + b.score, 0);
  const totalMaxScore = bugs.reduce((sum, b) => sum + b.maxScore, 0);

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case "Data Flow":
        return (
          <div className="w-6 h-6 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
            <Layers className="w-3.5 h-3.5" />
          </div>
        );
      case "Syntax":
        return (
          <div className="w-6 h-6 rounded-lg bg-orange-50 text-[#F05323] flex items-center justify-center">
            <Code2 className="w-3.5 h-3.5" />
          </div>
        );
      case "Business Logic":
      default:
        return (
          <div className="w-6 h-6 rounded-lg bg-rose-50 text-rose-600 flex items-center justify-center">
            <Bug className="w-3.5 h-3.5" />
          </div>
        );
    }
  };

  return (
    <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm space-y-4">
      {/* Header with View Toggle */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-2 border-b border-gray-100">
        <div>
          <h3 className="font-bold text-gray-900 text-base">
            Evaluation Results & Test Verification
          </h3>
          <p className="text-xs text-gray-500">
            {viewMode === "SUMMARY"
              ? "Aggregated scoring per bug category"
              : "Granular black-box HTTP test case execution logs"}
          </p>
        </div>

        <div className="flex items-center gap-1.5 bg-gray-100 p-1 rounded-xl">
          <button
            onClick={() => setViewMode("SUMMARY")}
            className={`px-3 py-1 text-xs font-bold rounded-lg transition-all ${
              viewMode === "SUMMARY"
                ? "bg-white text-gray-900 shadow-2xs"
                : "text-gray-600 hover:text-gray-900"
            }`}
          >
            Category Summary
          </button>
          <button
            onClick={() => setViewMode("TEST_AUDIT")}
            className={`px-3 py-1 text-xs font-bold rounded-lg transition-all ${
              viewMode === "TEST_AUDIT"
                ? "bg-white text-[#F05323] shadow-2xs"
                : "text-gray-600 hover:text-gray-900"
            }`}
          >
            Detailed Test Cases Audit ({testCases?.length || 0})
          </button>
        </div>
      </div>

      {viewMode === "SUMMARY" ? (
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-12">#</TableHead>
                <TableHead className="w-[180px]">Category</TableHead>
                <TableHead>Issue Type</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Score</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {bugs.map((bug) => (
                <TableRow key={bug.id} className="hover:bg-gray-50/70">
                  <TableCell className="font-semibold text-gray-500 text-xs">
                    {bug.id}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2 font-medium text-xs text-gray-800">
                      {getCategoryIcon(bug.category)}
                      <span>{bug.category}</span>
                    </div>
                  </TableCell>
                  <TableCell className="text-xs text-gray-700 font-medium">
                    {bug.issueType}
                  </TableCell>
                  <TableCell>
                    {bug.status === "FIXED" ? (
                      <Badge variant="fixed" className="gap-1">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                        <span>Fixed</span>
                      </Badge>
                    ) : (
                      <Badge variant="notFixed" className="gap-1">
                        <XCircle className="w-3.5 h-3.5 text-rose-600" />
                        <span>Not Fixed</span>
                      </Badge>
                    )}
                  </TableCell>
                  <TableCell className="text-right font-mono text-xs font-semibold text-gray-900">
                    {bug.score} <span className="text-gray-400 font-normal">/ {bug.maxScore}</span>
                  </TableCell>
                </TableRow>
              ))}

              {/* Total Row */}
              <TableRow className="border-t-2 border-gray-100 bg-gray-50/40 font-bold">
                <TableCell colSpan={4} className="text-xs uppercase tracking-wider text-gray-700">
                  Total Score
                </TableCell>
                <TableCell className="text-right font-mono text-sm text-emerald-700">
                  {totalScore} <span className="text-gray-400 text-xs font-normal">/ {totalMaxScore}</span>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      ) : (
        /* Detailed Test Cases Audit Table */
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-12">#</TableHead>
                <TableHead className="w-32">Test Type</TableHead>
                <TableHead>HTTP Call & Endpoint</TableHead>
                <TableHead>Expected / Actual</TableHead>
                <TableHead>Latency</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Details</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {(!testCases || testCases.length === 0) ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-xs text-gray-500">
                    No individual test case execution audits available for this assessment yet.
                  </TableCell>
                </TableRow>
              ) : (
                testCases.map((tc) => {
                  const isExpanded = expandedTestId === tc.id;
                  const isPassed = tc.status === "PASSED" || (tc as any).passed === true;
                  return (
                    <>
                      <tr key={tc.id} className="border-b border-gray-100 hover:bg-gray-50/70">
                        <TableCell className="font-mono text-xs font-bold text-gray-500">
                          TC-{tc.testCaseNumber}
                        </TableCell>
                        <TableCell>
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-gray-100 text-gray-700 border border-gray-200">
                            {tc.testType}
                          </span>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-1.5 font-mono text-xs text-gray-900 font-semibold">
                            <span className="px-1.5 py-0.5 rounded bg-blue-100 text-blue-800 text-[10px]">
                              {tc.httpMethod}
                            </span>
                            <span>{tc.endpoint}</span>
                          </div>
                        </TableCell>
                        <TableCell className="font-mono text-xs">
                          <span className="text-gray-600">Exp: <strong>{tc.expectedStatusCode}</strong></span>
                          <span className="text-gray-400 mx-1">|</span>
                          <span className={isPassed ? "text-emerald-700" : "text-rose-700"}>
                            Act: <strong>{tc.actualStatusCode ?? "ERR"}</strong>
                          </span>
                        </TableCell>
                        <TableCell className="text-xs text-gray-500 font-mono">
                          <span className="inline-flex items-center gap-1">
                            <Clock className="w-3 h-3 text-gray-400" />
                            {tc.executionTimeMs}ms
                          </span>
                        </TableCell>
                        <TableCell>
                          {isPassed ? (
                            <Badge variant="fixed" className="gap-1 text-[11px]">
                              <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                              <span>PASSED</span>
                            </Badge>
                          ) : (
                            <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full bg-rose-50 text-rose-700 border border-rose-200">
                              <XCircle className="w-3 h-3 text-rose-600" />
                              <span>FAILED</span>
                            </span>
                          )}
                        </TableCell>
                        <TableCell className="text-right">
                          <button
                            onClick={() => setExpandedTestId(isExpanded ? null : tc.id)}
                            className="p-1 rounded-md hover:bg-gray-100 text-gray-500 transition-colors"
                            title={isExpanded ? "Collapse Details" : "View Test Audit Details"}
                          >
                            {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                          </button>
                        </TableCell>
                      </tr>

                      {/* Expanded Details Panel */}
                      {isExpanded && (
                        <tr key={`${tc.id}-details`} className="bg-slate-900 text-slate-100 border-b border-slate-800">
                          <TableCell colSpan={7} className="p-4 space-y-3 font-mono text-xs">
                            {tc.failureReason && (
                              <div className="p-2.5 rounded-lg bg-rose-950/70 border border-rose-800/80 text-rose-300">
                                <span className="font-bold text-rose-400 block uppercase text-[10px] tracking-wider mb-1">
                                  Failure Reason
                                </span>
                                <div>{tc.failureReason}</div>
                              </div>
                            )}

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                              {tc.expectedResponse && (
                                <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 overflow-x-auto space-y-1">
                                  <span className="text-indigo-400 font-bold uppercase text-[10px] block">
                                    Expected Response Contract
                                  </span>
                                  <pre className="text-slate-300 text-[11px] whitespace-pre-wrap">{tc.expectedResponse}</pre>
                                </div>
                              )}
                              {tc.actualResponse && (
                                <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 overflow-x-auto space-y-1">
                                  <span className={isPassed ? "text-emerald-400 font-bold uppercase text-[10px] block" : "text-rose-400 font-bold uppercase text-[10px] block"}>
                                    Actual Response Received
                                  </span>
                                  <pre className="text-slate-300 text-[11px] whitespace-pre-wrap">{tc.actualResponse}</pre>
                                </div>
                              )}
                            </div>
                          </TableCell>
                        </tr>
                      )}
                    </>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
};
