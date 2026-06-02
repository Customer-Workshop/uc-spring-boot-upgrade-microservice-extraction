# Bounded Context Analysis

## Overview

This document maps the four bounded contexts in the RealWorld monolith and identifies their coupling points to guide microservice extraction.

**Codebase**: Spring Boot 2.6.3 monolith with REST + GraphQL (Netflix DGS) APIs, MyBatis persistence (SQLite), and a Next.js frontend.

---

## 1. Bounded Contexts

### 1.1 Articles & Tags

**Domain objects**: `Article`, `Tag`  
**Package**: `io.spring.core.article`

| Layer | Classes |
|-------|---------|
| Domain | `Article`, `Tag`, `ArticleRepository` |
| Application | `ArticleCommandService`, `ArticleQueryService`, `NewArticleParam`, `UpdateArticleParam`, `DuplicatedArticleValidator` |
| Application DTOs | `ArticleData`, `ArticleDataList`, `ArticleFavoriteCount` |
| API (REST) | `ArticleApi`, `ArticlesApi`, `TagsApi` |
| API (GraphQL) | `ArticleDatafetcher`, `ArticleMutation`, `TagDatafetcher` |
| Infrastructure | `ArticleMapper`, `ArticleReadService`, `TagReadService`, `MyBatisArticleRepository` |
| DB Tables | `articles`, `tags`, `article_tags` |

**Ownership**: Articles reference `userId` (FK to `users`). Tags are owned entirely by the article context.

### 1.2 Comments

**Domain objects**: `Comment`  
**Package**: `io.spring.core.comment`

| Layer | Classes |
|-------|---------|
| Domain | `Comment`, `CommentRepository` |
| Application | `CommentQueryService` |
| Application DTOs | `CommentData` |
| API (REST) | `CommentsApi` |
| API (GraphQL) | `CommentDatafetcher`, `CommentMutation` |
| Infrastructure | `CommentMapper`, `CommentReadService`, `MyBatisCommentRepository` |
| DB Tables | `comments` |

**Ownership**: Comments reference both `articleId` and `userId`. Tightly coupled to Article context via `articleId` lookups.

### 1.3 Favorites

**Domain objects**: `ArticleFavorite`  
**Package**: `io.spring.core.favorite`

| Layer | Classes |
|-------|---------|
| Domain | `ArticleFavorite`, `ArticleFavoriteRepository` |
| Application DTOs | `ArticleFavoriteCount` |
| API (REST) | `ArticleFavoriteApi` |
| Infrastructure | `ArticleFavoriteMapper`, `ArticleFavoritesReadService`, `MyBatisArticleFavoriteRepository` |
| DB Tables | `article_favorites` |

**Ownership**: Join entity linking `articleId` ↔ `userId`. Used by `ArticleQueryService` to compute `favoritesCount` and `favorited` flags on `ArticleData`.

### 1.4 Users & Profiles

**Domain objects**: `User`, `FollowRelation`  
**Package**: `io.spring.core.user`

| Layer | Classes |
|-------|---------|
| Domain | `User`, `FollowRelation`, `UserRepository` |
| Application | `UserService`, `UserQueryService`, `ProfileQueryService`, `RegisterParam`, `UpdateUserParam`, `UpdateUserCommand` |
| Application DTOs | `UserData`, `UserWithToken`, `ProfileData` |
| API (REST) | `UsersApi`, `CurrentUserApi`, `ProfileApi` |
| API (GraphQL) | `UserMutation`, `ProfileDatafetcher`, `RelationMutation`, `MeDatafetcher` |
| Infrastructure | `UserMapper`, `UserReadService`, `UserRelationshipQueryService`, `MyBatisUserRepository` |
| Security | `JwtTokenFilter`, `WebSecurityConfig`, `JwtService`, `DefaultJwtService` |
| DB Tables | `users`, `follows` |

**Ownership**: Users own authentication (JWT), profile data, and follow relationships.

---

## 2. Coupling Points

### 2.1 Article → User (strong)

