import { apiClient } from "@/lib/apiClient";

export interface WorkspaceItem {
  id: string;
  name: string;
  description: string;
  status: string;
}

export type WorkspaceResponse = WorkspaceItem;

export interface CreateWorkspacePayload {
  name: string;
  description?: string;
}

export interface WorkspaceCandidateItem {
  id: string;
  name: string;
  email: string;
  role?: string;
}

const isUuid = (str?: string): boolean =>
  Boolean(str && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str));

export const workspaceService = {
  getWorkspaces: async (): Promise<WorkspaceItem[]> => {
    return await apiClient.get("/workspaces");
  },

  getWorkspaceById: async (id: string): Promise<WorkspaceItem> => {
    if (!isUuid(id)) return null as any;
    return await apiClient.get(`/workspaces/${id}`);
  },

  createWorkspace: async (data: CreateWorkspacePayload): Promise<WorkspaceItem> => {
    return await apiClient.post("/workspaces", data);
  },

  updateWorkspace: async (id: string, data: Partial<CreateWorkspacePayload>): Promise<WorkspaceItem> => {
    if (!isUuid(id)) return null as any;
    return await apiClient.put(`/workspaces/${id}`, data);
  },

  deleteWorkspace: async (id: string): Promise<{ message: string }> => {
    if (!isUuid(id)) return { message: "Deleted locally" };
    return await apiClient.delete(`/workspaces/${id}`);
  },

  getCandidatesInWorkspace: async (workspaceId: string): Promise<WorkspaceCandidateItem[]> => {
    if (!isUuid(workspaceId)) return [];
    return await apiClient.get(`/workspaces/${workspaceId}/candidates`);
  },

  addCandidateToWorkspace: async (workspaceId: string, email: string, name?: string): Promise<any> => {
    if (!isUuid(workspaceId)) return null;
    return await apiClient.post(`/workspaces/${workspaceId}/candidates`, { email, name });
  },

  removeCandidateFromWorkspace: async (workspaceId: string, candidateId: string): Promise<any> => {
    if (!isUuid(workspaceId) || !isUuid(candidateId)) return null;
    return await apiClient.delete(`/workspaces/${workspaceId}/candidates/${candidateId}`);
  },
};
