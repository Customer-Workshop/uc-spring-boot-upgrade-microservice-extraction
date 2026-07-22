# Articles API — FastAPI port

A Python **FastAPI** re-implementation of the Articles API from the
Spring Boot RealWorld ("Conduit") backend in this repository. It is a **drop-in
replacement** that preserves the same REST endpoints and JSON response shape,
using **SQLAlchemy** for persistence and **Pydantic** for request/response
models.

See [`MIGRATION_NOTES.md`](./MIGRATION_NOTES.md) for the full translation
decisions and parity notes.

## Endpoints

- `GET /api/articles` — list (query: `offset`, `limit`, `tag`, `author`, `favorited`)
- `GET /api/articles/feed` — personalized feed (auth required)
- `GET /api/articles/{slug}` — single article
- `POST /api/articles` — create (auth required)
- `PUT /api/articles/{slug}` — update (auth required, author only)
- `DELETE /api/articles/{slug}` — delete (auth required, author only)

Authentication uses the same HS512 JWT (`Authorization: Token <jwt>`) as the
Java service, so tokens are interchangeable.

## Quick start

```bash
python3 -m venv .venv && . .venv/bin/activate
pip install -r requirements.txt

uvicorn app.main:app --port 8000   # seeds a local SQLite dev.db on startup
PYTHONPATH=. pytest                 # parity tests against captured Java golden responses
```

## Layout

```
app/
  main.py       FastAPI app + exception handlers
  articles.py   route handlers
  service.py    query/command logic (ports ArticleQueryService/CommandService)
  models.py     SQLAlchemy models (ports the V1 schema)
  schemas.py    Pydantic request/response models
  security.py   JWT auth (ports DefaultJwtService/JwtTokenFilter)
  seed.py       seed data (ports V2__seed_data.sql)
  domain.py     slug/datetime helpers (ports Article.toSlug, Util.isEmpty)
  errors.py     422/401/403/404 exceptions
tests/
  test_articles.py  parity tests
  golden/           JSON responses captured from the running Java service
```
