# Internal Dependency Analysis

## Overview

This document maps the package-level dependency graph of the RealWorld Spring Boot monolith,
identifies architectural violations, and provides prioritized refactoring recommendations
for microservice extraction readiness.

**Packages analyzed:** 19 (excluding DGS-generated `io.spring.graphql.types`)

---

## Package Structure

```
io.spring (root-config)
├── api/                          REST controllers
│   ├── exception/                Error handling & response types
│   └── security/                 JWT filter & Spring Security config
├── application/                  Query services (CQRS read side)
│   ├── article/                  Article command service & validation
│   ├── data/                     DTOs (ArticleData, CommentData, ProfileData, UserData)
│   └── user/                     User registration & update params
├── core/                         Domain model (write side)
│   ├── article/                  Article entity + ArticleRepository
│   ├── comment/                  Comment entity + CommentRepository
│   ├── favorite/                 ArticleFavorite + ArticleFavoriteRepository
│   ├── service/                  AuthorizationService, JwtService interface
│   └── user/                     User entity + UserRepository + FollowRelation
├── graphql/                      DGS datafetchers & mutations
│   └── exception/                GraphQL exception handler
└── infrastructure/               Technical implementations
    ├── mybatis/
    │   ├── mapper/               Write-side MyBatis mappers
    │   └── readservice/          Read-side MyBatis mappers (CQRS queries)
    ├── repository/               MyBatis-backed repository implementations
    └── service/                  DefaultJwtService
```

---

## Text-Based Dependency Graph

Arrows show the direction of dependency (`A --> B` means A imports from B).

```
                              ┌─────────────────────────────────────────────┐
                              │          io.spring.core.user                │
                              │  (User, UserRepository, FollowRelation)     │
                              │         FAN-IN: 11 packages                 │
                              └──────────────────────┬──────────────────────┘
                                                     │
          ┌──────────────────┬───────────────────────┼───────────────────────┬──────────────────┐
          │                  │                       │                       │                  │
          ▼                  ▼                       ▼                       ▼                  ▼
   ┌─────────────┐  ┌───────────────┐  ┌────────────────────┐  ┌──────────────────┐  ┌─────────────┐
   │   api (REST)│  │   graphql     │  │    application     │  │ infra.repository │  │ core.service│
   │  fan-out:10 │  │  fan-out:11   │  │    fan-in:5        │  │                  │  │  fan-in:4   │
   └──────┬──────┘  └───────┬───────┘  └─────────┬──────────┘  └──────────────────┘  └─────────────┘
          │                  │                    │
          │                  │                    │  *** DIP VIOLATION ***
          │                  │                    ▼
          │                  │         ┌──────────────────────────────┐
          │                  │         │ infra.mybatis.readservice    │
          │                  │         │ (ArticleReadService,         │
          │                  │         │  CommentReadService, etc.)   │
          │                  │         └──────────────┬───────────────┘
          │                  │                        │
          │                  │                        ▼
          │                  │              ┌──────────────────┐
          │                  │              │ application.data │
          │                  └─────────────►│ (DTOs)           │
          └────────────────────────────────►│                  │
                                            └──────────────────┘
```

### Detailed Dependency Edges

