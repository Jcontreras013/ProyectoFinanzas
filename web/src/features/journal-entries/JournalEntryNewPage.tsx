import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { Plus, Trash2 } from "lucide-react";
import { accountsApi } from "@/api/accounts";
import { partiesApi } from "@/api/parties";
import { ApiRequestError } from "@/api/client";
import { journalEntriesApi, type JournalEntryLineRequest } from "@/api/journalEntries";
import { PageHeader } from "@/components/layout/PageHeader";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { formatMoney, todayIso } from "@/lib/format";

interface LineDraft {
  accountId: string;
  partyId: string;
  debit: string;
  credit: string;
  description: string;
}

function emptyLine(): LineDraft {
  return { accountId: "", partyId: "", debit: "", credit: "", description: "" };
}

export function JournalEntryNewPage() {
  const navigate = useNavigate();
  const { data: accounts } = useQuery({ queryKey: ["accounts"], queryFn: accountsApi.list });
  const { data: parties } = useQuery({ queryKey: ["parties"], queryFn: partiesApi.list });
  const queryClient = useQueryClient();

  const [entryDate, setEntryDate] = useState(todayIso());
  const [description, setDescription] = useState("");
  const [lines, setLines] = useState<LineDraft[]>([emptyLine(), emptyLine()]);
  const [error, setError] = useState<string | null>(null);

  const postableAccounts = useMemo(
    () => (accounts ?? []).filter((a) => a.allowsPosting && a.isActive).sort((a, b) => a.code.localeCompare(b.code)),
    [accounts],
  );

  const totals = useMemo(() => {
    const debit = lines.reduce((acc, l) => acc + (Number(l.debit) || 0), 0);
    const credit = lines.reduce((acc, l) => acc + (Number(l.credit) || 0), 0);
    return { debit, credit, balanced: Math.abs(debit - credit) < 0.001 && debit > 0 };
  }, [lines]);

  const updateLine = (index: number, patch: Partial<LineDraft>) => {
    setLines((prev) => prev.map((l, i) => (i === index ? { ...l, ...patch } : l)));
  };

  const createMutation = useMutation({
    mutationFn: () => {
      const requestLines: JournalEntryLineRequest[] = lines
        .filter((l) => l.accountId)
        .map((l) => ({
          accountId: l.accountId,
          partyId: l.partyId || null,
          debit: l.debit || "0",
          credit: l.credit || "0",
          description: l.description || undefined,
        }));
      return journalEntriesApi.create({ entryDate, description, lines: requestLines });
    },
    onSuccess: (entry) => {
      queryClient.invalidateQueries({ queryKey: ["journal-entries"] });
      navigate(`/journal-entries/${entry.id}`);
    },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : "No se pudo crear el asiento"),
  });

  return (
    <div>
      <PageHeader title="Nuevo asiento contable" description="Registra un movimiento manual balanceado." />

      <Card>
        <CardContent className="pt-6">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              createMutation.mutate();
            }}
            className="space-y-6"
          >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="entryDate">Fecha</Label>
                <Input id="entryDate" type="date" required value={entryDate} onChange={(e) => setEntryDate(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="description">Descripción</Label>
                <Input id="description" required value={description} onChange={(e) => setDescription(e.target.value)} />
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <Label>Líneas del asiento</Label>
                <Button type="button" variant="outline" size="sm" onClick={() => setLines((prev) => [...prev, emptyLine()])}>
                  <Plus className="h-4 w-4" /> Agregar línea
                </Button>
              </div>

              <div className="space-y-2">
                {lines.map((line, index) => (
                  <div key={index} className="grid grid-cols-1 gap-2 rounded-md border border-border p-3 sm:grid-cols-12 sm:items-end">
                    <div className="space-y-1 sm:col-span-4">
                      <Label className="text-xs text-muted-foreground">Cuenta</Label>
                      <Select value={line.accountId} onChange={(e) => updateLine(index, { accountId: e.target.value })} required>
                        <option value="">Selecciona una cuenta</option>
                        {postableAccounts.map((a) => (
                          <option key={a.id} value={a.id}>{a.code} - {a.name}</option>
                        ))}
                      </Select>
                    </div>
                    <div className="space-y-1 sm:col-span-2">
                      <Label className="text-xs text-muted-foreground">Tercero (opcional)</Label>
                      <Select value={line.partyId} onChange={(e) => updateLine(index, { partyId: e.target.value })}>
                        <option value="">—</option>
                        {(parties ?? []).map((p) => (
                          <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                      </Select>
                    </div>
                    <div className="space-y-1 sm:col-span-2">
                      <Label className="text-xs text-muted-foreground">Débito</Label>
                      <Input
                        type="number"
                        step="0.01"
                        min="0"
                        value={line.debit}
                        onChange={(e) => updateLine(index, { debit: e.target.value, credit: e.target.value ? "" : line.credit })}
                      />
                    </div>
                    <div className="space-y-1 sm:col-span-2">
                      <Label className="text-xs text-muted-foreground">Crédito</Label>
                      <Input
                        type="number"
                        step="0.01"
                        min="0"
                        value={line.credit}
                        onChange={(e) => updateLine(index, { credit: e.target.value, debit: e.target.value ? "" : line.debit })}
                      />
                    </div>
                    <div className="flex sm:col-span-2 sm:justify-end">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        disabled={lines.length <= 2}
                        onClick={() => setLines((prev) => prev.filter((_, i) => i !== index))}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>

              <div className={`rounded-md p-3 text-sm ${totals.balanced ? "bg-success/10 text-success" : "bg-warning/15 text-warning"}`}>
                Débitos: {formatMoney(totals.debit)} · Créditos: {formatMoney(totals.credit)} ·{" "}
                {totals.balanced ? "El asiento está balanceado" : "El asiento aún no está balanceado"}
              </div>
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}

            <Button type="submit" disabled={!totals.balanced || createMutation.isPending}>
              {createMutation.isPending ? "Guardando..." : "Registrar asiento"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
