import { api } from "@/api/client";
import type { Account, AccountSystemRole, AccountType } from "@/types/domain";

export interface AccountRequest {
  code: string;
  name: string;
  type: AccountType;
  parentId?: string | null;
  allowsPosting: boolean;
  systemRole?: AccountSystemRole | null;
  isActive: boolean;
}

export const accountsApi = {
  list: () => api.get<Account[]>("/accounts"),
  get: (id: string) => api.get<Account>(`/accounts/${id}`),
  create: (request: AccountRequest) => api.post<Account>("/accounts", request),
  update: (id: string, request: AccountRequest) => api.put<Account>(`/accounts/${id}`, request),
  deactivate: (id: string) => api.delete<void>(`/accounts/${id}`),
};
