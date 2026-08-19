import { useState } from "react";
import { useRecruiterDashboard } from "@/hooks/useDashboard";
import { StatCard } from "@/components/dashboard/StatCard";
import { AssessmentDonutChart } from "@/components/dashboard/AssessmentDonutChart";
import { BugCategoriesBarChart } from "@/components/dashboard/BugCategoriesBarChart";
import { RecentAssessmentsTable } from "@/components/dashboard/RecentAssessmentsTable";
import { TopPerformersList } from "@/components/dashboard/TopPerformersList";
import { DashboardFilterBar } from "@/components/dashboard/DashboardFilterBar";
import {
  Users2,
  FileCheck2,
  CheckCircle2,
  Clock,
  Loader2,
} from "lucide-react";

export const DashboardOverview = () => {
  const { data, isLoading, error } = useRecruiterDashboard();
  const [searchQuery, setSearchQuery] = useState("");
  const [techFilter, setTechFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");

  if (isLoading) {
    return (
      <div className="h-96 flex flex-col items-center justify-center space-y-3">
        <Loader2 className="w-8 h-8 text-[#F05323] animate-spin" />
        <span className="text-xs font-semibold text-gray-500">
          Loading HR Dashboard statistics...
        </span>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="p-8 text-center bg-white rounded-2xl border border-rose-200 text-rose-700 text-sm">
        Failed to load dashboard data. Please try refreshing.
      </div>
    );
  }

  // Filter recent assessments locally based on filter bar state
  const filteredAssessments = data.recentAssessments.filter((item) => {
    const matchesSearch =
      item.candidateName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.project.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesTech =
      techFilter === "ALL" ||
      item.techStack.toLowerCase().includes(techFilter.toLowerCase());
    const matchesStatus =
      statusFilter === "ALL" || item.status === statusFilter;

    return matchesSearch && matchesTech && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* 1. Metric Stat Cards Row (4 cards) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Candidates */}
        <StatCard
          title="Total Candidates"
          value={data.totalCandidates.value}
          changePercentage={data.totalCandidates.changePercentage}
          isPositive={data.totalCandidates.isPositive}
          sparklineData={data.totalCandidates.sparkline}
          icon={<Users2 className="w-5 h-5 text-blue-600" />}
          iconBgColor="bg-blue-50"
          sparklineColor="#3B82F6"
        />

        {/* Total Assessments */}
        <StatCard
          title="Total Assessments"
          value={data.totalAssessments.value}
          changePercentage={data.totalAssessments.changePercentage}
          isPositive={data.totalAssessments.isPositive}
          sparklineData={data.totalAssessments.sparkline}
          icon={<FileCheck2 className="w-5 h-5 text-purple-600" />}
          iconBgColor="bg-purple-50"
          sparklineColor="#8B5CF6"
        />

        {/* Completion Rate */}
        <StatCard
          title="Completion Rate"
          value={data.completionRate.value}
          changePercentage={data.completionRate.changePercentage}
          isPositive={data.completionRate.isPositive}
          sparklineData={data.completionRate.sparkline}
          icon={<CheckCircle2 className="w-5 h-5 text-emerald-600" />}
          iconBgColor="bg-emerald-50"
          sparklineColor="#10B981"
        />

        {/* Avg. Fix Time */}
        <StatCard
          title="Avg. Fix Time"
          value={data.avgFixTime.value}
          changePercentage={data.avgFixTime.changePercentage}
          isPositive={data.avgFixTime.isPositive}
          sparklineData={data.avgFixTime.sparkline}
          icon={<Clock className="w-5 h-5 text-orange-600" />}
          iconBgColor="bg-orange-50"
          sparklineColor="#F05323"
        />
      </div>

      {/* 2. Charts Section (Donut on left, Horizontal Bars on right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-6">
          <AssessmentDonutChart data={data.assessmentStatusDistribution} />
        </div>
        <div className="lg:col-span-6">
          <BugCategoriesBarChart data={data.mostFailedBugCategories} />
        </div>
      </div>

      {/* 3. Tables Section (Recent Assessments on left, Top Performers on right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-8">
          <RecentAssessmentsTable assessments={filteredAssessments} />
        </div>
        <div className="lg:col-span-4">
          <TopPerformersList performers={data.topPerformers} />
        </div>
      </div>

      {/* 4. Bottom Filter & Search Controls */}
      <DashboardFilterBar
        onSearch={(q) => setSearchQuery(q)}
        onTechStackChange={(t) => setTechFilter(t)}
        onStatusChange={(s) => setStatusFilter(s)}
      />
    </div>
  );
};
