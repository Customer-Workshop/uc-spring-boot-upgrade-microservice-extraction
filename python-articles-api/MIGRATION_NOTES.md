# Articles API — Java/Spring Boot → Python/FastAPI migration notes

This directory contains a Python **FastAPI** port of the Articles slice of the
Spring Boot RealWorld (Conduit) service that lives at the repository root
(`src/main/java/io/spring/...`). It is designed to be a **drop-in replacement**:
the same endpoints, the same JSON envelopes, and byte-for-byte identical
response bodies for the same data.

## Source → target mapping

| Concern | Java / Spring Boot | Python / FastAPI |
| --- | --- | --- |
| HTTP layer | `ArticlesApi`, `ArticleApi` (`@RestController`) | `app/routers/articles.py` (`APIRouter`) |
| Read model | `ArticleQueryService` + MyBatis read services | `app/service.py` + `app/repository.py` |
| Write model | `ArticleCommandService` + `MyBatisArticleRepository` | `app/repository.py` (`create_article`, `update_article`, `delete_article`) |
| DTOs | `ArticleData`, `ProfileData`, `NewArticleParam`, `UpdateArticleParam` (Lombok + Jackson) | `app/schemas.py` (Pydantic v2) |
| Entities / ORM | MyBatis XML mappers over SQLite | `app/models.py` (SQLAlchemy 2.0 ORM) |
| Auth | `JwtTokenFilter` + `DefaultJwtService` (jjwt, HS512) | `app/security.py` (PyJWT, HS512) |
| Errors | `CustomizeExceptionHandler`, Spring default error attributes | `app/errors.py` |
| Slug | `Article#toSlug` | `app/slug.py` |

## Endpoints

All routes are mounted under `/api/articles` (the Java service serves them under
`/articles`; the `/api` prefix was requested for the port). Envelopes and status
codes are preserved.

| Method | Path | Auth | Notes |
| --- | --- | --- | --- |
| `GET` | `/api/articles` | optional | filters: `tag`, `author`, `favorited`, `offset`, `limit` |
| `GET` | `/api/articles/feed` | required | articles from followed users |
| `GET` | `/api/articles/{slug}` | optional | `404` when missing |
| `POST` | `/api/articles` | required | `422` validation, `201`→ actually `200` (see below) |
| `PUT` | `/api/articles/{slug}` | required | `403` if not author, `404` if missing |
| `DELETE` | `/api/articles/{slug}` | required | `204`, `403` if not author, `404` if missing |

## JSON shape (preserved exactly)

```jsonc
{
  "article": {
    "id": "...", "slug": "...", "title": "...",
    "description": "...", "body": "...",
    "favorited": false, "favoritesCount": 0,
    "createdAt": "2026-07-06T16:34:38.000Z",   // ISO-8601, UTC, millisecond precision, trailing Z
    "updatedAt": "2026-07-06T16:34:38.000Z",
    "tagList": ["java", "spring-boot"],
    "cursor": { "data": "2026-07-06T16:34:38.000Z" },  // see quirk #3
    "author": { "username": "...", "bio": "...", "image": "...", "following": false }  // no id — @JsonIgnore
  }
}
```

Lists use `{ "articles": [...], "articlesCount": N }`.

## Behavioural quirks that were deliberately reproduced

These are non-obvious behaviours of the Java implementation. They are preserved
so responses match exactly; the parity tests would fail otherwise.

1. **Single-article extra info only when authenticated.**
   `GET /articles/{slug}` (and the create/update responses go through the same
   path) only computes `favorited`, `favoritesCount` and `author.following` when
   a user is present. **Unauthenticated single-article reads always return
   `favoritesCount: 0` and `favorited: false`,** even for articles that have
   favorites. The list/feed endpoints, by contrast, always populate
   `favoritesCount`. See `service._fill_single` vs `service._fill_list`
   (mirrors `ArticleQueryService.fillExtraInfo` overloads).

