# Articles Bounded Context - Domain Boundary Analysis

## 1. Domain Overview

The **Articles** bounded context is the core content-management domain of the Conduit social blogging platform. It is responsible for:

- **Article lifecycle management** - Creating, reading, updating, and deleting blog articles
- **Feed generation** - Building personalized feeds of articles from followed authors and global article listings with filtering
- **Favorites** - Allowing users to favorite/unfavorite articles and tracking favorite counts
- **Tags** - Categorizing articles with tags and providing tag discovery

This domain is the largest bounded context in the application and serves as the central content aggregate around which the Users/Profiles and Comments domains revolve.

---

## 2. Entities & Aggregates

### Article (Aggregate Root)

**File:** `src/main/java/io/spring/core/article/Article.java`

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` (UUID) | Primary identifier, generated on creation |
| `userId` | `String` | Foreign reference to the author's User entity |
| `slug` | `String` | URL-friendly identifier derived from title via `toSlug()` |
| `title` | `String` | Article title |
| `description` | `String` | Short summary |
| `body` | `String` | Full article content |
| `tags` | `List<Tag>` | Associated tags (deduplicated on creation) |
| `createdAt` | `DateTime` (Joda) | Creation timestamp |
| `updatedAt` | `DateTime` (Joda) | Last update timestamp |

**Key behaviors:**
- `toSlug(title)` - Static method converting title to lowercase, hyphen-separated slug (replaces special characters, whitespace, CJK punctuation)
- `update(title, description, body)` - Partial update; only non-empty fields are applied; slug and `updatedAt` are refreshed on title change

### Tag (Value Object)

**File:** `src/main/java/io/spring/core/article/Tag.java`

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` (UUID) | Primary identifier |
| `name` | `String` | Tag label (equality is based on `name` only) |

Tags are deduplicated by name. When an article is created, existing tags are reused via `ArticleMapper.findTag()`.

### ArticleFavorite (Join Entity)

**File:** `src/main/java/io/spring/core/favorite/ArticleFavorite.java`

| Field | Type | Description |
|-------|------|-------------|
| `articleId` | `String` | References `Article.id` |
| `userId` | `String` | References `User.id` (cross-boundary) |

This is a simple association entity with no behavior beyond construction. It links a user to a favorited article.

---

## 3. API Endpoints

### REST Endpoints

| Method | Path | Controller | Description | Auth Required |
|--------|------|------------|-------------|---------------|
| `POST` | `/articles` | `ArticlesApi` | Create a new article | Yes |
| `GET` | `/articles` | `ArticlesApi` | List articles (filter by tag, author, favoritedBy; offset/limit pagination) | Optional |
| `GET` | `/articles/feed` | `ArticlesApi` | Personalized feed from followed users | Yes |
| `GET` | `/articles/{slug}` | `ArticleApi` | Get a single article by slug | Optional |
| `PUT` | `/articles/{slug}` | `ArticleApi` | Update an article (author only) | Yes |
| `DELETE` | `/articles/{slug}` | `ArticleApi` | Delete an article (author only) | Yes |
| `POST` | `/articles/{slug}/favorite` | `ArticleFavoriteApi` | Favorite an article | Yes |
| `DELETE` | `/articles/{slug}/favorite` | `ArticleFavoriteApi` | Unfavorite an article | Yes |
| `GET` | `/tags` | `TagsApi` | List all tags | No |

### GraphQL Operations

**Queries** (defined in `schema.graphqls`, implemented in `ArticleDatafetcher` and `TagDatafetcher`):

| Field | Arguments | Description |
|-------|-----------|-------------|
| `article(slug)` | `slug: String!` | Fetch single article by slug |
| `articles(...)` | `first/after/last/before`, `authoredBy`, `favoritedBy`, `withTag` | Cursor-paginated article listing |
| `feed(...)` | `first/after/last/before` | Cursor-paginated personal feed |
| `tags` | none | List all tag names |

**Mutations** (implemented in `ArticleMutation`):

