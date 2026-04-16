# Users/Profiles Bounded Context — Domain Boundary Analysis

## 1. Domain Overview

The **Users/Profiles** bounded context is the identity and social-graph foundation of the RealWorld application. It owns:

- **User identity management** — registration, authentication (JWT-based), and profile updates (email, username, password, bio, image).
- **Social graph (follow relationships)** — users can follow/unfollow other users; the "following" status is surfaced on every profile view and is consumed by other domains to decorate article and comment authors.
- **Authentication & authorization infrastructure** — JWT token issuance/validation, Spring Security filter chain, and password encoding. These are cross-cutting concerns that gate access to **all** domain endpoints, not just user endpoints.

This context is the **most depended-upon** bounded context in the monolith. Every other domain (Articles, Comments, Tags/Favorites) references `userId` as a foreign key, embeds `ProfileData` in its read models, and relies on `UserRelationshipQueryService` to populate the `following` field.

---

## 2. Entities & Aggregates

### 2.1 User (Aggregate Root)

| Field      | Type     | Notes                                      |
|------------|----------|--------------------------------------------|
| `id`       | `String` | UUID, generated at construction time       |
| `email`    | `String` | Unique, validated with `@Email`            |
| `username` | `String` | Unique                                     |
| `password` | `String` | BCrypt-encoded via `PasswordEncoder`       |
| `bio`      | `String` | Free-text biography                        |
| `image`    | `String` | URL; defaults to config value `image.default` |

- **Source**: `core/user/User.java`
- Identity is based on `id` (`@EqualsAndHashCode(of = {"id"})`).
- Mutable via `update()` method — only non-empty fields are overwritten.

### 2.2 FollowRelation (Entity)

| Field      | Type     | Notes                                 |
|------------|----------|---------------------------------------|
| `userId`   | `String` | The user who is following              |
| `targetId` | `String` | The user being followed                |

- **Source**: `core/user/FollowRelation.java`
- Simple value-like entity (uses `@Data` — equality on all fields).
- Currently lives inside the same aggregate as `User` because `UserRepository` handles both user CRUD **and** follow relationship CRUD.

### Aggregate Boundary Observation

`User` and `FollowRelation` are managed by the **same repository** (`UserRepository`), making them part of a single aggregate in the current design. However, they could arguably be **separated**:

- `FollowRelation` has no lifecycle dependency on `User` mutations — follows are created/deleted independently of user profile changes.
- The `follows` table has no foreign key constraints back to `users` (no `REFERENCES` clause in the DDL).
- Splitting `FollowRelation` into its own aggregate (with a dedicated `FollowRepository`) would reduce the surface area of `UserRepository` and make the social graph independently deployable.

---

## 3. API Endpoints

### 3.1 REST Endpoints

| Method   | Path                              | Controller         | Auth Required | Description                     |
|----------|-----------------------------------|--------------------|--------------|---------------------------------|
| `POST`   | `/users`                          | `UsersApi`         | No           | Register a new user             |
| `POST`   | `/users/login`                    | `UsersApi`         | No           | Authenticate and get JWT token  |
| `GET`    | `/user`                           | `CurrentUserApi`   | Yes          | Get current authenticated user  |
| `PUT`    | `/user`                           | `CurrentUserApi`   | Yes          | Update current user's profile   |
| `GET`    | `/profiles/{username}`            | `ProfileApi`       | Optional     | Get a user's public profile     |
| `POST`   | `/profiles/{username}/follow`     | `ProfileApi`       | Yes          | Follow a user                   |
| `DELETE` | `/profiles/{username}/follow`     | `ProfileApi`       | Yes          | Unfollow a user                 |

### 3.2 GraphQL Operations

