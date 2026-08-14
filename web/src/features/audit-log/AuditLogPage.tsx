import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { auditLogApi } from "@/api/auditLog";
import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDateTime } from "@/lib/format";
import type { AuditAction } from "@/types/domain";

const ACTION_VARIANT: Record<AuditAction, "success" | "warning" | "destructive"> = {
  CREATE: "success",
  UPDATE: "warning",
  DELETE: "destructive",
};

const ACTION_LABEL: Record<AuditAction, string> = { CREATE: "Creación", UPDATE: "Actualización", DELETE: "Baja" };

export function AuditLogPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useQuery({ queryKey: ["audit-log", page], queryFn: () => auditLogApi.list(page) });

  return (
    <div>
      <PageHeader title="Bitácora de auditoría" description="Historial de cambios sobre usuarios, cuentas y asientos contables." />

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Cargando...</p>
      ) : (
        <>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Fecha</TableHead>
                <TableHead>Entidad</TableHead>
                <TableHead>Acción</TableHead>
                <TableHead>Usuario</TableHead>
                <TableHead>Detalle</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.content.map((entry) => (
                <TableRow key={entry.id}>
                  <TableCell className="whitespace-nowrap">{formatDateTime(entry.occurredAt)}</TableCell>
                  <TableCell className="font-mono text-xs">{entry.entityName}</TableCell>
                  <TableCell><Badge variant={ACTION_VARIANT[entry.action]}>{ACTION_LABEL[entry.action]}</Badge></TableCell>
                  <TableCell>{entry.userName}</TableCell>
                  <TableCell className="max-w-md truncate text-xs text-muted-foreground" title={entry.newValue ?? undefined}>
                    {entry.newValue ?? "—"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
            <span>Página {(data?.number ?? 0) + 1} de {Math.max(data?.totalPages ?? 1, 1)}</span>
            <div className="flex gap-2">
              <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Anterior</Button>
              <Button
                variant="outline"
                size="sm"
                disabled={(data?.number ?? 0) + 1 >= (data?.totalPages ?? 1)}
                onClick={() => setPage((p) => p + 1)}
              >
                Siguiente
              </Button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
