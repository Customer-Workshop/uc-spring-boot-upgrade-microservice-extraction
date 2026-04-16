# Comments Bounded Context — Domain Boundary Analysis

## 1. Domain Overview

The **Comments** bounded context manages user-submitted comments on articles in the RealWorld application. It is responsible for:

- Creating comments on articles (authenticated users only)
- Listing comments for a given article (with author profile enrichment and follow-status resolution)
- Deleting comments (with authorization — comment owner OR article owner may delete)
- Cursor-based pagination of comments per article

Comments is a **small but highly coupled** domain. It has no standalone identity — every comment exists in the context of an Article and is authored by a User. This makes it the most entangled bounded context in the monolith and the hardest candidate for microservice extraction.

---

## 2. Entities & Aggregates

### Comment Entity

**File:** `src/main/java/io/spring/core/comment/Comment.java`

| Field       | Type       | Description                          |
|-------------|------------|--------------------------------------|
| `id`        | `String`   | UUID, generated at creation time     |
| `body`      | `String`   | Comment text content                 |
| `userId`    | `String`   | FK — references `users.id`           |
| `articleId` | `String`   | FK — references `articles.id`        |
| `createdAt` | `DateTime` | Joda-Time timestamp, set at creation |

**Constructor:** `Comment(String body, String userId, String articleId)` — requires both cross-domain identifiers at creation time.

### Aggregate Boundary Discussion

Comment is **not a true aggregate root** in DDD terms. It behaves more like a **child entity of Article**:

- `CommentRepository.findById(articleId, id)` requires the parent article's ID — the comment cannot be located by its own ID alone at the repository level
- All REST endpoints are nested under `/articles/{slug}/comments` — comments have no independent URL namespace
- The database schema uses `article_id` as a non-nullable column (logically, though no explicit FK constraint in the DDL)
- Deletion authorization checks the article owner in addition to the comment owner

However, the read-side (`CommentReadService.findById`) can look up a comment by its `id` alone, suggesting partial independence on the query path.

**Verdict:** Comment is a **dependent entity** within the Article aggregate boundary, but with enough query-side independence to potentially become its own aggregate root if the `articleId` requirement in `CommentRepository.findById` were relaxed.

---

## 3. API Endpoints

### REST Endpoints

**Controller:** `src/main/java/io/spring/api/CommentsApi.java`
**Base path:** `/articles/{slug}/comments`

| Method   | Path                              | Description                        | Auth Required |
|----------|-----------------------------------|------------------------------------|---------------|
| `POST`   | `/articles/{slug}/comments`       | Create a comment on an article     | Yes           |
| `GET`    | `/articles/{slug}/comments`       | List all comments for an article   | No (optional) |
| `DELETE` | `/articles/{slug}/comments/{id}`  | Delete a specific comment          | Yes           |

**Request body (POST):**
```json
{
  "comment": {
    "body": "string (required, not blank)"
  }
}
```

Defined via inline class `NewCommentParam` in `CommentsApi.java` (lines 96–102), annotated with `@JsonRootName("comment")`.

### GraphQL Endpoints

**Query — CommentDatafetcher:** `src/main/java/io/spring/graphql/CommentDatafetcher.java`

| Parent Type      | Field       | Description                                      |
|------------------|-------------|--------------------------------------------------|
| `CommentPayload` | `comment`   | Resolves a single comment after mutation          |
| `Article`        | `comments`  | Relay-style paginated comments connection (`first`/`after`, `last`/`before`) |

**Mutations — CommentMutation:** `src/main/java/io/spring/graphql/CommentMutation.java`

| Mutation         | Arguments          | Description                                         |
|------------------|--------------------|-----------------------------------------------------|
| `addComment`     | `slug`, `body`     | Create a comment on an article (requires auth)      |
| `deleteComment`  | `slug`, `id`       | Delete a comment (requires auth + authorization)    |

Both GraphQL mutations resolve the article by slug via `ArticleRepository` — same coupling as REST.

