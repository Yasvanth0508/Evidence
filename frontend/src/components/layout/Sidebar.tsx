import { Link, useLocation, useNavigate } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/store/authStore";
import {
  LayoutDashboard,
  FileCheck2,
  Users2,
  FolderGit2,
  HelpCircle,
  UserCog,
  FileSpreadsheet,
  Blocks,
  Settings,
  LogOut,
  UserCheck,
} from "lucide-react";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { label: "Overview", icon: LayoutDashboard, path: "/dashboard" },
  { label: "Assessments", icon: FileCheck2, path: "/dashboard/assessments" },
  { label: "Candidates", icon: Users2, path: "/dashboard/candidates" },
  { label: "Projects", icon: FolderGit2, path: "/dashboard/projects" },
  { label: "Question Bank", icon: HelpCircle, path: "/dashboard/questions" },
  { label: "Users", icon: UserCog, path: "/dashboard/users" },
  { label: "Reports", icon: FileSpreadsheet, path: "/dashboard/reports" },
  { label: "Integrations", icon: Blocks, path: "/dashboard/integrations" },
  { label: "Settings", icon: Settings, path: "/dashboard/settings" },
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
    <aside className="w-64 bg-white border-r border-gray-200/90 flex flex-col justify-between h-screen sticky top-0 flex-shrink-0 z-40">
      {/* 1. Brand Logo */}
      <div>
        <div className="h-20 px-6 flex items-center border-b border-gray-100">
          <Logo size="md" />
        </div>

        {/* 2. Navigation Menu */}
        <nav className="p-4 space-y-1.5 overflow-y-auto max-h-[calc(100vh-280px)]">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive =
              location.pathname === item.path ||
              (item.path !== "/dashboard" && location.pathname.startsWith(item.path));

            return (
              <Link
                key={item.label}
                to={item.path === "/dashboard" || item.path === "/dashboard/candidates" ? item.path : "/dashboard"}
                className={cn(
                  "flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all group",
                  isActive
                    ? "bg-orange-50/80 text-[#F05323] font-semibold"
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
        <div className="bg-orange-50/50 border border-orange-100/70 rounded-2xl p-3.5 space-y-2">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-orange-100 flex items-center justify-center text-[#F05323]">
              <UserCheck className="w-4 h-4" />
            </div>
            <div>
              <span className="text-[10px] uppercase font-bold text-[#F05323] tracking-wider block">
                {user?.role || "RECRUITER"}
              </span>
              <p className="text-xs text-gray-600 line-clamp-1 leading-tight font-medium">
                Access to manage assessments and candidates.
              </p>
            </div>
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={handleLogout}
            className="w-full h-8 text-xs font-semibold text-gray-700 bg-white hover:bg-rose-50 hover:text-rose-600 hover:border-rose-200"
          >
            <LogOut className="w-3.5 h-3.5 mr-1.5" /> Logout
          </Button>
        </div>
      </div>
    </aside>
  );
};
