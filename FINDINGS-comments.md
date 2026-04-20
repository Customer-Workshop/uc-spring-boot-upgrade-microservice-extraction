# Comments Bounded Context - Domain Boundary Analysis

## 1. Domain Overview

The **Comments** bounded context manages the lifecycle of user-generated comments on articles within the Conduit social blogging platform. It is responsible for:

- **Creating** comments on articles (authenticated users only)
- **Reading** comments for a given article (with author profile enrichment and follow-status resolution)
- **Deleting** comments (with authorization checks against both article and comment ownership)

Comments are always scoped to an article and authored by a user, making this context inherently dependent on both the **Articles** and **Users/Profiles** bounded contexts.

---

## 2. Entities & Aggregates

### 2.1 Comment Entity

| Field       | Type       | Description                                      |
|-------------|------------|--------------------------------------------------|
| `id`        | `String`   | UUID, generated at construction time              |
| `body`      | `String`   | The comment text content                          |
| `userId`    | `String`   | Foreign reference to the authoring user's ID      |
| `articleId` | `String`   | Foreign reference to the parent article's ID      |
| `createdAt` | `DateTime` | Joda `DateTime`, set at construction time         |

**Source:** `src/main/java/io/spring/core/comment/Comment.java`

The `Comment` entity is the sole aggregate in this bounded context. It has no child entities or value objects. The entity uses Lombok `@Getter`, `@NoArgsConstructor`, and `@EqualsAndHashCode(of = "id")`.

### 2.2 CommentData (Read-Model DTO)

| Field         | Type          | Description                                           |
|---------------|---------------|-------------------------------------------------------|
| `id`          | `String`      | Comment identifier                                    |
| `body`        | `String`      | Comment text                                          |
| `articleId`   | `String`      | Parent article ID (`@JsonIgnore` - not serialized)    |
| `createdAt`   | `DateTime`    | Creation timestamp                                    |
| `updatedAt`   | `DateTime`    | Last update timestamp                                 |
| `profileData` | `ProfileData` | Embedded author profile (serialized as `"author"`)    |

**Source:** `src/main/java/io/spring/application/data/CommentData.java`

Implements the `Node` interface for cursor-based pagination, exposing `getCursor()` which returns a `DateTimeCursor` based on `createdAt`.

### 2.3 ProfileData (Cross-Boundary - Users Domain)

| Field       | Type      | Description                                        |
|-------------|-----------|----------------------------------------------------|
| `id`        | `String`  | User ID (`@JsonIgnore`)                            |
| `username`  | `String`  | Display username                                   |
| `bio`       | `String`  | User biography                                     |
| `image`     | `String`  | Avatar URL                                         |
| `following` | `boolean` | Whether the current user follows this author       |

**Source:** `src/main/java/io/spring/application/data/ProfileData.java`

This DTO originates from the Users/Profiles domain and is embedded directly into `CommentData`, creating a tight read-model coupling.

---

## 3. API Endpoints

### 3.1 REST API

All REST endpoints are defined in `src/main/java/io/spring/api/CommentsApi.java`, mapped under `/articles/{slug}/comments`.

| Method   | Path                                | Auth Required | Description                          |
|----------|-------------------------------------|---------------|--------------------------------------|
| `POST`   | `/articles/{slug}/comments`         | Yes           | Create a new comment on an article   |
| `GET`    | `/articles/{slug}/comments`         | Optional      | List all comments for an article     |
| `DELETE` | `/articles/{slug}/comments/{id}`    | Yes           | Delete a specific comment            |

**Request body (POST):**
```json
{
  "comment": {
    "body": "string (required, not blank)"
  }
}
```

**Authorization logic (DELETE):** Uses `AuthorizationService.canWriteComment(user, article, comment)` which permits deletion if the authenticated user is either the article owner OR the comment author.

### 3.2 GraphQL API

Defined across two DGS components:

**CommentMutation** (`src/main/java/io/spring/graphql/CommentMutation.java`):

| Mutation         | Arguments               | Returns           | Description            |
|------------------|-------------------------|-------------------|------------------------|
| `addComment`     | `slug: String!, body: String!` | `CommentPayload` | Create a comment       |
| `deleteComment`  | `slug: String!, id: ID!`       | `DeletionStatus` | Delete a comment       |

**CommentDatafetcher** (`src/main/java/io/spring/graphql/CommentDatafetcher.java`):

| Field               | Parent Type      | Arguments                                     | Returns               |
|---------------------|------------------|-----------------------------------------------|-----------------------|
| `comment`           | `CommentPayload` | (none)                                        | `Comment`             |
| `comments`          | `Article`        | `first`, `after`, `last`, `before` (cursor)   | `CommentsConnection`  |

