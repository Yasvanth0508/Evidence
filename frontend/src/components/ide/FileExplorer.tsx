import React, { useState } from "react";
import { FileNode } from "@/types";
import {
  Folder,
  FolderOpen,
  FileCode,
  FileText,
  FileJson,
  File,
  ChevronRight,
  ChevronDown,
  FilePlus,
  FolderPlus,
  Trash2,
  RefreshCw,
  PanelLeftClose,
  Check,
  X,
  Loader2,
  AlertTriangle,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

interface FileExplorerProps {
  files: FileNode[];
  activeFilePath: string;
  theme?: "dark" | "light";
  onSelectFile: (path: string) => void;
  onCreateFile?: (path: string, type: "FILE" | "DIRECTORY") => Promise<void>;
  onDeleteFile?: (path: string) => Promise<void>;
  onRefresh?: () => void;
  onCollapse?: () => void;
}

interface FileTreeItemProps {
  node: FileNode;
  activeFilePath: string;
  theme?: "dark" | "light";
  onSelectFile: (path: string) => void;
  onInitiateCreate: (parentPath: string, type: "FILE" | "DIRECTORY") => void;
  onInitiateDelete: (node: FileNode) => void;
  level?: number;
}

const getFileIcon = (fileName: string) => {
  const lower = fileName.toLowerCase();
  if (lower.endsWith(".jsx") || lower.endsWith(".tsx") || lower.endsWith(".js") || lower.endsWith(".ts")) {
    return <FileCode className="w-3.5 h-3.5 text-cyan-500 flex-shrink-0" />;
  }
  if (lower.endsWith(".java")) {
    return <FileCode className="w-3.5 h-3.5 text-[#F05323] flex-shrink-0" />;
  }
  if (lower.endsWith(".html") || lower.endsWith(".htm")) {
    return <FileCode className="w-3.5 h-3.5 text-orange-500 flex-shrink-0" />;
  }
  if (lower.endsWith(".css") || lower.endsWith(".scss") || lower.endsWith(".less")) {
    return <FileCode className="w-3.5 h-3.5 text-blue-500 flex-shrink-0" />;
  }
  if (lower.endsWith(".xml") || lower.endsWith(".svg")) {
    return <FileCode className="w-3.5 h-3.5 text-indigo-500 flex-shrink-0" />;
  }
  if (lower.endsWith(".json")) {
    return <FileJson className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />;
  }
  if (lower.endsWith(".sql")) {
    return <FileCode className="w-3.5 h-3.5 text-emerald-500 flex-shrink-0" />;
  }
  if (lower.endsWith(".py")) {
    return <FileCode className="w-3.5 h-3.5 text-yellow-500 flex-shrink-0" />;
  }
  if (lower.endsWith(".md") || lower.endsWith(".txt") || lower.endsWith(".properties") || lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".env")) {
    return <FileText className="w-3.5 h-3.5 text-gray-500 flex-shrink-0" />;
  }
  return <File className="w-3.5 h-3.5 text-gray-400 flex-shrink-0" />;
};