| Operation  | Type       | Resolver                  | Auth Required | Description                     |
|------------|------------|---------------------------|--------------|---------------------------------|
| `createUser` | Mutation | `UserMutation`            | No           | Register a new user             |
| `login`      | Mutation | `UserMutation`            | No           | Authenticate user               |
| `updateUser` | Mutation | `UserMutation`            | Yes          | Update current user             |
| `me`         | Query    | `MeDatafetcher`           | Yes          | Get current user with token     |
| `profile`    | Query    | `ProfileDatafetcher`      | Optional     | Get profile by username         |
| `followUser` | Mutation | `RelationMutation`        | Yes          | Follow a user                   |
| `unfollowUser` | Mutation | `RelationMutation`      | Yes          | Unfollow a user                 |

**Cross-domain GraphQL resolvers** owned by this context but serving other domains:

| Resolver                                 | Parent Type | Field    | Description                                  |
|------------------------------------------|-------------|----------|----------------------------------------------|
| `ProfileDatafetcher.getAuthor`           | `Article`   | `author` | Resolves article author profile              |
| `ProfileDatafetcher.getCommentAuthor`    | `Comment`   | `author` | Resolves comment author profile              |
| `ProfileDatafetcher.getUserProfile`      | `User`      | `profile`| Resolves profile for a user type             |

---

## 4. Database Tables

### 4.1 `users` Table

```sql
create table users (
  id varchar(255) primary key,
  username varchar(255) UNIQUE,
  password varchar(255),
  email varchar(255) UNIQUE,
  bio text,
  image varchar(511)
);
```

- **Owned by**: Users/Profiles domain.
- **Referenced by**: `articles.user_id`, `comments.user_id`, `article_favorites.user_id` (no explicit FK constraints in DDL, but logical foreign keys via `userId` fields in other domain entities).

### 4.2 `follows` Table

```sql
create table follows (
  user_id varchar(255) not null,
  follow_id varchar(255) not null
);
```

- **Owned by**: Users/Profiles domain.
- **Notable**: No primary key constraint, no unique constraint, no foreign key constraints. Duplicate follow entries are prevented at the application layer (`MyBatisUserRepository.saveRelation` checks for existing relation before inserting).

---

## 5. Internal Dependencies

This section documents dependencies **within** the Users/Profiles domain (file-to-file references that stay inside the boundary).

```
UsersApi
  ├── UserService (createUser)
  ├── UserQueryService (findById → UserData)
  ├── UserRepository (findByEmail for login)
  ├── JwtService (toToken)
  └── PasswordEncoder (matches)

CurrentUserApi
  ├── UserQueryService (findById → UserData)
  └── UserService (updateUser)

ProfileApi
  ├── ProfileQueryService (findByUsername → ProfileData)
  └── UserRepository (findByUsername, saveRelation, findRelation, removeRelation)

UserService
  ├── UserRepository (save)
  ├── PasswordEncoder (encode)
  └── RegisterParam / UpdateUserCommand (validation DTOs)

ProfileQueryService
  ├── UserReadService [MyBatis mapper] (findByUsername → UserData)
  └── UserRelationshipQueryService [MyBatis mapper] (isUserFollowing)

UserQueryService
  └── UserReadService [MyBatis mapper] (findById → UserData)

JwtTokenFilter
  ├── JwtService (getSubFromToken)
  └── UserRepository (findById)

DefaultJwtService
  └── implements JwtService (toToken, getSubFromToken using jjwt + HS512)

MyBatisUserRepository
  └── UserMapper [MyBatis mapper] (insert, update, findById, findByUsername, findByEmail, saveRelation, findRelation, deleteRelation)

Validators (DuplicatedEmailValidator, DuplicatedUsernameValidator, UpdateUserValidator)
  └── UserRepository (findByEmail, findByUsername)

GraphQL Layer
  UserMutation → UserService, UserRepository, PasswordEncoder
  MeDatafetcher → UserQueryService, JwtService
  ProfileDatafetcher → ProfileQueryService
  RelationMutation → UserRepository, ProfileQueryService
  SecurityUtil → reads User from SecurityContextHolder
```

---

## 6. Cross-Domain Dependencies