| Mutation | Arguments | Description |
|----------|-----------|-------------|
| `createArticle(input)` | `CreateArticleInput!` (title, description, body, tagList) | Create article |
| `updateArticle(slug, changes)` | `slug: String!`, `UpdateArticleInput!` | Update article (author only) |
| `deleteArticle(slug)` | `slug: String!` | Delete article (author only) |
| `favoriteArticle(slug)` | `slug: String!` | Favorite an article |
| `unfavoriteArticle(slug)` | `slug: String!` | Unfavorite an article |

**Nested Fields on Profile** (resolved by `ArticleDatafetcher`):

| Field | Description |
|-------|-------------|
| `Profile.articles(...)` | Cursor-paginated articles authored by this user |
| `Profile.favorites(...)` | Cursor-paginated articles favorited by this user |
| `Profile.feed(...)` | Cursor-paginated feed for this user |

---

## 4. Application Services

The Articles domain follows a **CQRS-lite** pattern, separating read (query) and write (command) responsibilities.

### ArticleCommandService (Write Side)

**File:** `src/main/java/io/spring/application/article/ArticleCommandService.java`

| Method | Description |
|--------|-------------|
| `createArticle(NewArticleParam, User)` | Constructs an `Article` entity from validated params and the creator's ID, persists via `ArticleRepository` |
| `updateArticle(Article, UpdateArticleParam)` | Delegates to `Article.update()` then persists |

**DTOs:**
- `NewArticleParam` - `@JsonRootName("article")` with `@NotBlank` on title, description, body; optional `tagList`; includes `@DuplicatedArticleConstraint` on title
- `UpdateArticleParam` - `@JsonRootName("article")` with optional title, body, description (defaults to empty string)

### ArticleQueryService (Read Side)

**File:** `src/main/java/io/spring/application/ArticleQueryService.java`

| Method | Description |
|--------|-------------|
| `findById(id, user)` | Fetch article read-model by ID, enrich with favorite/follow status |
| `findBySlug(slug, user)` | Fetch article read-model by slug, enrich with favorite/follow status |
| `findRecentArticles(tag, author, favoritedBy, page, user)` | Offset-paginated article listing with filters |
| `findUserFeed(user, page)` | Offset-paginated feed (articles from followed users) |
| `findRecentArticlesWithCursor(...)` | Cursor-paginated article listing with filters |
| `findUserFeedWithCursor(user, page)` | Cursor-paginated feed |

**Read-Model DTOs:**
- `ArticleData` - Flat read-model including: id, slug, title, description, body, favorited (boolean), favoritesCount (int), createdAt, updatedAt, tagList, and embedded `ProfileData` (author). Implements `Node` for cursor pagination via `DateTimeCursor`.
- `ArticleDataList` - Wrapper containing `List<ArticleData>` and total count for offset pagination.
- `ArticleFavoriteCount` - Simple value object with article id and favorite count.

**Key internal methods (enrichment):**
- `fillExtraInfo()` - Orchestrates setting favorite count, isFavorite flag, and isFollowing flag
- `setFavoriteCount()` - Batch-fetches favorite counts via `ArticleFavoritesReadService`
- `setIsFavorite()` - Checks if current user has favorited each article
- `setIsFollowingAuthor()` - **Cross-boundary**: Checks if current user follows each article's author via `UserRelationshipQueryService`

### TagsQueryService

**File:** `src/main/java/io/spring/application/TagsQueryService.java`

| Method | Description |
|--------|-------------|
| `allTags()` | Returns all tag names via `TagReadService.all()` |

---

## 5. Data Storage

### Database: SQLite (via Flyway migrations)

**Migration file:** `src/main/resources/db/migration/V1__create_tables.sql`

#### Articles-Owned Tables

**`articles`**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | `varchar(255)` | PRIMARY KEY |
| `user_id` | `varchar(255)` | Foreign reference to `users.id` (no FK constraint) |
| `slug` | `varchar(255)` | UNIQUE |
| `title` | `varchar(255)` | |
| `description` | `text` | |
| `body` | `text` | |
| `created_at` | `TIMESTAMP` | NOT NULL |
| `updated_at` | `TIMESTAMP` | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**`tags`**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | `varchar(255)` | PRIMARY KEY |
| `name` | `varchar(255)` | NOT NULL |

**`article_tags`** (join table)

| Column | Type | Constraints |
|--------|------|-------------|
| `article_id` | `varchar(255)` | NOT NULL |
| `tag_id` | `varchar(255)` | NOT NULL |

