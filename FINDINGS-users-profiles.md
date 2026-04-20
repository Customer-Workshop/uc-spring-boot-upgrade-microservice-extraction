# Domain Boundary Analysis: Users/Profiles Bounded Context

## 1. Domain Overview

The **Users/Profiles** bounded context is responsible for all identity, authentication, and social-graph functionality within the Conduit blogging platform. It owns:

- **User registration** with email/username uniqueness validation and BCrypt password hashing
- **Authentication** via stateless JWT tokens (HS512 signing, 24-hour expiry)
- **Profile management** including bio, image, email, and username updates
- **Following relationships** (a directed social graph between users)

This context acts as the **identity backbone** of the entire application. Every authenticated API request across all domains flows through the JWT security filter, and every article or comment displayed to a user includes profile data enriched with following status.

---

## 2. Entities & Aggregates

### User (Aggregate Root)

| Field      | Type     | Notes                                      |
|------------|----------|--------------------------------------------|
| `id`       | `String` | UUID, generated at construction time       |
| `email`    | `String` | Unique, validated with `@Email`            |
| `username` | `String` | Unique, validated with `@NotBlank`         |
| `password` | `String` | BCrypt-hashed, never exposed in read DTOs  |
| `bio`      | `String` | Free-text biography                        |
| `image`    | `String` | Avatar URL, defaults to a configured value |

- **Source**: `io.spring.core.user.User`
- The `update()` method applies partial updates; empty strings are treated as "no change".
- Equality is based solely on `id` (`@EqualsAndHashCode(of = {"id"})`).

### FollowRelation (Value Object)

| Field      | Type     | Notes                              |
|------------|----------|------------------------------------|
| `userId`   | `String` | The follower                       |
| `targetId` | `String` | The user being followed            |

- **Source**: `io.spring.core.user.FollowRelation`
- Represents a directed edge in the social graph (A follows B != B follows A).
- Idempotent save: `MyBatisUserRepository.saveRelation()` checks for existing relation before inserting.

---

## 3. API Endpoints

### REST Endpoints

| Method   | Path                              | Controller        | Auth Required | Description                        |
|----------|-----------------------------------|--------------------|---------------|------------------------------------|
| `POST`   | `/users`                          | `UsersApi`         | No            | Register a new user                |
| `POST`   | `/users/login`                    | `UsersApi`         | No            | Authenticate and receive JWT       |
| `GET`    | `/user`                           | `CurrentUserApi`   | Yes           | Get current authenticated user     |
| `PUT`    | `/user`                           | `CurrentUserApi`   | Yes           | Update current user profile        |
| `GET`    | `/profiles/{username}`            | `ProfileApi`       | Optional      | Get public profile (+ following)   |
| `POST`   | `/profiles/{username}/follow`     | `ProfileApi`       | Yes           | Follow a user                      |
| `DELETE` | `/profiles/{username}/follow`     | `ProfileApi`       | Yes           | Unfollow a user                    |

### GraphQL Mutations

| Mutation       | DGS Component      | Description                          |
|----------------|----------------------|--------------------------------------|
| `createUser`   | `UserMutation`       | Register a new user                  |
| `login`        | `UserMutation`       | Authenticate with email/password     |
| `updateUser`   | `UserMutation`       | Update profile of current user       |
| `followUser`   | `RelationMutation`   | Follow a user by username            |
| `unfollowUser` | `RelationMutation`   | Unfollow a user by username          |

---

## 4. Application Services

### Command Services (Write Side)

| Service                  | Package                          | Responsibilities                                                                 |
|--------------------------|----------------------------------|----------------------------------------------------------------------------------|
| `UserService`            | `io.spring.application.user`     | `createUser()` - validates uniqueness, hashes password, persists user            |
|                          |                                  | `updateUser()` - validates email/username uniqueness on update, applies changes  |
| `UpdateUserValidator`    | `io.spring.application.user`     | Custom `ConstraintValidator` ensuring email/username aren't taken by another user |