### 6.1 User ID as Foreign Key (Data Reference)

| Consuming Domain | Table / Entity           | Field       | Coupling Type    |
|------------------|--------------------------|-------------|------------------|
| Articles         | `articles` / `Article`   | `user_id`   | Data reference   |
| Comments         | `comments` / `Comment`   | `user_id`   | Data reference   |
| Favorites        | `article_favorites` / `ArticleFavorite` | `user_id` | Data reference |

**Files involved**:
- `core/article/Article.java` — has `userId` field
- `core/comment/Comment.java` — has `userId` field
- `core/favorite/ArticleFavorite.java` — has `userId` field
- `V1__create_tables.sql` — `articles.user_id`, `comments.user_id`, `article_favorites.user_id`

**Extraction impact**: `userId` is a stable string identifier (UUID). In a microservice architecture, these become **eventual references** — the Articles and Comments services store the user ID but cannot join to the `users` table. No DDL-level FK constraints exist, which simplifies extraction.

---

### 6.2 ProfileData Embedded in Article and Comment Read Models (Shared DTO Type)

`ProfileData` (fields: `id`, `username`, `bio`, `image`, `following`) is embedded as the `author` field in both `ArticleData` and `CommentData`.

**Files involved**:
- `application/data/ProfileData.java` — the shared DTO
- `application/data/ArticleData.java` — `@JsonProperty("author") private ProfileData profileData`
- `application/data/CommentData.java` — `@JsonProperty("author") private ProfileData profileData`
- MyBatis XML mappers for articles and comments populate `ProfileData` via joined queries

**Extraction impact**: This is the **highest-friction coupling**. Every article and comment response includes the full author profile. After extraction, consuming services must either:
- Call a **User Profile API** to hydrate author data (adds latency per request, N+1 risk)
- Maintain a **local read replica** of profile data (via events like `UserProfileUpdated`)
- Accept **eventually consistent** profile data cached at read time

---

### 6.3 UserRelationshipQueryService Consumed by Articles & Comments (Service Call)

`ArticleQueryService` and `CommentQueryService` both directly inject and call `UserRelationshipQueryService` to determine whether the current user follows each article/comment author.

**Files involved**:
- `application/ArticleQueryService.java` — injects `UserRelationshipQueryService`, calls `isUserFollowing()`, `followingAuthors()`, `followedUsers()`
- `application/CommentQueryService.java` — injects `UserRelationshipQueryService`, calls `isUserFollowing()`, `followingAuthors()`
- `infrastructure/mybatis/readservice/UserRelationshipQueryService.java` — MyBatis mapper interface querying `follows` table

**Specific usage in ArticleQueryService**:
- `fillExtraInfo()` (line 175-183): calls `isUserFollowing()` to set `following` on single article's author profile
- `setIsFollowingAuthor()` (line 133-146): calls `followingAuthors()` batch query for list views
- `findUserFeedWithCursor()` / `findUserFeed()`: calls `followedUsers()` to get the list of users the current user follows, then fetches articles by those authors

**Specific usage in CommentQueryService**:
- `findById()` (line 23-35): calls `isUserFollowing()` for single comment
- `findByArticleId()` / `findByArticleIdWithCursor()`: calls `followingAuthors()` batch query

**Extraction impact**: This is the second-highest friction point. The social graph query is in the hot path of **every article and comment read operation**. Options:
- **Synchronous API call** to a User/Profiles microservice (latency concern)
- **Replicate follow graph** into consuming services via events (complexity, storage duplication)
- **Defer `following` field** computation to the API gateway or BFF layer

---

### 6.4 JwtService — Cross-Cutting Authentication Infrastructure

`JwtService` (interface in `core/service/`) and its implementation `DefaultJwtService` (in `infrastructure/service/`) are used for:
1. Token issuance at login/registration (`UsersApi`, `UserMutation`, `MeDatafetcher`)
2. Token validation on **every authenticated request** (`JwtTokenFilter`)

