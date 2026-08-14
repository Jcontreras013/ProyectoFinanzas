import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { usersApi, type CreateUserRequest, type UpdateUserRequest } from "@/api/users";
import { ApiRequestError } from "@/api/client";
import { PageHeader } from "@/components/layout/PageHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { Role, User } from "@/types/domain";

const ROLE_LABELS: Record<Role, string> = { ADMIN: "Administrador", ACCOUNTANT: "Contador", AUDITOR: "Auditor" };

export function UsersPage() {
  const queryClient = useQueryClient();
  const { data: users, isLoading } = useQuery({ queryKey: ["users"], queryFn: usersApi.list });

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [role, setRole] = useState<Role>("ACCOUNTANT");
  const [active, setActive] = useState(true);
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  const resetForm = () => {
    setEditingId(null);
    setEmail("");
    setFullName("");
    setRole("ACCOUNTANT");
    setActive(true);
    setPassword("");
  };

  const saveMutation = useMutation({
    mutationFn: () => {
      if (editingId) {
        const request: UpdateUserRequest = { fullName, role, active, password: password || undefined };
        return usersApi.update(editingId, request);
      }
      const request: CreateUserRequest = { email, password, fullName, role };
      return usersApi.create(request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setShowForm(false);
      resetForm();
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiRequestError ? err.message : "No se pudo guardar el usuario"),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: string) => usersApi.deactivate(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });

  const startEdit = (user: User) => {
    setEditingId(user.id);
    setEmail(user.email);
    setFullName(user.fullName);
    setRole(user.role);
    setActive(user.active);
    setPassword("");
    setShowForm(true);
  };

  return (
    <div>
      <PageHeader
        title="Usuarios"
        description="Gestión de accesos y roles del sistema."
        actions={<Button onClick={() => { resetForm(); setShowForm(true); }}><Plus className="h-4 w-4" /> Nuevo usuario</Button>}
      />

      {showForm && (
        <Card className="mb-6">
          <CardContent className="pt-6">
            <form className="grid grid-cols-1 gap-4 sm:grid-cols-2" onSubmit={(e) => { e.preventDefault(); saveMutation.mutate(); }}>
              <div className="space-y-1.5">
                <Label htmlFor="fullName">Nombre completo</Label>
                <Input id="fullName" required value={fullName} onChange={(e) => setFullName(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="email">Correo</Label>
                <Input id="email" type="email" required disabled={!!editingId} value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="role">Rol</Label>
                <Select id="role" value={role} onChange={(e) => setRole(e.target.value as Role)}>
                  {Object.entries(ROLE_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="password">{editingId ? "Nueva contraseña (opcional)" : "Contraseña"}</Label>
                <Input id="password" type="password" required={!editingId} minLength={8} value={password} onChange={(e) => setPassword(e.target.value)} />
              </div>
              {editingId && (
                <label className="flex items-center gap-2 text-sm">
                  <input type="checkbox" checked={active} onChange={(e) => setActive(e.target.checked)} />
                  Activo
                </label>
              )}
              {error && <p className="text-sm text-destructive sm:col-span-2">{error}</p>}
              <div className="flex gap-2 sm:col-span-2">
                <Button type="submit" disabled={saveMutation.isPending}>{saveMutation.isPending ? "Guardando..." : "Guardar"}</Button>
                <Button type="button" variant="outline" onClick={() => setShowForm(false)}>Cancelar</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Cargando...</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nombre</TableHead>
              <TableHead>Correo</TableHead>
              <TableHead>Rol</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead className="text-right">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {(users ?? []).map((user) => (
              <TableRow key={user.id} className={!user.active ? "opacity-50" : undefined}>
                <TableCell className="font-medium">{user.fullName}</TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell><Badge variant="primary">{ROLE_LABELS[user.role]}</Badge></TableCell>
                <TableCell>{user.active ? "Activo" : "Inactivo"}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="sm" onClick={() => startEdit(user)}>Editar</Button>
                    {user.active && (
                      <Button variant="ghost" size="sm" onClick={() => deactivateMutation.mutate(user.id)}>Desactivar</Button>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
