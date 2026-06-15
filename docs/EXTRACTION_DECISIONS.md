# Article Service Extraction Decisions

## Overview

This document captures the architectural decisions made when extracting the **Article** bounded context from the monolith into a standalone microservice (`article-service/`).

## Bounded Contexts Identified

| Context | Domain Objects | Stays / Moves |
|---------|---------------|---------------|
| Articles & Tags | `Article`, `Tag`, `ArticleRepository` | **Moves** to article-service |
| Comments | `Comment`, `CommentRepository` | Stays in monolith |
| Favorites | `ArticleFavorite`, `ArticleFavoriteRepository` | Stays in monolith |
| Users & Profiles | `User`, `FollowRelation`, `UserRepository` | Stays in monolith |

## What Moves to article-service

### Domain Layer (`core/`)
- `Article` — aggregate root, owns slug generation and update logic
- `Tag` — value object associated with articles
- `ArticleRepository` — write-side repository interface

### Application Layer (`application/`)
- `ArticleCommandService` — create/update commands
- `ArticleQueryService` — read-side queries (adapted to use REST client for user profiles)
- `TagsQueryService` — list all tags
- `NewArticleParam`, `UpdateArticleParam` — command DTOs
- `DuplicatedArticleConstraint` / `DuplicatedArticleValidator` — unique-title validation
- `ArticleData`, `ArticleDataList`, `ProfileData` — read-side DTOs
- `Page`, `CursorPager`, `CursorPageParameter`, `DateTimeCursor`, `Node`, `PageCursor` — pagination utilities

### API Layer (`api/`)
- `ArticleApi` — single-article CRUD (`/articles/{slug}`)
- `ArticlesApi` — list/create/feed (`/articles`, `/articles/feed`)
- `TagsApi` — tag listing (`/tags`)

### Infrastructure Layer (`infrastructure/`)
- `MyBatisArticleRepository` — write-side MyBatis implementation
- `ArticleMapper` — MyBatis mapper interface + XML
- `ArticleReadService` — read-side mapper (rewritten to not JOIN users)
- `TagReadService` — tag read mapper
- `DateTimeHandler` — Joda DateTime ↔ JDBC type handler
- `DefaultJwtService` — JWT token parsing (shared secret with monolith)

## What Stays in the Monolith

- **Users/Profiles**: `User`, `UserRepository`, `UserReadService`, `UserRelationshipQueryService`, `ProfileQueryService`, `UsersApi`, `ProfileApi`, `CurrentUserApi`
- **Comments**: `Comment`, `CommentRepository`, `CommentMapper`, `CommentReadService`, `CommentQueryService`, `CommentsApi`
- **Favorites**: `ArticleFavorite`, `ArticleFavoriteRepository`, `ArticleFavoriteMapper`, `ArticleFavoritesReadService`, `ArticleFavoriteApi`
- **Security config** (WebSecurityConfig, JwtTokenFilter) — duplicated in article-service with its own rules

## Coupling Points

| Coupling | Location | Resolution |
|----------|----------|------------|
| `Article.userId` stores a user ID FK | `Article` entity | Retain as opaque string; no FK constraint in article DB |
| `ArticleReadService.xml` JOINs `users` table for author profile | SQL mapper | Rewrite query to select only article/tag columns; fetch profiles via REST |
| `ArticleQueryService` calls `UserRelationshipQueryService` | Application service | Replace with `UserServiceClient.getProfile()` call |
| `ArticleQueryService` calls `ArticleFavoritesReadService` | Application service | Remove; article-service returns `favorited=false`, `favoritesCount=0` (favorites are a separate bounded context) |
| `AuthorizationService.canWriteArticle(User, Article)` | Core service | Simplified to compare `userId` strings directly (no User entity dependency) |
| `@AuthenticationPrincipal User` in controllers | API layer | Article-service defines a minimal `User` resolved from JWT (id only); no DB lookup required |
| `ArticleCommandService.createArticle(param, User)` | Application service | Changed to accept `userId` string instead of full User entity |

## Cross-Service Communication Strategy

### Pattern: Synchronous REST Client

The article-service calls the monolith's REST API to fetch user profile data when building article responses.

```
article-service                          monolith
     │                                      │
     │  GET /profiles/{username}            │
     │  (or internal /api/users/{id})       │
     │─────────────────────────────────────►│
     │                                      │
     │  { "profile": { username, bio,       │
     │    image, following } }              │
     │◄─────────────────────────────────────│
```

### Implementation

- **`UserServiceClient`** — Spring `RestTemplate`-based client in `infrastructure/client/`
- **`UserProfileDto`** — DTO representing the profile response from the monolith
- **Failure handling** — Returns a default profile (unknown user) on timeout/error; logs warning
- **Base URL** — Configurable via `user-service.url` property (defaults to `http://localhost:8080`)

### Design Trade-offs

1. **No shared database** — Article-service has its own SQLite file with only articles/tags/article_tags tables
2. **Favorites removed from article read model** — Since favorites are a separate bounded context, the article-service always returns `favorited=false` and `favoritesCount=0`. Clients needing favorite data query the monolith directly.
3. **Follow status removed** — `profileData.following` always returns `false` from article-service. The monolith remains the authority for social relationships.
4. **JWT shared secret** — Both services share the same JWT signing secret, allowing tokens issued by the monolith to be validated by the article-service without an additional auth call.

## Database Schema (article-service)

```sql
-- articles table (owned)
CREATE TABLE articles (
  id VARCHAR(255) PRIMARY KEY,
  user_id VARCHAR(255),
  slug VARCHAR(255) UNIQUE,
  title VARCHAR(255),
  description TEXT,
  body TEXT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- tags table (owned)
CREATE TABLE tags (
  id VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

-- junction table (owned)
CREATE TABLE article_tags (
  article_id VARCHAR(255) NOT NULL,
  tag_id VARCHAR(255) NOT NULL
);
```

## Port Allocation

| Service | Port |
|---------|------|
| Monolith | 8080 |
| article-service | 8081 |
