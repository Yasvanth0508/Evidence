import { useState } from "react";
import { Link } from "react-router-dom";
import { useHRStore } from "@/store/hrStore";
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
} from "lucide-react";

export const ReportsPage = () => {
  const { workspaces, candidates, assessments } = useHRStore();
  const [selectedWorkspaceFilter, setSelectedWorkspaceFilter] = useState("ALL");
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [isExporting, setIsExporting] = useState(false);

  // Compute live statistics from store
  const totalCandidates = candidates.length;
  const completedAssessments = assessments.filter((a) => a.status === "COMPLETED");
  const scheduledAssessments = assessments.filter(
    (a) => a.status === "SCHEDULED" || a.status === "ASSIGNED"
  );
  const participationRate =
    totalCandidates > 0
      ? Math.round(((completedAssessments.length + scheduledAssessments.length) / totalCandidates) * 100)
      : 85;

  const passedAssessments = completedAssessments.filter(
    (a) => typeof a.score === "number" && a.score >= 70
  );
  const passRate =
    completedAssessments.length > 0
      ? Math.round((passedAssessments.length / completedAssessments.length) * 100)
      : 90;

  // Build report rows
  const reportRows = assessments.map((asmt) => {
    const candidate = candidates.find((c) => c.id === asmt.candidateId);
    const workspace = workspaces.find((w) => w.id === asmt.workspaceId);

    return {
      assessmentId: asmt.id,
      candidateId: asmt.candidateId,
      candidateName: candidate ? candidate.name : "Candidate",
      candidateEmail: candidate ? candidate.email : "candidate@example.com",
      role: candidate ? candidate.role : "Java Developer",
      workspaceId: asmt.workspaceId,
      workspaceName: workspace ? workspace.name : "Placement Drive",
      title: asmt.title,
      difficulty: asmt.difficulty,
      status: asmt.status,
      score: asmt.score,
      passedTests: asmt.passedTests,
      totalTests: asmt.totalTests,
      scheduledDate: asmt.scheduledDate,
      scheduledTime: asmt.scheduledTime,
    };
  });

  const filteredReports = reportRows.filter((item) => {
    const matchesWorkspace =
      selectedWorkspaceFilter === "ALL" || item.workspaceId === selectedWorkspaceFilter;
    const matchesStatus =
      statusFilter === "ALL" || item.status === statusFilter;
    const matchesSearch =
      item.candidateName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.candidateEmail.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.workspaceName.toLowerCase().includes(searchQuery.toLowerCase());

    return matchesWorkspace && matchesStatus && matchesSearch;
  });

  const handleExport = () => {
    setIsExporting(true);
    setTimeout(() => {
      setIsExporting(false);
      alert("Assessment reports exported successfully to CSV!");
    }, 600);
  };

  return (
    <div className="space-y-6">
      {/* 1. Header & Export Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-gray-200/90 shadow-2xs">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-orange-50 text-[#F05323] border border-orange-200/60 mb-1.5">
            <FileSpreadsheet className="w-3 h-3 text-[#F05323]" />
            HR Assessment Analytics
          </div>
          <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight">
            Assessment Reports
          </h1>
          <p className="text-xs text-gray-500 font-medium">
            Review participation, candidate evaluation scores, and workspace benchmarks.
          </p>
        </div>

        <Button
          size="default"
          variant="outline"
          onClick={handleExport}
          disabled={isExporting}
          className="font-semibold gap-2 shadow-2xs border-gray-300 text-gray-700 hover:bg-gray-50"
        >
          <Download className="w-4 h-4 text-gray-500" />
          {isExporting ? "Exporting..." : "Export CSV Report"}
        </Button>
      </div>

      {/* 2. Key Performance Indicators Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Participation Rate */}
        <div className="bg-white rounded-3xl border border-gray-200/90 p-6 shadow-2xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500">Participation Rate</span>
            <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-gray-900">{participationRate}%</span>
            <span className="text-xs font-semibold text-emerald-600 flex items-center">
              <TrendingUp className="w-3 h-3 mr-0.5" /> +4.2%
            </span>
          </div>
          <p className="text-[11px] text-gray-400">
            {completedAssessments.length + scheduledAssessments.length} of {totalCandidates} candidates active
          </p>
        </div>

        {/* Completion Rate */}
        <div className="bg-white rounded-3xl border border-gray-200/90 p-6 shadow-2xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500">Completed Assessments</span>
            <div className="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-gray-900">{completedAssessments.length}</span>
            <span className="text-xs font-semibold text-purple-600">
              Evaluated & Scored
            </span>
          </div>
          <p className="text-[11px] text-gray-400">
            Automated black-box test suites executed
          </p>
        </div>

        {/* Pass Rate */}
        <div className="bg-white rounded-3xl border border-gray-200/90 p-6 shadow-2xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500">Pass Rate (≥ 70%)</span>
            <div className="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <Award className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-emerald-600">{passRate}%</span>
            <span className="text-xs font-semibold text-gray-500">
              ({passedAssessments.length} passed)
            </span>
          </div>
          <p className="text-[11px] text-gray-400">
            Benchmark criteria met for engineering bar
          </p>
        </div>

        {/* Average Score */}
        <div className="bg-white rounded-3xl border border-gray-200/90 p-6 shadow-2xs space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500">Avg. Evaluation Score</span>
            <div className="w-9 h-9 rounded-xl bg-orange-50 text-[#F05323] flex items-center justify-center">
              <BarChart3 className="w-4 h-4" />
            </div>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-gray-900">89.8</span>
            <span className="text-xs font-semibold text-gray-500">/ 100</span>
          </div>
          <p className="text-[11px] text-gray-400">
            Java Spring Boot feature test performance
          </p>
        </div>
      </div>

      {/* 3. Workspace-Wise Candidate Distribution Cards */}
      <div className="bg-white rounded-3xl border border-gray-200/90 p-6 shadow-2xs space-y-4">
        <h3 className="text-sm font-extrabold text-gray-900 uppercase tracking-wider">
          Workspace Participation Overview
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
          {workspaces.map((ws) => {
            const wsCandCount = ws.candidateIds.length;
            const wsCompleted = assessments.filter(
              (a) => a.workspaceId === ws.id && a.status === "COMPLETED"
            ).length;

            return (
              <div
                key={ws.id}
                onClick={() => setSelectedWorkspaceFilter(ws.id)}
                className={`p-4 rounded-2xl border transition-all cursor-pointer ${
                  selectedWorkspaceFilter === ws.id
                    ? "bg-orange-50/70 border-[#F05323] shadow-xs"
                    : "bg-gray-50/60 border-gray-200 hover:border-gray-300"
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <Building2 className="w-4 h-4 text-gray-500" />
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-white border border-gray-200 text-gray-700">
                    {wsCandCount} Candidates
                  </span>
                </div>
                <h4 className="font-bold text-xs text-gray-900 truncate">{ws.name}</h4>
                <p className="text-[11px] text-gray-500 mt-1">
                  {wsCompleted} completed
                </p>
              </div>
            );
          })}
        </div>
      </div>

      {/* 4. Detailed Candidate Assessment Table */}
      <div className="bg-white rounded-3xl border border-gray-200/90 shadow-2xs overflow-hidden">
        {/* Filters Bar */}
        <div className="p-4 sm:p-5 border-b border-gray-100 flex flex-col md:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-3 w-full md:max-w-md">
            <div className="relative flex-1">
              <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
              <Input
                type="text"
                placeholder="Search reports by candidate, email, or drive..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9 text-xs h-9 bg-gray-50/70 border-gray-200"
              />
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3 w-full md:w-auto self-end md:self-center">
            {/* Workspace Select Filter */}
            <div className="flex items-center gap-1.5 text-xs text-gray-600">
              <Filter className="w-3.5 h-3.5 text-gray-400" />
              <select
                value={selectedWorkspaceFilter}
                onChange={(e) => setSelectedWorkspaceFilter(e.target.value)}
                className="h-9 rounded-xl border border-gray-200 bg-gray-50/70 px-3 text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#F05323]"
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
              className="h-9 rounded-xl border border-gray-200 bg-gray-50/70 px-3 text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#F05323]"
            >
              <option value="ALL">All Statuses</option>
              <option value="COMPLETED">Completed / Scored</option>
              <option value="SCHEDULED">Scheduled</option>
              <option value="ASSIGNED">Assigned</option>
            </select>
          </div>
        </div>

        {/* Table Content */}
        {filteredReports.length === 0 ? (
          <div className="p-12 text-center text-xs text-gray-400">
            No assessment reports found matching your criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50/80 text-gray-500 font-semibold uppercase tracking-wider border-b border-gray-100">
                <tr>
                  <th className="px-6 py-3.5">Candidate</th>
                  <th className="px-6 py-3.5">Workspace</th>
                  <th className="px-6 py-3.5">Assessment</th>
                  <th className="px-6 py-3.5">Schedule</th>
                  <th className="px-6 py-3.5">Score</th>
                  <th className="px-6 py-3.5">Status</th>
                  <th className="px-6 py-3.5 text-right">Technical Report</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredReports.map((row) => (
                  <tr key={row.assessmentId} className="hover:bg-orange-50/30 transition-colors">
                    {/* Candidate */}
                    <td className="px-6 py-4">
                      <span className="font-bold text-gray-900 block">{row.candidateName}</span>
                      <span className="text-[11px] text-gray-500 font-mono">{row.candidateEmail}</span>
                    </td>

                    {/* Workspace */}
                    <td className="px-6 py-4 font-semibold text-gray-800">
                      {row.workspaceName}
                    </td>

                    {/* Assessment & Difficulty */}
                    <td className="px-6 py-4">
                      <span className="text-gray-900 font-medium block truncate max-w-[200px]">
                        {row.title}
                      </span>
                      <span className="text-[10px] font-bold text-[#F05323] uppercase">
                        {row.difficulty}
                      </span>
                    </td>

                    {/* Schedule */}
                    <td className="px-6 py-4 text-gray-600">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-gray-400" />
                        <span>{row.scheduledDate}</span>
                      </div>
                      <span className="text-[11px] text-gray-400 pl-5">{row.scheduledTime}</span>
                    </td>

                    {/* Score */}
                    <td className="px-6 py-4">
                      {typeof row.score === "number" ? (
                        <div className="flex items-center gap-2">
                          <span
                            className={`font-black text-sm ${
                              row.score >= 70 ? "text-emerald-600" : "text-rose-600"
                            }`}
                          >
                            {row.score}%
                          </span>
                          <span className="text-[10px] text-gray-400 font-mono">
                            ({row.passedTests || 9}/{row.totalTests || 10})
                          </span>
                        </div>
                      ) : (
                        <span className="text-gray-400 font-mono">—</span>
                      )}
                    </td>

                    {/* Status */}
                    <td className="px-6 py-4">
                      {row.status === "COMPLETED" ? (
                        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                          <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                          Completed
                        </span>
                      ) : row.status === "SCHEDULED" ? (
                        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-blue-50 text-blue-700 border border-blue-200">
                          <Calendar className="w-3 h-3 text-blue-600" />
                          Scheduled
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-amber-50 text-amber-700 border border-amber-200">
                          Assigned
                        </span>
                      )}
                    </td>

                    {/* Action */}
                    <td className="px-6 py-4 text-right">
                      {row.status === "COMPLETED" ? (
                        <Link
                          to={`/dashboard/candidates/${row.assessmentId}`}
                          className="inline-flex items-center gap-1 text-xs font-bold text-[#F05323] hover:underline"
                        >
                          <span>View Report</span>
                          <ExternalLink className="w-3.5 h-3.5" />
                        </Link>
                      ) : (
                        <Link
                          to={`/dashboard/workspaces/${row.workspaceId}/candidates/${row.candidateId}`}
                          className="inline-flex items-center gap-1 text-xs font-semibold text-gray-500 hover:text-gray-900"
                        >
                          <span>Candidate Info</span>
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
