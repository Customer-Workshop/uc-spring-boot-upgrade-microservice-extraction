# Articles Domain Boundary Analysis

## 1. Domain Overview

The **Articles** bounded context is responsible for the core content-publishing functionality of the RealWorld application. It manages the full lifecycle of articles (create, read, update, delete), the tagging system that categorizes articles, and the user-favoriting mechanism that allows users to bookmark articles. It exposes both REST and GraphQL APIs for article operations, article listing/filtering, feed generation, tag retrieval, and favorite toggling.

This context is one of the central domains in the application. It depends heavily on the **User/Profile** domain for author information, follow relationships (used in feed generation), and authorization checks.

---

## 2. Entities & Aggregates

### Article (Aggregate Root)

| Field         | Type           | Description                                      |
|---------------|----------------|--------------------------------------------------|
| `id`          | `String` (UUID)| Primary key, generated on creation               |
| `userId`      | `String`       | FK referencing the author in the `users` table    |
| `slug`        | `String`       | URL-friendly, derived from title                  |
| `title`       | `String`       | Article title                                     |
| `description` | `String`       | Short summary                                     |
| `body`        | `String`       | Full article content                              |
| `tags`        | `List<Tag>`    | Collection of associated tags (child entities)    |
| `createdAt`   | `DateTime`     | Joda-Time timestamp                               |
| `updatedAt`   | `DateTime`     | Joda-Time timestamp, updated on mutation          |

**File:** `core/article/Article.java`

- The Article aggregate root owns its Tags via a `List<Tag>` collection.
- Slug generation is a pure domain function (`Article.toSlug()`).
- The `update()` method only modifies non-empty fields and bumps `updatedAt`.

### Tag (Child Entity of Article)

| Field  | Type            | Description              |
|--------|-----------------|--------------------------|
| `id`   | `String` (UUID) | Primary key              |
| `name` | `String`        | Tag label (unique by equality) |

**File:** `core/article/Tag.java`

- Tags are value-like: equality is based on `name` only (`@EqualsAndHashCode(of = "name")`).
- Tags are deduplicated in the Article constructor via `HashSet`.

### ArticleFavorite (Cross-Domain Join Entity)

| Field       | Type     | Description                        |
|-------------|----------|------------------------------------|
| `articleId` | `String` | FK referencing `articles.id`       |
| `userId`    | `String` | FK referencing `users.id`          |

**File:** `core/favorite/ArticleFavorite.java`

- This entity straddles the Article and User domains.
- It has no aggregate root of its own; it is a many-to-many join concept.

### Aggregate Boundaries

- **Article Aggregate** = `Article` + `Tag[]` (via `article_tags` join table). The Article owns its tags.
- **ArticleFavorite** is an independent entity outside the Article aggregate. It references both `articleId` and `userId` and lives in its own `core/favorite` package.

---

## 3. API Endpoints

### REST Endpoints

| Method   | Path                          | Controller               | Description                                           |
|----------|-------------------------------|--------------------------|-------------------------------------------------------|
| `POST`   | `/articles`                   | `ArticlesApi`            | Create a new article (authenticated)                  |
| `GET`    | `/articles`                   | `ArticlesApi`            | List/filter articles (by tag, author, favoritedBy)    |
| `GET`    | `/articles/feed`              | `ArticlesApi`            | Get personalized feed (articles by followed users)    |
| `GET`    | `/articles/{slug}`            | `ArticleApi`             | Get a single article by slug                          |
| `PUT`    | `/articles/{slug}`            | `ArticleApi`             | Update an article (owner only)                        |
| `DELETE` | `/articles/{slug}`            | `ArticleApi`             | Delete an article (owner only)                        |
| `POST`   | `/articles/{slug}/favorite`   | `ArticleFavoriteApi`     | Favorite an article (authenticated)                   |
| `DELETE` | `/articles/{slug}/favorite`   | `ArticleFavoriteApi`     | Unfavorite an article (authenticated)                 |
| `GET`    | `/tags`                       | `TagsApi`                | Get all tags                                          |

### GraphQL Queries & Mutations