| Source Package | Target Package | Via (key files) |
|---|---|---|
| `api` | `application` | ArticleApi, ArticlesApi, CommentsApi, ProfileApi, TagsApi, UsersApi, CurrentUserApi, ArticleFavoriteApi |
| `api` | `application.data` | ArticleApi, CommentsApi, ProfileApi, CurrentUserApi, TagsApi, ArticleFavoriteApi |
| `api` | `application.article` | ArticleApi, ArticlesApi |
| `api` | `application.user` | CurrentUserApi, UsersApi |
| `api` | `core.article` | ArticleApi, ArticleFavoriteApi, ArticlesApi, CommentsApi |
| `api` | `core.comment` | CommentsApi |
| `api` | `core.favorite` | ArticleFavoriteApi |
| `api` | `core.service` | ArticleApi, CommentsApi, UsersApi |
| `api` | `core.user` | ArticleApi, ArticleFavoriteApi, ArticlesApi, CommentsApi, CurrentUserApi, ProfileApi, UsersApi |
| `api` | `api.exception` | ArticleApi, ArticleFavoriteApi, CommentsApi, CurrentUserApi, UsersApi |
| `graphql` | `application` | ArticleDatafetcher, CommentDatafetcher, CommentMutation, MeDatafetcher, ProfileDatafetcher, RelationMutation, TagDatafetcher |
| `graphql` | `application.data` | ArticleDatafetcher, CommentDatafetcher, CommentMutation, MeDatafetcher, ProfileDatafetcher, RelationMutation |
| `graphql` | `core.user` | ArticleDatafetcher, ArticleMutation, CommentDatafetcher, CommentMutation, MeDatafetcher, ProfileDatafetcher, RelationMutation, UserMutation |
| `graphql` | `core.article` | ArticleMutation, CommentMutation |
| `graphql` | `core.comment` | CommentMutation |
| `graphql` | `core.favorite` | ArticleMutation |
| `graphql` | `core.service` | ArticleMutation, CommentMutation, MeDatafetcher |
| `graphql` | `api.exception` | ArticleDatafetcher, ArticleMutation, CommentMutation, MeDatafetcher, ProfileDatafetcher, RelationMutation, UserMutation |
| `graphql` | `application.article` | ArticleMutation |
| `graphql` | `application.user` | UserMutation |
| `graphql` | `graphql.exception` | ArticleMutation, CommentMutation, RelationMutation, UserMutation |
| **`application`** | **`infra.mybatis.readservice`** | **ArticleQueryService, CommentQueryService, ProfileQueryService, TagsQueryService, UserQueryService** |
| `application` | `application.data` | ArticleQueryService, CommentQueryService, ProfileQueryService, UserQueryService |
| `application` | `core.user` | ArticleQueryService, CommentQueryService, ProfileQueryService |
| `application.article` | `core.article` | ArticleCommandService, DuplicatedArticleValidator |
| `application.article` | `core.user` | ArticleCommandService |
| `application.article` | `application` | DuplicatedArticleValidator |
| `application.user` | `core.user` | DuplicatedEmailValidator, DuplicatedUsernameValidator, UpdateUserCommand, UserService |
| `application.data` | `application` | ArticleData (implements Node), CommentData (implements Node) |
| `infra.mybatis.readservice` | `application` | ArticleReadService (Page, CursorPageParameter), CommentReadService (CursorPageParameter) |
| `infra.mybatis.readservice` | `application.data` | ArticleFavoritesReadService, ArticleReadService, CommentReadService, UserReadService |
| `infra.mybatis.readservice` | `core.user` | ArticleFavoritesReadService |
| `infra.repository` | `core.article` | MyBatisArticleRepository |
| `infra.repository` | `core.comment` | MyBatisCommentRepository |
| `infra.repository` | `core.favorite` | MyBatisArticleFavoriteRepository |
| `infra.repository` | `core.user` | MyBatisUserRepository |
| `infra.repository` | `infra.mybatis.mapper` | All repository implementations |
| `infra.service` | `core.service` | DefaultJwtService |
| `infra.service` | `core.user` | DefaultJwtService |
| `core.service` | `core.article` | AuthorizationService |
| `core.service` | `core.comment` | AuthorizationService |
| `core.service` | `core.user` | AuthorizationService, JwtService |
| `graphql.exception` | `api.exception` | GraphQLCustomizeExceptionHandler |

---

## Circular Dependencies

### 1. `application` <--> `infrastructure.mybatis.readservice` (CRITICAL)

**Nature:** Dependency Inversion Principle (DIP) violation. The application layer directly
imports infrastructure-layer MyBatis mapper interfaces.

