import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Plus } from "lucide-react";
import { journalEntriesApi } from "@/api/journalEntries";
import { useAuth } from "@/auth/AuthContext";
import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDate } from "@/lib/format";

const SOURCE_LABELS: Record<string, string> = {
  MANUAL: "Manual",
  INVOICE: "Factura",
  EXPENSE: "Gasto",
  PAYMENT: "Cobro/Pago",
  REVERSAL: "Reversión",
};

export function JournalEntriesListPage() {
  const { hasRole } = useAuth();
  const [page, setPage] = useState(0);
  const { data, isLoading } = useQuery({
    queryKey: ["journal-entries", page],
    queryFn: () => journalEntriesApi.list(page),
  });

  return (
    <div>
      <PageHeader
        title="Asientos contables"
        description="Libro diario de la empresa. Los asientos contabilizados son inmutables; solo pueden reversarse."
        actions={
          hasRole("ADMIN", "ACCOUNTANT") ? (
            <Link to="/journal-entries/new" className={buttonVariants({})}>
              <Plus className="h-4 w-4" /> Nuevo asiento
            </Link>
          ) : undefined
        }
      />

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Cargando...</p>
      ) : (
        <>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>#</TableHead>
                <TableHead>Fecha</TableHead>
                <TableHead>Descripción</TableHead>
                <TableHead>Origen</TableHead>
                <TableHead>Creado por</TableHead>
                <TableHead className="text-right">Ver</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.content.map((entry) => (
                <TableRow key={entry.id}>
                  <TableCell className="font-mono">{entry.entryNumber}</TableCell>
                  <TableCell>{formatDate(entry.entryDate)}</TableCell>
                  <TableCell>{entry.description}</TableCell>
                  <TableCell><Badge variant="default">{SOURCE_LABELS[entry.sourceType]}</Badge></TableCell>
                  <TableCell className="text-muted-foreground">{entry.createdByName}</TableCell>
                  <TableCell className="text-right">
                    <Link to={`/journal-entries/${entry.id}`} className={buttonVariants({ variant: "ghost", size: "sm" })}>
                      Detalle
                    </Link>
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