**`article_favorites`** (join table)

| Column | Type | Constraints |
|--------|------|-------------|
| `article_id` | `varchar(255)` | NOT NULL |
| `user_id` | `varchar(255)` | NOT NULL |
| | | PRIMARY KEY (`article_id`, `user_id`) |

#### Tables Referenced but Not Owned

| Table | Owner Domain | How Articles Uses It |
|-------|-------------|---------------------|
| `users` | Users/Profiles | JOINed in read queries to resolve author profile (username, bio, image) |
| `follows` | Users/Profiles | Queried by `UserRelationshipQueryService` to build feeds and check follow status |

### MyBatis Mapper XML

**File:** `src/main/resources/mapper/ArticleReadService.xml`

Key SQL patterns showing cross-boundary joins:

```sql
-- Article read queries JOIN to users table for author profile
LEFT JOIN users U ON U.id = A.user_id

-- Article listing queries also JOIN users for author/favoritedBy filtering
LEFT JOIN users AU ON AU.id = A.user_id
LEFT JOIN users AFU ON AFU.id = AF.user_id
```

These joins directly couple the Articles read path to the Users domain's `users` table.

### Infrastructure Layer

#### Write-Side Mappers

| Interface | Implementation | Description |
|-----------|---------------|-------------|
| `ArticleRepository` | `MyBatisArticleRepository` | `@Transactional` save (insert-or-update), findById, findBySlug, remove. Tag deduplication handled in `createNew()` |
| `ArticleFavoriteRepository` | `MyBatisArticleFavoriteRepository` | Idempotent save, find by composite key, remove |

**Underlying MyBatis mappers:**
- `ArticleMapper` - insert, findById, findBySlug, update, delete, findTag, insertTag, insertArticleTagRelation
- `ArticleFavoriteMapper` - find, insert, delete

#### Read-Side Mappers

| Mapper | Description |
|--------|-------------|
| `ArticleReadService` | Complex SQL queries with JOINs across `articles`, `article_tags`, `tags`, `users`, `article_favorites` |
| `ArticleFavoritesReadService` | isUserFavorite, articleFavoriteCount, batch counts, user favorites set |
| `TagReadService` | Simple `all()` query returning tag names |

---

## 6. Cross-Boundary Dependencies

### 6.1 Articles -> Users/Profiles Domain

#### Direct Entity Reference

| Location | Coupling Point | Details |
|----------|---------------|---------|
| `Article.userId` | Field-level | Stores the author's User ID directly on the Article entity |
| `ArticleFavorite.userId` | Field-level | Stores the favoriting user's ID |
| `ArticleData.profileData` | DTO embedding | `ProfileData` (id, username, bio, image, following) is embedded in article read model |

#### Authentication Dependency

| Location | Coupling Point | Details |
|----------|---------------|---------|
| `ArticlesApi` | `@AuthenticationPrincipal User user` | Spring Security injects `User` entity from Users domain |
| `ArticleApi` | `@AuthenticationPrincipal User user` | Same pattern |
| `ArticleFavoriteApi` | `@AuthenticationPrincipal User user` | Same pattern |
| `ArticleMutation` | `SecurityUtil.getCurrentUser()` | GraphQL equivalent; returns `Optional<User>` |
| `ArticleDatafetcher` | `SecurityUtil.getCurrentUser()` | Same pattern for read operations |

#### Authorization Dependency

| Location | Coupling Point | Details |
|----------|---------------|---------|
| `ArticleApi.updateArticle()` | `AuthorizationService.canWriteArticle(user, article)` | Checks `user.getId().equals(article.getUserId())` |
| `ArticleApi.deleteArticle()` | `AuthorizationService.canWriteArticle(user, article)` | Same check |
| `ArticleMutation.updateArticle()` | `AuthorizationService.canWriteArticle(user, article)` | GraphQL equivalent |
| `ArticleMutation.deleteArticle()` | `AuthorizationService.canWriteArticle(user, article)` | GraphQL equivalent |

`AuthorizationService` is a shared service in `core/service/` that spans both Article and Comment domains.

#### Query-Time Dependencies on UserRelationshipQueryService

