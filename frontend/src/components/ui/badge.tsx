import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-transparent bg-primary text-white",
        secondary:
          "border-transparent bg-gray-100 text-gray-900 dark:bg-slate-800 dark:text-slate-200",
        destructive:
          "border-transparent bg-red-100 text-red-700 dark:bg-red-950/50 dark:text-red-400 dark:border-red-900/50",
        outline:
          "text-gray-700 border border-gray-200 dark:text-slate-300 dark:border-slate-700",
        completed:
          "bg-emerald-50 text-emerald-700 border border-emerald-200/60 font-semibold dark:bg-emerald-950/50 dark:text-emerald-400 dark:border-emerald-800/50",
        inProgress:
          "bg-blue-50 text-blue-700 border border-blue-200/60 font-semibold dark:bg-blue-950/50 dark:text-blue-400 dark:border-blue-800/50",
        notStarted:
          "bg-gray-100 text-gray-700 border border-gray-200/80 font-medium dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700",
        fixed:
          "bg-emerald-50 text-emerald-700 border border-emerald-200 font-semibold dark:bg-emerald-950/50 dark:text-emerald-400 dark:border-emerald-800/50",
        notFixed:
          "bg-rose-50 text-rose-700 border border-rose-200 font-semibold dark:bg-rose-950/50 dark:text-rose-400 dark:border-rose-800/50",
        riskMedium:
          "bg-amber-50 text-amber-800 border border-amber-200 font-bold uppercase tracking-wider text-[10px] dark:bg-amber-950/50 dark:text-amber-400 dark:border-amber-800/50",
        purple:
          "bg-purple-50 text-purple-700 border border-purple-200/60 font-medium dark:bg-purple-950/50 dark:text-purple-400 dark:border-purple-800/50",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {
  dot?: boolean;
}

function Badge({ className, variant, dot, children, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props}>
      {dot && (
        <span
          className={cn(
            "mr-1.5 h-1.5 w-1.5 rounded-full",
            variant === "completed" && "bg-emerald-500",
            variant === "inProgress" && "bg-blue-500",
            variant === "notStarted" && "bg-gray-400",
            variant === "destructive" && "bg-red-500",
            variant === "fixed" && "bg-emerald-500",
            variant === "notFixed" && "bg-rose-500"
          )}
        />
      )}
      {children}
    </div>
  );
}

export { Badge, badgeVariants };