### Query Services (Read Side)

| Service                  | Package                          | Responsibilities                                                                 |
|--------------------------|----------------------------------|----------------------------------------------------------------------------------|
| `UserQueryService`       | `io.spring.application`          | Wraps `UserReadService.findById()` for user data retrieval                       |
| `ProfileQueryService`    | `io.spring.application`          | Builds `ProfileData` from `UserReadService` + `UserRelationshipQueryService`     |

### DTOs

| DTO              | Fields                                           | Used By                                                |
|------------------|--------------------------------------------------|--------------------------------------------------------|
| `UserData`       | id, email, username, bio, image                  | `UserQueryService`, API responses                      |
| `ProfileData`    | id (hidden), username, bio, image, following      | `ProfileQueryService`, embedded in `ArticleData` and `CommentData` |
| `UserWithToken`  | email, username, bio, image, token               | Registration and login responses                       |
| `RegisterParam`  | email, username, password                        | Registration request body                              |
| `UpdateUserParam`| email, username, password, bio, image            | Profile update request body                            |
| `UpdateUserCommand` | targetUser (User), param (UpdateUserParam)    | Wraps user + update payload for validation             |

---

## 5. Security Infrastructure

### JWT Authentication

- **Interface**: `io.spring.core.service.JwtService`
  - `toToken(User)` - generates a signed JWT with user ID as subject
  - `getSubFromToken(String)` - validates and extracts user ID from token
- **Implementation**: `io.spring.infrastructure.service.DefaultJwtService`
  - Algorithm: HS512
  - Signing key: derived from `jwt.secret` application property
  - Token expiry: configurable via `jwt.sessionTime` (default 86400 seconds = 24 hours)
  - Library: JJWT (`io.jsonwebtoken`)

### JwtTokenFilter

- **Source**: `io.spring.api.security.JwtTokenFilter`
- Extends `OncePerRequestFilter`
- Parses the `Authorization` header with format `Token <jwt>`
- On valid token: resolves User from `UserRepository.findById()` and populates `SecurityContextHolder`
- On missing/invalid token: passes through (anonymous access)

### WebSecurityConfig

- **Source**: `io.spring.api.security.WebSecurityConfig`
- Extends `WebSecurityConfigurerAdapter` (Spring Security 5.x pattern)
- Key settings:
  - CSRF: **disabled** (stateless API)
  - Session management: **STATELESS** (no HTTP sessions)
  - CORS: all origins allowed, credentials disabled
  - Password encoding: **BCryptPasswordEncoder**
- Public endpoints: `POST /users`, `POST /users/login`, `GET /articles/**`, `GET /profiles/**`, `GET /tags`, `/graphql`, `/graphiql`
- Protected endpoints: `GET /articles/feed`, all other requests

### AuthorizationService

- **Source**: `io.spring.core.service.AuthorizationService`
- Static utility methods:
  - `canWriteArticle(User, Article)` - checks `user.id == article.userId`
  - `canWriteComment(User, Article, Comment)` - checks if user is article author OR comment author
- This service spans domain boundaries: it imports `Article` and `Comment` entities from other contexts.

---

## 6. Data Storage

### Database: SQLite (via JDBC + Flyway migrations)

### Tables Owned by Users/Profiles

#### `users`

| Column     | Type          | Constraints         |
|------------|---------------|---------------------|
| `id`       | `varchar(255)` | PRIMARY KEY         |
| `username` | `varchar(255)` | UNIQUE              |
| `password` | `varchar(255)` |                     |
| `email`    | `varchar(255)` | UNIQUE              |
| `bio`      | `text`         |                     |
| `image`    | `varchar(511)` |                     |

#### `follows`

| Column      | Type          | Constraints |
|-------------|---------------|-------------|
| `user_id`   | `varchar(255)` | NOT NULL    |
| `follow_id` | `varchar(255)` | NOT NULL    |

