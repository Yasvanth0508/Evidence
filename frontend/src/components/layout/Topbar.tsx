import { useAuthStore } from "@/store/authStore";
import { Button } from "@/components/ui/button";
import {
  Calendar,
  Filter,
  Download,
  Bell,
} from "lucide-react";

interface TopbarProps {
  title?: string;
  subtitle?: string;
  showBackToCandidates?: boolean;
}

export const Topbar = ({
  title = "HR Dashboard",
  subtitle = "Overview of all assessment activity",
  showBackToCandidates = false,
}: TopbarProps) => {
  const { user } = useAuthStore();
  const dateRange = "May 12 – May 18, 2026";

  return (
    <header className="h-20 bg-white border-b border-gray-200/80 px-6 sm:px-8 flex items-center justify-between sticky top-0 z-30">
      {/* Title & Breadcrumbs */}
      <div>
        {showBackToCandidates && (
          <a
            href="/dashboard"
            className="text-xs text-gray-500 hover:text-gray-900 font-medium mb-0.5 inline-flex items-center gap-1"
          >
            ← Back to Candidates
          </a>
        )}
        <h1 className="text-xl sm:text-2xl font-extrabold text-gray-900 tracking-tight leading-tight">
          {title}
        </h1>
        {subtitle && (
          <p className="text-xs text-gray-500 font-medium">{subtitle}</p>
        )}
      </div>

      {/* Action Controls & User Profile */}
      <div className="flex items-center gap-3">
        {/* Date Filter */}
        <div className="hidden lg:flex items-center gap-2 h-9 px-3 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 shadow-sm cursor-pointer hover:bg-gray-100/80 transition-colors">
          <Calendar className="w-3.5 h-3.5 text-gray-500" />
          <span>{dateRange}</span>
        </div>

        {/* Filter Button */}
        <Button
          variant="outline"
          size="sm"
          className="hidden sm:inline-flex items-center gap-1.5 h-9 text-xs font-semibold text-gray-700"
        >
          <Filter className="w-3.5 h-3.5 text-gray-500" />
          <span>Filter</span>
        </Button>

        {/* Export Button */}
        <Button
          variant="outline"
          size="sm"
          className="hidden sm:inline-flex items-center gap-1.5 h-9 text-xs font-semibold text-gray-700"
          onClick={() => alert("Exporting report...")}
        >
          <Download className="w-3.5 h-3.5 text-gray-500" />
          <span>Export</span>
        </Button>

        {/* Notifications */}
        <button className="relative w-9 h-9 rounded-xl border border-gray-200 bg-white hover:bg-gray-50 flex items-center justify-center text-gray-500 transition-colors">
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-[#F05323]"></span>
        </button>

        <div className="h-6 w-px bg-gray-200 mx-1 hidden sm:block"></div>

        {/* User Avatar & Info */}
        <div className="flex items-center gap-3 pl-1">
          <img
            src={
              user?.avatarUrl ||
              "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"
            }
            alt={user?.name || "User Avatar"}
            className="w-9 h-9 rounded-full object-cover border border-orange-200 shadow-sm"
          />
          <div className="hidden md:block text-left">
            <span className="text-xs font-bold text-gray-900 block leading-tight">
              {user?.name || "Rahul Sharma"}
            </span>
            <span className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider block">
              {user?.role || "RECRUITER"}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};
