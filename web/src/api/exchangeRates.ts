import { api } from "@/api/client";
import type { ExchangeRate } from "@/types/domain";

export interface ExchangeRateRequest {
  rateDate: string;
  rate: string;
}

export const exchangeRatesApi = {
  list: () => api.get<ExchangeRate[]>("/exchange-rates"),
  upsert: (request: ExchangeRateRequest) => api.post<ExchangeRate>("/exchange-rates", request),
};