> Note: The `follows` table has no composite primary key or unique constraint defined at the schema level. Idempotency is enforced at the application layer (`MyBatisUserRepository.saveRelation()` checks before insert).

### MyBatis Mapper: `UserMapper.xml`

Maps all SQL operations for the `users` and `follows` tables:
- `insert` / `update` / `findById` / `findByUsername` / `findByEmail` for users
- `saveRelation` / `deleteRelation` / `findRelation` for follows

### Read-Side Mappers

| Mapper Interface                   | Methods                                            |
|------------------------------------|----------------------------------------------------|
| `UserReadService`                  | `findByUsername(String)`, `findById(String)`        |
| `UserRelationshipQueryService`     | `isUserFollowing(userId, anotherUserId)`, `followingAuthors(userId, ids)`, `followedUsers(userId)` |

---

## 7. Cross-Boundary Dependencies

This is the most critical section for microservice extraction planning. The Users/Profiles context is heavily consumed by both the Articles and Comments domains.

### 7.1 Articles Domain -> Users/Profiles

| Dependency | Type | Detail |
|---|---|---|
| `Article.userId` | Data coupling | Every article stores the author's user ID as a foreign key reference |
| `ArticleFavorite.userId` | Data coupling | Every favorite stores the user ID of who favorited |
| `ArticleQueryService` -> `UserRelationshipQueryService.followedUsers()` | Service coupling | Used to build the user feed (`/articles/feed`) - fetches IDs of all users the current user follows |
| `ArticleQueryService` -> `UserRelationshipQueryService.followingAuthors()` | Service coupling | Enriches article listings with `following` flag on each author's profile |
| `ArticleQueryService` -> `UserRelationshipQueryService.isUserFollowing()` | Service coupling | Enriches single article view with following status |
| `ArticleData` embeds `ProfileData` | DTO coupling | Every article response includes the full author profile (username, bio, image, following) |
| All Article API controllers | Auth coupling | Accept `@AuthenticationPrincipal User user` - depend on the `User` domain entity directly |
| `AuthorizationService.canWriteArticle(User, Article)` | Logic coupling | Authorization check uses `User.getId()` to compare against `Article.getUserId()` |

### 7.2 Comments Domain -> Users/Profiles

| Dependency | Type | Detail |
|---|---|---|
| `Comment.userId` | Data coupling | Every comment stores the author's user ID |
| `CommentQueryService` -> `UserRelationshipQueryService.isUserFollowing()` | Service coupling | Enriches single comment view with following status on author |
| `CommentQueryService` -> `UserRelationshipQueryService.followingAuthors()` | Service coupling | Enriches comment listings with following flags |
| `CommentData` embeds `ProfileData` | DTO coupling | Every comment response includes the full author profile |
| All Comment API controllers | Auth coupling | Accept `@AuthenticationPrincipal User user` |
| `AuthorizationService.canWriteComment(User, Article, Comment)` | Logic coupling | Authorization check uses `User.getId()` to compare against both article and comment authors |

### 7.3 Users/Profiles Provides Shared Infrastructure

| Shared Component | Consumers | Impact |
|---|---|---|
| `JwtService` / `DefaultJwtService` | All API controllers (global filter) | Every authenticated request in the system flows through `JwtTokenFilter` |
| `WebSecurityConfig` | Entire application | Defines security rules for ALL endpoints across all domains |
| `BCryptPasswordEncoder` | `UsersApi`, `UserMutation` | Password hashing (contained to Users domain) |
| `@AuthenticationPrincipal User` | Every authenticated controller | The `User` domain entity is injected into controllers across all domains |

### 7.4 Dependency Direction Summary

```
Articles Domain ----depends-on----> Users/Profiles Domain
Comments Domain ---depends-on----> Users/Profiles Domain
Users/Profiles Domain ---depends-on----> (nothing external)
```

