import { NavLink, Outlet } from "react-router-dom";
import { LogOut, Landmark } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { navItems } from "@/components/layout/nav";
import { cn } from "@/lib/utils";

export function AppShell() {
  const { user, hasRole, logout } = useAuth();

  const visibleItems = navItems.filter((item) => !item.roles || hasRole(...item.roles));

  return (
    <div className="flex h-screen w-full overflow-hidden bg-background text-foreground">
      <aside className="flex w-64 shrink-0 flex-col border-r border-border bg-card">
        <div className="flex items-center gap-2 border-b border-border px-4 py-4">
          <Landmark className="h-6 w-6 text-primary" />
          <div>
            <p className="text-sm font-semibold leading-tight">Sistema Contable</p>
            <p className="text-xs text-muted-foreground">Honduras · HNL / USD</p>
          </div>
        </div>
        <nav className="flex-1 space-y-1 overflow-y-auto p-2">
          {visibleItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) =>
                cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  isActive
                    ? "bg-primary text-primary-foreground"
                    : "text-foreground/80 hover:bg-accent hover:text-accent-foreground",
                )
              }
            >
              <item.icon className="h-4 w-4 shrink-0" />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-border p-3">
          <div className="mb-2 px-1">
            <p className="truncate text-sm font-medium">{user?.fullName}</p>
            <p className="truncate text-xs text-muted-foreground">{user?.email} · {roleLabel(user?.role)}</p>
          </div>
          <button
            onClick={logout}
            className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
          >
            <LogOut className="h-4 w-4" />
            Cerrar sesión
          </button>
        </div>
      </aside>
      <main className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-6xl px-6 py-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

function roleLabel(role?: string) {
  switch (role) {
    case "ADMIN":
      return "Administrador";
    case "ACCOUNTANT":
      return "Contador";
    case "AUDITOR":
      return "Auditor";
    default:
      return "";
  }
}
