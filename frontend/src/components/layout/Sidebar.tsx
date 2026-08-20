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
    navigate("/login?role=recruiter");
  };

  return (
    <aside className="w-64 bg-white border-r border-gray-200/90 flex flex-col justify-between h-screen sticky top-0 flex-shrink-0 z-40">
      {/* 1. Brand Logo */}
      <div>
        <div className="h-20 px-6 flex items-center border-b border-gray-100">
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
                    ? "bg-orange-50 text-[#F05323] font-bold shadow-2xs border border-orange-200/60"
                    : "text-gray-600 hover:text-gray-900 hover:bg-gray-50"
                )}
              >
                <Icon
                  className={cn(
                    "w-4 h-4 transition-colors",
                    isActive ? "text-[#F05323]" : "text-gray-400 group-hover:text-gray-600"
                  )}
                />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      {/* 3. Bottom Recruiter Profile & Logout Card */}
      <div className="p-4 border-t border-gray-100 space-y-3">
        <div className="bg-orange-50/50 border border-orange-100/70 rounded-2xl p-3.5 space-y-2.5">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-orange-100 flex items-center justify-center text-[#F05323]">
              <UserCheck className="w-4 h-4" />
            </div>
            <div className="overflow-hidden">
              <span className="text-[10px] uppercase font-bold text-[#F05323] tracking-wider block">
                {user?.role || "HR RECRUITER"}
              </span>
              <p className="text-xs text-gray-700 font-semibold truncate leading-tight">
                {user?.name || "Rahul Sharma"}
              </p>
              <p className="text-[11px] text-gray-500 truncate leading-tight">
                {user?.email || "recruiter@example.com"}
              </p>
            </div>
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={handleLogout}
            className="w-full h-8 text-xs font-semibold text-gray-700 bg-white hover:bg-rose-50 hover:text-rose-600 hover:border-rose-200"
          >
            <LogOut className="w-3.5 h-3.5 mr-1.5" /> Sign Out
          </Button>
        </div>
      </div>
    </aside>
  );
};
