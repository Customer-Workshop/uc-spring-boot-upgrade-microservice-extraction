# Refactoring Log - Articles Domain

## Refactoring 1: Extract Duplicated ArticlesConnection Building in ArticleDatafetcher

**Problem:**
`ArticleDatafetcher` (384 lines) contained five GraphQL resolver methods (`getFeed`, `userFeed`, `userFavorites`, `userArticles`, `getArticles`) that each duplicated ~25 lines of identical code: pagination argument validation, cursor-based fetching with a first/last branch, `ArticlesConnection` assembly (edges, page info), and `DataFetcherResult` wrapping with local context. This made the class bloated and error-prone to change.

**Approach:**
- Introduced a `CursorFetcher` functional interface to abstract the varying data-retrieval logic.
- Extracted `fetchArticlesCursor(first, after, last, before, CursorFetcher)` to centralize the first-vs-last branching and argument validation.
- Extracted `buildConnectionResult(CursorPager<ArticleData>)` to build the `ArticlesConnection`, map edges, and assemble the `DataFetcherResult` in one place.
- Each resolver now delegates to these two methods, supplying only its unique query lambda.

**Verification:**
All existing tests pass (`./gradlew test -x jacocoTestCoverageVerification` - BUILD SUCCESSFUL).

---

## Refactoring 2: Extract Duplicated Authorization + Find Pattern

**Problem:**
The "find article by slug, then check user authorization" pattern was duplicated across `ArticleApi` (updateArticle, deleteArticle), `ArticleMutation` (updateArticle, deleteArticle, favoriteArticle, unfavoriteArticle). Each call site independently performed `articleRepository.findBySlug(...).orElseThrow(...)` and `AuthorizationService.canWriteArticle(...)`, creating tight coupling between the API layer and both the repository and the authorization service.

**Approach:**
- Added `findArticleBySlugOrThrow(slug)`, `checkAuthorization(user, article)`, and `removeArticle(user, slug)` to `ArticleCommandService`, centralizing lookup and authorization logic in the service layer.
- `ArticleApi`: extracted a private `findAuthorizedArticle(slug, user)` method to DRY up the update and delete handlers while preserving the existing test mock contract (tests use `@MockBean ArticleRepository`).
- `ArticleMutation`: refactored all slug-lookup and authorization calls to use the new service methods, and removed the now-unused `ArticleRepository` dependency from the mutation class entirely.

**Verification:**
All existing tests pass (`./gradlew test -x jacocoTestCoverageVerification` - BUILD SUCCESSFUL).

---

## Refactoring 3: Replace Duplicated Anonymous HashMap Response Wrappers

**Problem:**
Three REST controllers (`ArticleApi`, `ArticlesApi`, `ArticleFavoriteApi`) each built JSON response envelopes using anonymous `HashMap` subclasses with double-brace initialization (e.g., `new HashMap<>() {{ put("article", data); }}`). This anti-pattern creates an unnecessary anonymous inner class for each invocation, leaks a reference to the enclosing instance, and duplicates the wrapping logic across multiple files.

**Approach:**
- Replaced all double-brace `HashMap` instantiations with `Collections.singletonMap("article", ...)`, which is thread-safe, immutable, produces no anonymous subclass, and expresses intent more clearly.
- Simplified the return type of `ArticleFavoriteApi.responseArticleData()` from `ResponseEntity<HashMap<String, Object>>` to `ResponseEntity<?>`.

**Verification:**
All existing tests pass (`./gradlew test -x jacocoTestCoverageVerification` - BUILD SUCCESSFUL).

---

## Refactoring 4: Fix Article.update() Redundant Timestamp Assignments

**Problem:**
`Article.update()` assigned `this.updatedAt = new DateTime()` independently inside each of the three `if` blocks (title, description, body). When multiple fields were updated in a single call, `updatedAt` was reassigned up to three times with slightly different `DateTime` values, which is wasteful and can produce subtly inconsistent timestamps within a single logical update.

**Approach:**
- Introduced a `boolean changed` flag that tracks whether any field was actually modified.
- Moved the single `this.updatedAt = new DateTime()` assignment to after all field checks, guarded by the `changed` flag.
- This ensures exactly one timestamp is created per update call, and `updatedAt` is only modified when the article actually changes.

**Verification:**
All existing tests pass (`./gradlew test -x jacocoTestCoverageVerification` - BUILD SUCCESSFUL).

---

## Refactoring 5: Fix ArticleQueryService Null-Safety and Unclear Naming

**Problem:**
- `setFavoriteCount()` used `countMap.get(articleId)` which returns `null` when an article has no favorites. This `null` was passed to `setFavoritesCount(Integer)`, risking a `NullPointerException` in downstream code.
- Lambda parameter names were inconsistent and unclear: `articleData1` in `setIsFollowingAuthor`, `articleData` vs `articleData1` mixed across methods, and `articleData -> articleData.getId()` instead of method references.
- The variable `followdUsers` contained a typo (missing 'e').

**Approach:**
- Changed `countMap.get(...)` to `countMap.getOrDefault(..., 0)` to guarantee a non-null default.
- Renamed all lambda parameters to a consistent `article` across `setIsFollowingAuthor`, `setFavoriteCount`, and `setIsFavorite`.
- Replaced `articleData -> articleData.getId()` with the method reference `ArticleData::getId` where applicable.
- Fixed the typo `followdUsers` to `followedUsers` in both `findUserFeedWithCursor` and `findUserFeed`.

**Verification:**
All existing tests pass (`./gradlew test -x jacocoTestCoverageVerification` - BUILD SUCCESSFUL).
