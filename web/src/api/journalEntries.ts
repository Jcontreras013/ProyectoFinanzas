import { api } from "@/api/client";
import type { PageResponse } from "@/types/common";
import type { JournalEntry } from "@/types/domain";

export interface JournalEntryLineRequest {
  accountId: string;
  partyId?: string | null;
  debit: string;
  credit: string;
  description?: string;
}

export interface CreateJournalEntryRequest {
  entryDate: string;
  description: string;
  lines: JournalEntryLineRequest[];
}

export const journalEntriesApi = {
  list: (page = 0, size = 20) =>
    api.get<PageResponse<JournalEntry>>("/journal-entries", { page, size, sort: "entryDate,desc" }),
  get: (id: string) => api.get<JournalEntry>(`/journal-entries/${id}`),
  create: (request: CreateJournalEntryRequest) => api.post<JournalEntry>("/journal-entries", request),
  reverse: (id: string, reason: string) => api.post<JournalEntry>(`/journal-entries/${id}/reverse`, { reason }),
};
