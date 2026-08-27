import { useState, useMemo, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useHRStore } from "@/store/hrStore";
import { useAuthStore } from "@/store/authStore";
import { dashboardService } from "@/services/dashboardService";
import { ScheduledAssessmentCard } from "@/components/candidate/ScheduledAssessmentCard";
import { CompletedAssessmentCard } from "@/components/candidate/CompletedAssessmentCard";
import { Button } from "@/components/ui/button";
import {
  Code2,
  CheckCircle2,
  Clock,
  LogOut,
  User,
} from "lucide-react";

export const CandidateDashboard = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const {
    candidates,
    activeCandidateId,
    setActiveCandidateId,
    getCandidateAssessments,
    getCandidateById,
    assignAssessment,
  } = useHRStore();

  useEffect(() => {
    let isMounted = true;
    dashboardService.getCandidateDashboard()
      .then((data) => {
        if (isMounted && data) {
          (data.scheduledAssessments || []).forEach((asmt) => {
            assignAssessment({
              id: asmt.assessmentId,
              workspaceId: "ws-live",
              candidateId: activeCandidateId,
              title: `${asmt.workspaceName} Assessment`,
              repositoryUrl: "https://github.com/scanurag/FoodFrenzy.git",
              branchName: "master",
              difficulty: (asmt.difficulty as any) || "INTERMEDIATE",
              durationMinutes: asmt.durationMinutes || 90,
              scheduledDate: new Date(asmt.scheduledStartAt).toLocaleDateString(),
              scheduledTime: new Date(asmt.scheduledStartAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            });
          });
        }
      })
      .catch((err) => {
        console.debug("Candidate dashboard sync skipped:", err);
      });
    return () => {
      isMounted = false;
    };
  }, [activeCandidateId]);

  // Find candidate profile matching active ID or logged-in user
  const activeCandidate = useMemo(() => {
    const fromId = activeCandidateId ? getCandidateById(activeCandidateId) : null;
    if (fromId) return fromId;
    const fromEmail = user?.email
      ? candidates.find((c) => c.email.toLowerCase() === user.email.toLowerCase())
      : null;
    if (fromEmail) return fromEmail;
    if (candidates.length > 0) return candidates[0];
    return {
      id: user?.id || "cand-current",
      name: user?.name || "Candidate",
      email: user?.email || "candidate@example.com",
      role: "Java Backend Developer",
      isSelected: false,
    };
  }, [activeCandidateId, user, candidates, getCandidateById]);

  // Fetch assessments for this candidate
  const { completed, scheduled } = useMemo(() => {
    if (!activeCandidate?.id) return { completed: [], scheduled: [] };
    return getCandidateAssessments(activeCandidate.id);
  }, [activeCandidate?.id, getCandidateAssessments]);

  const [activeTab, setActiveTab] = useState<"ALL" | "SCHEDULED" | "COMPLETED">("ALL");

  const handleLogout = () => {
    logout();
    navigate("/login?role=candidate");
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col font-sans">
      {/* 1. Candidate Portal Topbar */}
      <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100 shadow-2xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-xl bg-orange-50 text-[#F05323] flex items-center justify-center font-bold">
                <Code2 className="w-5 h-5" />
              </div>
              <span className="font-extrabold text-xl text-gray-900 tracking-tight">
                EVIDENCE
              </span>
            </Link>
            <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
              Candidate Portal
            </span>
          </div>

          {/* User Profile & Demo Candidate Switcher */}
          <div className="flex items-center gap-4">
            {/* Demo Candidate Switcher Dropdown */}
            <div className="hidden sm:flex items-center gap-1.5 text-xs text-gray-600 bg-gray-50 px-3 py-1.5 rounded-xl border border-gray-200">
              <User className="w-3.5 h-3.5 text-gray-400" />
              <span className="text-gray-400">Viewing as:</span>
              <select
                value={activeCandidate.id}
                onChange={(e) => setActiveCandidateId(e.target.value)}
                className="bg-transparent font-bold text-gray-900 focus:outline-none cursor-pointer"
              >
                {candidates.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.email})
                  </option>
                ))}
              </select>
            </div>

            {/* Logout button */}
            <Button
              variant="outline"
              size="sm"
              onClick={handleLogout}
              className="text-xs font-semibold text-gray-700 border-gray-200 hover:bg-rose-50 hover:text-rose-600"
            >
              <LogOut className="w-3.5 h-3.5 mr-1" /> Sign Out
            </Button>
          </div>
        </div>
      </header>

      {/* 2. Main Content Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Candidate Profile Header Card */}
        <div className="bg-white rounded-3xl border border-gray-200/90 p-6 sm:p-8 shadow-2xs">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div className="flex items-center gap-4">
              <img
                src={
                  activeCandidate.avatarUrl ||
                  "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80"
                }
                alt={activeCandidate.name}
                className="w-16 h-16 rounded-2xl object-cover border-2 border-orange-100 shadow-2xs flex-shrink-0"
              />
              <div className="space-y-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight">
                    Welcome, {activeCandidate.name}
                  </h1>
                  <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-orange-50 text-[#F05323] border border-orange-200">
                    Java Candidate
                  </span>
                </div>
                <p className="text-xs text-gray-500 font-medium font-mono">
                  {activeCandidate.email} • {activeCandidate.role}
                </p>
              </div>
            </div>

            {/* Quick Metrics */}
            <div className="flex items-center gap-4 border-t md:border-t-0 md:border-l border-gray-100 pt-4 md:pt-0 md:pl-6">
              <div>
                <span className="text-[10px] uppercase font-bold text-gray-400 block tracking-wider">
                  Scheduled
                </span>
                <span className="text-2xl font-black text-[#F05323] block">
                  {scheduled.length}
                </span>
              </div>
              <div className="h-8 w-px bg-gray-100"></div>
              <div>
                <span className="text-[10px] uppercase font-bold text-gray-400 block tracking-wider">
                  Completed
                </span>
                <span className="text-2xl font-black text-emerald-600 block">
                  {completed.length}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Section Navigation Tabs */}
        <div className="flex items-center gap-2 border-b border-gray-200 pb-2">
          <button
            onClick={() => setActiveTab("ALL")}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === "ALL"
                ? "bg-[#0F172A] text-white shadow-xs"
                : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
            }`}
          >
            All Assessments ({scheduled.length + completed.length})
          </button>
          <button
            onClick={() => setActiveTab("SCHEDULED")}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
              activeTab === "SCHEDULED"
                ? "bg-[#F05323] text-white shadow-xs"
                : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
            }`}
          >
            <Clock className="w-3.5 h-3.5" />
            Scheduled Assessments ({scheduled.length})
          </button>
          <button
            onClick={() => setActiveTab("COMPLETED")}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
              activeTab === "COMPLETED"
                ? "bg-emerald-600 text-white shadow-xs"
                : "text-gray-600 hover:text-gray-900 hover:bg-gray-100"
            }`}
          >
            <CheckCircle2 className="w-3.5 h-3.5" />
            Completed Assessments ({completed.length})
          </button>
        </div>

        {/* ---------------------------------------------------- */}
        {/* 3. Scheduled Assessments Section (Requirement 3)     */}
        {/* ---------------------------------------------------- */}
        {(activeTab === "ALL" || activeTab === "SCHEDULED") && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-lg bg-orange-100 text-[#F05323] flex items-center justify-center font-bold text-xs">
                  <Clock className="w-4 h-4" />
                </div>
                <div>
                  <h2 className="text-lg font-extrabold text-gray-900 tracking-tight">
                    Scheduled Assessments
                  </h2>
                  <p className="text-xs text-gray-500">
                    Live second-by-second countdown for upcoming technical evaluations.
                  </p>
                </div>
              </div>

              <span className="text-xs font-bold text-gray-400">
                {scheduled.length} Scheduled
              </span>
            </div>

            {scheduled.length === 0 ? (
              <div className="bg-white rounded-3xl border border-dashed border-gray-300 p-10 text-center space-y-2">
                <Clock className="w-10 h-10 text-gray-300 mx-auto" />
                <h4 className="text-sm font-bold text-gray-800">
                  No Pending Scheduled Assessments
                </h4>
                <p className="text-xs text-gray-500 max-w-sm mx-auto">
                  When recruiters schedule an assessment for your email, it will appear here with a real-time countdown timer.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {scheduled.map((asmt) => (
                  <ScheduledAssessmentCard key={asmt.id} assessment={asmt} />
                ))}
              </div>
            )}
          </div>
        )}

        {/* ---------------------------------------------------- */}
        {/* 4. Completed Assessments Section (Requirement 2)     */}
        {/* ---------------------------------------------------- */}
        {(activeTab === "ALL" || activeTab === "COMPLETED") && (
          <div className="space-y-4 pt-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold text-xs">
                  <CheckCircle2 className="w-4 h-4" />
                </div>
                <div>
                  <h2 className="text-lg font-extrabold text-gray-900 tracking-tight">
                    Completed Assessments
                  </h2>
                  <p className="text-xs text-gray-500">
                    Review your completed assessment results and evaluation scores.
                  </p>
                </div>
              </div>

              <span className="text-xs font-bold text-gray-400">
                {completed.length} Completed
              </span>
            </div>

            {completed.length === 0 ? (
              <div className="bg-white rounded-3xl border border-dashed border-gray-300 p-10 text-center space-y-2">
                <CheckCircle2 className="w-10 h-10 text-gray-300 mx-auto" />
                <h4 className="text-sm font-bold text-gray-800">
                  No Completed Assessments Yet
                </h4>
                <p className="text-xs text-gray-500 max-w-sm mx-auto">
                  Once you take and submit an assessment in the browser Monaco IDE, your score and evaluation details will appear here.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {completed.map((asmt) => (
                  <CompletedAssessmentCard key={asmt.id} assessment={asmt} />
                ))}
              </div>
            )}
          </div>
        )}
      </main>

      {/* 5. Footer */}
      <footer className="bg-white border-t border-gray-200 py-6 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-4">
          <p>© 2026 EVIDENCE. Recruiter & Candidate Technical Assessment Platform.</p>
          <div className="flex space-x-6">
            <Link to="/#overview" className="hover:text-gray-600">Home</Link>
            <Link to="/#how-it-works" className="hover:text-gray-600">How It Works</Link>
          </div>
        </div>
      </footer>
    </div>
  );
};
