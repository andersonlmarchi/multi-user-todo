# Contrato REST — Tasklist API (`/api/v1`)

Prefixo base: `/api/v1`.

## Autenticação (público)

| Método | Caminho | Corpo | Resposta |
|--------|---------|-------|----------|
| POST | `/auth/register` | `{ "name", "email", "password" }` | `201` + `{ "accessToken", "refreshToken", "tokenType": "Bearer", "expiresIn" }` |
| POST | `/auth/login` | `{ "email", "password" }` | `200` + mesmo formato de tokens |
| POST | `/auth/refresh` | `{ "refreshToken" }` | `200` + novo par de tokens |

Demais rotas exigem cabeçalho `Authorization: Bearer <accessToken>`.

## Listas de tarefas

Ownership: `userId` vem do JWT (`sub`); nunca do corpo.

| Método | Caminho | Notas |
|--------|---------|--------|
| GET | `/task-lists?archived=false|true` | Lista do usuário; padrão `archived=false`. |
| POST | `/task-lists` | `{ "name" }` — `201` + TaskListResponse |
| GET | `/task-lists/{id}` | `404` se não existir ou não for do usuário |
| PUT | `/task-lists/{id}` | `{ "name" }` |
| POST | `/task-lists/{id}/archive` | Soft delete / arquivar |
| POST | `/task-lists/{id}/unarchive` | Desarquivar |

## Tarefas (aninhadas)

| Método | Caminho | Notas |
|--------|---------|--------|
| GET | `/task-lists/{listId}/tasks?archived=false|true` | Tarefas da lista (lista deve pertencer ao usuário) |
| POST | `/task-lists/{listId}/tasks` | `{ "title" }` — título obrigatório, máx. 500 caracteres; sem regra de unicidade |
| GET | `/task-lists/{listId}/tasks/{taskId}` | |
| PUT | `/task-lists/{listId}/tasks/{taskId}` | `{ "title", "done" }` (campos opcionais com “patch” semântico: enviados são aplicados) |
| POST | `/task-lists/{listId}/tasks/{taskId}/archive` | |
| POST | `/task-lists/{listId}/tasks/{taskId}/unarchive` | |

## Erros

Formato existente [`ApiError`](jtech-tasklist-backend/src/main/java/br/com/jtech/tasklist/config/infra/exceptions/ApiError.java) via `GlobalExceptionHandler`.

Códigos usuais: `400` validação, `401` não autenticado, `403` proibido, `404` não encontrado, `409` conflito (ex.: email já cadastrado).
