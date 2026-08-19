import { useState } from "react";
import { Search, Filter, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";

interface DashboardFilterBarProps {
  onSearch?: (query: string) => void;
  onTechStackChange?: (tech: string) => void;
  onStatusChange?: (status: string) => void;
}

export const DashboardFilterBar = ({
  onSearch,
  onTechStackChange,
  onStatusChange,
}: DashboardFilterBarProps) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [techStack, setTechStack] = useState("ALL");
  const [status, setStatus] = useState("ALL");

  const handleClear = () => {
    setSearchQuery("");
    setTechStack("ALL");
    setStatus("ALL");
    onSearch?.("");
    onTechStackChange?.("ALL");
    onStatusChange?.("ALL");
  };

  return (
    <div className="bg-white border border-gray-200/90 rounded-2xl p-4 sm:p-5 shadow-sm space-y-3">
      <div className="text-xs font-bold uppercase tracking-wider text-gray-500">
        Filters & Search
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 items-center">
        {/* Tech Stack Select */}
        <div className="sm:col-span-3">
          <select
            value={techStack}
            onChange={(e) => {
              setTechStack(e.target.value);
              onTechStackChange?.(e.target.value);
            }}
            className="w-full h-10 rounded-xl border border-gray-200 bg-white px-3 text-xs font-semibold text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#F05323] shadow-xs"
          >
            <option value="ALL">All Tech Stacks</option>
            <option value="JAVA">Java, Spring Boot</option>
            <option value="REACT">React, Node.js</option>
            <option value="PYTHON">Python, Django</option>
          </select>
        </div>

        {/* Status Select */}
        <div className="sm:col-span-3">
          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              onStatusChange?.(e.target.value);
            }}
            className="w-full h-10 rounded-xl border border-gray-200 bg-white px-3 text-xs font-semibold text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#F05323] shadow-xs"
          >
            <option value="ALL">All Statuses</option>
            <option value="COMPLETED">Completed</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="SCHEDULED">Not Started / Scheduled</option>
          </select>
        </div>

        {/* Search Query Input */}
        <div className="sm:col-span-4 relative">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search candidates, projects..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              onSearch?.(e.target.value);
            }}
            className="w-full h-10 pl-9 pr-3 rounded-xl border border-gray-200 bg-white text-xs text-gray-800 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-[#F05323] shadow-xs"
          />
        </div>

        {/* Action Buttons */}
        <div className="sm:col-span-2 flex items-center gap-2">
          <Button
            size="sm"
            variant="outline"
            className="flex-1 h-10 text-xs font-semibold gap-1.5"
          >
            <Filter className="w-3.5 h-3.5 text-gray-500" />
            Filter
          </Button>
          <Button
            size="sm"
            variant="outline"
            onClick={handleClear}
            className="h-10 text-xs font-semibold gap-1.5 text-gray-500 hover:text-gray-900"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Clear
          </Button>
        </div>
      </div>
    </div>
  );
};
