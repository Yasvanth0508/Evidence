import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { workspaceService } from "@/services/workspaceService";
import { assessmentService } from "@/services/assessmentService";
import { Assessment, FileNode, AssessmentStatus, DifficultyLevel } from "@/types";

export const useWorkspaces = () => {
  return useQuery({
    queryKey: ["workspaces"],
    queryFn: async () => {
      return await workspaceService.getWorkspaces();
    },
  });
};

export const useAssessments = (workspaceId?: string) => {
  return useQuery({
    queryKey: ["assessments", workspaceId],
    queryFn: async () => {
      if (!workspaceId) return [];
      return await assessmentService.getAssessmentsByWorkspace(workspaceId);
    },
    enabled: Boolean(workspaceId),
  });
};

export const useAssessment = (id: string) => {
  return useQuery<Assessment>({
    queryKey: ["assessment", id],
    queryFn: async () => {
      const res: any = await assessmentService.getAssessmentById(id);
      if (!res) throw new Error("Assessment not found");
      const cand = res.candidate || {};
      return {
        id: res.id,
        workspaceId: res.workspaceId,
        candidateId: cand.id || res.candidateId,
        candidateName: cand.name || res.candidateName || "Candidate",
        candidateEmail: cand.email || res.candidateEmail || "",
        projectName: res.repositoryUrl
          ? res.repositoryUrl.split("/").pop()?.replace(".git", "") || "Spring Boot REST API"
          : "Spring Boot REST API",
        repositoryUrl: res.repositoryUrl,
        branchName: res.branchName,
        backendRootDirectory: res.backendRootDirectory,
        difficulty: (res.difficulty as DifficultyLevel) || "INTERMEDIATE",
        durationMinutes: res.durationMinutes || 60,
        scheduledStartAt: res.scheduledStartAt,
        scheduledEndAt: res.scheduledEndAt,
        status: (res.status as AssessmentStatus) || "IN_PROGRESS",
        score: res.score ?? null,
        createdAt: res.createdAt,
        updatedAt: res.updatedAt || res.createdAt,
      };
    },
    enabled: Boolean(id),
  });
};

function mapToFileNode(node: any): FileNode {
  return {
    id: node.id || node.path || node.name,
    name: node.name,
    path: node.path,
    type: node.type?.toUpperCase() === "DIRECTORY" ? "DIRECTORY" : "FILE",
    children: Array.isArray(node.children) ? node.children.map(mapToFileNode) : undefined,
  };
}

export const useFileTree = (assessmentId: string) => {
  return useQuery<FileNode[]>({
    queryKey: ["file-tree", assessmentId],
    queryFn: async () => {
      const files: any = await assessmentService.getFileTree(assessmentId);
      if (!files) return [];
      if (Array.isArray(files)) {
        return files.map(mapToFileNode);
      }
      // If backend returned single root Directory node with children
      if (files.children && Array.isArray(files.children)) {
        return files.children.map(mapToFileNode);
      }
      return [mapToFileNode(files)];
    },
    enabled: Boolean(assessmentId),
  });
};

export const useFeatureSpec = (assessmentId: string) => {
  return useQuery({
    queryKey: ["feature-spec", assessmentId],
    queryFn: async () => {
      return await assessmentService.getFeatureSpec(assessmentId);
    },
    enabled: Boolean(assessmentId),
  });
};

export const useFileContent = (assessmentId: string, path: string) => {
  return useQuery({
    queryKey: ["file-content", assessmentId, path],
    queryFn: async () => {
      return await assessmentService.getFileContent(assessmentId, path);
    },
    enabled: Boolean(assessmentId && path),
  });
};

export const useSaveFile = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      assessmentId,
      path,
      content,
    }: {
      assessmentId: string;
      path: string;
      content: string;
    }) => {
      return await assessmentService.saveFile(assessmentId, path, content);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["file-content", variables.assessmentId, variables.path],
      });
    },
  });
};

