import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from "recharts";
import { AssessmentStatusSlice } from "@/types";

interface AssessmentDonutChartProps {
  data: AssessmentStatusSlice[];
}

export const AssessmentDonutChart = ({ data }: AssessmentDonutChartProps) => {
  const totalCount = data.reduce((sum, item) => sum + item.count, 0);

  return (
    <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full">
      {/* Card Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-bold text-gray-900 text-base">Assessment Status</h3>
        <select className="text-xs font-semibold text-gray-600 bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-1 focus:outline-none focus:ring-1 focus:ring-[#F05323]">
          <option>All Time</option>
          <option>This Month</option>
          <option>This Week</option>
        </select>
      </div>

      {/* Donut Chart & Legend Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-12 items-center gap-4 py-2">
        {/* Donut Canvas */}
        <div className="sm:col-span-6 relative h-48 flex items-center justify-center">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={data}
                dataKey="count"
                nameKey="name"
                innerRadius={55}
                outerRadius={80}
                paddingAngle={3}
                stroke="#FFFFFF"
                strokeWidth={2}
              >
                {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value: any, name: any) => [
                  `${Number(value).toLocaleString()} assessments`,
                  name,
                ]}
                contentStyle={{
                  backgroundColor: "#1E293B",
                  borderRadius: "10px",
                  color: "#FFFFFF",
                  border: "none",
                  fontSize: "12px",
                }}
                itemStyle={{ color: "#FFFFFF" }}
              />
            </PieChart>
          </ResponsiveContainer>

          {/* Centered Total Overlay */}
          <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none text-center">
            <span className="text-xl font-extrabold text-gray-900 leading-tight">
              {totalCount.toLocaleString()}
            </span>
            <span className="text-[11px] font-semibold text-gray-400 uppercase">
              Total
            </span>
          </div>
        </div>

        {/* Legend Breakdown */}
        <div className="sm:col-span-6 space-y-3 pl-2">
          {data.map((item) => (
            <div
              key={item.name}
              className="flex items-center justify-between text-xs"
            >
              <div className="flex items-center gap-2">
                <span
                  className="w-2.5 h-2.5 rounded-full flex-shrink-0"
                  style={{ backgroundColor: item.color }}
                />
                <span className="font-semibold text-gray-700">{item.name}</span>
              </div>
              <div className="text-right font-mono">
                <span className="font-bold text-gray-900">
                  {item.count.toLocaleString()}
                </span>{" "}
                <span className="text-gray-400 text-[11px]">
                  ({item.percentage}%)
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
