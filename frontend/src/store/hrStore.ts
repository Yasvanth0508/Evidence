import { create } from "zustand";

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

  createWorkspace: (data: {
    name: string;
    description: string;
    track?: string;
    defaultDurationMinutes?: number;
  }) => HRWorkspace;

  addCandidateToWorkspace: (workspaceId: string, candidateId: string) => boolean;

  removeCandidateFromWorkspace: (workspaceId: string, candidateId: string) => boolean;

  createAndAddCandidate: (
    workspaceId: string,
    candidate: { name: string; email: string; role: string; phone?: string }
  ) => HRCandidate;

  assignAssessment: (data: {
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

// Initial Mock Workspaces - Lean initialized so all candidates can be searched and added
const INITIAL_WORKSPACES: HRWorkspace[] = [
  {
    id: "ws-iit-bombay",
    name: "IIT Bombay",
    description: "Computer Science & Engineering Campus Drive 2026",
    track: "Java Spring Boot Backend",
    defaultDurationMinutes: 90,
    status: "ACTIVE",
    createdAt: "2026-08-10",
    candidateIds: ["cand-001"],
  },
  {
    id: "ws-nit-trichy",
    name: "NIT Trichy",
    description: "Backend & Systems Engineering Campus Placement Drive",
    track: "Java Microservices & REST APIs",
    defaultDurationMinutes: 90,
    status: "ACTIVE",
    createdAt: "2026-08-12",
    candidateIds: ["cand-004"],
  },
  {
    id: "ws-vit-vellore",
    name: "VIT Vellore",
    description: "Software Development Engineer (SDE-1) Hiring Drive",
    track: "Java Backend Development",
    defaultDurationMinutes: 60,
    status: "ACTIVE",
    createdAt: "2026-08-14",
    candidateIds: ["cand-006"],
  },
  {
    id: "ws-bits-pilani",
    name: "BITS Pilani",
    description: "Core Platform, Cloud & Distributed Systems Hiring",
    track: "Java Spring Boot & Cloud APIs",
    defaultDurationMinutes: 120,
    status: "ACTIVE",
    createdAt: "2026-08-15",
    candidateIds: ["cand-007"],
  },
  {
    id: "ws-tcs-rec",
    name: "TCS - REC Placement Drive",
    description: "Enterprise Backend Developer placement assessment",
    track: "Spring Boot Microservices",
    defaultDurationMinutes: 90,
    status: "ACTIVE",
    createdAt: "2026-08-18",
    candidateIds: ["cand-008"],
  },
];

// Initial Mock Candidates
const INITIAL_CANDIDATES: HRCandidate[] = [
  {
    id: "cand-001",
    name: "Arun Kumar",
    email: "arun@gmail.com",
    phone: "+91 98765 43210",
    role: "Java Backend Developer",
    avatarUrl: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
    isSelected: true,
    selectionStatus: "SELECTED",
    selectedDate: "2026-08-16",
    latestScore: 88.5,
  },
  {
    id: "cand-002",
    name: "Priya S",
    email: "priya@gmail.com",
    phone: "+91 98765 43211",
    role: "Java Software Engineer",
    avatarUrl: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop&q=80",
    isSelected: false,
    latestScore: null,
  },
  {
    id: "cand-003",
    name: "Rahul M",
    email: "rahul@gmail.com",
    phone: "+91 98765 43212",
    role: "Backend Systems Developer",
    avatarUrl: "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=100&auto=format&fit=crop&q=80",
    isSelected: false,
    latestScore: null,
  },
  {
    id: "cand-004",
    name: "Sneha Rao",
    email: "sneha@gmail.com",
    phone: "+91 98765 43213",
    role: "Java Full Stack Developer",
    avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80",
    isSelected: true,
    selectionStatus: "OFFER_EXTENDED",
    selectedDate: "2026-08-18",
    latestScore: 92.0,
  },
  {
    id: "cand-005",
    name: "Vikram Malhotra",
    email: "vikram.m@gmail.com",
    phone: "+91 98765 43214",
    role: "Spring Boot Engineer",
    avatarUrl: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&auto=format&fit=crop&q=80",
    isSelected: false,
    latestScore: null,
  },
  {
    id: "cand-006",
    name: "Ananya Iyer",
    email: "ananya.i@gmail.com",
    phone: "+91 98765 43215",
    role: "API Platform Engineer",
    avatarUrl: "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100&auto=format&fit=crop&q=80",
    isSelected: true,
    selectionStatus: "SHORTLISTED",
    selectedDate: "2026-08-19",
    latestScore: 85.0,
  },
  {
    id: "cand-007",
    name: "Rohan Mehta",
    email: "rohan.mehta@example.com",
    phone: "+91 98765 43216",
    role: "Java Backend Developer",
    avatarUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&auto=format&fit=crop&q=80",
    isSelected: false,
    latestScore: null,
  },
  {
    id: "cand-008",
    name: "Aarav Patel",
    email: "aarav.patel@example.com",
    phone: "+91 98765 43217",
    role: "Full Stack Java Engineer",
    avatarUrl: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
    isSelected: true,
    selectionStatus: "SELECTED",
    selectedDate: "2026-08-17",
    latestScore: 94.5,
  },
  {
    id: "cand-009",
    name: "Neha Singh",
    email: "neha.singh@example.com",
    phone: "+91 98765 43218",
    role: "Backend Developer",
    avatarUrl: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&auto=format&fit=crop&q=80",
    isSelected: false,
    latestScore: null,
  },
  {
    id: "cand-010",
    name: "Karan Verma",
    email: "karan.verma@example.com",
    phone: "+91 98765 43219",
    role: "Microservices Developer",
    avatarUrl: "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=100&auto=format&fit=crop&q=80",
    isSelected: false,
    latestScore: null,
  },
];

// Initial Mock Assessments
const INITIAL_ASSESSMENTS: HRAssessment[] = [
  {
    id: "asmt-001",
    workspaceId: "ws-iit-bombay",
    candidateId: "cand-001",
    title: "Java Spring Boot Notes API Assessment",
    category: "Spring Boot REST API",
    repositoryUrl: "https://github.com/example/notes-app.git",
    branchName: "main",
    backendRootDirectory: "backend",
    difficulty: "INTERMEDIATE",
    durationMinutes: 90,
    scheduledDate: "25 August 2026",
    scheduledTime: "10:42 AM",
    scheduledStartAt: "2026-08-25T10:00:00+05:30",
    scheduledEndAt: "2026-08-25T11:30:00+05:30",
    status: "COMPLETED",
    score: 88.5,
    totalTests: 10,
    passedTests: 9,
    failedTests: 1,
    completedAt: "25 August 2026 10:42 AM",
  },
  {
    id: "asmt-002",
    workspaceId: "ws-iit-bombay",
    candidateId: "cand-001",
    title: "Java Spring Boot Microservices Assessment",
    category: "Microservices & Caching",
    repositoryUrl: "https://github.com/example/user-service.git",
    branchName: "main",
    backendRootDirectory: "backend",
    difficulty: "INTERMEDIATE",
    durationMinutes: 90,
    scheduledDate: "30 August 2026",
    scheduledTime: "10:00 AM",
    scheduledStartAt: "2026-08-30T10:00:00+05:30",
    scheduledEndAt: "2026-08-30T11:30:00+05:30",
    status: "SCHEDULED",
  },
  {
    id: "asmt-live-001",
    workspaceId: "ws-tcs-rec",
    candidateId: "cand-001",
    title: "Java Spring Boot Real-Time REST API Assessment",
    category: "Spring Boot Live Evaluation",
    repositoryUrl: "https://github.com/example/notes-app.git",
    branchName: "main",
    backendRootDirectory: "backend",
    difficulty: "INTERMEDIATE",
    durationMinutes: 90,
    scheduledDate: "Today (Live)",
    scheduledTime: "10:00 AM",
    scheduledStartAt: new Date(Date.now() - 60000).toISOString(),
    status: "SCHEDULED",
  },
  {
    id: "asmt-003",
    workspaceId: "ws-iit-bombay",
    candidateId: "cand-003",
    title: "Java Spring Boot Microservices Assessment",
    category: "Microservices & Cache",
    repositoryUrl: "https://github.com/example/user-service.git",
    branchName: "main",
    backendRootDirectory: "/",
    difficulty: "INTERMEDIATE",
    durationMinutes: 90,
    scheduledDate: "28 August 2026",
    scheduledTime: "11:00 AM",
    scheduledStartAt: "2026-08-28T11:00:00+05:30",
    scheduledEndAt: "2026-08-28T12:30:00+05:30",
    status: "SCHEDULED",
  },
  {
    id: "asmt-004",
    workspaceId: "ws-nit-trichy",
    candidateId: "cand-004",
    title: "Spring Boot REST Order Service Assessment",
    category: "Order Management API",
    repositoryUrl: "https://github.com/example/order-service.git",
    branchName: "main",
    backendRootDirectory: "order-backend",
    difficulty: "DIFFICULT",
    durationMinutes: 90,
    scheduledDate: "22 August 2026",
    scheduledTime: "02:00 PM",
    scheduledStartAt: "2026-08-22T14:00:00+05:30",
    scheduledEndAt: "2026-08-22T15:30:00+05:30",
    status: "COMPLETED",
    score: 92.0,
    totalTests: 10,
    passedTests: 10,
    failedTests: 0,
    completedAt: "22 August 2026 03:15 PM",
  },
  {
    id: "asmt-005",
    workspaceId: "ws-nit-trichy",
    candidateId: "cand-005",
    title: "Java Spring Boot Inventory Assessment",
    category: "Inventory Service",
    repositoryUrl: "https://github.com/example/inventory-app.git",
    branchName: "master",
    backendRootDirectory: "/",
    difficulty: "EASY",
    durationMinutes: 60,
    scheduledDate: "30 August 2026",
    scheduledTime: "03:30 PM",
    scheduledStartAt: "2026-08-30T15:30:00+05:30",
    scheduledEndAt: "2026-08-30T16:30:00+05:30",
    status: "SCHEDULED",
  },
  {
    id: "asmt-006",
    workspaceId: "ws-vit-vellore",
    candidateId: "cand-006",
    title: "Java Spring Boot API Platform Assessment",
    category: "Payment Gateway Service",
    repositoryUrl: "https://github.com/example/payment-gateway.git",
    branchName: "main",
    backendRootDirectory: "src",
    difficulty: "INTERMEDIATE",
    durationMinutes: 90,
    scheduledDate: "19 August 2026",
    scheduledTime: "10:00 AM",
    scheduledStartAt: "2026-08-19T10:00:00+05:30",
    scheduledEndAt: "2026-08-19T11:30:00+05:30",
    status: "COMPLETED",
    score: 85.0,
    totalTests: 10,
    passedTests: 8,
    failedTests: 2,
    completedAt: "19 August 2026 11:10 AM",
  },
  {
    id: "asmt-007",
    workspaceId: "ws-bits-pilani",
    candidateId: "cand-007",
    title: "Java Spring Boot Distributed Cache Assessment",
    category: "Distributed Caching Engine",
    repositoryUrl: "https://github.com/example/cache-engine.git",
    branchName: "main",
    backendRootDirectory: "/",
    difficulty: "DIFFICULT",
    durationMinutes: 120,
    scheduledDate: "29 August 2026",
    scheduledTime: "09:30 AM",
    scheduledStartAt: "2026-08-29T09:30:00+05:30",
    scheduledEndAt: "2026-08-29T11:30:00+05:30",
    status: "SCHEDULED",
  },
  {
    id: "asmt-008",
    workspaceId: "ws-tcs-rec",
    candidateId: "cand-008",
    title: "Java Spring Boot Enterprise Assessment",
    category: "E-Commerce Microservice",
    repositoryUrl: "https://github.com/example/ecommerce-api.git",
    branchName: "main",
    backendRootDirectory: "/",
    difficulty: "INTERMEDIATE",
    durationMinutes: 90,
    scheduledDate: "18 August 2026",
    scheduledTime: "10:00 AM",
    scheduledStartAt: "2026-08-18T10:00:00+05:30",
    scheduledEndAt: "2026-08-18T11:30:00+05:30",
    status: "COMPLETED",
    score: 94.5,
    totalTests: 10,
    passedTests: 10,
    failedTests: 0,
    completedAt: "18 August 2026 11:15 AM",
  },
];

export const useHRStore = create<HRStoreState>((set, get) => ({
  workspaces: INITIAL_WORKSPACES,
  candidates: INITIAL_CANDIDATES,
  assessments: INITIAL_ASSESSMENTS,
  activeCandidateId: "cand-001",

  setActiveCandidateId: (id) => set({ activeCandidateId: id }),

  createWorkspace: (data) => {
    const newWs: HRWorkspace = {
      id: "ws-" + Math.random().toString(36).substring(2, 9),
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
    const newCand: HRCandidate = {
      id: "cand-" + Math.random().toString(36).substring(2, 9),
      name: candidateData.name.trim(),
      email: candidateData.email.trim().toLowerCase(),
      phone: candidateData.phone || "+91 98000 00000",
      role: candidateData.role || "Java Backend Developer",
      avatarUrl: `https://images.unsplash.com/photo-${1530000000000 + Math.floor(Math.random() * 100000)}?w=100&auto=format&fit=crop&q=80`,
      isSelected: false,
    };

    set((state) => ({
      candidates: [newCand, ...state.candidates],
      workspaces: state.workspaces.map((w) =>
        w.id === workspaceId
          ? { ...w, candidateIds: [...w.candidateIds, newCand.id] }
          : w
      ),
    }));

    return newCand;
  },

  assignAssessment: (data) => {
    const existingIndex = get().assessments.findIndex(
      (a) => a.workspaceId === data.workspaceId && a.candidateId === data.candidateId
    );

    const newAssessment: HRAssessment = {
      id: existingIndex >= 0 ? get().assessments[existingIndex].id : "asmt-" + Math.random().toString(36).substring(2, 9),
      workspaceId: data.workspaceId,
      candidateId: data.candidateId,
      title: data.title || "Java Spring Boot Backend Assessment",
      category: data.category || "Spring Boot REST API",
      repositoryUrl: data.repositoryUrl || "https://github.com/example/backend-app.git",
      branchName: data.branchName || "main",
      backendRootDirectory: data.backendRootDirectory || "backend",
      difficulty: data.difficulty || "INTERMEDIATE",
      durationMinutes: data.durationMinutes || 90,
      scheduledDate: data.scheduledDate,
      scheduledTime: data.scheduledTime,
      status: "SCHEDULED",
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
    return get().assessments.find(
      (a) => a.workspaceId === workspaceId && a.candidateId === candidateId
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
      : 86.4;

    return {
      totalWorkspaces,
      totalCandidates,
      candidatesAssigned,
      completedAssessments,
      selectedCandidates,
      avgScore,
    };
  },
}));
