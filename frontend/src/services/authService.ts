import { apiClient } from "@/lib/apiClient";
import { UserRole } from "@/store/authStore";

export interface AuthResponseData {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  token: string;
  authProvider?: "LOCAL" | "GOOGLE" | "GITHUB";
  avatarUrl?: string;
}

export const authService = {
  /**
   * Unified single sign-in for all user roles.
   */
  login: async (email: string, password: string): Promise<AuthResponseData> => {
    return await apiClient.post("/auth/login", { email, password });
  },

  /**
   * Unified registration endpoint with user-selected role.
   */
  signup: async (
    name: string,
    email: string,
    password: string,
    role: UserRole
  ): Promise<AuthResponseData> => {
    return await apiClient.post(`/auth/signup?role=${role}`, { name, email, password });
  },

  /**
   * Unified registration endpoint accepting object payload.
   */
  register: async (payload: {
    name: string;
    email: string;
    password: string;
    role: UserRole;
  }): Promise<AuthResponseData> => {
    return await apiClient.post(`/auth/signup?role=${payload.role}`, {
      name: payload.name,
      email: payload.email,
      password: payload.password,
    });
  },

  /**
   * Google OAuth2 ID Token login / signup endpoint.
   */
  googleAuth: async (credential: string, role?: UserRole): Promise<AuthResponseData> => {
    return await apiClient.post("/auth/google", { credential, role });
  },

  /**
   * Fetches the current session profile from backend JWT.
   */
  getMe: async (): Promise<AuthResponseData> => {
    return await apiClient.get("/auth/me");
  },
};