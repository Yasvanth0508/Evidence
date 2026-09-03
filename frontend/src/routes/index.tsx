import { createBrowserRouter, Navigate } from "react-router-dom";
import { LandingPage } from "@/pages/public/LandingPage";
import { LoginPage } from "@/pages/auth/LoginPage";
import { SignupPage } from "@/pages/auth/SignupPage";
import { DashboardLayout } from "@/components/layout/DashboardLayout";
import { DashboardOverview } from "@/pages/dashboard/DashboardOverview";
import { WorkspacesPage } from "@/pages/dashboard/WorkspacesPage";
import { WorkspaceDetailPage } from "@/pages/dashboard/WorkspaceDetailPage";
import { CandidateDetailPage } from "@/pages/dashboard/CandidateDetailPage";
import { ReportsPage } from "@/pages/dashboard/ReportsPage";
import { SelectedCandidatesPage } from "@/pages/dashboard/SelectedCandidatesPage";
import { CandidateReport } from "@/pages/dashboard/CandidateReport";
import { CandidateDashboard } from "@/pages/candidate/CandidateDashboard";
import { CandidatePerformanceReportPage } from "@/pages/candidate/CandidatePerformanceReportPage";
import { AssessmentWorkspace } from "@/pages/assessment/AssessmentWorkspace";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <LandingPage />,
  },
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/signup",
    element: <SignupPage />,
  },
  {
    path: "/dashboard",
    element: (
      <ProtectedRoute allowedRoles={["RECRUITER"]}>
        <DashboardLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <DashboardOverview />,
      },
      {
        path: "workspaces",
        element: <WorkspacesPage />,
      },
      {
        path: "workspaces/:workspaceId",
        element: <WorkspaceDetailPage />,
      },
      {
        path: "workspaces/:workspaceId/candidates/:candidateId",
        element: <CandidateDetailPage />,
      },
      {
        path: "reports",
        element: <ReportsPage />,
      },
      {
        path: "selected-candidates",
        element: <SelectedCandidatesPage />,
      },
      {
        path: "candidates/:id",
        element: <CandidateReport />,
      },
      {
        path: "*",
        element: <Navigate to="/dashboard" replace />,
      },
    ],
  },
  {
    path: "/candidate",
    element: <Navigate to="/candidate/dashboard" replace />,
  },
  {
    path: "/candidate/dashboard",
    element: (
      <ProtectedRoute allowedRoles={["CANDIDATE"]}>
        <CandidateDashboard />
      </ProtectedRoute>
    ),
  },
  {
    path: "/candidate/assessments/:id/report",
    element: (
      <ProtectedRoute allowedRoles={["CANDIDATE"]}>
        <CandidatePerformanceReportPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/assessment/:id",
    element: (
      <ProtectedRoute>
        <AssessmentWorkspace />
      </ProtectedRoute>
    ),
  },
  {
    path: "*",
    element: <Navigate to="/" replace />,
  },
]);