const FileTreeItem = ({
  node,
  activeFilePath,
  theme = "dark",
  onSelectFile,
  onInitiateCreate,
  onInitiateDelete,
  level = 0,
}: FileTreeItemProps) => {
  const [isOpen, setIsOpen] = useState(true);
  const isDirectory = node.type === "DIRECTORY";
  const isActive = activeFilePath === node.path || activeFilePath === `/${node.path}` || `/${activeFilePath}` === node.path;
  const isDark = theme === "dark";

  const handleClick = () => {
    if (isDirectory) {
      setIsOpen(!isOpen);
    } else {
      onSelectFile(node.path);
    }
  };

  return (
    <div className="group/item relative">
      <div
        style={{ paddingLeft: `${level * 14 + 6}px` }}
        className={cn(
          "w-full flex items-center justify-between py-1 pr-1.5 text-xs font-mono rounded-md text-left transition-colors cursor-pointer select-none",
          isActive
            ? isDark
              ? "bg-orange-950/60 text-orange-400 font-bold border-l-2 border-[#F05323]"
              : "bg-orange-50/90 text-[#F05323] font-bold"
            : isDark
            ? "text-slate-300 hover:bg-slate-800/80 hover:text-white"
            : "text-gray-600 hover:bg-gray-100/90 hover:text-gray-900"
        )}
        onClick={handleClick}
      >
        <div className="flex items-center gap-1.5 truncate min-w-0 flex-1">
          {isDirectory ? (
            <>
              <button
                type="button"
                className={cn(
                  "p-0.5 rounded",
                  isDark ? "hover:bg-slate-700/60 text-slate-400" : "hover:bg-gray-200/60 text-gray-400"
                )}
                onClick={(e) => {
                  e.stopPropagation();
                  setIsOpen(!isOpen);
                }}
              >
                {isOpen ? (
                  <ChevronDown className="w-3 h-3 flex-shrink-0" />
                ) : (
                  <ChevronRight className="w-3 h-3 flex-shrink-0" />
                )}
              </button>
              {isOpen ? (
                <FolderOpen className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />
              ) : (
                <Folder className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />
              )}
              <span className={cn("truncate font-semibold", isDark ? "text-slate-200" : "text-gray-700")}>
                {node.name}
              </span>
            </>
          ) : (
            <>
              <span className="w-3 flex-shrink-0" />
              {getFileIcon(node.name)}
              <span className="truncate">{node.name}</span>
            </>
          )}
        </div>

        {/* Hover Action Buttons */}
        <div
          className={cn(
            "hidden group-hover/item:flex items-center gap-0.5 ml-1 flex-shrink-0 shadow-2xs rounded px-1 border",
            isDark
              ? "bg-slate-800 border-slate-700 text-slate-300"
              : "bg-white/90 border-gray-200/80 text-gray-400"
          )}
        >
          {isDirectory && (
            <>
              <button
                type="button"
                title="New File in Folder"
                className={cn(
                  "p-1 rounded",
                  isDark ? "hover:text-[#F05323] hover:bg-slate-700" : "hover:text-[#F05323] hover:bg-gray-100"
                )}
                onClick={(e) => {
                  e.stopPropagation();
                  onInitiateCreate(node.path, "FILE");
                }}
              >
                <FilePlus className="w-3 h-3" />
              </button>
              <button
                type="button"
                title="New Folder in Folder"
                className={cn(
                  "p-1 rounded",
                  isDark ? "hover:text-amber-400 hover:bg-slate-700" : "hover:text-amber-600 hover:bg-gray-100"
                )}
                onClick={(e) => {
                  e.stopPropagation();
                  onInitiateCreate(node.path, "DIRECTORY");
                }}
              >
                <FolderPlus className="w-3 h-3" />
              </button>
            </>
          )}

          <button
            type="button"
            title={`Delete ${isDirectory ? "Folder" : "File"}`}
            className={cn(
              "p-1 rounded",
              isDark ? "hover:text-rose-400 hover:bg-rose-950/60" : "hover:text-rose-600 hover:bg-rose-50"
            )}
            onClick={(e) => {
              e.stopPropagation();
              onInitiateDelete(node);
            }}
          >
            <Trash2 className="w-3 h-3" />
          </button>
        </div>
      </div>

      {/* Recursive Children Rendering */}
      {isDirectory && isOpen && node.children && (
        <div className="space-y-0.5">
          {node.children.map((child) => (
            <FileTreeItem
              key={child.id || child.path}
              node={child}
              activeFilePath={activeFilePath}
              theme={theme}
              onSelectFile={onSelectFile}
              onInitiateCreate={onInitiateCreate}
              onInitiateDelete={onInitiateDelete}
              level={level + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export const FileExplorer = ({
  files,
  activeFilePath,
  theme = "dark",
  onSelectFile,
  onCreateFile,
  onDeleteFile,
  onRefresh,
  onCollapse,
}: FileExplorerProps) => {
  const isDark = theme === "dark";

  // New File / Folder inline creation state
  const [isCreating, setIsCreating] = useState(false);
  const [createType, setCreateType] = useState<"FILE" | "DIRECTORY">("FILE");
  const [createParentPath, setCreateParentPath] = useState<string>("");
  const [newPathInput, setNewPathInput] = useState<string>("");
  const [isSubmittingCreate, setIsSubmittingCreate] = useState(false);
  const [createError, setCreateError] = useState<string>("");

  // Delete Confirmation Modal State
  const [nodeToDelete, setNodeToDelete] = useState<FileNode | null>(null);
  const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
  const [deleteError, setDeleteError] = useState<string>("");

  const handleStartCreate = (parentPath: string = "", type: "FILE" | "DIRECTORY" = "FILE") => {
    setCreateParentPath(parentPath);
    setCreateType(type);
    setNewPathInput("");
    setCreateError("");
    setIsCreating(true);
  };

  const handleCancelCreate = () => {
    setIsCreating(false);
    setNewPathInput("");
    setCreateError("");
  };

  const handleConfirmCreate = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!newPathInput.trim()) {
      setCreateError("Name cannot be empty");
      return;
    }
    const cleanName = newPathInput.trim().replace(/^\/+/, "");
    const fullPath = createParentPath
      ? `${createParentPath.replace(/\/+$/, "")}/${cleanName}`
      : cleanName;

    setIsSubmittingCreate(true);
    setCreateError("");

    try {
      if (onCreateFile) {
        await onCreateFile(fullPath, createType);
      }
      setIsCreating(false);
      setNewPathInput("");
      if (createType === "FILE") {
        onSelectFile(fullPath);
      }
    } catch (err: any) {
      console.error("File creation error:", err);
      setCreateError(err.message || "Failed to create item");
    } finally {
      setIsSubmittingCreate(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!nodeToDelete) return;
    setIsSubmittingDelete(true);
    setDeleteError("");

    try {
      if (onDeleteFile) {
        await onDeleteFile(nodeToDelete.path);
      }
      setNodeToDelete(null);
    } catch (err: any) {
      console.error("File delete error:", err);
      setDeleteError(err.message || "Failed to delete item");
    } finally {
      setIsSubmittingDelete(false);
    }
  };

  return (
    <div
      className={cn(
        "h-full flex flex-col select-none transition-colors border-r",
        isDark
          ? "bg-[#0F172A] border-slate-800 text-slate-200"
          : "bg-gray-50/70 border-gray-200 text-gray-700"
      )}
    >
      {/* File Explorer Header Bar */}
      <div
        className={cn(
          "h-10 px-3 border-b flex items-center justify-between transition-colors flex-shrink-0",
          isDark
            ? "bg-[#1E293B] border-slate-800 text-slate-300"
            : "bg-gray-100/60 border-gray-200 text-gray-600"
        )}
      >
        <span className="text-[11px] font-bold uppercase tracking-wider">
          Explorer
        </span>

        <div className="flex items-center gap-1">
          <button
            type="button"
            title="New File"
            onClick={() => handleStartCreate("", "FILE")}
            className={cn(
              "p-1 rounded transition-colors",
              isDark
                ? "text-slate-400 hover:text-[#F05323] hover:bg-slate-800"
                : "text-gray-500 hover:text-[#F05323] hover:bg-gray-200/70"
            )}
          >
            <FilePlus className="w-3.5 h-3.5" />
          </button>
          <button
            type="button"
            title="New Folder"
            onClick={() => handleStartCreate("", "DIRECTORY")}
            className={cn(
              "p-1 rounded transition-colors",
              isDark
                ? "text-slate-400 hover:text-amber-400 hover:bg-slate-800"
                : "text-gray-500 hover:text-amber-600 hover:bg-gray-200/70"
            )}
          >
            <FolderPlus className="w-3.5 h-3.5" />
          </button>
          {onRefresh && (
            <button
              type="button"
              title="Refresh Files"
              onClick={onRefresh}
              className={cn(
                "p-1 rounded transition-colors",
                isDark
                  ? "text-slate-400 hover:text-slate-200 hover:bg-slate-800"
                  : "text-gray-500 hover:text-gray-800 hover:bg-gray-200/70"
              )}
            >
              <RefreshCw className="w-3.5 h-3.5" />
            </button>
          )}
          {onCollapse && (
            <button
              type="button"
              title="Collapse Sidebar"
              onClick={onCollapse}
              className={cn(
                "p-1 rounded transition-colors ml-0.5",
                isDark
                  ? "text-slate-400 hover:text-slate-200 hover:bg-slate-800"
                  : "text-gray-500 hover:text-gray-800 hover:bg-gray-200/70"
              )}
            >
              <PanelLeftClose className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      </div>

      {/* Inline Create Input (if open at root level) */}
      {isCreating && (
        <form
          onSubmit={handleConfirmCreate}
          className={cn(
            "p-2 border-b space-y-1 transition-colors",
            isDark
              ? "bg-slate-900 border-slate-700"
              : "border-orange-200 bg-orange-50/50"
          )}
        >
          <div className="flex items-center gap-1.5 text-[11px]">
            {createType === "FILE" ? (
              <FilePlus className="w-3.5 h-3.5 text-[#F05323]" />
            ) : (
              <FolderPlus className="w-3.5 h-3.5 text-amber-500" />
            )}
            <span
              className={cn(
                "font-semibold truncate",
                isDark ? "text-slate-300" : "text-gray-700"
              )}
            >
              New {createType === "FILE" ? "File" : "Folder"}
              {createParentPath ? ` in /${createParentPath}` : " at root"}
            </span>
          </div>

          <div className="flex items-center gap-1">
            <input
              type="text"
              autoFocus
              placeholder={createType === "FILE" ? "e.g. UserService.java" : "e.g. service"}
              value={newPathInput}
              onChange={(e) => setNewPathInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Escape") handleCancelCreate();
              }}
              className={cn(
                "flex-1 px-2 py-1 text-xs font-mono border rounded focus:outline-none focus:ring-1 focus:ring-[#F05323]",
                isDark
                  ? "bg-slate-950 border-slate-700 text-white placeholder-slate-500"
                  : "bg-white border-gray-300 text-gray-900"
              )}
            />
            <button
              type="submit"
              disabled={isSubmittingCreate}
              className="p-1 bg-[#F05323] text-white rounded hover:bg-[#d94417] disabled:opacity-50"
              title="Confirm"
            >
              {isSubmittingCreate ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <Check className="w-3.5 h-3.5" />
              )}
            </button>
            <button
              type="button"
              onClick={handleCancelCreate}
              className={cn(
                "p-1 rounded",
                isDark
                  ? "text-slate-400 hover:text-white hover:bg-slate-800"
                  : "text-gray-500 hover:text-gray-800 hover:bg-gray-200"
              )}
              title="Cancel"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
          {createError && (
            <p className="text-[10px] text-rose-500 font-semibold px-1">{createError}</p>
          )}
        </form>
      )}

      {/* Hierarchical File Tree View */}
      <div className="flex-1 p-2 overflow-y-auto space-y-0.5">
        {files.length === 0 ? (
          <div className="p-4 text-center text-xs text-gray-400 space-y-2">
            <p>No files in repository sandbox.</p>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleStartCreate("", "FILE")}
              className="text-xs gap-1.5"
            >
              <FilePlus className="w-3 h-3" /> Create First File
            </Button>
          </div>
        ) : (
          files.map((node) => (
            <FileTreeItem
              key={node.id || node.path}
              node={node}
              activeFilePath={activeFilePath}
              theme={theme}
              onSelectFile={onSelectFile}
              onInitiateCreate={handleStartCreate}
              onInitiateDelete={(targetNode) => {
                setDeleteError("");
                setNodeToDelete(targetNode);
              }}
            />
          ))
        )}
      </div>

      {/* Delete Confirmation Modal Dialog */}
      {nodeToDelete && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div
            className={cn(
              "rounded-2xl max-w-sm w-full p-5 space-y-4 shadow-2xl border animate-in zoom-in-95",
              isDark
                ? "bg-slate-900 border-slate-700 text-slate-100"
                : "bg-white border-gray-100 text-gray-900"
            )}
          >
            <div className="w-10 h-10 rounded-full bg-rose-500/10 text-rose-500 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-5 h-5" />
            </div>

            <div className="text-center space-y-1">
              <h4 className="text-sm font-bold">
                Delete {nodeToDelete.type === "DIRECTORY" ? "Folder" : "File"}?
              </h4>
              <p
                className={cn(
                  "text-xs break-all",
                  isDark ? "text-slate-400" : "text-gray-500"
                )}
              >
                Are you sure you want to permanently delete{" "}
                <strong className={cn("font-mono", isDark ? "text-slate-200" : "text-gray-800")}>
                  {nodeToDelete.path}
                </strong>?
              </p>
            </div>

            {deleteError && (
              <p className="text-xs text-rose-500 font-semibold text-center bg-rose-500/10 p-2 rounded-lg">
                {deleteError}
              </p>
            )}

            <div className="flex items-center gap-2 pt-2">
              <Button
                variant="outline"
                size="sm"
                className={cn(
                  "flex-1 text-xs",
                  isDark ? "border-slate-700 text-slate-300 hover:bg-slate-800" : ""
                )}
                disabled={isSubmittingDelete}
                onClick={() => setNodeToDelete(null)}
              >
                Cancel
              </Button>
              <Button
                size="sm"
                disabled={isSubmittingDelete}
                className="flex-1 text-xs bg-rose-600 hover:bg-rose-700 text-white gap-1.5"
                onClick={handleConfirmDelete}
              >
                {isSubmittingDelete ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Trash2 className="w-3.5 h-3.5" />
                )}
                Delete
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
