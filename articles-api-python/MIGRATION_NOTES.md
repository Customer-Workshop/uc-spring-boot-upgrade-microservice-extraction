# Migration Notes: Java/Spring Boot → Python/FastAPI (Articles API)

This document records the key translation decisions made while porting
the Articles API from the `uc-framework-upgrade-monolith-to-microservices`
Java/Spring Boot monolith to a standalone Python/FastAPI application.

---

## 1. Project Layout

| Java (monolith)             | Python (FastAPI)                |
|-----------------------------|---------------------------------|
| `io.spring.api.*`           | `app/routes.py`                 |
| `io.spring.core.article.*`  | `app/models.py`                 |
| `io.spring.core.user.*`     | `app/models.py`                 |
| `io.spring.application.data.*` | `app/schemas.py`             |
| `io.spring.application.article.*` | `app/schemas.py` (request models) |
| `io.spring.infrastructure.mybatis.*` | SQLAlchemy (built-in)    |

The Java monolith uses a layered hexagonal architecture with separate
`core`, `application`, and `infrastructure` packages.  The Python
version consolidates these into three focused modules (`models`,
`schemas`, `routes`) because FastAPI's dependency-injection and
Pydantic validation make the extra layering unnecessary for a single
bounded context.

## 2. ORM — MyBatis → SQLAlchemy

| Aspect         | Java                  | Python                      |
|----------------|-----------------------|-----------------------------|
| ORM            | MyBatis (XML mappers) | SQLAlchemy 2.0 (mapped_column) |
| DB             | H2 (in-memory)        | SQLite (file / in-memory for tests) |
| Migrations     | Schema via MyBatis DDL | `Base.metadata.create_all()` auto-DDL |

- MyBatis uses hand-written SQL in XML mapper files.  SQLAlchemy's
  declarative ORM generates SQL from Python class definitions, which
  is more idiomatic in the Python ecosystem.
- Association tables (`article_tags`, `article_favorites`,
  `user_follows`) use SQLAlchemy `Table` objects, matching the
  join-table pattern in the MyBatis mappers.

## 3. Domain Model Mapping

### Article

| Java field  | Python column     | Notes |
|-------------|-------------------|-------|
| `id` (UUID string) | `id` (String(36)) | Generated via `uuid.uuid4()` |
| `slug`      | `slug`            | Derived from title via `_to_slug()` |
| `title`     | `title`           | |
| `description` | `description`   | |
| `body`      | `body`            | |
| `userId`    | `user_id`         | FK → `users.id` |
| `tags` (List\<Tag\>) | `tags` (M2M relationship) | via `article_tags` table |
| `createdAt` (Joda DateTime) | `created_at` (datetime UTC) | See §5 |
| `updatedAt` (Joda DateTime) | `updated_at` (datetime UTC) | |

### User

| Java field | Python column | Notes |
|------------|---------------|-------|
| `id`       | `id`          | UUID string |
| `email`    | `email`       | unique |
| `username` | `username`    | unique |
| `password` | `password`    | stored as-is (hashing handled at service layer) |
| `bio`      | `bio`         | nullable |
| `image`    | `image`       | nullable |

### Tag

Simple `id` + `name`.  Tags are deduplicated on insert — if a tag
name already exists, the existing row is reused (mirrors the Java
`HashSet<>(tagList)` deduplication).

## 4. JSON Response Shapes

The Python API preserves the **exact same JSON keys and nesting** as
the Java version so it can act as a drop-in replacement.

| Java DTO / annotation         | Python Pydantic model         | JSON key mapping |
|-------------------------------|-------------------------------|------------------|
| `ArticleData`                 | `ArticleData`                 | camelCase aliases (`favoritesCount`, `createdAt`, `tagList`) |
| `ArticleData.profileData` (`@JsonProperty("author")`) | `ArticleData.author` | key = `"author"` |
| `ProfileData.id` (`@JsonIgnore`) | excluded from schema         | not in JSON |
| `ArticleDataList.articleDatas` (`@JsonProperty("articles")`) | response dict `"articles"` | |
| `ArticleDataList.count` (`@JsonProperty("articlesCount")`) | response dict `"articlesCount"` | |

Single-article endpoints wrap the object in `{"article": {...}}`,
matching the Java `articleResponse()` helper.