---

## 4. Database Tables

**Migration:** `src/main/resources/db/migration/V1__create_tables.sql` (lines 42–49)

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

**Observations:**
- `article_id` and `user_id` are **not declared as formal foreign keys** (no `REFERENCES` clause), but are logical FKs to `articles.id` and `users.id`
- `updated_at` has a default value but the application sets it to `createdAt` on insert (see `CommentMapper.xml` line 12) — effectively unused for updates since comments are immutable (no update endpoint exists)
- No indexes beyond the primary key — potential performance concern for `findByArticleId` queries at scale

### Related Tables (owned by other domains)

| Table      | Relationship                       | Domain  |
|------------|------------------------------------|---------|
| `articles` | `comments.article_id` → `articles.id` | Article |
| `users`    | `comments.user_id` → `users.id`       | User    |

---

## 5. Internal Dependencies

The Comments domain is small enough that internal dependencies are minimal:

```
CommentRepository (core)
    └── implemented by MyBatisCommentRepository (infrastructure)
            └── delegates to CommentMapper (infrastructure)
                    └── mapped by CommentMapper.xml

CommentReadService (infrastructure, read-side)
    └── mapped by CommentReadService.xml
            └── result mapped by TransferData.xml (commentData resultMap)

CommentQueryService (application)
    ├── uses CommentReadService (read-side queries)
    └── uses UserRelationshipQueryService (CROSS-DOMAIN — see Section 6)

CommentsApi (api)
    ├── uses CommentRepository (write-side)
    ├── uses CommentQueryService (read-side)
    └── uses ArticleRepository (CROSS-DOMAIN — see Section 6)
```

**Write path:** `CommentsApi` → `CommentRepository` → `MyBatisCommentRepository` → `CommentMapper` → `CommentMapper.xml`

**Read path:** `CommentsApi` → `CommentQueryService` → `CommentReadService` → `CommentReadService.xml` → `TransferData.xml`

---

## 6. Cross-Domain Dependencies

This is the **most coupled domain** in the codebase. Every operation requires data from at least one other bounded context.

### 6.1 Comment → Article: Entity Reference

- **Type:** Entity foreign key
- **Files:** `core/comment/Comment.java` (field `articleId`), `comments` table (`article_id` column)
- **Impact:** Every Comment entity stores an `articleId`. The Comment cannot exist without an Article.
- **Extraction change:** Replace direct `articleId` storage with a locally-managed article reference, potentially synchronized via domain events.

### 6.2 Comment → Article: Repository Coupling

- **Type:** Repository method signature dependency
- **Files:** `core/comment/CommentRepository.java` — `findById(String articleId, String id)`; `CommentMapper.xml` — SQL filters on both `id` and `article_id`
- **Impact:** The write-side repository **requires** the article ID to look up a comment. This is architecturally unusual — most repositories allow lookup by entity ID alone.
- **Extraction change:** Either relax `findById` to accept only comment ID, or maintain a local article-comment mapping in the Comments microservice.

### 6.3 CommentsApi → ArticleRepository: Direct Controller Dependency

- **Type:** Service/repository dependency (direct import)
- **Files:** `api/CommentsApi.java` — imports and injects `io.spring.core.article.ArticleRepository`; every endpoint calls `articleRepository.findBySlug(slug)`
- **Impact:** The Comments REST controller cannot function without the Article domain's repository. All three endpoints (POST, GET, DELETE) resolve the article by slug before performing any comment operation.
- **Extraction change:** Replace `ArticleRepository` dependency with a synchronous call to an Article Service API, or maintain a local slug→articleId lookup table populated via events.

### 6.4 CommentsApi → URL Nesting Under /articles/{slug}

- **Type:** URL/routing coupling
- **Files:** `api/CommentsApi.java` — `@RequestMapping(path = "/articles/{slug}/comments")`
- **Impact:** Comments have no independent REST namespace. Every comment URL includes the parent article's slug.
- **Extraction change:** Introduce an API Gateway that routes `/articles/{slug}/comments` to the Comments microservice, or give Comments its own endpoint (e.g., `/comments?articleId=...`) and use the gateway to maintain backward compatibility.