| Location | Method Called | Purpose |
|----------|-------------|---------|
| `ArticleQueryService.findUserFeed()` | `userRelationshipQueryService.followedUsers(userId)` | Get list of users the current user follows, then fetch their articles |
| `ArticleQueryService.findUserFeedWithCursor()` | `userRelationshipQueryService.followedUsers(userId)` | Same, cursor-paginated variant |
| `ArticleQueryService.setIsFollowingAuthor()` | `userRelationshipQueryService.followingAuthors(userId, authorIds)` | Batch check if user follows each article's author |
| `ArticleQueryService.fillExtraInfo()` (single) | `userRelationshipQueryService.isUserFollowing(userId, authorId)` | Single-article follow check |

`UserRelationshipQueryService` is a MyBatis mapper that queries the `follows` table (owned by Users domain).

#### Database-Level Coupling

The `ArticleReadService.xml` mapper performs direct SQL JOINs against the `users` table:

- `LEFT JOIN users U ON U.id = A.user_id` - Resolve author profile for every article query
- `LEFT JOIN users AU ON AU.id = A.user_id` - Filter articles by author username
- `LEFT JOIN users AFU ON AFU.id = AF.user_id` - Filter articles by favoritedBy username

These are **the tightest coupling points** -- the Articles read path cannot function without direct access to the `users` table.

#### GraphQL Layer Dependencies

| Location | Dependency | Details |
|----------|-----------|---------|
| `ArticleDatafetcher` | `UserRepository` | `userFeed()` calls `userRepository.findByUsername()` to resolve a `Profile` into a `User` for feed queries |
| `ArticleDatafetcher.getCommentArticle()` | `CommentData` | Reads `comment.getArticleId()` from local context (Comment domain crossing into Articles) |

### 6.2 Articles -> Comments Domain

| Location | Coupling Point | Details |
|----------|---------------|---------|
| `AuthorizationService.canWriteComment()` | Shared service | Accepts `Article` and `Comment` -- spans both domains |
| `ArticleDatafetcher.getCommentArticle()` | GraphQL resolver | Resolves `Comment.article` field; receives `CommentData` from Comment domain's local context |
| GraphQL schema | `Article.comments` field | The `Article` type exposes a `comments` connection, resolved by `CommentDatafetcher` |

### 6.3 Shared / Cross-Cutting Services

| Service | Location | Used By |
|---------|----------|---------|
| `AuthorizationService` | `core/service/` | ArticleApi, ArticleMutation (for articles); CommentsApi, CommentMutation (for comments) |
| `JwtService` / `JwtTokenFilter` | `infrastructure/service/` | All authenticated endpoints across all domains |
| `SecurityUtil` | `graphql/` | All GraphQL resolvers across all domains |

### Dependency Direction Summary

```
Articles ──depends on──> Users/Profiles
  - Article.userId (entity reference)
  - ArticleFavorite.userId (entity reference)
  - @AuthenticationPrincipal User (API layer)
  - UserRelationshipQueryService (feed + follow checks)
  - Direct SQL JOINs to `users` table (read path)
  - UserRepository (GraphQL layer)

Articles <──depended on by── Comments
  - Comment.articleId (entity reference)
  - AuthorizationService.canWriteComment (shared)
  - Article.comments GraphQL field
```

---

## 7. Recommendations for Microservice Extraction

### 7.1 Replace Direct Database JOINs with API Calls

**Current state:** `ArticleReadService.xml` JOINs against the `users` table to resolve author profiles.

**Recommendation:**
- Remove all JOINs to the `users` table from article queries
- Article queries should return only `user_id` (the author reference)
- Introduce an internal API call (REST or gRPC) to a Users/Profiles service to resolve `user_id` -> `ProfileData`
- Use batch resolution (e.g., `GET /internal/profiles?ids=id1,id2,...`) to avoid N+1 calls when listing articles
- Cache profile data aggressively since profiles change infrequently relative to read volume

### 7.2 Replace UserRelationshipQueryService with an API or Event-Driven Approach

**Current state:** `ArticleQueryService` directly calls `UserRelationshipQueryService` (a MyBatis mapper querying the `follows` table) for:
- Building feeds (get followed user IDs)
- Enriching articles with "is following author" status

