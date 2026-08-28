import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

export type AssessmentDisplayStatus = "Not Taken" | "Assigned" | "Scheduled" | "Taken";

export interface HRCandidate {
  id: string;
  name: string;
  email: string;
  phone?: string;
  role: string;
  avatarUrl?: string;
  isSelected?: boolean;
  selectionStatus?: "SELECTED" | "SHORTLISTED" | "OFFER_EXTENDED" | "UNDER_REVIEW";
  selectedDate?: string;
  latestScore?: number | null;
}

export interface HRAssessment {
  id: string;
  workspaceId: string;
  candidateId: string;
  title: string;
  category: string;
  repositoryUrl: string;
  branchName: string;
  backendRootDirectory: string;
  difficulty: "EASY" | "INTERMEDIATE" | "DIFFICULT";
  durationMinutes: number;
  scheduledDate: string; // e.g. "25 August 2026"
  scheduledTime: string; // e.g. "10:30 AM"
  scheduledStartAt?: string;
  scheduledEndAt?: string;
  status: "NOT_ASSIGNED" | "ASSIGNED" | "SCHEDULED" | "IN_PROGRESS" | "COMPLETED";
  score?: number | null;
  totalTests?: number;
  passedTests?: number;
  failedTests?: number;
  completedAt?: string | null;
}

export interface HRWorkspace {
  id: string;
  name: string;
  description: string;
  track: string;
  defaultDurationMinutes: number;
  status: "ACTIVE" | "ARCHIVED";
  createdAt: string;
  candidateIds: string[];
}

interface HRStoreState {
  workspaces: HRWorkspace[];
  candidates: HRCandidate[];
  assessments: HRAssessment[];
  activeCandidateId: string;

  // Actions
  setActiveCandidateId: (id: string) => void;

  setWorkspaces: (workspaces: HRWorkspace[]) => void;
  resetStore: () => void;

  createWorkspace: (data: {
    id?: string;
    name: string;
    description: string;
    track?: string;
    defaultDurationMinutes?: number;
  }) => HRWorkspace;

  addCandidateToWorkspace: (workspaceId: string, candidateId: string) => boolean;

  removeCandidateFromWorkspace: (workspaceId: string, candidateId: string) => boolean;

  createAndAddCandidate: (
    workspaceId: string,
    candidate: { id?: string; name: string; email: string; role: string; phone?: string }
  ) => HRCandidate;

  assignAssessment: (data: {
    id?: string;
    workspaceId: string;
    candidateId: string;
    title: string;
    category?: string;
    repositoryUrl: string;
    branchName?: string;
    backendRootDirectory?: string;
    difficulty: "EASY" | "INTERMEDIATE" | "DIFFICULT";
    durationMinutes: number;
    scheduledDate: string;
    scheduledTime: string;
    scheduledStartAt?: string;
    scheduledEndAt?: string;
    status?: any;
    score?: number;
  }) => HRAssessment;

  updateSelectionStatus: (
    candidateId: string,
    isSelected: boolean,
    status?: "SELECTED" | "SHORTLISTED" | "OFFER_EXTENDED" | "UNDER_REVIEW"
  ) => void;

  // Selectors
  getWorkspaceById: (id: string) => HRWorkspace | undefined;
  getCandidatesForWorkspace: (
    workspaceId: string
  ) => Array<HRCandidate & { assessment?: HRAssessment; displayStatus: AssessmentDisplayStatus }>;
  getCandidateById: (id: string) => HRCandidate | undefined;
  getCandidateByEmail: (email: string) => HRCandidate | undefined;
  getCandidateAssessment: (workspaceId: string, candidateId: string) => HRAssessment | undefined;
  getCandidateAssessmentsInWorkspace: (workspaceId?: string, candidateId?: string) => HRAssessment[];
  getCandidateAssessments: (candidateId: string) => {
    completed: Array<HRAssessment & { workspaceName?: string }>;
    scheduled: Array<HRAssessment & { workspaceName?: string }>;
  };
  getAllSelectedCandidates: () => Array<
    HRCandidate & { workspaceName?: string; assessment?: HRAssessment }
  >;
  getDashboardMetrics: () => {
    totalWorkspaces: number;
    totalCandidates: number;
    candidatesAssigned: number;
    completedAssessments: number;
    selectedCandidates: number;
    avgScore: number;
  };
}

const generateUUID = (): string => {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
};