### 6.5 Comment → User: Entity Reference

- **Type:** Entity foreign key
- **Files:** `core/comment/Comment.java` (field `userId`), `comments` table (`user_id` column)
- **Impact:** Every Comment stores the authoring user's ID.
- **Extraction change:** `userId` can remain as an opaque reference (stable identifier). No structural change needed if the Comments service treats it as an external ID.

### 6.6 CommentReadService.xml → users Table: SQL JOIN

- **Type:** SQL join / infrastructure-level coupling
- **Files:** `src/main/resources/mapper/CommentReadService.xml` (line 12–13) — `left join users U on C.user_id = U.id`
- **Impact:** The read-side query directly joins the `users` table to fetch comment author profile data (username, bio, image). This is a **database-level coupling** — the Comments read model cannot function without access to the `users` table.
- **Extraction change:** Maintain a local `comment_authors` projection table in the Comments database, populated via User domain events (e.g., `UserProfileUpdated`). Or call a User Service API at query time.

### 6.7 CommentReadService.xml → ArticleReadService.xml: Shared SQL Fragment

- **Type:** Shared infrastructure artifact (MyBatis SQL fragment)
- **Files:** `CommentReadService.xml` (line 10) — `<include refid="io.spring.infrastructure.mybatis.readservice.ArticleReadService.profileColumns"/>`; defined in `ArticleReadService.xml` (lines 4–9)
- **Impact:** The Comments read mapper reuses a SQL fragment (`profileColumns`) defined in the Article domain's mapper XML. This creates a compile-time/build-time dependency between mapper files across domain boundaries.
- **Extraction change:** Duplicate the `profileColumns` fragment into the Comments mapper XML (or into a shared infrastructure module), or replace with a local definition since the fragment is simple (4 column aliases).

### 6.8 CommentData → ProfileData: Shared DTO

- **Type:** Shared data transfer object
- **Files:** `application/data/CommentData.java` — embeds `ProfileData` as `@JsonProperty("author")`; `application/data/ProfileData.java` — shared DTO from User domain
- **Impact:** The Comments read model directly embeds a User-domain DTO. The `ProfileData` type is shared across Articles, Comments, and User contexts.
- **Extraction change:** Define a Comments-owned `AuthorProfile` DTO within the Comments bounded context. Map from the shared `ProfileData` at the anti-corruption layer boundary.

### 6.9 CommentQueryService → UserRelationshipQueryService: Cross-Domain Service Call

- **Type:** Direct service/query dependency
- **Files:** `application/CommentQueryService.java` — imports and injects `UserRelationshipQueryService`; calls `isUserFollowing()` (line 31) and `followingAuthors()` (lines 41, 64)
- **Impact:** **Every** query method in `CommentQueryService` calls into the User domain to determine whether the current user follows each comment author. This is the most pervasive cross-domain dependency — it affects all read paths.
- **Extraction change:** Replace with a User Service API call, or adopt event-driven replication where the Comments service maintains a local `follows` projection. Alternatively, defer follow-status resolution to the API gateway or client via a separate call.

### 6.10 AuthorizationService.canWriteComment: Tri-Domain Authorization

- **Type:** Cross-domain authorization logic
- **Files:** `core/service/AuthorizationService.java` — `canWriteComment(User user, Article article, Comment comment)` (line 12–14); called from `CommentsApi.java` (line 78) and `CommentMutation.java` (line 60)
- **Impact:** Authorization for comment deletion requires data from **all three domains**: User (current user ID), Article (article owner ID), and Comment (comment owner ID). The logic: a user can delete a comment if they own the article OR own the comment.
- **Extraction change:** This is a **distributed authorization** problem. Options: (a) the Comments service stores both `articleOwnerId` and `commentOwnerId` locally (denormalized), (b) the API gateway or an Authorization service makes the decision after querying both Article and Comment services, or (c) adopt a claims-based approach where the auth token carries ownership context.