```
application/ArticleQueryService.java  ──imports──►  infra.mybatis.readservice/ArticleReadService
application/CommentQueryService.java  ──imports──►  infra.mybatis.readservice/CommentReadService
application/ProfileQueryService.java  ──imports──►  infra.mybatis.readservice/UserReadService
application/ProfileQueryService.java  ──imports──►  infra.mybatis.readservice/UserRelationshipQueryService
application/TagsQueryService.java     ──imports──►  infra.mybatis.readservice/TagReadService
application/UserQueryService.java     ──imports──►  infra.mybatis.readservice/UserReadService

infra.mybatis.readservice/ArticleReadService  ──imports──►  application/CursorPageParameter, Page
infra.mybatis.readservice/CommentReadService  ──imports──►  application/CursorPageParameter
```

**Impact:** Prevents independent deployment of the application layer; changes to MyBatis
configuration ripple into business logic. Blocks clean microservice extraction of any
domain context because read-model contracts are locked into the persistence technology.

### 2. `application` <--> `application.data` (Low severity)

**Nature:** Sub-package coupling. DTOs (`ArticleData`, `CommentData`) implement the `Node`
interface and reference `DateTimeCursor` from the parent `application` package.

```
application.data/ArticleData.java  ──implements──►  application/Node
application.data/CommentData.java  ──implements──►  application/Node
```

**Impact:** Minor; these are within the same logical module. However, it complicates
extracting DTOs into a shared library for cross-service communication.

### 3. `graphql` <--> `graphql.exception` (Low severity)

**Nature:** The exception handler imports DGS-generated types (`io.spring.graphql.types.Error`,
`ErrorItem`), while datafetchers import from `graphql.exception`.

**Impact:** Negligible. The `graphql.types` package is auto-generated by DGS and is
effectively a shared dependency, not a true architectural cycle.

---

## Fan-In / Fan-Out Analysis

### Highest Fan-In (most depended upon -- changes here cascade widely)

| Package | Fan-In | Key Dependents |
|---|---|---|
| `core.user` | 11 | api, graphql, application, application.article, application.user, core.service, infra.repository, infra.mybatis.mapper, infra.mybatis.readservice, infra.service, api.security |
| `core.article` | 6 | api, graphql, core.service, application.article, infra.repository, infra.mybatis.mapper |
| `core.comment` | 5 | api, graphql, core.service, infra.repository, infra.mybatis.mapper |
| `application` | 5 | api, graphql, application.article, application.data, infra.mybatis.readservice |
| `core.service` | 4 | api, graphql, api.security, infra.service |
| `application.data` | 4 | api, graphql, application, infra.mybatis.readservice |
| `core.favorite` | 4 | api, graphql, infra.repository, infra.mybatis.mapper |

### Highest Fan-Out (most dependencies -- fragile, breaks easily)

| Package | Fan-Out | Key Dependencies |
|---|---|---|
| `graphql` | 11 | api.exception, application, application.article, application.data, application.user, core.article, core.comment, core.favorite, core.service, core.user, graphql.exception |
| `api` | 10 | api.exception, application, application.article, application.data, application.user, core.article, core.comment, core.favorite, core.service, core.user |
| `infra.repository` | 5 | core.article, core.comment, core.favorite, core.user, infra.mybatis.mapper |
| `infra.mybatis.mapper` | 4 | core.article, core.comment, core.favorite, core.user |

### Coupling Hotspots (combined fan-in + fan-out)

| Package | Total Connections | Fan-In | Fan-Out | Risk Assessment |
|---|---|---|---|---|
| `core.user` | 12 | 11 | 1 | **HIGH** - God package; nearly universal dependency |
| `graphql` | 12 | 1 | 11 | **HIGH** - Extremely broad reach; any domain change affects it |
| `api` | 10 | 0 | 10 | **MEDIUM** - Broad but expected for controller layer |
| `application` | 8 | 5 | 3 | **MEDIUM** - Central hub; mediates between layers |
| `core.service` | 7 | 4 | 3 | **MEDIUM** - Cross-domain coupling (article + comment + user) |
| `core.article` | 7 | 6 | 1 | **MEDIUM** - High fan-in but stable domain entity |

---

## Tightly Coupled Modules

### 1. `core.user` -- Universal Dependency (Instability Risk: HIGH)

