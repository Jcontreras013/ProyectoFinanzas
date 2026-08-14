import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { Plus, Trash2 } from "lucide-react";
import { accountsApi } from "@/api/accounts";
import { partiesApi } from "@/api/parties";
import { ApiRequestError } from "@/api/client";
import { invoicesApi, type InvoiceLineRequest } from "@/api/invoices";
import { PageHeader } from "@/components/layout/PageHeader";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { formatMoney, todayIso } from "@/lib/format";
import type { Currency } from "@/types/common";

interface LineDraft {
  description: string;
  quantity: string;
  unitPrice: string;
  taxRate: string;
  accountId: string;
}

function emptyLine(): LineDraft {
  return { description: "", quantity: "1", unitPrice: "", taxRate: "15", accountId: "" };
}

export function InvoiceNewPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { data: accounts } = useQuery({ queryKey: ["accounts"], queryFn: accountsApi.list });
  const { data: parties } = useQuery({ queryKey: ["parties"], queryFn: partiesApi.list });

  const [partyId, setPartyId] = useState("");
  const [issueDate, setIssueDate] = useState(todayIso());
  const [dueDate, setDueDate] = useState(todayIso());
  const [currency, setCurrency] = useState<Currency>("HNL");
  const [exchangeRate, setExchangeRate] = useState("");
  const [notes, setNotes] = useState("");
  const [lines, setLines] = useState<LineDraft[]>([emptyLine()]);
  const [error, setError] = useState<string | null>(null);

  const revenueAccounts = useMemo(
    () => (accounts ?? []).filter((a) => a.type === "INCOME" && a.allowsPosting && a.isActive),
    [accounts],
  );
  const customers = useMemo(
    () => (parties ?? []).filter((p) => p.type === "CUSTOMER" || p.type === "BOTH"),
    [parties],
  );

  const updateLine = (index: number, patch: Partial<LineDraft>) =>
    setLines((prev) => prev.map((l, i) => (i === index ? { ...l, ...patch } : l)));

  const totals = useMemo(() => {
    let subtotal = 0;
    let tax = 0;
    for (const line of lines) {
      const lineTotal = (Number(line.quantity) || 0) * (Number(line.unitPrice) || 0);
      subtotal += lineTotal;
      tax += lineTotal * ((Number(line.taxRate) || 0) / 100);
    }
    return { subtotal, tax, total: subtotal + tax };
  }, [lines]);

  const createMutation = useMutation({
    mutationFn: () => {
      const requestLines: InvoiceLineRequest[] = lines.map((l) => ({
        description: l.description,
        quantity: l.quantity,
        unitPrice: l.unitPrice,
        taxRate: l.taxRate,
        accountId: l.accountId,
      }));
      return invoicesApi.create({
        partyId,
        issueDate,
        dueDate,
        currency,
        exchangeRate: currency === "USD" && exchangeRate ? exchangeRate : undefined,
        notes: notes || undefined,
        lines: requestLines,
      });
    },
    onSuccess: (invoice) => {
      queryClient.invalidateQueries({ queryKey: ["invoices"] });
      navigate(`/invoices/${invoice.id}`);
    },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : "No se pudo crear la factura"),
  });

  return (
    <div>
      <PageHeader title="Nueva factura" description="Se contabiliza automáticamente al guardar (CxC, ingresos e ISV)." />

      <Card>
        <CardContent className="pt-6">
          <form onSubmit={(e) => { e.preventDefault(); createMutation.mutate(); }} className="space-y-6">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div className="space-y-1.5 lg:col-span-2">
                <Label htmlFor="party">Cliente</Label>
                <Select id="party" required value={partyId} onChange={(e) => setPartyId(e.target.value)}>
                  <option value="">Selecciona un cliente</option>
                  {customers.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="issueDate">Fecha de emisión</Label>
                <Input id="issueDate" type="date" required value={issueDate} onChange={(e) => setIssueDate(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="dueDate">Fecha de vencimiento</Label>
                <Input id="dueDate" type="date" required value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="currency">Moneda</Label>
                <Select id="currency" value={currency} onChange={(e) => setCurrency(e.target.value as Currency)}>
                  <option value="HNL">Lempiras (HNL)</option>
                  <option value="USD">Dólares (USD)</option>
                </Select>
              </div>
              {currency === "USD" && (
                <div className="space-y-1.5">
                  <Label htmlFor="exchangeRate">Tasa de cambio (opcional)</Label>
                  <Input id="exchangeRate" type="number" step="0.0001" placeholder="Usa la tasa vigente" value={exchangeRate} onChange={(e) => setExchangeRate(e.target.value)} />
                </div>
              )}
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <Label>Líneas de la factura</Label>
                <Button type="button" variant="outline" size="sm" onClick={() => setLines((prev) => [...prev, emptyLine()])}>
                  <Plus className="h-4 w-4" /> Agregar línea
                </Button>
              </div>
              <div className="space-y-2">
                {lines.map((line, index) => (
                  <div key={index} className="grid grid-cols-1 gap-2 rounded-md border border-border p-3 sm:grid-cols-12 sm:items-end">
                    <div className="space-y-1 sm:col-span-4">
                      <Label className="text-xs text-muted-foreground">Descripción</Label>
                      <Input required value={line.description} onChange={(e) => updateLine(index, { description: e.target.value })} />
                    </div>
                    <div className="space-y-1 sm:col-span-1">
                      <Label className="text-xs text-muted-foreground">Cant.</Label>
                      <Input type="number" step="0.01" min="0.01" required value={line.quantity} onChange={(e) => updateLine(index, { quantity: e.target.value })} />
                    </div>
                    <div className="space-y-1 sm:col-span-2">
                      <Label className="text-xs text-muted-foreground">Precio unit.</Label>
                      <Input type="number" step="0.01" min="0" required value={line.unitPrice} onChange={(e) => updateLine(index, { unitPrice: e.target.value })} />
                    </div>
                    <div className="space-y-1 sm:col-span-1">
                      <Label className="text-xs text-muted-foreground">ISV %</Label>
                      <Input type="number" step="0.01" min="0" value={line.taxRate} onChange={(e) => updateLine(index, { taxRate: e.target.value })} />
                    </div>
                    <div className="space-y-1 sm:col-span-3">
                      <Label className="text-xs text-muted-foreground">Cuenta de ingreso</Label>
                      <Select required value={line.accountId} onChange={(e) => updateLine(index, { accountId: e.target.value })}>
                        <option value="">Selecciona</option>
                        {revenueAccounts.map((a) => <option key={a.id} value={a.id}>{a.code} - {a.name}</option>)}
                      </Select>
                    </div>
                    <div className="flex sm:col-span-1 sm:justify-end">
                      <Button type="button" variant="ghost" size="icon" disabled={lines.length <= 1} onClick={() => setLines((prev) => prev.filter((_, i) => i !== index))}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
              <div className="rounded-md bg-muted/50 p-3 text-sm">
                Subtotal: {formatMoney(totals.subtotal, currency)} · ISV: {formatMoney(totals.tax, currency)} ·{" "}
                <span className="font-semibold">Total: {formatMoney(totals.total, currency)}</span>
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="notes">Notas (opcional)</Label>
              <Textarea id="notes" value={notes} onChange={(e) => setNotes(e.target.value)} />
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}

            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? "Guardando..." : "Emitir factura"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
