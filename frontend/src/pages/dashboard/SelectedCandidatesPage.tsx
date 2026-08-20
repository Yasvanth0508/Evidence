import { useState } from "react";
import { Link } from "react-router-dom";
import { useHRStore } from "@/store/hrStore";
import { Input } from "@/components/ui/input";
import {
  UserCheck2,
  Search,
  Award,
  Calendar,
  Building2,
  ExternalLink,
  Mail,
  Filter,
  CheckCircle2,
  Clock,
  Sparkles,
} from "lucide-react";

export const SelectedCandidatesPage = () => {
  const { getAllSelectedCandidates, workspaces } = useHRStore();
  const selectedCandidates = getAllSelectedCandidates();

  const [searchQuery, setSearchQuery] = useState("");
  const [workspaceFilter, setWorkspaceFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const filteredCandidates = selectedCandidates.filter((cand) => {
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
            <span className="text-[10px] uppercase font-bold text-emerald-700 block">Total Selected</span>
            <span className="text-xl font-extrabold text-emerald-900 block">
              {selectedCandidates.length}
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
          {/* Workspace Filter */}
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

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-9 rounded-xl border border-gray-200 bg-gray-50/70 px-3 text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#F05323]"
          >
            <option value="ALL">All Stages</option>
            <option value="SELECTED">Selected</option>
            <option value="OFFER_EXTENDED">Offer Extended</option>
            <option value="SHORTLISTED">Shortlisted</option>
          </select>
        </div>
      </div>

      {/* 3. Selected Candidates Table */}
      <div className="bg-white rounded-3xl border border-gray-200/90 shadow-2xs overflow-hidden">
        {filteredCandidates.length === 0 ? (
          <div className="p-12 text-center space-y-3">
            <UserCheck2 className="w-10 h-10 text-gray-300 mx-auto" />
            <h4 className="text-sm font-bold text-gray-900">No Selected Candidates Found</h4>
            <p className="text-xs text-gray-500 max-w-sm mx-auto">
              Candidates marked as selected from workspace candidate detail views will appear here.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50/80 text-gray-500 font-semibold uppercase tracking-wider border-b border-gray-100">
                <tr>
                  <th className="px-6 py-3.5">Candidate</th>
                  <th className="px-6 py-3.5">Position / Role</th>
                  <th className="px-6 py-3.5">Placement Drive</th>
                  <th className="px-6 py-3.5">Assessment Score</th>
                  <th className="px-6 py-3.5">Selection Status</th>
                  <th className="px-6 py-3.5">Selected Date</th>
                  <th className="px-6 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredCandidates.map((cand) => (
                  <tr key={cand.id} className="hover:bg-orange-50/30 transition-colors">
                    {/* Candidate */}
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <img
                          src={
                            cand.avatarUrl ||
                            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80"
                          }
                          alt={cand.name}
                          className="w-9 h-9 rounded-full object-cover border border-gray-200"
                        />
                        <div>
                          <span className="font-bold text-gray-900 block">{cand.name}</span>
                          <span className="text-[11px] text-gray-500 font-mono flex items-center gap-1">
                            <Mail className="w-3 h-3 text-gray-400" />
                            {cand.email}
                          </span>
                        </div>
                      </div>
                    </td>

                    {/* Role */}
                    <td className="px-6 py-4">
                      <span className="font-semibold text-gray-800">{cand.role}</span>
                    </td>

                    {/* Drive */}
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1 text-gray-700 bg-gray-100 px-2.5 py-1 rounded-lg">
                        <Building2 className="w-3.5 h-3.5 text-gray-400" />
                        {cand.workspaceName}
                      </span>
                    </td>

                    {/* Score */}
                    <td className="px-6 py-4">
                      {cand.latestScore || cand.assessment?.score ? (
                        <div className="flex items-center gap-1.5">
                          <Award className="w-4 h-4 text-emerald-600" />
                          <span className="font-extrabold text-sm text-emerald-600">
                            {cand.latestScore || cand.assessment?.score}%
                          </span>
                        </div>
                      ) : (
                        <span className="text-gray-400 font-mono">Pending</span>
                      )}
                    </td>

                    {/* Selection Status */}
                    <td className="px-6 py-4">
                      {getStatusBadge(cand.selectionStatus)}
                    </td>

                    {/* Selected Date */}
                    <td className="px-6 py-4 text-gray-600">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-gray-400" />
                        <span>{cand.selectedDate || "2026-08-18"}</span>
                      </div>
                    </td>

                    {/* Actions */}
                    <td className="px-6 py-4 text-right">
                      {cand.assessment?.status === "COMPLETED" ? (
                        <Link
                          to={`/dashboard/candidates/${cand.assessment.id}`}
                          className="inline-flex items-center gap-1 text-xs font-bold text-[#F05323] hover:underline"
                        >
                          <span>View Evaluation</span>
                          <ExternalLink className="w-3.5 h-3.5" />
                        </Link>
                      ) : (
                        <span className="text-[11px] text-gray-400 font-medium">Selected</span>
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
