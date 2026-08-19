import { ApiResponse, AuthResponse, User } from "@/types";

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const mockAuthService = {
  login: async (
    email: string,
    _password: string
  ): Promise<ApiResponse<AuthResponse>> => {
    await delay(450);
    const isRecruiter = !email.includes("candidate") && !email.includes("aarav");
    const user: User = {
      id: isRecruiter ? "recruiter-001" : "cand-001",
      name: isRecruiter ? "Rahul Sharma" : "Aarav Patel",
      email: email || (isRecruiter ? "recruiter@example.com" : "aarav.patel@example.com"),
      role: isRecruiter ? "RECRUITER" : "CANDIDATE",
      company: isRecruiter ? "EVIDENCE Corp" : undefined,
      avatarUrl: isRecruiter
        ? "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"
        : "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
    };

    return {
      success: true,
      message: "Login successful",
      data: {
        user,
        token: "mock-jwt-auth-bearer-token-12345",
      },
      timestamp: new Date().toISOString(),
    };
  },

  signup: async (
    name: string,
    email: string,
    role: "RECRUITER" | "CANDIDATE",
    company?: string
  ): Promise<ApiResponse<AuthResponse>> => {
    await delay(500);
    const user: User = {
      id: "user-" + Math.random().toString(36).substring(7),
      name: name || "New User",
      email: email || "user@example.com",
      role,
      company,
    };

    return {
      success: true,
      message: "Account created successfully",
      data: {
        user,
        token: "mock-jwt-auth-bearer-token-12345",
      },
      timestamp: new Date().toISOString(),
    };
  },

  me: async (): Promise<ApiResponse<User>> => {
    await delay(200);
    return {
      success: true,
      message: "User session valid",
      data: {
        id: "recruiter-001",
        name: "Rahul Sharma",
        email: "recruiter@example.com",
        role: "RECRUITER",
        company: "EVIDENCE Corp",
      },
      timestamp: new Date().toISOString(),
    };
  },
};
