import axios, { AxiosError } from "axios";
import { useAuthStore } from "@/store/authStore";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api/v1";

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 90000,
});

// Request interceptor: attach Authorization Bearer JWT & fallback headers
apiClient.interceptors.request.use(
  (config) => {
    const { token } = useAuthStore.getState();

    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }


    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: unwrap backend ApiResponse<T> or handle errors
apiClient.interceptors.response.use(
  (response) => {
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

    if (error.response?.status === 401 && !error.config?.url?.includes("/auth/login")) {
      console.warn("[API 401 Unauthorized] Session expired or invalid.");
    }

    return Promise.reject(new Error(message));
  }
);