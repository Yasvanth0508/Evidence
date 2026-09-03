import React from "react";
import { ResponsiveContainer, LineChart, Line } from "recharts";
import { ArrowUpRight, ArrowDownRight } from "lucide-react";
import { cn } from "@/lib/utils";

interface StatCardProps {
  title: string;
  value: string | number;
  changePercentage: number;
  isPositive: boolean;
  sparklineData: { val: number }[];
  icon: React.ReactNode;
  iconBgColor?: string;
  sparklineColor?: string;
}

export const StatCard = ({
  title,
  value,
  changePercentage,
  isPositive,
  sparklineData,
  icon,
  iconBgColor = "bg-blue-50 text-blue-600 dark:bg-blue-950/60 dark:text-blue-400",
  sparklineColor = "#3B82F6",
}: StatCardProps) => {
  return (
    <div className="bg-white dark:bg-slate-900 border border-gray-200/90 dark:border-slate-800 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
      {/* Top Row: Icon & Title */}
      <div className="flex items-center gap-3 mb-2">
        <div
          className={cn(
            "w-10 h-10 rounded-xl flex items-center justify-center font-bold text-sm shadow-sm",
            iconBgColor
          )}
        >
          {icon}
        </div>
        <span className="text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wide">
          {title}
        </span>
      </div>

      {/* Main Metric Value */}
      <div className="my-2">
        <h3 className="text-3xl font-extrabold text-gray-900 dark:text-white tracking-tight">
          {value}
        </h3>
      </div>

      {/* Bottom Row: Trend and Sparkline */}
      <div className="flex items-center justify-between pt-2">
        {/* Trend Indicator */}
        <div className="flex items-center gap-1 text-xs font-bold">
          {isPositive ? (
            <span className="text-emerald-600 dark:text-emerald-400 flex items-center">
              <ArrowUpRight className="w-3.5 h-3.5" />
              {changePercentage}%
            </span>
          ) : (
            <span className="text-rose-600 dark:text-rose-400 flex items-center">
              <ArrowDownRight className="w-3.5 h-3.5" />
              {changePercentage}%
            </span>
          )}
          <span className="text-[11px] font-normal text-gray-400 dark:text-slate-500">vs last week</span>
        </div>

        {/* Mini Sparkline Chart */}
        <div className="w-24 h-8">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={sparklineData}>
              <Line
                type="monotone"
                dataKey="val"
                stroke={sparklineColor}
                strokeWidth={2}
                dot={false}
                isAnimationActive={true}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
