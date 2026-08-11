# Mutation Testing Report — Articles Domain

Tooling: [PIT](https://pitest.org/) 1.9.11 via the `info.solidsoft.pitest` Gradle plugin 1.9.11
(configured in `build.gradle`).

```bash
./gradlew pitest            # HTML report: build/reports/pitest/index.html
```

## Scope

`pitest` is scoped to the articles domain only:

| Setting | Value |
| --- | --- |
| `targetClasses` | `io.spring.core.article.*`, `io.spring.application.article.*`, `io.spring.application.ArticleQueryService*`, `io.spring.application.data.Article*`, `io.spring.api.Article*`, `io.spring.infrastructure.repository.MyBatisArticleRepository*` |
| `targetTests` | `io.spring.core.article.*`, `io.spring.application.article.*`, `io.spring.infrastructure.article.*`, `io.spring.api.Article*`, `io.spring.api.ListArticleApiTest` |
| `mutators` | `DEFAULTS` |
| `mutationThreshold` | 90 (build fails below it) |

`lombok.config` sets `lombok.addLombokGeneratedAnnotation = true` so Lombok-generated getters,
setters, builders and `equals`/`hashCode` carry `@lombok.Generated` and are filtered out by PIT
(and JaCoCo). Without it, PIT reports 269 mutations for the same scope, 176 of them in generated
accessors (baseline score 35%), which drowns out the hand-written logic. All numbers below are with
that filter on.

## Mutation score: before vs. after

| Metric | Before | After |
| --- | --- | --- |
| Mutations generated | 93 | 93 |
| Mutations killed | 63 | 93 |
| **Mutation score** | **68%** | **100%** |
| Survived | 12 | 0 |
| No coverage | 18 | 0 |
| Test strength | 84% | 100% |
| Line coverage (in scope) | 83% (222/268) | 100% (268/268) |

Per class:

| Class | Mutations | Killed before | Killed after |
| --- | --- | --- | --- |
| `core.article.Article` | 4 | 2 | 4 |
| `application.ArticleQueryService` | 52 | 31 | 52 |
| `application.article.ArticleCommandService` | 5 | 0 | 5 |
| `application.article.DuplicatedArticleValidator` | 2 | 2 | 2 |
| `application.data.ArticleData` | 1 | 1 | 1 |
| `api.ArticlesApi` | 3 | 1 | 3 |
| `api.ArticleApi` | 10 | 10 | 10 |
| `api.ArticleFavoriteApi` | 6 | 6 | 6 |
| `infrastructure.repository.MyBatisArticleRepository` | 10 | 10 | 10 |

## Top 10 surviving mutants and how they were killed

Surviving mutant = the code was changed and the whole articles test suite still passed. Each entry
lists the mutation, why the old tests missed it, and the assertion that now fails when the mutation
is applied.

| # | Location | Mutation | Why it survived | Test that kills it |
| --- | --- | --- | --- | --- |
| 1 | `Article.update:57` | `RemoveConditional` — `if (!isEmpty(description))` forced to `false`, so the description is never updated | `update()` had no test at all | `ArticleTest.should_update_title_description_and_body_when_values_are_not_empty` / `should_update_only_the_provided_fields` assert `getDescription()` is the new value |
| 2 | `Article.update:61` | Same for the body branch | as above | same tests assert `getBody()` is the new value |
| 3 | `ArticleQueryService.findById:32` | `RemoveConditional` — `articleData == null` forced to `false`, so a missing article is wrapped in `Optional.of(null)` instead of `Optional.empty()` | every test looked up an existing article | `ArticleQueryServiceTest.should_get_empty_optional_for_unknown_article_id` asserts `findById("not-exist-id", …)` is empty (mutant throws NPE) |
| 4 | `ArticleQueryService.fillExtraInfo:180` | `VoidMethodCall` — removed `ProfileData.setFollowing(...)` on the single-article path | following was only asserted on the list path (`findRecentArticles`) | `should_show_following_author_when_fetching_single_article` asserts `isFollowing()` true for a follower and false for a stranger |
| 5 | `ArticleQueryService.findRecentArticlesWithCursor:65` | `ConditionalsBoundary` — `size() > limit` → `size() >= limit` | every cursor test used `limit = 20` with 2 articles, so the boundary was never reached | `ArticleQueryServiceCursorTest.should_not_flag_next_page_when_article_ids_fit_the_limit` asserts `hasNext()` false when size == limit (mutant drops an element / flags a next page) |
| 6 | `ArticleQueryService.findRecentArticlesWithCursor:65` | `RemoveConditional (ORDER)` — `hasExtra` forced to `false` | no test ever produced an extra row | `should_drop_extra_article_id_and_keep_order_when_paging_by_cursor` asserts `hasNext()` true and that only the first `limit` ids reach `findArticles` |
| 7 | `ArticleQueryService.findRecentArticlesWithCursor:66` | `RemoveConditional` — `if (hasExtra)` forced to `false`, extra id never removed | as above | same test — the captured `findArticles` argument would contain 3 ids instead of 2 |
| 8 | `ArticleQueryService.findRecentArticlesWithCursor:69/70` | `RemoveConditional` + `VoidMethodCall` — `Collections.reverse(articleIds)` skipped for `PREV` paging | the reversal is invisible through the DB (the `findArticles` SQL re-sorts by `created_at desc`) | `should_reverse_article_ids_when_paging_backwards_by_cursor` stubs the read service and asserts the id order handed to `findArticles` |
| 9 | `ArticleQueryService.findRecentArticlesWithCursor:74` | `VoidMethodCall` — removed `fillExtraInfo(articles, currentUser)`, so cursor pages lose favorite/following info | cursor tests only asserted ids and sizes | `ArticleQueryServiceTest.should_fill_favorite_and_following_info_when_paging_by_cursor` asserts `favoritesCount`, `favorited` and `author.following` |
| 10 | `ArticlesApi.getArticles:56` and `ArticlesApi.getFeed:45` | `NullReturnVals` — controller returns `null` (still HTTP 200, empty body) | `ListArticleApiTest` only asserted `statusCode(200)` | `ListArticleApiTest.should_get_default_article_list` / `should_get_feeds_success` assert `articlesCount`, `articles.size()`, slug/title/description/body and author |

## Additional mutants killed (uncovered code)

The 18 `NO_COVERAGE` mutants were also addressed, since untested code is a superset of the problem:

- `ArticleCommandService.createArticle/updateArticle` (5 mutants: removed `repository.save`, removed
  `article.update`, `null` returns) — new `ArticleCommandServiceTest` verifies the saved article via
  an `ArgumentCaptor` and asserts the returned/updated fields.
- `ArticleQueryService.findBySlug` (4 mutants) — `should_fetch_article_by_slug` and
  `should_fill_extra_info_when_fetching_article_by_slug`.
- `ArticleQueryService.findUserFeedWithCursor` (9 mutants: boundary, extra-element removal, reverse,
  `null` returns) — `should_get_user_feed_by_cursor` plus the stubbed
  `should_drop_extra_feed_article_and_keep_order`, `should_reverse_feed_articles_when_paging_backwards`,
  `should_not_flag_next_page_when_feed_articles_fit_the_limit` and
  `should_return_empty_pager_when_user_follows_nobody`.

## Tests added or improved

| File | Change |
| --- | --- |
| `src/test/java/io/spring/core/article/ArticleTest.java` | 4 tests for `update()` semantics (per-field updates, empty values ignored, slug refresh, `updatedAt` refresh) |
| `src/test/java/io/spring/application/article/ArticleQueryServiceTest.java` | 6 tests: unknown id, following on single-article fetch, cursor extra-element trimming, size == limit, backwards paging order, extra info on cursor pages, slug lookups, feed by cursor |
| `src/test/java/io/spring/application/article/ArticleQueryServiceCursorTest.java` | New Mockito-based test for cursor pagination boundaries and ordering (list sizes/order cannot be forced through the MyBatis queries, which re-sort results) |
| `src/test/java/io/spring/application/article/ArticleCommandServiceTest.java` | New test covering create/update command paths and repository persistence |
| `src/test/java/io/spring/api/ListArticleApiTest.java` | Response-body assertions on `GET /articles` and `GET /articles/feed` instead of status code only |

## Notes

- Two mutants (`Collections.reverse` on the `findRecentArticlesWithCursor` PREV path) are
  functionally equivalent when exercised through the database, because `findArticles` re-orders by
  `created_at desc`. They are killed at the unit level by asserting the id order passed to the read
  service, which is the contract the method actually implements.
- The JaCoCo gate is unchanged; `./gradlew build -x jacocoTestCoverageVerification` remains the
  documented build command for this repository.
