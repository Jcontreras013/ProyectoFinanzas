import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import type { Role } from "@/types/domain";

export function RequireRole({ roles }: { roles: Role[] }) {
  const { hasRole } = useAuth();

  if (!hasRole(...roles)) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
}