**GraphQL Schema Types** (from `src/main/resources/schema/schema.graphqls`):

```graphql
type Comment {
  id: ID!
  body: String!
  createdAt: String!
  updatedAt: String!
  author: Profile!
}

type CommentEdge {
  cursor: String
  node: Comment
}

type CommentsConnection {
  edges: [CommentEdge]
  pageInfo: PageInfo!
}

type CommentPayload {
  comment: Comment
}
```

---

## 4. Application Services

### 4.1 CommentQueryService (CQRS Read Side)

**Source:** `src/main/java/io/spring/application/CommentQueryService.java`

| Method                          | Parameters                                    | Returns                      | Description                                              |
|---------------------------------|-----------------------------------------------|------------------------------|----------------------------------------------------------|
| `findById(id, user)`           | Comment ID, authenticated user                | `Optional<CommentData>`      | Single comment with author follow-status enrichment      |
| `findByArticleId(articleId, user)` | Article ID, authenticated user             | `List<CommentData>`          | All comments for an article with batch follow-status     |
| `findByArticleIdWithCursor(articleId, user, page)` | Article ID, user, cursor params | `CursorPager<CommentData>`   | Cursor-based paginated comments with follow-status       |

**Dependencies injected:**
- `CommentReadService` - MyBatis mapper for read queries
- `UserRelationshipQueryService` - MyBatis mapper for resolving follow relationships (cross-boundary dependency on Users domain)

**Key behavior:** All three methods enrich the returned `CommentData` by resolving whether the current user follows each comment's author. This is done via `UserRelationshipQueryService.isUserFollowing()` (single comment) or `UserRelationshipQueryService.followingAuthors()` (batch for multiple comments).

---

## 5. Data Storage

### 5.1 Database Table: `comments`

**Source:** `src/main/resources/db/migration/V1__create_tables.sql` (lines 42-49)

