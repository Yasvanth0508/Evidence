import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Editor from "@monaco-editor/react";
import { useAssessment, useFileTree, useFileContent, useSaveFile } from "@/hooks/useAssessments";
import { IdeHeader } from "@/components/ide/IdeHeader";
import { FileExplorer } from "@/components/ide/FileExplorer";
import { TerminalConsole } from "@/components/ide/TerminalConsole";
import { FeatureSpecDrawer } from "@/components/ide/FeatureSpecDrawer";
import { mockFeatureSpec } from "@/mocks/data/assessment.mock";
import { Loader2, Save, FileCode, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";

export const AssessmentWorkspace = () => {
  const { id = "asmt-001" } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: assessment, isLoading: isAssessmentLoading } = useAssessment(id);
  const { data: fileTree = [], isLoading: isTreeLoading } = useFileTree(id);

  const [activeFilePath, setActiveFilePath] = useState<string>(
    "/src/main/java/com/example/notes/NoteController.java"
  );
  const { data: fileData, isLoading: isFileLoading } = useFileContent(
    id,
    activeFilePath
  );
  const saveFileMutation = useSaveFile();

  const [fileContent, setFileContent] = useState<string>("");
  const [isDirty, setIsDirty] = useState(false);
  const [isSpecDrawerOpen, setIsSpecDrawerOpen] = useState(false);
  const [isRunningBuild, setIsRunningBuild] = useState(false);
  const [buildStatus, setBuildStatus] = useState<"IDLE" | "BUILDING" | "SUCCESS" | "FAILED">("FAILED");
  const [isSubmitModalOpen, setIsSubmitModalOpen] = useState(false);

  // Initial terminal logs matching the mockup
  const [logs, setLogs] = useState<string[]>([
    "[INFO] Scanning for projects...",
    "[INFO] -------------------< com.example:notes-service >-------------------",
    "[INFO] Building notes-service 1.0.0",
    "[INFO] --------------------------------[ jar ]---------------------------------",
    "[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) @ notes-service ---",
    "[INFO] Changes detected - recompiling the module!",
    "[INFO] Compiling 4 source files to /target/classes",
    "Error: Cannot GET /api/users/123 (Route Mismatch)",
    "  at Layer.handle (node_modules/express/lib/router/layer.js:95:5)",
    "  at next (node_modules/express/lib/router/route.js:144:13)",
    "✗ Build failed with exit code 1",
  ]);

  // Sync loaded file content into editor state
  useEffect(() => {
    if (fileData?.content !== undefined) {
      setFileContent(fileData.content);
      setIsDirty(false);
    }
  }, [fileData]);

  const handleEditorChange = (value: string | undefined) => {
    setFileContent(value || "");
    setIsDirty(true);
  };

  const handleSave = () => {
    if (!activeFilePath) return;
    saveFileMutation.mutate({
      assessmentId: id,
      path: activeFilePath,
      content: fileContent,
    });
    setIsDirty(false);
  };

  const getLanguage = (path: string) => {
    if (path.endsWith(".java")) return "java";
    if (path.endsWith(".xml")) return "xml";
    if (path.endsWith(".json")) return "json";
    if (path.endsWith(".md")) return "markdown";
    if (path.endsWith(".js") || path.endsWith(".ts")) return "javascript";
    return "plaintext";
  };

  const handleRunBuild = () => {
    setIsRunningBuild(true);
    setBuildStatus("BUILDING");
    setLogs((prev) => [
      ...prev,
      `[${new Date().toLocaleTimeString()}] Starting Docker compilation container...`,
      "[INFO] Executing: ./mvnw clean spring-boot:run",
    ]);

    setTimeout(() => {
      setIsRunningBuild(false);
      // If candidate fixed the controller code, simulate success!
      if (fileContent.includes("/search")) {
        setBuildStatus("SUCCESS");
        setLogs((prev) => [
          ...prev,
          "[INFO] Compiling 4 source files to /target/classes",
          "[INFO] BUILD SUCCESS",
          "[INFO] Tomcat started on port(s): 8080 (http) with context path ''",
          "[INFO] Started Application in 2.148 seconds (process running for 2.602)",
          "✔ Application is running and listening for test requests!",
        ]);
      } else {
        setBuildStatus("FAILED");
        setLogs((prev) => [
          ...prev,
          "[INFO] Compiling 4 source files to /target/classes",
          "Error: Route Mismatch or Missing Search Endpoint /api/notes/search",
          "✗ Build failed with exit code 1",
        ]);
      }
    }, 1800);
  };

  const handleConfirmSubmit = () => {
    setIsSubmitModalOpen(false);
    navigate(`/dashboard/candidates/${id}`);
  };

  if (isAssessmentLoading || isTreeLoading) {
    return (
      <div className="h-screen bg-white flex flex-col items-center justify-center space-y-3">
        <Loader2 className="w-8 h-8 text-[#F05323] animate-spin" />
        <span className="text-xs font-semibold text-gray-500">
          Loading isolated coding workspace...
        </span>
      </div>
    );
  }

  const activeFileName = activeFilePath.split("/").pop() || "Editor";

  return (
    <div className="h-screen flex flex-col bg-[#F9FAFB] overflow-hidden font-sans">
      {/* 1. Top IDE Header */}
      <IdeHeader
        projectName={assessment?.projectName || "E-Commerce Platform"}
        initialDurationMinutes={assessment?.durationMinutes || 90}
        isRunningBuild={isRunningBuild}
        onRunBuild={handleRunBuild}
        onSubmitAssessment={() => setIsSubmitModalOpen(true)}
        onToggleFeatureSpec={() => setIsSpecDrawerOpen(!isSpecDrawerOpen)}
      />

      {/* 2. Main IDE Body: Split Layout */}
      <div className="flex-1 grid grid-cols-12 overflow-hidden">
        {/* Left Column: File Explorer (col-span-3 or 2.5) */}
        <div className="col-span-12 md:col-span-3 lg:col-span-2 h-full overflow-hidden">
          <FileExplorer
            files={fileTree}
            activeFilePath={activeFilePath}
            onSelectFile={(path) => setActiveFilePath(path)}
          />
        </div>

        {/* Right Column: Code Editor + Terminal Console */}
        <div className="col-span-12 md:col-span-9 lg:col-span-10 flex flex-col h-full overflow-hidden bg-white">
          {/* Active File Tab Bar */}
          <div className="h-10 bg-gray-100/90 border-b border-gray-200 px-3 flex items-center justify-between select-none">
            <div className="flex items-center gap-2">
              <div className="flex items-center gap-2 bg-white px-3 py-1.5 rounded-t-lg border-t-2 border-[#F05323] text-xs font-mono font-semibold text-gray-900 shadow-2xs">
                <FileCode className="w-3.5 h-3.5 text-[#F05323]" />
                <span>{activeFileName}</span>
                {isDirty && (
                  <span className="w-2 h-2 rounded-full bg-[#F05323]" title="Unsaved changes" />
                )}
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={handleSave}
                disabled={!isDirty || saveFileMutation.isPending}
                className="flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-200 disabled:opacity-40 transition-colors"
                title="Save changes (Ctrl+S)"
              >
                <Save className="w-3.5 h-3.5" />
                <span>{saveFileMutation.isPending ? "Saving..." : "Save"}</span>
              </button>
            </div>
          </div>

          {/* Monaco Editor Pane (60% height) */}
          <div className="flex-1 relative overflow-hidden bg-white">
            {isFileLoading ? (
              <div className="h-full flex items-center justify-center space-x-2 text-xs text-gray-400">
                <Loader2 className="w-4 h-4 animate-spin text-[#F05323]" />
                <span>Loading file content...</span>
              </div>
            ) : (
              <Editor
                height="100%"
                language={getLanguage(activeFilePath)}
                value={fileContent}
                onChange={handleEditorChange}
                theme="vs-light"
                options={{
                  fontSize: 13,
                  fontFamily: "'JetBrains Mono', 'Fira Code', Menlo, monospace",
                  minimap: { enabled: false },
                  scrollBeyondLastLine: false,
                  lineNumbers: "on",
                  roundedSelection: true,
                  automaticLayout: true,
                  tabSize: 4,
                  wordWrap: "on",
                }}
              />
            )}
          </div>

          {/* Bottom Terminal Console (40% height) */}
          <div className="h-56 flex-shrink-0">
            <TerminalConsole
              logs={logs}
              buildStatus={buildStatus}
              onClearLogs={() => setLogs([])}
            />
          </div>
        </div>
      </div>

      {/* 3. Feature Specification Side Drawer */}
      <FeatureSpecDrawer
        spec={mockFeatureSpec}
        isOpen={isSpecDrawerOpen}
        onClose={() => setIsSpecDrawerOpen(false)}
      />

      {/* 4. Submit Assessment Confirmation Modal */}
      {isSubmitModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="w-12 h-12 rounded-full bg-orange-50 text-[#F05323] flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-6 h-6" />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-lg font-extrabold text-gray-900">
                Submit Technical Assessment?
              </h3>
              <p className="text-xs text-gray-500 leading-relaxed">
                Are you ready to submit your code? Once submitted, the hidden automated test suite will execute and generate your evaluation score report.
              </p>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <Button
                variant="outline"
                className="flex-1 font-semibold text-xs"
                onClick={() => setIsSubmitModalOpen(false)}
              >
                Continue Coding
              </Button>
              <Button
                className="flex-1 font-semibold text-xs shadow-sm"
                onClick={handleConfirmSubmit}
              >
                Yes, Submit Code
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