## 5. Date/Time Handling

| Java             | Python                |
|------------------|-----------------------|
| Joda `DateTime`  | `datetime` (stdlib, UTC) |
| ISO-8601 via Jackson Joda module | ISO-8601 via `isoformat()` |

Joda-Time is deprecated in modern Java but was used in the original
codebase.  Python's `datetime` with `timezone.utc` produces the same
ISO-8601 strings.

## 6. Slug Generation

The Java `Article.toSlug()` method uses a regex to replace special
characters with hyphens:

```java
title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\'|\\"|\\s\\?\\,\\.]+", "-")
```

The Python `_to_slug()` function replicates this behaviour.  For
production use, the `python-slugify` library is also included as a
dependency for richer Unicode handling if needed.

## 7. Authentication

The Java app uses Spring Security with a JWT filter
(`JwtTokenFilter`) that resolves the current `User` from the
`Authorization: Token <jwt>` header.

The Python version provides a **pluggable stub** (`_get_current_user`)
that returns the first user in the database.  To add real JWT
authentication:

1. Implement a FastAPI `Depends` callable that decodes the JWT.
2. Replace `_get_current_user` with that dependency in each route.
3. The `python-jose` and `passlib` packages are already included
   in `requirements.txt` for this purpose.

This decision keeps the API surface and response shapes testable
without needing a full security stack, while the Java
`@AuthenticationPrincipal` annotation is documented as the pattern
to follow.

## 8. Validation

| Java                   | Python                        |
|------------------------|-------------------------------|
| `@NotBlank`            | Pydantic `str` (required)     |
| `@Valid @RequestBody`  | FastAPI auto-validates Pydantic models |
| `@JsonRootName("article")` | Wrapper model (`NewArticleWrapper`) |
| `@DuplicatedArticleConstraint` | Not ported (can add via custom validator) |

The Java `@JsonRootName` causes Jackson to expect/produce a root
wrapper key (e.g. `{"article": {...}}`).  In Pydantic we model this
explicitly with a wrapper class.

## 9. Error Responses

| Java exception                | Python / FastAPI               |
|-------------------------------|--------------------------------|
| `ResourceNotFoundException`   | `HTTPException(404)`           |
| `NoAuthorizationException`    | `HTTPException(403)`           |
| `InvalidRequestException`     | FastAPI 422 (automatic)        |

FastAPI's default 422 validation-error format differs slightly from
the Java `ErrorResource` serialiser.  For full parity a custom
exception handler could be added.

## 10. Testing Strategy

Tests use `pytest` + `httpx` (via `TestClient`) with an **in-memory
SQLite** database.  Each test function gets a fresh schema via
`create_all` / `drop_all`.

Tests verify:
- Correct HTTP status codes for each endpoint
- JSON response keys match the Java DTOs **exactly**
- Filtering (by tag, author, favorited) works as expected
- Offset/limit pagination
- Feed returns only followed authors' articles
- `author.id` is excluded from JSON (matches `@JsonIgnore`)
- `tagList` is sorted alphabetically
- Timestamps are ISO-8601 formatted

## 11. Dependencies

| Purpose          | Package            | Java equivalent            |
|------------------|--------------------|----------------------------|
| Web framework    | FastAPI            | Spring Boot Web            |
| ASGI server      | Uvicorn            | Embedded Tomcat            |
| ORM              | SQLAlchemy 2.0     | MyBatis                    |
| Validation       | Pydantic 2.x       | Hibernate Validator        |
| JWT (ready)      | python-jose        | io.jsonwebtoken (jjwt)     |
| Password hashing | passlib            | Spring PasswordEncoder     |
| Slugify          | python-slugify     | custom `toSlug()` method   |
| Testing          | pytest, httpx      | JUnit, Spring MockMvc      |

## 12. What Was NOT Ported

The following parts of the Java monolith are **outside the scope**
of the Articles API translation:

- GraphQL data-fetchers (`io.spring.graphql.*`)
- Comments API (`CommentsApi`)
- Users / authentication API (`UsersApi`, `CurrentUserApi`)
- Profile API (`ProfileApi`)
- Favorites API (`ArticleFavoriteApi`)
- Tags API (`TagsApi`)
- Cursor-based pagination (`CursorPager`)

These could be added as separate FastAPI routers in the future.
