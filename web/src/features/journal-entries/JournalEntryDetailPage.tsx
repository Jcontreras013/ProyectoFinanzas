import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate, useParams } from "react-router-dom";
import { journalEntriesApi } from "@/api/journalEntries";
import { ApiRequestError } from "@/api/client";
import { useAuth } from "@/auth/AuthContext";
import { PageHeader } from "@/components/layout/PageHeader";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDateTime, formatDate, formatMoney } from "@/lib/format";

export function JournalEntryDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [showReverseForm, setShowReverseForm] = useState(false);

  const { data: entry, isLoading } = useQuery({
    queryKey: ["journal-entries", id],
    queryFn: () => journalEntriesApi.get(id!),
    enabled: !!id,
  });

  const reverseMutation = useMutation({
    mutationFn: () => journalEntriesApi.reverse(id!, reason),
    onSuccess: (reversal) => {
      queryClient.invalidateQueries({ queryKey: ["journal-entries"] });
      navigate(`/journal-entries/${reversal.id}`);
    },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : "No se pudo reversar el asiento"),
  });

  if (isLoading || !entry) return <p className="text-sm text-muted-foreground">Cargando...</p>;

  const totalDebit = entry.lines.reduce((acc, l) => acc + Number(l.debit), 0);
  const totalCredit = entry.lines.reduce((acc, l) => acc + Number(l.credit), 0);

  return (
    <div>
      <PageHeader
        title={`Asiento #${entry.entryNumber}`}
        description={`${formatDate(entry.entryDate)} · ${entry.description}`}
        actions={
          hasRole("ADMIN", "ACCOUNTANT") && entry.sourceType !== "REVERSAL" ? (
            <Button variant="outline" onClick={() => setShowReverseForm((v) => !v)}>Reversar asiento</Button>
          ) : undefined
        }
      />

      {showReverseForm && (
        <div className="mb-6 rounded-md border border-border p-4">
          <form
            className="flex flex-wrap items-end gap-3"
            onSubmit={(e) => { e.preventDefault(); reverseMutation.mutate(); }}
          >
            <div className="flex-1 space-y-1.5">
              <label className="text-sm font-medium">Motivo de la reversión</label>
              <input
                className="flex h-9 w-full rounded-md border border-input bg-card px-3 py-1 text-sm shadow-sm"
                required
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="Ej. error en la cuenta seleccionada"
              />
            </div>
            <Button type="submit" variant="destructive" disabled={reverseMutation.isPending}>
              {reverseMutation.isPending ? "Reversando..." : "Confirmar reversión"}
            </Button>
          </form>
          {error && <p className="mt-2 text-sm text-destructive">{error}</p>}
        </div>
      )}

      <p className="mb-2 text-xs text-muted-foreground">
        Creado por {entry.createdByName} el {formatDateTime(entry.createdAt)}
        {entry.reversalOfId && " · Este asiento es una reversión"}
      </p>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Cuenta</TableHead>
            <TableHead>Tercero</TableHead>
            <TableHead>Descripción</TableHead>
            <TableHead className="text-right">Débito</TableHead>
            <TableHead className="text-right">Crédito</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {entry.lines.map((line) => (
            <TableRow key={line.id}>
              <TableCell className="font-mono text-xs">{line.accountCode} <span className="font-sans">{line.accountName}</span></TableCell>
              <TableCell className="text-muted-foreground">{line.partyName ?? "—"}</TableCell>
              <TableCell className="text-muted-foreground">{line.description ?? "—"}</TableCell>
              <TableCell className="text-right font-mono">{Number(line.debit) > 0 ? formatMoney(line.debit) : ""}</TableCell>
              <TableCell className="text-right font-mono">{Number(line.credit) > 0 ? formatMoney(line.credit) : ""}</TableCell>
            </TableRow>
          ))}
          <TableRow className="font-semibold">
            <TableCell colSpan={3}>Totales</TableCell>
            <TableCell className="text-right font-mono">{formatMoney(totalDebit)}</TableCell>
            <TableCell className="text-right font-mono">{formatMoney(totalCredit)}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  );
}
