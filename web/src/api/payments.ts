import { api } from "@/api/client";
import type { Currency } from "@/types/common";
import type { Payment, PaymentMethod } from "@/types/domain";

export interface CreatePaymentRequest {
  invoiceId?: string | null;
  expenseId?: string | null;
  amount: string;
  currency: Currency;
  exchangeRate?: string | null;
  paymentDate: string;
  method: PaymentMethod;
}

export const paymentsApi = {
  create: (request: CreatePaymentRequest) => api.post<Payment>("/payments", request),
  listByInvoice: (invoiceId: string) => api.get<Payment[]>("/payments", { invoiceId }),
  listByExpense: (expenseId: string) => api.get<Payment[]>("/payments", { expenseId }),
};
