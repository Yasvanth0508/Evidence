import { useState, useEffect, useRef, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Editor from "@monaco-editor/react";
import {
  useAssessment,
  useFileTree,
  useFileContent,
  useSaveFile,
  useCreateFile,
  useDeleteFile,
  useRenameFile,
  useRunApplication,
  useStopApplication,
  useSubmitAssessment,
  useFeatureSpec,
} from "@/hooks/useAssessments";
import { assessmentService } from "@/services/assessmentService";
import { useIntegrityStore } from "@/store/integrityStore";
import { useThemeStore } from "@/store/themeStore";
import { IdeHeader } from "@/components/ide/IdeHeader";
import { FileExplorer } from "@/components/ide/FileExplorer";
import { TerminalConsole } from "@/components/ide/TerminalConsole";
import { FeatureSpecDrawer } from "@/components/ide/FeatureSpecDrawer";
import { SubmissionLoadingOverlay } from "@/components/ide/SubmissionLoadingOverlay";
import {
  Loader2,
  Save,
  FileCode,
  CheckCircle2,
  PanelLeftOpen,
  AlertTriangle,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export const AssessmentWorkspace = () => {
  const { id = "asmt-001" } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: assessment, isLoading: isAssessmentLoading } = useAssessment(id);
  const { data: fileTree = [], isLoading: isTreeLoading, refetch: refetchTree } = useFileTree(id);
  const { data: featureSpec } = useFeatureSpec(id);

  const [activeFilePath, setActiveFilePath] = useState<string>("");
  const { data: fileData, isLoading: isFileLoading } = useFileContent(
    id,
    activeFilePath
  );
  const saveFileMutation = useSaveFile();
  const createFileMutation = useCreateFile();
  const deleteFileMutation = useDeleteFile();
  const renameFileMutation = useRenameFile();
  const runMutation = useRunApplication();
  const stopMutation = useStopApplication();
  const submitMutation = useSubmitAssessment();

  const [fileContent, setFileContent] = useState<string>("");
  const [isDirty, setIsDirty] = useState(false);
  const [isSpecDrawerOpen, setIsSpecDrawerOpen] = useState(false);
  const [isRunningBuild, setIsRunningBuild] = useState(false);
  const [buildStatus, setBuildStatus] = useState<"IDLE" | "BUILDING" | "SUCCESS" | "FAILED">("IDLE");
  const [isSubmitModalOpen, setIsSubmitModalOpen] = useState(false);

  // Global Unified Theme State (synced with IDE and Monaco editor)
  const { theme, toggleTheme: handleToggleTheme } = useThemeStore();

  // Layout resizing & collapse states
  const [sidebarWidth, setSidebarWidth] = useState<number>(260);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState<boolean>(false);
  const [terminalHeight, setTerminalHeight] = useState<number>(220);
  const [isTerminalCollapsed, setIsTerminalCollapsed] = useState<boolean>(false);
  const [isDraggingSidebar, setIsDraggingSidebar] = useState<boolean>(false);
  const [isDraggingTerminal, setIsDraggingTerminal] = useState<boolean>(false);

  // Monaco Editor Ref
  const editorRef = useRef<any>(null);
  const autosaveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Zustand Integrity & Tab Switching Proctoring Store
  const {
    tabSwitchCount,
    showWarningModal,
    warningTitle,
    warningMessage,
    isAutoSubmitted,
    recordTabSwitch,
    acknowledgeWarning,
    resetIntegrity,
  } = useIntegrityStore();

  // Reset integrity state on new assessment load
  useEffect(() => {
    resetIntegrity();
  }, [id, resetIntegrity]);

  // Auto-start assessment workspace on mount
  useEffect(() => {
    if (id) {
      assessmentService.startAssessment(id).catch((e) => {
        console.debug("Assessment start check:", e);
      });
    }
  }, [id]);

  // Helper to find the first leaf file in the tree
  const findFirstFile = useCallback((nodes: any[]): string | null => {
    for (const node of nodes) {
      if (node.type === "FILE") return node.path;
      if (node.children && node.children.length > 0) {
        const found = findFirstFile(node.children);
        if (found) return found;
      }
    }
    return null;
  }, []);

  // Set active file automatically when file tree is loaded
  useEffect(() => {
    if (fileTree && fileTree.length > 0) {
      const fileExists = (nodes: any[], target: string): boolean => {
        for (const node of nodes) {
          if (node.type === "FILE" && (node.path === target || `/${node.path}` === target)) return true;
          if (node.children && fileExists(node.children, target)) return true;
        }
        return false;
      };

      if (!activeFilePath || !fileExists(fileTree, activeFilePath)) {
        const first = findFirstFile(fileTree);
        if (first) {
          setActiveFilePath(first);
        }
      }
    }
  }, [fileTree, activeFilePath, findFirstFile]);

  // Toggle proctoring (disabled for testing)
  const PROCTORING_ENABLED = false;

  // Initial terminal logs
  const [logs, setLogs] = useState<string[]>([
    "[INFO] Initializing candidate sandbox workspace...",
    "[INFO] Tab switching proctoring is disabled for testing.",
    "[INFO] Workspace isolated on disk. Ready for edits.",
    "[INFO] Tip: Click 'Run Build' in top bar to compile and test against container.",
  ]);

  // Sync loaded file content into editor state
  useEffect(() => {
    if (fileData?.content !== undefined) {
      setFileContent(fileData.content);
      setIsDirty(false);
    }
  }, [fileData]);

  // Debounced Autosave (2500ms)
  const handleEditorChange = (value: string | undefined) => {
    const nextContent = value || "";
    setFileContent(nextContent);
    setIsDirty(true);

    if (autosaveTimerRef.current) {
      clearTimeout(autosaveTimerRef.current);
    }

    autosaveTimerRef.current = setTimeout(() => {
      if (activeFilePath && id) {
        saveFileMutation.mutate({
          assessmentId: id,
          path: activeFilePath,
          content: nextContent,
        });
        setIsDirty(false);
      }
    }, 2500);
  };

  const handleSave = () => {
    if (!activeFilePath || !id) return;
    if (autosaveTimerRef.current) clearTimeout(autosaveTimerRef.current);
    saveFileMutation.mutate({
      assessmentId: id,
      path: activeFilePath,
      content: fileContent,
    });
    setIsDirty(false);
  };

  const handleCreateFile = async (path: string, type: "FILE" | "DIRECTORY") => {
    if (!id) return;
    await createFileMutation.mutateAsync({
      assessmentId: id,
      path,
      type,
      content: type === "FILE" ? "// New file\n" : undefined,
    });
    setLogs((prev) => [
      ...prev,
      `[FILE_SYSTEM] Created ${type === "DIRECTORY" ? "directory" : "file"}: ${path}`,
    ]);
  };

  const handleDeleteFile = async (path: string) => {
    if (!id) return;
    await deleteFileMutation.mutateAsync({
      assessmentId: id,
      path,
    });
    setLogs((prev) => [
      ...prev,
      `[FILE_SYSTEM] Deleted: ${path}`,
    ]);
    if (activeFilePath === path || activeFilePath.startsWith(`${path}/`)) {
      const remainingFirst = findFirstFile(fileTree.filter((n) => n.path !== path));
      setActiveFilePath(remainingFirst || "");
    }
  };

  const handleRenameFile = async (oldPath: string, newPath: string) => {
    if (!id) return;
    await renameFileMutation.mutateAsync({
      assessmentId: id,
      oldPath,
      newPath,
    });
    setLogs((prev) => [
      ...prev,
      `[FILE_SYSTEM] Renamed: ${oldPath} -> ${newPath}`,
    ]);
    if (activeFilePath === oldPath) {
      setActiveFilePath(newPath);
    } else if (activeFilePath.startsWith(`${oldPath}/`)) {
      setActiveFilePath(activeFilePath.replace(oldPath, newPath));
    }
  };

  // Drag Resizer Handlers for Sidebar
  const handleSidebarMouseDown = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsDraggingSidebar(true);
    const startX = e.clientX;
    const startWidth = sidebarWidth;

    const handleMouseMove = (moveEvent: MouseEvent) => {
      const delta = moveEvent.clientX - startX;
      const nextWidth = Math.max(160, Math.min(550, startWidth + delta));
      setSidebarWidth(nextWidth);
    };

    const handleMouseUp = () => {
      setIsDraggingSidebar(false);
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);
  };

  // Drag Resizer Handlers for Terminal Height
  const handleTerminalMouseDown = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsDraggingTerminal(true);
    const startY = e.clientY;
    const startHeight = terminalHeight;

    const handleMouseMove = (moveEvent: MouseEvent) => {
      const delta = startY - moveEvent.clientY;
      const nextHeight = Math.max(80, Math.min(650, startHeight + delta));
      setTerminalHeight(nextHeight);
    };

    const handleMouseUp = () => {
      setIsDraggingTerminal(false);
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);
  };

  // Full syntax highlighting mapping for all languages
  const getLanguage = (path: string): string => {
    if (!path) return "plaintext";
    const lower = path.toLowerCase();
    if (lower.endsWith(".java")) return "java";
    if (lower.endsWith(".jsx") || lower.endsWith(".js") || lower.endsWith(".mjs") || lower.endsWith(".cjs")) return "javascript";
    if (lower.endsWith(".tsx") || lower.endsWith(".ts") || lower.endsWith(".mts") || lower.endsWith(".cts")) return "typescript";
    if (lower.endsWith(".html") || lower.endsWith(".htm")) return "html";
    if (lower.endsWith(".css")) return "css";
    if (lower.endsWith(".scss") || lower.endsWith(".sass")) return "scss";
    if (lower.endsWith(".less")) return "less";
    if (lower.endsWith(".json")) return "json";
    if (lower.endsWith(".xml") || lower.endsWith(".svg")) return "xml";
    if (lower.endsWith(".sql")) return "sql";
    if (lower.endsWith(".py")) return "python";
    if (lower.endsWith(".sh") || lower.endsWith(".bash") || lower.endsWith(".zsh")) return "shell";
    if (lower.endsWith(".md") || lower.endsWith(".markdown")) return "markdown";
    if (lower.endsWith(".properties") || lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".env")) return "yaml";
    if (lower.endsWith("dockerfile") || lower.endsWith(".dockerfile")) return "dockerfile";
    if (lower.endsWith(".graphql") || lower.endsWith(".gql")) return "graphql";
    if (lower.endsWith(".rs")) return "rust";
    if (lower.endsWith(".go")) return "go";
    if (lower.endsWith(".cpp") || lower.endsWith(".cc") || lower.endsWith(".c") || lower.endsWith(".h") || lower.endsWith(".hpp")) return "cpp";
    if (lower.endsWith(".cs")) return "csharp";
    return "plaintext";
  };

  const handleRunBuild = async () => {
    if (!id) return;
    setIsRunningBuild(true);
    setBuildStatus("BUILDING");
    if (isTerminalCollapsed) setIsTerminalCollapsed(false);
    setLogs([
      `[${new Date().toLocaleTimeString()}] Starting build and execution pipeline for assessment...`,
      "[INFO] Saving latest workspace changes to candidate sandbox...",
      "[INFO] Step 1/3: Compiling and packaging Java Spring Boot application...",
    ]);

    try {
      if (activeFilePath && isDirty) {
        await saveFileMutation.mutateAsync({
          assessmentId: id,
          path: activeFilePath,
          content: fileContent,
        });
        setIsDirty(false);
      }

      const res = await runMutation.mutateAsync(id);

      try {
        const logRes = await assessmentService.getExecutionLogs(id);
        if (logRes && logRes.logs) {
          const rawLines = logRes.logs.split("\n").filter((l: string) => l.trim().length > 0);
          setLogs(rawLines);
        }
      } catch (logErr) {
        console.debug("Could not fetch execution logs:", logErr);
      }

      setIsRunningBuild(false);
      if (res.status === "FAILED") {
        setBuildStatus("FAILED");
      } else {
        setBuildStatus("SUCCESS");
        setLogs((prev) => [
          ...prev,
          `✔ Application is running! Sandbox listening on dynamic port: ${res.port || 18080}`,
        ]);
      }
    } catch (err: any) {
      setIsRunningBuild(false);
      setBuildStatus("FAILED");
      try {
        const logRes = await assessmentService.getExecutionLogs(id);
        if (logRes && logRes.logs) {
          const rawLines = logRes.logs.split("\n").filter((l: string) => l.trim().length > 0);
          setLogs(rawLines);
        } else {
          setLogs((prev) => [
            ...prev,
            `[ERROR] Execution failed: ${err.message || "Unknown error occurred"}`,
            "✗ Build failed with error",
          ]);
        }
      } catch {
        setLogs((prev) => [
          ...prev,
          `[ERROR] Execution failed: ${err.message || "Unknown error occurred"}`,
          "✗ Build failed with error",
        ]);
      }
    }
  };
  const handleStopApplication = async () => {
    if (!id) return;
    try {
      await stopMutation.mutateAsync(id);
      setBuildStatus("IDLE");
      setLogs((prev) => [
        ...prev,
        `[${new Date().toLocaleTimeString()}] Application container stopped by candidate.`,
      ]);
    } catch (err: any) {
      console.error("Stop application error:", err);
      setBuildStatus("IDLE");
      setLogs((prev) => [
        ...prev,
        `[${new Date().toLocaleTimeString()}] Application stopped.`,
      ]);
    }
  };

  const handleConfirmSubmit = useCallback(async () => {
    if (!id || submitMutation.isPending) return;
    try {
      await submitMutation.mutateAsync({
        assessmentId: id,
        data: {
          tabSwitchCount: tabSwitchCount || 0,
          copyPasteEvents: 0,
          idleTimeMinutes: 1,
        },
      });
      setIsSubmitModalOpen(false);
      navigate(`/candidate/assessments/${id}/report`);
    } catch (err: any) {
      console.error("Submit error:", err);
      setIsSubmitModalOpen(false);
      navigate(`/candidate/assessments/${id}/report`);
    }
  }, [id, submitMutation, navigate, tabSwitchCount]);

  // Anti-Cheat Tab Switching Monitor (disabled when PROCTORING_ENABLED is false)
  useEffect(() => {
    if (!id || !PROCTORING_ENABLED) return;

    const handleVisibilityChange = () => {
      if (document.hidden && !isAutoSubmitted) {
        const { count, shouldAutoSubmit } = recordTabSwitch(id);
        setLogs((prev) => [
          ...prev,
          `[INTEGRITY_WARNING] Browser tab hidden/switched. Violation #${count} recorded.`,
        ]);

        if (shouldAutoSubmit) {
          setLogs((prev) => [
            ...prev,
            `[INTEGRITY_VIOLATION] Tab switch limit exceeded (>2 switches). Automatically submitting assessment...`,
          ]);
          handleConfirmSubmit();
        }
      }
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);
    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [id, isAutoSubmitted, recordTabSwitch, handleConfirmSubmit, PROCTORING_ENABLED]);

  if (isAssessmentLoading || isTreeLoading) {
    return (
      <div className="h-screen bg-white flex flex-col items-center justify-center space-y-3">
        <Loader2 className="w-8 h-8 text-primary animate-spin" />
        <span className="text-xs font-semibold text-gray-500">
          Loading isolated coding workspace...
        </span>
      </div>
    );
  }

  const activeFileName = activeFilePath.split("/").pop() || "Editor";
  const isDark = theme === "dark";

  return (
    <div
      className={cn(
        "h-screen flex flex-col overflow-hidden select-none font-sans transition-colors",
        isDark ? "bg-slate-900 text-gray-100" : "bg-gray-100 text-gray-900"
      )}
    >
      {/* 1. IDE Header */}
      <IdeHeader
        projectName={assessment?.projectName || (assessment as any)?.title || "Spring Boot Candidate Assessment"}
        initialDurationMinutes={assessment?.durationMinutes || 90}
        isRunningBuild={isRunningBuild}
        isApplicationRunning={buildStatus === "SUCCESS"}
        theme={theme}
        onToggleTheme={handleToggleTheme}
        onRunBuild={handleRunBuild}
        onStopApplication={handleStopApplication}
        onSubmitAssessment={() => setIsSubmitModalOpen(true)}
        onToggleFeatureSpec={() => setIsSpecDrawerOpen(!isSpecDrawerOpen)}
      />

      {/* 2. Main IDE Body: Flex Resizable Layout */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Column: File Explorer (Resizable & Collapsible) */}
        {!isSidebarCollapsed ? (
          <div
            style={{ width: `${sidebarWidth}px` }}
            className="h-full flex-shrink-0 overflow-hidden relative"
          >
            <FileExplorer
              files={fileTree}
              activeFilePath={activeFilePath}
              theme={theme}
              onSelectFile={(path) => setActiveFilePath(path)}
              onCreateFile={handleCreateFile}
              onRenameFile={handleRenameFile}
              onDeleteFile={handleDeleteFile}
              onRefresh={() => refetchTree()}
              onCollapse={() => setIsSidebarCollapsed(true)}
            />
          </div>
        ) : (
          /* Slim Sidebar Rail when Collapsed */
          <div
            className={cn(
              "w-10 h-full border-r flex flex-col items-center py-2 gap-2 select-none flex-shrink-0 transition-colors",
              isDark
                ? "bg-[#0F172A] border-slate-800 text-slate-400"
                : "bg-gray-100/90 border-gray-200 text-gray-500"
            )}
          >
            <button
              type="button"
              onClick={() => setIsSidebarCollapsed(false)}
              className={cn(
                "p-1.5 rounded-md transition-colors",
                isDark
                  ? "text-slate-400 hover:text-primary hover:bg-slate-800"
                  : "text-gray-500 hover:text-primary hover:bg-gray-200"
              )}
              title="Open File Explorer (Expand Sidebar)"
            >
              <PanelLeftOpen className="w-4 h-4" />
            </button>
            <span
              className={cn(
                "text-[10px] uppercase font-bold tracking-wider rotate-90 mt-6 whitespace-nowrap cursor-pointer transition-colors",
                isDark ? "text-slate-500 hover:text-slate-200" : "text-gray-400 hover:text-gray-700"
              )}
              onClick={() => setIsSidebarCollapsed(false)}
            >
              Explorer
            </span>
          </div>
        )}

        {/* Vertical Resizer Handle between Sidebar & Editor */}
        {!isSidebarCollapsed && (
          <div
            onMouseDown={handleSidebarMouseDown}
            className={cn(
              "w-1 hover:w-1.5 cursor-col-resize active:bg-primary transition-colors flex-shrink-0 relative group select-none z-10",
              isDark
                ? "bg-slate-800 hover:bg-primary"
                : "bg-gray-200 hover:bg-primary",
              isDraggingSidebar && "w-1.5 bg-primary"
            )}
            title="Drag to resize sidebar"
          />
        )}

        {/* Right Column: Code Editor + Terminal Console (Flex column) */}
        <div
          className={cn(
            "flex-1 flex flex-col h-full overflow-hidden min-w-0 transition-colors",
            isDark ? "bg-[#1E1E1E]" : "bg-white"
          )}
        >
          {/* Active File Tab Bar */}
          <div
            className={cn(
              "h-10 border-b px-3 flex items-center justify-between select-none flex-shrink-0 transition-colors",
              isDark
                ? "bg-[#0F172A] border-slate-800 text-slate-300"
                : "bg-gray-100/90 border-gray-200 text-gray-700"
            )}
          >
            <div className="flex items-center gap-2 truncate min-w-0">
              {isSidebarCollapsed && (
                <button
                  type="button"
                  onClick={() => setIsSidebarCollapsed(false)}
                  className={cn(
                    "p-1 rounded mr-1 transition-colors",
                    isDark
                      ? "text-slate-400 hover:text-white hover:bg-slate-800"
                      : "text-gray-500 hover:text-gray-900 hover:bg-gray-200"
                  )}
                  title="Expand File Explorer"
                >
                  <PanelLeftOpen className="w-3.5 h-3.5" />
                </button>
              )}

              <div
                className={cn(
                  "flex items-center gap-2 px-3 py-1.5 rounded-t-lg border-t-2 border-primary text-xs font-mono font-semibold truncate transition-colors",
                  isDark
                    ? "bg-[#1E293B] text-slate-100 shadow-sm"
                    : "bg-white text-gray-900 shadow-2xs"
                )}
              >
                <FileCode className="w-3.5 h-3.5 text-primary flex-shrink-0" />
                <span className="truncate">{activeFileName}</span>
                {isDirty && (
                  <span className="w-2 h-2 rounded-full bg-primary flex-shrink-0" title="Unsaved changes" />
                )}
              </div>
            </div>

            <div className="flex items-center gap-2 flex-shrink-0">
              <button
                onClick={handleSave}
                disabled={!isDirty || saveFileMutation.isPending}
                className={cn(
                  "flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-md disabled:opacity-40 transition-colors",
                  isDark
                    ? "text-slate-300 hover:text-white hover:bg-slate-800"
                    : "text-gray-600 hover:text-gray-900 hover:bg-gray-200"
                )}
                title="Save changes (Ctrl+S)"
              >
                <Save className="w-3.5 h-3.5" />
                <span>{saveFileMutation.isPending ? "Saving..." : "Save"}</span>
              </button>
            </div>
          </div>

          {/* Monaco Editor Pane */}
          <div
            className={cn(
              "flex-1 relative overflow-hidden transition-colors",
              isDark ? "bg-[#1E1E1E]" : "bg-white"
            )}
          >
            {isFileLoading ? (
              <div className="h-full flex items-center justify-center space-x-2 text-xs text-gray-400">
                <Loader2 className="w-4 h-4 animate-spin text-primary" />
                <span>Loading file content...</span>
              </div>
            ) : !activeFilePath ? (
              <div
                className={cn(
                  "h-full flex flex-col items-center justify-center space-y-2 text-xs",
                  isDark ? "text-slate-500" : "text-gray-400"
                )}
              >
                <FileCode className={cn("w-8 h-8", isDark ? "text-slate-600" : "text-gray-300")} />
                <p>Select a file from the explorer or create a new one to begin editing.</p>
              </div>
            ) : (
              <Editor
                height="100%"
                path={activeFilePath ? `file:///${activeFilePath}` : undefined}
                language={getLanguage(activeFilePath)}
                value={fileContent}
                onChange={handleEditorChange}
                onMount={(editor) => {
                  editorRef.current = editor;
                }}
                theme={theme === "dark" ? "vs-dark" : "vs-light"}
                options={{
                  fontSize: 14,
                  fontFamily: "Consolas, 'Cascadia Code', 'Courier New', monospace",
                  fontLigatures: false,
                  letterSpacing: 0,
                  minimap: { enabled: false },
                  scrollBeyondLastLine: false,
                  lineNumbers: "on",
                  roundedSelection: true,
                  automaticLayout: true,
                  tabSize: 4,
                  wordWrap: "on",
                  cursorBlinking: "smooth",
                  cursorSmoothCaretAnimation: "on",
                  cursorStyle: "line",
                  cursorWidth: 2,
                  smoothScrolling: true,
                  renderLineHighlight: "all",
                }}
              />
            )}
          </div>

          {/* Horizontal Resizer Handle between Editor & Terminal */}
          {!isTerminalCollapsed && (
            <div
              onMouseDown={handleTerminalMouseDown}
              className={cn(
                "h-1 hover:h-1.5 cursor-row-resize active:bg-primary transition-colors flex-shrink-0 select-none z-10",
                isDark
                  ? "bg-slate-800 hover:bg-primary"
                  : "bg-gray-300/80 hover:bg-primary",
                isDraggingTerminal && "h-1.5 bg-primary"
              )}
              title="Drag to resize terminal panel"
            />
          )}

          {/* Bottom Terminal Console (Resizable & Collapsible) */}
          <div
            style={{
              height: isTerminalCollapsed ? "40px" : `${terminalHeight}px`,
            }}
            className="flex-shrink-0 transition-[height] duration-75 overflow-hidden"
          >
            <TerminalConsole
              logs={logs}
              buildStatus={buildStatus}
              onClearLogs={() => setLogs([])}
              isCollapsed={isTerminalCollapsed}
              onToggleCollapse={() => setIsTerminalCollapsed(!isTerminalCollapsed)}
            />
          </div>
        </div>
      </div>

      {/* 3. Feature Specification Side Drawer */}
      <FeatureSpecDrawer
        spec={(featureSpec as any) || {
          assessmentId: id,
          title: assessment?.projectName || "Java Spring Boot Feature",
          description: "Implement the requested endpoints and business logic according to test specifications.",
          requirements: ["Implement REST controller endpoints", "Handle business exceptions with appropriate HTTP status codes"],
          endpoint: "/api/v1/resource",
          httpMethod: "GET",
          requestSpecification: "GET /api/v1/resource",
          responseSpecification: "HTTP 200 OK",
          constraints: ["Follow standard Spring Boot conventions", "Pass all unit and integration test assertions"],
        }}
        isOpen={isSpecDrawerOpen}
        theme={theme}
        onClose={() => setIsSpecDrawerOpen(false)}
      />

      {/* 4. Submit Assessment Confirmation Modal */}
      {isSubmitModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div
            className={cn(
              "rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl animate-in zoom-in-95 duration-200 border",
              isDark
                ? "bg-slate-900 border-slate-700 text-slate-100"
                : "bg-white border-gray-100 text-gray-900"
            )}
          >
            <div className="w-12 h-12 rounded-full bg-primary-light0/10 text-primary flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-lg font-extrabold">
                Submit Technical Assessment?
              </h3>
              <p
                className={cn(
                  "text-xs leading-relaxed",
                  isDark ? "text-slate-400" : "text-gray-500"
                )}
              >
                Are you ready to submit your code? Once submitted, the hidden automated test suite will execute and generate your evaluation score report.
              </p>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <Button
                variant="outline"
                disabled={submitMutation.isPending}
                className={cn(
                  "flex-1 font-semibold text-xs",
                  isDark ? "border-slate-700 text-slate-300 hover:bg-slate-800" : ""
                )}
                onClick={() => setIsSubmitModalOpen(false)}
              >
                Continue Coding
              </Button>
              <Button
                disabled={submitMutation.isPending}
                className="flex-1 font-semibold text-xs shadow-sm gap-1.5"
                onClick={handleConfirmSubmit}
              >
                {submitMutation.isPending ? (
                  <>
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    <span>Evaluating...</span>
                  </>
                ) : (
                  <span>Yes, Submit Code</span>
                )}
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* 5. Submitting & Blackbox Evaluation Loading Overlay */}
      <SubmissionLoadingOverlay
        isOpen={submitMutation.isPending}
        theme={theme}
      />

      {/* 5. Anti-Cheat Tab Switching Warning / Auto-Submit Modal */}
      {PROCTORING_ENABLED && showWarningModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl animate-in zoom-in-95 duration-200 border border-primary-border/60">
            <div
              className={cn(
                "w-12 h-12 rounded-full flex items-center justify-center mx-auto",
                isAutoSubmitted
                  ? "bg-rose-100 text-rose-600"
                  : "bg-amber-100 text-amber-600"
              )}
            >
              <AlertTriangle className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1.5">
              <h3
                className={cn(
                  "text-lg font-extrabold",
                  isAutoSubmitted ? "text-rose-700" : "text-gray-900"
                )}
              >
                {warningTitle}
              </h3>
              <p className="text-xs text-gray-600 leading-relaxed font-medium">
                {warningMessage}
              </p>
              {!isAutoSubmitted && (
                <div className="mt-2 text-[11px] font-semibold text-amber-700 bg-amber-50 py-1.5 px-3 rounded-lg inline-block border border-amber-200">
                  Total Tab Switches: {tabSwitchCount} / 2 Allowed
                </div>
              )}
            </div>

            <div className="pt-2">
              {isAutoSubmitted ? (
                <Button
                  className="w-full font-semibold text-xs bg-rose-600 hover:bg-rose-700 text-white shadow-sm"
                  onClick={() => {
                    acknowledgeWarning();
                    navigate(`/dashboard/candidates/${id}`);
                  }}
                >
                  View Evaluation Results
                </Button>
              ) : (
                <Button
                  className="w-full font-semibold text-xs shadow-sm bg-gray-900 hover:bg-black text-white"
                  onClick={acknowledgeWarning}
                >
                  I Understand & Return to Assessment
                </Button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