| Operation  | Field / Mutation       | Class                  | Description                                                    |
|------------|------------------------|------------------------|----------------------------------------------------------------|
| Query      | `feed`                 | `ArticleDatafetcher`   | User's feed with cursor pagination                             |
| Query      | `articles`             | `ArticleDatafetcher`   | List articles (filter by tag, author, favoritedBy) with cursor |
| Query      | `article(slug)`        | `ArticleDatafetcher`   | Get single article by slug                                     |
| Query      | `tags`                 | `TagDatafetcher`       | Get all tag names                                              |
| Data       | `Profile.feed`         | `ArticleDatafetcher`   | Articles in a specific user's feed                             |
| Data       | `Profile.favorites`    | `ArticleDatafetcher`   | A user's favorited articles                                    |
| Data       | `Profile.articles`     | `ArticleDatafetcher`   | A user's authored articles                                     |
| Data       | `ArticlePayload.article` | `ArticleDatafetcher` | Resolve article after mutation                                 |
| Data       | `Comment.article`      | `ArticleDatafetcher`   | Resolve article from a comment                                 |
| Mutation   | `createArticle`        | `ArticleMutation`      | Create a new article                                           |
| Mutation   | `updateArticle`        | `ArticleMutation`      | Update an article (owner only)                                 |
| Mutation   | `deleteArticle`        | `ArticleMutation`      | Delete an article (owner only)                                 |
| Mutation   | `favoriteArticle`      | `ArticleMutation`      | Favorite an article                                            |
| Mutation   | `unfavoriteArticle`    | `ArticleMutation`      | Unfavorite an article                                          |

---

## 4. Database Tables

All tables are defined in `V1__create_tables.sql`.

### `articles`

| Column       | Type                  | Constraints                        |
|--------------|-----------------------|------------------------------------|
| `id`         | `varchar(255)`        | PRIMARY KEY                        |
| `user_id`    | `varchar(255)`        | FK to `users.id` (implicit, no DDL FK) |
| `slug`       | `varchar(255)`        | UNIQUE                             |
| `title`      | `varchar(255)`        |                                    |
| `description`| `text`                |                                    |
| `body`       | `text`                |                                    |
| `created_at` | `TIMESTAMP NOT NULL`  |                                    |
| `updated_at` | `TIMESTAMP NOT NULL`  | DEFAULT CURRENT_TIMESTAMP          |

### `tags`

| Column | Type           | Constraints   |
|--------|----------------|---------------|
| `id`   | `varchar(255)` | PRIMARY KEY   |
| `name` | `varchar(255)` | NOT NULL      |

### `article_tags` (join table)

| Column       | Type           | Constraints |
|--------------|----------------|-------------|
| `article_id` | `varchar(255)` | NOT NULL    |
| `tag_id`     | `varchar(255)` | NOT NULL    |

### `article_favorites` (join table)

| Column       | Type           | Constraints                          |
|--------------|----------------|--------------------------------------|
| `article_id` | `varchar(255)` | NOT NULL, composite PK               |
| `user_id`    | `varchar(255)` | NOT NULL, composite PK               |

**Note:** The DDL uses no explicit `FOREIGN KEY` constraints. Referential integrity is enforced only at the application layer.

---

## 5. Internal Dependencies

These are dependencies **within** the Articles bounded context itself.

```
ArticlesApi / ArticleApi / ArticleFavoriteApi / TagsApi
        |                   |                     |
        v                   v                     v
ArticleCommandService   ArticleQueryService   TagsQueryService
        |                   |                     |
        v                   v                     v
ArticleRepository       ArticleReadService     TagReadService
        |               ArticleFavoritesReadService
        v                   |
ArticleMapper           (MyBatis XML mappers)
        |
  MyBatisArticleRepository
```

