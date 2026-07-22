# Migration Notes — Articles API (Java/Spring Boot → Python/FastAPI)

This document records how the **Articles API** of
`uc-spring-boot-upgrade-microservice-extraction` (Spring Boot 2.6.3 / Java 11,
RealWorld "Conduit" backend) was translated into a Python **FastAPI**
application, and the decisions taken to keep it a **drop-in replacement** with
an identical JSON contract.

## Scope

Only the Articles API was ported. The following endpoints were translated:

| Method | Java route (`ArticlesApi`/`ArticleApi`) | Python route |
| ------ | ---------------------------------------- | ------------ |
| GET    | `/articles`                              | `/api/articles` |
| GET    | `/articles/feed`                         | `/api/articles/feed` |
| GET    | `/articles/{slug}`                       | `/api/articles/{slug}` |
| POST   | `/articles`                              | `/api/articles` |
| PUT    | `/articles/{slug}`                       | `/api/articles/{slug}` |
| DELETE | `/articles/{slug}`                       | `/api/articles/{slug}` |

### URL prefix
The Java service mounts these controllers at `/articles` (no `/api` context
path). The task asked for `/api/articles`, which is also the canonical RealWorld
path, so the Python router uses the `/api/articles` prefix. This is the only
intentional path difference; the request/response bodies are identical.

## Source → target mapping

| Java (Spring Boot)                              | Python (FastAPI)            |
| ----------------------------------------------- | --------------------------- |
| `ArticlesApi`, `ArticleApi` (controllers)       | `app/articles.py` (router)  |
| `ArticleQueryService`, `ArticleCommandService`  | `app/service.py`            |
| `ArticleData`, `ProfileData`, `ArticleDataList` | `app/schemas.py` (Pydantic) |
| `NewArticleParam`, `UpdateArticleParam`         | `app/schemas.py`            |
| MyBatis mappers + `Article`/`User` entities     | `app/models.py` (SQLAlchemy)|
| `V1__create_tables.sql`                         | `app/models.py` schema      |
| `V2__seed_data.sql`                             | `app/seed.py`               |
| `DefaultJwtService`, `JwtTokenFilter`           | `app/security.py`           |
| `CustomizeExceptionHandler` / `ErrorResource`   | `app/errors.py` + handlers  |
| `Article.toSlug`, `Util.isEmpty`                | `app/domain.py`             |

- **Persistence**: SQLAlchemy 2.0 ORM over the same SQLite schema
  (`users`, `articles`, `tags`, `article_tags`, `article_favorites`, `follows`).
- **Request/response models**: Pydantic v2.
- **Auth**: PyJWT verifying the *same* HS512 token the user-service issues
  (same secret), so tokens are interchangeable between the two services.

## JSON contract — parity decisions

The response envelope is preserved exactly:
- single article → `{"article": {...}}`
- list/feed → `{"articles": [...], "articlesCount": N}`

The article object fields (and the intentionally-omitted / extra ones) match the
Java Jackson output:

1. **`author` (not `profileData`)** — Java maps `ProfileData` with
   `@JsonProperty("author")`, and `ProfileData.id` is `@JsonIgnore`. The Python
   `ProfileData` schema is named/shaped identically and omits `id`.

2. **`cursor` field is preserved.** `ArticleData` implements the `Node`
   interface (`getCursor()`), and Jackson serializes that getter, so every
   article in the REST response contains
   `"cursor": {"data": "<updatedAt>"}` (the cursor is built from `updatedAt`).
   This is arguably an accidental leak of an internal pagination concept, but
   because clients receive it today, it is reproduced to stay a true drop-in
   replacement. See `Cursor` in `app/schemas.py`.

3. **Timestamp format.** Joda `DateTime` serializes as ISO-8601 in UTC with
   milliseconds and a `Z` suffix, e.g. `2026-07-15T06:53:44.000Z`.
   `app/domain.format_datetime` reproduces this exactly (always 3 millisecond
   digits, `Z`).

