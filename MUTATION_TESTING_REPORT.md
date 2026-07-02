# Mutation Testing Report — Articles Domain

## Overview

PIT mutation testing was configured for the Articles domain packages (`io.spring.core.article` and `io.spring.application.article`) to evaluate and improve test effectiveness.

**Run command:** `JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew pitest`

---

## Before / After Summary

| Metric                    | Before  | After   |
|---------------------------|---------|---------|
| Mutations Generated       | 69      | 69      |
| Mutations Killed          | 9       | 54      |
| **Mutation Kill Rate**    | **13%** | **78%** |
| No Coverage               | 57      | 11      |
| Survived                  | 3       | 4       |
| **Test Strength**         | **75%** | **93%** |
| Line Coverage             | 39%     | 92%     |
| Tests Run per Mutation    | 0.71    | 1.65    |

---

## Top 10 Surviving Mutants Targeted

### 1. `Article::getTitle` — replaced return with `""` (SURVIVED → KILLED)
- **Mutator:** EmptyObjectReturnValsMutator
- **Fix:** Added `should_store_title_description_and_body` test asserting `assertEquals("My Title", article.getTitle())`

### 2. `Article::getDescription` — replaced return with `""` (SURVIVED → KILLED)
- **Mutator:** EmptyObjectReturnValsMutator
- **Fix:** Same test as #1 asserts `assertEquals("My Description", article.getDescription())`

### 3. `Article::getBody` — replaced return with `""` (SURVIVED → KILLED)
- **Mutator:** EmptyObjectReturnValsMutator
- **Fix:** Same test as #1 asserts `assertEquals("My Body", article.getBody())`

### 4. `Article::update` — removed title conditional (NO_COVERAGE → KILLED)
- **Mutator:** RemoveConditionalMutator (line 52)
- **Fix:** Added `should_update_title_and_slug` test verifying title/slug change and `should_not_update_when_all_params_empty` verifying no change on empty input

### 5. `Article::update` — removed description conditional (NO_COVERAGE → KILLED)
- **Mutator:** RemoveConditionalMutator (line 57)
- **Fix:** Added `should_update_description` test verifying description changes when non-empty

### 6. `Article::update` — removed body conditional (NO_COVERAGE → KILLED)
- **Mutator:** RemoveConditionalMutator (line 61)
- **Fix:** Added `should_update_body` test verifying body changes when non-empty

### 7. `ArticleCommandService::createArticle` — removed save call (NO_COVERAGE → KILLED)
- **Mutator:** VoidMethodCallMutator (line 26)
- **Fix:** Added `ArticleCommandServiceTest.should_create_article_and_save` with `verify(articleRepository).save(any(Article.class))`

### 8. `ArticleCommandService::createArticle` — null return (NO_COVERAGE → KILLED)
- **Mutator:** NullReturnValsMutator (line 27)
- **Fix:** Same test asserts `assertNotNull(article)` and verifies field values

### 9. `ArticleCommandService::updateArticle` — removed update call (NO_COVERAGE → KILLED)
- **Mutator:** VoidMethodCallMutator (line 31)
- **Fix:** Added `should_update_article_title` test verifying the article fields change after update

### 10. `ArticleCommandService::updateArticle` — removed save call (NO_COVERAGE → KILLED)
- **Mutator:** VoidMethodCallMutator (line 35)
- **Fix:** Same test uses `verify(articleRepository).save(article)` to ensure persistence

---

## Additional Mutants Killed

Beyond the top 10, several more mutants were killed by the new tests:

- **Article/Tag `equals`/`hashCode`** — 12 Lombok-generated mutants killed via null-field equality tests, hashCode value assertions (`assertEquals(59 + id.hashCode(), article.hashCode())`), and different-id/different-name hashCode inequality checks
- **Article `update` method** — all 3 conditional mutants killed; updatedAt timestamp assertions confirm conditional branches execute
- **Tag getters/setters** — name and id return-value mutants killed through direct assertions

---

## Remaining Survivors (4)

All 4 remaining survivors are in Lombok-generated `canEqual` methods — these are **equivalent mutants** in this codebase:

| Class   | Mutant                                              | Why It Survives |
|---------|-----------------------------------------------------|-----------------|
| Article | `canEqual` returns `true` instead of `instanceof`   | No subclass with its own `@EqualsAndHashCode` exists; `canEqual` always returns true for same-class comparisons |
| Article | `equals` skips `canEqual` check                     | Same reason — without subclasses, `canEqual` is always true, so skipping it is equivalent |
| Tag     | `canEqual` returns `true` instead of `instanceof`   | Same — no subclass override |
| Tag     | `equals` skips `canEqual` check                     | Same |

These are standard Lombok equivalent mutants that only become relevant if a subclass introduces its own `@EqualsAndHashCode`. They do not represent a testing gap for the current codebase.

---

## Remaining No-Coverage Mutations (11)

| Class                                | Count | Reason |
|--------------------------------------|-------|--------|
| `NewArticleParam$Builder` (Lombok)   | 6     | Lombok-generated builder chain methods; exercised indirectly via integration tests |
| `NewArticleParam` (Lombok)           | 1     | Lombok `builder()` static factory |
| `DuplicatedArticleValidator`         | 2     | Requires Spring validation context; covered by API-level integration tests |
| `Article` (anonymous subclass)       | 1     | Generated in test; not production code |
| `Tag::toString` (Lombok)             | 1     | Lombok `@Data` toString; cosmetic |

---

## Test Files Modified/Created

- **`src/test/java/io/spring/core/article/ArticleTest.java`** — Enhanced from 5 to 19 tests covering construction, field storage, update logic, and equals/hashCode contract
- **`src/test/java/io/spring/core/article/TagTest.java`** — New file with 12 tests covering Tag creation, equality, hashCode, and collection behavior
- **`src/test/java/io/spring/application/article/ArticleCommandServiceTest.java`** — New file with 4 Mockito-based unit tests for `ArticleCommandService` create/update operations