| Coupling Type | Location | Detail |
|---------------|----------|--------|
| **Data FK** | `articles.user_id` | Every article stores the author's user ID |
| **Domain import** | `AuthorizationService` | `canWriteArticle(User, Article)` checks `user.getId().equals(article.getUserId())` |
| **DTO embedding** | `ArticleData.profileData` | `ProfileData` (username, bio, image, following) embedded in every article response |
| **Query join** | `ArticleReadService.xml` | SQL joins `articles` → `users` to populate `ProfileData` in read queries |
| **Follow check** | `ArticleQueryService.fillExtraInfo()` | Calls `UserRelationshipQueryService.isUserFollowing()` to set `profileData.following` |
| **Feed query** | `ArticleQueryService.findUserFeed()` | Calls `UserRelationshipQueryService.followedUsers()` to get followed user IDs for the feed |
| **Auth injection** | `@AuthenticationPrincipal User user` | All article controllers receive the full `User` domain object from Spring Security |

### 2.2 Comment → User (strong)

| Coupling Type | Location | Detail |
|---------------|----------|--------|
| **Data FK** | `comments.user_id` | Every comment stores the author's user ID |
| **Domain import** | `AuthorizationService` | `canWriteComment(User, Article, Comment)` checks both article owner and comment owner |
| **DTO embedding** | `CommentData.profileData` | `ProfileData` embedded in every comment response |
| **Query join** | `CommentReadService.xml` | SQL joins `comments` → `users` to populate `ProfileData` |
| **Follow check** | `CommentQueryService` | Calls `UserRelationshipQueryService.isUserFollowing()` / `followingAuthors()` |

### 2.3 Comment → Article (strong)

| Coupling Type | Location | Detail |
|---------------|----------|--------|
| **Data FK** | `comments.article_id` | Every comment references its parent article |
| **API routing** | `CommentsApi` | Mounted under `/articles/{slug}/comments` — requires `ArticleRepository.findBySlug()` to resolve the article |
| **Delete auth** | `CommentsApi.deleteComment()` | Needs both `Article` and `Comment` objects for authorization check |

### 2.4 Favorite → Article (strong)

| Coupling Type | Location | Detail |
|---------------|----------|--------|
| **Data FK** | `article_favorites(article_id, user_id)` | Composite PK linking articles to users |
| **API routing** | `ArticleFavoriteApi` | Mounted under `/articles/{slug}/favorite` — requires `ArticleRepository.findBySlug()` |
| **Read-side enrichment** | `ArticleQueryService` | Calls `ArticleFavoritesReadService` to compute `favoritesCount` and `favorited` on every article read |

### 2.5 Favorite → User (moderate)

| Coupling Type | Location | Detail |
|---------------|----------|--------|
| **Data FK** | `article_favorites.user_id` | Links favorites to users |
| **Query** | `ArticleFavoritesReadService.userFavorites()` | Queries which articles a user has favorited |

### 2.6 Cross-cutting: AuthorizationService

`AuthorizationService` imports from all three non-user contexts (`Article`, `Comment`, `User`). It acts as a coupling bridge. In extraction, this must be split — article-side authorization in the Article service, user identity passed as an ID/DTO.

### 2.7 Cross-cutting: GraphQL Schema

The single `schema.graphqls` defines types spanning all four contexts. After extraction, the Article service needs its own schema subset (Article, Comment, Tag types) while the monolith retains User/Profile types.

---

## 3. Shared Infrastructure

| Component | Used By | Extraction Impact |
|-----------|---------|-------------------|
| `Util.isEmpty()` | Article, User | Trivial — copy to both services |
| `JacksonCustomizations` | All | Copy to Article service |
| `MyBatisConfig` | All | Each service gets its own config |
| `CursorPager`, `Page`, etc. | Article, Comment queries | Move to Article service |
| `DateTimeHandler` | All MyBatis mappers | Copy to both services |
| `WebSecurityConfig` / `JwtTokenFilter` | All APIs | Article service needs its own JWT validation |
| Flyway migrations | Single DB | Article service gets its own migration set |

---

## 4. Extraction Boundary Summary

**Moves to Article Service**: Articles, Tags, Comments, Favorites (all four sub-contexts are tightly coupled to each other and loosely coupled to Users)

**Stays in Monolith**: Users, Profiles, Follow Relations, Authentication (JWT)

**Cross-service boundary**: The Article service will reference users by ID only. User profile data (username, bio, image, following) will be fetched via a REST client calling the monolith's `/profiles/{username}` endpoint (or a new internal `/api/users/{id}` endpoint).
