# Article Service Extraction Decisions

## Bounded Contexts Identified

| Context | Domain Objects | Stays/Moves |
|---------|---------------|-------------|
| Articles/Tags | `Article`, `Tag`, `ArticleRepository` | **Moves** to article-service |
| Comments | `Comment`, `CommentRepository` | **Moves** to article-service |
| Favorites | `ArticleFavorite`, `ArticleFavoriteRepository` | **Moves** to article-service |
| Users/Profiles | `User`, `UserRepository`, `FollowRelation` | **Stays** in monolith |

## What Moves to Article Service

- **Entities**: Article, Tag, Comment, ArticleFavorite
- **Repositories**: ArticleRepository, CommentRepository, ArticleFavoriteRepository
- **Services**: ArticleCommandService, ArticleQueryService, CommentQueryService, TagsQueryService, AuthorizationService (article/comment subset)
- **API Controllers**: ArticleApi, ArticlesApi, ArticleFavoriteApi, CommentsApi, TagsApi
- **MyBatis Mappers**: ArticleMapper, CommentMapper, ArticleFavoriteMapper
- **Read Services**: ArticleReadService, CommentReadService, TagReadService, ArticleFavoritesReadService
- **Database Tables**: articles, tags, article_tags, comments, article_favorites

## What Stays in the Monolith

- **Entities**: User, FollowRelation
- **Repositories**: UserRepository
- **Services**: UserService, ProfileQueryService, UserQueryService, JwtService
- **API Controllers**: UsersApi, CurrentUserApi, ProfileApi
- **MyBatis Mappers**: UserMapper
- **Read Services**: UserReadService, UserRelationshipQueryService
- **Database Tables**: users, follows

## Coupling Points

### 1. Article → User (Author)
- `Article.userId` references the author
- `ArticleReadService.xml` JOINs `users` table for author profile data
- **Resolution**: Article-service maintains a read-only `user_profiles` cache table for SQL JOINs. Profile data is fetched via REST client on cache miss.

### 2. Comment → User (Commenter)
- `Comment.userId` references the commenter
- `CommentReadService.xml` JOINs `users` table for commenter profile
- **Resolution**: Same `user_profiles` cache table used for comment author resolution.

### 3. Favorites → User
- `ArticleFavorite.userId` references who favorited
- Query by "favoritedBy" username requires user lookup
- **Resolution**: `user_profiles` table stores username for query support.

### 4. Follow Relationships (Feed)
- `ArticleQueryService.findUserFeed` calls `UserRelationshipQueryService.followedUsers`
- Comment/Article display needs "is following author" state
- **Resolution**: `UserServiceClient` REST call to monolith's `/profiles/{username}` endpoint to get follow state. Feed queries call `/api/users/{id}/following` on the monolith.

### 5. Authorization
- `AuthorizationService.canWriteArticle(User, Article)` checks `user.getId().equals(article.getUserId())`
- **Resolution**: Article-service receives userId from JWT token directly (stateless auth). Ownership check uses the userId from the token against `article.userId` without needing the full User entity.

## Cross-Service Communication Strategy

### REST Client Pattern
- `UserServiceClient` in article-service calls monolith endpoints
- Endpoints used:
  - `GET /profiles/{username}` — resolve profile data and follow state
  - `GET /api/users/{id}/following` — get list of followed user IDs (for feed)
- Failure handling: Return default ProfileData with "Unknown" username on timeout/error

### Authentication
- Both services share the same JWT secret for token validation
- Article-service validates JWT independently (stateless)
- userId extracted from JWT is used for authorization checks

### Data Ownership
- Article-service owns: articles, tags, comments, favorites
- Monolith owns: users, profiles, follow relationships
- Article-service keeps a `user_profiles` read-model table populated from REST responses for efficient SQL JOINs
