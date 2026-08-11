# Mutation Testing Report — Articles Domain

PIT (pitest) mutation testing was set up via the `info.solidsoft.pitest` Gradle plugin
(v1.9.11, PIT 1.9.11, JUnit 5 plugin 1.1.2), targeting the Articles domain:

- `io.spring.core.article.*` (`Article`, `Tag`, `ArticleRepository`)
- `io.spring.application.article.*` (`ArticleCommandService`, `NewArticleParam`, `UpdateArticleParam`, ...)

Run it with:

```bash
./gradlew pitest
# HTML report: build/reports/pitest/index.html
```

## Before / After Scores

| Metric | Before | After |
|---|---|---|
| Line coverage (PIT) | 49/75 (65%) | 75/75 (100%) |
| Mutations generated | 71 | 71 |
| Mutations killed | 26 (37%) | 63 (89%) |
| Mutations with no coverage | 37 | 4 |
| Test strength (killed / covered) | 76% | 94% |

## Top 10 Surviving Mutants and How They Were Killed

| # | Class / Method | Mutation | Status before | Killed by |
|---|---|---|---|---|
| 1 | `Article.update` (L57) | negated conditional on the `description` empty-check | SURVIVED | `update_with_only_description_should_change_description_only` and `update_with_empty_values_should_change_nothing` in `ArticleTest` assert that only the description changes when provided and nothing changes for empty input |
| 2 | `Article.update` (L61) | negated conditional on the `body` empty-check | SURVIVED | `update_with_only_body_should_change_body_only` and `update_with_empty_values_should_change_nothing` assert body-only updates and no-op behavior |
| 3 | `Article.equals` (L16) | negated conditional in id comparison | SURVIVED | `should_be_equal_to_itself_and_not_to_different_article` asserts reflexive equality and inequality of articles with different ids |
| 4 | `Article.canEqual` (L16) | replaced boolean return with `true` | SURVIVED | `can_equal_should_reject_non_article_types` asserts `canEqual` returns `false` for a non-`Article` argument |
| 5 | `Tag.equals` (L10) | negated conditional on `name` comparison | SURVIVED | `tags_with_same_name_should_be_equal_with_same_hash_code` / `tags_with_different_names_should_not_be_equal` in the new `TagTest` |
| 6 | `Tag.equals` (L10) | negated conditional on null-name branch | SURVIVED | `tag_should_not_equal_null_name_tag_or_other_types` compares tags with null vs non-null names in both directions |
| 7 | `Tag.equals` (L10) | replaced boolean return with `true` | SURVIVED | `tag_should_not_equal_null_name_tag_or_other_types` asserts inequality against `null` and other types |
| 8 | `Tag.canEqual` (L10) | replaced boolean return with `true` | SURVIVED | `can_equal_should_reject_non_tag_types` asserts `canEqual` returns `false` for a non-`Tag` argument |
| 9 | `ArticleCommandService.createArticle` (L26/L27) | removed call to `ArticleRepository::save`; replaced return value with `null` | NO_COVERAGE | New `ArticleCommandServiceTest.should_create_article_and_save_it` verifies `save(created)` is invoked and asserts all fields of the returned article |
| 10 | `ArticleCommandService.updateArticle` (L31/L35/L36) | removed call to `Article::update`; removed call to `ArticleRepository::save`; replaced return value with `null` | NO_COVERAGE | New `ArticleCommandServiceTest.should_update_article_and_save_it` asserts the article's fields are updated, the same instance is returned, and `save` is invoked |

Additional mutants killed along the way (previously NO_COVERAGE): `Article`/`Tag`
`hashCode` (negated conditional, "replaced int return with 0"), `Tag.toString`,
`NewArticleParam` builder and getters, and `UpdateArticleParam` getters — all now
exercised by `ArticleTest`, `TagTest`, and `ArticleCommandServiceTest`.

## Remaining Survivors (8 mutants)

All remaining mutants live in Lombok-generated `equals`/`hashCode`/`toString` code:

- `Article.hashCode` / `Tag.hashCode`: "Replaced integer multiplication with division" and
  "Replaced integer addition with subtraction" in the Lombok hash formula
  (`result * 59 + fieldHash`). These are practically equivalent mutants — `hashCode` has no
  correctness contract other than "equal objects hash equally", which still holds under
  these mutations for the single-field hash used here.
- `Article.equals` / `Tag.equals` "replaced boolean return with true/false" and the
  `NewArticleParamBuilder.toString` mutant sit on Lombok-generated branches that are
  unreachable in practice (e.g. the `canEqual` symmetry branch), so no test can cover them.

## Test Changes

- `src/test/java/io/spring/core/article/ArticleTest.java` — added equality/hashCode/canEqual
  tests and four `update` field-isolation tests.
- `src/test/java/io/spring/core/article/TagTest.java` — new test class for `Tag`
  equality, hashCode, canEqual, and toString.
- `src/test/java/io/spring/application/article/ArticleCommandServiceTest.java` — new
  Mockito-based unit test for `ArticleCommandService` create/update flows.
