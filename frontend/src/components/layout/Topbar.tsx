import { useLocation } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";
import {
  Calendar,
  Bell,
} from "lucide-react";
import { ThemeToggle } from "@/components/common/ThemeToggle";

interface TopbarProps {
  title?: string;
  subtitle?: string;
}

export const Topbar = ({ title, subtitle }: TopbarProps) => {
  const location = useLocation();
  const { user } = useAuthStore();
  const dateRange = "August 2026";

  // Dynamic route-based title & subtitle
  let displayTitle = title || "HR Dashboard";
  let displaySubtitle = subtitle || "Overview of all assessment and hiring activity";

  if (!title) {
    if (location.pathname === "/dashboard") {
      displayTitle = "HR Dashboard";
      displaySubtitle = "Overview of placement drives, active workspaces, and benchmarks";
    } else if (location.pathname === "/dashboard/workspaces") {
      displayTitle = "Workspaces";
      displaySubtitle = "Folder directory of placement drives and assessment hubs";
    } else if (location.pathname.startsWith("/dashboard/workspaces/") && location.pathname.includes("/candidates/")) {
      displayTitle = "Candidate Assessment Details";
      displaySubtitle = "Configure repository and schedule assessment attendance window";
    } else if (location.pathname.startsWith("/dashboard/workspaces/")) {
      displayTitle = "Workspace Details";
      displaySubtitle = "Inspect workspace candidates, add profiles, and track assessment status";
    } else if (location.pathname === "/dashboard/reports") {
      displayTitle = "Reports & Analytics";
      displaySubtitle = "Comprehensive candidate performance, score distributions, and pass rates";
    } else if (location.pathname === "/dashboard/selected-candidates") {
      displayTitle = "Selected Candidates";
      displaySubtitle = "Candidates who successfully demonstrated engineering mastery";
    } else if (location.pathname.startsWith("/dashboard/candidates/")) {
      displayTitle = "Technical Evaluation Report";
      displaySubtitle = "Automated black-box test results, code metrics, and integrity insights";
    }
  }

  return (
    <header className="h-20 bg-white dark:bg-slate-900 border-b border-gray-200/80 dark:border-slate-800 px-6 sm:px-8 flex items-center justify-between sticky top-0 z-30 shadow-2xs transition-colors duration-200">
      {/* Title & Subtitle */}
      <div>
        <h1 className="text-xl sm:text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight leading-tight">
          {displayTitle}
        </h1>
        {displaySubtitle && (
          <p className="text-xs text-gray-500 dark:text-slate-400 font-medium">{displaySubtitle}</p>
        )}
      </div>

      {/* Action Controls & User Profile */}
      <div className="flex items-center gap-3">
        {/* Date Filter Badge */}
        <div className="hidden lg:flex items-center gap-2 h-9 px-3 bg-gray-50 dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-xl text-xs font-medium text-gray-700 dark:text-slate-300 shadow-2xs">
          <Calendar className="w-3.5 h-3.5 text-gray-500 dark:text-slate-400" />
          <span>{dateRange}</span>
        </div>

        {/* Notifications */}
        <button
          type="button"
          className="relative w-9 h-9 rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-gray-50 dark:hover:bg-slate-700 flex items-center justify-center text-gray-500 dark:text-slate-400 transition-colors"
          title="Notifications"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-primary"></span>
        </button>

        {/* Dark Mode Theme Switcher */}
        <ThemeToggle size="md" />

        <div className="h-6 w-px bg-gray-200 dark:bg-slate-700 mx-1 hidden sm:block"></div>

        {/* User Avatar & Info */}
        <div className="flex items-center gap-3 pl-1">
          <img
            src={
              user?.avatarUrl ||
              "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"
            }
            alt={user?.name || "User Avatar"}
            className="w-9 h-9 rounded-full object-cover border border-primary-border dark:border-primary/30 shadow-2xs"
          />
          <div className="hidden md:block text-left">
            <span className="text-xs font-bold text-gray-900 dark:text-slate-100 block leading-tight">
              {user?.name || "Rahul Sharma"}
            </span>
            <span className="text-[10px] font-semibold text-gray-400 dark:text-slate-500 uppercase tracking-wider block">
              {user?.role || "HR RECRUITER"}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};
