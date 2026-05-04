# Migration Notes: Java Spring Boot → Python FastAPI (Articles API)

## Overview

This document records the translation decisions made while porting the Articles API from the Java/Spring Boot monolith to a Python FastAPI application. The goal was a **drop-in replacement** that exposes the same REST endpoints with identical JSON response shapes.

---

## Endpoint Mapping

| HTTP Method | Path                     | Java Controller        | Python Module         |
|-------------|--------------------------|------------------------|-----------------------|
| GET         | `/api/articles`          | `ArticlesApi`          | `app.articles`        |
| GET         | `/api/articles/feed`     | `ArticlesApi`          | `app.articles`        |
| GET         | `/api/articles/{slug}`   | `ArticleApi`           | `app.articles`        |
| POST        | `/api/articles`          | `ArticlesApi`          | `app.articles`        |
| PUT         | `/api/articles/{slug}`   | `ArticleApi`           | `app.articles`        |
| DELETE      | `/api/articles/{slug}`   | `ArticleApi`           | `app.articles`        |

> **Note:** The Java version maps endpoints under `/articles` and relies on the Spring property `server.servlet.context-path` or front-proxy to add the `/api` prefix. The Python version includes `/api/articles` directly in the router prefix so it is self-contained.

---

## Technology Translation

| Concern               | Java (Spring Boot)                          | Python (FastAPI)                              |
|------------------------|---------------------------------------------|-----------------------------------------------|
| Web framework          | Spring MVC (`@RestController`)              | FastAPI `APIRouter`                           |
| ORM / persistence      | MyBatis XML mappers + SQLite                | SQLAlchemy 2.0 mapped columns + SQLite        |
| Request validation     | `javax.validation` (`@NotBlank`)            | Pydantic `BaseModel` + manual checks          |
| Response serialization | Jackson (`@JsonProperty`, `@JsonRootName`)  | Pydantic `BaseModel` with `model_dump()`      |
| DateTime handling      | Joda-Time `DateTime` → ISO 8601 via custom serializer | Python `datetime` → manual ISO 8601 formatting |
| Authentication         | Spring Security + JWT (`HS512`)             | FastAPI `Depends()` + `python-jose` (`HS512`) |
| Password hashing       | BCrypt                                      | `passlib[bcrypt]` (available but unused in Articles API) |
| Testing                | JUnit 5 + MockMvc + RestAssured + Mockito   | pytest + FastAPI `TestClient` (httpx)         |

---

## Key Translation Decisions

### 1. JSON Root Wrapping

The Java API uses `spring.jackson.deserialization.UNWRAP_ROOT_VALUE=true` with `@JsonRootName("article")` to unwrap incoming payloads like `{"article": {...}}`. In FastAPI, we model this explicitly with Pydantic wrapper types:

- `NewArticleWrapper` containing `article: NewArticleRequest`
- `UpdateArticleWrapper` containing `article: UpdateArticleRequest`

Response wrapping (`{"article": {...}}` for single, `{"articles": [...], "articlesCount": N}` for lists) is handled via `SingleArticleResponse` and `MultipleArticlesResponse`.

### 2. DateTime Serialization

Java uses a custom Jackson serializer that formats Joda-Time `DateTime` as ISO 8601 with millisecond precision and UTC zone: `2024-01-15T10:30:00.000Z`.

Python equivalent uses `strftime` with manual millisecond formatting:
```python
dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{dt.microsecond // 1000:03d}Z"
```

### 3. Slug Generation

Java regex: `"[\\&|[\\uFE30-\\uFFA0]|\\'|\\\"\\s\\?\\,\\.]+"`
Python regex: `r"[\s&\ufe30-\uffa0'\"?,\\.]+"`

Both lower-case the title and replace matched characters with hyphens.

### 4. Entity IDs

The Java domain uses `UUID.randomUUID().toString()` for entity IDs. The Python version uses `str(uuid.uuid4())` identically, stored as `VARCHAR(255)` primary keys to match the existing SQLite schema.

### 5. Profile Data Shape

The Java `ProfileData` DTO uses `@JsonIgnore` on the `id` field so it is excluded from JSON output. The Python `ProfileResponse` Pydantic model simply omits the `id` field entirely, achieving the same effect.

### 6. Authorization Model

Java's `AuthorizationService.canWriteArticle()` checks `user.getId().equals(article.getUserId())`. The Python version performs the same check inline:
```python
if article.user_id != current_user.id:
    raise HTTPException(status_code=403, detail="Not authorized")
```

### 7. Authentication Token Format

Both versions use `Authorization: Token <jwt>` header format (not `Bearer`), consistent with the RealWorld API spec. The JWT is signed with HS512 using the same secret key.

### 8. Pagination

Java's `Page` class caps `limit` at 100 and defaults to 20. Python replicates this via FastAPI `Query` parameter constraints: `Query(20, ge=1, le=100)`.

### 9. SQLAlchemy vs MyBatis

MyBatis uses hand-written SQL in XML mapper files. SQLAlchemy uses the ORM query API with `joinedload` for eager-loading relationships (tags, author). The article favorites count and user-specific favorited/following flags are computed via separate count queries to match the Java service's behavior.

### 10. Error Responses

- **404 Not Found**: Both versions return 404 when an article slug doesn't match.
- **403 Forbidden**: Returned when a non-owner attempts to update/delete an article.
- **401 Unauthorized**: Returned when no/invalid JWT is provided for authenticated endpoints.
- **422 Unprocessable Entity**: Returned for validation errors (empty body, duplicate title).
- **204 No Content**: DELETE returns 204 with empty body (Java returns `ResponseEntity.noContent().build()`).

---

## Database Schema Compatibility

The Python app uses the **exact same SQLite schema** defined in the Flyway migrations (`V1__create_tables.sql`). Table names, column names, types, and constraints are preserved. The SQLAlchemy models map to the existing tables without modification.

---

## What Is Not Ported

- **GraphQL API** (`ArticleDatafetcher`, `ArticleMutation`): Out of scope; only REST endpoints were requested.
- **Comments API** (`CommentsApi`): Not part of the Articles API translation scope.
- **User registration/login** (`UsersApi`, `CurrentUserApi`): Only JWT validation is ported for authentication; user management remains in the Java service.
- **Article favorites toggling** (`ArticleFavoriteApi`): Separate endpoint, not included.
- **Cursor-based pagination** (`findRecentArticlesWithCursor`): Only offset/limit pagination is exposed via the REST API.