| From                              | To                                  | Relationship                               |
|-----------------------------------|-------------------------------------|--------------------------------------------|
| `ArticlesApi`                     | `ArticleCommandService`             | Delegates article creation                 |
| `ArticlesApi`                     | `ArticleQueryService`               | Delegates article queries                  |
| `ArticleApi`                      | `ArticleQueryService`               | Delegates single-article reads             |
| `ArticleApi`                      | `ArticleCommandService`             | Delegates article updates                  |
| `ArticleApi`                      | `ArticleRepository`                 | Direct lookup for slug-based find + delete |
| `ArticleFavoriteApi`              | `ArticleFavoriteRepository`         | Save/remove favorites                      |
| `ArticleFavoriteApi`              | `ArticleRepository`                 | Lookup article by slug                     |
| `ArticleFavoriteApi`              | `ArticleQueryService`               | Return enriched article data               |
| `TagsApi`                         | `TagsQueryService`                  | Delegates tag listing                      |
| `ArticleCommandService`           | `ArticleRepository`                 | Persists articles                          |
| `ArticleQueryService`             | `ArticleReadService`                | MyBatis read queries                       |
| `ArticleQueryService`             | `ArticleFavoritesReadService`       | Favorite counts and user-favorite checks   |
| `TagsQueryService`                | `TagReadService`                    | MyBatis tag read queries                   |
| `MyBatisArticleRepository`        | `ArticleMapper`                     | MyBatis write mapper                       |
| `MyBatisArticleFavoriteRepository`| `ArticleFavoriteMapper`             | MyBatis write mapper for favorites         |
| `ArticleDatafetcher`              | `ArticleQueryService`               | GraphQL query resolution                   |
| `ArticleMutation`                 | `ArticleCommandService`             | GraphQL mutations (create/update)          |
| `ArticleMutation`                 | `ArticleRepository`                 | Slug lookup, delete                        |
| `ArticleMutation`                 | `ArticleFavoriteRepository`         | Favorite/unfavorite mutations              |

---

## 6. Cross-Domain Dependencies

### 6.1 Article -> User: `userId` Foreign Key

- **Coupling type:** Data reference (implicit FK)
- **Files involved:**
  - `core/article/Article.java` — `userId` field (line 18)
  - `V1__create_tables.sql` — `articles.user_id` column references `users.id`
  - `ArticleMapper.xml` — stores `article.userId` on insert
- **Impact for extraction:** The `userId` is a string-based reference, which is relatively easy to keep as an opaque external ID. No domain entity import of `User` in the Article entity itself. This is already a **loose coupling** pattern. In a microservice world, this field becomes a cross-service ID that must be validated via an API call or event.

### 6.2 ArticleReadService.xml -> `users` Table (Direct JOIN)

- **Coupling type:** Shared database / cross-domain SQL JOIN
- **Files involved:**
  - `ArticleReadService.xml` — `selectArticleData` SQL fragment (line 25): `left join users U on U.id = A.user_id`
  - `ArticleReadService.xml` — `selectArticleIds` SQL fragment (lines 35-36): `left join users AU on AU.id = A.user_id` and `left join users AFU on AFU.id = AF.user_id`
  - `ArticleReadService.xml` — `countArticle` query (lines 71-72): same JOINs
  - `ArticleReadService.xml` — `profileColumns` SQL fragment (lines 4-8): selects `U.id`, `U.username`, `U.bio`, `U.image`
- **Impact for extraction:** This is the **highest-friction coupling point**. The read-side queries directly JOIN the `users` table to embed author profile data (`ProfileData`) into `ArticleData`. In a microservice architecture:
  - The `users` table would not be accessible from the Articles service database.
  - Author profile data must be fetched via an API call or materialized locally via events.
  - The MyBatis XML queries must be rewritten to remove all `users` table JOINs.
  - Filter-by-author (`AU.username = #{author}`) and filter-by-favoritedBy (`AFU.username = #{favoritedBy}`) queries resolve usernames against the `users` table — these would need a username-to-userId resolution service or a local projection.

### 6.3 ArticleFavorite -> User Domain

- **Coupling type:** Data reference (cross-domain join table)
- **Files involved:**
  - `core/favorite/ArticleFavorite.java` — `userId` field
  - `V1__create_tables.sql` — `article_favorites` table with `user_id` column
  - `ArticleFavoritesReadService.xml` — queries `article_favorites` filtered by `user_id`
  - `ArticleFavoriteApi.java` (line 34) — `new ArticleFavorite(article.getId(), user.getId())`
  - `ArticleMutation.java` (line 77) — same pattern in GraphQL
