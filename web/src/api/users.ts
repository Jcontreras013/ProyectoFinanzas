import { api } from "@/api/client";
import type { Role, User } from "@/types/domain";

export interface CreateUserRequest {
  email: string;
  password: string;
  fullName: string;
  role: Role;
}

export interface UpdateUserRequest {
  fullName: string;
  role: Role;
  active: boolean;
  password?: string;
}

export const usersApi = {
  list: () => api.get<User[]>("/users"),
  create: (request: CreateUserRequest) => api.post<User>("/users", request),
  update: (id: string, request: UpdateUserRequest) => api.put<User>(`/users/${id}`, request),
  deactivate: (id: string) => api.delete<void>(`/users/${id}`),
};
