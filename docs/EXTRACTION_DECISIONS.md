# Microservice Extraction Decisions

## 1. Domain Boundary Analysis

The RealWorld monolith implements a social blogging platform with two primary bounded contexts:

### Bounded Context: Article (to be extracted)

| Sub-domain | Entities | Tables | Rationale |
|------------|----------|--------|-----------|
| **Articles** | `Article` | `articles` | Core aggregate root; owns slug, title, body, timestamps |
| **Tags** | `Tag` | `tags`, `article_tags` | Tightly coupled to articles; no independent lifecycle |
| **Comments** | `Comment` | `comments` | Scoped by `article_id`; lifecycle depends on articles |
| **Favorites** | `ArticleFavorite` | `article_favorites` | Join between article and user IDs; logically article-centric |

**Application services moving:** `ArticleQueryService`, `CommentQueryService`, `TagsQueryService`, `ArticleCommandService`

**API controllers moving:** `ArticleApi`, `ArticlesApi`, `CommentsApi`, `ArticleFavoriteApi`, `TagsApi`

**Infrastructure moving:** `ArticleMapper`, `ArticleFavoriteMapper`, `CommentMapper`, `MyBatisArticleRepository`, `MyBatisArticleFavoriteRepository`, `MyBatisCommentRepository`, `ArticleReadService`, `ArticleFavoritesReadService`, `CommentReadService`, `TagReadService`

### Bounded Context: User/Profile (stays in monolith)

| Sub-domain | Entities | Tables | Rationale |
|------------|----------|--------|-----------|
| **Users** | `User` | `users` | Identity and authentication root |
| **Follows** | `FollowRelation` | `follows` | Social graph, user-to-user relationship |
| **Profiles** | (read model) | — | Projection of `User` + follow state |
| **Auth/JWT** | `JwtService`, `JwtTokenFilter` | — | Cross-cutting security concern |

**Stays in monolith:** `UserRepository`, `UserReadService`, `UserRelationshipQueryService`, `ProfileQueryService`, `UserQueryService`, `UsersApi`, `CurrentUserApi`, `ProfileApi`, `WebSecurityConfig`, `JwtTokenFilter`, `DefaultJwtService`

## 2. Cross-Service Dependencies

### Article -> User dependencies (replaced with REST client)

| Caller (Article Service) | Dependency (User Service) | Resolution |
|--------------------------|---------------------------|------------|
| `ArticleQueryService.fillExtraInfo()` | `UserRelationshipQueryService.isUserFollowing()` | REST call to User Service |
| `ArticleQueryService.setIsFollowingAuthor()` | `UserRelationshipQueryService.followingAuthors()` | REST call to User Service |
| `ArticleQueryService.findUserFeed*()` | `UserRelationshipQueryService.followedUsers()` | REST call to User Service |
| `CommentQueryService.findById()` | `UserRelationshipQueryService.isUserFollowing()` | REST call to User Service |
| `CommentQueryService.findByArticleId()` | `UserRelationshipQueryService.followingAuthors()` | REST call to User Service |
| `JwtTokenFilter` | `UserRepository.findById()` | Shared JWT validation (both services use same secret) |
| `ArticleApi/ArticlesApi` | `@AuthenticationPrincipal User` | JWT filter resolves User from token locally |

### User -> Article dependencies

The User/Profile context does **not** depend on Article entities. The `ProfileApi` and `UsersApi` only reference `User`, `FollowRelation`, and profile data. No changes needed in the monolith for the extraction.

## 3. Cross-Service Communication Design

### Approach: REST Client with DTOs

The Article Service will call the User Service (monolith) over HTTP for user-relationship queries:

**New endpoint on the monolith** (User Service):
- `GET /api/internal/users/{userId}/following/{targetId}` -> `boolean`
- `POST /api/internal/users/{userId}/following-authors` (body: list of IDs) -> `Set<String>`
- `GET /api/internal/users/{userId}/followed-users` -> `List<String>`
- `GET /api/internal/users/{userId}` -> `UserDto` (for JWT filter)

**Article Service REST client:** `UserServiceClient` wraps `RestTemplate` calls to these endpoints.

### Authentication
Both services share the same JWT secret. The Article Service runs its own `JwtTokenFilter` and resolves the user ID from the token. It then fetches the full `User` object from the User Service via REST when needed.

## 4. What Changes in the Monolith

1. **New internal API controller** (`InternalUserApi`) exposes user-relationship data for the Article Service.
2. **No code removed** from the monolith - it retains all existing functionality for backward compatibility.
3. The monolith's existing Article endpoints continue to work (both services can coexist during migration).

## 5. Article Service Structure

```
article-service/
  build.gradle                    # Standalone Spring Boot 2.6.3 build
  src/main/java/io/spring/
    ArticleServiceApplication.java
    Util.java
    JacksonCustomizations.java
    MyBatisConfig.java
    api/                          # Article, Articles, Comments, Favorite, Tags controllers
    api/exception/                # Exception handlers (shared)
    api/security/                 # JWT filter + security config
    application/                  # Query services + command service
    application/article/          # Article command params
    application/data/             # DTOs
    core/article/                 # Article, Tag, ArticleRepository
    core/comment/                 # Comment, CommentRepository
    core/favorite/                # ArticleFavorite, ArticleFavoriteRepository
    core/service/                 # AuthorizationService, JwtService
    core/user/                    # User entity (read-only copy for JWT)
    infrastructure/
      mybatis/                    # DateTimeHandler, mappers, read services
      repository/                 # MyBatis repository implementations
      service/                    # DefaultJwtService
    client/                       # UserServiceClient (REST client to monolith)
  src/main/resources/
    application.properties        # Port 8081, user-service.url config
    db/migration/                 # Article-only schema migrations
    mapper/                       # MyBatis XML mappers (article-related only)
```

## 6. Key Design Decisions

1. **Tags with articles, not separate service:** Tags have no independent lifecycle - they're created as part of article creation and queried alongside articles.

2. **Comments with articles:** Comments are scoped by `article_id` and have no cross-aggregate references beyond `user_id` (which becomes a REST lookup).

3. **Favorites with articles:** The `article_favorites` table is a join between article and user IDs. The article service owns the table and references user IDs as opaque foreign keys.

4. **User entity copy in article-service:** A minimal read-only `User` class is kept in the article service for JWT authentication. The full user lifecycle (registration, update, password) stays in the monolith.

5. **Shared JWT secret:** Both services validate JWT tokens independently using the same secret, avoiding a network hop for every authenticated request.

6. **SQLite per-service:** Each service gets its own SQLite database file, consistent with the monolith's existing approach.
