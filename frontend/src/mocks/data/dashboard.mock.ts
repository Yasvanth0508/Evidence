import { RecruiterDashboardData } from "@/types";

export const mockDashboardData: RecruiterDashboardData = {
  totalCandidates: {
    value: "12,845",
    changePercentage: 18.6,
    isPositive: true,
    sparkline: [
      { val: 10200 },
      { val: 10800 },
      { val: 11400 },
      { val: 11900 },
      { val: 12200 },
      { val: 12500 },
      { val: 12845 },
    ],
  },
  totalAssessments: {
    value: "18,732",
    changePercentage: 22.4,
    isPositive: true,
    sparkline: [
      { val: 14000 },
      { val: 15200 },
      { val: 16100 },
      { val: 16900 },
      { val: 17500 },
      { val: 18100 },
      { val: 18732 },
    ],
  },
  completionRate: {
    value: "78.4%",
    changePercentage: 6.3,
    isPositive: true,
    sparkline: [
      { val: 71.0 },
      { val: 72.5 },
      { val: 74.0 },
      { val: 75.8 },
      { val: 76.5 },
      { val: 77.2 },
      { val: 78.4 },
    ],
  },
  avgFixTime: {
    value: "42.7 mins",
    changePercentage: 8.1,
    isPositive: false, // lower fix time is usually better, but trend is up
    sparkline: [
      { val: 48.0 },
      { val: 46.5 },
      { val: 45.2 },
      { val: 44.0 },
      { val: 43.1 },
      { val: 42.9 },
      { val: 42.7 },
    ],
  },
  assessmentStatusDistribution: [
    {
      name: "Completed",
      count: 8469,
      percentage: 45.2,
      color: "#10B981", // Emerald Green
    },
    {
      name: "In Progress",
      count: 6126,
      percentage: 32.7,
      color: "#3B82F6", // Blue
    },
    {
      name: "Not Started",
      count: 4137,
      percentage: 22.1,
      color: "#94A3B8", // Slate Gray
    },
  ],
  mostFailedBugCategories: [
    {
      category: "Business Logic Bugs",
      failureRate: 62.3,
      count: 11670,
      color: "#EF4444", // Red
    },
    {
      category: "Syntax / Structural Bugs",
      failureRate: 48.7,
      count: 9122,
      color: "#F05323", // Evidence Orange
    },
    {
      category: "Data Flow Bugs",
      failureRate: 37.1,
      count: 6949,
      color: "#3B82F6", // Blue
    },
  ],
  recentAssessments: [
    {
      id: "asmt-001",
      candidateName: "Aarav Patel",
      candidateEmail: "aarav.patel@example.com",
      project: "E-Commerce API",
      techStack: "Java, Spring Boot",
      status: "COMPLETED",
      timeTaken: "38 mins",
      integrity: 98,
    },
    {
      id: "asmt-002",
      candidateName: "Neha Singh",
      candidateEmail: "neha.singh@example.com",
      project: "Task Manager",
      techStack: "React, Node.js",
      status: "IN_PROGRESS",
      timeTaken: "—",
      integrity: 96,
    },
    {
      id: "asmt-003",
      candidateName: "Rohan Mehta",
      candidateEmail: "rohan.mehta@example.com",
      project: "Payment Gateway",
      techStack: "Python, Django",
      status: "COMPLETED",
      timeTaken: "52 mins",
      integrity: 95,
    },
    {
      id: "asmt-004",
      candidateName: "Priya Nair",
      candidateEmail: "priya.nair@example.com",
      project: "Chat Application",
      techStack: "React, Socket.io",
      status: "SCHEDULED",
      timeTaken: "—",
      integrity: 100,
    },
    {
      id: "asmt-005",
      candidateName: "Karan Verma",
      candidateEmail: "karan.verma@example.com",
      project: "Inventory System",
      techStack: "Java, Spring Boot",
      status: "COMPLETED",
      timeTaken: "41 mins",
      integrity: 99,
    },
  ],
  topPerformers: [
    {
      rank: 1,
      candidateName: "Aarav Patel",
      score: 92.6,
      avgFixTime: "28 mins",
    },
    {
      rank: 2,
      candidateName: "Rohan Mehta",
      score: 90.3,
      avgFixTime: "31 mins",
    },
    {
      rank: 3,
      candidateName: "Ananya Iyer",
      score: 88.7,
      avgFixTime: "33 mins",
    },
    {
      rank: 4,
      candidateName: "Karan Verma",
      score: 87.1,
      avgFixTime: "35 mins",
    },
    {
      rank: 5,
      candidateName: "Neha Singh",
      score: 85.4,
      avgFixTime: "37 mins",
    },
  ],
};
