import { api } from "@/api/client";
import type { Party, PartyType } from "@/types/domain";

export interface PartyRequest {
  type: PartyType;
  name: string;
  rtn?: string | null;
  email?: string | null;
  phone?: string | null;
  address?: string | null;
  isActive: boolean;
}

export const partiesApi = {
  list: () => api.get<Party[]>("/parties"),
  get: (id: string) => api.get<Party>(`/parties/${id}`),
  create: (request: PartyRequest) => api.post<Party>("/parties", request),
  update: (id: string, request: PartyRequest) => api.put<Party>(`/parties/${id}`, request),
  deactivate: (id: string) => api.delete<void>(`/parties/${id}`),
};
