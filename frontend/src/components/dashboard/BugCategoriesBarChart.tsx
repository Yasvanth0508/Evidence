import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell,
} from "recharts";
import { BugCategoryStat } from "@/types";

interface BugCategoriesBarChartProps {
  data: BugCategoryStat[];
}

export const BugCategoriesBarChart = ({ data }: BugCategoriesBarChartProps) => {
  return (
    <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-bold text-gray-900 text-base">
          Most Failed Bug Categories
        </h3>
        <select className="text-xs font-semibold text-gray-600 bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-1 focus:outline-none focus:ring-1 focus:ring-[#F05323]">
          <option>All Time</option>
          <option>This Month</option>
          <option>This Week</option>
        </select>
      </div>

      {/* Horizontal Bar Chart */}
      <div className="h-48 w-full py-1">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            layout="vertical"
            data={data}
            margin={{ top: 5, right: 30, left: 40, bottom: 5 }}
          >
            <XAxis
              type="number"
              domain={[0, 100]}
              tickFormatter={(val) => `${val}%`}
              tick={{ fontSize: 10, fill: "#94A3B8" }}
              axisLine={{ stroke: "#E2E8F0" }}
              tickLine={false}
            />
            <YAxis
              type="category"
              dataKey="category"
              tick={{ fontSize: 11, fill: "#334155", fontWeight: 500 }}
              axisLine={false}
              tickLine={false}
              width={140}
            />
            <Tooltip
              formatter={(value: any) => [`${value}% Failure Rate`, "Rate"]}
              contentStyle={{
                backgroundColor: "#1E293B",
                borderRadius: "10px",
                color: "#FFFFFF",
                border: "none",
                fontSize: "12px",
              }}
              itemStyle={{ color: "#FFFFFF" }}
            />
            <Bar
              dataKey="failureRate"
              radius={[0, 6, 6, 0]}
              barSize={18}
              label={{
                position: "right",
                formatter: (val: any) => `${val}%`,
                fill: "#475569",
                fontSize: 11,
                fontWeight: 600,
              }}
            >
              {data.map((entry, index) => (
                <Cell key={`bar-cell-${index}`} fill={entry.color} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="text-[11px] text-gray-400 text-center mt-1">
        Failure Rate (%)
      </div>
    </div>
  );
};
