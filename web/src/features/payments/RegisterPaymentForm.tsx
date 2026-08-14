import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { paymentsApi } from "@/api/payments";
import { ApiRequestError } from "@/api/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { todayIso } from "@/lib/format";
import type { Currency } from "@/types/common";
import type { PaymentMethod } from "@/types/domain";

interface Props {
  invoiceId?: string;
  expenseId?: string;
  currency: Currency;
  maxAmount: number;
  invalidateKey: unknown[];
  onRegistered: () => void;
}

export function RegisterPaymentForm({ invoiceId, expenseId, currency, maxAmount, invalidateKey, onRegistered }: Props) {
  const queryClient = useQueryClient();
  const [amount, setAmount] = useState(maxAmount > 0 ? maxAmount.toFixed(2) : "");
  const [paymentCurrency, setPaymentCurrency] = useState<Currency>(currency);
  const [paymentDate, setPaymentDate] = useState(todayIso());
  const [method, setMethod] = useState<PaymentMethod>("BANK");
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () =>
      paymentsApi.create({
        invoiceId,
        expenseId,
        amount,
        currency: paymentCurrency,
        paymentDate,
        method,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invalidateKey });
      setError(null);
      onRegistered();
    },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : "No se pudo registrar el pago"),
  });

  return (
    <form
      className="flex flex-wrap items-end gap-3 rounded-md border border-border p-4"
      onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }}
    >
      <div className="space-y-1.5">
        <Label htmlFor="pay-amount">Monto</Label>
        <Input id="pay-amount" type="number" step="0.01" min="0.01" required value={amount} onChange={(e) => setAmount(e.target.value)} />
      </div>
      <div className="space-y-1.5">
        <Label htmlFor="pay-currency">Moneda</Label>
        <Select id="pay-currency" value={paymentCurrency} onChange={(e) => setPaymentCurrency(e.target.value as Currency)}>
          <option value="HNL">HNL</option>
          <option value="USD">USD</option>
        </Select>
      </div>
      <div className="space-y-1.5">
        <Label htmlFor="pay-date">Fecha</Label>
        <Input id="pay-date" type="date" required value={paymentDate} onChange={(e) => setPaymentDate(e.target.value)} />
      </div>
      <div className="space-y-1.5">
        <Label htmlFor="pay-method">Forma</Label>
        <Select id="pay-method" value={method} onChange={(e) => setMethod(e.target.value as PaymentMethod)}>
          <option value="BANK">Transferencia/Banco</option>
          <option value="CASH">Efectivo</option>
        </Select>
      </div>
      <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? "Registrando..." : "Registrar pago"}</Button>
      {error && <p className="w-full text-sm text-destructive">{error}</p>}
    </form>
  );
}
