import { useState, useEffect, useCallback } from "react";
import { useParams, Link } from "react-router-dom";
import { useHRStore } from "@/store/hrStore";
import { assessmentService } from "@/services/assessmentService";
import { workspaceService } from "@/services/workspaceService";
import { candidateService } from "@/services/candidateService";
import { reportService } from "@/services/reportService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Calendar,
  Clock,
  CheckCircle2,
  FolderGit2,
  FileCheck2,
  PlusCircle,
  X,
  Loader2,
  ExternalLink,
  Check,
  Building2,
  Award,
  Sparkles,
  Binary,
} from "lucide-react";
import { AssessmentProcessingModal } from "@/components/dashboard/AssessmentProcessingModal";
import { AstAnalysisDrawer } from "@/components/dashboard/AstAnalysisDrawer";

export const CandidateDetailPage = () => {
  const { workspaceId = "", candidateId = "" } = useParams<{
    workspaceId: string;
    candidateId: string;
  }>();

  const {
    getWorkspaceById,
    getCandidateById,
    getCandidateAssessment,
    getCandidateAssessmentsInWorkspace,
    createWorkspace,
    createAndAddCandidate,
    assignAssessment,
    updateSelectionStatus,
  } = useHRStore();

  const workspace = getWorkspaceById(workspaceId);
  const candidate = getCandidateById(candidateId);
  const candidateAssessments = getCandidateAssessmentsInWorkspace(workspaceId, candidateId);
  const latestAssessment = candidateAssessments[0] || getCandidateAssessment(workspaceId, candidateId);

  const [isAddAssessmentModalOpen, setIsAddAssessmentModalOpen] = useState(false);
  const [isProcessingModalOpen, setIsProcessingModalOpen] = useState(false);
  const [isAstDrawerOpen, setIsAstDrawerOpen] = useState(false);
  const [activeInspectAssessment, setActiveInspectAssessment] = useState<any>(null);
  const [activeProgressAssessment, setActiveProgressAssessment] = useState<any>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSelectionLoading, setIsSelectionLoading] = useState(false);
  const [successToast, setSuccessToast] = useState("");
  const [lastCreatedAssessmentId, setLastCreatedAssessmentId] = useState<string | undefined>();

  // Add Assessment Form State
  const [title, setTitle] = useState("Java Spring Boot Backend Assessment");
  const [category, setCategory] = useState("Spring Boot REST API");
  const [repositoryUrl, setRepositoryUrl] = useState("https://github.com/scanurag/FoodFrenzy.git");
  const [branchName, setBranchName] = useState("master");
  const [backendRootDirectory, setBackendRootDirectory] = useState("");
  const [difficulty, setDifficulty] = useState<"EASY" | "INTERMEDIATE" | "DIFFICULT">("INTERMEDIATE");
  const [durationMinutes, setDurationMinutes] = useState(workspace?.defaultDurationMinutes || 90);
  const [scheduledDate, setScheduledDate] = useState(() => new Date().toISOString().split("T")[0]);
  const [scheduledTime, setScheduledTime] = useState("10:30");
  const [formError, setFormError] = useState("");

  // Fetch all assessments for this workspace and candidate
  const refreshWorkspaceAssessments = useCallback(async () => {
    if (!workspaceId) return;
    try {
      const asmtList = await assessmentService.getAssessmentsByWorkspace(workspaceId);
      if (Array.isArray(asmtList)) {
        asmtList.forEach((asmt: any) => {
          const cId = asmt.candidate?.id || asmt.candidateId;
          const asmtId = asmt.id || asmt.assessmentId;
          if (asmtId && (cId === candidateId || !candidateId || candidate?.email === asmt.candidate?.email)) {
            assignAssessment({
              id: asmtId,
              workspaceId,
              candidateId: candidateId || cId,
              title: asmt.title || "Java Spring Boot Technical Assessment",
              category: "Spring Boot REST API",
              repositoryUrl: asmt.repositoryUrl || "https://github.com/scanurag/FoodFrenzy.git",
              branchName: asmt.branchName || "master",
              backendRootDirectory: asmt.backendRootDirectory || "",
              difficulty: asmt.difficulty || "INTERMEDIATE",
              durationMinutes: asmt.durationMinutes || 90,
              status: asmt.status || "SCHEDULED",
              score: asmt.score,
              scheduledDate: asmt.scheduledStartAt ? new Date(asmt.scheduledStartAt).toLocaleDateString() : "Today",
              scheduledTime: asmt.scheduledStartAt ? new Date(asmt.scheduledStartAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : "10:00 AM",
              scheduledStartAt: asmt.scheduledStartAt,
              scheduledEndAt: asmt.scheduledEndAt,
            });
          }
        });
      }
    } catch (err) {
      console.debug("Backend assessment sync error:", err);
    }
  }, [workspaceId, candidateId, candidate?.email, assignAssessment]);

  // Sync workspace, candidates, and assessments from backend
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
        console.debug("Backend workspace sync:", err);
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
        console.debug("Backend candidates sync:", err);
      });

    // 3. Fetch all assessments
    refreshWorkspaceAssessments();

    // 4. Sync DB selection status for this candidate in this workspace
    reportService
      .getSelectedCandidates(workspaceId)
      .then((selectedList) => {
        if (isMounted && Array.isArray(selectedList)) {
          const matched = selectedList.find(
            (sc) =>
              sc.candidateId === candidateId ||
              (candidate?.email && sc.candidateEmail?.toLowerCase() === candidate.email.toLowerCase())
          );
          if (matched) {
            updateSelectionStatus(candidateId, true, (matched.selectionStatus as any) || "SELECTED");
          } else {
            updateSelectionStatus(candidateId, false);
          }
        }
      })
      .catch((err) => {
        console.debug("Backend selected candidates sync:", err);
      });

    return () => {
      isMounted = false;
    };
  }, [workspaceId, candidateId, candidate?.email, refreshWorkspaceAssessments, getWorkspaceById, createWorkspace, createAndAddCandidate, updateSelectionStatus]);

  if (!candidate || !workspace) {
    return (
      <div className="bg-white rounded-3xl border border-gray-200 p-12 text-center space-y-4">
        <h2 className="text-lg font-bold text-gray-900">Candidate Not Found</h2>
        <p className="text-xs text-gray-500">The requested candidate or workspace does not exist.</p>
        <Link to="/dashboard/workspaces">
          <Button size="sm" variant="outline">
            Back to Workspaces
          </Button>
        </Link>
      </div>
    );
  }

  const handleAssignAssessmentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError("");

    if (!title.trim() || !repositoryUrl.trim() || !scheduledDate.trim() || !scheduledTime.trim()) {
      setFormError("Please fill out all required fields.");
      return;
    }

    setIsSubmitting(true);

    try {
      let createdAsmtId: string | undefined;

      const isUuid = (str?: string) =>
        Boolean(str && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str));

      // Calculate start and end ISO times accurately from selected date & time
      let startDate: Date;
      if (scheduledDate && scheduledTime) {
        // Try ISO combined first: YYYY-MM-DDTHH:mm
        const isoCombined = new Date(`${scheduledDate}T${scheduledTime}`);
        if (!isNaN(isoCombined.getTime())) {
          startDate = isoCombined;
        } else {
          const spaceCombined = new Date(`${scheduledDate} ${scheduledTime}`);
          if (!isNaN(spaceCombined.getTime())) {
            startDate = spaceCombined;
          } else {
            startDate = new Date(scheduledDate);
            if (isNaN(startDate.getTime())) {
              startDate = new Date();
            }
          }
        }
      } else {
        startDate = new Date();
      }

      const scheduledStartAt = startDate.toISOString();
      const scheduledEndAt = new Date(startDate.getTime() + (Number(durationMinutes) || 90) * 60 * 1000).toISOString();
      let targetWsUuid = workspaceId;
      let targetCandidateUuid = candidateId;

      if (!isUuid(targetWsUuid)) {
        setFormError("Invalid workspace ID. Please select a valid workspace.");
        return;
      }

      if (candidate?.email) {
        try {
          const found = await candidateService.searchCandidateByEmail(candidate.email);
          if (found && found.id) {
            targetCandidateUuid = found.id;
          }
        } catch (searchErr) {
          console.debug("Candidate search by email:", searchErr);
        }
      }

      if (!isUuid(targetCandidateUuid)) {
        setFormError("Invalid candidate ID. Please select a valid registered candidate.");
        return;
      }

      const apiRes = await assessmentService.createAssessment(targetWsUuid, {
        candidateId: targetCandidateUuid,
        title: title.trim(),
        repositoryUrl: repositoryUrl.trim(),
        branchName: branchName.trim() || "master",
        backendRootDirectory: backendRootDirectory.trim() || "",
        difficulty,
        durationMinutes: Number(durationMinutes) || 90,
        scheduledStartAt,
        scheduledEndAt,
      });

      const asmtUuid = apiRes?.assessmentId || apiRes?.id;
      if (!asmtUuid) {
        throw new Error("Failed to create assessment on backend server.");
      }

      createdAsmtId = asmtUuid;
      setLastCreatedAssessmentId(asmtUuid);

      assignAssessment({
        id: createdAsmtId,
        workspaceId,
        candidateId,
        title,
        category,
        repositoryUrl,
        branchName,
        backendRootDirectory,
        difficulty,
        durationMinutes: Number(durationMinutes),
        status: "CREATING",
        scheduledDate,
        scheduledTime,
        scheduledStartAt,
        scheduledEndAt,
      });

      setActiveProgressAssessment({
        id: createdAsmtId,
        title,
        repositoryUrl,
        branchName,
        backendRootDirectory,
        status: "CREATING",
      });

      // Synchronize latest assessments from backend
      refreshWorkspaceAssessments();

      setSuccessToast("Assessment created! AI Generation Pipeline initiated in background.");
      setIsAddAssessmentModalOpen(false);
      setIsProcessingModalOpen(true);
      setTimeout(() => setSuccessToast(""), 3000);
    } catch (err: any) {
      console.error("Assessment creation error:", err);
      setFormError(err.message || "Failed to assign assessment. Please verify repository details.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleToggleSelection = async () => {
    const nextState = !candidate.isSelected;
    setIsSelectionLoading(true);
    try {
      if (nextState) {
        // Resolve the latest assessment UUID for this candidate in this workspace
        const latestAsmtId = latestAssessment?.id;
        // Resolve a real UUID for candidateId from the backend
        let resolvedCandidateId: string = candidateId;
        if (candidate?.email) {
          try {
            const found = await candidateService.searchCandidateByEmail(candidate.email);
            if (found && found.id) resolvedCandidateId = found.id;
          } catch {
            // keep candidateId from params
          }
        }

        await reportService.selectCandidate({
          workspaceId,
          candidateId: resolvedCandidateId,
          assessmentId: latestAsmtId,
          selectionStatus: "SELECTED",
        });
        updateSelectionStatus(candidate.id, true, "SELECTED");
        setSuccessToast(`${candidate.name} has been marked as Selected for hiring!`);
      } else {
        // Resolve real candidateId
        let resolvedCandidateId: string = candidateId;
        if (candidate?.email) {
          try {
            const found = await candidateService.searchCandidateByEmail(candidate.email);
            if (found && found.id) resolvedCandidateId = found.id;
          } catch {
            // keep candidateId from params
          }
        }
        await reportService.removeSelectedCandidateByWorkspaceAndCandidate(workspaceId, resolvedCandidateId);
        updateSelectionStatus(candidate.id, false);
        setSuccessToast(`${candidate.name} removed from Selected Candidates.`);
      }
    } catch (err: any) {
      console.error("Selection toggle error:", err);
      setSuccessToast(nextState
        ? `Failed to mark ${candidate.name} as selected. Please try again.`
        : `Failed to remove ${candidate.name} from selection.`);
    } finally {
      setIsSelectionLoading(false);
      setTimeout(() => setSuccessToast(""), 3000);
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Breadcrumbs & Header Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-xs font-medium text-gray-500 dark:text-slate-400">
          <Link
            to="/dashboard/workspaces"
            className="hover:text-gray-900 dark:hover:text-white transition-colors"
          >
            Workspaces
          </Link>
          <span>/</span>
          <Link
            to={`/dashboard/workspaces/${workspaceId}`}
            className="hover:text-gray-900 dark:hover:text-white transition-colors"
          >
            {workspace.name}
          </Link>
          <span>/</span>
          <span className="text-gray-900 dark:text-white font-bold">{candidate.name}</span>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={handleToggleSelection}
            disabled={isSelectionLoading}
            className={`text-xs font-semibold gap-1.5 transition-colors ${
              candidate.isSelected
                ? "border-emerald-300 dark:border-emerald-800 bg-emerald-50 dark:bg-emerald-950/50 text-emerald-700 dark:text-emerald-300 hover:bg-emerald-100 dark:hover:bg-emerald-900/60"
                : "border-gray-300 dark:border-slate-700 text-gray-700 dark:text-slate-300 hover:bg-gray-50 dark:hover:bg-slate-800"
            }`}
          >
            {isSelectionLoading ? (
              <Loader2 className="w-3.5 h-3.5 animate-spin" />
            ) : (
              <Check className="w-3.5 h-3.5" />
            )}
            {isSelectionLoading
              ? candidate.isSelected ? "Removing..." : "Selecting..."
              : candidate.isSelected ? "Selected Candidate" : "Mark as Selected"}
          </Button>
        </div>
      </div>

      {/* Success Toast */}
      {successToast && (
        <div className="p-3.5 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-200 dark:border-emerald-800 text-xs font-semibold text-emerald-800 dark:text-emerald-300 flex items-center justify-between shadow-xs animate-in slide-in-from-top duration-200">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400 flex-shrink-0" />
            <span>{successToast}</span>
          </div>
          <button onClick={() => setSuccessToast("")} className="text-emerald-600 dark:text-emerald-400 hover:text-emerald-900 dark:hover:text-emerald-200">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* 2. Candidate Profile Card */}
      <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-2xs transition-colors">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
          <div className="space-y-1">
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight">
                {candidate.name}
              </h1>
              {candidate.isSelected && (
                <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                  <Award className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
                  Selected for Hire
                </span>
              )}
            </div>
            <p className="text-xs text-gray-500 dark:text-slate-400 font-medium">{candidate.role}</p>

            <div className="flex flex-wrap items-center gap-4 pt-2 text-xs text-gray-600 dark:text-slate-300">
              <span className="font-mono bg-gray-100 dark:bg-slate-800 px-2.5 py-0.5 rounded-lg border border-gray-200/80 dark:border-slate-700">
                {candidate.email}
              </span>
              <span className="text-gray-400 dark:text-slate-500">•</span>
              <span>{candidate.phone || "+91 98765 43210"}</span>
              <span className="text-gray-400 dark:text-slate-500">•</span>
              <span className="inline-flex items-center gap-1 text-gray-700 dark:text-slate-300">
                <Building2 className="w-3.5 h-3.5 text-gray-400 dark:text-slate-500" />
                {workspace.name}
              </span>
            </div>
          </div>

          <div className="border-t sm:border-t-0 sm:border-l border-gray-100 dark:border-slate-800 pt-4 sm:pt-0 sm:pl-6">
            <span className="text-[11px] font-semibold text-gray-400 dark:text-slate-500 block uppercase tracking-wider">
              Assessment Status
            </span>
            <div className="mt-1">
              {latestAssessment?.status === "COMPLETED" ? (
                <div className="flex items-center gap-2">
                  <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400">
                    {latestAssessment.score}%
                  </span>
                  <span className="px-2.5 py-0.5 text-[11px] font-bold rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                    Taken & Completed
                  </span>
                </div>
              ) : (latestAssessment?.status as string) === "SCHEDULED" || (latestAssessment?.status as string) === "READY" ? (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-blue-50 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 border border-blue-200 dark:border-blue-800">
                  <Calendar className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
                  {latestAssessment.status} ({candidateAssessments.length})
                </span>
              ) : (latestAssessment?.status as string) === "ASSIGNED" || (latestAssessment?.status as string) === "IN_PROGRESS" ? (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-amber-50 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800">
                  <Clock className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400" />
                  Assigned ({candidateAssessments.length})
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-gray-100 dark:bg-slate-800 text-gray-600 dark:text-slate-400 border border-gray-200 dark:border-slate-700">
                  <span className="w-1.5 h-1.5 rounded-full bg-gray-400 dark:bg-slate-500"></span>
                  Not Assigned
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 3. Assessment Details Section */}
      <div className="bg-white dark:bg-slate-900 rounded-3xl border border-gray-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-2xs space-y-6 transition-colors">
        <div className="flex items-center justify-between pb-4 border-b border-gray-100 dark:border-slate-800">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">
                Technical Assessment Assignments
              </h3>
              <span className="px-2.5 py-0.5 text-xs font-extrabold rounded-full bg-primary-light dark:bg-primary/25 text-primary dark:text-primary">
                {candidateAssessments.length}
              </span>
            </div>
            <p className="text-xs text-gray-500 dark:text-slate-400">
              All project-specific Java Spring Boot coding assessments configured for {candidate.name}.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <Button
              size="sm"
              onClick={() => setIsAddAssessmentModalOpen(true)}
              className="gap-1.5 font-semibold bg-primary hover:bg-primary-hover text-white shadow-xs text-xs"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              Add Assessment
            </Button>
          </div>
        </div>

        {/* List of all candidate assessments */}
        {candidateAssessments.length > 0 ? (
          <div className="space-y-6">
            {candidateAssessments.map((asmt, idx) => (
              <div key={asmt.id || idx} className="bg-gray-50/70 dark:bg-slate-800/80 rounded-2xl border border-gray-200 dark:border-slate-700 p-6 space-y-5 transition-colors">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] font-bold uppercase tracking-wider text-primary dark:text-primary">
                        {asmt.category || "Spring Boot REST API"}
                      </span>
                      <span className="text-[10px] font-mono text-gray-400 dark:text-slate-500">
                        ID: {asmt.id}
                      </span>
                    </div>
                    <h4 className="text-xl font-extrabold text-gray-900 dark:text-white mt-0.5">
                      {asmt.title}
                    </h4>
                  </div>

                  <div className="flex items-center gap-2">
                    {["CREATING", "ANALYZING", "GENERATING_FEATURE", "GENERATING_TESTS"].includes(asmt.status) && (
                      <span className="text-xs font-bold px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-900/50 flex items-center gap-1.5 animate-pulse">
                        <span className="w-2 h-2 rounded-full bg-blue-500" />
                        AI Pipeline Running
                      </span>
                    )}
                    {asmt.status === "READY" && (
                      <span className="text-xs font-bold px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-900/50">
                        Pipeline Ready
                      </span>
                    )}
                    {asmt.status === "FAILED" && (
                      <span className="text-xs font-bold px-3 py-1 rounded-full bg-rose-50 dark:bg-rose-950/60 text-rose-700 dark:text-rose-400 border border-rose-200 dark:border-rose-900/50">
                        Pipeline Failed
                      </span>
                    )}
                    <span className="text-xs font-bold px-3 py-1 rounded-full bg-primary-light/70 dark:bg-primary/25 text-primary dark:text-primary border border-primary-border dark:border-primary/30">
                      {asmt.difficulty} Difficulty
                    </span>
                    <span className="text-xs font-bold px-3 py-1 rounded-full bg-gray-200/80 dark:bg-slate-700 text-gray-700 dark:text-slate-300">
                      {asmt.durationMinutes} mins
                    </span>
                  </div>
                </div>

                {/* Scheduled Date & Time Callout Box */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 bg-white dark:bg-slate-900 p-4 rounded-xl border border-gray-200 dark:border-slate-700 shadow-2xs">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400 flex items-center justify-center flex-shrink-0">
                      <Calendar className="w-5 h-5" />
                    </div>
                    <div>
                      <span className="text-[10px] uppercase font-bold text-gray-400 dark:text-slate-500 tracking-wider block">
                        Assessment Date
                      </span>
                      <span className="text-sm font-extrabold text-gray-900 dark:text-white block">
                        {asmt.scheduledDate}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-purple-50 dark:bg-purple-950/60 text-purple-600 dark:text-purple-400 flex items-center justify-center flex-shrink-0">
                      <Clock className="w-5 h-5" />
                    </div>
                    <div>
                      <span className="text-[10px] uppercase font-bold text-gray-400 dark:text-slate-500 tracking-wider block">
                        Assessment Time
                      </span>
                      <span className="text-sm font-extrabold text-gray-900 dark:text-white block">
                        {asmt.scheduledTime}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Repository Configuration Details */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs font-mono">
                  <div className="bg-white dark:bg-slate-900 p-3 rounded-xl border border-gray-200 dark:border-slate-700">
                    <span className="text-gray-400 dark:text-slate-500 block text-[10px] uppercase font-semibold">Repository URL</span>
                    <span className="text-gray-900 dark:text-slate-200 font-medium truncate block mt-0.5" title={asmt.repositoryUrl}>
                      {asmt.repositoryUrl}
                    </span>
                  </div>
                  <div className="bg-white dark:bg-slate-900 p-3 rounded-xl border border-gray-200 dark:border-slate-700">
                    <span className="text-gray-400 dark:text-slate-500 block text-[10px] uppercase font-semibold">Branch</span>
                    <span className="text-gray-900 dark:text-slate-200 font-medium block mt-0.5">
                      {asmt.branchName}
                    </span>
                  </div>
                  <div className="bg-white dark:bg-slate-900 p-3 rounded-xl border border-gray-200 dark:border-slate-700">
                    <span className="text-gray-400 dark:text-slate-500 block text-[10px] uppercase font-semibold">Backend Root Dir</span>
                    <span className="text-gray-900 dark:text-slate-200 font-medium block mt-0.5">
                      {asmt.backendRootDirectory || "(root)"}
                    </span>
                  </div>
                </div>

                {/* Action Bar for Inspection & Pipeline Progress */}
                <div className="flex flex-wrap items-center gap-3 pt-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      setActiveProgressAssessment(asmt);
                      setIsProcessingModalOpen(true);
                    }}
                    className={`text-xs font-bold gap-2 transition-all ${
                      ["CREATING", "ANALYZING", "GENERATING_FEATURE", "GENERATING_TESTS"].includes(asmt.status)
                        ? "border-primary-border dark:border-primary/30 bg-primary-light/90 dark:bg-primary/25 text-primary dark:text-primary hover:bg-primary-light shadow-xs"
                        : "border-gray-200 dark:border-slate-700 text-gray-700 dark:text-slate-300 hover:bg-gray-100 dark:hover:bg-slate-800"
                    }`}
                  >
                    {["CREATING", "ANALYZING", "GENERATING_FEATURE", "GENERATING_TESTS"].includes(asmt.status) ? (
                      <Loader2 className="w-3.5 h-3.5 text-primary animate-spin" />
                    ) : (
                      <Binary className="w-3.5 h-3.5 text-gray-500 dark:text-slate-400" />
                    )}
                    Inspect AI Pipeline
                  </Button>

                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      setActiveInspectAssessment(asmt);
                      setIsAstDrawerOpen(true);
                    }}
                    className="text-xs font-semibold gap-2 border-primary-border dark:border-primary/30 text-primary dark:text-primary hover:bg-primary-light dark:hover:bg-primary/20"
                  >
                    <Sparkles className="w-3.5 h-3.5 text-primary dark:text-primary" />
                    Inspect Codebase (AST) & AI Feature Spec
                  </Button>
                </div>

                {/* Evaluation Outcome if Completed */}
                {asmt.status === "COMPLETED" && (
                  <div className="bg-emerald-50/70 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-2xl p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <CheckCircle2 className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
                        <h5 className="text-sm font-bold text-emerald-950 dark:text-emerald-300">
                          Assessment Completed & Evaluated
                        </h5>
                      </div>
                      <p className="text-xs text-emerald-800 dark:text-emerald-400">
                        Objective evaluation score: <strong>{asmt.score} / 100</strong>.
                      </p>
                    </div>

                    <Link to={`/dashboard/candidates/${asmt.id}`}>
                      <Button size="sm" className="gap-1.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs">
                        View Full Technical Report <ExternalLink className="w-3.5 h-3.5" />
                      </Button>
                    </Link>
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          /* Empty Assessment State */
          <div className="p-12 text-center space-y-4 bg-gray-50/50 dark:bg-slate-800/50 rounded-2xl border border-dashed border-gray-300 dark:border-slate-700">
            <div className="w-12 h-12 rounded-2xl bg-primary-light dark:bg-primary/25 text-primary dark:text-primary flex items-center justify-center mx-auto">
              <FileCheck2 className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-gray-900 dark:text-white">
                No Assessment Assigned Yet
              </h4>
              <p className="text-xs text-gray-500 dark:text-slate-400 max-w-sm mx-auto mt-1">
                Configure a project-specific Java Spring Boot assessment and schedule the exact date and time for {candidate.name}.
              </p>
            </div>
            <Button
              size="sm"
              onClick={() => setIsAddAssessmentModalOpen(true)}
              className="gap-2 font-semibold bg-primary hover:bg-primary-hover text-white text-xs shadow-xs"
            >
              <PlusCircle className="w-4 h-4" />
              Add Assessment
            </Button>
          </div>
        )}
      </div>

      {/* 4. Add / Schedule Assessment Modal Form */}
      {isAddAssessmentModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-900 rounded-3xl max-w-xl w-full p-6 sm:p-8 shadow-2xl border border-gray-100 dark:border-slate-800 space-y-5 animate-in zoom-in-95 duration-200 max-h-[90vh] overflow-y-auto transition-colors">
            {/* Modal Header */}
            <div className="flex items-center justify-between pb-3 border-b border-gray-100 dark:border-slate-800">
              <div className="flex items-center gap-2.5">
                <div className="w-10 h-10 rounded-xl bg-primary-light dark:bg-primary/25 text-primary dark:text-primary flex items-center justify-center">
                  <FileCheck2 className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-lg font-extrabold text-gray-900 dark:text-white">
                    Schedule Assessment for {candidate.name}
                  </h3>
                  <p className="text-xs text-gray-500 dark:text-slate-400">
                    Configure repository, difficulty, and scheduled attendance window.
                  </p>
                </div>
              </div>

              <button
                onClick={() => setIsAddAssessmentModalOpen(false)}
                className="w-8 h-8 rounded-full bg-gray-100 dark:bg-slate-800 hover:bg-gray-200 dark:hover:bg-slate-700 flex items-center justify-center text-gray-500 dark:text-slate-400 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {formError && (
              <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-900 text-xs text-rose-700 dark:text-rose-400 font-medium">
                {formError}
              </div>
            )}

            <form onSubmit={handleAssignAssessmentSubmit} className="space-y-4">
              {/* Assessment Title */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-gray-700 dark:text-slate-300 block">
                  Assessment Title <span className="text-primary">*</span>
                </label>
                <Input
                  type="text"
                  required
                  placeholder="e.g. Java Spring Boot Notes API Assessment"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="text-xs bg-gray-50/70 dark:bg-slate-800 border-gray-200 dark:border-slate-700"
                />
              </div>

              {/* Category / Type */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-gray-700 dark:text-slate-300 block">
                  Assessment Type / Category
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className="flex h-10 w-full rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50/70 dark:bg-slate-800 px-3 text-xs text-gray-900 dark:text-slate-200 shadow-2xs focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
                >
                  <option value="Spring Boot REST API">Spring Boot REST API</option>
                  <option value="Microservices & Caching">Microservices & Caching</option>
                  <option value="Enterprise Spring Data JPA">Enterprise Spring Data JPA</option>
                  <option value="Fullstack API & Frontend">Fullstack API & Frontend</option>
                </select>
              </div>

              {/* Repository Configuration */}
              <div className="space-y-3 p-4 bg-gray-50/80 dark:bg-slate-800/80 rounded-2xl border border-gray-200 dark:border-slate-700">
                <span className="text-xs font-bold text-gray-800 dark:text-slate-200 flex items-center gap-1.5">
                  <FolderGit2 className="w-4 h-4 text-gray-500 dark:text-slate-400" />
                  GitHub Repository Configuration
                </span>

                <div className="space-y-1.5">
                  <label className="text-[11px] font-semibold text-gray-600 dark:text-slate-400 block">
                    Repository URL <span className="text-primary">*</span>
                  </label>
                  <Input
                    type="text"
                    required
                    placeholder="https://github.com/candidate/repo.git"
                    value={repositoryUrl}
                    onChange={(e) => setRepositoryUrl(e.target.value)}
                    className="text-xs font-mono bg-white dark:bg-slate-900 border-gray-200 dark:border-slate-700"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1.5">
                    <label className="text-[11px] font-semibold text-gray-600 dark:text-slate-400 block">
                      Branch Name
                    </label>
                    <Input
                      type="text"
                      placeholder="main"
                      value={branchName}
                      onChange={(e) => setBranchName(e.target.value)}
                      className="text-xs font-mono bg-white dark:bg-slate-900 border-gray-200 dark:border-slate-700"
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-[11px] font-semibold text-gray-600 dark:text-slate-400 block">
                      Backend Root Directory
                    </label>
                    <Input
                      type="text"
                      placeholder="backend"
                      value={backendRootDirectory}
                      onChange={(e) => setBackendRootDirectory(e.target.value)}
                      className="text-xs font-mono bg-white dark:bg-slate-900 border-gray-200 dark:border-slate-700"
                    />
                  </div>
                </div>
              </div>

              {/* Difficulty & Duration */}
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-gray-700 dark:text-slate-300 block">
                    Difficulty Level
                  </label>
                  <select
                    value={difficulty}
                    onChange={(e) =>
                      setDifficulty(
                        e.target.value as "EASY" | "INTERMEDIATE" | "DIFFICULT"
                      )
                    }
                    className="flex h-10 w-full rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50/70 dark:bg-slate-800 px-3 text-xs text-gray-900 dark:text-slate-200 shadow-2xs focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary"
                  >
                    <option value="EASY">EASY (30-60 mins)</option>
                    <option value="INTERMEDIATE">INTERMEDIATE (90 mins)</option>
                    <option value="DIFFICULT">DIFFICULT (120 mins)</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-gray-700 dark:text-slate-300 block">
                    Duration (Minutes)
                  </label>
                  <Input
                    type="number"
                    min={30}
                    max={240}
                    step={15}
                    value={durationMinutes}
                    onChange={(e) => setDurationMinutes(Number(e.target.value))}
                    className="text-xs bg-gray-50/70 dark:bg-slate-800 border-gray-200 dark:border-slate-700"
                  />
                </div>
              </div>

              {/* Assessment Scheduling (Date & Time Picker) */}
              <div className="space-y-3 p-4 bg-primary-light/50 dark:bg-primary/15 rounded-2xl border border-primary-border/70 dark:border-primary/30">
                <span className="text-xs font-bold text-primary dark:text-primary flex items-center gap-1.5">
                  <Calendar className="w-4 h-4" />
                  Assessment Attendance Schedule
                </span>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {/* Date Selector */}
                  <div className="space-y-1.5">
                    <label className="text-[11px] font-semibold text-gray-700 dark:text-slate-300 block">
                      Assessment Date <span className="text-primary">*</span>
                    </label>
                    <Input
                      type="date"
                      required
                      value={scheduledDate}
                      onChange={(e) => setScheduledDate(e.target.value)}
                      icon={<Calendar className="w-4 h-4 text-gray-400 dark:text-slate-500" />}
                      className="text-xs bg-white dark:bg-slate-900 border-gray-200 dark:border-slate-700"
                    />
                  </div>

                  {/* Time Selector */}
                  <div className="space-y-1.5">
                    <label className="text-[11px] font-semibold text-gray-700 dark:text-slate-300 block">
                      Assessment Time <span className="text-primary">*</span>
                    </label>
                    <Input
                      type="time"
                      required
                      value={scheduledTime}
                      onChange={(e) => setScheduledTime(e.target.value)}
                      icon={<Clock className="w-4 h-4 text-gray-400 dark:text-slate-500" />}
                      className="text-xs bg-white dark:bg-slate-900 border-gray-200 dark:border-slate-700"
                    />
                  </div>
                </div>
              </div>

              {/* Footer Buttons */}
              <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100 dark:border-slate-800">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setIsAddAssessmentModalOpen(false)}
                  className="text-xs font-semibold"
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  disabled={isSubmitting}
                  size="sm"
                  className="text-xs font-semibold gap-2 bg-primary hover:bg-primary-hover text-white shadow-xs"
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" /> Scheduling...
                    </>
                  ) : (
                    <>
                      <Calendar className="w-3.5 h-3.5" />
                      Assign & Schedule Assessment
                    </>
                  )}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Assessment Pipeline Processing Progress Modal */}
      <AssessmentProcessingModal
        isOpen={isProcessingModalOpen}
        onClose={() => {
          setIsProcessingModalOpen(false);
          setActiveProgressAssessment(null);
          refreshWorkspaceAssessments();
        }}
        assessmentTitle={activeProgressAssessment?.title || latestAssessment?.title || title}
        repositoryUrl={activeProgressAssessment?.repositoryUrl || latestAssessment?.repositoryUrl || repositoryUrl}
        assessmentId={activeProgressAssessment?.id || lastCreatedAssessmentId || latestAssessment?.id}
        onComplete={() => {
          refreshWorkspaceAssessments();
        }}
      />

      {/* AST Codebase & AI Feature Spec Inspector Drawer */}
      <AstAnalysisDrawer
        isOpen={isAstDrawerOpen}
        onClose={() => {
          setIsAstDrawerOpen(false);
          setActiveInspectAssessment(null);
        }}
        assessmentTitle={activeInspectAssessment?.title || latestAssessment?.title || title}
        assessmentId={activeInspectAssessment?.id || lastCreatedAssessmentId || latestAssessment?.id}
      />
    </div>
  );
};