The `User` entity is imported by 11 of 19 packages. Every controller, datafetcher, query
service, repository, and authorization check depends on `core.user`. This means:
- Changing `User`'s interface affects the entire application
- Microservice extraction of any domain requires a user-service client
- The user context cannot be extracted without touching every other package

### 2. `graphql` -- Kitchen Sink Datafetcher Layer (Instability Risk: HIGH)

The `graphql` package has fan-out of 11, reaching into every domain context (articles,
comments, favorites, users) plus application services and exceptions. A single
`ArticleMutation.java` imports from 6 different packages.

### 3. `api` + `graphql` both depend on `api.exception` (Shared Concern)

The GraphQL exception handler (`graphql.exception`) imports REST-layer exception types
(`FieldErrorResource`, `InvalidAuthenticationException`). This couples the two adapter
layers and prevents independent evolution of error handling strategies.

### 4. `core.service.AuthorizationService` -- Cross-Aggregate Coupling

`AuthorizationService` imports from `core.article`, `core.comment`, AND `core.user`,
creating a transitive dependency across all three domain aggregates. Any refactoring of
article or comment entities must consider authorization logic.

---

## Refactoring Recommendations (Prioritized)

### Priority 1: Break `application --> infrastructure.mybatis.readservice` (Quick Win)

**Effort:** Low | **Impact:** High | **Risk:** Low

Move the 6 read service mapper interfaces from `infrastructure.mybatis.readservice/` to
`application/readservice/`. These are pure interfaces (no implementation logic) that define
the read-model contract. The application layer should own its port interfaces per DIP.

**Steps:**
1. Create `src/main/java/io/spring/application/readservice/` package
2. Move `ArticleReadService`, `CommentReadService`, `UserReadService`, `TagReadService`,
   `ArticleFavoritesReadService`, `UserRelationshipQueryService` there
3. Update `package` declarations and all import statements
4. Update MyBatis XML mapper `namespace` attributes
5. Update logging config in `application.properties`

**Result:** Eliminates the circular dependency and establishes correct dependency direction
(infrastructure depends on application, not vice versa).

### Priority 2: Extract Shared Exception Types

**Effort:** Low | **Impact:** Medium | **Risk:** Low

Create `io.spring.common.exception` (or move to `core`) for exception types used by both
REST and GraphQL layers: `InvalidAuthenticationException`, `ResourceNotFoundException`,
`FieldErrorResource`. This decouples the GraphQL adapter from the REST adapter.

### Priority 3: Introduce User Identity Value Object

**Effort:** Medium | **Impact:** High | **Risk:** Medium

Many packages only need `User.getId()`. Introduce a lightweight `UserId` value object
(or interface like `UserIdentity`) that packages can depend on instead of the full `User`
entity. This reduces the effective fan-in of `core.user`.

### Priority 4: Split `AuthorizationService` by Domain

**Effort:** Medium | **Impact:** Medium | **Risk:** Low

Move authorization logic into each aggregate:
- `core.article.ArticleAuthorization` (canWriteArticle)
- `core.comment.CommentAuthorization` (canWriteComment)

This eliminates the cross-aggregate dependency in `core.service`.

### Priority 5: Domain-Scoped Datafetcher Packages

**Effort:** High | **Impact:** High | **Risk:** Medium

Split `io.spring.graphql` into domain-scoped sub-packages:
- `graphql.article/` (ArticleDatafetcher, ArticleMutation)
- `graphql.comment/` (CommentDatafetcher, CommentMutation)
- `graphql.user/` (MeDatafetcher, UserMutation, RelationMutation, ProfileDatafetcher)

This aligns the GraphQL layer with domain boundaries for future service extraction.

---

## Microservice Extraction Readiness

Based on this analysis, the recommended extraction order (least coupled first):

1. **Tags Service** -- Minimal dependencies (TagsQueryService + TagReadService only)
2. **User/Profile Service** -- High fan-in but self-contained domain; other services would
   use a client
3. **Comments Service** -- Depends on user and article IDs but not entities
4. **Favorites Service** -- Depends on user and article IDs
5. **Articles Service** -- Most complex; depends on tags, user profiles, favorites
