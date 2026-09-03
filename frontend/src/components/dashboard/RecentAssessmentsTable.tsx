import { useState } from "react";
import { Link } from "react-router-dom";
import { RecentAssessmentItem } from "@/types";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Eye, ShieldCheck, ChevronLeft, ChevronRight } from "lucide-react";

interface RecentAssessmentsTableProps {
  assessments: RecentAssessmentItem[];
}

export const RecentAssessmentsTable = ({
  assessments,
}: RecentAssessmentsTableProps) => {
  const [currentPage, setCurrentPage] = useState(1);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return (
          <Badge variant="completed" dot>
            Completed
          </Badge>
        );
      case "IN_PROGRESS":
        return (
          <Badge variant="inProgress" dot>
            In Progress
          </Badge>
        );
      case "SCHEDULED":
      case "NOT_STARTED":
      default:
        return (
          <Badge variant="notStarted" dot>
            Not Started
          </Badge>
        );
    }
  };

  return (
    <div className="bg-white border border-gray-200/90 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-bold text-gray-900 text-base">Recent Assessments</h3>
        <Link
          to="/dashboard/assessments"
          className="text-xs font-bold text-primary hover:underline"
        >
          View All
        </Link>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[180px]">Candidate</TableHead>
              <TableHead>Project</TableHead>
              <TableHead>Tech Stack</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Time Taken</TableHead>
              <TableHead>Integrity</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {assessments.map((asmt) => (
              <TableRow key={asmt.id} className="hover:bg-gray-50/70">
                {/* Candidate Name */}
                <TableCell className="font-semibold text-gray-900">
                  {asmt.candidateName}
                </TableCell>

                {/* Project */}
                <TableCell className="text-gray-600 font-medium">
                  {asmt.project}
                </TableCell>

                {/* Tech Stack */}
                <TableCell className="text-gray-500 text-xs">
                  {asmt.techStack}
                </TableCell>

                {/* Status */}
                <TableCell>{getStatusBadge(asmt.status)}</TableCell>

                {/* Time Taken */}
                <TableCell className="text-gray-600 text-xs font-mono">
                  {asmt.timeTaken}
                </TableCell>

                {/* Integrity */}
                <TableCell>
                  <div className="flex items-center gap-1 text-xs font-semibold text-emerald-700">
                    <ShieldCheck className="w-4 h-4 text-emerald-600" />
                    <span>{asmt.integrity}%</span>
                  </div>
                </TableCell>

                {/* Actions */}
                <TableCell className="text-right">
                  <Link
                    to={`/dashboard/candidates/${asmt.id}`}
                    className="inline-flex items-center justify-center w-8 h-8 rounded-lg border border-gray-200 bg-white hover:bg-gray-50 hover:text-primary text-gray-400 transition-colors shadow-xs"
                    title="View Candidate Report"
                  >
                    <Eye className="w-4 h-4" />
                  </Link>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Pagination Footer */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-4 border-t border-gray-100 text-xs text-gray-500">
        <div>Showing 1 to 5 of 25 results</div>

        <div className="flex items-center space-x-1">
          <button className="w-7 h-7 rounded-lg border border-gray-200 flex items-center justify-center text-gray-400 hover:bg-gray-50 disabled:opacity-40">
            <ChevronLeft className="w-3.5 h-3.5" />
          </button>
          {[1, 2, 3, 4, 5].map((page) => (
            <button
              key={page}
              onClick={() => setCurrentPage(page)}
              className={`w-7 h-7 rounded-lg text-xs font-semibold transition-colors ${
                currentPage === page
                  ? "border border-primary-border text-primary bg-primary-light/50"
                  : "text-gray-600 hover:bg-gray-50"
              }`}
            >
              {page}
            </button>
          ))}
          <button className="w-7 h-7 rounded-lg border border-gray-200 flex items-center justify-center text-gray-400 hover:bg-gray-50">
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
};
