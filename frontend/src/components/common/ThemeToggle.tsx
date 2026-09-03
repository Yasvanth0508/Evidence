import { useThemeStore } from "@/store/themeStore";
import { Sun, Moon } from "lucide-react";
import { cn } from "@/lib/utils";

interface ThemeToggleProps {
  className?: string;
  size?: "sm" | "md" | "lg";
  showLabel?: boolean;
}

/**
 * Standard inline Theme Toggle button for headers, topbars, and navbars.
 */
export const ThemeToggle = ({
  className,
  size = "md",
  showLabel = false,
}: ThemeToggleProps) => {
  const { theme, toggleTheme } = useThemeStore();
  const isDark = theme === "dark";

  const sizeClasses = {
    sm: "w-8 h-8 rounded-lg text-xs",
    md: "w-9 h-9 rounded-xl text-sm",
    lg: "w-10 h-10 rounded-xl text-base",
  };

  const iconSizes = {
    sm: "w-3.5 h-3.5",
    md: "w-4 h-4",
    lg: "w-5 h-5",
  };

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className={cn(
        "relative flex items-center justify-center transition-all duration-200 border cursor-pointer select-none",
        "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2",
        isDark
          ? "bg-slate-800/90 border-slate-700 text-amber-300 hover:bg-slate-700 hover:text-amber-200 shadow-sm shadow-black/20"
          : "bg-white border-gray-200 text-gray-700 hover:bg-gray-100 hover:text-gray-900 shadow-2xs",
        sizeClasses[size],
        showLabel && "w-auto px-3 gap-2",
        className
      )}
      aria-label={`Switch to ${isDark ? "light" : "dark"} mode`}
      title={`Switch to ${isDark ? "Light" : "Dark"} Mode`}
    >
      <div className="relative flex items-center justify-center">
        {isDark ? (
          <Sun
            className={cn(
              iconSizes[size],
              "transition-transform duration-300 rotate-0 hover:rotate-45"
            )}
          />
        ) : (
          <Moon
            className={cn(
              iconSizes[size],
              "transition-transform duration-300 rotate-0 hover:-rotate-12"
            )}
          />
        )}
      </div>

      {showLabel && (
        <span className="font-semibold text-xs whitespace-nowrap">
          {isDark ? "Light Mode" : "Dark Mode"}
        </span>
      )}
    </button>
  );
};

/**
 * Floating corner theme toggle widget permanently anchored to the bottom-right corner of the website on every page.
 */
export const FloatingThemeToggle = () => {
  const { theme, toggleTheme } = useThemeStore();
  const isDark = theme === "dark";

  return (
    <aside
      aria-label="Theme switcher"
      className="fixed bottom-6 right-6 z-50 flex items-center gap-2 select-none"
    >
      <button
        type="button"
        onClick={toggleTheme}
        className={cn(
          "group relative flex items-center gap-2.5 px-3.5 py-2.5 rounded-full border shadow-xl backdrop-blur-md transition-all duration-300",
          "hover:scale-105 active:scale-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary cursor-pointer",
          isDark
            ? "bg-slate-900/90 border-slate-700 text-amber-300 hover:border-amber-400/50 shadow-black/40 hover:bg-slate-850"
            : "bg-white/95 border-gray-200 text-gray-700 hover:border-primary-border hover:text-gray-900 shadow-gray-300/40"
        )}
        aria-label={`Switch to ${isDark ? "Light" : "Dark"} Mode`}
        title={`Click to switch to ${isDark ? "Light" : "Dark"} Mode`}
      >
        <div className="relative flex items-center justify-center w-5 h-5">
          {isDark ? (
            <Sun className="w-4 h-4 text-amber-300 transition-transform duration-300 group-hover:rotate-90" />
          ) : (
            <Moon className="w-4 h-4 text-slate-700 transition-transform duration-300 group-hover:-rotate-45" />
          )}
        </div>

        <span className="text-xs font-bold tracking-tight pr-0.5 hidden sm:inline-block">
          {isDark ? "Dark" : "Light"}
        </span>

        {/* Pulse dot indicator */}
        <span
          className={cn(
            "w-2 h-2 rounded-full",
            isDark ? "bg-amber-400 shadow-[0_0_8px_#f59e0b]" : "bg-primary shadow-[0_0_8px_var(--theme-primary)]"
          )}
        />
      </button>
    </aside>
  );
};
