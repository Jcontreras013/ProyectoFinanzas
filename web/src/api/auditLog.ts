import { api } from "@/api/client";
import type { PageResponse } from "@/types/common";
import type { AuditLogEntry } from "@/types/domain";

export const auditLogApi = {
  list: (page = 0, size = 30) => api.get<PageResponse<AuditLogEntry>>("/audit-log", { page, size }),
};
