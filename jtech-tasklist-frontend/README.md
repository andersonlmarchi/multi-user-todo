# jtech-tasklist — Frontend (Vue 3)

SPA do TODO multi-usuário: login/registro contra a API, listas e tarefas com Pinia persistido, UI com Vuetify e chamadas HTTP com Axios (access + refresh automático).

Documentação do monorepo: [README.md](../README.md). Contrato da API: [docs/API_CONTRACT.md](../docs/API_CONTRACT.md).

---

## Pré-requisitos

- **Node.js** `^20.19.0` ou `>=22.12.0` (conforme `package.json`).
- API backend em execução (por padrão `http://localhost:8080`).

---

## Execução local

### 1. Instalar dependências

```bash
npm install
```

### 2. URL da API

O Vite lê `VITE_API_BASE_URL`. Já existe exemplo em [`.env.development`](.env.development):

```env
VITE_API_BASE_URL=http://localhost:8080
```

Ajuste se a API estiver em outra origem/porta. Em Docker Compose (raiz do monorepo), a variável é injetada no serviço do frontend para apontar para o host.

### 3. Modo desenvolvimento

```bash
npm run dev
```

Abra o endereço indicado no terminal (geralmente `http://localhost:5173`). O backend deve permitir essa origem no CORS (já configurado para `localhost:5173` na API).

### Build de produção

```bash
npm run build
```

Saída em `dist/`. Pré-visualização local do build:

```bash
npm run preview
```

### Checagem de tipos

```bash
npm run type-check
```

---

## Testes

```bash
npm run test:unit
```

Hoje a suíte inclui testes de store (ex.: [`src/stores/__tests__/auth.spec.ts`](src/stores/__tests__/auth.spec.ts)) com Vitest, com `api` mockado.

---

## Decisões técnicas (e o que vai além do template Vue)

A spec do desafio pedia **Vue 3**, **Vue Router**, **Pinia**, **Vuetify** (ou equivalente Material), **Vitest** e preferência por **TypeScript**. Abaixo: escolhas extras e motivos.

### Vuetify 4 + `vite-plugin-vuetify`

- **Por quê:** Material Design exigido pelo desafio; Vuetify integra bem a Vue 3 + Vite com auto-import de componentes.
- O template original do projeto era “Vue + Vite” sem UI kit; adicionamos Vuetify para formulários, layout (`v-app`, drawer, listas) e consistência visual.

### Axios (em vez de só `fetch`)

- **Por quê:** decisão arquitetural do projeto: **interceptors** para anexar `Authorization: Bearer` e, em **401**, tentar **refresh** com `POST /auth/refresh` e repetir a requisição.
- Centraliza a lógica em [`src/services/api.ts`](src/services/api.ts) sem espalhar `fetch` nas views.

### `pinia-plugin-persistedstate`

- **Por quê:** decisão de produto: persistir **tokens** e **cache de listas/tarefas** no `localStorage` para melhor UX após F5 (com trade-off de dados eventualmente defasados — mitigável com refetch após mutações).
- A spec do desafio pedia “estado persistido”, sem impor biblioteca; o plugin oficializa a persistência sem reinventar serialização.

### Duas stores: `authStore` + `appStore`

- **authStore:** sessão (access/refresh, email).
- **appStore:** listas selecionadas, tarefas, flags de visualização de arquivados.
- Separa autenticação do restante do estado, alinhado à decisão “auth + app com módulos” sem um único store gigante sem fronteiras.

### Rotas e guards

- `/login`, `/register` (somente convidado se já autenticado), `/app` (exige token). Implementação em [`src/router/index.ts`](src/router/index.ts).

### TypeScript + `vue-tsc`

- Tipagem nos DTOs da API em [`src/types/api.ts`](src/types/api.ts) e nos componentes; `vue-tsc` substitui `tsc` para arquivos `.vue`.

---

## Estrutura relevante

```
src/
├── views/           # LoginView, RegisterView, BoardView
├── stores/          # auth.ts, app.ts (+ testes)
├── services/        # Axios (api.ts)
├── router/
├── types/
├── plugins/         # vuetify.ts
├── App.vue
└── main.ts
```

---

## IDE recomendada

[VS Code](https://code.visualstudio.com/) + [Vue (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.volar). Para imports `.vue`, o projeto usa `vue-tsc` no script `type-check`.

---

## Lint e formatação

```bash
npm run lint
npm run format
```
