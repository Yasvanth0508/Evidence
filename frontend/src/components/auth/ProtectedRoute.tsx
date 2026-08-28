import { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore, UserRole } from "@/store/authStore";

interface ProtectedRouteProps {
  children?: ReactNode;
  allowedRoles?: UserRole[];
}

/**
 * Route protection wrapper verifying user authentication and role-based permissions.
 */
export const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    // Redirect user to their own role dashboard if trying to access unauthorized portal
    if (user.role === "CANDIDATE") {
      return <Navigate to="/candidate/dashboard" replace />;
    } else {
      return <Navigate to="/dashboard" replace />;
    }
  }

  return <>{children}</>;
};