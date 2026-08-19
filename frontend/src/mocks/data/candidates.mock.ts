import { CandidateReportData, User } from "@/types";

export const mockCandidatesList: User[] = [
  {
    id: "cand-001",
    name: "Aarav Patel",
    email: "aarav.patel@example.com",
    role: "CANDIDATE",
    avatarUrl: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
  },
  {
    id: "cand-002",
    name: "Neha Singh",
    email: "neha.singh@example.com",
    role: "CANDIDATE",
  },
  {
    id: "cand-003",
    name: "Rohan Mehta",
    email: "rohan.mehta@example.com",
    role: "CANDIDATE",
  },
  {
    id: "cand-004",
    name: "Priya Nair",
    email: "priya.nair@example.com",
    role: "CANDIDATE",
  },
  {
    id: "cand-005",
    name: "Karan Verma",
    email: "karan.verma@example.com",
    role: "CANDIDATE",
  },
];

export const mockAaravPatelReport: CandidateReportData = {
  id: "asmt-001",
  candidate: {
    id: "cand-001",
    name: "Aarav Patel",
    email: "aarav.patel@example.com",
    avatarInitials: "AS",
    status: "COMPLETED",
  },
  project: {
    name: "E-Commerce API",
    techStack: "Java, Spring Boot",
    date: "May 15, 2026",
    totalTimeTaken: "42 mins",
  },
  scoreOverview: {
    overallScore: 82,
    scoreRating: "Good Performance",
    bugsFixed: { fixed: 2, total: 3 },
    testCasesPassed: { passed: 18, total: 24 },
    totalTimeTakenMinutes: 42,
  },
  bugBreakdown: [
    {
      id: 1,
      category: "Data Flow",
      issueType: "API Route Mismatch",
      status: "FIXED",
      score: 25,
      maxScore: 25,
    },
    {
      id: 2,
      category: "Syntax",
      issueType: "Type/Signature Mismatch",
      status: "FIXED",
      score: 23,
      maxScore: 25,
    },
    {
      id: 3,
      category: "Business Logic",
      issueType: "Reversed Condition",
      status: "NOT_FIXED",
      score: 0,
      maxScore: 25,
    },
  ],
  aiSummary:
    "Aarav shows strong skills in API debugging and type handling. The main issue was in business logic where a reversed condition caused incorrect behavior. Edge cases for empty input were not handled. Overall performance is good with room for improvement in logic validation.",
  strengths: [
    "API debugging and error handling",
    "Type checking and validation",
    "Clean and readable code structure",
    "Good test case coverage",
  ],
  improvements: [
    "Logic validation and condition handling",
    "Consider edge cases (null, empty input)",
    "Improve variable naming for clarity",
    "Add more unit tests for business logic",
  ],
  integrity: {
    overallRiskBadge: "MEDIUM",
    behaviorSummary: {
      copyPasteEvents: 3,
      buildRuns: 4,
      testRuns: 6,
      idleTimeMinutes: 12,
    },
    riskAnalysis:
      "Candidate shows normal behavior with few copy-paste events and moderate idle time. No major integrity concerns detected.",
  },
};
