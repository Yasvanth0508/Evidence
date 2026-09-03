import { Link } from "react-router-dom";
import { cn } from "@/lib/utils";

interface LogoProps {
  className?: string;
  showSubtitle?: boolean;
  size?: "sm" | "md" | "lg";
  to?: string;
}

export const Logo = ({
  className,
  showSubtitle = true,
  size = "md",
  to = "/",
}: LogoProps) => {
  const iconSizes = {
    sm: "w-6 h-6",
    md: "w-8 h-8",
    lg: "w-10 h-10",
  };

  const textSizes = {
    sm: "text-lg",
    md: "text-xl",
    lg: "text-2xl",
  };

  const content = (
    <div className={cn("flex items-center gap-2.5 select-none", className)}>
      {/* Evidence Diamond Brand Icon */}
      <div className={cn("relative flex items-center justify-center flex-shrink-0", iconSizes[size])}>
        <svg
          viewBox="0 0 40 40"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="w-full h-full drop-shadow-sm"
        >
          {/* Evidence Diamond / Chevrons */}
          <path
            d="M12 20L19 13L21 15L16 20L21 25L19 27L12 20Z"
            fill="var(--theme-primary)"
          />
          <path
            d="M28 20L21 13L19 15L24 20L19 25L21 27L28 20Z"
            fill="var(--theme-primary)"
          />
          <rect
            x="3"
            y="20"
            width="24"
            height="24"
            rx="4"
            transform="rotate(-45 3 20)"
            stroke="var(--theme-primary)"
            strokeWidth="3.5"
          />
          <circle cx="16" cy="20" r="1.5" fill="var(--theme-primary)" />
          <circle cx="24" cy="20" r="1.5" fill="var(--theme-primary)" />
        </svg>
      </div>

      {/* Brand Text */}
      <div className="flex flex-col">
        <span
          className={cn(
            "font-extrabold tracking-tight text-gray-900 leading-none dark:text-white",
            textSizes[size]
          )}
        >
          EVIDENCE
        </span>
        {showSubtitle && (
          <span className="text-[10px] font-medium tracking-wide text-gray-500 mt-0.5 leading-none dark:text-slate-400">
            Verify Real-World Mastery
          </span>
        )}
      </div>
    </div>
  );

  if (to) {
    return <Link to={to} className="inline-block">{content}</Link>;
  }

  return content;
};
