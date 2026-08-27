import { apiClient } from "@/lib/apiClient";
import { User, UserRole } from "@/types";

export interface AuthResponseData {
  user: User;
  token: string;
}

export const authService = {
  login: async (
    email: string,
    password: string,
    role: UserRole
  ): Promise<AuthResponseData> => {
    const endpoint =
      role === "CANDIDATE" ? "/auth/candidate/login" : "/auth/recruiter/login";
    return await apiClient.post(endpoint, { email, password });
  },

  signup: async (
    name: string,
    email: string,
    password: string,
    role: UserRole
  ): Promise<AuthResponseData> => {
    const endpoint =
      role === "CANDIDATE" ? "/auth/candidate/signup" : "/auth/recruiter/signup";
    return await apiClient.post(endpoint, { name, email, password });
  },
};
