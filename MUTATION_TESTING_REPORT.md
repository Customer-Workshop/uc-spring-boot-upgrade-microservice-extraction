# Article-domain mutation testing

## Running PIT

Run:

```bash
./gradlew pitest
```

The report is written to `build/reports/pitest/`. It includes HTML, XML, and
CSV output, with a stable (non-timestamped) report directory.

PIT is scoped to:

- `io.spring.core.article.*`
- `io.spring.application.article.*`

The test scope includes the Article unit, application, repository/infrastructure,
and API tests:

- `io.spring.core.article.*`
- `io.spring.application.article.*`
- `io.spring.infrastructure.article.*`
- `io.spring.api.*`

The broader test scope was selected empirically. With only
`io.spring.core.article.*`, PIT killed 2 of 71 generated mutants. With the
broader scope, the frozen baseline killed 26 of 71 mutants and raised mutated
line coverage from 25% to 65%, so the broader scope is materially more useful
for this domain.

## Before and after

The before values below are copied from the frozen capture in
`/home/ubuntu/pit-baseline/`; they were not regenerated after changing tests.
PIT's mutation score and test strength use killed / (killed + survived), while
overall mutation coverage uses killed / generated and therefore makes
no-coverage progress visible.

| Metric | Before | After |
| --- | ---: | ---: |
| Generated mutants | 71 | 71 |
| Mutants killed | 26 | 52 |
| Mutants survived | 8 | 2 |
| Mutants with no coverage | 37 | 17 |
| Mutation score (killed / killed + survived) | 76.47% | 96.30% |
| Overall mutation coverage (killed / generated) | 36.62% | 73.24% |
| Test strength | 76.47% | 96.30% |
| Mutated line coverage | 49/75 (65%) | 75/75 (100%) |

### Per-class results

`n/a` means that all generated mutants for the class had no coverage, so
there were no covered mutants in the score denominator.

| Class | Before killed/survived/no-coverage | Before score | After killed/survived/no-coverage | After score |
| --- | --- | ---: | --- | ---: |
| `Article` | 17 / 4 / 9 | 80.95% | 22 / 1 / 7 | 95.65% |
| `Tag` | 7 / 4 / 9 | 63.64% | 10 / 1 / 9 | 90.91% |
| `NewArticleParam$NewArticleParamBuilder` | 0 / 0 / 6 | n/a | 5 / 0 / 1 | 100% |
| `NewArticleParam` | 0 / 0 / 5 | n/a | 5 / 0 / 0 | 100% |
| `ArticleCommandService` | 0 / 0 / 5 | n/a | 5 / 0 / 0 | 100% |
| `UpdateArticleParam` | 0 / 0 / 3 | n/a | 3 / 0 / 0 | 100% |
| `DuplicatedArticleValidator` | 2 / 0 / 0 | 100% | 2 / 0 / 0 | 100% |

## Survivors targeted

The baseline contained eight surviving mutants, rather than ten. The table
combines all eight baseline survivors with the previously uncovered mutants
selected for meaningful behavioral coverage. The new tests killed all six
non-equivalent baseline survivors and 20 previously no-coverage mutants,
exceeding the requested ten newly killed mutants. The two remaining baseline
survivors are Lombok `canEqual` return mutants and are equivalent for these
non-final, single-class equality implementations.

| Class:line | Mutator | Why it survived | Test added or improved |
| --- | --- | --- | --- |
| `Article:16` | `BooleanTrueReturnValsMutator` (`canEqual`) | Generated equality helper was not exercised against a case distinguishing its type guard | Reviewed; equivalent for the current Article equality hierarchy |
| `Article:16` | `NegateConditionalsMutator` (`equals`) | No direct assertion covered the unequal-ID path | `should_not_equal_articles_with_different_ids` |
| `Article:57` | `NegateConditionalsMutator` (`update`) | No test verified that an empty description is ignored while other fields update | `should_update_description_without_changing_other_fields_when_title_and_body_empty` |
| `Article:61` | `NegateConditionalsMutator` (`update`) | No test verified that an empty body is ignored while other fields update | `should_update_body_without_changing_other_fields_when_title_and_description_empty` |
| `Tag:10` | `BooleanTrueReturnValsMutator` (`canEqual`) | Generated equality helper was not exercised against a case distinguishing its type guard | Reviewed; equivalent for the current Tag equality hierarchy |
| `Tag:10` | `NegateConditionalsMutator` (`equals`) | Tag equality behavior was not directly asserted for equal names | `should_equal_tags_with_same_name_and_reject_different_names` |
| `Tag:10` | `NegateConditionalsMutator` (`equals`) | Tag inequality behavior was not directly asserted for different names | `should_equal_tags_with_same_name_and_reject_different_names` |
| `Tag:10` | `BooleanTrueReturnValsMutator` (`equals`) | Tag equality behavior had no direct assertions | `should_equal_tags_with_same_name_and_reject_different_names` |
| `Article:16` | `BooleanFalseReturnValsMutator` (`equals`) and `BooleanTrueReturnValsMutator` (`equals`) | Article equality branches had no direct assertion for distinct generated IDs | `should_not_equal_articles_with_different_ids` |
| `ArticleCommandService:26` | `VoidMethodCallMutator` (`createArticle`) | Repository save was unexercised by unit tests | `should_create_article_and_save_all_article_fields` |
| `ArticleCommandService:27` | `NullReturnValsMutator` (`createArticle`) | Service return value was unobserved | `should_create_article_and_save_all_article_fields` |
| `ArticleCommandService:31,35,36` | `VoidMethodCallMutator` and `NullReturnValsMutator` (`updateArticle`) | Update delegation, persistence, and return value were unexercised | `should_update_article_and_save_updated_article` |
| `NewArticleParam$NewArticleParamBuilder:15` | `NullReturnValsMutator` (`title`, `description`, `body`, `tagList`, `build`) | Lombok builder mapping was unexercised | `should_map_all_fields_through_builder` |
| `NewArticleParam:15,19,22,25,27` | `NullReturnValsMutator` (`builder`) and `EmptyObjectReturnValsMutator` (getters) | Parameter construction and field access were unexercised | `should_map_all_fields_through_builder` |
| `UpdateArticleParam:13-15` | `EmptyObjectReturnValsMutator` (getters) | Update parameter field mapping was unexercised | `should_map_all_fields_through_constructor` |
| `NewArticleParam:description,body` | Bean validation (`NotBlank`) | No direct validation assertions covered required fields | `should_report_blank_description_and_body` |

## Remaining notable survivors

- `Article:16` `canEqual` returning `true`.
- `Tag:10` `canEqual` returning `true`.

These are Lombok-generated helper mutations. The classes have no subclass
hierarchy or extra subclass state whose equality contract would distinguish
the generated type guard from `true`; adding a test solely to kill these
equivalent mutants would be contrived. One builder mutant remains uncovered
because it is an equivalent/generated builder return path not needed by the
behavioral field-mapping assertion.

## Verification

PIT completed successfully after the test changes with 52 killed and 2
surviving mutants. The focused Article-domain test executions completed
without test failures. The ordinary test executions completed without
test failures, but the repository's existing JaCoCo verification then failed
the configured 80% bundle threshold:

```text
Rule violated for bundle uc-spring-boot-upgrade-microservice-extraction:
instructions covered ratio is 0.33, but expected minimum is 0.80
```

No production code or existing tests were weakened to address that pre-existing
coverage-gate failure.
