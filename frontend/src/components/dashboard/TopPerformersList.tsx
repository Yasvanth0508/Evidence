import { Link } from "react-router-dom";
import { TopPerformerItem } from "@/types";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Medal } from "lucide-react";

interface TopPerformersListProps {
  performers: TopPerformerItem[];
}

export const TopPerformersList = ({ performers }: TopPerformersListProps) => {
  const getRankBadge = (rank: number) => {
    switch (rank) {
      case 1:
        return (
          <div className="w-6 h-6 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 font-bold text-xs shadow-xs">
            <Medal className="w-3.5 h-3.5" />
          </div>
        );
      case 2:
        return (
          <div className="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 font-bold text-xs shadow-xs">
            <Medal className="w-3.5 h-3.5" />
          </div>
        );
      case 3:
        return (
          <div className="w-6 h-6 rounded-full bg-orange-100 flex items-center justify-center text-orange-700 font-bold text-xs shadow-xs">
            <Medal className="w-3.5 h-3.5" />
          </div>
        );
      default:
        return (
          <span className="w-6 text-center text-xs font-semibold text-gray-500">
            {rank}
          </span>
        );
    }
  };

  return (
    <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-bold text-gray-900 text-base">Top Performers</h3>
        <Link
          to="/dashboard/candidates"
          className="text-xs font-bold text-[#F05323] hover:underline"
        >
          View All
        </Link>
      </div>

      {/* Leaderboard Table */}
      <div className="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-12">Rank</TableHead>
              <TableHead>Candidate</TableHead>
              <TableHead>Score</TableHead>
              <TableHead className="text-right">Avg. Fix Time</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {performers.map((p) => (
              <TableRow key={p.rank} className="hover:bg-gray-50/70">
                <TableCell>{getRankBadge(p.rank)}</TableCell>
                <TableCell className="font-semibold text-gray-900">
                  {p.candidateName}
                </TableCell>
                <TableCell>
                  <span className="font-bold text-emerald-600 font-mono text-xs">
                    {p.score}
                  </span>{" "}
                  <span className="text-[11px] text-gray-400">/ 100</span>
                </TableCell>
                <TableCell className="text-right font-mono text-xs text-gray-600">
                  {p.avgFixTime}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};
