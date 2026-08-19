import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-transparent bg-[#F05323] text-white",
        secondary:
          "border-transparent bg-gray-100 text-gray-900",
        destructive:
          "border-transparent bg-red-100 text-red-700",
        outline:
          "text-gray-700 border border-gray-200",
        completed:
          "bg-emerald-50 text-emerald-700 border border-emerald-200/60 font-semibold",
        inProgress:
          "bg-blue-50 text-blue-700 border border-blue-200/60 font-semibold",
        notStarted:
          "bg-gray-100 text-gray-700 border border-gray-200/80 font-medium",
        fixed:
          "bg-emerald-50 text-emerald-700 border border-emerald-200 font-semibold",
        notFixed:
          "bg-rose-50 text-rose-700 border border-rose-200 font-semibold",
        riskMedium:
          "bg-amber-50 text-amber-800 border border-amber-200 font-bold uppercase tracking-wider text-[10px]",
        purple:
          "bg-purple-50 text-purple-700 border border-purple-200/60 font-medium",
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
