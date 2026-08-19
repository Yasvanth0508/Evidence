import { create } from "zustand";

export type UserRole = "RECRUITER" | "CANDIDATE";

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  avatarUrl?: string;
  company?: string;
}

interface AuthState {
  user: UserProfile | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (user: UserProfile, token: string) => void;
  logout: () => void;
  switchRole: (role: UserRole) => void;
}

// Default mock recruiter for frictionless developer experience matching the mockups
const DEFAULT_RECRUITER: UserProfile = {
  id: "recruiter-001",
  name: "Rahul Sharma",
  email: "recruiter@example.com",
  role: "RECRUITER",
  company: "EVIDENCE Corp",
  avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80",
};

export const useAuthStore = create<AuthState>((set) => ({
  user: DEFAULT_RECRUITER,
  token: "mock-jwt-token-xyz-123",
  isAuthenticated: true,
  login: (user, token) => set({ user, token, isAuthenticated: true }),
  logout: () => set({ user: null, token: null, isAuthenticated: false }),
  switchRole: (role) =>
    set((state) => ({
      user: state.user
        ? { ...state.user, role }
        : {
            id: "user-001",
            name: role === "RECRUITER" ? "Rahul Sharma" : "Aarav Patel",
            email: role === "RECRUITER" ? "recruiter@example.com" : "aarav.patel@example.com",
            role,
          },
    })),
}));
