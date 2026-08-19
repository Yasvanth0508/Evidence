import { useState } from "react";
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
} from "lucide-react";
import { cn } from "@/lib/utils";

interface FileExplorerProps {
  files: FileNode[];
  activeFilePath: string;
  onSelectFile: (path: string) => void;
}

interface FileTreeItemProps {
  node: FileNode;
  activeFilePath: string;
  onSelectFile: (path: string) => void;
  level?: number;
}

const getFileIcon = (fileName: string) => {
  if (fileName.endsWith(".java") || fileName.endsWith(".js") || fileName.endsWith(".ts")) {
    return <FileCode className="w-3.5 h-3.5 text-[#F05323]" />;
  }
  if (fileName.endsWith(".xml") || fileName.endsWith(".html")) {
    return <FileCode className="w-3.5 h-3.5 text-blue-500" />;
  }
  if (fileName.endsWith(".json")) {
    return <FileJson className="w-3.5 h-3.5 text-amber-500" />;
  }
  if (fileName.endsWith(".md") || fileName.endsWith(".txt") || fileName.endsWith(".properties")) {
    return <FileText className="w-3.5 h-3.5 text-gray-500" />;
  }
  return <File className="w-3.5 h-3.5 text-gray-400" />;
};

const FileTreeItem = ({
  node,
  activeFilePath,
  onSelectFile,
  level = 0,
}: FileTreeItemProps) => {
  const [isOpen, setIsOpen] = useState(true);
  const isDirectory = node.type === "DIRECTORY";
  const isActive = activeFilePath === node.path;

  const handleClick = () => {
    if (isDirectory) {
      setIsOpen(!isOpen);
    } else {
      onSelectFile(node.path);
    }
  };

  return (
    <div>
      <button
        type="button"
        onClick={handleClick}
        style={{ paddingLeft: `${level * 12 + 8}px` }}
        className={cn(
          "w-full flex items-center gap-1.5 py-1 pr-2 text-xs font-mono rounded-lg text-left transition-colors group select-none",
          isActive
            ? "bg-orange-50 text-[#F05323] font-bold"
            : "text-gray-600 hover:bg-gray-100/80 hover:text-gray-900"
        )}
      >
        {isDirectory ? (
          <>
            {isOpen ? (
              <ChevronDown className="w-3 h-3 text-gray-400" />
            ) : (
              <ChevronRight className="w-3 h-3 text-gray-400" />
            )}
            {isOpen ? (
              <FolderOpen className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />
            ) : (
              <Folder className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />
            )}
            <span className="truncate font-semibold text-gray-700">{node.name}</span>
          </>
        ) : (
          <>
            <span className="w-3" />
            {getFileIcon(node.name)}
            <span className="truncate">{node.name}</span>
          </>
        )}
      </button>

      {isDirectory && isOpen && node.children && (
        <div>
          {node.children.map((child) => (
            <FileTreeItem
              key={child.id || child.path}
              node={child}
              activeFilePath={activeFilePath}
              onSelectFile={onSelectFile}
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
}: FileExplorerProps) => {
  return (
    <div className="h-full flex flex-col bg-gray-50/60 border-r border-gray-200">
      <div className="p-3 border-b border-gray-200 flex items-center justify-between">
        <span className="text-[10px] font-bold uppercase tracking-wider text-gray-500">
          Explorer
        </span>
      </div>

      <div className="flex-1 p-2 overflow-y-auto space-y-0.5">
        {files.map((node) => (
          <FileTreeItem
            key={node.id || node.path}
            node={node}
            activeFilePath={activeFilePath}
            onSelectFile={onSelectFile}
          />
        ))}
      </div>
    </div>
  );
};
