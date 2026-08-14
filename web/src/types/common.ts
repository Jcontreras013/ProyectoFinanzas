export type Currency = "HNL" | "USD";

export type Role = "ADMIN" | "ACCOUNTANT" | "AUDITOR";

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