```sql
create table comments (
  id varchar(255) primary key,
  body text,
  article_id varchar(255),
  user_id varchar(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

| Column       | Type           | Constraints              | Notes                                      |
|--------------|----------------|--------------------------|--------------------------------------------|
| `id`         | `varchar(255)` | `PRIMARY KEY`            | UUID generated by application              |
| `body`       | `text`         |                          | Comment content                            |
| `article_id` | `varchar(255)` |                          | FK to `articles.id` (no DB constraint)     |
| `user_id`    | `varchar(255)` |                          | FK to `users.id` (no DB constraint)        |
| `created_at` | `TIMESTAMP`    | `NOT NULL`               | Set by application at creation time        |
| `updated_at` | `TIMESTAMP`    | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Defaults to creation time        |

**Notable:** There are no foreign key constraints enforced at the database level for `article_id` or `user_id`. Referential integrity is maintained purely at the application level.

### 5.2 Write Mapper: CommentMapper

**Interface:** `src/main/java/io/spring/infrastructure/mybatis/mapper/CommentMapper.java`
**XML:** `src/main/resources/mapper/CommentMapper.xml`

| Operation  | SQL                                                        |
|------------|------------------------------------------------------------|
| `insert`   | `INSERT INTO comments (id, body, user_id, article_id, created_at, updated_at)` |
| `findById` | `SELECT ... FROM comments WHERE id = ? AND article_id = ?` |
| `delete`   | `DELETE FROM comments WHERE id = ?`                        |

### 5.3 Read Mapper: CommentReadService

**Interface:** `src/main/java/io/spring/infrastructure/mybatis/readservice/CommentReadService.java`
**XML:** `src/main/resources/mapper/CommentReadService.xml`

| Query                       | SQL Pattern                                                                     |
|-----------------------------|---------------------------------------------------------------------------------|
| `findById`                  | `SELECT ... FROM comments C LEFT JOIN users U ON C.user_id = U.id WHERE C.id = ?` |
| `findByArticleId`           | Same join, `WHERE C.article_id = ?`                                             |
| `findByArticleIdWithCursor` | Same join with cursor-based pagination (`created_at < ?` or `> ?`)              |

**Cross-boundary SQL:** The read queries perform a `LEFT JOIN` on the `users` table to fetch author profile data (`username`, `bio`, `image`) inline. This is a direct SQL-level coupling to the Users domain's table.

### 5.4 Repository Implementation

**Source:** `src/main/java/io/spring/infrastructure/repository/MyBatisCommentRepository.java`

Implements `CommentRepository` by delegating to `CommentMapper`. Straightforward pass-through with `Optional` wrapping for `findById`.

---

## 6. Cross-Boundary Dependencies

### 6.1 Comments -> Articles Domain

| Coupling Point | Location | Description |
|---|---|---|
| `Comment.articleId` field | `core/comment/Comment.java:16` | Direct foreign key reference to an Article by ID |
| `CommentsApi` injects `ArticleRepository` | `api/CommentsApi.java:36` | All REST endpoints first resolve the article by slug via `articleRepository.findBySlug(slug)` |
| `CommentMutation` injects `ArticleRepository` | `graphql/CommentMutation.java:27` | GraphQL mutations also resolve articles by slug |
| `CommentRepository.findById(articleId, commentId)` | `core/comment/CommentRepository.java:8` | The repository interface itself requires `articleId` as a parameter |
| `CommentMapper.findById` SQL | `mapper/CommentMapper.xml:26` | SQL queries filter by both `id` AND `article_id` |
| Nested URL pattern | `api/CommentsApi.java:33` | REST path `/articles/{slug}/comments` nests comments under articles |
| `AuthorizationService.canWriteComment()` | `core/service/AuthorizationService.java:12` | Takes an `Article` parameter to check article ownership |
| `CommentDatafetcher.articleComments()` | `graphql/CommentDatafetcher.java:50` | GraphQL resolves comments as a field on the `Article` type |

**Impact:** The Comments domain cannot function independently without the ability to resolve articles by slug and verify article existence. Every write and read operation begins with an article lookup.

### 6.2 Comments -> Users/Profiles Domain

| Coupling Point | Location | Description |
|---|---|---|
| `Comment.userId` field | `core/comment/Comment.java:15` | Direct foreign key reference to the authoring user |
| `@AuthenticationPrincipal User user` | `api/CommentsApi.java:43,55,71` | REST endpoints accept the `User` domain entity directly |
| `CommentQueryService` -> `UserRelationshipQueryService` | `application/CommentQueryService.java:21` | Injected dependency to resolve follow relationships |
| `CommentData` embeds `ProfileData` | `application/data/CommentData.java:23` | Read model directly contains the Users domain DTO |
| SQL `LEFT JOIN users` | `mapper/CommentReadService.xml:12-13` | Read queries join on the `users` table to fetch author profile data |
| `AuthorizationService.canWriteComment()` | `core/service/AuthorizationService.java:12-13` | Takes a `User` parameter and checks `user.getId().equals(comment.getUserId())` |

**Impact:** The Comments read model is deeply coupled to the Users domain. Every comment query joins the `users` table, and the follow-status enrichment requires calling into the Users domain's query service.

### 6.3 Shared / Cross-Cutting Services

| Service | Location | Domains Involved | Description |
|---|---|---|---|
| `AuthorizationService.canWriteComment(User, Article, Comment)` | `core/service/AuthorizationService.java:12-14` | Users, Articles, Comments | Spans all three domains: checks if user is article owner OR comment owner |
| Spring Security / JWT authentication | `api/security/` | Users, Comments | JWT filter resolves `User` entity from token, injected into comment endpoints |
| `SecurityUtil.getCurrentUser()` | `graphql/SecurityUtil.java` | Users, Comments | GraphQL equivalent of `@AuthenticationPrincipal` |

### 6.4 Dependency Direction Summary

```
                 +-----------+
                 |   Users   |
                 | /Profiles |
                 +-----^-----+
                       |
          (userId, ProfileData, follow-status)
                       |
+----------+     +-----+-----+
| Articles | <-- | Comments  |
+----------+     +-----------+
  (articleId, slug resolution,
   article ownership check)