### 6.11 GraphQL Layer: CommentMutation → ArticleRepository

- **Type:** Service/repository dependency (direct import)
- **Files:** `graphql/CommentMutation.java` — imports and injects `ArticleRepository`; both `createComment` (line 36) and `removeComment` (line 55) call `articleRepository.findBySlug(slug)`
- **Impact:** Same coupling as the REST layer — the GraphQL mutation handler directly depends on the Article domain's repository.
- **Extraction change:** Same as 6.3 — replace with Article Service API call.

### 6.12 TransferData.xml: Shared ResultMap Configuration

- **Type:** Shared MyBatis configuration
- **Files:** `src/main/resources/mapper/TransferData.xml` — defines `commentData` resultMap (lines 32–39) alongside `articleData` and `profileData` resultMaps
- **Impact:** The Comment read model's result mapping is co-located with Article and User result mappings in a single shared XML file. The `commentData` resultMap references the shared `profileData` resultMap via `<association>`.
- **Extraction change:** Move `commentData` resultMap to a Comments-owned mapper configuration. Duplicate or reference a local `profileData` resultMap definition.

---

## 7. Shared Kernel / Anti-Corruption Layer Candidates

### 7.1 `profileColumns` SQL Fragment

- **Current state:** Defined in `ArticleReadService.xml`, consumed by both `ArticleReadService.xml` and `CommentReadService.xml`
- **Recommendation:** Extract into a **shared infrastructure kernel** module, or simply duplicate the 4-line fragment into each consumer. Given its simplicity (`U.id userId, U.username userUsername, U.bio userBio, U.image userImage`), duplication is acceptable and reduces coupling.

### 7.2 `AuthorizationService`

- **Current state:** Static utility class in `core/service/` spanning User, Article, and Comment domains
- **Recommendation:** This is a strong candidate for a **dedicated Authorization Service** or **policy engine** in a microservices architecture. In the interim, the Comments service could own `canWriteComment` logic locally if it maintains denormalized `articleOwnerId` alongside each comment.

### 7.3 `ProfileData` as Shared Type

- **Current state:** Used by Articles (author profile), Comments (comment author profile), and User domain
- **Recommendation:** `ProfileData` is a classic **shared kernel** type. For microservice extraction, each service should own its own profile DTO (Anti-Corruption Layer pattern). The Comments service would define `CommentAuthorProfile` and map from the User service's response.

### 7.4 `articleId` / `userId` as Stable References

- **Current state:** Both are UUID strings stored directly in the `comments` table
- **Recommendation:** These are already **stable identifiers** suitable for cross-service references. In a microservices world, `articleId` and `userId` become opaque foreign references — no schema change needed in the Comments database. The key change is how these IDs are resolved (service calls or local projections instead of JOINs).

### 7.5 `TransferData.xml` Shared ResultMap Registry

- **Current state:** Single XML file containing result maps for Article, Comment, and Profile data
- **Recommendation:** Split into per-domain result map files during extraction. Each microservice owns its MyBatis configuration entirely.

---

## 8. Microservice Extraction Considerations

The Comments domain is the **hardest context to extract** from this monolith due to its deep coupling with both Articles and Users. Below is a phased extraction strategy.

### 8.1 Key Challenges

| Challenge | Detail |
|-----------|--------|
| **URL nesting** | All REST endpoints live under `/articles/{slug}/comments`. A standalone Comments service has no article slug resolution capability. |
| **Repository coupling** | `CommentRepository.findById` requires `articleId` — the service cannot look up its own entities independently. |
| **Controller → ArticleRepository** | Both REST and GraphQL handlers directly call `ArticleRepository.findBySlug()` to resolve articles before any comment operation. |
| **Tri-domain authorization** | `canWriteComment` needs User, Article, AND Comment data simultaneously. |
| **Query-time profile enrichment** | Every read path calls `UserRelationshipQueryService` to check follow status for comment authors. |
| **SQL JOINs across domains** | `CommentReadService.xml` joins `users` table directly. |
| **Shared SQL fragments** | `profileColumns` fragment is defined in Article domain's mapper XML. |
| **Shared DTOs** | `CommentData` embeds `ProfileData` from the User domain. |

