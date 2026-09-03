import { Link, useLocation, useNavigate } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/store/authStore";
import {
  LayoutDashboard,
  FolderGit2,
  FileSpreadsheet,
  UserCheck2,
  LogOut,
  UserCheck,
} from "lucide-react";
import { cn } from "@/lib/utils";

// Exactly 4 main navigation options required for HR Dashboard
const NAV_ITEMS = [
  { label: "Dashboard", icon: LayoutDashboard, path: "/dashboard", exact: true },
  { label: "Workspace", icon: FolderGit2, path: "/dashboard/workspaces", exact: false },
  { label: "Reports", icon: FileSpreadsheet, path: "/dashboard/reports", exact: false },
  { label: "Selected Candidates", icon: UserCheck2, path: "/dashboard/selected-candidates", exact: false },
];

export const Sidebar = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <aside className="w-64 bg-white dark:bg-slate-900 border-r border-gray-200/90 dark:border-slate-800 flex flex-col justify-between h-screen sticky top-0 flex-shrink-0 z-40 transition-colors duration-200">
      {/* 1. Brand Logo */}
      <div>
        <div className="h-20 px-6 flex items-center border-b border-gray-100 dark:border-slate-800">
          <Logo size="md" />
        </div>

        {/* 2. Navigation Menu - Exactly 4 Main Options */}
        <nav className="p-4 space-y-1.5 overflow-y-auto">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive = item.exact
              ? location.pathname === item.path
              : location.pathname.startsWith(item.path);

            return (
              <Link
                key={item.label}
                to={item.path}
                className={cn(
                  "flex items-center gap-3 px-3.5 py-3 rounded-xl text-sm font-medium transition-all group",
                  isActive
                    ? "bg-primary-light text-primary font-bold shadow-2xs border border-primary-border dark:bg-primary/20 dark:border-primary/30 dark:text-primary"
                    : "text-gray-600 hover:text-gray-900 hover:bg-gray-50 dark:text-slate-400 dark:hover:text-slate-100 dark:hover:bg-slate-800/60"
                )}
              >
                <Icon
                  className={cn(
                    "w-4 h-4 transition-colors",
                    isActive
                      ? "text-primary dark:text-primary"
                      : "text-gray-400 group-hover:text-gray-600 dark:text-slate-500 dark:group-hover:text-slate-300"
                  )}
                />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      {/* 3. Bottom Recruiter Profile & Logout Card */}
      <div className="p-4 border-t border-gray-100 dark:border-slate-800 space-y-3">
        <div className="bg-primary-light/50 border border-primary-border/60 dark:bg-slate-800/80 dark:border-slate-700/80 rounded-2xl p-3.5 space-y-2.5">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-primary-light dark:bg-primary/30 flex items-center justify-center text-primary dark:text-primary">
              <UserCheck className="w-4 h-4" />
            </div>
            <div className="overflow-hidden">
              <span className="text-[10px] uppercase font-bold text-primary dark:text-primary tracking-wider block">
                {user?.role || ""}
              </span>
              <p className="text-xs text-gray-700 dark:text-slate-200 font-semibold truncate leading-tight">
                {user?.name || ""}
              </p>
              <p className="text-[11px] text-gray-500 dark:text-slate-400 truncate leading-tight">
                {user?.email || ""}
              </p>
            </div>
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={handleLogout}
            className="w-full h-8 text-xs font-semibold text-gray-700 dark:text-slate-300 bg-white dark:bg-slate-800 hover:bg-rose-50 hover:text-rose-600 hover:border-rose-200 dark:hover:bg-rose-950/30 dark:hover:text-rose-400 dark:hover:border-rose-900"
          >
            <LogOut className="w-3.5 h-3.5 mr-1.5" /> Sign Out
          </Button>
        </div>
      </div>
    </aside>
  );
};