**Files involved**:
- `core/service/JwtService.java` — interface: `toToken(User)`, `getSubFromToken(String)`
- `infrastructure/service/DefaultJwtService.java` — HS512 HMAC implementation using `jjwt`
- `api/security/JwtTokenFilter.java` — validates JWT on every request, resolves `User` from `UserRepository`
- `api/security/WebSecurityConfig.java` — registers the filter, defines endpoint security rules for **all** domains

**Extraction impact**: JWT is inherently cross-cutting. In a microservice world:
- An **API gateway** or **auth sidecar** should handle token validation
- The signing secret (`jwt.secret`) must be shared or asymmetric keys (RS256) should replace symmetric (HS512)
- `JwtTokenFilter` currently loads the full `User` entity from the database on every request — this must be replaced with claims-based identity or a lightweight user-info cache

---

### 6.5 AuthorizationService — Cross-Domain Permission Checks

`AuthorizationService` is a static utility that checks whether a `User` can write/delete articles and comments. It imports `User`, `Article`, and `Comment` entities — spanning all three core domains.

**Files involved**:
- `core/service/AuthorizationService.java` — `canWriteArticle(User, Article)`, `canWriteComment(User, Article, Comment)`

**Extraction impact**: This service couples Users to Articles and Comments at the **domain model level**. After extraction, authorization logic should live in each respective service, comparing the authenticated `userId` (from JWT claims) against the resource's `userId` field. The `User` entity itself is not needed — only the `userId` string.

---

### 6.6 Spring Security Configuration — Global Endpoint Gating

`WebSecurityConfig` defines access rules for endpoints across **all** domains:
- `/users`, `/users/login` — permitAll (Users domain)
- `/articles/**`, `/profiles/**`, `/tags` — GET permitAll
- `/articles/feed` — authenticated
- `/graphql`, `/graphiql` — permitAll
- Everything else — authenticated

**Files involved**:
- `api/security/WebSecurityConfig.java`
- `api/security/JwtTokenFilter.java`

**Extraction impact**: Each microservice will need its own security configuration or delegate to an API gateway. The current monolithic config cannot be split without rewriting.

---

### 6.7 PasswordEncoder — Shared Spring Bean

`PasswordEncoder` (BCrypt) is defined as a bean in `WebSecurityConfig` and consumed by:
- `UserService.createUser()` — encoding passwords at registration
- `UsersApi.userLogin()` — matching passwords at login
- `UserMutation.login()` — matching passwords in GraphQL

**Files involved**:
- `api/security/WebSecurityConfig.java` — `@Bean PasswordEncoder`
- `application/user/UserService.java` — constructor injection
- `api/UsersApi.java` — constructor injection
- `graphql/UserMutation.java` — constructor injection

**Extraction impact**: Low friction. `PasswordEncoder` is a standard Spring Security bean and would simply move into the User microservice's own config.

---

### 6.8 SecurityUtil — GraphQL Security Helper

`SecurityUtil.getCurrentUser()` extracts the authenticated `User` entity from `SecurityContextHolder`. It is used by GraphQL resolvers across domains:
- `ProfileDatafetcher` (Users/Profiles)
- `RelationMutation` (Users/Profiles)
- `ArticleMutation`, `ArticleDatafetcher`, `CommentMutation`, `CommentDatafetcher` (Articles/Comments — not in this domain)

**Files involved**:
- `graphql/SecurityUtil.java`

**Extraction impact**: Shared utility; in a microservice world, each service would have its own mechanism to extract the authenticated user from the request context (e.g., from JWT claims or a gateway-injected header).

---

### 6.9 User Entity in `@AuthenticationPrincipal` Across All Controllers

The `JwtTokenFilter` resolves the full `User` entity and places it in `SecurityContextHolder`. All REST controllers use `@AuthenticationPrincipal User` to access the current user. This means the `User` domain entity is imported by controllers in **every** domain:
- `ArticleApi`, `ArticlesApi`, `ArticleFavoriteApi` (Articles)
- `CommentsApi` (Comments)
- `UsersApi`, `CurrentUserApi`, `ProfileApi` (Users/Profiles)