- **Impact for extraction:** The `article_favorites` table is a pure join between Article and User IDs. Ownership is ambiguous (see Section 8). The API controllers access `user.getId()` from the authenticated Spring Security principal (`@AuthenticationPrincipal User user`), which is a User domain type imported directly.

### 6.4 ArticleQueryService -> UserRelationshipQueryService

- **Coupling type:** Service call (cross-domain query dependency)
- **Files involved:**
  - `application/ArticleQueryService.java` (line 27): field `UserRelationshipQueryService userRelationshipQueryService`
  - `ArticleQueryService.java` lines 82, 114: `userRelationshipQueryService.followedUsers(user.getId())` — used by feed endpoints to get the list of user IDs the current user follows
  - `ArticleQueryService.java` lines 134-139: `userRelationshipQueryService.followingAuthors(...)` — checks if the current user follows each article's author
  - `ArticleQueryService.java` line 181: `userRelationshipQueryService.isUserFollowing(...)` — single-article follow check
  - `infrastructure/mybatis/readservice/UserRelationshipQueryService.java` — queries the `follows` table (User domain)
- **Impact for extraction:** This is a **synchronous cross-domain service call**. In a microservice architecture:
  - The feed endpoint requires knowing which users the current user follows — this must become an API call to the User/Profile service or be replaced by event-sourced local state.
  - The "following" flag on each article's author profile is computed per-request — this is a prime candidate for an async event + local cache pattern.

### 6.5 ArticleData -> ProfileData (Embedded DTO)

- **Coupling type:** Embedded cross-domain DTO
- **Files involved:**
  - `application/data/ArticleData.java` (line 27): `@JsonProperty("author") private ProfileData profileData`
  - `application/data/ProfileData.java` — contains `id`, `username`, `bio`, `image`, `following`
  - `ArticleQueryService.java` lines 138, 142-143, 178-182 — reads and mutates `ProfileData` (sets `following` flag)
- **Impact for extraction:** `ProfileData` is a User-domain DTO that is **embedded** inside the Article read model. In a microservice, the Articles service would need to:
  - Define its own `AuthorSummary` DTO.
  - Populate it via an API call to the User/Profile service or from a local materialized view.
  - The `following` boolean must be resolved at query time or via a BFF/API gateway.

### 6.6 AuthorizationService — Shared Across Domains

- **Coupling type:** Shared service (cross-domain utility)
- **Files involved:**
  - `core/service/AuthorizationService.java` — `canWriteArticle(User user, Article article)` and `canWriteComment(User user, Article article, Comment comment)`
  - `api/ArticleApi.java` lines 53, 72 — calls `AuthorizationService.canWriteArticle(user, article)`
  - `graphql/ArticleMutation.java` lines 59, 108 — same
- **Impact for extraction:** This service imports `User`, `Article`, and `Comment` — three different domain types. It must be split:
  - Articles service gets its own `canWriteArticle()` check (comparing `userId` strings).
  - Comments service gets its own `canWriteComment()` check.

### 6.7 GraphQL ArticleDatafetcher -> UserRepository

- **Coupling type:** Direct repository call into User domain
- **Files involved:**
  - `graphql/ArticleDatafetcher.java` (line 40): field `UserRepository userRepository`
  - `ArticleDatafetcher.java` lines 101-103: `userRepository.findByUsername(profile.getUsername())` — resolves a User entity to build a user-specific feed in the `userFeed` GraphQL resolver
- **Impact for extraction:** The GraphQL layer directly calls into the User domain's repository. This must be replaced with a service call or the feed resolution logic moved to a BFF layer.

### 6.8 REST Controllers -> `User` Entity (Authentication Principal)

- **Coupling type:** Type dependency on User domain entity
- **Files involved:**
  - `api/ArticlesApi.java`, `api/ArticleApi.java`, `api/ArticleFavoriteApi.java` — all use `@AuthenticationPrincipal User user`
  - `graphql/SecurityUtil.getCurrentUser()` — returns `Optional<User>`