### 8.2 Recommended Extraction Strategy

#### Phase 1: Decouple at the Code Level (Pre-Extraction)

1. **Relax `CommentRepository.findById`** — allow lookup by comment ID alone (remove `articleId` requirement). Update `CommentMapper.xml` accordingly.
2. **Extract `profileColumns`** — duplicate the SQL fragment into `CommentReadService.xml`.
3. **Introduce `CommentAuthorProfile`** — create a Comments-owned DTO to replace direct `ProfileData` embedding.
4. **Move `canWriteComment` logic** — internalize authorization in the Comments domain, storing `articleOwnerId` as a denormalized field on Comment.
5. **Split `TransferData.xml`** — move `commentData` resultMap to a Comments-owned file.

#### Phase 2: Introduce Service Boundaries

6. **Create Article Service API client** — replace `ArticleRepository` dependency in `CommentsApi` and `CommentMutation` with a client that calls the Article service (sync HTTP or gRPC).
7. **Create User Service API client** — replace `UserRelationshipQueryService` calls in `CommentQueryService` with a User service client.
8. **Remove `users` table JOIN** — replace with a local `comment_authors` projection table, populated via `UserProfileUpdated` events.

#### Phase 3: Event-Driven Data Replication

9. **Subscribe to Article events** — maintain a local `articles_reference` table with `(articleId, slug, ownerId)` so the Comments service can resolve slugs and check article ownership locally.
10. **Subscribe to User events** — maintain a local `user_profiles` table with `(userId, username, bio, image)` for read-side enrichment.
11. **Publish Comment events** — emit `CommentCreated` and `CommentDeleted` events for other services that may need to react (e.g., notification service, article comment count denormalization).

#### Phase 4: API Gateway Routing

12. **Configure API Gateway** — route `/articles/{slug}/comments` to the Comments microservice. The gateway can either:
    - Pass-through the slug and let Comments resolve it from its local `articles_reference` table
    - Resolve slug → articleId at the gateway level and forward the articleId to Comments
13. **Add independent endpoints** — optionally expose `/comments/{id}` and `/comments?articleId=...` for direct access.

### 8.3 Data Ownership Summary

| Data | Current Owner | After Extraction |
|------|--------------|-----------------|
| `comments` table | Monolith (shared DB) | Comments microservice (own DB) |
| `article_id` on comment | Logical FK to `articles` | Opaque reference + local `articles_reference` projection |
| `user_id` on comment | Logical FK to `users` | Opaque reference + local `user_profiles` projection |
| Comment author profile | JOINed from `users` table | Local projection or API call to User service |
| Follow status | Queried via `UserRelationshipQueryService` | API call to User service or local `follows` projection |
| Article ownership (for auth) | Loaded from `articles` table via `ArticleRepository` | Local `articles_reference` projection or API call |

### 8.4 Estimated Extraction Effort

- **Pre-extraction refactoring (Phase 1):** Low effort, high value — can be done within the monolith
- **Service boundary introduction (Phase 2):** Medium effort — requires Article and User services to exist first
- **Event-driven replication (Phase 3):** High effort — requires event infrastructure (Kafka/RabbitMQ) and projection management
- **API Gateway routing (Phase 4):** Medium effort — depends on gateway technology choice

**Recommendation:** Extract Articles and Users as microservices **first**. Comments should be the **last** domain extracted due to its dependency on both. The pre-extraction refactoring (Phase 1) should be done immediately to reduce coupling within the monolith, regardless of extraction timeline.
