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
mutants with no coverage are reported separately.

| Metric | Before | After |
| --- | ---: | ---: |
| Generated mutants | 71 | 71 |
| Mutants killed | 26 | 33 |
| Mutants survived | 8 | 2 |
| Mutants with no coverage | 37 | 36 |
| Mutation score / mutation coverage | 76.47% | 94.29% |
| Test strength | 76.47% | 94.29% |
| Mutated line coverage | 49/75 (65%) | 53/75 (71%) |

### Per-class results

`n/a` means that all generated mutants for the class had no coverage, so
there were no covered mutants in the score denominator.

| Class | Before killed/survived/no-coverage | Before score | After killed/survived/no-coverage | After score |
| --- | --- | ---: | --- | ---: |
| `Article` | 17 / 4 / 9 | 80.95% | 21 / 1 / 8 | 95.45% |
| `Tag` | 7 / 4 / 9 | 63.64% | 10 / 1 / 9 | 90.91% |
| `NewArticleParam$NewArticleParamBuilder` | 0 / 0 / 6 | n/a | 0 / 0 / 6 | n/a |
| `NewArticleParam` | 0 / 0 / 5 | n/a | 0 / 0 / 5 | n/a |
| `ArticleCommandService` | 0 / 0 / 5 | n/a | 0 / 0 / 5 | n/a |
| `UpdateArticleParam` | 0 / 0 / 3 | n/a | 0 / 0 / 3 | n/a |
| `DuplicatedArticleValidator` | 2 / 0 / 0 | 100% | 2 / 0 / 0 | 100% |

## Survivors targeted

The baseline contained eight surviving mutants, rather than ten; all eight
were reviewed and targeted. Six meaningful behavioral survivors were killed
with assertions in `ArticleTest`. The two remaining survivors are Lombok
`canEqual` return mutants and are equivalent for these non-final, single-class
equality implementations.

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

## Remaining notable survivors

- `Article:16` `canEqual` returning `true`.
- `Tag:10` `canEqual` returning `true`.

These are Lombok-generated helper mutations. The classes have no subclass
hierarchy or extra subclass state whose equality contract would distinguish
the generated type guard from `true`; adding a test solely to kill these
equivalent mutants would be contrived. The remaining no-coverage mutants are
primarily generated accessors/builders and unexecuted command-parameter paths.

## Verification

PIT completed successfully after the test changes with 33 killed and 2
surviving mutants. The ordinary test executions completed 72 tests without
test failures, but the repository's existing JaCoCo verification then failed
the configured 80% bundle threshold:

```text
Rule violated for bundle uc-spring-boot-upgrade-microservice-extraction:
instructions covered ratio is 0.33, but expected minimum is 0.80
```

No production code or existing tests were weakened to address that pre-existing
coverage-gate failure.
