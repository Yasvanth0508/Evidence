import axios, { AxiosError } from "axios";
import { useAuthStore } from "@/store/authStore";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api/v1";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

const isUuid = (id?: string) =>
  Boolean(id && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id));

// Request interceptor: attach User ID & Role headers for backend identification
apiClient.interceptors.request.use(
  (config) => {
    const user = useAuthStore.getState().user;
    if (user) {
      if (user.role === "RECRUITER" && isUuid(user.id)) {
        config.headers["X-Recruiter-Id"] = user.id;
      }
      if (user.role === "CANDIDATE" && isUuid(user.id)) {
        config.headers["X-Candidate-Id"] = user.id;
      }
      if (isUuid(user.id)) {
        config.headers["X-User-Id"] = user.id;
      }
      config.headers["X-User-Role"] = user.role;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: unwrap backend ApiResponse<T> or handle errors
apiClient.interceptors.response.use(
  (response) => {
    // If backend returns ApiResponse envelope, return the .data payload if present
    if (response.data && typeof response.data === "object" && "data" in response.data) {
      return response.data.data;
    }
    return response.data;
  },
  (error: AxiosError<any>) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      "An unexpected network error occurred.";
    console.error(`[API Error] ${error.config?.url}:`, message);
    return Promise.reject(new Error(message));
  }
);
