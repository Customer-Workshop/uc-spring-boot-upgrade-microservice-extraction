# Migration Notes: Java/Spring Boot → Python/FastAPI

This document records the key translation decisions made while porting the
Articles API from a Java/Spring Boot monolith to a Python/FastAPI application.

---

## 1. Framework Mapping

| Java / Spring Boot | Python / FastAPI | Notes |
|-|-|-|
| `@RestController` + `@RequestMapping` | `APIRouter(prefix=…)` | FastAPI routers replace Spring controllers |
| `@GetMapping`, `@PostMapping`, etc. | `@router.get()`, `@router.post()`, etc. | Decorator-per-verb is identical in both |
| `@RequestParam` | `Query(…)` (FastAPI) | Default values preserved (`offset=0`, `limit=20`) |
| `@PathVariable` | Function parameter with `{slug}` in path | Same semantics |
| `@RequestBody` + `@JsonRootName` | Pydantic `BaseModel` wrapper | Java wraps under `"article"` key; replicated with `NewArticleRequestWrapper` / `UpdateArticleRequestWrapper` |
| `@Valid` + `@NotBlank` | Manual validation in route + 422 response | FastAPI's built-in validation is supplemented with explicit checks to match Java error shape |
| `ResponseEntity.ok(…)` | Direct return + `response_model` | FastAPI serializes the return value automatically |
| `ResponseEntity.noContent().build()` | `status_code=204`, return `None` | Identical HTTP semantics |

## 2. Persistence

| Java | Python | Notes |
|-|-|-|
| MyBatis (XML mappers) | SQLAlchemy 2.0 ORM | MyBatis is a SQL-mapping framework; SQLAlchemy ORM provides a more Pythonic equivalent with the same relational model |
| `ArticleRepository` (interface) | SQLAlchemy `Session.query(Article)` | Repository pattern replaced by direct session queries in route handlers |
| `ArticleReadService` (read-optimised queries) | Inline SQLAlchemy queries | The Java codebase separates read/write; Python version keeps them together for simplicity |
| H2 / MySQL | SQLite (dev) | Lightweight default; swap `SQLALCHEMY_DATABASE_URL` for production |
| UUID primary keys (`UUID.randomUUID().toString()`) | `Column(String(36), default=lambda: str(uuid.uuid4()))` | Same string-UUID strategy |

## 3. Domain Model Translation

### Article
- Java: `Article` entity with `List<Tag>` (mapped via MyBatis XML).
- Python: `Article` model with `tags` relationship through `article_tags` association table.
- `toSlug()` regex ported to Python's `re.sub()` in `utils.py`.
- `update()` logic mirrors the Java version: only non-empty fields are updated.

### Tag
- Java: Separate `Tag` entity with `id` + `name`.
- Python: Same structure; `get_or_create` pattern used to avoid duplicate tags.

### User
- Java: `User` entity with `id`, `email`, `username`, `password`, `bio`, `image`.
- Python: Identical columns. `following` relationship uses an association table (`user_follows`).

### ArticleFavorite
- Java: Separate `ArticleFavorite` entity.
- Python: Modeled as a many-to-many via `article_favorites` association table between `Article` and `User`.

## 4. Authentication & Authorization

| Java | Python | Notes |
|-|-|-|
| `JwtTokenFilter` + Spring Security | `get_current_user_required` / `get_current_user_optional` FastAPI dependencies | JWT validation replaced with simple token-based lookup for the translation; production would use `python-jose` or similar |
| `@AuthenticationPrincipal User user` | `Depends(get_current_user_…)` | Same nullable/non-nullable semantics preserved |
| `AuthorizationService.canWriteArticle(user, article)` | Inline `article.user_id != current_user.id` check | Equivalent owner-only guard |
| `NoAuthorizationException` → 403 | `HTTPException(status_code=403)` | Same HTTP status |
| `InvalidAuthenticationException` → 401 | `HTTPException(status_code=401)` | Same HTTP status |

## 5. JSON Response Shape

The Python API produces **identical JSON** to the Java version:

### Single article
```json
{
  "article": {
    "id": "…",
    "slug": "…",
    "title": "…",
    "description": "…",
    "body": "…",
    "favorited": false,
    "favoritesCount": 0,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z",
    "tagList": ["tag1", "tag2"],
    "author": {
      "username": "…",
      "bio": "…",
      "image": "…",
      "following": false
    }
  }
}
```

### Article list
```json
{
  "articles": [ … ],
  "articlesCount": 42
}
```

Key decisions:
- **camelCase field names** are preserved via Pydantic `Field(alias=…)` and model attribute names that already match Java's `@JsonProperty` output.
- **`ProfileData.id`** is excluded from author (Java uses `@JsonIgnore`).
- **`profileData`** is serialized as `"author"` (Java uses `@JsonProperty("author")`).

## 6. Error Handling

| Java | Python |
|-|-|
| `@Valid` + `FieldErrorResource` → 422 with `{"errors": {"field": ["message"]}}` | `HTTPException(422)` with same shape in `detail` |
| `ResourceNotFoundException` → 404 | `HTTPException(404)` |
| `NoAuthorizationException` → 403 | `HTTPException(403)` |

## 7. Pagination

- Java's `Page` class enforces `MAX_LIMIT = 100` and positive offsets.
- Python's `_clamp_limit()` / `_clamp_offset()` replicate the same constraints.
- Query parameters default to `offset=0`, `limit=20` in both versions.

## 8. Testing

- Java uses JUnit 5 + Mockito + RestAssured on MockMvc.
- Python uses pytest + FastAPI `TestClient` (backed by httpx) with a real in-memory SQLite database.
- Each Python test documents its Java counterpart in the docstring.
- Tests validate the **exact JSON shape** (field names, nesting, types) to ensure drop-in compatibility.

## 9. What Was Left Out

The following Java components were **not** ported because they are outside the
Articles API scope requested:

- GraphQL layer (`graphql/` package)
- Comments API (`CommentsApi`)
- User registration / login (`UsersApi`, `CurrentUserApi`)
- Profile API (`ProfileApi`)
- Article Favorite API (`ArticleFavoriteApi`)
- Tags API (`TagsApi`)

These could be added incrementally following the same patterns established here.

## 10. Running the Application

```bash
cd python_fastapi
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 11. Running Tests

```bash
cd python_fastapi
pytest tests/ -v
```
