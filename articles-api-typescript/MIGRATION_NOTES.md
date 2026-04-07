# Migration Notes: Java/Spring Boot → TypeScript/Express

This document captures the key translation decisions made when porting the Articles API from the Java/Spring Boot monolith to TypeScript/Express.

## Tech Stack Mapping

| Java/Spring Boot | TypeScript/Express |
|---|---|
| Spring Boot + Spring MVC | Express.js |
| MyBatis (SQL mapper) | TypeORM (ORM with SQLite) |
| Joda-Time `DateTime` | Native `Date` (ISO 8601 strings) |
| `javax.validation` | Manual validation in route handlers |
| Spring Security + JWT | Stub auth middleware (returns first DB user) |
| H2 / MySQL | SQLite via `better-sqlite3` |
| Lombok (`@Getter`, `@Data`, etc.) | TypeScript class properties |
| `UUID.randomUUID()` | `uuid` package (`v4`) |

## Endpoint Mapping

All endpoints are mounted under `/api/articles` to match the Java version:

| Method | Path | Java Class | Notes |
|---|---|---|---|
| `GET` | `/api/articles` | `ArticlesApi.getArticles()` | Same query params: `tag`, `author`, `favorited`, `offset`, `limit` |
| `GET` | `/api/articles/feed` | `ArticlesApi.getFeed()` | Requires auth |
| `POST` | `/api/articles` | `ArticlesApi.createArticle()` | Requires auth |
| `GET` | `/api/articles/:slug` | `ArticleApi.article()` | Public |
| `PUT` | `/api/articles/:slug` | `ArticleApi.updateArticle()` | Requires auth + author check |
| `DELETE` | `/api/articles/:slug` | `ArticleApi.deleteArticle()` | Requires auth + author check, returns 204 |

## Key Translation Decisions

### 1. Authentication Stub

The Java version uses JWT-based authentication via `JwtTokenFilter` and Spring Security's `@AuthenticationPrincipal`. The TypeScript version uses a **stub middleware** that returns the first user from the database (ordered alphabetically by username). This simplifies testing while preserving the same request flow.

**Decision:** Auth is stubbed out intentionally. In production, this would be replaced with JWT verification middleware that mirrors the Java `DefaultJwtService`.

### 2. Slug Generation

The Java `Article.toSlug()` method uses a regex:
```java
title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\'|\\"|\\s\\?\\,\\.]+", "-")
```

The TypeScript version replicates this regex pattern:
```typescript
title.toLowerCase().replace(/[&|\uFE30-\uFFA0'"\s?,.\[\]]+/g, "-")
```

### 3. Tag Deduplication

The Java version deduplicates tags using `new HashSet<>(tagList)` in the `Article` constructor. The TypeScript version uses `[...new Set(tagList)]` for the same effect. Additionally, the service layer checks for existing tags by name to avoid duplicates in the database.

### 4. `tagList` Sorting

The Java `ArticleData.tagList` is a `List<String>` populated by MyBatis queries. In the TypeScript version, `tagList` is explicitly sorted alphabetically when building the response to match the Java output behavior.

### 5. `ProfileData.id` Exclusion

The Java `ProfileData` class uses `@JsonIgnore` on the `id` field. The TypeScript `ProfileResponse` interface simply omits the `id` field entirely, achieving the same result — the author's internal ID is never exposed in API responses.

### 6. Date Format

The Java version uses Joda-Time `DateTime` serialized to ISO 8601 via Jackson. The TypeScript version uses native `Date` objects serialized with `.toISOString()`, producing the same format: `"2024-01-01T00:00:00.000Z"`.

### 7. Update Behavior (Non-Empty Fields Only)

The Java `Article.update()` method uses `Util.isEmpty()` (checks for `null` or empty string) to conditionally update fields. The TypeScript version mirrors this: only non-null, non-empty strings trigger updates. The `updatedAt` timestamp is refreshed independently for each changed field, matching the Java behavior.

### 8. Authorization Check

The Java `AuthorizationService.canWriteArticle()` checks `user.getId().equals(article.getUserId())`. The TypeScript version performs the same check: `article.userId !== currentUser.id`. Unauthorized access returns 403 (Java throws `NoAuthorizationException`).

### 9. Database Choice

The Java version uses MyBatis with configurable databases (H2 for tests, MySQL for production). The TypeScript version uses TypeORM with SQLite (`better-sqlite3`), which provides zero-configuration persistence suitable for development and testing. For production, the TypeORM data source configuration can be swapped to PostgreSQL or MySQL.

### 10. Error Handling

The Java version uses custom exception classes (`ResourceNotFoundException`, `NoAuthorizationException`, `InvalidRequestException`) handled by `CustomizeExceptionHandler`. The TypeScript version uses inline HTTP status codes:
- `404` for resource not found
- `403` for authorization failures
- `422` for validation errors
- `401` for missing authentication
- `500` for unexpected errors

### 11. Pagination

The Java `Page` class caps `limit` at 100 and floors `offset` at 0. The TypeScript version uses `parseInt()` with fallback defaults (`offset=0`, `limit=20`) matching the Java `@RequestParam` defaults. The MAX_LIMIT cap of 100 from the Java version is preserved in the query behavior via TypeORM's `take` parameter.

### 12. Response Shape Fidelity

The JSON response shapes are verified to be identical to the Java version:
- Single article: `{ "article": { ... } }`
- Article list: `{ "articles": [ ... ], "articlesCount": N }`
- Delete: `204 No Content` with empty body

All field names match exactly (`favoritesCount`, `tagList`, `createdAt`, etc.).

## Files Structure

```
articles-api-typescript/
├── src/
│   ├── entities/          # TypeORM entities (Article, Tag, User, FollowRelation, ArticleFavorite)
│   ├── middleware/         # Auth stub middleware
│   ├── routes/            # Express route handlers
│   ├── services/          # Business logic (ArticleService)
│   ├── types/             # TypeScript interfaces for request/response models
│   ├── app.ts             # Express app factory
│   ├── database.ts        # TypeORM DataSource configuration
│   └── index.ts           # Server entry point
├── __tests__/             # Jest test suite
├── MIGRATION_NOTES.md     # This file
├── package.json
├── tsconfig.json
└── jest.config.js
```
