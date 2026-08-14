import { api } from "@/api/client";
import type { User } from "@/types/domain";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export const authApi = {
  login: (request: LoginRequest) => api.post<LoginResponse>("/auth/login", request),
  me: () => api.get<User>("/auth/me"),
};