**Extraction impact**: After microservice extraction, controllers won't have access to the `User` entity. They will receive a user ID (or lightweight claims object) from the API gateway/auth layer instead.

---

## 7. Shared Kernel / Anti-Corruption Layer Candidates

### 7.1 ProfileData as a Shared Kernel Type

`ProfileData` is the strongest shared-kernel candidate. It is:
- **Produced** by `ProfileQueryService` (Users/Profiles domain)
- **Consumed** by `ArticleData` and `CommentData` as an embedded `author` field
- **Mutated** by `ArticleQueryService` and `CommentQueryService` (which set the `following` flag)

**Recommendation**: Define `ProfileData` (or a slimmed-down `AuthorSummary`) as a **published language** / shared contract. In a microservice architecture, this becomes a well-defined API response schema that other services can cache or replicate.

### 7.2 UserRelationshipQueryService as a Shared Service

This MyBatis mapper is consumed by three application services:
- `ProfileQueryService` (Users/Profiles)
- `ArticleQueryService` (Articles)
- `CommentQueryService` (Comments)

**Recommendation**: Extract this into a dedicated **Follow/Social-Graph API** (could be part of the Users microservice or a standalone service). Expose:
- `GET /internal/follows/is-following?userId=X&targetId=Y`
- `POST /internal/follows/following-authors` (batch query)
- `GET /internal/follows/followed-users?userId=X`

Alternatively, publish `UserFollowed` / `UserUnfollowed` events so consuming services can maintain a local follow-graph projection.

### 7.3 JWT / Security as Cross-Cutting Infrastructure

JWT token validation, the `JwtTokenFilter`, `WebSecurityConfig`, and `SecurityUtil` are infrastructure concerns that span all domains.

**Recommendation**: Move to an **API gateway** pattern:
- **Token issuance** stays in the Users/Auth microservice
- **Token validation** moves to the API gateway (or a shared sidecar/library)
- **Signing keys** are shared via a secrets manager or replaced with asymmetric keys (RS256/ES256) where the gateway holds the public key
- The gateway injects a trusted `X-User-Id` (and optionally `X-Username`) header into downstream requests, eliminating the need for each service to resolve the `User` entity from the database

### 7.4 AuthorizationService as an Anti-Corruption Layer Seam

`AuthorizationService` currently couples `User`, `Article`, and `Comment` entities. This is a natural seam for an **anti-corruption layer**:
- Each microservice implements its own authorization check: `if (requestUserId.equals(resource.getUserId()))`
- The `User` entity is no longer needed — only the stable `userId` string from the auth context

---

## 8. Microservice Extraction Considerations

### 8.1 Key Challenge: Most-Depended-Upon Domain

The Users/Profiles context has the highest **afferent coupling** in the monolith:
- **Every table** references `users.id` (logically, even if not via DDL FK constraints)
- **Every read model** embeds `ProfileData`
- **Every authenticated request** resolves a `User` entity
- **Two other query services** depend on `UserRelationshipQueryService`

This means Users/Profiles should be extracted **first** (or at least early), since it is a foundational service. However, extraction must be carefully staged to avoid breaking all consumers simultaneously.

### 8.2 Recommended Extraction Strategy

#### Phase 1: API Gateway & Auth Externalization
1. Deploy an **API gateway** (e.g., Spring Cloud Gateway, Kong, Envoy) that handles JWT validation.
2. Replace `JwtTokenFilter`'s database lookup with **claims-based identity** — encode `userId` and `username` in the JWT payload.
3. The gateway injects `X-User-Id` / `X-Username` headers into downstream service requests.
4. Each service trusts the gateway and reads identity from headers instead of `@AuthenticationPrincipal User`.

