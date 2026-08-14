const STORAGE_KEY = "contafin.token";

export const tokenStorage = {
  get(): string | null {
    return localStorage.getItem(STORAGE_KEY);
  },
  set(token: string) {
    localStorage.setItem(STORAGE_KEY, token);
  },
  clear() {
    localStorage.removeItem(STORAGE_KEY);
  },
};
