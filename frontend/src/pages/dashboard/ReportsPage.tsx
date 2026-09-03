import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { reportService, ReportItem } from "@/services/reportService";
import { workspaceService, WorkspaceResponse } from "@/services/workspaceService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  FileSpreadsheet,
  Users,
  CheckCircle2,
  TrendingUp,
  Download,
  Filter,
  Search,
  Building2,
  ExternalLink,
  Award,
  BarChart3,
  Calendar,
  Loader2,
} from "lucide-react";

export const ReportsPage = () => {
  const [workspaces, setWorkspaces] = useState<WorkspaceResponse[]>([]);
  const [reports, setReports] = useState<ReportItem[]>([]);
  const [summary, setSummary] = useState({
    totalCandidates: 0,
    completedAssessments: 0,
    scheduledAssessments: 0,
    participationRate: 0,
    passedAssessments: 0,
    passRate: 0,
    averageScore: 0,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [selectedWorkspaceFilter, setSelectedWorkspaceFilter] = useState("ALL");
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [isExporting, setIsExporting] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadData() {
      setIsLoading(true);
      try {
        const [wsList, reportsRes, summaryRes] = await Promise.all([
          workspaceService.getWorkspaces().catch(() => []),
          reportService.getReports({ page: 0, size: 100 }).catch(() => ({ content: [], reports: [] } as any)),
          reportService.getReportSummary().catch(() => null),
        ]);

        if (!isMounted) return;

        if (Array.isArray(wsList)) {
          setWorkspaces(wsList);
        }

        const reportItems: ReportItem[] = reportsRes?.content || reportsRes?.reports || [];
        setReports(reportItems);

        if (summaryRes) {
          setSummary({
            totalCandidates: summaryRes.totalCandidates || reportItems.length,
            completedAssessments: summaryRes.completedAssessments || reportItems.filter((r) => r.status === "COMPLETED").length,
            scheduledAssessments: summaryRes.scheduledAssessments || reportItems.filter((r) => r.status !== "COMPLETED").length,
            participationRate: summaryRes.participationRate || 0,
            passedAssessments: summaryRes.passedAssessments || reportItems.filter((r) => (r.score || 0) >= 70).length,
            passRate: summaryRes.passRate || 0,
            averageScore: Number(summaryRes.averageScore || 0),
          });
        } else {
          // Client-side fallback computation from real API reports
          const total = reportItems.length;
          const completed = reportItems.filter((r) => r.status === "COMPLETED");
          const passed = completed.filter((r) => (r.score || 0) >= 70);
          const avg = completed.length > 0
            ? Number((completed.reduce((acc, r) => acc + (r.score || 0), 0) / completed.length).toFixed(1))
            : 0;

          setSummary({
            totalCandidates: total,
            completedAssessments: completed.length,
            scheduledAssessments: total - completed.length,
            participationRate: total > 0 ? 100 : 0,
            passedAssessments: passed.length,
            passRate: completed.length > 0 ? Math.round((passed.length / completed.length) * 100) : 0,
            averageScore: avg,
          });
        }
      } catch (err) {
        console.error("Failed to load reports data:", err);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    }

    loadData();

    return () => {
      isMounted = false;
    };
  }, []);

  const filteredReports = reports.filter((item) => {
    const matchesWorkspace =
      selectedWorkspaceFilter === "ALL" || item.workspaceId === selectedWorkspaceFilter;
    const matchesStatus =
      statusFilter === "ALL" || item.status === statusFilter;
    const matchesSearch =
      (item.candidateName || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.candidateEmail || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.workspaceName || "").toLowerCase().includes(searchQuery.toLowerCase());

    return matchesWorkspace && matchesStatus && matchesSearch;
  });

  const handleExport = () => {
    setIsExporting(true);
    try {
      const headers = ["Candidate Name", "Candidate Email", "Workspace", "Status", "Score (%)", "Submitted At"];
      const csvRows = [
        headers.join(","),
        ...filteredReports.map((r) =>
          [
            `"${r.candidateName || ""}"`,
            `"${r.candidateEmail || ""}"`,
            `"${r.workspaceName || ""}"`,
            `"${r.status || ""}"`,
            `"${r.score ?? ""}"`,
            `"${r.completedAt || ""}"`,
          ].join(",")
        ),
      ];

      const csvBlob = new Blob([csvRows.join("\n")], { type: "text/csv;charset=utf-8;" });
      const url = URL.createObjectURL(csvBlob);
      const link = document.createElement("a");
      link.setAttribute("href", url);
      link.setAttribute("download", `Evidence_Assessment_Reports_${new Date().toISOString().split("T")[0]}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (e) {
      console.error("CSV Export failed:", e);
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Header & Export Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white dark:bg-slate-900 p-6 rounded-3xl border border-gray-200/90 dark:border-slate-800 shadow-2xs transition-colors">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-primary-light dark:bg-primary/20 text-primary dark:text-primary border border-primary-border/60 dark:border-primary/30 mb-1.5">
            <FileSpreadsheet className="w-3 h-3 text-primary dark:text-primary" />
            HR Assessment Analytics
          </div>
          <h1 className="text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight">
            Assessment Reports
          </h1>
          <p className="text-xs text-gray-500 dark:text-slate-400 font-medium">
            Review live participation, candidate evaluation scores, and workspace benchmarks.
          </p>
        </div>

        <Button
          size="default"
          variant="outline"
          onClick={handleExport}
          disabled={isExporting || filteredReports.length === 0}
          className="font-semibold gap-2 shadow-2xs border-gray-300 dark:border-slate-700 text-gray-700 dark:text-slate-200 hover:bg-gray-50 dark:hover:bg-slate-800"
        >
          <Download className="w-4 h-4 text-gray-500 dark:text-slate-400" />
          {isExporting ? "Exporting..." : "Export CSV Report"}
        </Button>
      </div>

      {/* 2. Key Performance Indicators Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Participation Rate */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 shadow-2xs space-y-2 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">Participation Rate</span>
            <div className="w-9 h-9 rounded-xl bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 flex items-center justify-center">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white">{summary.participationRate}%</span>
            <span className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 flex items-center">
              <TrendingUp className="w-3 h-3 mr-0.5" /> Live
            </span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">
            {summary.completedAssessments + summary.scheduledAssessments} of {summary.totalCandidates} candidates active
          </p>
        </div>

        {/* Completion Rate */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 shadow-2xs space-y-2 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">Completed Assessments</span>
            <div className="w-9 h-9 rounded-xl bg-purple-50 dark:bg-purple-950/40 text-purple-600 dark:text-purple-400 flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white">{summary.completedAssessments}</span>
            <span className="text-xs font-semibold text-purple-600 dark:text-purple-400">
              Evaluated & Scored
            </span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">
            Automated black-box test suites executed
          </p>
        </div>

        {/* Pass Rate */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 shadow-2xs space-y-2 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">Pass Rate (≥ 70%)</span>
            <div className="w-9 h-9 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <Award className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-emerald-600 dark:text-emerald-400">{summary.passRate}%</span>
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">
              ({summary.passedAssessments} passed)
            </span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">
            Benchmark criteria met for engineering bar
          </p>
        </div>

        {/* Average Score */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 shadow-2xs space-y-2 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">Avg. Evaluation Score</span>
            <div className="w-9 h-9 rounded-xl bg-primary-light dark:bg-primary/20 text-primary dark:text-primary flex items-center justify-center">
              <BarChart3 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-gray-900 dark:text-white">{summary.averageScore}</span>
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">/ 100</span>
          </div>
          <p className="text-[11px] text-gray-400 dark:text-slate-500">
            Live Spring Boot test suite performance
          </p>
        </div>
      </div>

      {/* 3. Workspace Overview Cards */}
      {workspaces.length > 0 && (
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 shadow-2xs space-y-4 transition-colors">
          <h3 className="text-sm font-extrabold text-gray-900 dark:text-white uppercase tracking-wider">
            Workspace Filter Overview
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div
              onClick={() => setSelectedWorkspaceFilter("ALL")}
              className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                selectedWorkspaceFilter === "ALL"
                  ? "bg-primary-light/70 dark:bg-primary/25 border-primary dark:border-primary shadow-xs"
                  : "bg-gray-50/60 dark:bg-slate-800/60 border-gray-200 dark:border-slate-700 hover:border-gray-300 dark:hover:border-slate-600"
              }`}
            >
              <div className="flex items-center justify-between mb-2">
                <Building2 className="w-4 h-4 text-gray-500 dark:text-slate-400" />
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-white dark:bg-slate-700 border border-gray-200 dark:border-slate-600 text-gray-700 dark:text-slate-300">
                  All Drives
                </span>
              </div>
              <h4 className="font-bold text-xs text-gray-900 dark:text-white truncate">All Workspaces</h4>
              <p className="text-[11px] text-gray-500 dark:text-slate-400 mt-1">
                {reports.length} total reports
              </p>
            </div>

            {workspaces.map((ws) => {
              const wsReports = reports.filter((r) => r.workspaceId === ws.id);
              return (
                <div
                  key={ws.id}
                  onClick={() => setSelectedWorkspaceFilter(ws.id)}
                  className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                    selectedWorkspaceFilter === ws.id
                      ? "bg-primary-light/70 dark:bg-primary/25 border-primary dark:border-primary shadow-xs"
                      : "bg-gray-50/60 dark:bg-slate-800/60 border-gray-200 dark:border-slate-700 hover:border-gray-300 dark:hover:border-slate-600"
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <Building2 className="w-4 h-4 text-gray-500 dark:text-slate-400" />
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-white dark:bg-slate-700 border border-gray-200 dark:border-slate-600 text-gray-700 dark:text-slate-300">
                      {ws.status}
                    </span>
                  </div>
                  <h4 className="font-bold text-xs text-gray-900 dark:text-white truncate">{ws.name}</h4>
                  <p className="text-[11px] text-gray-500 dark:text-slate-400 mt-1">
                    {wsReports.length} candidate reports
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* 4. Detailed Candidate Assessment Table */}
      <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 shadow-2xs overflow-hidden transition-colors">
        {/* Filters Bar */}
        <div className="p-4 sm:p-5 border-b border-gray-100 dark:border-slate-800 flex flex-col md:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-3 w-full md:max-w-md">
            <div className="relative flex-1">
              <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 dark:text-slate-500" />
              <Input
                type="text"
                placeholder="Search reports by candidate, email, or drive..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9 text-xs h-9 bg-gray-50/70 dark:bg-slate-800 border-gray-200 dark:border-slate-700"
              />
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3 w-full md:w-auto self-end md:self-center">
            {/* Workspace Select Filter */}
            <div className="flex items-center gap-1.5 text-xs text-gray-600 dark:text-slate-300">
              <Filter className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
              <select
                value={selectedWorkspaceFilter}
                onChange={(e) => setSelectedWorkspaceFilter(e.target.value)}
                className="h-9 rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50/70 dark:bg-slate-800 px-3 text-xs text-gray-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="ALL">All Workspaces</option>
                {workspaces.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Status Select Filter */}
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="h-9 rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50/70 dark:bg-slate-800 px-3 text-xs text-gray-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="ALL">All Statuses</option>
              <option value="COMPLETED">Completed / Scored</option>
              <option value="SCHEDULED">Scheduled</option>
              <option value="IN_PROGRESS">In Progress</option>
            </select>
          </div>
        </div>

        {/* Table Content */}
        {isLoading ? (
          <div className="p-12 flex flex-col items-center justify-center gap-2 text-xs text-gray-400 dark:text-slate-500">
            <Loader2 className="w-5 h-5 animate-spin text-primary" />
            Loading assessment reports...
          </div>
        ) : filteredReports.length === 0 ? (
          <div className="p-12 text-center text-xs text-gray-400 dark:text-slate-500">
            No assessment reports found matching your criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50/80 dark:bg-slate-800 text-gray-500 dark:text-slate-400 font-semibold uppercase tracking-wider border-b border-gray-100 dark:border-slate-800">
                <tr>
                  <th className="px-6 py-3.5">Candidate</th>
                  <th className="px-6 py-3.5">Workspace</th>
                  <th className="px-6 py-3.5">Date Submitted</th>
                  <th className="px-6 py-3.5">Score</th>
                  <th className="px-6 py-3.5">Status</th>
                  <th className="px-6 py-3.5 text-right">Technical Report</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-slate-800">
                {filteredReports.map((row) => (
                  <tr key={row.assessmentId} className="hover:bg-primary-light/30 dark:hover:bg-slate-800/50 transition-colors">
                    {/* Candidate */}
                    <td className="px-6 py-4">
                      <span className="font-bold text-gray-900 dark:text-white block">{row.candidateName}</span>
                      <span className="text-[11px] text-gray-500 dark:text-slate-400 font-mono">{row.candidateEmail}</span>
                    </td>

                    {/* Workspace */}
                    <td className="px-6 py-4 font-semibold text-gray-800 dark:text-slate-200">
                      {row.workspaceName}
                    </td>

                    {/* Date Submitted */}
                    <td className="px-6 py-4 text-gray-600 dark:text-slate-400">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
                        <span>{row.completedAt ? new Date(row.completedAt).toLocaleDateString() : "Recent"}</span>
                      </div>
                    </td>

                    {/* Score */}
                    <td className="px-6 py-4">
                      {typeof row.score === "number" && row.status === "COMPLETED" ? (
                        <span
                          className={`font-black text-sm ${
                            row.score >= 70 ? "text-emerald-600 dark:text-emerald-400" : "text-rose-600 dark:text-rose-400"
                          }`}
                        >
                          {row.score}%
                        </span>
                      ) : (
                        <span className="text-gray-400 dark:text-slate-500 font-mono">—</span>
                      )}
                    </td>

                    {/* Status */}
                    <td className="px-6 py-4">
                      {row.status === "COMPLETED" ? (
                        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/50 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                          <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
                          Completed
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-blue-50 dark:bg-blue-950/50 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800">
                          <Calendar className="w-3 h-3 text-blue-600 dark:text-blue-400" />
                          {row.status}
                        </span>
                      )}
                    </td>

                    {/* Action */}
                    <td className="px-6 py-4 text-right">
                      {row.status === "COMPLETED" ? (
                        <Link
                          to={`/dashboard/candidates/${row.assessmentId}`}
                          className="inline-flex items-center gap-1 text-xs font-bold text-primary dark:text-primary hover:underline"
                        >
                          <span>View Report</span>
                          <ExternalLink className="w-3.5 h-3.5" />
                        </Link>
                      ) : (
                        <Link
                          to={`/assessment/${row.assessmentId}`}
                          className="inline-flex items-center gap-1 text-xs font-semibold text-gray-500 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white"
                        >
                          <span>Open Workspace</span>
                        </Link>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
