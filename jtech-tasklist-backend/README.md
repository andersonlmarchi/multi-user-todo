# jtech-tasklist — Backend (API)

API REST do TODO multi-usuário: autenticação JWT, listas de tarefas e tarefas com controle de proprietário, persistência PostgreSQL via Hibernate e migrações Flyway.

Documentação do monorepo e do desafio: [README.md](../README.md), [README_CHALLENGE.md](../README_CHALLENGE.md). Contrato HTTP: [docs/API_CONTRACT.md](../docs/API_CONTRACT.md).

---

## Pré-requisitos

- **JDK 17+** (o projeto usa `sourceCompatibility` / `targetCompatibility` 17 no Gradle).
- **PostgreSQL 16+** (ou use o serviço `db` do [docker-compose.yml](../docker-compose.yml) na raiz).
- Variável **`JWT_SECRET`** (recomendada em produção): string em UTF-8 com **pelo menos 32 bytes** (ex.: 32 caracteres ASCII) — exigência do HMAC-SHA256. Em **dev**, se não exportar nada, o `application.yml` já define um valor de exemplo com tamanho suficiente.

---

## Execução local

### 1. Banco de dados

Crie o banco (ou use os defaults abaixo). O `application.yml` espera por padrão:

| Variável    | Padrão sem env        |
| ----------- | --------------------- |
| `DS_URL`    | `localhost`           |
| `DS_PORT`   | `5433`                |
| `DS_DATABASE` | `tasklist_db`      |
| `DS_USER`   | `postgres`            |
| `DS_PASS`   | `postgres`            |

Exemplo com Docker só do Postgres:

```bash
docker run -d --name tasklist-pg -p 5433:5432 \
  -e POSTGRES_DB=tasklist_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16-alpine
```

### 2. Variáveis úteis

```bash
export JWT_SECRET="sua-chave-com-pelo-menos-32-caracteres!!"
export PORT=8080
# opcional: sobrescrever datasource
export DS_URL=localhost
export DS_PORT=5433
```

### 3. Subir a API

Na pasta deste módulo:

```bash
./gradlew bootRun
```

A API sobe em `http://localhost:${PORT:-8080}`. Documentação OpenAPI/Swagger segue os caminhos configurados em `application.yml` (ex.: `/doc/tasklist/v1/api.html`).

---

## Testes

### Todos os testes

```bash
./gradlew test
```

### O que os testes cobrem

- **Unitários (Mockito):** [`AuthServiceTest`](src/test/java/br/com/jtech/tasklist/application/core/services/AuthServiceTest.java) — registro, credenciais inválidas, refresh.
- **Integração:** [`TaskBoardApiIntegrationTest`](src/test/java/br/com/jtech/tasklist/api/TaskBoardApiIntegrationTest.java) — **Testcontainers** sobe um PostgreSQL real; Flyway aplica o schema; fluxo `register → login → CRUD` via **MockMvc** e JWT.

Os testes de integração **não** usam H2 para a API completa, para aproximar dialeto e comportamento do Postgres usado em desenvolvimento e em produção.

---

## Decisões técnicas (e o que não estava na spec original)

A especificação do desafio exige **Spring Boot**, **Hibernate/JPA**, **PostgreSQL**, **JWT**, **BCrypt** e testes. Abaixo entram escolhas **adicionais** ou de detalhamento e o **porquê**.

### Flyway

- **Por quê:** a spec pede Hibernate, mas não obriga ferramenta de migração. Usamos **Flyway** para versionar o schema em SQL explícito (`src/main/resources/db/migration`), em vez de depender só de `ddl-auto`.
- **Benefício:** o mesmo script roda em dev, CI e produção; `ddl-auto=validate` evita drift entre o que o JPA acha que existe e o que está no banco; equipe e avaliadores veem a evolução do modelo em arquivos versionados.

### `spring-boot-starter-oauth2-resource-server` + JJWT

- **Resource Server:** validação de JWT no filtro padrão do Spring Security (`NimbusJwtDecoder` com HS256), alinhada ao ecossistema e ao item de decisão do projeto (validação “tipo OAuth2 resource server”).
- **JJWT (emissão):** os tokens **access** e **refresh** são gerados com **JJWT** e a **mesma chave** configurada em `app.jwt.secret`. Isso evitou problemas operacionais com `NimbusJwtEncoder` + chave simétrica no ambiente de build; o formato continua sendo JWT padrão consumido pelo decoder do Resource Server.

### Testcontainers (PostgreSQL)

- **Por quê:** testes de integração rodam contra **motor real** Postgres, não H2, reduzindo diferenças de SQL, tipos e constraints.
- **Custo:** testes mais lentos e dependência de Docker (ou ambiente compatível com Testcontainers).

### Java 17 no Gradle

- O `build.gradle` usa **Java 17** como `sourceCompatibility`/`targetCompatibility` para funcionar em ambientes sem JDK 21 e alinhar à imagem Docker (Temurin 17). A spec citava Java 17+.

### CORS

- Não faz parte do texto do desafio; é necessário para o SPA Vue (`localhost:5173`) chamar a API em outra origem durante o desenvolvimento.

### Estrutura `application/core/services`

- Serviços `AuthService` e `TaskBoardService` concentram regras entre controllers e repositórios Spring Data, equivalente à camada de “serviço de aplicação” pedida na avaliação em termos de separação de responsabilidades.

---

## Build para produção / Docker

Imagem descrita no [Dockerfile](Dockerfile) da pasta (multi-stage: Gradle `bootJar` + JRE 17). No monorepo, o [docker-compose.yml](../docker-compose.yml) na raiz monta `db`, `backend` e `frontend` juntos.

---

## Estrutura relevante

```
src/main/java/br/com/jtech/tasklist/
├── adapters/input/controllers/    # REST
├── adapters/input/protocols/      # DTOs
├── adapters/output/repositories/  # Spring Data + entidades JPA
├── application/core/services/     # AuthService, TaskBoardService
├── application/core/exceptions/
├── config/security/               # Security, JWT, CORS
└── config/infra/                 # Swagger, exception handler
src/main/resources/db/migration/   # Flyway
```