**Recommendation:**
- **Feed generation:** Call a Users service API to get followed user IDs, then query articles locally. Alternatively, adopt an event-driven approach where the Articles service maintains a local projection of follow relationships by consuming `UserFollowed` / `UserUnfollowed` events.
- **Follow status enrichment:** Call a Users service API with batch support: `POST /internal/relationships/check` with `{ userId, targetIds }` returning a set of followed IDs.

### 7.3 Decouple Authentication

**Current state:** All API controllers accept `@AuthenticationPrincipal User user`, creating a compile-time dependency on `io.spring.core.user.User`.

**Recommendation:**
- Replace `@AuthenticationPrincipal User` with a lightweight, domain-agnostic principal (e.g., a `UserPrincipal` record containing just `id` and `username`)
- JWT validation and principal extraction should happen in a gateway or shared auth library, not by depending on the full User entity
- The Articles service should only need a `userId` string, not the full User aggregate

### 7.4 Extract Authorization into the Articles Service

**Current state:** `AuthorizationService` is a shared class in `core/service/` that handles both article and comment authorization.

**Recommendation:**
- Move `canWriteArticle()` into the Articles service since it only compares `user.getId()` with `article.getUserId()`
- Leave comment authorization to the Comments service
- Each microservice should own its authorization logic for its own resources

### 7.5 Adopt Event-Driven Communication for Favorites

**Current state:** `ArticleFavorite` is a synchronous write operation that links `articleId` to `userId`.

**Recommendation:**
- Favorites can remain synchronous within the Articles service (it owns the `article_favorites` table)
- However, if other services need to know about favorites (e.g., for notifications or recommendation feeds), publish `ArticleFavorited` / `ArticleUnfavorited` domain events via a message broker (Kafka, RabbitMQ, etc.)

### 7.6 Handle the `article_favorites` Table Ownership

The `article_favorites` table references both `article_id` and `user_id`. In a microservices world:
- The Articles service should own this table since favorites are inherently an article-scoped concept
- `user_id` becomes an opaque external reference (no foreign key constraint, which is already the case in the current schema)
- The `favoritedBy` filter (listing articles favorited by a specific username) currently requires a JOIN to `users` to resolve username -> user_id. This should be changed to accept `user_id` directly, with the API gateway or frontend resolving username -> ID first.

### 7.7 Separate the GraphQL Layer

**Current state:** GraphQL data fetchers in `io.spring.graphql` span all domains in a single schema.

**Recommendation:**
- Use **Apollo Federation** or **Netflix DGS Federation** to split the schema across microservices
- The Articles service owns the `Article`, `ArticlesConnection`, `ArticleEdge` types and related queries/mutations
- The `Article.author` field becomes a federated reference resolved by the Users/Profiles service
- The `Article.comments` field becomes a federated reference resolved by the Comments service
- The `Profile.articles`, `Profile.favorites`, and `Profile.feed` fields are contributed by the Articles service as entity extensions on the `Profile` type

### 7.8 Data Migration Strategy

For extracting the Articles database:

1. **Tables to migrate to Articles service database:** `articles`, `tags`, `article_tags`, `article_favorites`
2. **Tables to leave with Users service:** `users`, `follows`
3. **Tables to migrate to Comments service:** `comments`
4. **Schema changes needed:**
   - Remove any implicit assumptions about foreign key integrity between `articles.user_id` and `users.id`
   - The `article_favorites.user_id` column remains but becomes an external reference
   - Add indexes on `user_id` columns since cross-service consistency checks via JOINs are no longer possible

### 7.9 Summary of Extraction Effort

| Area | Effort | Risk |
|------|--------|------|
| Core domain (Article, Tag, ArticleFavorite) | Low | Low - already well-encapsulated |
| Write path (ArticleCommandService) | Low | Low - only needs userId, no User dependency |
| Read path (ArticleQueryService) | **High** | **High** - tightly coupled to users table via SQL JOINs and UserRelationshipQueryService |
| REST API layer | Medium | Medium - needs auth principal decoupling |
| GraphQL layer | **High** | Medium - requires federation setup |
| Feed generation | **High** | **High** - fundamentally cross-domain (requires follow graph from Users domain) |
| Favorites | Low | Low - self-contained within Articles |
| Tags | Low | Low - fully self-contained, no cross-boundary dependencies |
