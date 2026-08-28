import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { candidateService, CandidateProfile } from "@/services/candidateService";
import { reportService, ReportItem } from "@/services/reportService";
import { workspaceService, WorkspaceResponse } from "@/services/workspaceService";
import { Input } from "@/components/ui/input";
import {
  UserCheck2,
  Search,
  Award,
  Building2,
  ExternalLink,
  Filter,
  CheckCircle2,
  Clock,
  Sparkles,
  Loader2,
} from "lucide-react";

interface DisplayCandidate {
  id: string;
  name: string;
  email: string;
  role: string;
  workspaceName: string;
  score?: number;
  status: string;
  assessmentId?: string;
  selectionStatus: "SELECTED" | "SHORTLISTED" | "OFFER_EXTENDED";
}

export const SelectedCandidatesPage = () => {
  const [candidates, setCandidates] = useState<DisplayCandidate[]>([]);
  const [workspaces, setWorkspaces] = useState<WorkspaceResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [workspaceFilter, setWorkspaceFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");

  useEffect(() => {
    let isMounted = true;

    async function loadData() {
      setIsLoading(true);
      try {
        const [candList, reportsRes, wsList] = await Promise.all([
          candidateService.getAllCandidates().catch(() => []),
          reportService.getReports({ page: 0, size: 100 }).catch(() => ({ content: [], reports: [] } as any)),
          workspaceService.getWorkspaces().catch(() => []),
        ]);

        if (!isMounted) return;

        if (Array.isArray(wsList)) {
          setWorkspaces(wsList);
        }

        const reports: ReportItem[] = reportsRes?.content || reportsRes?.reports || [];

        // Build list of candidates who passed (>=70%) or have reports
        const mapped: DisplayCandidate[] = [];

        // Add from completed assessment reports
        reports.forEach((r) => {
          const score = typeof r.score === "number" ? r.score : 0;
          let selStatus: "SELECTED" | "SHORTLISTED" | "OFFER_EXTENDED" = "SHORTLISTED";
          if (score >= 90) selStatus = "OFFER_EXTENDED";
          else if (score >= 70) selStatus = "SELECTED";

          mapped.push({
            id: r.candidateId || r.assessmentId,
            name: r.candidateName || "Candidate",
            email: r.candidateEmail || "",
            role: "Java Backend Engineer",
            workspaceName: r.workspaceName || "Campus Drive",
            score: r.score,
            status: r.status,
            assessmentId: r.assessmentId,
            selectionStatus: selStatus,
          });
        });

        // Add any remaining candidates from candidate registry
        if (Array.isArray(candList)) {
          candList.forEach((c: CandidateProfile) => {
            if (!mapped.some((m) => m.email.toLowerCase() === c.email.toLowerCase())) {
              mapped.push({
                id: c.id,
                name: c.name,
                email: c.email,
                role: c.role || "Java Software Engineer",
                workspaceName: "General Pool",
                status: "READY",
                selectionStatus: "SHORTLISTED",
              });
            }
          });
        }

        setCandidates(mapped);
      } catch (err) {
        console.error("Failed to load selected candidates:", err);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    }

    loadData();

    return () => {
      isMounted = false;
    };
  }, []);

  const filteredCandidates = candidates.filter((cand) => {
    const matchesSearch =
      cand.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cand.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cand.role.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesWorkspace =
      workspaceFilter === "ALL" || cand.workspaceName === workspaceFilter;
    const matchesStatus =
      statusFilter === "ALL" || cand.selectionStatus === statusFilter;

    return matchesSearch && matchesWorkspace && matchesStatus;
  });

  const getStatusBadge = (status?: string) => {
    switch (status) {
      case "OFFER_EXTENDED":
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-purple-50 text-purple-700 border border-purple-200">
            <Sparkles className="w-3 h-3 text-purple-600" />
            Offer Extended
          </span>
        );
      case "SHORTLISTED":
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-blue-50 text-blue-700 border border-blue-200">
            <Clock className="w-3 h-3 text-blue-600" />
            Shortlisted
          </span>
        );
      case "SELECTED":
      default:
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="w-3 h-3 text-emerald-600" />
            Selected for Hire
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Header Card */}
      <div className="bg-white p-6 rounded-3xl border border-gray-200/90 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 mb-1.5">
            <UserCheck2 className="w-3 h-3 text-emerald-600" />
            Hiring Pipeline
          </div>
          <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight">
            Selected Candidates
          </h1>
          <p className="text-xs text-gray-500 font-medium">
            Candidates who demonstrated engineering mastery and have been selected for hiring.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="px-4 py-2 bg-emerald-50 border border-emerald-200 rounded-2xl text-center">
            <span className="text-[10px] uppercase font-bold text-emerald-700 block">Total In Pipeline</span>
            <span className="text-xl font-extrabold text-emerald-900 block">
              {candidates.length}
            </span>
          </div>
        </div>
      </div>

      {/* 2. Filters & Search */}
      <div className="bg-white px-4 py-3 rounded-2xl border border-gray-200 shadow-2xs flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="relative w-full md:max-w-md">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <Input
            type="text"
            placeholder="Search selected candidates by name, email, or role..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9 text-xs h-9 bg-gray-50/70 border-gray-200"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto self-end md:self-center">
          <div className="flex items-center gap-1.5 text-xs text-gray-600">
            <Filter className="w-3.5 h-3.5 text-gray-400" />
            <select
              value={workspaceFilter}
              onChange={(e) => setWorkspaceFilter(e.target.value)}
              className="h-9 rounded-xl border border-gray-200 bg-gray-50/70 px-3 text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#F05323]"
            >
              <option value="ALL">All Workspaces</option>
              {workspaces.map((w) => (
                <option key={w.id} value={w.name}>
                  {w.name}
                </option>
              ))}
            </select>
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-9 rounded-xl border border-gray-200 bg-gray-50/70 px-3 text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#F05323]"
          >
            <option value="ALL">All Pipeline Stages</option>
            <option value="OFFER_EXTENDED">Offer Extended</option>
            <option value="SELECTED">Selected for Hire</option>
            <option value="SHORTLISTED">Shortlisted</option>
          </select>
        </div>
      </div>

      {/* 3. Candidate Grid */}
      {isLoading ? (
        <div className="bg-white rounded-3xl border border-gray-200 p-12 flex flex-col items-center justify-center gap-2 text-xs text-gray-400">
          <Loader2 className="w-5 h-5 animate-spin text-[#F05323]" />
          Loading candidate pipeline...
        </div>
      ) : filteredCandidates.length === 0 ? (
        <div className="bg-white rounded-3xl border border-gray-200 p-12 text-center text-xs text-gray-400 space-y-2">
          <p>No candidates found matching the selected filters.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredCandidates.map((cand) => (
            <div
              key={cand.id}
              className="bg-white rounded-3xl border border-gray-200/90 p-6 shadow-2xs hover:shadow-md transition-all flex flex-col justify-between space-y-4"
            >
              <div className="space-y-3">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-bold text-sm text-gray-900">{cand.name}</h3>
                    <p className="text-[11px] text-gray-500 font-mono">{cand.email}</p>
                    <span className="inline-block text-[11px] font-semibold text-gray-600 mt-1">
                      {cand.role}
                    </span>
                  </div>
                  {getStatusBadge(cand.selectionStatus)}
                </div>

                <div className="p-3 bg-gray-50/80 rounded-2xl border border-gray-100 space-y-1.5 text-xs text-gray-600">
                  <div className="flex items-center gap-2">
                    <Building2 className="w-3.5 h-3.5 text-gray-400" />
                    <span className="font-medium truncate">{cand.workspaceName}</span>
                  </div>
                  {typeof cand.score === "number" && (
                    <div className="flex items-center gap-2">
                      <Award className="w-3.5 h-3.5 text-[#F05323]" />
                      <span className="font-bold text-gray-900">Score: {cand.score}%</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="pt-2 border-t border-gray-100 flex items-center justify-between">
                <span className="text-[10px] text-gray-400">Live Evaluation</span>
                {cand.assessmentId ? (
                  <Link
                    to={`/dashboard/candidates/${cand.assessmentId}`}
                    className="inline-flex items-center gap-1 text-xs font-bold text-[#F05323] hover:underline"
                  >
                    <span>View Report</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </Link>
                ) : (
                  <Link
                    to="/dashboard/workspaces"
                    className="text-xs font-semibold text-gray-500 hover:text-gray-900"
                  >
                    View Workspace
                  </Link>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