Users/Profiles is a **dependency sink** - it has no outbound dependencies on other bounded contexts, making it a natural candidate for extraction first.

---

## 8. Recommendations for Microservice Extraction

### 8.1 Extract a User/Profile API Service

Create a standalone service owning the `users` and `follows` tables with these APIs:

| Endpoint | Purpose | Consumers |
|---|---|---|
| `GET /internal/users/{id}` | Resolve user by ID | Articles, Comments services |
| `GET /internal/users/by-username/{username}` | Resolve user by username | Profile pages |
| `GET /internal/profiles/{username}?currentUserId=X` | Profile with following status | Articles, Comments (for embedding author data) |
| `GET /internal/users/{userId}/following` | List of followed user IDs | Articles service (for feed) |
| `POST /internal/users/{userId}/following-authors` | Batch check: which authors does user follow? | Articles, Comments (for setting `following` flags in lists) |
| `POST /internal/users/batch` | Bulk user lookup by IDs | Articles, Comments (avoid N+1 calls) |

### 8.2 Authentication Strategy

**Option A: Shared JWT library (recommended for initial extraction)**
- Extract `JwtService` / `DefaultJwtService` into a shared library artifact
- Each service includes this library and validates tokens locally
- Pros: no network hop for auth, simple
- Cons: secret key must be distributed to all services

**Option B: Auth sidecar / API gateway**
- API gateway validates JWT and forwards user identity as a trusted header (e.g., `X-User-Id`)
- Downstream services trust the gateway
- Pros: single secret location, centralized auth
- Cons: requires gateway infrastructure

**Option C: OAuth2 / OpenID Connect migration**
- Replace custom JWT with an identity provider (Keycloak, Auth0, etc.)
- Most robust long-term but highest migration effort

### 8.3 Decouple ProfileData from Article/Comment Read Models

Currently `ArticleData` and `CommentData` embed `ProfileData` directly, and the query services call `UserRelationshipQueryService` in-process. After extraction:

1. **Synchronous approach**: Articles/Comments services make HTTP calls to the User/Profile service to resolve author profiles. Use batch endpoints to avoid N+1.
2. **Data denormalization**: Replicate a minimal `author_profiles` read-only cache in the Articles/Comments databases. Keep it updated via events.
3. **API composition**: An API gateway or BFF (Backend-for-Frontend) composes article data with profile data at the edge.

### 8.4 Event Publishing for Follow/Unfollow

Introduce domain events for the social graph:

| Event | Payload | Consumers |
|---|---|---|
| `UserFollowed` | `{ followerId, followedId, timestamp }` | Articles service (update feed caches) |
| `UserUnfollowed` | `{ followerId, followedId, timestamp }` | Articles service (update feed caches) |
| `UserProfileUpdated` | `{ userId, username, bio, image }` | Articles, Comments services (update denormalized author data) |
| `UserRegistered` | `{ userId, username, email }` | Any service needing to react to new users |

Use an event broker (Kafka, RabbitMQ, or cloud-native equivalent) for these events.

### 8.5 Refactor AuthorizationService

`AuthorizationService` currently imports `Article` and `Comment` entities, making it a cross-cutting concern that spans all three domains. To decouple:

- Move article authorization logic (`canWriteArticle`) into the Articles domain
- Move comment authorization logic (`canWriteComment`) into the Comments domain
- Each service only needs the `userId` from the authenticated token to compare against the resource's `userId` field - no need to import the full `User` entity

### 8.6 Remove Direct User Entity Dependency from Controllers

All controllers currently inject `@AuthenticationPrincipal User user`, which couples them to the `User` domain entity. After extraction:

- The JWT filter should produce a lightweight `AuthenticatedPrincipal` (with just `userId` and optionally `username`) instead of the full `User` entity
- Controllers in Articles/Comments services work with this thin identity object
- Full user data is fetched from the User/Profile service only when needed

### 8.7 Database Migration