#### Phase 2: User ID as a Stable Contract
1. `userId` (UUID string) becomes the **canonical cross-service reference**. No entity sharing.
2. All foreign keys (`articles.user_id`, `comments.user_id`, `article_favorites.user_id`) remain as-is — they reference a user ID, not a user table.
3. No schema migration needed in consuming services; they simply stop joining to `users`.

#### Phase 3: Profile Data Replication vs. API Calls
Two options (not mutually exclusive):

**Option A — Synchronous Profile API**:
- Users microservice exposes `GET /api/users/{id}/profile` and `GET /api/users/batch-profiles?ids=...`
- Article/Comment services call this API when assembling read models
- **Pro**: simple; always consistent. **Con**: added latency, N+1 risk on list views.

**Option B — Event-Driven Replication**:
- Users microservice publishes `UserProfileUpdated` events (containing `id`, `username`, `bio`, `image`).
- Article/Comment services maintain a local `user_profiles` projection table.
- Read models are assembled locally with no cross-service call.
- **Pro**: fast reads, no runtime dependency. **Con**: eventual consistency, additional infrastructure (message broker, event schema).

**Recommendation**: Use **Option B** for list views (articles feed, comments list) where latency matters, and **Option A** as a fallback for cache misses or real-time profile pages.

#### Phase 4: Follow Graph Service
1. Extract follow relationships into the Users microservice (or a dedicated Social Graph service).
2. Expose internal APIs:
   - `GET /internal/follows/is-following?userId=X&targetId=Y` → `boolean`
   - `POST /internal/follows/following-authors` (body: `{userId, authorIds}`) → `Set<String>`
   - `GET /internal/follows/followed-users?userId=X` → `List<String>`
3. Publish `UserFollowed` / `UserUnfollowed` domain events for consumers that want a local projection.
4. The "feed" feature (`/articles/feed`) currently uses `followedUsers()` to get a list of followed author IDs, then queries articles by those authors. After extraction, this becomes a cross-service orchestration: call Follow API → get author IDs → call Articles API with author filter.

#### Phase 5: Event-Driven Follow Notifications
- Publish `UserFollowed(userId, targetId, timestamp)` and `UserUnfollowed(userId, targetId, timestamp)` events.
- Article feed service can subscribe to maintain a local follow index for efficient feed generation.
- Notification services can subscribe to send follow alerts.

### 8.3 Summary of Extraction Seams

| Seam                           | Current Coupling            | Target Architecture                          |
|--------------------------------|-----------------------------|----------------------------------------------|
| User identity (`userId`)       | FK in every table           | Stable string contract, no entity sharing    |
| ProfileData                    | Embedded DTO in all reads   | API call or event-replicated projection      |
| UserRelationshipQueryService   | Direct MyBatis mapper call  | Internal REST API + optional event stream    |
| JwtService / JwtTokenFilter    | Shared filter + DB lookup   | API gateway + claims-based auth              |
| AuthorizationService           | Static utility, 3 entities  | Per-service `userId` comparison              |
| WebSecurityConfig              | Monolithic endpoint rules   | Per-service security config + gateway rules  |
| PasswordEncoder                | Shared Spring bean          | Moves entirely into Users microservice       |
| SecurityUtil (GraphQL)         | Static utility              | Per-service auth context extraction          |

### 8.4 Risk Assessment

| Risk                                    | Severity | Mitigation                                              |
|-----------------------------------------|----------|---------------------------------------------------------|
| Profile data staleness after extraction | Medium   | TTL-based cache + event-driven refresh                  |
| Follow graph query latency              | High     | Batch APIs + local projections for hot paths            |
| JWT secret sharing across services      | Medium   | Switch to asymmetric keys (RS256) + gateway validation  |
| Feed feature requires cross-service orchestration | High | Dedicated feed service or BFF pattern          |
| Dual-write risk during migration        | Medium   | Strangler fig pattern, read from old + write to new     |
| N+1 profile lookups on list views       | High     | Batch profile API + client-side caching                 |
