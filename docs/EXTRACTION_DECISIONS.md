# Article Service Extraction Decisions

## Bounded Contexts Identified

| Context | Domain Objects | Tables |
|---|---|---|
| **Articles / Tags** | `Article`, `Tag`, `ArticleRepository` | `articles`, `tags`, `article_tags` |
| **Comments** | `Comment`, `CommentRepository` | `comments` |
| **Favorites** | `ArticleFavorite`, `ArticleFavoriteRepository` | `article_favorites` |
| **Users / Profiles** | `User`, `FollowRelation`, `UserRepository` | `users`, `follows` |

## What Moves to `article-service`

### Domain Objects
- `Article`, `Tag` (`core.article`)
- `Comment` (`core.comment`)
- `ArticleFavorite` (`core.favorite`)
- `AuthorizationService` (`core.service`) — article/comment write-permission checks

### API Layer
- `ArticleApi`, `ArticlesApi` — single-article CRUD and list/feed endpoints
- `CommentsApi` — comment CRUD scoped to articles
- `ArticleFavoriteApi` — favorite/unfavorite
- `TagsApi` — tag listing

### Application / Query Layer
- `ArticleQueryService`, `CommentQueryService`, `TagsQueryService`
- `ArticleCommandService` and related params/validators
- DTOs: `ArticleData`, `ArticleDataList`, `ArticleFavoriteCount`, `CommentData`, `ProfileData`
- Pagination: `Page`, `CursorPager`, `CursorPageParameter`, `Node`, `PageCursor`, `DateTimeCursor`

### Infrastructure
- MyBatis mappers: `ArticleMapper`, `CommentMapper`, `ArticleFavoriteMapper`
- MyBatis read services: `ArticleReadService`, `CommentReadService`, `ArticleFavoritesReadService`, `TagReadService`
- Repositories: `MyBatisArticleRepository`, `MyBatisCommentRepository`, `MyBatisArticleFavoriteRepository`
- XML mappers: `ArticleMapper.xml`, `CommentMapper.xml`, `ArticleFavoriteMapper.xml`, `ArticleReadService.xml`, `CommentReadService.xml`, `ArticleFavoritesReadService.xml`, `TagReadService.xml`, `TransferData.xml`

### Shared Utilities (copied to new service)
- `Util` (string-empty check)
- `JacksonCustomizations` (Joda DateTime serializer)
- `MyBatisConfig` (transaction management)
- `DateTimeHandler` (MyBatis type handler for Joda DateTime)

## What Stays in the Monolith

- `User`, `FollowRelation`, `UserRepository` and all user persistence
- `UsersApi`, `CurrentUserApi`, `ProfileApi`
- `UserService`, `UserQueryService`, `ProfileQueryService`
- `JwtService` / `DefaultJwtService` — JWT token creation and validation
- `JwtTokenFilter`, `WebSecurityConfig` — authentication filter chain
- User-related validators: `DuplicatedEmailValidator`, `DuplicatedUsernameValidator`
- GraphQL layer (`graphql/` package) — remains in monolith

## Coupling Points Between Domains

### 1. User Identity in Article Domain (userId as FK)
- `Article.userId`, `Comment.userId`, `ArticleFavorite.userId` reference User by ID string.
- **Decision:** Keep userId as an opaque string. No `User` entity import needed in article-service.

### 2. Author Profile in Read Models (ArticleData, CommentData)
- `ArticleData.profileData` and `CommentData.profileData` include author's `ProfileData` (username, bio, image, following).
- In the monolith, this is resolved via SQL JOINs against the `users` table.
- **Decision:** The article-service calls the monolith's user API via `UserServiceClient` to resolve profile data. The `ArticleReadService.xml` and `CommentReadService.xml` queries are rewritten to exclude the `users` JOIN — profile enrichment happens at the application layer.

### 3. Follow Relationship Checks
- `ArticleQueryService.fillExtraInfo()` and `CommentQueryService` call `UserRelationshipQueryService` to check if current user follows the author.
- **Decision:** `UserServiceClient` exposes a method to check follow status. The article-service calls this instead of querying the `follows` table directly.

### 4. Feed (articles from followed users)
- `ArticleQueryService.findUserFeed()` calls `UserRelationshipQueryService.followedUsers()`.
- **Decision:** `UserServiceClient.getFollowedUserIds(userId)` fetches the list from the monolith. Article-service then queries its own `articles` table filtered by those author IDs.

### 5. JWT Authentication
- The monolith's `JwtTokenFilter` resolves a JWT to a `User` object from the `users` table.
- **Decision:** The article-service shares the same JWT secret and validates tokens independently, but resolves only the userId (sub claim) from the token — not the full `User` entity. API controllers receive userId as a string via a custom security principal.

### 6. Authorization Checks
- `AuthorizationService.canWriteArticle(user, article)` compares `user.getId()` with `article.getUserId()`.
- **Decision:** Copied to article-service. Only needs userId string comparison, no User entity dependency.

## Cross-Service Communication Strategy

### REST Client: `UserServiceClient`
Located in `io.spring.infrastructure.client`. Calls the monolith's REST API:

| Method | Monolith Endpoint | Purpose |
|---|---|---|
| `getProfile(userId)` | `GET /internal/users/{id}/profile` | Resolve ProfileData for article/comment author |
| `getProfiles(userIds)` | `POST /internal/users/profiles` | Batch-resolve profiles for list views |
| `isFollowing(userId, targetId)` | `GET /internal/users/{userId}/following/{targetId}` | Check follow status |
| `getFollowingAuthors(userId, authorIds)` | `POST /internal/users/{userId}/following-authors` | Batch follow check |
| `getFollowedUserIds(userId)` | `GET /internal/users/{userId}/followed` | Get followed user IDs for feed |

### DTOs
- `UserProfileResponse` — DTO for profile data from monolith (maps to `ProfileData`)
- Located in `io.spring.infrastructure.client.dto` in the article-service

### Error Handling
- REST client failures return sensible defaults (empty profile, not-following) rather than propagating HTTP errors.
- Timeouts: 5s connect, 10s read.

### Internal API Endpoints (added to monolith)
New `InternalUserApi` controller in the monolith exposes user data for service-to-service calls. These endpoints are not part of the public RealWorld API spec.

## Database Strategy

- Article-service uses its own SQLite database file (`article-dev.db`)
- Tables copied: `articles`, `tags`, `article_tags`, `comments`, `article_favorites`
- **No `users` or `follows` tables** in article-service — user data comes from the monolith via REST
- Flyway migrations: `V1__create_tables.sql` (article tables only), `V2__seed_data.sql` (article seed data only)

## Configuration

- Same Spring Boot version (2.6.3) and dependency versions as the monolith
- Article-service runs on port 8081 (monolith on 8080)
- Shared JWT secret for independent token validation
- `user-service.url` property configures the monolith base URL
