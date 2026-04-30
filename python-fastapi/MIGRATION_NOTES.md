# Migration Notes: Java Spring Boot → Python FastAPI (Articles API)

## Overview

This document describes the translation of the RealWorld "Conduit" Articles API from
Java/Spring Boot to Python/FastAPI. The Python version is a **drop-in replacement** that
exposes identical REST endpoints with the same JSON response shapes.

## Endpoint Mapping

| HTTP Method | Path                    | Java Controller       | Python Router             |
|-------------|-------------------------|-----------------------|---------------------------|
| GET         | `/api/articles`         | `ArticlesApi`         | `routers/articles.py`     |
| GET         | `/api/articles/feed`    | `ArticlesApi`         | `routers/articles.py`     |
| GET         | `/api/articles/:slug`   | `ArticleApi`          | `routers/articles.py`     |
| POST        | `/api/articles`         | `ArticlesApi`         | `routers/articles.py`     |
| PUT         | `/api/articles/:slug`   | `ArticleApi`          | `routers/articles.py`     |
| DELETE      | `/api/articles/:slug`   | `ArticleApi`          | `routers/articles.py`     |

## Technology Mapping

| Concern              | Java / Spring Boot                        | Python / FastAPI                          |
|----------------------|-------------------------------------------|-------------------------------------------|
| Web framework        | Spring MVC (`@RestController`)            | FastAPI (`APIRouter`)                     |
| ORM / persistence    | MyBatis (XML mappers)                     | SQLAlchemy 2.0 (declarative models)       |
| Database             | SQLite via JDBC                           | SQLite via SQLAlchemy                     |
| Request validation   | `javax.validation` (`@NotBlank`)          | Pydantic v2 models                       |
| Response DTOs        | Lombok `@Data` classes + Jackson          | Pydantic `BaseModel` with `model_config`  |
| Authentication       | Spring Security + `OncePerRequestFilter`  | FastAPI `Depends()` + manual JWT parsing  |
| JWT library          | `io.jsonwebtoken` (jjwt)                  | `python-jose`                             |
| Password hashing     | BCrypt (Spring Security)                  | `passlib[bcrypt]`                         |
| Slug generation      | Manual regex in `Article.toSlug()`        | `re.sub` in `article_service._slugify()`  |
| Dependency injection | Spring `@Autowired` / constructor DI      | FastAPI `Depends()` function injection    |
| Testing              | JUnit 5 + MockMvc                         | pytest + `TestClient` (Starlette)         |
| Linting              | Spotless (Google Java Format)             | Ruff                                      |

## Key Translation Decisions

### 1. Project Structure

The Java project uses a layered DDD-style architecture (`core`, `application`, `infrastructure`,
`api`). The Python version simplifies to:

```
python-fastapi/
├── app/
│   ├── main.py            # FastAPI app entrypoint
│   ├── config.py           # Settings via pydantic-settings
│   ├── database.py         # SQLAlchemy engine + session
│   ├── models/models.py    # SQLAlchemy ORM models
│   ├── schemas/article.py  # Pydantic request/response models
│   ├── services/           # Business logic + auth
│   └── routers/articles.py # FastAPI route handlers
└── tests/
```

This is idiomatic for a FastAPI project while preserving separation of concerns.

### 2. CQRS → Unified Service

The Java version uses a CQRS pattern with separate `ArticleCommandService` (write) and
`ArticleQueryService` (read). The Python version consolidates into a single
`article_service.py` module since SQLAlchemy ORM handles both reads and writes naturally.

### 3. MyBatis XML → SQLAlchemy ORM

The Java version uses MyBatis with hand-written SQL in XML mapper files. The Python version
uses SQLAlchemy's declarative ORM, which provides equivalent functionality with less boilerplate.
The same database schema (SQLite) is preserved.

### 4. JSON Response Shape Preservation

Pydantic models use **camelCase** field names (e.g., `favoritesCount`, `tagList`, `createdAt`)
to match Jackson's serialization output. The Java `@JsonProperty("author")` annotation on
`ProfileData` is replicated by naming the Pydantic field `author` directly.

The wrapper structure is preserved:
- Single article: `{"article": {...}}`
- Article list: `{"articles": [...], "articlesCount": N}`

### 5. Authentication

The Java `JwtTokenFilter` extracts tokens from `Authorization: Token <jwt>` headers.
The Python equivalent uses FastAPI dependency injection:
- `get_current_user_optional`: Returns `None` for anonymous requests
- `get_current_user_required`: Raises 401 if no valid token

Both use HS512 with the same shared secret, so tokens are cross-compatible.

### 6. Slug Generation

The Java `Article.toSlug()` uses a complex regex including Unicode ranges. The Python
`_slugify()` function uses a simplified regex that handles ASCII punctuation and whitespace,
which is sufficient for the RealWorld spec.

### 7. Database Schema Compatibility

The Python SQLAlchemy models map to the **exact same tables** as the Flyway migrations:
- `users`, `articles`, `tags`, `article_tags`, `article_favorites`, `follows`, `comments`

This means the Python API can run against the same SQLite database as the Java API.

### 8. Error Handling

| Java Exception                  | Python Equivalent                    | HTTP Status |
|---------------------------------|--------------------------------------|-------------|
| `ResourceNotFoundException`     | `HTTPException(404)`                 | 404         |
| `NoAuthorizationException`      | `HTTPException(403)`                 | 403         |
| Authentication failure          | `HTTPException(401)`                 | 401         |
| `DuplicatedArticleConstraint`   | `HTTPException(422)` via `ValueError`| 422         |

### 9. Pagination

The Java `Page` class caps `limit` at 100 and defaults `offset` to 0 / `limit` to 20.
The Python version replicates this exact logic in the service layer.

## Running the Python API

```bash
cd python-fastapi
python3 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"

# Run the server
uvicorn app.main:app --host 0.0.0.0 --port 8000

# Run tests
pytest tests/ -v
```

## What Is Not Translated

This migration covers **only the Articles API endpoints**. The following are out of scope:
- User registration / login (`/api/users`)
- Profile endpoints (`/api/profiles`)
- Comment endpoints (`/api/articles/:slug/comments`)
- Tag listing (`/api/tags`)
- Article favorite endpoints (`/api/articles/:slug/favorite`)
- GraphQL API (Netflix DGS)
- Frontend (Next.js)