```

Comments depends on both Articles and Users but neither Articles nor Users depend on Comments. This makes Comments a **leaf context** in the dependency graph.

---

## 7. Recommendations for Microservice Extraction

### 7.1 Slug Resolution Strategy

**Problem:** All comment endpoints use `/articles/{slug}/comments`, requiring a slug-to-articleId resolution that currently calls `ArticleRepository.findBySlug()`.

**Options:**

| Approach | Pros | Cons |
|---|---|---|
| **API Gateway resolves slug** | Comments service only deals with article IDs; clean separation | Gateway needs article lookup logic; adds latency |
| **Comments service caches slug->ID mapping** | Fast lookups; no runtime dependency on Articles service | Stale cache risk; needs invalidation strategy |
| **Synchronous API call to Articles service** | Always fresh data; simple implementation | Runtime coupling; cascading failures; latency |
| **Event-driven slug materialization** | Comments service maintains its own slug->ID mapping table updated via events | Eventually consistent; best long-term decoupling |

**Recommendation:** Use an **event-driven approach**. The Articles service publishes `ArticleCreated`, `ArticleUpdated` (slug changes), and `ArticleDeleted` events. The Comments service maintains a local `article_slugs` lookup table. This eliminates the synchronous dependency while keeping the nested URL pattern intact.

### 7.2 Remove Direct ArticleRepository Dependency

**Current state:** Both `CommentsApi` and `CommentMutation` directly inject and call `ArticleRepository`.

**Target state:** Replace with either:
1. An **Articles API client** (synchronous) for slug resolution, or
2. A **local projection table** populated by Article domain events (preferred).

The `CommentRepository.findById(articleId, commentId)` interface should be simplified to `findById(commentId)` once the service owns its own article reference data.

### 7.3 Decompose AuthorizationService.canWriteComment()

**Problem:** `canWriteComment(User, Article, Comment)` spans all three domains. It checks:
- `user.getId().equals(article.getUserId())` - Is the user the article author?
- `user.getId().equals(comment.getUserId())` - Is the user the comment author?

**Recommendation:** Split into two checks:
1. **Comments service** checks comment ownership locally: `user.getId().equals(comment.getUserId())`
2. **Article ownership check** is handled by either:
   - Storing `article.userId` in the Comments service's local article projection (from `ArticleCreated` events)
   - Making a synchronous call to the Articles service (less preferred)

This eliminates the need to pass a full `Article` entity into the Comments domain.

### 7.4 Decouple User/Profile Enrichment

**Problem:** `CommentReadService` SQL joins the `users` table directly, and `CommentQueryService` calls `UserRelationshipQueryService` for follow-status.

**Recommendation:**
1. **Author profile data:** The Comments service should store a **denormalized author snapshot** (username, bio, image) updated via `UserUpdated` events. This replaces the SQL JOIN on `users`.
2. **Follow-status enrichment:** Call a **Users/Profiles API** at query time, or use a **Backend-for-Frontend (BFF)** pattern where the API gateway/aggregator composes comment data with follow-status from the Profiles service.
3. **Alternative:** Accept eventual consistency and let the frontend make a separate call to the Profiles service for follow-status.

### 7.5 Handle Cascade Deletion via Events

**Problem:** When an article is deleted, its comments should also be deleted. Currently this may rely on application-level cleanup or is not explicitly handled (no DB-level CASCADE constraint exists).

**Recommendation:** The Articles service publishes an `ArticleDeleted` event. The Comments service subscribes and performs `DELETE FROM comments WHERE article_id = ?`. This is a natural fit for event-driven architecture and maintains data consistency without synchronous coupling.

### 7.6 Database Separation

**Current state:** The `comments` table shares a SQLite database with `users`, `articles`, and all other tables. Read queries join across tables.

**Target state:**
- Comments service owns its own database containing:
  - `comments` table (unchanged)
  - `article_references` table (id, slug, user_id - populated via events)
  - `author_profiles` table (user_id, username, bio, image - populated via events)
- All cross-table JOINs are replaced with local lookups against projection tables.

### 7.7 API Path Considerations

The nested REST path `/articles/{slug}/comments` can be preserved in two ways:

1. **API Gateway routing:** Gateway routes `/articles/{slug}/comments` to the Comments service, passing the resolved `articleId` as a header or query parameter.
2. **Comments service owns the path:** The Comments service handles slug resolution internally using its local article reference data.

For GraphQL, the `Article.comments` field resolver would be handled by **schema stitching** or **federation**, where the Comments service extends the `Article` type with a `comments` field.

### 7.8 Summary of Required Events

| Event | Publisher | Consumer (Comments) | Action |
|---|---|---|---|
| `ArticleCreated` | Articles | Comments | Insert into `article_references` |
| `ArticleUpdated` | Articles | Comments | Update slug/userId in `article_references` |
| `ArticleDeleted` | Articles | Comments | Delete from `article_references`; cascade-delete comments |
| `UserUpdated` | Users | Comments | Update author profile snapshot |
| `UserDeleted` | Users | Comments | Anonymize or delete comments by that user |

### 7.9 Migration Sequence

1. **Phase 1 - Introduce event contracts:** Define domain event schemas for Articles and Users. Publish events from the monolith alongside existing direct calls.
2. **Phase 2 - Build Comments projection tables:** Add `article_references` and `author_profiles` tables within the monolith. Populate via event listeners. Migrate read queries to use local projections instead of JOINs.
3. **Phase 3 - Extract Comments service:** Move comment code to a standalone Spring Boot service with its own database. Replace event listeners with message broker consumers (e.g., Kafka, RabbitMQ). Replace direct `ArticleRepository` calls with local projection lookups.
4. **Phase 4 - Wire API gateway:** Route `/articles/{slug}/comments` to the new Comments service. Implement GraphQL federation for the `Article.comments` field.
5. **Phase 5 - Remove monolith comment code:** Delete comment-related code from the monolith. Drop the `comments` table from the monolith database.
