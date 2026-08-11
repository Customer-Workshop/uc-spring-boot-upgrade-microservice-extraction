# Mutation Testing Report — Articles Domain

Mutation testing with [PIT](https://pitest.org/) (`info.solidsoft.pitest` Gradle plugin 1.9.11, PIT 1.9.11,
JUnit 5 plugin 1.0.0) scoped to the Articles domain:

- `io.spring.core.article.*` — `Article`, `Tag`
- `io.spring.application.article.*` — `ArticleCommandService`, `NewArticleParam`, `UpdateArticleParam`,
  `DuplicatedArticleValidator`

## Running it

```bash
./gradlew pitest
```

Reports: `build/reports/pitest/index.html` (HTML) and `build/reports/pitest/mutations.xml` (XML).
The Selenium E2E suite is excluded from the mutation run (`excludedTestClasses = ['io.spring.selenium.*']`).

## Scores

| Metric | Before | After |
| --- | --- | --- |
| Mutations generated | 71 | 71 |
| Mutations killed | 26 | 63 |
| **Mutation score** | **37%** | **89%** |
| Line coverage (of mutated classes) | 49/75 (65%) | 75/75 (100%) |
| Test strength (killed / covered) | 76% | 91% |
| Survived | 8 | 8 → 6 (all equivalent, see below) |
| No coverage | 37 | 2 |

Per class:

| Class | Before (killed/total) | After (killed/total) |
| --- | --- | --- |
| `Article` | 17/30 | 26/30 |
| `Tag` | 7/20 | 16/20 |
| `ArticleCommandService` | 0/5 | 5/5 |
| `NewArticleParam` (+ builder) | 0/11 | 11/11 |
| `UpdateArticleParam` | 0/3 | 3/3 |
| `DuplicatedArticleValidator` | 2/2 | 2/2 |

## Top 10 mutants that were not killed, and how they were killed

The Articles domain had no unit test for `ArticleCommandService` or for the command params at all, and
`Article.update` was only partially exercised, so the highest-value mutants were a mix of `SURVIVED`
(covered but undetected) and `NO_COVERAGE`.

| # | Mutant | Status before | Why it survived | Test added |
| --- | --- | --- | --- | --- |
| 1 | `ArticleCommandService.createArticle` — removed call to `ArticleRepository::save` | NO_COVERAGE | Nothing exercised the service; creation was never persisted-checked | `ArticleCommandServiceTest.should_create_and_save_article` verifies `save` with an `ArgumentCaptor` and asserts the captured article |
| 2 | `ArticleCommandService.createArticle` — replaced return value with `null` | NO_COVERAGE | Return value never inspected | same test asserts the returned article and all of its fields (`title`, `slug`, `description`, `body`, tags, `userId`) |
| 3 | `ArticleCommandService.updateArticle` — removed call to `Article::update` | NO_COVERAGE | Update path untested | `should_update_and_save_article` asserts the article's fields and slug actually changed |
| 4 | `ArticleCommandService.updateArticle` — removed call to `ArticleRepository::save` | NO_COVERAGE | Persistence never verified | `should_update_and_save_article` does `verify(articleRepository, times(1)).save(article)` |
| 5 | `ArticleCommandService.updateArticle` — replaced return value with `null` | NO_COVERAGE | Return value never inspected | `should_update_and_save_article` asserts the returned instance is the same article and is non-null |
| 6 | `Article.update:57` — negated conditional on `!Util.isEmpty(description)` | SURVIVED | Existing tests only ever passed a non-empty description, so both branches produced the same result | `ArticleTest.should_not_update_description_with_empty_value` (plus `should_update_title_description_and_body` for the positive branch) |
| 7 | `Article.update:61` — negated conditional on `!Util.isEmpty(body)` | SURVIVED | Same as above for `body` | `ArticleTest.should_not_update_body_with_empty_value` |
| 8 | `Article.equals` — negated conditional / boolean return substitutions on the `id` comparison | SURVIVED + NO_COVERAGE | Equality was only ever used implicitly; null-id and wrong-type comparisons were never made | `ArticleTest.should_only_be_equal_to_article_with_same_id` covers self, different id, `null`, wrong type, null-id-vs-id, and both-null-id |
| 9 | `Tag.equals` — negated conditionals and boolean return substitutions on the `name` comparison | SURVIVED (3) + NO_COVERAGE | No test for `Tag` existed; tag equality is what de-duplicates tags on an article | `TagTest.should_only_be_equal_to_tag_with_same_name` plus `ArticleTest.should_deduplicate_tags` |
| 10 | `NewArticleParam` builder / getters and `UpdateArticleParam` getters — `null` and `""` return substitutions (14 mutants) | NO_COVERAGE | The params were only ever built by Jackson in web-layer tests, never asserted | `ArticleParamTest` builds via the builder, asserts every getter, the builder `toString`, and the empty-string defaults of `UpdateArticleParam` |

`Article.hashCode`'s "replaced int return with 0" and null-check mutants were also killed by
`ArticleTest.should_have_hash_code_consistent_with_id` (and the `Tag` equivalent in `TagTest`).

## Remaining survivors (6) — all equivalent mutants

All remaining mutants are in Lombok-generated `equals`/`hashCode` boilerplate and cannot be killed by a
behaviour-preserving test:

- `Article.canEqual` / `Tag.canEqual` — "replaced boolean return with `true`" (2). `canEqual` is only ever
  called from the generated `equals`, after an `instanceof` check has already succeeded, so the mutated
  return value is unreachable in any observable state.
- `Article.hashCode` / `Tag.hashCode` — "replaced integer multiplication with division" and "replaced
  integer addition with subtraction" (4). These change the hash value but preserve the `equals`/`hashCode`
  contract, so only an assertion hard-coding Lombok's internal hashing constants would detect them — a
  brittle test with no value.

The 2 remaining `NO_COVERAGE` mutants ("replaced boolean return with `true`" in `Article.equals` /
`Tag.equals`) sit on the unreachable `return true` tail of the generated `equals` and are reported as
uncovered for the same reason.

## Notes

- The pre-existing repository-wide JaCoCo gate (`jacocoTestCoverageVerification`, minimum 0.80) is
  unrelated to this work and still fails on the whole-project instruction ratio, as before; per
  `AGENTS.md`, run builds with `-x jacocoTestCoverageVerification`.
