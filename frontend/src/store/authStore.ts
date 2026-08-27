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
export const DEFAULT_RECRUITER: UserProfile = {
  id: "1d453ede-caab-4729-8072-9b6e3c600ba3",
  name: "Demo Recruiter",
  email: "recruiter@example.com",
  role: "RECRUITER",
  company: "EVIDENCE Corp",
  avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80",
};

export const DEFAULT_CANDIDATE: UserProfile = {
  id: "52127120-fa9e-43a1-958a-6a63ef9af8c5",
  name: "Arun Kumar",
  email: "arun@gmail.com",
  role: "CANDIDATE",
  avatarUrl: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
};

export const useAuthStore = create<AuthState>((set) => ({
  user: DEFAULT_RECRUITER,
  token: "jwt-session-token-1d453ede-caab-4729-8072-9b6e3c600ba3",
  isAuthenticated: true,
  login: (user, token) => set({ user, token, isAuthenticated: true }),
  logout: () => set({ user: null, token: null, isAuthenticated: false }),
  switchRole: (role) =>
    set(() => ({
      user: role === "RECRUITER" ? DEFAULT_RECRUITER : DEFAULT_CANDIDATE,
      token: role === "RECRUITER"
        ? "jwt-session-token-1d453ede-caab-4729-8072-9b6e3c600ba3"
        : "jwt-session-token-52127120-fa9e-43a1-958a-6a63ef9af8c5",
      isAuthenticated: true,
    })),
}));
