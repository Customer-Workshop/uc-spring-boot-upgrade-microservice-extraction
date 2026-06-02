# Article Service Extraction Decisions

## Overview

This document records the decisions made for extracting an **Article Service** from the RealWorld Spring Boot monolith. It covers which domain objects move, which stay, where the two services remain coupled, and how they will communicate across the service boundary.

---

## 1. What Moves to the Article Service

Four tightly-coupled sub-domains move together. They share data through foreign keys, co-appear in the same API responses, and are queried as a unit.

### 1.1 Articles & Tags

| Asset | Current Location |
|-------|-----------------|
| `Article` entity | `io.spring.core.article.Article` |
| `Tag` entity | `io.spring.core.article.Tag` |
| `ArticleRepository` | `io.spring.core.article.ArticleRepository` |
| `ArticleCommandService` | `io.spring.application.article.ArticleCommandService` |
| `ArticleQueryService` | `io.spring.application.ArticleQueryService` |
| `ArticleData`, `ArticleDataList`, `ArticleFavoriteCount` DTOs | `io.spring.application.data.*` |
| `ArticleApi`, `ArticlesApi`, `TagsApi` | `io.spring.api.*` |
| `ArticleDatafetcher`, `ArticleMutation`, `TagDatafetcher` | `io.spring.graphql.*` |
| `ArticleMapper`, `ArticleReadService`, `TagReadService` | `io.spring.infrastructure.mybatis.*` |
| `MyBatisArticleRepository` | `io.spring.infrastructure.repository.*` |
| DB tables | `articles`, `tags`, `article_tags` |

**Rationale**: Articles are the central aggregate of the content domain. Tags exist exclusively as article metadata (the `article_tags` join table and the `Tag` entity have no relationship to users or profiles).

### 1.2 Comments

| Asset | Current Location |
|-------|-----------------|
| `Comment` entity | `io.spring.core.comment.Comment` |
| `CommentRepository` | `io.spring.core.comment.CommentRepository` |
| `CommentQueryService` | `io.spring.application.CommentQueryService` |
| `CommentData` DTO | `io.spring.application.data.CommentData` |
| `CommentsApi` | `io.spring.api.CommentsApi` |
| `CommentDatafetcher`, `CommentMutation` | `io.spring.graphql.*` |
| `CommentMapper`, `CommentReadService` | `io.spring.infrastructure.mybatis.*` |
| `MyBatisCommentRepository` | `io.spring.infrastructure.repository.*` |
| DB table | `comments` |

**Rationale**: Comments are children of articles (`comments.article_id` FK). The `CommentsApi` is mounted under `/articles/{slug}/comments` and requires `ArticleRepository.findBySlug()` to resolve the parent. Keeping comments with articles avoids a synchronous cross-service call on every comment operation.

### 1.3 Favorites

| Asset | Current Location |
|-------|-----------------|
| `ArticleFavorite` entity | `io.spring.core.favorite.ArticleFavorite` |
| `ArticleFavoriteRepository` | `io.spring.core.favorite.ArticleFavoriteRepository` |
| `ArticleFavoriteApi` | `io.spring.api.ArticleFavoriteApi` |
| `ArticleFavoriteMapper`, `ArticleFavoritesReadService` | `io.spring.infrastructure.mybatis.*` |
| `MyBatisArticleFavoriteRepository` | `io.spring.infrastructure.repository.*` |
| DB table | `article_favorites` |

**Rationale**: Favorites are a join between `articleId` and `userId`. The favorite count and "is favorited" flag are computed on the read side inside `ArticleQueryService.fillExtraInfo()` and embedded directly into every `ArticleData` response. Moving favorites with articles keeps this hot-path query local — splitting them would add a synchronous cross-service call to every article list/detail request.

---

## 2. What Stays in the Monolith (User Service)

### 2.1 Users & Profiles

| Asset | Current Location |
|-------|-----------------|
| `User` entity | `io.spring.core.user.User` |
| `FollowRelation` entity | `io.spring.core.user.FollowRelation` |
| `UserRepository` | `io.spring.core.user.UserRepository` |
| `UserService`, `ProfileQueryService` | `io.spring.application.*` |
| `UserData`, `UserWithToken`, `ProfileData` DTOs | `io.spring.application.data.*` |
| `UsersApi`, `CurrentUserApi`, `ProfileApi` | `io.spring.api.*` |
| `UserMutation`, `ProfileDatafetcher`, `RelationMutation`, `MeDatafetcher` | `io.spring.graphql.*` |
| `UserMapper`, `UserReadService`, `UserRelationshipQueryService` | `io.spring.infrastructure.mybatis.*` |
| `MyBatisUserRepository` | `io.spring.infrastructure.repository.*` |
| `JwtTokenFilter`, `WebSecurityConfig`, `JwtService`, `DefaultJwtService` | `io.spring.api.security`, `io.spring.infrastructure.service` |
| DB tables | `users`, `follows` |

**Rationale**: Users own authentication (JWT issuance and validation), profile data, and follow relationships. These capabilities are consumed by every service and are the natural identity provider. Keeping them in the monolith avoids migrating auth infrastructure and lets the monolith continue serving the `/users`, `/user`, and `/profiles` endpoints unchanged.

---

## 3. Coupling Points Between Domains

The article domain and user domain are coupled in the following ways. Each coupling point must be addressed during extraction.

### 3.1 Data-level Coupling

| Table/Column | Direction | Description |
|-------------|-----------|-------------|
| `articles.user_id` | Article -> User | Every article stores its author's user ID. |
| `comments.user_id` | Article -> User | Every comment stores its author's user ID. |
| `article_favorites.user_id` | Article -> User | Every favorite links to a user ID. |