- **Impact for extraction:** Every authenticated endpoint depends on `io.spring.core.user.User`. In a microservice, the Articles service would receive a JWT or token and extract a userId — it would not need the full `User` domain entity.

### 6.9 GraphQL ArticleDatafetcher -> CommentData

- **Coupling type:** Cross-domain DTO reference (Comment domain)
- **Files involved:**
  - `graphql/ArticleDatafetcher.java` (line 19): imports `io.spring.application.data.CommentData`
  - `ArticleDatafetcher.java` lines 321-340: `getCommentArticle()` resolver extracts `comment.getArticleId()` from `CommentData` local context
- **Impact for extraction:** The Article GraphQL fetcher resolves `Comment.article` — it consumes a Comment-domain DTO. In a microservice, this resolver would live in the Comments service or a GraphQL federation gateway.

---

## 7. Shared Kernel / Anti-Corruption Layer Candidates

### Current Shared Types

| Shared Type                  | Used By                                  | Nature                            |
|-----------------------------|------------------------------------------|-----------------------------------|
| `userId` (String)           | Article, ArticleFavorite, controllers    | Opaque ID reference across domains |
| `ProfileData`               | ArticleData, ArticleQueryService         | User-domain DTO embedded in Article read model |
| `User` entity               | All controllers, GraphQL SecurityUtil    | Full domain entity used as auth principal |
| `AuthorizationService`      | ArticleApi, ArticleMutation, CommentApi  | Static utility spanning Article + Comment + User |
| `CommentData`               | ArticleDatafetcher                       | Comment-domain DTO consumed by Article GraphQL |
| `UserRepository`            | ArticleDatafetcher                       | User-domain repository called from Article GraphQL layer |
| `UserRelationshipQueryService` | ArticleQueryService                   | User-domain query service called from Article application layer |

### Anti-Corruption Layer (ACL) Recommendations

1. **Replace `ProfileData` with a local `AuthorSummary` DTO** in the Articles domain. Populate it via:
   - An API call to the User/Profile service, or
   - A local materialized view updated by `UserProfileUpdated` events.

2. **Replace `User` auth principal with a lightweight `AuthenticatedUser` value object** containing only `userId` (String). Extract from JWT claims at the gateway.

3. **Replace `UserRelationshipQueryService` dependency with an ACL interface** (e.g., `FollowQueryPort`) in the Articles domain, backed by:
   - A synchronous REST/gRPC call to the User/Profile service, or
   - A local `follows` projection updated by domain events.

4. **Split `AuthorizationService`** into per-domain authorization logic:
   - Articles: `ArticleAuthorizationService.canWrite(userId, article)` — purely string comparison.
   - Comments: `CommentAuthorizationService.canWrite(userId, articleOwnerId, comment)`.

5. **Remove direct `UserRepository` dependency** from `ArticleDatafetcher`. Move user-lookup responsibility to a User-domain GraphQL module or a federated schema.

---

## 8. Microservice Extraction Considerations

### Data Ownership Questions

| Table               | Current Owner | Recommended Owner  | Rationale                                                   |
|---------------------|---------------|--------------------|-------------------------------------------------------------|
| `articles`          | Articles      | Articles           | Core Article aggregate data                                 |
| `tags`              | Articles      | Articles           | Tags are children of the Article aggregate                  |
| `article_tags`      | Articles      | Articles           | Join table within the Article aggregate                     |
| `article_favorites` | Ambiguous     | **Articles**       | Favorite counts are a core Article read concern; the join only needs `userId` as an opaque reference. Articles service owns the data; User service can subscribe to `ArticleFavorited` / `ArticleUnfavorited` events if it needs a "my favorites" view. |
| `users`             | User/Profile  | User/Profile       | Not owned by Articles, but currently JOINed in Article read queries |
| `follows`           | User/Profile  | User/Profile       | Used by Articles for feed generation but owned by User/Profile |

### Database Separation Steps