The `follows` table should:
1. Add a composite unique constraint on `(user_id, follow_id)` to enforce uniqueness at the database level
2. Move to the User/Profile service's dedicated database
3. Foreign key references to `users.id` in the `articles` and `comments` tables become logical references (no DB-level FK enforcement across service boundaries)

### 8.8 Suggested Extraction Order

1. **Extract Users/Profiles first** - it has zero outbound dependencies, making it the cleanest extraction target
2. **Introduce the internal User/Profile API** and update Articles/Comments monolith code to call it (strangler fig pattern)
3. **Add event publishing** for follow/unfollow and profile updates
4. **Extract Comments next** - simpler domain, fewer dependencies
5. **Extract Articles last** - most complex domain with favorites, tags, and feed logic

---

## Appendix: File Inventory

### Core Domain Layer
| File | Path |
|---|---|
| `User.java` | `src/main/java/io/spring/core/user/User.java` |
| `FollowRelation.java` | `src/main/java/io/spring/core/user/FollowRelation.java` |
| `UserRepository.java` | `src/main/java/io/spring/core/user/UserRepository.java` |
| `JwtService.java` | `src/main/java/io/spring/core/service/JwtService.java` |
| `AuthorizationService.java` | `src/main/java/io/spring/core/service/AuthorizationService.java` |

### API Layer
| File | Path |
|---|---|
| `UsersApi.java` | `src/main/java/io/spring/api/UsersApi.java` |
| `CurrentUserApi.java` | `src/main/java/io/spring/api/CurrentUserApi.java` |
| `ProfileApi.java` | `src/main/java/io/spring/api/ProfileApi.java` |
| `WebSecurityConfig.java` | `src/main/java/io/spring/api/security/WebSecurityConfig.java` |
| `JwtTokenFilter.java` | `src/main/java/io/spring/api/security/JwtTokenFilter.java` |

### Application Layer
| File | Path |
|---|---|
| `UserQueryService.java` | `src/main/java/io/spring/application/UserQueryService.java` |
| `ProfileQueryService.java` | `src/main/java/io/spring/application/ProfileQueryService.java` |
| `UserService.java` | `src/main/java/io/spring/application/user/UserService.java` |
| `RegisterParam.java` | `src/main/java/io/spring/application/user/RegisterParam.java` |
| `UpdateUserParam.java` | `src/main/java/io/spring/application/user/UpdateUserParam.java` |
| `UpdateUserCommand.java` | `src/main/java/io/spring/application/user/UpdateUserCommand.java` |
| `UserData.java` | `src/main/java/io/spring/application/data/UserData.java` |
| `ProfileData.java` | `src/main/java/io/spring/application/data/ProfileData.java` |
| `UserWithToken.java` | `src/main/java/io/spring/application/data/UserWithToken.java` |

### Infrastructure Layer
| File | Path |
|---|---|
| `DefaultJwtService.java` | `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java` |
| `MyBatisUserRepository.java` | `src/main/java/io/spring/infrastructure/repository/MyBatisUserRepository.java` |
| `UserMapper.java` | `src/main/java/io/spring/infrastructure/mybatis/mapper/UserMapper.java` |
| `UserReadService.java` | `src/main/java/io/spring/infrastructure/mybatis/readservice/UserReadService.java` |
| `UserRelationshipQueryService.java` | `src/main/java/io/spring/infrastructure/mybatis/readservice/UserRelationshipQueryService.java` |
| `UserMapper.xml` | `src/main/resources/mapper/UserMapper.xml` |

### GraphQL Layer
| File | Path |
|---|---|
| `UserMutation.java` | `src/main/java/io/spring/graphql/UserMutation.java` |
| `RelationMutation.java` | `src/main/java/io/spring/graphql/RelationMutation.java` |

### Database
| File | Path |
|---|---|
| `V1__create_tables.sql` | `src/main/resources/db/migration/V1__create_tables.sql` |
