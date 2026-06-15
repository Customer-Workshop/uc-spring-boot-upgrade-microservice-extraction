# Article Service Extraction Decisions

## Overview

This document captures the decisions made while extracting the **Article** bounded context from the RealWorld monolith into a standalone microservice (`article-service/`).

---

## Bounded Contexts Identified

| Context              | Domain Objects                          | Tables                                         |
|----------------------|-----------------------------------------|------------------------------------------------|
| **Articles / Tags**  | `Article`, `Tag`, `ArticleRepository`   | `articles`, `tags`, `article_tags`             |
| **Comments**         | `Comment`, `CommentRepository`          | `comments`                                     |
| **Favorites**        | `ArticleFavorite`, `ArticleFavoriteRepository` | `article_favorites`                     |
| **Users / Profiles** | `User`, `FollowRelation`, `UserRepository` | `users`, `follows`                          |

---

## What Moves to article-service

### Domain Objects
- `Article` — core aggregate root (owns slug, title, description, body, userId, timestamps)
- `Tag` — value object associated with articles via `article_tags` join table
- `ArticleRepository` — write-side persistence interface

### Application Layer
- `ArticleCommandService` — create/update article commands
- `ArticleQueryService` — read-side queries (list, feed, find by slug/id)
- `TagsQueryService` — list all tags
- `DuplicatedArticleValidator` / `DuplicatedArticleConstraint` — slug uniqueness validation
- `NewArticleParam`, `UpdateArticleParam` — command DTOs
- `ArticleData`, `ArticleDataList`, `ArticleFavoriteCount`, `ProfileData` — read DTOs
- Pagination utilities: `Page`, `CursorPageParameter`, `CursorPager`, `Node`, `PageCursor`, `DateTimeCursor`

### Infrastructure
- `MyBatisArticleRepository` — write-side MyBatis implementation
- `ArticleMapper` — MyBatis mapper interface for articles/tags
- `ArticleReadService` — MyBatis read-side mapper for article queries
- `TagReadService` — MyBatis read-side mapper for tags
- MyBatis XML mappers: `ArticleMapper.xml`, `ArticleReadService.xml`, `TagReadService.xml`, `TransferData.xml`
- `DateTimeHandler` — Joda DateTime ↔ JDBC type handler

### API Layer
- `ArticlesApi` — POST /articles, GET /articles, GET /articles/feed
- `ArticleApi` — GET/PUT/DELETE /articles/{slug}
- `TagsApi` — GET /tags
- `AuthorizationService` — article-level authorization checks

### Database Tables
- `articles` — article content and metadata
- `tags` — tag names
- `article_tags` — many-to-many join table

---

## What Stays in the Monolith

| Component                    | Reason                                               |
|------------------------------|------------------------------------------------------|
| `Comment`, `CommentRepository` | Separate bounded context; references articleId by ID only |
| `ArticleFavorite`, `ArticleFavoriteRepository` | Separate bounded context; cross-cuts articles and users |
| `User`, `UserRepository`, `FollowRelation` | Core identity context; owned by monolith |
| `ProfileApi`, `ProfileQueryService` | User/profile domain |
| `UsersApi`, `CurrentUserApi`, `UserService` | User registration/authentication |
| `CommentsApi`, `CommentQueryService` | Comment domain |
| `ArticleFavoriteApi` | Favorites domain |
| JWT / Security infrastructure | Authentication stays centralized in the monolith |

---

## Coupling Points

### 1. Article → User (author)
- **Monolith**: `Article.userId` is a foreign key; `ArticleReadService.xml` JOINs `users` table to populate `ProfileData` (author username, bio, image).
- **article-service**: Cannot JOIN the `users` table. Replaced with a `UserServiceClient` REST client that calls `GET /api/profiles/{userId}` on the monolith to fetch `ProfileData`.
- **Fallback**: If the monolith is unreachable, the client returns a default `ProfileData` with the userId as username.

### 2. ArticleQueryService → ArticleFavoritesReadService
- **Monolith**: `ArticleQueryService.fillExtraInfo()` calls `ArticleFavoritesReadService` to populate `favorited` and `favoritesCount` on `ArticleData`.
- **article-service**: Favorites data is not available locally. The `ArticleQueryService` in article-service sets `favorited=false` and `favoritesCount=0` as defaults. The monolith (or an API gateway) can enrich these fields.

### 3. ArticleQueryService → UserRelationshipQueryService
- **Monolith**: `fillExtraInfo()` calls `UserRelationshipQueryService` to set `following` on the author's `ProfileData`.
- **article-service**: The `UserServiceClient` can optionally resolve follow status, but by default `following=false` is returned. The monolith can enrich this.

### 4. Article feed → Follow relations
- **Monolith**: `findUserFeed()` queries `UserRelationshipQueryService.followedUsers()` then fetches articles by those authors.
- **article-service**: The feed endpoint accepts author IDs as query parameters, delegating follow-resolution to the caller (monolith or gateway).

### 5. Article listing → Favorites filtering
- **Monolith**: `queryArticles` with `favoritedBy` parameter JOINs `article_favorites` and `users` tables.
- **article-service**: The `favoritedBy` filter is not supported locally since `article_favorites` stays in the monolith. Queries with this parameter return empty results; the monolith handles favorite-based filtering.

### 6. AuthorizationService
- **Monolith**: `AuthorizationService.canWriteArticle(user, article)` compares `user.getId()` with `article.getUserId()`.
- **article-service**: The same check is performed, but `User` is replaced with a `userId` string extracted from the JWT. A lightweight `AuthorizationService` compares userId strings.

---

## Cross-Service Communication Strategy

### Protocol
- Synchronous REST over HTTP (JSON)
- The article-service calls the monolith's internal API to resolve user profiles

### Endpoints Consumed (monolith → article-service)
- The monolith does **not** call article-service in this phase; articles are extracted but the monolith retains read access to article data through its own database for backward compatibility

### Endpoints Consumed (article-service → monolith)
| Endpoint                          | Purpose                                  |
|-----------------------------------|------------------------------------------|
| `GET {monolith}/profiles/{userId}` | Resolve author profile for article responses |

### New Internal Endpoint Added to Monolith
- `GET /api/internal/profiles/{userId}` — returns `ProfileData` by user ID (not username)

### Authentication
- article-service validates JWTs independently using the same shared secret
- Cross-service calls use an internal header or shared secret (configurable)

### Failure Handling
- REST client uses `RestTemplate` with connection/read timeouts
- On failure: log warning, return default `ProfileData` with userId as username
- No circuit breaker in initial extraction (can be added later)

### Data Consistency
- Article and tag data is fully owned by article-service
- Favorite counts and follow status are eventually consistent (enriched by caller)
- No distributed transactions required — each service owns its data

---

## Database Strategy

- article-service uses its own SQLite database file (`article-dev.db`)
- Flyway migrations contain only `articles`, `tags`, and `article_tags` tables
- Seed data is copied for articles and tags only
- No shared database between services
