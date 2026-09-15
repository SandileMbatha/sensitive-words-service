# Sensitive Words Service

A small Spring Boot microservice that:

1. Lets an admin/internal system manage a list of "sensitive words" (CRUD).
2. Exposes a single business endpoint that takes a free-text message and returns it with every
   sensitive word starred out, e.g.

   ```
   Request:  "You need to create a string"
   Response: "You need to ****** a string"
   ```

Built for a technical assessment based on the brief in
[`assignment/Interview-SqlWords.pdf`](assignment/Interview-SqlWords.pdf), using the SQL keyword
list in [`assignment/sql_sensitive_list.txt`](assignment/sql_sensitive_list.txt) as the seed data
(228 words, preloaded via Flyway).

## Tech stack

| Concern           | Choice                                   |
|--------------------|-------------------------------------------|
| Language / runtime | Java 21                                    |
| Framework          | Spring Boot 3.5.3 (Web, Data JPA, Validation, Cache, Actuator) |
| Database           | Microsoft SQL Server                       |
| Migrations / seed  | Flyway                                     |
| API docs           | springdoc-openapi (Swagger UI)             |
| Tests              | JUnit 5, Mockito, MockMvc, AssertJ         |
| Boilerplate        | Lombok                                     |

## Project layout

```
src/main/java/com/sensitivewords/
├── controller/    # REST controllers (CRUD + sanitize)
├── service/       # Business logic
├── repository/    # Spring Data JPA repositories
├── entity/        # JPA entities
├── dto/           # Request/response records
├── exception/     # Custom exceptions + @RestControllerAdvice
└── config/        # OpenAPI bean
src/main/resources/db/migration/  # Flyway scripts (schema + seed data)
src/test/java/...                 # Unit tests, mirroring the main package layout
```

Layering is a plain **Controller → Service → Repository**. Nothing fancier than that was needed
for the scope of this assignment.

## Running it locally

### Option A: everything in Docker

```bash
docker compose up --build
```

This starts SQL Server, creates the `sensitivewords` database, and starts the service on
`http://localhost:8080`. Flyway then creates the schema and seeds the 228 words automatically the
first time the app connects.

> If you're behind a corporate proxy that does TLS interception, the Maven build step inside
> Docker may fail to reach Maven Central unless the proxy's root CA is trusted inside the build
> container - that's an environment/network detail, not an application issue. On a normal network
> or in CI it builds cleanly (verified independently for this submission by running the Maven
> build stage on the host and the runtime stage in Docker).

### Option B: SQL Server in Docker, app on the host

```bash
docker compose up -d mssql mssql-init
mvn spring-boot:run
```

Override connection details with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables if
needed (see `application.yml` for defaults, which match `docker-compose.yml`).

### API docs

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/actuator/health

## Endpoints

### CRUD - internal consumption (`/api/v1/sensitive-words`)

| Method | Path                       | Description               |
|--------|----------------------------|----------------------------|
| POST   | `/api/v1/sensitive-words`      | Add a new sensitive word |
| GET    | `/api/v1/sensitive-words`      | List all sensitive words (alphabetical) |
| GET    | `/api/v1/sensitive-words/{id}` | Get one sensitive word |
| PUT    | `/api/v1/sensitive-words/{id}` | Update a sensitive word |
| DELETE | `/api/v1/sensitive-words/{id}` | Remove a sensitive word |

### Business logic - external consumption (`/api/v1/sanitize`)

```bash
curl -X POST http://localhost:8080/api/v1/sanitize \
  -H "Content-Type: application/json" \
  -d '{"message":"SELECT * FROM sensitiveWords"}'

# {"originalMessage":"SELECT * FROM sensitiveWords","sanitizedMessage":"****** * FROM sensitiveWords"}
```

**Matching rules:** case-insensitive, whole-word only (so `TABLE` is starred but `TABLETOP` is
not), and each matched word is replaced by asterisks of the same length. This is a deliberate,
simple rule that matches the example in the assignment brief. It does **not** attempt multi-word
phrase matching (the seed list contains one phrase, `"SELECT * FROM"`, which is preloaded as data
but only matched as individual words today) - see "Additional enhancements" below for how I'd
extend it.

## Tests

```bash
mvn test
```

26 unit tests covering:
- `SanitizeServiceTest` - the starring-out logic (case-insensitivity, whole-word matching, no
  false positives on partial matches, empty input).
- `SensitiveWordServiceTest` - CRUD logic, duplicate detection, not-found handling.
- `SensitiveWordControllerTest` / `SanitizeControllerTest` - HTTP layer via `MockMvc`, including
  validation and error responses.

