import type { Currency } from "@/types/common";

const currencyFormatters: Record<Currency, Intl.NumberFormat> = {
  HNL: new Intl.NumberFormat("es-HN", { style: "currency", currency: "HNL" }),
  USD: new Intl.NumberFormat("es-HN", { style: "currency", currency: "USD" }),
};

/**
 * Los montos llegan del backend como string (nunca number) para no perder precisión en
 * JSON; aquí solo se formatean para mostrar, nunca se usan para cálculos que se persistan.
 */
export function formatMoney(amount: string | number, currency: Currency = "HNL"): string {
  const value = typeof amount === "string" ? Number(amount) : amount;
  return currencyFormatters[currency].format(value);
}

export function formatDate(date: string): string {
  return new Intl.DateTimeFormat("es-HN", { dateStyle: "medium" }).format(new Date(`${date}T00:00:00`));
}

export function formatDateTime(isoDateTime: string): string {
  return new Intl.DateTimeFormat("es-HN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(isoDateTime));
}

export function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}
