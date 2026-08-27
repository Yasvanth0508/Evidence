import { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useHRStore } from "@/store/hrStore";
import { workspaceService } from "@/services/workspaceService";
import { candidateService } from "@/services/candidateService";
import { assessmentService } from "@/services/assessmentService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Folder,
  UserPlus,
  ArrowLeft,
  Search,
  CheckCircle2,
  Clock,
  AlertCircle,
  Calendar,
  X,
  Mail,
  User,
  ChevronRight,
  Trash2,
  Check,
} from "lucide-react";

export const WorkspaceDetailPage = () => {
  const { workspaceId = "ws-iit-bombay" } = useParams<{ workspaceId: string }>();
  const navigate = useNavigate();
  const {
    getWorkspaceById,
    getCandidatesForWorkspace,
    candidates: globalCandidates,
    createWorkspace,
    addCandidateToWorkspace,
    removeCandidateFromWorkspace,
    createAndAddCandidate,
    assignAssessment,
  } = useHRStore();

  const workspace = getWorkspaceById(workspaceId);
  const workspaceCandidates = getCandidatesForWorkspace(workspaceId);

  const [tableSearch, setTableSearch] = useState("");
  const [isAddCandidateModalOpen, setIsAddCandidateModalOpen] = useState(false);

  // Search Modal State
  const [candidateSearchQuery, setCandidateSearchQuery] = useState("");
  const [selectedCandidateId, setSelectedCandidateId] = useState<string | null>(null);
  const [showCreateTab, setShowCreateTab] = useState(false);
  const [newCandidateName, setNewCandidateName] = useState("");
  const [newCandidateEmail, setNewCandidateEmail] = useState("");
  const [newCandidateRole, setNewCandidateRole] = useState("Java Backend Developer");
  const [statusMessage, setStatusMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const [backendAllCandidates, setBackendAllCandidates] = useState<any[]>([]);

  // Sync workspace and candidates from backend
  useEffect(() => {
    let isMounted = true;
    if (!workspaceId) return;

    // 1. Fetch workspace details
    workspaceService
      .getWorkspaceById(workspaceId)
      .then((wsData) => {
        if (isMounted && wsData) {
          const currentWs = getWorkspaceById(workspaceId);
          if (!currentWs) {
            createWorkspace({
              id: wsData.id,
              name: wsData.name,
              description: wsData.description || "",
              track: "Java Spring Boot Backend",
              defaultDurationMinutes: 90,
            });
          }
        }
      })
      .catch((err) => {
        console.debug("Backend workspace fetch failed or offline:", err);
      });

    // 2. Fetch candidates enrolled in this workspace
    workspaceService
      .getCandidatesInWorkspace(workspaceId)
      .then((candList) => {
        if (isMounted && Array.isArray(candList)) {
          candList.forEach((item: any) => {
            const cand = item.candidate || item;
            if (!cand || !cand.id) return;
            createAndAddCandidate(workspaceId, {
              id: cand.id,
              name: cand.name || "Candidate",
              email: cand.email,
              role: cand.role || "Java Backend Developer",
            });
          });
        }
      })
      .catch((err) => {
        console.debug("Backend candidates fetch failed:", err);
      });

    // 3. Fetch all registered candidates in DB for search & add
    candidateService
      .getAllCandidates()
      .then((allCands: any) => {
        if (isMounted && Array.isArray(allCands)) {
          setBackendAllCandidates(allCands);
          allCands.forEach((c: any) => {
            const existing = globalCandidates.find((gc) => gc.id === c.id || gc.email === c.email);
            if (!existing) {
              createAndAddCandidate(workspaceId, {
                id: c.id,
                name: c.name || "Candidate",
                email: c.email,
                role: c.role || "Java Backend Developer",
              });
            }
          });
        }
      })
      .catch((err: any) => {
        console.debug("Backend all-candidates fetch failed:", err);
      });

    // 4. Fetch assessments in this workspace
    assessmentService
      .getAssessmentsByWorkspace(workspaceId)
      .then((asmtList) => {
        if (isMounted && Array.isArray(asmtList)) {
          asmtList.forEach((asmt: any) => {
            const candId = asmt.candidate?.id || asmt.candidateId;
            if (candId) {
              assignAssessment({
                id: asmt.id,
                workspaceId,
                candidateId: candId,
                title: asmt.title || "Java Spring Boot Technical Assessment",
                category: "Spring Boot REST API",
                repositoryUrl: asmt.repositoryUrl || "https://github.com/scanurag/FoodFrenzy.git",
                branchName: asmt.branchName || "master",
                backendRootDirectory: asmt.backendRootDirectory || "",
                difficulty: asmt.difficulty || "INTERMEDIATE",
                durationMinutes: asmt.durationMinutes || 90,
                scheduledDate: asmt.scheduledStartAt ? new Date(asmt.scheduledStartAt).toLocaleDateString() : "Today",
                scheduledTime: asmt.scheduledStartAt ? new Date(asmt.scheduledStartAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : "10:00 AM",
              });
            }
          });
        }
      })
      .catch((err) => {
        console.debug("Backend assessments fetch failed:", err);
      });

    return () => {
      isMounted = false;
    };
  }, [workspaceId]);

  if (!workspace) {
    return (
      <div className="bg-white rounded-3xl border border-gray-200 p-12 text-center space-y-4">
        <AlertCircle className="w-10 h-10 text-rose-500 mx-auto" />
        <h2 className="text-lg font-bold text-gray-900">Workspace Not Found</h2>
        <p className="text-xs text-gray-500">The requested workspace could not be found.</p>
        <Link to="/dashboard/workspaces">
          <Button size="sm" variant="outline">
            Back to Workspaces
          </Button>
        </Link>
      </div>
    );
  }

  // Filter candidates currently in workspace
  const filteredWorkspaceCandidates = workspaceCandidates.filter(
    (c) =>
      c.name.toLowerCase().includes(tableSearch.toLowerCase()) ||
      c.email.toLowerCase().includes(tableSearch.toLowerCase()) ||
      c.displayStatus.toLowerCase().includes(tableSearch.toLowerCase())
  );

  // Combined pool of searchable candidates
  const allKnownCandidates = Array.from(
    new Map(
      [...globalCandidates, ...backendAllCandidates].map((c) => [c.id || c.email, c])
    ).values()
  );

  const searchableCandidates = allKnownCandidates.filter(
    (c) =>
      c.name?.toLowerCase().includes(candidateSearchQuery.toLowerCase()) ||
      c.email?.toLowerCase().includes(candidateSearchQuery.toLowerCase())
  );

  const handleSelectCandidate = (candId: string) => {
    setSelectedCandidateId(candId);
    setStatusMessage(null);
  };

  const handleAddExistingCandidate = async () => {
    if (!selectedCandidateId) return;
    setStatusMessage(null);

    const isAlreadyInWs = workspace.candidateIds.includes(selectedCandidateId);
    const addedCand = allKnownCandidates.find((c) => c.id === selectedCandidateId);

    if (isAlreadyInWs) {
      setStatusMessage({
        type: "success",
        text: `Candidate "${addedCand?.name}" is already in this workspace.`,
      });
      setTimeout(() => {
        setIsAddCandidateModalOpen(false);
        setSelectedCandidateId(null);
        setCandidateSearchQuery("");
        setStatusMessage(null);
      }, 700);
      return;
    }

    if (addedCand) {
      try {
        const enrollRes = await workspaceService.addCandidateToWorkspace(workspaceId, addedCand.email, addedCand.name);
        const resolvedCand = enrollRes?.candidate || addedCand;
        createAndAddCandidate(workspaceId, {
          id: resolvedCand.id,
          name: resolvedCand.name,
          email: resolvedCand.email,
          role: resolvedCand.role || "Java Backend Developer",
        });
      } catch (err) {
        console.warn("Backend candidate addition offline:", err);
        addCandidateToWorkspace(workspaceId, selectedCandidateId);
      }
    }

    setStatusMessage({
      type: "success",
      text: `Candidate "${addedCand?.name}" successfully added to ${workspace.name}!`,
    });

    setTimeout(() => {
      setIsAddCandidateModalOpen(false);
      setSelectedCandidateId(null);
      setCandidateSearchQuery("");
      setStatusMessage(null);
    }, 700);
  };

  const handleRemoveCandidate = async (e: React.MouseEvent, candId: string, candName: string) => {
    e.stopPropagation();
    if (confirm(`Remove ${candName} from this workspace?`)) {
      try {
        await workspaceService.removeCandidateFromWorkspace(workspaceId, candId);
      } catch (err) {
        console.warn("Backend candidate removal offline:", err);
      }
      removeCandidateFromWorkspace(workspaceId, candId);
    }
  };

  const handleCreateAndAddCandidate = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatusMessage(null);

    if (!newCandidateName.trim() || !newCandidateEmail.trim()) {
      setStatusMessage({ type: "error", text: "Name and email are required." });
      return;
    }

    try {
      const enrollRes = await workspaceService.addCandidateToWorkspace(
        workspaceId,
        newCandidateEmail.trim(),
        newCandidateName.trim()
      );
      const returnedCand = enrollRes?.candidate || {
        id: enrollRes?.id || `cand-${Date.now()}`,
        name: newCandidateName.trim(),
        email: newCandidateEmail.trim(),
        role: newCandidateRole,
      };

      createAndAddCandidate(workspaceId, {
        id: returnedCand.id,
        name: returnedCand.name,
        email: returnedCand.email,
        role: newCandidateRole,
      });

      setStatusMessage({ type: "success", text: `Candidate "${returnedCand.name}" enrolled and added to workspace!` });
    } catch (err) {
      console.warn("Backend candidate enrollment error:", err);
      createAndAddCandidate(workspaceId, {
        name: newCandidateName.trim(),
        email: newCandidateEmail.trim(),
        role: newCandidateRole,
      });
      setStatusMessage({ type: "success", text: `Candidate "${newCandidateName}" added to workspace!` });
    }

    setTimeout(() => {
      setIsAddCandidateModalOpen(false);
      setNewCandidateName("");
      setNewCandidateEmail("");
      setStatusMessage(null);
    }, 700);
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "Taken":
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
            Taken
          </span>
        );
      case "Scheduled":
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200">
            <Calendar className="w-3.5 h-3.5 text-blue-600" />
            Scheduled
          </span>
        );
      case "Assigned":
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200">
            <Clock className="w-3.5 h-3.5 text-amber-600" />
            Assigned
          </span>
        );
      case "Not Taken":
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-gray-100 text-gray-600 border border-gray-200">
            <span className="w-1.5 h-1.5 rounded-full bg-gray-400"></span>
            Not Taken
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Breadcrumbs & Top Navigation Bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs font-medium text-gray-500">
          <Link
            to="/dashboard/workspaces"
            className="hover:text-gray-900 inline-flex items-center gap-1 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            Workspaces
          </Link>
          <span>/</span>
          <span className="text-gray-900 font-bold">{workspace.name}</span>
        </div>

        <Button
          size="sm"
          onClick={() => {
            setSelectedCandidateId(null);
            setStatusMessage(null);
            setIsAddCandidateModalOpen(true);
          }}
          className="gap-2 font-semibold bg-[#F05323] hover:bg-[#d94417] text-white shadow-xs"
        >
          <UserPlus className="w-4 h-4" />
          Add Candidate
        </Button>
      </div>

      {/* 2. Workspace Overview Banner */}
      <div className="bg-white rounded-3xl border border-gray-200/90 p-6 sm:p-8 shadow-2xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-start gap-4">
            <div className="w-14 h-14 rounded-2xl bg-orange-50 border border-orange-100 flex items-center justify-center text-[#F05323] flex-shrink-0">
              <Folder className="w-7 h-7 fill-orange-100/60" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight">
                  {workspace.name}
                </h1>
                <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                  {workspace.status}
                </span>
              </div>
              <p className="text-xs text-gray-600 mt-1 max-w-2xl leading-relaxed">
                {workspace.description || "Workspace candidate assessment and evaluation hub"}
              </p>
            </div>
          </div>

          {/* Quick Metrics */}
          <div className="flex items-center gap-4 border-t sm:border-t-0 sm:border-l border-gray-100 pt-3 sm:pt-0 sm:pl-6">
            <div>
              <span className="text-[11px] font-medium text-gray-400 block">Total Candidates</span>
              <span className="text-xl font-extrabold text-gray-900 block">
                {workspaceCandidates.length}
              </span>
            </div>
            <div className="h-8 w-px bg-gray-100"></div>
            <div>
              <span className="text-[11px] font-medium text-gray-400 block">Assessment Track</span>
              <span className="text-xs font-bold text-[#F05323] block max-w-[140px] truncate">
                {workspace.track}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 3. Candidates List Table */}
      <div className="bg-white rounded-3xl border border-gray-200/90 shadow-2xs overflow-hidden">
        {/* Table Header Filter */}
        <div className="p-4 sm:p-5 border-b border-gray-100 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="relative w-full sm:max-w-xs">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
            <Input
              type="text"
              placeholder="Search candidate by name or email..."
              value={tableSearch}
              onChange={(e) => setTableSearch(e.target.value)}
              className="pl-9 text-xs h-9 bg-gray-50/70 border-gray-200"
            />
          </div>

          <div className="text-xs text-gray-500 font-medium self-end sm:self-center">
            Showing <strong className="text-gray-900">{filteredWorkspaceCandidates.length}</strong> candidates
          </div>
        </div>

        {/* Table Content */}
        {filteredWorkspaceCandidates.length === 0 ? (
          <div className="p-12 text-center space-y-3">
            <UserPlus className="w-10 h-10 text-gray-300 mx-auto" />
            <h4 className="text-sm font-bold text-gray-900">No candidates in this workspace</h4>
            <p className="text-xs text-gray-500 max-w-sm mx-auto">
              Add candidates by clicking the "Add Candidate" button above to assign project-specific assessments.
            </p>
            <Button
              size="sm"
              onClick={() => {
                setSelectedCandidateId(null);
                setStatusMessage(null);
                setIsAddCandidateModalOpen(true);
              }}
              className="gap-1.5 font-semibold bg-[#F05323] hover:bg-[#d94417] text-xs"
            >
              <UserPlus className="w-3.5 h-3.5" /> Add First Candidate
            </Button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50/80 text-gray-500 font-semibold uppercase tracking-wider border-b border-gray-100">
                <tr>
                  <th className="px-6 py-3.5">Candidate</th>
                  <th className="px-6 py-3.5">Email</th>
                  <th className="px-6 py-3.5">Role / Position</th>
                  <th className="px-6 py-3.5">Assessment Status</th>
                  <th className="px-6 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredWorkspaceCandidates.map((c) => (
                  <tr
                    key={c.id}
                    onClick={() =>
                      navigate(
                        `/dashboard/workspaces/${workspaceId}/candidates/${c.id}`
                      )
                    }
                    className="hover:bg-orange-50/30 transition-colors cursor-pointer group"
                  >
                    {/* Candidate Name & Avatar */}
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <img
                          src={
                            c.avatarUrl ||
                            `https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80`
                          }
                          alt={c.name}
                          className="w-9 h-9 rounded-full object-cover border border-gray-200"
                        />
                        <div>
                          <span className="font-bold text-gray-900 group-hover:text-[#F05323] transition-colors block">
                            {c.name}
                          </span>
                          <span className="text-[11px] text-gray-400 block font-mono">
                            {c.id}
                          </span>
                        </div>
                      </div>
                    </td>

                    {/* Email */}
                    <td className="px-6 py-4 font-mono text-gray-600">
                      {c.email}
                    </td>

                    {/* Role */}
                    <td className="px-6 py-4">
                      <span className="text-gray-700 font-medium">{c.role}</span>
                    </td>

                    {/* Assessment Status */}
                    <td className="px-6 py-4">
                      {getStatusBadge(c.displayStatus)}
                    </td>

                    {/* Action Buttons */}
                    <td className="px-6 py-4 text-right">
                      <div className="inline-flex items-center gap-3">
                        <div
                          className="inline-flex items-center gap-1 font-semibold text-[#F05323] group-hover:translate-x-0.5 transition-transform"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(
                              `/dashboard/workspaces/${workspaceId}/candidates/${c.id}`
                            );
                          }}
                        >
                          <span>View Details</span>
                          <ChevronRight className="w-3.5 h-3.5" />
                        </div>

                        <button
                          type="button"
                          title="Remove candidate from workspace"
                          onClick={(e) => handleRemoveCandidate(e, c.id, c.name)}
                          className="p-1 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* 4. Add Candidate Modal */}
      {isAddCandidateModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl border border-gray-100 space-y-5 animate-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div className="flex items-center gap-2.5">
                <div className="w-10 h-10 rounded-xl bg-orange-50 text-[#F05323] flex items-center justify-center">
                  <UserPlus className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-lg font-extrabold text-gray-900">
                    Add Candidate to {workspace.name}
                  </h3>
                  <p className="text-xs text-gray-500">
                    Click a candidate to select and enroll them in this workspace.
                  </p>
                </div>
              </div>

              <button
                onClick={() => {
                  setIsAddCandidateModalOpen(false);
                  setStatusMessage(null);
                }}
                className="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-gray-500 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Mode Switcher Tabs (Search Existing vs Create Profile) */}
            <div className="flex p-1 bg-gray-100 rounded-xl">
              <button
                type="button"
                onClick={() => {
                  setShowCreateTab(false);
                  setStatusMessage(null);
                }}
                className={`flex-1 py-1.5 text-xs font-bold rounded-lg transition-all ${
                  !showCreateTab
                    ? "bg-white text-gray-900 shadow-2xs"
                    : "text-gray-500 hover:text-gray-900"
                }`}
              >
                Search Existing Candidates
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowCreateTab(true);
                  setStatusMessage(null);
                }}
                className={`flex-1 py-1.5 text-xs font-bold rounded-lg transition-all ${
                  showCreateTab
                    ? "bg-white text-[#F05323] shadow-2xs"
                    : "text-gray-500 hover:text-gray-900"
                }`}
              >
                Add New Candidate Profile
              </button>
            </div>

            {/* Alert */}
            {statusMessage && (
              <div
                className={`p-3 rounded-xl border text-xs font-medium ${
                  statusMessage.type === "success"
                    ? "bg-emerald-50 border-emerald-200 text-emerald-700"
                    : "bg-rose-50 border-rose-200 text-rose-700"
                }`}
              >
                {statusMessage.text}
              </div>
            )}

            {!showCreateTab ? (
              /* Search Existing Candidate Tab */
              <div className="space-y-4">
                {/* Search Bar */}
                <div className="relative">
                  <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
                  <Input
                    type="text"
                    placeholder="Search candidate by name or email (e.g. Priya, priya@gmail.com)..."
                    value={candidateSearchQuery}
                    onChange={(e) => setCandidateSearchQuery(e.target.value)}
                    className="pl-9 text-xs"
                    autoFocus
                  />
                </div>

                {/* Search Results List */}
                <div className="max-h-64 overflow-y-auto space-y-2 pr-1">
                  {searchableCandidates.length === 0 ? (
                    <div className="py-8 text-center text-xs text-gray-400 space-y-2">
                      <p>No candidates found matching "{candidateSearchQuery}".</p>
                      <button
                        type="button"
                        onClick={() => {
                          setShowCreateTab(true);
                          setNewCandidateEmail(candidateSearchQuery);
                        }}
                        className="text-[#F05323] font-bold hover:underline"
                      >
                        Create candidate profile instead →
                      </button>
                    </div>
                  ) : (
                    searchableCandidates.map((cand) => {
                      const isAlreadyInWs = workspace.candidateIds.includes(cand.id);
                      const isSelected = selectedCandidateId === cand.id;

                      return (
                        <div
                          key={cand.id}
                          onClick={() => handleSelectCandidate(cand.id)}
                          className={`p-3.5 rounded-2xl border transition-all flex items-center justify-between cursor-pointer ${
                            isSelected
                              ? "bg-orange-50/90 border-[#F05323] ring-2 ring-[#F05323]/20 shadow-xs"
                              : "bg-white border-gray-200 hover:border-orange-300 hover:bg-orange-50/30"
                          }`}
                        >
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
                              <span className="font-bold text-xs text-gray-900 block leading-tight">
                                {cand.name}
                              </span>
                              <span className="text-[11px] text-gray-500 font-mono">
                                {cand.email}
                              </span>
                              <span className="text-[10px] text-gray-400 block mt-0.5">
                                {cand.role}
                              </span>
                            </div>
                          </div>

                          <div className="flex items-center gap-2">
                            {isAlreadyInWs && (
                              <span className="text-[10px] font-bold text-gray-500 bg-gray-100 border border-gray-200 px-2 py-0.5 rounded-full">
                                In Workspace
                              </span>
                            )}

                            {/* Radio / Check Selection Indicator */}
                            <div
                              className={`w-5 h-5 rounded-full flex items-center justify-center transition-all ${
                                isSelected
                                  ? "bg-[#F05323] text-white shadow-xs"
                                  : "border-2 border-gray-300 bg-white"
                              }`}
                            >
                              {isSelected && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                            </div>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>

                {/* Selected candidate notification */}
                {selectedCandidateId && (
                  <div className="p-2.5 rounded-xl bg-orange-50/60 border border-orange-200/80 text-xs text-gray-700 flex items-center justify-between">
                    <span>
                      Selected: <strong>{globalCandidates.find((c) => c.id === selectedCandidateId)?.name}</strong>
                    </span>
                    <span className="text-[11px] text-[#F05323] font-semibold">
                      Ready to add
                    </span>
                  </div>
                )}

                {/* Footer Buttons */}
                <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setIsAddCandidateModalOpen(false)}
                    className="text-xs font-semibold"
                  >
                    Cancel
                  </Button>
                  <Button
                    type="button"
                    disabled={!selectedCandidateId}
                    onClick={handleAddExistingCandidate}
                    size="sm"
                    className="text-xs font-semibold gap-2 bg-[#F05323] hover:bg-[#d94417] text-white shadow-xs cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <UserPlus className="w-3.5 h-3.5" />
                    Add Candidate to Workspace
                  </Button>
                </div>
              </div>
            ) : (
              /* Create New Candidate Profile Form */
              <form onSubmit={handleCreateAndAddCandidate} className="space-y-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-gray-700 block">
                    Full Name <span className="text-[#F05323]">*</span>
                  </label>
                  <Input
                    type="text"
                    required
                    placeholder="e.g. Priya S"
                    value={newCandidateName}
                    onChange={(e) => setNewCandidateName(e.target.value)}
                    icon={<User className="w-4 h-4 text-gray-400" />}
                    className="text-xs"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-gray-700 block">
                    Email Address <span className="text-[#F05323]">*</span>
                  </label>
                  <Input
                    type="email"
                    required
                    placeholder="e.g. priya@gmail.com"
                    value={newCandidateEmail}
                    onChange={(e) => setNewCandidateEmail(e.target.value)}
                    icon={<Mail className="w-4 h-4 text-gray-400" />}
                    className="text-xs"
                  />
                  <p className="text-[11px] text-gray-400">
                    Candidate will be matched or registered with this unique email address.
                  </p>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-gray-700 block">
                    Role / Position
                  </label>
                  <Input
                    type="text"
                    placeholder="e.g. Java Software Engineer"
                    value={newCandidateRole}
                    onChange={(e) => setNewCandidateRole(e.target.value)}
                    className="text-xs"
                  />
                </div>

                {/* Footer Buttons */}
                <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setIsAddCandidateModalOpen(false)}
                    className="text-xs font-semibold"
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    size="sm"
                    className="text-xs font-semibold gap-2 bg-[#F05323] hover:bg-[#d94417] text-white shadow-xs"
                  >
                    <UserPlus className="w-3.5 h-3.5" />
                    Create & Add Candidate
                  </Button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