Repository/database code isn't covered by these tests on purpose - the value of a unit test here
would be low (it's a thin Spring Data interface), and I'd rather add a couple of integration tests
against a real SQL Server (via Testcontainers) than fake it with an in-memory database that
doesn't behave like SQL Server. See "Additional enhancements".

---

## What would you do to enhance performance of your project?

- **Cache the word list** (already done): `/api/v1/sanitize` is the hot, externally-facing
  endpoint, so it shouldn't hit the database on every call for a list that rarely changes. The
  word list is loaded once into a `Set<String>` via Spring Cache (`@Cacheable`) and evicted
  whenever a word is created/updated/deleted. Today that's the default in-memory
  `ConcurrentMapCacheManager`; in a multi-instance deployment I'd swap in Redis so all instances
  share one cache and invalidate together.
- **Index/collation-backed uniqueness check**: the `word` column has a unique constraint, so
  duplicate checks and lookups use an index rather than a table scan.
- **Only select what's needed**: the sanitize path uses a projection query
  (`select w.word from SensitiveWord w`) instead of loading full entities, which avoids pulling
  back the `id`/timestamps for every row.
- **Connection pooling** is already handled by HikariCP (Spring Boot default) - I'd tune pool size
  based on load testing rather than guessing.
- **Precompiled regex**: the word-matching pattern is compiled once (static final `Pattern`), not
  per request.
- For real scale, I'd also look at: read replicas for the CRUD list/read endpoints, pagination on
  `GET /sensitive-words` once the list grows well beyond a few hundred entries, and load testing
  (e.g. Gatling/k6) to find the actual bottleneck before optimizing further.

## What additional enhancements would you add to make it more complete?

- **AuthN/AuthZ** on the CRUD endpoints (they're internal-only) - e.g. OAuth2/JWT with a
  `SENSITIVE_WORDS_ADMIN` role, while the `/sanitize` endpoint stays open to trusted internal
  callers only, behind an API gateway/mTLS.
- **Multi-word phrase matching** - support sensitive *phrases*, not just single words (the seed
  list has one: `"SELECT * FROM"`). This would need a small change to the matcher (e.g. an
  Aho-Corasick style multi-pattern search) so longer phrases are matched before falling back to
  single words.
- **Pagination + search** on `GET /api/v1/sensitive-words` once the list grows.
- **Auditing** - who added/changed/removed a word and when (basic `createdAt`/`updatedAt` exist;
  I'd add `createdBy`/`updatedBy` once auth is in place).
- **Bulk import endpoint** for the word list instead of one-by-one `POST` calls.
- **Integration tests with Testcontainers** running against a real SQL Server container, so CI
  verifies the Flyway migrations and JPA mappings against the real database engine.
- **Rate limiting** on the public `/sanitize` endpoint.
- **Distributed cache (Redis)** once this runs as more than one instance, so cache invalidation is
  consistent across instances.
- **Structured/correlation-id logging** and basic metrics (Micrometer + Prometheus) for
  observability in production.

## How would you deploy this in a production environment?

1. **CI pipeline** (e.g. GitHub Actions): checkout → `mvn verify` (compile, unit tests) → build a
   Docker image → push to a container registry (ECR/ACR/GHCR) tagged with the commit SHA →
   optionally run a quick smoke/integration test against a throwaway SQL Server container.
2. **Database**: a managed SQL Server instance (Azure SQL / AWS RDS for SQL Server) rather than a
   self-hosted container. Flyway migrations run automatically on startup as they do locally, so
   schema changes ship with the application version.
3. **Runtime**: deploy the image to Kubernetes - a `Deployment` (2+ replicas for availability) and
   a `Service`, with:
   - Liveness/readiness probes wired to Spring Boot Actuator (`/actuator/health/liveness` and
     `/readiness`).
   - Config/secrets externalized via `ConfigMap`/`Secret` (or Vault, per this org's convention)
     rather than baked into the image - DB host/credentials, cache config, etc.
   - A `HorizontalPodAutoscaler` on CPU/memory (or request rate) since `/sanitize` is externally
     facing and traffic can spike.
   - Rolling updates by default; the app is stateless so this is safe with no extra work.
4. **Networking**: an API gateway/ingress in front for TLS termination, routing, and rate limiting
   - only `/api/v1/sanitize` would be exposed externally; the CRUD endpoints would stay internal
     (private ingress class / network policy) or sit behind auth as noted above.
5. **Observability**: centralized logs (e.g. ELK/Fluent Bit), metrics scraped by Prometheus and
   visualized in Grafana, and alerts on error rate/latency/DB connection saturation.
6. **Promotion path**: the same image is promoted dev → int → prod via config changes only (no
   rebuilds), verified with ArgoCD (or similar) sync status before considering a deployment done.
