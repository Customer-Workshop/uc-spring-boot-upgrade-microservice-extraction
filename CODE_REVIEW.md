# Code Review: Top 10 Quality Concerns

## 1. Missing Input Validation on Article and User Updates

**Files:** `Article.java`, `User.java`, `UpdateArticleParam.java`, `UpdateUserParam.java`

The `update()` methods on domain entities accept raw strings and only check for emptiness via `Util.isEmpty()`. There is no length validation, no sanitization of HTML/script content in article bodies, and no format validation on fields like `image` (should be a valid URL). The update param DTOs default fields to empty strings (`""`) rather than `null`, making it impossible to distinguish "field not provided" from "field intentionally cleared."

## 2. Inconsistent Error Handling and HTTP Status Codes

**Files:** `UsersApi.java`, `ArticleFavoriteApi.java`, `CurrentUserApi.java`

- `UsersApi.createUser()` returns `201` but uses `ResponseEntity.status(201)` instead of `HttpStatus.CREATED`.
- `ArticleFavoriteApi.favoriteArticle()` does not check whether the user has already favorited the article before saving (duplicate favorites are silently absorbed in the repository layer rather than being reported to the caller).
- `CurrentUserApi.currentUser()` calls `authorization.split(" ")[1]` without validating the header format, risking an `ArrayIndexOutOfBoundsException` if the header is malformed.
- Login failure throws `InvalidAuthenticationException` which maps to `422 Unprocessable Entity`, but `401 Unauthorized` would be more semantically correct.

## 3. Raw `HashMap` Response Envelopes Instead of Typed Response DTOs

**Files:** `ArticlesApi.java`, `CommentsApi.java`, `TagsApi.java`, `CurrentUserApi.java`, `ProfileApi.java`

Nearly every controller builds response bodies using anonymous `HashMap` subclasses:
```java
new HashMap<String, Object>() {{ put("article", data); }}
```
This pattern creates a new anonymous class per call site, lacks type safety, and makes it difficult to evolve the API contract. Typed response wrapper classes would improve maintainability and enable compile-time validation.

## 4. No Authorization Check Before Favorite/Unfavorite

**Files:** `ArticleFavoriteApi.java`

The `favoriteArticle()` and `unfavoriteArticle()` endpoints do not verify that the `@AuthenticationPrincipal User user` is non-null before proceeding. While Spring Security may enforce authentication at the filter level, the controller has no explicit guard, unlike `CommentsApi.deleteComment()` which checks authorization explicitly. A missing or misconfigured security rule would expose an NPE.

## 5. Tight Coupling Between Read and Write Models

**Files:** `ArticleQueryService.java`, `CommentQueryService.java`, `ProfileQueryService.java`

The read-side query services directly depend on infrastructure-layer MyBatis mapper interfaces (`ArticleReadService`, `ArticleFavoritesReadService`, `UserRelationshipQueryService`) rather than abstracting through domain-level read interfaces. This creates a direct dependency from the `application` package to `infrastructure.mybatis.readservice`, violating the layered architecture and making it difficult to swap persistence implementations.

## 6. Missing Null Safety and Defensive Checks

**Files:** `ArticleQueryService.java`, `SecurityUtil.java`, `Article.java`

- `ArticleQueryService.findRecentArticles()` calls `articleReadService.queryArticles()` which may return `null` rather than an empty list, but the result is passed directly to `findArticles()` without a null check.
- `SecurityUtil.getCurrentUser()` checks `authentication.getPrincipal() == null` but does not first check if `authentication` itself is null.
- `Article.toSlug()` will throw `NullPointerException` if `title` is null, but the constructor does not validate the title parameter.

## 7. Password Handling Gaps

**Files:** `UserService.java`, `User.java`, `UserMutation.java`

- `UserService.createUser()` encodes the password before saving, but `UserService.updateUser()` delegates to `User.update()` which stores the raw password string without encoding. The password update path in `UserMutation.updateUser()` does encode via `encryptService`, but the REST path through `CurrentUserApi.updateProfile()` does not.
- There is no minimum password length or complexity validation on either registration or update.
- The `User` entity stores the password alongside other profile fields with no separation of concerns.

## 8. GraphQL Endpoints Duplicate REST Authorization Logic

**Files:** `ArticleMutation.java`, `CommentMutation.java`, `RelationMutation.java`, `UserMutation.java`

Every GraphQL mutation reimplements the authentication check:
```java
User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
```
And authorization checks are copy-pasted between REST controllers and GraphQL mutations rather than being centralized in a shared service. This duplication increases the risk of divergent behavior between the two API surfaces.

## 9. Inconsistent Use of Spring Stereotype Annotations

**Files:** `MyBatisCommentRepository.java`, `ProfileQueryService.java`

- `MyBatisCommentRepository` uses `@Component` while the other repositories use `@Repository`. This means MyBatis exceptions from this repository won't be translated to Spring's `DataAccessException` hierarchy.
- `ProfileQueryService` uses `@Component` while the other query services use `@Service`. Although functionally equivalent, inconsistent annotation usage obscures the intended role of each class.

## 10. No Pagination Limits or Rate Limiting on List Endpoints

**Files:** `ArticlesApi.java`, `Page.java`, `CursorPageParameter.java`

- `Page` caps the limit at 100, but `CursorPageParameter` caps at 1000. The inconsistency means cursor-based queries can return 10x more data than offset-based ones.
- There is no server-side rate limiting on any endpoint. The `getArticles()` endpoint accepts arbitrary filter combinations that could result in expensive database queries (e.g., filtering by tag + author + favorited-by simultaneously with a high limit).
- The `offset` parameter in `Page` has no upper bound, allowing clients to request arbitrarily deep pagination which degrades database performance.
