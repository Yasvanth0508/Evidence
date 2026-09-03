import { useState, useEffect, useCallback } from "react";
import { Link } from "react-router-dom";
import { reportService, SelectedCandidateItem } from "@/services/reportService";
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
  X,
  StickyNote,
  ChevronDown,
  Users,
  Trash2,
} from "lucide-react";

export const SelectedCandidatesPage = () => {
  const [candidates, setCandidates] = useState<SelectedCandidateItem[]>([]);
  const [workspaces, setWorkspaces] = useState<WorkspaceResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [workspaceFilter, setWorkspaceFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [removingId, setRemovingId] = useState<string | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error" = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const loadData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [selectedList, wsList] = await Promise.all([
        reportService.getSelectedCandidates().catch(() => []),
        workspaceService.getWorkspaces().catch(() => []),
      ]);

      if (Array.isArray(wsList)) setWorkspaces(wsList);

      // Only show candidates explicitly marked as selected in DB
      if (Array.isArray(selectedList)) {
        setCandidates(selectedList);
      }
    } catch (err) {
      console.error("Failed to load selected candidates:", err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const filteredCandidates = candidates.filter((cand) => {
    const matchesSearch =
      cand.candidateName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cand.candidateEmail.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (cand.candidateRole || "").toLowerCase().includes(searchQuery.toLowerCase());
    const matchesWorkspace =
      workspaceFilter === "ALL" || cand.workspaceId === workspaceFilter;
    const matchesStatus =
      statusFilter === "ALL" || cand.selectionStatus === statusFilter;
    return matchesSearch && matchesWorkspace && matchesStatus;
  });

  const handleStatusChange = async (cand: SelectedCandidateItem, newStatus: string) => {
    setUpdatingId(cand.id);
    try {
      await reportService.updateSelectionStatus(cand.id, {
        workspaceId: cand.workspaceId,
        candidateId: cand.candidateId,
        selectionStatus: newStatus,
        selectionNotes: cand.selectionNotes,
      });
      setCandidates((prev) =>
        prev.map((c) => (c.id === cand.id ? { ...c, selectionStatus: newStatus } : c))
      );
      showToast(`Pipeline stage updated to "${getStatusLabel(newStatus)}"`);
    } catch (err) {
      console.error("Failed to update selection status:", err);
      showToast("Failed to update pipeline stage", "error");
    } finally {
      setUpdatingId(null);
    }
  };

  const handleRemove = async (cand: SelectedCandidateItem) => {
    if (!confirm(`Remove ${cand.candidateName} from the selected candidates list?`)) return;
    setRemovingId(cand.id);
    try {
      await reportService.removeSelectedCandidateByWorkspaceAndCandidate(
        cand.workspaceId,
        cand.candidateId
      );
      setCandidates((prev) => prev.filter((c) => c.id !== cand.id));
      showToast(`${cand.candidateName} removed from selected candidates`);
    } catch (err) {
      console.error("Failed to remove selected candidate:", err);
      showToast("Failed to remove candidate", "error");
    } finally {
      setRemovingId(null);
    }
  };

  const getStatusLabel = (status?: string) => {
    switch (status) {
      case "OFFER_EXTENDED": return "Offer Extended";
      case "SHORTLISTED": return "Shortlisted";
      case "SELECTED": return "Selected for Hire";
      default: return status || "Selected for Hire";
    }
  };

  const getStatusBadge = (status?: string) => {
    switch (status) {
      case "OFFER_EXTENDED":
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-purple-50 dark:bg-purple-950/40 text-purple-700 dark:text-purple-300 border border-purple-200 dark:border-purple-800">
            <Sparkles className="w-3 h-3 text-purple-600 dark:text-purple-400" />
            Offer Extended
          </span>
        );
      case "SHORTLISTED":
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-blue-50 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800">
            <Clock className="w-3 h-3 text-blue-600 dark:text-blue-400" />
            Shortlisted
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
            <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Selected for Hire
          </span>
        );
    }
  };

  const getScoreColor = (score?: number) => {
    if (score == null) return "text-gray-500 dark:text-slate-400";
    if (score >= 90) return "text-purple-600 dark:text-purple-400";
    if (score >= 70) return "text-emerald-600 dark:text-emerald-400";
    return "text-rose-600 dark:text-rose-400";
  };

  const formatSelectedAt = (selectedAt: string) => {
    try {
      return new Date(selectedAt).toLocaleDateString("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric",
      });
    } catch {
      return selectedAt;
    }
  };

  const getInitials = (name: string) =>
    name
      .split(" ")
      .map((n) => n[0])
      .slice(0, 2)
      .join("")
      .toUpperCase();

  return (
    <div className="space-y-6">
      {/* Toast */}
      {toast && (
        <div
          className={`fixed top-5 right-5 z-50 flex items-center gap-2.5 px-4 py-3 rounded-2xl shadow-lg text-xs font-semibold animate-in slide-in-from-top duration-200 ${
            toast.type === "success"
              ? "bg-emerald-50 dark:bg-emerald-950/80 text-emerald-800 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800"
              : "bg-rose-50 dark:bg-rose-950/80 text-rose-800 dark:text-rose-300 border border-rose-200 dark:border-rose-800"
          }`}
        >
          {toast.type === "success" ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
          ) : (
            <X className="w-4 h-4 text-rose-600 dark:text-rose-400" />
          )}
          {toast.message}
        </div>
      )}

      {/* 1. Header Card */}
      <div className="bg-white dark:bg-slate-900 p-6 rounded-3xl border border-gray-200/90 dark:border-slate-800 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-colors">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800 mb-1.5">
            <UserCheck2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Hiring Pipeline
          </div>
          <h1 className="text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight">
            Selected Candidates
          </h1>
          <p className="text-xs text-gray-500 dark:text-slate-400 font-medium">
            Candidates explicitly marked as selected for hiring. Go to a candidate's workspace detail page to mark them.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="px-4 py-2 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-2xl text-center">
            <span className="text-[10px] uppercase font-bold text-emerald-700 dark:text-emerald-400 block">In Pipeline</span>
            <span className="text-xl font-extrabold text-emerald-900 dark:text-emerald-300 block">
              {candidates.length}
            </span>
          </div>
        </div>
      </div>

      {/* 2. Filters & Search */}
      <div className="bg-white dark:bg-slate-900 px-4 py-3 rounded-2xl border border-gray-200 dark:border-slate-800 shadow-2xs flex flex-col md:flex-row items-center justify-between gap-3 transition-colors">
        <div className="relative w-full md:max-w-md">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 dark:text-slate-500" />
          <Input
            type="text"
            placeholder="Search by name, email, or role..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9 text-xs h-9 bg-gray-50/70 dark:bg-slate-800 border-gray-200 dark:border-slate-700"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto self-end md:self-center">
          <div className="flex items-center gap-1.5 text-xs text-gray-600 dark:text-slate-300">
            <Filter className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
            <select
              value={workspaceFilter}
              onChange={(e) => setWorkspaceFilter(e.target.value)}
              className="h-9 rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50/70 dark:bg-slate-800 px-3 text-xs text-gray-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-[var(--theme-primary)]"
            >
              <option value="ALL">All Workspaces</option>
              {workspaces.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-9 rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50/70 dark:bg-slate-800 px-3 text-xs text-gray-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-[var(--theme-primary)]"
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
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200 dark:border-slate-800 p-12 flex flex-col items-center justify-center gap-2 text-xs text-gray-400 dark:text-slate-500 transition-colors">
          <Loader2 className="w-5 h-5 animate-spin text-[var(--theme-primary)]" />
          Loading candidate pipeline...
        </div>
      ) : filteredCandidates.length === 0 ? (
        <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200 dark:border-slate-800 p-16 text-center space-y-4 transition-colors">
          <div className="w-14 h-14 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center mx-auto">
            <Users className="w-7 h-7" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-gray-900 dark:text-white">
              {candidates.length === 0
                ? "No candidates selected yet"
                : "No candidates match your filters"}
            </h3>
            <p className="text-xs text-gray-500 dark:text-slate-400 max-w-sm mx-auto mt-1 leading-relaxed">
              {candidates.length === 0
                ? "Go to a candidate's workspace detail page and click \"Mark as Selected\" to add them to the hiring pipeline."
                : "Try adjusting your search or filter criteria."}
            </p>
          </div>
          {candidates.length === 0 && (
            <Link
              to="/dashboard/workspaces"
              className="inline-flex items-center gap-1.5 text-xs font-bold text-[var(--theme-primary)] dark:text-primary hover:underline"
            >
              <ExternalLink className="w-3.5 h-3.5" />
              Go to Workspaces
            </Link>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredCandidates.map((cand) => (
            <div
              key={cand.id}
              className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 shadow-2xs hover:shadow-md transition-all flex flex-col justify-between space-y-4"
            >
              {/* Top: Avatar + Name + Badge */}
              <div className="space-y-3">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    {/* Avatar */}
                    <div className="w-10 h-10 rounded-full bg-purple-100 dark:bg-purple-950/60 border border-purple-200 dark:border-purple-800 text-purple-700 dark:text-purple-300 flex items-center justify-center font-bold text-sm flex-shrink-0">
                      {getInitials(cand.candidateName)}
                    </div>
                    <div>
                      <h3 className="font-bold text-sm text-gray-900 dark:text-white leading-tight">
                        {cand.candidateName}
                      </h3>
                      <p className="text-[11px] text-gray-500 dark:text-slate-400 font-mono truncate max-w-[160px]">
                        {cand.candidateEmail}
                      </p>
                      <span className="inline-block text-[11px] font-semibold text-gray-600 dark:text-slate-300 mt-0.5">
                        {cand.candidateRole || "Java Backend Engineer"}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Workspace + Selection Date */}
                <div className="p-3 bg-gray-50/80 dark:bg-slate-800 rounded-2xl border border-gray-100 dark:border-slate-700 space-y-1.5 text-xs text-gray-600 dark:text-slate-300">
                  <div className="flex items-center gap-2">
                    <Building2 className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500 flex-shrink-0" />
                    <span className="font-medium truncate">{cand.workspaceName}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 dark:text-emerald-400 flex-shrink-0" />
                    <span className="text-gray-500 dark:text-slate-400">
                      Selected on {formatSelectedAt(cand.selectedAt)}
                    </span>
                  </div>
                </div>

                {/* Evaluation Score Section */}
                {typeof cand.score === "number" && cand.score > 0 ? (
                  <div className="p-3 bg-gray-50/80 dark:bg-slate-800 rounded-2xl border border-gray-100 dark:border-slate-700 space-y-1.5">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Award className="w-3.5 h-3.5 text-[var(--theme-primary)] dark:text-primary" />
                        <span className={`text-sm font-black ${getScoreColor(cand.score)}`}>
                          {cand.score}%
                        </span>
                        {cand.scoreRating && (
                          <span className="text-[10px] font-bold text-gray-500 dark:text-slate-400 bg-gray-100 dark:bg-slate-700 px-1.5 py-0.5 rounded-lg">
                            {cand.scoreRating}
                          </span>
                        )}
                      </div>
                      {cand.timeTakenMinutes != null && (
                        <div className="flex items-center gap-1 text-[11px] text-gray-500 dark:text-slate-400">
                          <Clock className="w-3 h-3" />
                          <span>{cand.timeTakenMinutes} mins</span>
                        </div>
                      )}
                    </div>
                    {cand.passedTests != null && cand.totalTests != null && (
                      <div className="text-[11px] text-gray-500 dark:text-slate-400">
                        <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                          {cand.passedTests}
                        </span>
                        <span> / {cand.totalTests} Tests Passed</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="p-3 bg-gray-50/60 dark:bg-slate-800/60 rounded-2xl border border-dashed border-gray-200 dark:border-slate-700 text-[11px] text-gray-400 dark:text-slate-500 text-center">
                    No evaluation data yet
                  </div>
                )}

                {/* Selection Notes */}
                {cand.selectionNotes && cand.selectionNotes.trim() && (
                  <div className="p-2.5 bg-amber-50/60 dark:bg-amber-950/30 rounded-xl border border-amber-100 dark:border-amber-900/50 flex items-start gap-2">
                    <StickyNote className="w-3 h-3 text-amber-500 dark:text-amber-400 flex-shrink-0 mt-0.5" />
                    <p className="text-[11px] text-amber-800 dark:text-amber-300 leading-relaxed line-clamp-2">
                      {cand.selectionNotes}
                    </p>
                  </div>
                )}

                {/* Pipeline Stage Dropdown */}
                <div className="relative">
                  <div className="flex items-center gap-1.5 mb-1">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-gray-400 dark:text-slate-500">
                      Pipeline Stage
                    </span>
                    {updatingId === cand.id && (
                      <Loader2 className="w-3 h-3 animate-spin text-[var(--theme-primary)]" />
                    )}
                  </div>
                  <div className="relative">
                    <select
                      value={cand.selectionStatus || "SELECTED"}
                      onChange={(e) => handleStatusChange(cand, e.target.value)}
                      disabled={updatingId === cand.id}
                      className="appearance-none w-full h-8 rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 pl-3 pr-7 text-xs font-semibold text-gray-800 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-[var(--theme-primary)] disabled:opacity-60 cursor-pointer"
                    >
                      <option value="SELECTED">Selected for Hire</option>
                      <option value="SHORTLISTED">Shortlisted</option>
                      <option value="OFFER_EXTENDED">Offer Extended</option>
                    </select>
                    <ChevronDown className="absolute right-2 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-400 dark:text-slate-500 pointer-events-none" />
                  </div>
                </div>
              </div>

              {/* Bottom: Status badge + Actions */}
              <div className="pt-3 border-t border-gray-100 dark:border-slate-800 flex items-center justify-between">
                <div>{getStatusBadge(cand.selectionStatus)}</div>

                <div className="flex items-center gap-2">
                  {/* Remove Button */}
                  <button
                    onClick={() => handleRemove(cand)}
                    disabled={removingId === cand.id}
                    title="Remove from selected"
                    className="p-1.5 rounded-lg text-gray-400 dark:text-slate-500 hover:text-rose-500 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors disabled:opacity-50"
                  >
                    {removingId === cand.id ? (
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                      <Trash2 className="w-3.5 h-3.5" />
                    )}
                  </button>

                  {/* View Report */}
                  {cand.assessmentId ? (
                    <Link
                      to={`/dashboard/candidates/${cand.assessmentId}`}
                      className="inline-flex items-center gap-1 text-xs font-bold text-[var(--theme-primary)] dark:text-primary hover:underline"
                    >
                      <span>View Report</span>
                      <ExternalLink className="w-3.5 h-3.5" />
                    </Link>
                  ) : (
                    <Link
                      to="/dashboard/workspaces"
                      className="text-xs font-semibold text-gray-500 dark:text-slate-400 hover:text-gray-900 dark:hover:text-white"
                    >
                      View Workspace
                    </Link>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
