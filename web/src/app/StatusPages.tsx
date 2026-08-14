import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { ShieldAlert, FileQuestion } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";

export function ForbiddenPage() {
  return (
    <StatusPage
      icon={<ShieldAlert className="h-10 w-10 text-destructive" />}
      title="Acceso no autorizado"
      description="Tu rol no tiene permiso para ver esta sección."
    />
  );
}

export function NotFoundPage() {
  return (
    <StatusPage
      icon={<FileQuestion className="h-10 w-10 text-muted-foreground" />}
      title="Página no encontrada"
      description="La página que buscas no existe."
    />
  );
}

function StatusPage({ icon, title, description }: { icon: ReactNode; title: string; description: string }) {
  return (
    <div className="flex h-full min-h-[60vh] flex-col items-center justify-center gap-3 text-center">
      {icon}
      <h1 className="text-xl font-semibold">{title}</h1>
      <p className="text-sm text-muted-foreground">{description}</p>
      <Link to="/" className={buttonVariants({ variant: "outline" })}>
        Volver al panel principal
      </Link>
    </div>
  );
}
