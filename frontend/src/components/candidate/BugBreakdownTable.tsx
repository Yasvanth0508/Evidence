import { BugBreakdownItem } from "@/types";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Layers, Code2, Bug, CheckCircle2, XCircle } from "lucide-react";

interface BugBreakdownTableProps {
  bugs: BugBreakdownItem[];
}

export const BugBreakdownTable = ({ bugs }: BugBreakdownTableProps) => {
  const totalScore = bugs.reduce((sum, b) => sum + b.score, 0);
  const totalMaxScore = bugs.reduce((sum, b) => sum + b.maxScore, 0);

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case "Data Flow":
        return (
          <div className="w-6 h-6 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
            <Layers className="w-3.5 h-3.5" />
          </div>
        );
      case "Syntax":
        return (
          <div className="w-6 h-6 rounded-lg bg-orange-50 text-[#F05323] flex items-center justify-center">
            <Code2 className="w-3.5 h-3.5" />
          </div>
        );
      case "Business Logic":
      default:
        return (
          <div className="w-6 h-6 rounded-lg bg-rose-50 text-rose-600 flex items-center justify-center">
            <Bug className="w-3.5 h-3.5" />
          </div>
        );
    }
  };

  return (
    <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-gray-900 text-base">
          Bug Breakdown <span className="text-xs font-normal text-gray-500">(The Core Feature)</span>
        </h3>
      </div>

      <div className="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-12">#</TableHead>
              <TableHead className="w-[180px]">Category</TableHead>
              <TableHead>Issue Type</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Score</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {bugs.map((bug) => (
              <TableRow key={bug.id} className="hover:bg-gray-50/70">
                <TableCell className="font-semibold text-gray-500 text-xs">
                  {bug.id}
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-2 font-medium text-xs text-gray-800">
                    {getCategoryIcon(bug.category)}
                    <span>{bug.category}</span>
                  </div>
                </TableCell>
                <TableCell className="text-xs text-gray-700 font-medium">
                  {bug.issueType}
                </TableCell>
                <TableCell>
                  {bug.status === "FIXED" ? (
                    <Badge variant="fixed" className="gap-1">
                      <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                      <span>Fixed</span>
                    </Badge>
                  ) : (
                    <Badge variant="notFixed" className="gap-1">
                      <XCircle className="w-3.5 h-3.5 text-rose-600" />
                      <span>Not Fixed</span>
                    </Badge>
                  )}
                </TableCell>
                <TableCell className="text-right font-mono text-xs font-semibold text-gray-900">
                  {bug.score} <span className="text-gray-400 font-normal">/ {bug.maxScore}</span>
                </TableCell>
              </TableRow>
            ))}

            {/* Total Row */}
            <TableRow className="border-t-2 border-gray-100 bg-gray-50/40 font-bold">
              <TableCell colSpan={4} className="text-xs uppercase tracking-wider text-gray-700">
                Total
              </TableCell>
              <TableCell className="text-right font-mono text-xs font-bold text-gray-900">
                {totalScore} <span className="text-gray-400 font-normal">/ {totalMaxScore}</span>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </div>
  );
};
