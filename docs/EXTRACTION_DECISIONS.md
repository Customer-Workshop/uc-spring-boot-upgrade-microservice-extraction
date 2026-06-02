# Article Service Extraction Decisions

## Bounded Contexts Identified

| Context | Domain Objects | Tables |
|---------|---------------|--------|
| **Articles/Tags** | `Article`, `Tag`, `ArticleRepository` | `articles`, `tags`, `article_tags` |
| **Comments** | `Comment`, `CommentRepository` | `comments` |
| **Favorites** | `ArticleFavorite`, `ArticleFavoriteRepository` | `article_favorites` |
| **Users/Profiles** | `User`, `FollowRelation`, `UserRepository` | `users`, `follows` |

## What Moves to Article Service

### Domain (core)
- `Article` — the aggregate root
- `Tag` — value object owned by articles
- `ArticleRepository` — write-side persistence interface

### Application
- `ArticleCommandService` — create/update orchestration
- `ArticleQueryService` — read-side queries (articles, feed, cursor pagination)
- `TagsQueryService` — tag listing
- `NewArticleParam`, `UpdateArticleParam` — command DTOs
- `DuplicatedArticleConstraint` / `DuplicatedArticleValidator` — slug uniqueness
- `ArticleData`, `ArticleDataList`, `ArticleFavoriteCount` — read DTOs
- Pagination helpers: `Page`, `CursorPager`, `CursorPageParameter`, `DateTimeCursor`, `Node`, `PageCursor`

### Infrastructure
- `MyBatisArticleRepository` — write-side MyBatis impl
- `ArticleMapper` — MyBatis mapper interface
- `ArticleReadService`, `TagReadService` — read-side MyBatis mappers
- All corresponding XML mappers: `ArticleMapper.xml`, `ArticleReadService.xml`, `TagReadService.xml`, `TransferData.xml`
- `DateTimeHandler` — Joda-Time type handler

### API
- `ArticleApi` — single-article CRUD (`/articles/{slug}`)
- `ArticlesApi` — article listing/creation (`/articles`, `/articles/feed`)
- `TagsApi` — tag listing (`/tags`)

### Shared/Utility
- `Util` — `isEmpty` helper
- `JacksonCustomizations` — DateTime serializer
- `MyBatisConfig` — transaction management

## What Stays in the Monolith

- **Users/Profiles**: `User`, `UserRepository`, `UserService`, `UsersApi`, `CurrentUserApi`, `ProfileApi`
- **Comments**: `Comment`, `CommentRepository`, `CommentsApi`, `CommentQueryService`
- **Favorites**: `ArticleFavorite`, `ArticleFavoriteRepository`, `ArticleFavoriteApi`
- **Security**: `JwtService`, `DefaultJwtService`, `JwtTokenFilter`, `WebSecurityConfig`
- **GraphQL**: All DGS data fetchers and mutations

## Coupling Points

### Article → User (the main coupling)
- `Article.userId` references `User.id` — stored as a foreign key string
- `ArticleApi` uses `@AuthenticationPrincipal User` for authorization checks
- `ArticlesApi.createArticle()` passes `User` to `ArticleCommandService`
- `ArticleQueryService` joins articles with users table to populate `ProfileData` (author info)
- `AuthorizationService.canWriteArticle()` compares `User.id` with `Article.userId`

### Article → Favorites (read-side coupling)
- `ArticleQueryService.fillExtraInfo()` calls `ArticleFavoritesReadService` to get favorite counts and user-favorite status
- `ArticleReadService.xml` joins `article_favorites` table for filtering by "favorited by"

### Article → Follows (feed feature)
- `ArticleQueryService.findUserFeed()` calls `UserRelationshipQueryService.followedUsers()` to get the feed

## Cross-Service Communication Strategy

The extracted Article Service replaces direct `User` domain references with:

1. **`UserServiceClient`** — a REST client that calls the monolith's `/users` and `/profiles` endpoints to fetch user data when needed
2. **`UserDto`** — a lightweight DTO in the article-service containing only `id`, `username`, `bio`, `image` (no password/email)
3. **Authorization** — the article-service accepts a user ID (from JWT or header) and uses it for ownership checks, without needing the full `User` domain object
4. **Favorites/Follows** — these remain in the monolith; the article-service does not handle favorite counts or follow-based feeds in this initial extraction. The monolith calls the article-service APIs when it needs article data for favorites/feed queries.

### API Contract
- Article Service runs on **port 8081** with its own SQLite database (`article-dev.db`)
- Monolith continues on **port 8080**
- Article Service exposes: `GET/POST /articles`, `GET/PUT/DELETE /articles/{slug}`, `GET /tags`