export const useHRStore = create<HRStoreState>()(
  persist(
    (set, get) => ({
      workspaces: [],
      candidates: [],
      assessments: [],
      activeCandidateId: "",

      setActiveCandidateId: (id) => set({ activeCandidateId: id }),

      setWorkspaces: (workspacesList) => set({ workspaces: workspacesList }),

      resetStore: () =>
        set({
          workspaces: [],
          candidates: [],
          assessments: [],
          activeCandidateId: "",
        }),

      createWorkspace: (data) => {
        const newWs: HRWorkspace = {
          id: data.id || generateUUID(),
          name: data.name.trim(),
          description: data.description.trim(),
          track: data.track || "Java Spring Boot Backend",
          defaultDurationMinutes: data.defaultDurationMinutes || 90,
          status: "ACTIVE",
          createdAt: new Date().toISOString().split("T")[0],
          candidateIds: [],
        };

        set((state) => ({
          workspaces: [newWs, ...state.workspaces],
        }));

        return newWs;
      },

      addCandidateToWorkspace: (workspaceId, candidateId) => {
        const ws = get().workspaces.find((w) => w.id === workspaceId);
        if (!ws) return false;

        set((state) => ({
          workspaces: state.workspaces.map((w) =>
            w.id === workspaceId
              ? {
                  ...w,
                  candidateIds: w.candidateIds.includes(candidateId)
                    ? w.candidateIds
                    : [...w.candidateIds, candidateId],
                }
              : w
          ),
        }));

        return true;
      },

      removeCandidateFromWorkspace: (workspaceId, candidateId) => {
        set((state) => ({
          workspaces: state.workspaces.map((w) =>
            w.id === workspaceId
              ? {
                  ...w,
                  candidateIds: w.candidateIds.filter((id) => id !== candidateId),
                }
              : w
          ),
        }));
        return true;
      },

      createAndAddCandidate: (workspaceId, candidateData) => {
        const candidateId = candidateData.id || generateUUID();
        const existingCand = get().candidates.find(
          (c) => c.id === candidateId || c.email.toLowerCase() === candidateData.email.trim().toLowerCase()
        );

        const newCand: HRCandidate = existingCand || {
          id: candidateId,
          name: candidateData.name.trim(),
          email: candidateData.email.trim().toLowerCase(),
          phone: candidateData.phone || "+91 98000 00000",
          role: candidateData.role || "Java Backend Developer",
          avatarUrl: `https://images.unsplash.com/photo-${1530000000000 + Math.floor(Math.random() * 100000)}?w=100&auto=format&fit=crop&q=80`,
          isSelected: false,
        };

        set((state) => ({
          candidates: existingCand
            ? state.candidates
            : [newCand, ...state.candidates],
          workspaces: state.workspaces.map((w) =>
            w.id === workspaceId
              ? {
                  ...w,
                  candidateIds: w.candidateIds.includes(newCand.id)
                    ? w.candidateIds
                    : [...w.candidateIds, newCand.id],
                }
              : w
          ),
        }));

        return newCand;
      },

      assignAssessment: (data) => {
        const existingIndex = data.id
          ? get().assessments.findIndex((a) => a.id === data.id)
          : -1;

        const newAssessment: HRAssessment = {
          id: data.id || generateUUID(),
          workspaceId: data.workspaceId,
          candidateId: data.candidateId,
          title: data.title || "Java Spring Boot Backend Assessment",
          category: data.category || "Spring Boot REST API",
          repositoryUrl: data.repositoryUrl || "https://github.com/scanurag/FoodFrenzy.git",
          branchName: data.branchName || "master",
          backendRootDirectory: data.backendRootDirectory || "",
          difficulty: data.difficulty || "INTERMEDIATE",
          durationMinutes: data.durationMinutes || 90,
          scheduledDate: data.scheduledDate,
          scheduledTime: data.scheduledTime,
          scheduledStartAt: data.scheduledStartAt,
          scheduledEndAt: data.scheduledEndAt,
          status: (data as any).status || "SCHEDULED",
          score: (data as any).score,
        };

        set((state) => {
          let updatedList = [...state.assessments];
          if (existingIndex >= 0) {
            updatedList[existingIndex] = newAssessment;
          } else {
            updatedList = [newAssessment, ...updatedList];
          }
          return { assessments: updatedList };
        });

        return newAssessment;
      },

      updateSelectionStatus: (candidateId, isSelected, status = "SELECTED") => {
        set((state) => ({
          candidates: state.candidates.map((c) =>
            c.id === candidateId
              ? {
                  ...c,
                  isSelected,
                  selectionStatus: isSelected ? status : undefined,
                  selectedDate: isSelected ? new Date().toISOString().split("T")[0] : undefined,
                }
              : c
          ),
        }));
      },

  getWorkspaceById: (id) => {
    return get().workspaces.find((w) => w.id === id);
  },

  getCandidatesForWorkspace: (workspaceId) => {
    const ws = get().workspaces.find((w) => w.id === workspaceId);
    if (!ws) return [];

    return ws.candidateIds.map((candId) => {
      const cand = get().candidates.find((c) => c.id === candId) || {
        id: candId,
        name: "Unknown Candidate",
        email: "unknown@example.com",
        role: "Software Developer",
      };

      const asmt = get().assessments.find(
        (a) => a.workspaceId === workspaceId && a.candidateId === candId
      );

      let displayStatus: AssessmentDisplayStatus = "Not Taken";
      if (asmt) {
        if (asmt.status === "COMPLETED") displayStatus = "Taken";
        else if (asmt.status === "SCHEDULED") displayStatus = "Scheduled";
        else if (asmt.status === "ASSIGNED") displayStatus = "Assigned";
        else if (asmt.status === "IN_PROGRESS") displayStatus = "Assigned";
      }

      return {
        ...cand,
        assessment: asmt,
        displayStatus,
      };
    });
  },

  getCandidateById: (id) => {
    return get().candidates.find((c) => c.id === id);
  },

  getCandidateByEmail: (email) => {
    return get().candidates.find(
      (c) => c.email.toLowerCase() === email.toLowerCase()
    );
  },

  getCandidateAssessment: (workspaceId, candidateId) => {
    const candidateAssessments = get().assessments.filter(
      (a) => (!workspaceId || a.workspaceId === workspaceId) && a.candidateId === candidateId
    );
    return candidateAssessments[0] || undefined;
  },

  getCandidateAssessmentsInWorkspace: (workspaceId, candidateId) => {
    return get().assessments.filter(
      (a) => (!workspaceId || a.workspaceId === workspaceId) && (!candidateId || a.candidateId === candidateId)
    );
  },

  getCandidateAssessments: (candidateId) => {
    const candidateAssessments = get().assessments.filter(
      (a) => a.candidateId === candidateId
    );

    const completed = candidateAssessments
      .filter((a) => a.status === "COMPLETED")
      .map((a) => {
        const ws = get().workspaces.find((w) => w.id === a.workspaceId);
        return { ...a, workspaceName: ws ? ws.name : "Placement Drive" };
      });

    const scheduled = candidateAssessments
      .filter((a) => a.status !== "COMPLETED")
      .map((a) => {
        const ws = get().workspaces.find((w) => w.id === a.workspaceId);
        return { ...a, workspaceName: ws ? ws.name : "Placement Drive" };
      });

    return { completed, scheduled };
  },

  getAllSelectedCandidates: () => {
    const selected = get().candidates.filter((c) => c.isSelected);
    return selected.map((cand) => {
      const ws = get().workspaces.find((w) => w.candidateIds.includes(cand.id));
      const asmt = get().assessments.find((a) => a.candidateId === cand.id);

      return {
        ...cand,
        workspaceName: ws ? ws.name : "Placement Drive",
        assessment: asmt,
      };
    });
  },

  getDashboardMetrics: () => {
    const { workspaces, candidates, assessments } = get();

    const totalWorkspaces = workspaces.length;
    const totalCandidates = candidates.length;

    const assignedCandidateIds = new Set(assessments.map((a) => a.candidateId));
    const candidatesAssigned = assignedCandidateIds.size;

    const completedList = assessments.filter((a) => a.status === "COMPLETED");
    const completedAssessments = completedList.length;

    const selectedCandidates = candidates.filter((c) => c.isSelected).length;

    const scoredList = completedList.filter((a) => typeof a.score === "number");
    const avgScore = scoredList.length > 0
      ? Number((scoredList.reduce((acc, a) => acc + (a.score || 0), 0) / scoredList.length).toFixed(1))
      : 0;

    return {
      totalWorkspaces,
      totalCandidates,
      candidatesAssigned,
      completedAssessments,
      selectedCandidates,
      avgScore,
    };
  },
}),
    {
      name: "evidence-hr-live-db",
      storage: createJSONStorage(() => localStorage),
    }
  )
);