1. **Remove all `users` table JOINs from Article MyBatis XML.**
   - `ArticleReadService.xml`: Remove `left join users U on U.id = A.user_id` from `selectArticleData`.
   - `ArticleReadService.xml`: Remove `left join users AU ...` and `left join users AFU ...` from `selectArticleIds` and `countArticle`.
   - Replace with a post-query enrichment step that calls the User/Profile service.

2. **Replace username-based filters** (`author`, `favoritedBy` in `queryArticles`) with a two-step process:
   - Step 1: Resolve `username -> userId` via User/Profile API.
   - Step 2: Filter by `userId` in the Articles database.

3. **Materialize a local `authors` projection table** (userId, username, bio, image) in the Articles database, updated via `UserProfileUpdated` events. This avoids synchronous calls on every article read.

### API Gateway Needs

- **Authentication translation:** The gateway must extract the authenticated user's ID from the JWT and pass it as a header or claim — the Articles service should not depend on `io.spring.core.user.User`.
- **Feed endpoint orchestration:** The `GET /articles/feed` endpoint currently calls `UserRelationshipQueryService.followedUsers()` then queries articles. Options:
  - **Option A (preferred):** Articles service maintains a local `follows` projection, updated via events. Feed query is self-contained.
  - **Option B:** API gateway / BFF calls User service for followed IDs, then calls Articles service with those IDs.
- **Profile enrichment:** The gateway or a BFF layer can enrich Article responses with author profile data by calling the User/Profile service, removing the need for the Articles service to know about profiles.

### Event-Driven Alternatives

| Current Synchronous Call                             | Replacement Event / Pattern                                  |
|------------------------------------------------------|--------------------------------------------------------------|
| `UserRelationshipQueryService.followedUsers()`       | `UserFollowed` / `UserUnfollowed` events -> local `follows` projection in Articles DB |
| `UserRelationshipQueryService.isUserFollowing()`     | Same local projection lookup                                 |
| `UserRelationshipQueryService.followingAuthors()`    | Same local projection lookup                                 |
| `users` table JOIN for author profile                | `UserProfileUpdated` event -> local `authors` projection     |
| Username-based article filters                       | Local `authors` projection provides username -> userId mapping |

### Key Challenges

1. **Read-model denormalization:** The biggest challenge is the `ArticleReadService.xml` which tightly couples Article and User data in SQL JOINs. Every query that returns `ArticleData` includes author profile information from the `users` table. This requires either:
   - Local materialized views (eventual consistency trade-off), or
   - Per-request API calls to the User service (latency trade-off).

2. **Feed generation latency:** The feed endpoint currently does two synchronous DB queries (get followed users, then get their articles). With microservices, this becomes cross-service communication. A local `follows` projection is strongly recommended.

3. **Consistency of favorite counts:** If `article_favorites` is owned by Articles, unfavoriting must be initiated through the Articles service. The User service's "my favorites" view becomes eventually consistent.

4. **GraphQL federation:** The `ArticleDatafetcher` currently resolves `Profile.feed`, `Profile.favorites`, `Profile.articles`, and `Comment.article`. In a federated GraphQL schema, these resolvers must be split across services or handled by a composition layer.

5. **Authorization decoupling:** `AuthorizationService` is trivial (string ID comparison) but currently imports three domain types. Splitting it is straightforward but must be done as part of extraction.

6. **Slug uniqueness across instances:** Article slugs are derived from titles and stored as UNIQUE. In a distributed system, slug generation and uniqueness checks remain local to the Articles service — no cross-service concern.

### Recommended Extraction Order

1. **Phase 1 — Decouple auth principal:** Replace `User` entity with a lightweight auth context (userId only).
2. **Phase 2 — Introduce local projections:** Create `authors` and `follows` projection tables in the Articles DB; publish/consume domain events.
3. **Phase 3 — Rewrite read queries:** Remove all `users` table JOINs from MyBatis XML; use local projections instead.
4. **Phase 4 — Extract service:** Separate Articles into its own deployable with its own database (articles, tags, article_tags, article_favorites, authors projection, follows projection).
5. **Phase 5 — GraphQL federation:** Split GraphQL resolvers across services or introduce a federated gateway.
