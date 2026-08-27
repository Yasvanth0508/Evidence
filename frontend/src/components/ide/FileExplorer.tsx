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
  onSelectFile: (path: string) => void;
  onCreateFile?: (path: string, type: "FILE" | "DIRECTORY") => Promise<void>;
  onDeleteFile?: (path: string) => Promise<void>;
  onRefresh?: () => void;
  onCollapse?: () => void;
}

interface FileTreeItemProps {
  node: FileNode;
  activeFilePath: string;
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
  onSelectFile,
  onInitiateCreate,
  onInitiateDelete,
  level = 0,
}: FileTreeItemProps) => {
  const [isOpen, setIsOpen] = useState(true);
  const isDirectory = node.type === "DIRECTORY";
  const isActive = activeFilePath === node.path || activeFilePath === `/${node.path}` || `/${activeFilePath}` === node.path;

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
            ? "bg-orange-50/90 text-[#F05323] font-bold"
            : "text-gray-600 hover:bg-gray-100/90 hover:text-gray-900"
        )}
        onClick={handleClick}
      >
        <div className="flex items-center gap-1.5 truncate min-w-0 flex-1">
          {isDirectory ? (
            <>
              <button
                type="button"
                className="p-0.5 hover:bg-gray-200/60 rounded"
                onClick={(e) => {
                  e.stopPropagation();
                  setIsOpen(!isOpen);
                }}
              >
                {isOpen ? (
                  <ChevronDown className="w-3 h-3 text-gray-400 flex-shrink-0" />
                ) : (
                  <ChevronRight className="w-3 h-3 text-gray-400 flex-shrink-0" />
                )}
              </button>
              {isOpen ? (
                <FolderOpen className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />
              ) : (
                <Folder className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />
              )}
              <span className="truncate font-semibold text-gray-700">{node.name}</span>
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
        <div className="hidden group-hover/item:flex items-center gap-0.5 ml-1 flex-shrink-0 bg-white/90 shadow-2xs rounded px-1 border border-gray-200/80">
          {isDirectory && (
            <>
              <button
                type="button"
                title="New File in Folder"
                className="p-1 hover:text-[#F05323] text-gray-400 hover:bg-gray-100 rounded"
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
                className="p-1 hover:text-amber-600 text-gray-400 hover:bg-gray-100 rounded"
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
            className="p-1 hover:text-rose-600 text-gray-400 hover:bg-rose-50 rounded"
            onClick={(e) => {
              e.stopPropagation();
              onInitiateDelete(node);
            }}
          >
            <Trash2 className="w-3 h-3" />
          </button>
        </div>
      </div>

      {isDirectory && isOpen && node.children && (
        <div className="space-y-0.5">
          {node.children.map((child) => (
            <FileTreeItem
              key={child.id || child.path}
              node={child}
              activeFilePath={activeFilePath}
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
  onSelectFile,
  onCreateFile,
  onDeleteFile,
  onRefresh,
  onCollapse,
}: FileExplorerProps) => {
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
    <div className="h-full flex flex-col bg-gray-50/70 border-r border-gray-200 select-none">
      {/* File Explorer Header Bar */}
      <div className="h-10 px-3 border-b border-gray-200 flex items-center justify-between bg-gray-100/60">
        <span className="text-[11px] font-bold uppercase tracking-wider text-gray-600">
          Explorer
        </span>

        <div className="flex items-center gap-1">
          <button
            type="button"
            title="New File"
            onClick={() => handleStartCreate("", "FILE")}
            className="p-1 text-gray-500 hover:text-[#F05323] hover:bg-gray-200/70 rounded transition-colors"
          >
            <FilePlus className="w-3.5 h-3.5" />
          </button>
          <button
            type="button"
            title="New Folder"
            onClick={() => handleStartCreate("", "DIRECTORY")}
            className="p-1 text-gray-500 hover:text-amber-600 hover:bg-gray-200/70 rounded transition-colors"
          >
            <FolderPlus className="w-3.5 h-3.5" />
          </button>
          {onRefresh && (
            <button
              type="button"
              title="Refresh Files"
              onClick={onRefresh}
              className="p-1 text-gray-500 hover:text-gray-800 hover:bg-gray-200/70 rounded transition-colors"
            >
              <RefreshCw className="w-3.5 h-3.5" />
            </button>
          )}
          {onCollapse && (
            <button
              type="button"
              title="Collapse Sidebar"
              onClick={onCollapse}
              className="p-1 text-gray-500 hover:text-gray-800 hover:bg-gray-200/70 rounded transition-colors ml-0.5"
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
          className="p-2 border-b border-orange-200 bg-orange-50/50 space-y-1"
        >
          <div className="flex items-center gap-1.5 text-[11px] text-gray-500">
            {createType === "FILE" ? (
              <FilePlus className="w-3.5 h-3.5 text-[#F05323]" />
            ) : (
              <FolderPlus className="w-3.5 h-3.5 text-amber-600" />
            )}
            <span className="font-semibold text-gray-700 truncate">
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
              className="flex-1 px-2 py-1 text-xs font-mono bg-white border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-[#F05323]"
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
              className="p-1 text-gray-500 hover:text-gray-800 hover:bg-gray-200 rounded"
              title="Cancel"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
          {createError && (
            <p className="text-[10px] text-rose-600 font-semibold px-1">{createError}</p>
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
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-sm w-full p-5 space-y-4 shadow-2xl border border-gray-100 animate-in zoom-in-95">
            <div className="w-10 h-10 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-5 h-5" />
            </div>

            <div className="text-center space-y-1">
              <h4 className="text-sm font-bold text-gray-900">
                Delete {nodeToDelete.type === "DIRECTORY" ? "Folder" : "File"}?
              </h4>
              <p className="text-xs text-gray-500 break-all">
                Are you sure you want to permanently delete{" "}
                <strong className="text-gray-800 font-mono">{nodeToDelete.path}</strong>?
              </p>
            </div>

            {deleteError && (
              <p className="text-xs text-rose-600 font-semibold text-center bg-rose-50 p-2 rounded-lg">
                {deleteError}
              </p>
            )}

            <div className="flex items-center gap-2 pt-2">
              <Button
                variant="outline"
                size="sm"
                className="flex-1 text-xs"
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
