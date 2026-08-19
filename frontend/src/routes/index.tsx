import { createBrowserRouter, Navigate } from "react-router-dom";
import { LandingPage } from "@/pages/public/LandingPage";
import { LoginPage } from "@/pages/auth/LoginPage";
import { SignupPage } from "@/pages/auth/SignupPage";
import { DashboardLayout } from "@/components/layout/DashboardLayout";
import { DashboardOverview } from "@/pages/dashboard/DashboardOverview";
import { CandidateReport } from "@/pages/dashboard/CandidateReport";
import { AssessmentWorkspace } from "@/pages/assessment/AssessmentWorkspace";

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
    element: <DashboardLayout />,
    children: [
      {
        index: true,
        element: <DashboardOverview />,
      },
      {
        path: "assessments",
        element: <DashboardOverview />,
      },
      {
        path: "candidates",
        element: <DashboardOverview />,
      },
      {
        path: "candidates/:id",
        element: <CandidateReport />,
      },
    ],
  },
  {
    path: "/assessment/:id",
    element: <AssessmentWorkspace />,
  },
  {
    path: "*",
    element: <Navigate to="/" replace />,
  },
]);