After extraction the Article service stores these as opaque string IDs with no foreign-key constraint to the `users` table (which lives in a separate database).

### 3.2 Domain-level Coupling

| Coupling | Location | Detail |
|----------|----------|--------|
| `AuthorizationService.canWriteArticle(User, Article)` | `io.spring.core.service` | Compares `user.getId()` with `article.getUserId()`. Moves to Article service; receives user ID only (not the full `User` entity). |
| `AuthorizationService.canWriteComment(User, Article, Comment)` | `io.spring.core.service` | Checks both article owner and comment owner. Same treatment as above. |
| `@AuthenticationPrincipal User` | All article/comment/favorite controllers | Controllers receive the full `User` domain object from Spring Security. After extraction, the Article service validates the JWT independently and extracts only the user ID from the token claims. |

### 3.3 Read-side / Query Coupling

| Coupling | Location | Detail |
|----------|----------|--------|
| `ProfileData` embedding | `ArticleData.profileData`, `CommentData.profileData` | Every article and comment response includes the author's profile (username, bio, image, following). Currently populated via SQL joins to the `users` table. |
| `ArticleReadService.xml` joins | MyBatis mapper XML | Article queries join `articles` -> `users` to populate `ProfileData`. |
| `CommentReadService.xml` joins | MyBatis mapper XML | Comment queries join `comments` -> `users` to populate `ProfileData`. |
| `UserRelationshipQueryService.isUserFollowing()` | `ArticleQueryService.fillExtraInfo()`, `CommentQueryService` | Used to set `profileData.following` on article and comment responses. |
| `UserRelationshipQueryService.followedUsers()` | `ArticleQueryService.findUserFeed()` | Returns the list of user IDs that the current user follows, used to build the personalized feed. |
| `ArticleFavoritesReadService.userFavorites()` | `ArticleQueryService.fillExtraInfo()` | Queries which articles a given user has favorited (user ID crosses the boundary). |
| Filter by `favoritedBy` / `author` | `ArticleReadService.queryArticles()` | Accepts username strings that are resolved against the `users` table. |

### 3.4 GraphQL Schema Coupling

The single `schema.graphqls` defines types for all domains. After extraction, the Article service owns `Article`, `Comment`, `Tag`, and `ArticleFavorite` types. The monolith retains `User` and `Profile` types. If both services continue to serve GraphQL, an Apollo-style federation gateway (or similar) can compose the schemas.

---

## 4. Cross-Service Communication Strategy

### 4.1 Synchronous: REST API Calls

The Article service needs user profile data to build `ArticleData` and `CommentData` responses. It will call the monolith (User service) over HTTP.

**Endpoint consumed by Article service**:

```
GET /api/internal/users/{userId}
Response: { "id", "username", "bio", "image" }
```

An internal endpoint (not the public `/profiles/{username}`) keyed by user ID avoids a two-step lookup. The `/profiles/{username}` endpoint remains for public use.

**Where this replaces SQL joins**:
- `ArticleReadService.xml` — remove the `LEFT JOIN users` and instead fetch profile data via REST after the article query returns.
- `CommentReadService.xml` — same treatment.

**Follow-status resolution**:

```
GET /api/internal/users/{userId}/follows?ids={id1,id2,...}
Response: { "following": ["id1", "id3"] }
```

Replaces `UserRelationshipQueryService.isUserFollowing()` and `followingAuthors()`. The Article service batches user IDs from a page of articles/comments into a single call.

**Feed (followed-users list)**:

```
GET /api/internal/users/{userId}/following
Response: { "userIds": ["uid1", "uid2", ...] }
```

Replaces `UserRelationshipQueryService.followedUsers()`. The Article service uses the returned list to query its own `articles` table.

### 4.2 Caching

Profile data changes infrequently. The Article service should cache user profiles with a short TTL (e.g., 30-60 seconds) to avoid calling the User service on every request. A local in-memory cache (Caffeine) is sufficient initially.

### 4.3 Authentication

Both services share the same JWT secret (or public key). The Article service validates tokens independently using its own `JwtTokenFilter` and extracts the user ID from claims. It does **not** call the User service to authenticate requests.

### 4.4 Authorization

`AuthorizationService` moves to the Article service. After extraction it compares the authenticated user ID (from the JWT) against `article.getUserId()` or `comment.getUserId()` directly — no `User` entity import needed.

### 4.5 Future: Asynchronous Events

Once the synchronous approach is stable, profile data synchronization can be moved to an event-driven model:

| Event | Publisher | Consumer | Effect |
|-------|-----------|----------|--------|
| `UserProfileUpdated` | User service | Article service | Invalidate/update cached profile data |
| `UserDeleted` | User service | Article service | Soft-delete or anonymize articles/comments |
| `ArticleCreated` | Article service | User service (optional) | Update activity feeds, analytics |

This eliminates the synchronous dependency for profile data on the read path and decouples deployments further.

---

## 5. Summary Table

| Domain Object | Moves to Article Service? | Reason |
|--------------|--------------------------|--------|
| `Article` | Yes | Core aggregate of the content domain |
| `Tag` | Yes | Owned exclusively by articles |
| `Comment` | Yes | Child of articles; routed under `/articles/{slug}/comments` |
| `ArticleFavorite` | Yes | Join entity; favorite counts are embedded in every article response |
| `User` | No | Identity provider, JWT issuer, shared across services |
| `FollowRelation` | No | Social graph owned by user domain |
| `ProfileData` | Shared (DTO) | Produced by User service, consumed by Article service via REST |