4. **Anonymous vs authenticated `favoritesCount` quirk.** In Java the two read
   paths differ:
   - **Single** article (`findById`/`findBySlug`): extra info is filled **only
     when a user is authenticated**. So an anonymous `GET /articles/{slug}`
     returns `favoritesCount: 0`, `favorited: false`, `author.following: false`
     regardless of the real favorite count.
   - **List/feed** (`fillExtraInfo(List, user)`): `favoritesCount` is filled
     **always**; `favorited`/`following` only when authenticated.

   This is replicated via the `is_list` flag in
   `service.build_article_data`.

5. **`PUT` does not change `updatedAt`.** The domain object updates
   `updatedAt`, but `ArticleMapper.update` never writes the `updated_at`
   column, so the persisted/returned value is unchanged after an update. The
   Python `update_article` deliberately leaves `updated_at` untouched to match.

6. **Slug generation** (`Article.toSlug`) is ported verbatim, including the
   original character class:
   `title.lower()` then replace runs of
   `[& | \uFE30-\uFFA0 ’ ” whitespace ? , .]` with `-`.

7. **Partial update semantics.** `UpdateArticleParam` fields default to `""`;
   only non-empty fields are applied (title change also regenerates the slug),
   mirroring `Article.update` + the `<if test="... != ''">` MyBatis conditions.

8. **Tag handling on create** mirrors `MyBatisArticleRepository.createNew`:
   the incoming `tagList` is de-duplicated (Java wraps it in a `HashSet`),
   existing tags are reused by name, and new tags are inserted with a generated
   UUID id.

9. **Ordering.**
   - List: `ORDER BY created_at DESC` (with `offset`/`limit`).
   - Feed: no `ORDER BY` in Java → natural row (insertion) order; reproduced by
     ordering on the SQLite `rowid`.

## Error responses

| Situation | Status | Body |
| --------- | ------ | ---- |
| Validation failure (`@NotBlank`, duplicate title) | `422` | `{"errors": {"field": ["message"]}}` (matches `ErrorResourceSerializer`) |
| Missing/invalid auth on protected route | `401` | JSON status body |
| Non-author edits/deletes an article | `403` | JSON status body |
| Unknown slug | `404` | JSON status body |

The `422` validation shape is reproduced exactly because it is a
domain-defined contract. For `401/403/404` the **status codes** are reproduced;
the response *bodies* differ from Spring's default `{"timestamp","status",
"error","path"}` (which contains a per-request timestamp and path that are not a
meaningful part of the contract).

## Verification

`tests/` contains pytest **parity tests**. The golden fixtures in
`tests/golden/*.json` were captured from the **running Java service** for the
same inputs. Because both services seed timestamps relative to "now"
(`datetime('now', '-N days')`), the parity assertions normalize the
time-relative timestamp strings (`normalize()` in `tests/conftest.py`) and then
require deep equality of everything else; timestamp **format** and internal
**consistency** (`createdAt == updatedAt == cursor.data` for un-edited
articles) are asserted separately.

Endpoints covered: list (all / limit+offset / by tag / by author / by
favorited / empty / anonymous-vs-authenticated flags), get-by-slug (anonymous /
author / favorited+following), feed (auth required / order / count), create
(auth required / validation error / full lifecycle), update (partial update /
`updatedAt` unchanged / 404 / 403) and delete (204 / 403).

During development the Python app was also diffed live against the running Java
app for every read endpoint — all responses were byte-identical after timestamp
normalization, and Java-issued JWTs authenticate successfully against the Python
service.

## Running

```bash
cd python-articles-api
python3 -m venv .venv && . .venv/bin/activate
pip install -r requirements.txt

# serve (seeds a local SQLite dev.db on startup)
uvicorn app.main:app --port 8000

# tests
PYTHONPATH=. pytest
```
