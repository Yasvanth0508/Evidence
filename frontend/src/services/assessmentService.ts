import { apiClient } from "@/lib/apiClient";

export interface CreateAssessmentPayload {
  candidateId: string;
  title?: string;
  repositoryUrl: string;
  branchName: string;
  backendRootDirectory: string;
  difficulty: "EASY" | "INTERMEDIATE" | "DIFFICULT";
  durationMinutes: number;
  scheduledStartAt: string;
  scheduledEndAt: string;
}

export interface AssessmentResponse {
  id: string;
  assessmentId?: string;
  title?: string;
  workspaceId: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  repositoryUrl: string;
  branchName: string;
  backendRootDirectory: string;
  difficulty: string;
  durationMinutes: number;
  scheduledStartAt: string;
  scheduledEndAt: string;
  status: string;
  score?: number;
  createdAt: string;
}

export interface FileNodeDto {
  id: string;
  name: string;
  path: string;
  type: "file" | "directory";
  children?: FileNodeDto[];
}

export interface FileContentResponse {
  path: string;
  content: string;
  language: string;
}

export interface FeatureSpecificationResponse {
  assessmentId: string;
  title: string;
  description: string;
  requirements: string[];
  endpoint: string;
  httpMethod: string;
  requestSpecification: string;
  responseSpecification: string;
  constraints: string[];
}

const isUuid = (str?: string): boolean =>
  Boolean(str && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str));

export const assessmentService = {
  createAssessment: async (workspaceId: string, data: CreateAssessmentPayload): Promise<AssessmentResponse> => {
    if (!isUuid(workspaceId)) return null as any;
    return await apiClient.post(`/workspaces/${workspaceId}/assessments`, data);
  },

  getAssessmentsByWorkspace: async (workspaceId: string): Promise<AssessmentResponse[]> => {
    if (!isUuid(workspaceId)) return [];
    return await apiClient.get(`/workspaces/${workspaceId}/assessments`);
  },

  getAssessmentById: async (id: string): Promise<AssessmentResponse> => {
    if (!isUuid(id)) return null as any;
    return await apiClient.get(`/assessments/${id}`);
  },

  updateAssessment: async (id: string, data: Partial<CreateAssessmentPayload>): Promise<AssessmentResponse> => {
    if (!isUuid(id)) return null as any;
    return await apiClient.put(`/assessments/${id}`, data);
  },

  cancelAssessment: async (id: string): Promise<{ message: string }> => {
    if (!isUuid(id)) return { message: "Cancelled locally" };
    return await apiClient.post(`/assessments/${id}/cancel`);
  },

  startAssessment: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.post(`/assessments/${id}/start`);
  },

  submitAssessment: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.post(`/assessments/${id}/submit`);
  },

  getProcessingStatus: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.get(`/assessments/${id}/processing-status`);
  },

  getFeatureSpec: async (id: string): Promise<FeatureSpecificationResponse> => {
    if (!isUuid(id)) return null as any;
    return await apiClient.get(`/assessments/${id}/feature`);
  },

  getFileTree: async (id: string): Promise<FileNodeDto[]> => {
    if (!isUuid(id)) return [];
    return await apiClient.get(`/assessments/${id}/files`);
  },

  getFileContent: async (id: string, path: string): Promise<FileContentResponse> => {
    if (!isUuid(id)) return { path, content: "", language: "java" };
    return await apiClient.get(`/assessments/${id}/files/content`, { params: { path } });
  },

  saveFile: async (id: string, path: string, content: string): Promise<{ path: string; saved: boolean }> => {
    if (!isUuid(id)) return { path, saved: true };
    return await apiClient.put(`/assessments/${id}/files/content`, { path, content });
  },

  createFile: async (id: string, path: string, type: "FILE" | "DIRECTORY", content?: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.post(`/assessments/${id}/files`, { path, type, content });
  },

  deleteFile: async (id: string, path: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.delete(`/assessments/${id}/files`, { params: { path } });
  },

  renameFile: async (id: string, oldPath: string, newPath: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.post(`/assessments/${id}/files/rename`, { oldPath, newPath });
  },

  runApplication: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.post(`/assessments/${id}/run`);
  },

  stopApplication: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.post(`/assessments/${id}/stop`);
  },

  getExecutionStatus: async (id: string): Promise<any> => {
    if (!isUuid(id)) return { status: "IDLE" };
    return await apiClient.get(`/assessments/${id}/execution/status`);
  },

  getExecutionLogs: async (id: string): Promise<{ logs: string }> => {
    if (!isUuid(id)) return { logs: "" };
    return await apiClient.get(`/assessments/${id}/execution/logs`);
  },

  getCandidateResult: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.get(`/assessments/${id}/result`);
  },

  getRecruiterReport: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.get(`/assessments/${id}/report`);
  },

  getTestResults: async (id: string): Promise<any> => {
    if (!isUuid(id)) return [];
    return await apiClient.get(`/assessments/${id}/test-results`);
  },

  getRepositoryAnalysis: async (id: string): Promise<any> => {
    if (!isUuid(id)) return null;
    return await apiClient.get(`/assessments/${id}/repository-analysis`);
  },
};
