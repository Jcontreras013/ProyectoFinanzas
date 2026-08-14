import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { exchangeRatesApi } from "@/api/exchangeRates";
import { ApiRequestError } from "@/api/client";
import { PageHeader } from "@/components/layout/PageHeader";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDate, todayIso } from "@/lib/format";

export function ExchangeRatesPage() {
  const queryClient = useQueryClient();
  const { data: rates, isLoading } = useQuery({ queryKey: ["exchange-rates"], queryFn: exchangeRatesApi.list });
  const [rateDate, setRateDate] = useState(todayIso());
  const [rate, setRate] = useState("");
  const [error, setError] = useState<string | null>(null);

  const upsertMutation = useMutation({
    mutationFn: () => exchangeRatesApi.upsert({ rateDate, rate }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["exchange-rates"] });
      setRate("");
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : "No se pudo guardar la tasa"),
  });

  return (
    <div>
      <PageHeader
        title="Tasas de cambio"
        description="Lempiras (HNL) por 1 dólar (USD). Se usa la tasa vigente más reciente para facturas y gastos en USD."
      />

      <Card className="mb-6">
        <CardContent className="pt-6">
          <form
            className="flex flex-wrap items-end gap-4"
            onSubmit={(e) => { e.preventDefault(); upsertMutation.mutate(); }}
          >
            <div className="space-y-1.5">
              <Label htmlFor="rateDate">Fecha</Label>
              <Input id="rateDate" type="date" required value={rateDate} onChange={(e) => setRateDate(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="rate">Tasa (HNL por USD)</Label>
              <Input id="rate" type="number" step="0.0001" min="0" required value={rate} onChange={(e) => setRate(e.target.value)} placeholder="24.85" />
            </div>
            <Button type="submit" disabled={upsertMutation.isPending}>
              {upsertMutation.isPending ? "Guardando..." : "Registrar tasa"}
            </Button>
          </form>
          {error && <p className="mt-2 text-sm text-destructive">{error}</p>}
        </CardContent>
      </Card>

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Cargando...</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Fecha</TableHead>
              <TableHead>Tasa</TableHead>
              <TableHead>Registrada por</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {(rates ?? []).map((r) => (
              <TableRow key={r.id}>
                <TableCell>{formatDate(r.rateDate)}</TableCell>
                <TableCell className="font-mono">{r.rate}</TableCell>
                <TableCell className="text-muted-foreground">{r.createdByName}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