2. **Feed pagination limits joined rows, not distinct articles.**
   `findArticlesOfAuthors` applies `LIMIT offset, count` to the
   `articles ⨯ article_tags` join **with no `ORDER BY`**, then MyBatis collapses
   rows by article id. A small `limit` therefore returns *fewer articles than
   requested and truncated `tagList`s* (e.g. `feed?limit=1` can return one
   article carrying only its first tag). `articlesCount` still reflects the true
   total. This is faithfully reproduced in `repository.find_articles_of_authors`
   by issuing the identical SQL. The list endpoint (`queryArticles`) does *not*
   have this bug because it paginates over `DISTINCT(A.id)` first.

3. **`cursor` object leaks into every article.** `ArticleData implements Node`,
   so Jackson serializes the `getCursor()` property as
   `"cursor": { "data": <updatedAt> }`. It is included for exact parity.

4. **`toSlug` regex.** Ported verbatim, including the CJK compatibility range
   `U+FE30..U+FFA0` and treatment of `& | ’ ” ? , .` and whitespace.

5. **`created_at == updated_at` on create; partial update semantics.**
   `Article#update` only touches fields whose incoming value is non-empty and
   bumps `updatedAt` for each such field. Omitted/empty `UpdateArticleParam`
   fields leave the stored value (and slug) unchanged.

6. **Status codes / error envelopes.**
   - Missing auth on protected routes → `401` with an **empty body**
     (Spring's `HttpStatusEntryPoint`).
   - `403`/`404` → Spring's default error attributes
     `{ timestamp, status, error, path }`.
   - Validation → `{ "errors": { "<field>": ["<message>"] } }`
     (`ErrorResourceSerializer`); `@NotBlank` → `"can't be empty"`, duplicate
     title → `"article name exists"`.
   - `POST /articles` returns **`200`**, not `201` (the Java controller uses
     `ResponseEntity.ok(...)`).

## Persistence choices

- **SQLAlchemy 2.0** ORM models mirror the Flyway schema
  (`src/main/resources/db/migration`). Writes use the ORM.
- **Reads use raw SQL (`text()`) that mirrors the MyBatis XML mappers exactly.**
  This was a deliberate decision: the row-collapsing + join-limit behaviour of
  MyBatis result maps (quirk #2) and the exact result ordering are hard to
  reproduce with idiomatic ORM queries but trivial to guarantee by running the
  same SQL against the same SQLite database. Row→object collapsing is done in
  `repository._group_articles`, matching the MyBatis `articleData` resultMap.

## JWT compatibility

`app/config.py` reuses the Spring `jwt.secret` and `HS512` algorithm, with the
user id as the `sub` claim, so tokens issued by either service validate on the
other.

## Parity testing strategy

`tests/` verifies the Python endpoints return **identical** responses to the
Java version:

1. The seeded SQLite database produced by Flyway was copied to
   `tests/fixtures/seed.db`.
2. Golden responses were captured from the **running Java service** against that
   exact database and stored in `tests/fixtures/golden/*.json`
   (`_index.json` lists the GET cases + required auth).
3. Each test copies `seed.db`, points the FastAPI app at the copy, replays the
   request, and asserts equality with the golden body. GET responses are
   compared in full; create/update/delete normalize the inherently
   non-deterministic fields (generated `id`, timestamps, and `HashSet`-ordered
   `tagList`) while still asserting the deterministic slug, envelope and status.

Run them with:

```bash
cd python-articles-api
python3 -m venv .venv && . .venv/bin/activate
pip install -r requirements.txt
pytest
```

## Running the service

```bash
# point at a copy of the Spring service's SQLite DB (or any DB with the schema)
DATABASE_URL="sqlite:///./dev.db" uvicorn app.main:app --port 8000
```

The schema is auto-created if absent; to get identical data, run against a copy
of the Spring service's database (the same file used by
`spring.datasource.url=jdbc:sqlite:dev.db`).

## Known intentional differences

- Route prefix is `/api/articles` (vs `/articles`).
- `403`/`404` error bodies carry a live `timestamp` and the request `path`
  (which now includes `/api`), matching Spring's *format* but not those two
  volatile values — parity tests assert status + `error`/`status` fields.
- A `null` `tagList` on create is treated as an empty list (the Java constructor
  would NPE / 500); all realistic clients send a list.
