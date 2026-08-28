import { create } from "zustand";
import { persist } from "zustand/middleware";

export type UserRole = "RECRUITER" | "CANDIDATE";

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  avatarUrl?: string;
  company?: string;
  authProvider?: "LOCAL" | "GOOGLE" | "GITHUB";
}

interface AuthState {
  user: UserProfile | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (user: UserProfile, token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (user: UserProfile, token: string) => {
        try {
          localStorage.removeItem("evidence-hr-live-db");
          localStorage.removeItem("evidence-candidate-live-db");
        } catch {}
        set({ user, token, isAuthenticated: true });
      },
      logout: () => {
        try {
          localStorage.removeItem("evidence-hr-live-db");
          localStorage.removeItem("evidence-candidate-live-db");
        } catch {}
        set({ user: null, token: null, isAuthenticated: false });
      },
    }),
    {
      name: "evidence_auth_session",
    }
  )
);