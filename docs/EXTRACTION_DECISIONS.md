# Microservice Extraction Decisions

## 1. Bounded Context Analysis

The monolith implements the RealWorld blogging platform with four identifiable bounded contexts:

### 1.1 User / Profile Context
**Entities:** `User`, `FollowRelation`
**Tables:** `users`, `follows`
**APIs:** `UsersApi` (register, login), `CurrentUserApi` (get/update), `ProfileApi` (get, follow, unfollow)
**Services:** `UserService`, `UserQueryService`, `ProfileQueryService`
**Read Services:** `UserReadService`, `UserRelationshipQueryService`

This context owns identity, authentication (JWT), and social relationships (follow/unfollow).

### 1.2 Article / Tag Context
**Entities:** `Article`, `Tag`
**Tables:** `articles`, `tags`, `article_tags`
**APIs:** `ArticlesApi` (create, list, feed), `ArticleApi` (get, update, delete), `TagsApi` (list)
**Services:** `ArticleCommandService`, `ArticleQueryService`, `TagsQueryService`
**Read Services:** `ArticleReadService`, `TagReadService`

This context owns article lifecycle (CRUD), slugs, and tag management.

### 1.3 Comment Context
**Entities:** `Comment`
**Tables:** `comments`
**APIs:** `CommentsApi` (create, list, delete for a given article)
**Services:** `CommentQueryService`
**Read Services:** `CommentReadService`

Comments are tightly coupled to articles (every comment references an `articleId`).

### 1.4 Favorite Context
**Entities:** `ArticleFavorite`
**Tables:** `article_favorites`
**APIs:** `ArticleFavoriteApi` (favorite/unfavorite an article)
**Services:** (inline in API controller)
**Read Services:** `ArticleFavoritesReadService`

Favorites link a user to an article. They enrich article responses with `favorited` and `favoritesCount`.

---

## 2. Extraction Decision

### What moves to `article-service`
| Component | Rationale |
|-----------|-----------|
| Articles (CRUD) | Core of the Article bounded context |
| Tags | Tags are article metadata; no standalone usage outside articles |
| Comments | Tightly coupled to articles (`articleId` FK); always accessed via article slug |
| Favorites | Favorites are article-scoped (`article_favorites` join table); always accessed via article slug |

### What stays in the main app
| Component | Rationale |
|-----------|-----------|
| Users / Auth | Identity is a cross-cutting concern consumed by all services; moving it would create circular dependencies |
| Profiles / Follows | Social graph (follow/unfollow) is user-centric; profiles are rendered using user data |
| JWT / Security | Token issuance and validation belong to the identity provider |
| GraphQL layer | Stays in main app as an aggregation gateway (can call both services) |

### Cross-service boundary
The key coupling point is that **article responses include author profile data** (`ProfileData` with username, bio, image, following status). After extraction:

- The article-service stores only the `userId` foreign key on articles and comments.
- When building article/comment responses, the article-service calls the main app's `/api/internal/users/{id}/profile` REST endpoint to resolve user profile information.
- A `UserServiceClient` (REST-based) replaces the direct `UserRelationshipQueryService` and `UserReadService` calls inside the article-service.
- DTOs (`UserProfileResponse`) define the service-to-service contract.

### Authorization
`AuthorizationService.canWriteArticle(user, article)` and `canWriteComment(user, article, comment)` compare `user.getId()` with `article.getUserId()` / `comment.getUserId()`. The article-service receives the authenticated `userId` from the JWT (shared secret) and performs these checks locally — no cross-service call needed for authorization.

---

## 3. Database Strategy

Each service owns its database:
- **Main app:** SQLite with `users` and `follows` tables
- **article-service:** SQLite with `articles`, `tags`, `article_tags`, `comments`, `article_favorites` tables

Flyway migrations are split accordingly.

---

## 4. Shared Infrastructure

Both services share:
- **JWT secret** — so the article-service can validate tokens independently
- **Jackson customizations** — Joda `DateTime` serialization
- **MyBatis configuration** — `DateTimeHandler`, underscore-to-camel-case mapping

These are duplicated (not shared as a library) to keep services independently deployable.