export const useCreateFile = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      assessmentId,
      path,
      type,
      content = "",
    }: {
      assessmentId: string;
      path: string;
      type: "FILE" | "DIRECTORY";
      content?: string;
    }) => {
      return await assessmentService.createFile(assessmentId, path, type, content);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["file-tree", variables.assessmentId],
      });
    },
  });
};

export const useDeleteFile = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      assessmentId,
      path,
    }: {
      assessmentId: string;
      path: string;
    }) => {
      return await assessmentService.deleteFile(assessmentId, path);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["file-tree", variables.assessmentId],
      });
    },
  });
};

export const useRenameFile = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      assessmentId,
      oldPath,
      newPath,
    }: {
      assessmentId: string;
      oldPath: string;
      newPath: string;
    }) => {
      return await assessmentService.renameFile(assessmentId, oldPath, newPath);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["file-tree", variables.assessmentId],
      });
    },
  });
};

export const useRunApplication = () => {
  return useMutation({
    mutationFn: async (assessmentId: string) => {
      return await assessmentService.runApplication(assessmentId);
    },
  });
};

export const useStopApplication = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (assessmentId: string) => {
      return await assessmentService.stopApplication(assessmentId);
    },
    onSuccess: (_, assessmentId) => {
      queryClient.invalidateQueries({ queryKey: ["execution-status", assessmentId] });
      queryClient.invalidateQueries({ queryKey: ["execution-logs", assessmentId] });
    },
  });
};

export const useExecutionLogs = (assessmentId: string) => {
  return useQuery({
    queryKey: ["execution-logs", assessmentId],
    queryFn: async () => {
      return await assessmentService.getExecutionLogs(assessmentId);
    },
    refetchInterval: 2000,
    enabled: Boolean(assessmentId),
  });
};

export const useSubmitAssessment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (
      args: string | { assessmentId: string; data?: { tabSwitchCount?: number; copyPasteEvents?: number; idleTimeMinutes?: number } }
    ) => {
      const assessmentId = typeof args === "string" ? args : args.assessmentId;
      const data = typeof args === "string" ? undefined : args.data;
      return await assessmentService.submitAssessment(assessmentId, data);
    },
    onSuccess: (_, args) => {
      const assessmentId = typeof args === "string" ? args : args.assessmentId;
      queryClient.invalidateQueries({ queryKey: ["assessment", assessmentId] });
      queryClient.invalidateQueries({ queryKey: ["candidate-dashboard"] });
      queryClient.invalidateQueries({ queryKey: ["recruiter-dashboard"] });
    },
  });
};

export const useCreateAssessment = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      workspaceId,
      data,
    }: {
      workspaceId: string;
      data: any;
    }) => {
      return await assessmentService.createAssessment(workspaceId, data);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["assessments", variables.workspaceId] });
      queryClient.invalidateQueries({ queryKey: ["recruiter-dashboard"] });
    },
  });
};

export const useAssessmentProcessingStatus = (assessmentId?: string) => {
  return useQuery({
    queryKey: ["assessment-processing-status", assessmentId],
    queryFn: async () => {
      if (!assessmentId) return null;
      return await assessmentService.getProcessingStatus(assessmentId);
    },
    enabled: Boolean(assessmentId),
    refetchInterval: (query) => {
      const data = query.state.data;
      if (!data) return 2000;
      const status = data.assessmentStatus;
      if (status === "READY" || status === "SCHEDULED" || status === "FAILED" || status === "COMPLETED") {
        return false;
      }
      return 2000;
    },
  });
};

export const useRepositoryAnalysis = (assessmentId?: string) => {
  return useQuery({
    queryKey: ["repository-analysis", assessmentId],
    queryFn: async () => {
      if (!assessmentId) return null;
      return await assessmentService.getRepositoryAnalysis(assessmentId);
    },
    enabled: Boolean(assessmentId),
  });
};



