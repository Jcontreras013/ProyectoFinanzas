import { useQuery } from "@tanstack/react-query";
import { paymentsApi } from "@/api/payments";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { formatDate, formatMoney } from "@/lib/format";

export function PaymentHistory({ invoiceId, expenseId }: { invoiceId?: string; expenseId?: string }) {
  const { data: payments, isLoading } = useQuery({
    queryKey: invoiceId ? ["payments", "invoice", invoiceId] : ["payments", "expense", expenseId],
    queryFn: () => (invoiceId ? paymentsApi.listByInvoice(invoiceId) : paymentsApi.listByExpense(expenseId!)),
    enabled: !!(invoiceId || expenseId),
  });

  if (isLoading) return <p className="text-sm text-muted-foreground">Cargando pagos...</p>;
  if (!payments || payments.length === 0) return <p className="text-sm text-muted-foreground">Sin pagos registrados.</p>;

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>#</TableHead>
          <TableHead>Fecha</TableHead>
          <TableHead>Forma</TableHead>
          <TableHead className="text-right">Monto</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {payments.map((p) => (
          <TableRow key={p.id}>
            <TableCell className="font-mono">{p.paymentNumber}</TableCell>
            <TableCell>{formatDate(p.paymentDate)}</TableCell>
            <TableCell>{p.method === "CASH" ? "Efectivo" : "Banco"}</TableCell>
            <TableCell className="text-right font-mono">{formatMoney(p.amount, p.currency)}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
