import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { mockAssessmentService } from "@/mocks/services/mockAssessmentService";

export const useWorkspaces = () => {
  return useQuery({
    queryKey: ["workspaces"],
    queryFn: async () => {
      const res = await mockAssessmentService.getWorkspaces();
      return res.data;
    },
  });
};

export const useAssessments = () => {
  return useQuery({
    queryKey: ["assessments"],
    queryFn: async () => {
      const res = await mockAssessmentService.getAssessments();
      return res.data;
    },
  });
};

export const useAssessment = (id: string) => {
  return useQuery({
    queryKey: ["assessment", id],
    queryFn: async () => {
      const res = await mockAssessmentService.getAssessmentById(id);
      return res.data;
    },
    enabled: Boolean(id),
  });
};

export const useFileTree = (assessmentId: string) => {
  return useQuery({
    queryKey: ["file-tree", assessmentId],
    queryFn: async () => {
      const res = await mockAssessmentService.getFileTree(assessmentId);
      return res.data;
    },
    enabled: Boolean(assessmentId),
  });
};

export const useFileContent = (assessmentId: string, path: string) => {
  return useQuery({
    queryKey: ["file-content", assessmentId, path],
    queryFn: async () => {
      const res = await mockAssessmentService.getFileContent(assessmentId, path);
      return res.data;
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
      const res = await mockAssessmentService.saveFile(assessmentId, path, content);
      return res.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["file-content", variables.assessmentId, variables.path],
      });
    },
  });
};
