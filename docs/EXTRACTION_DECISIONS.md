# Article Service Extraction Decisions

## Bounded Contexts Identified

### 1. Articles / Tags
- **Domain objects:** `Article`, `Tag`
- **Repositories:** `ArticleRepository`, `ArticleMapper`
- **Read services:** `ArticleReadService`, `TagReadService`
- **Application services:** `ArticleQueryService`, `TagsQueryService`, `ArticleCommandService`
- **API:** `ArticlesApi`, `ArticleApi`, `TagsApi`
- **Coupling:** Articles reference `userId` (author). Queries join `users` table to populate `ProfileData` for the author. Tag queries are standalone.

### 2. Comments
- **Domain objects:** `Comment`
- **Repositories:** `CommentRepository`, `CommentMapper`
- **Read services:** `CommentReadService`
- **Application services:** `CommentQueryService`
- **API:** `CommentsApi`
- **Coupling:** Comments reference `userId` (author) and `articleId`. Read queries join `users` table for author `ProfileData`. `UserRelationshipQueryService` used to determine if current user follows comment authors.

### 3. Favorites
- **Domain objects:** `ArticleFavorite`
- **Repositories:** `ArticleFavoriteRepository`, `ArticleFavoriteMapper`
- **Read services:** `ArticleFavoritesReadService`
- **API:** `ArticleFavoriteApi`
- **Coupling:** Favorites link `articleId` to `userId`. Favorite counts and "is favorited" status are enriched into `ArticleData` responses.

### 4. Users / Profiles
- **Domain objects:** `User`, `FollowRelation`
- **Repositories:** `UserRepository`, `UserMapper`
- **Read services:** `UserReadService`, `UserRelationshipQueryService`
- **Application services:** `ProfileQueryService`, `UserQueryService`, `UserService`
- **API:** `UsersApi`, `CurrentUserApi`, `ProfileApi`
- **Security:** `JwtTokenFilter`, `WebSecurityConfig`, `JwtService`, `DefaultJwtService`
- **Coupling:** Users own articles, comments, and favorites via `userId` foreign keys. Follow relationships used by article and comment queries.

## What Moves to the Article Service

| Domain          | Entities                   | Tables                                        |
|-----------------|----------------------------|-----------------------------------------------|
| Articles        | `Article`, `Tag`           | `articles`, `tags`, `article_tags`             |
| Comments        | `Comment`                  | `comments`                                    |
| Favorites       | `ArticleFavorite`          | `article_favorites`                           |

**Application layer moving:** `ArticleQueryService`, `CommentQueryService`, `TagsQueryService`, `ArticleCommandService`, all article/comment/favorite data classes (`ArticleData`, `CommentData`, `ArticleDataList`, `ArticleFavoriteCount`, `ProfileData`), pagination utilities (`Page`, `CursorPager`, `CursorPageParameter`, `Node`, `PageCursor`, `DateTimeCursor`), validation (`NewArticleParam`, `UpdateArticleParam`, `DuplicatedArticleConstraint/Validator`).

**Infrastructure moving:** `ArticleMapper`, `CommentMapper`, `ArticleFavoriteMapper`, `ArticleReadService`, `CommentReadService`, `ArticleFavoritesReadService`, `TagReadService`, `MyBatisArticleRepository`, `MyBatisCommentRepository`, `MyBatisArticleFavoriteRepository`, `DateTimeHandler`, all corresponding MyBatis XML mapper files.

**API layer moving:** `ArticlesApi`, `ArticleApi`, `CommentsApi`, `ArticleFavoriteApi`, `TagsApi`.

**Also moving:** `AuthorizationService` (depends on Article and Comment), exception classes, `Util`.

## What Stays in the Monolith

| Domain          | Entities                   | Tables                 |
|-----------------|----------------------------|------------------------|
| Users           | `User`, `FollowRelation`   | `users`, `follows`     |

**Stays:** `UserRepository`, `UserMapper`, `UserReadService`, `UserRelationshipQueryService`, `ProfileQueryService`, `UserQueryService`, `UserService`, `UsersApi`, `CurrentUserApi`, `ProfileApi`, JWT security (`JwtTokenFilter`, `WebSecurityConfig`, `JwtService`, `DefaultJwtService`), all user-related GraphQL data fetchers (`UserMutation`, `MeDatafetcher`, `ProfileDatafetcher`, `RelationMutation`), all user-related MyBatis XML mappers.

## Coupling Points Between Domains

### 1. Article author resolution
- **Where:** `ArticleReadService.xml` joins `articles A` → `users U` on `A.user_id = U.id` to populate `ProfileData` (username, bio, image).
- **Strategy:** Article service stores `userId` as a foreign reference. A `UserServiceClient` REST client calls the monolith's user API to resolve profile data. The article service's MyBatis queries no longer join the `users` table — instead, author profile data is fetched via HTTP after loading article data.

### 2. Comment author resolution
- **Where:** `CommentReadService.xml` joins `comments C` → `users U` on `C.user_id = U.id` for author `ProfileData`.
- **Strategy:** Same as article author — `UserServiceClient` resolves comment author profiles via REST.

### 3. Follow relationship checks
- **Where:** `ArticleQueryService.fillExtraInfo()` and `CommentQueryService` call `UserRelationshipQueryService.isUserFollowing()` and `followingAuthors()` to set `profileData.following`.
- **Strategy:** Article service calls the monolith's user API to check follow status. On failure, defaults to `following = false`.

### 4. Favorite-user linkage
- **Where:** `ArticleFavoritesReadService.userFavorites()` references `currentUser.id`.
- **Strategy:** `userId` is passed as a string parameter — no direct `User` domain object dependency in the extracted service. The `User` type parameter is replaced with a `String userId`.

### 5. Authorization checks
- **Where:** `AuthorizationService.canWriteArticle(user, article)` and `canWriteComment(user, article, comment)` compare `user.getId()` with `article.getUserId()` / `comment.getUserId()`.
- **Strategy:** The article service accepts `userId` (string) from the JWT-authenticated request and compares directly — no `User` domain object needed.

### 6. JWT authentication
- **Where:** `JwtTokenFilter` loads `User` from `UserRepository` and sets it as the Spring Security principal.
- **Strategy:** Article service has its own `JwtTokenFilter` that validates JWT tokens using the same shared secret. Instead of loading the full `User` object, it creates a lightweight principal containing only the user ID. Profile data is fetched lazily via `UserServiceClient` when needed.

## Cross-Service Communication Strategy

### REST Client (`UserServiceClient`)
The article service communicates with the monolith via a dedicated `UserServiceClient` that uses Spring's `RestTemplate`:

- **`GET /api/internal/users/{id}/profile`** — Fetch a user's profile (id, username, bio, image) by user ID.
- **`GET /api/internal/users/{userId}/following/{targetId}`** — Check if a user follows another user.
- **`POST /api/internal/users/following-authors`** — Batch check which authors from a list the current user follows.

### DTOs
Cross-service DTOs live in the article service (not shared):
- `UserProfileResponse` — mirrors `ProfileData` (id, username, bio, image).

### Failure Handling
- If the user service is unavailable, profile data defaults to a placeholder (`username = "unknown"`, `following = false`).
- HTTP errors are logged and wrapped in domain-specific exceptions.

### Internal API Endpoints (added to monolith)
The monolith exposes new internal endpoints under `/api/internal/users/` for the article service to consume. These are not part of the public RealWorld API spec.
