# How to Do Mutation Testing — A Step-by-Step Guide

> A practical, hands-on walkthrough for running mutation testing, tailored to this
> repository (Spring Boot 2.6.3 / Java 11 / Gradle / JUnit 5) using **PIT (pitest)**,
> plus generic instructions for Maven and JavaScript projects.
>
> For the conceptual background — what mutation testing is, mutation operators, metrics,
> theory — see the companion guide: [Mutation Testing — The Complete Guide](mutation-testing-guide.md).

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Step 1: Add PIT to the Gradle Build](#2-step-1-add-pit-to-the-gradle-build)
3. [Step 2: Configure PIT for This Repository](#3-step-2-configure-pit-for-this-repository)
4. [Step 3: Run Your First Mutation Test](#4-step-3-run-your-first-mutation-test)
5. [Step 4: Read the Report](#5-step-4-read-the-report)
6. [Step 5: Kill Your First Surviving Mutant](#6-step-5-kill-your-first-surviving-mutant)
7. [Step 6: Iterate — The Mutation Testing Workflow](#7-step-6-iterate--the-mutation-testing-workflow)
8. [Step 7: Add It to CI](#8-step-7-add-it-to-ci)
9. [Tuning and Troubleshooting](#9-tuning-and-troubleshooting)
10. [Doing It in Other Ecosystems (Maven, JS/TS, Python)](#10-doing-it-in-other-ecosystems-maven-jsts-python)
11. [Quick Reference Cheat Sheet](#11-quick-reference-cheat-sheet)

---

## 1. Prerequisites

- A **green test suite**: PIT refuses to run if the baseline fails. Verify first:

  ```bash
  ./gradlew test -x jacocoTestCoverageVerification
  ```

  (Per `AGENTS.md`, skip the JaCoCo coverage gate rather than fighting it.)

- **Fast unit tests** to mutate against. In this repo, the JUnit 5 tests under
  `src/test/java/io/spring/` (excluding `selenium/`) are the right target. The Selenium
  E2E suite is already excluded from the `test` task, which is exactly what we want —
  never run E2E tests inside a mutation run.

- Know your **target packages**. Here, the highest-value targets are:
  - `io.spring.core.*` — domain entities and business rules (`Article`, `User`, ...)
  - `io.spring.application.*` — CQRS read services, pagination, validation
  - `io.spring.Util` — shared utility used by all `update` methods

  Low-value targets to exclude: DTOs, `api/` wiring, generated DGS classes, config.

---

## 2. Step 1: Add PIT to the Gradle Build

Add the PIT Gradle plugin to the `plugins` block of `build.gradle`:

```groovy
plugins {
    id 'org.springframework.boot' version '2.6.3'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id "com.netflix.dgs.codegen" version "5.0.6"
    id "com.diffplug.spotless" version "6.2.1"
    id 'jacoco'
    id 'info.solidsoft.pitest' version '1.9.11'   // ← add this
}
```

Because this project uses **JUnit 5**, PIT needs the JUnit 5 plugin as well:

```groovy
pitest {
    junit5PluginVersion = '1.1.2'   // required for JUnit Platform / Jupiter
}
```

> Version notes (for this repo's Java 11 / Gradle toolchain):
> - Gradle plugin `info.solidsoft.pitest` **1.9.11** pulls pitest core **1.9.x**, which
>   supports Java 11 bytecode fine.
> - `junit5PluginVersion` **1.1.2** matches pitest 1.9.x. If you bump pitest, check the
>   [compatibility table](https://github.com/pitest/pitest-junit5-plugin#compatibility).

Sanity check that the task now exists:

```bash
./gradlew tasks --all | grep -i pitest
```

---

## 3. Step 2: Configure PIT for This Repository

A full, recommended configuration block for `build.gradle`:

```groovy
pitest {
    junit5PluginVersion = '1.1.2'

    // WHAT to mutate — business logic only
    targetClasses = [
        'io.spring.core.*',
        'io.spring.application.*',
        'io.spring.Util'
    ]

    // WHICH tests to run — fast unit tests only (Selenium already excluded from 'test')
    targetTests = ['io.spring.*']
    excludedTestClasses = ['io.spring.selenium.*']

    // Mutators: start with the balanced default set
    mutators = ['DEFAULTS']

    // Performance
    threads = 4                       // ≈ number of CPU cores
    timeoutConstInMillis = 4000       // per-test timeout floor for infinite-loop mutants

    // Output
    outputFormats = ['HTML', 'XML']
    timestampedReports = false        // stable path: build/reports/pitest/index.html

    // Incremental analysis — makes re-runs dramatically faster
    enableDefaultIncrementalAnalysis = true

    // Quality gates — enable these LATER, once you know your baseline (see Step 7)
    // mutationThreshold = 70
    // coverageThreshold = 80
}
```

Also tell PIT to skip Lombok-generated code (this repo uses Lombok heavily — `@Getter`,
`@EqualsAndHashCode`, `@NoArgsConstructor`). Create/extend `lombok.config` at the repo
root:

```
config.stopBubbling = true
lombok.addLombokGeneratedAnnotation = true
```

Without this, PIT wastes time mutating generated getters/equals and floods the report
with un-killable noise.

---

## 4. Step 3: Run Your First Mutation Test

### Full scoped run

```bash
./gradlew pitest
```

Expected console output shape:

```
================================================================================
- Statistics
================================================================================
>> Line Coverage: 312/389 (80%)
>> Generated 274 mutations Killed 201 (73%)
>> Mutations with no coverage 31. Test strength 83%
>> Ran 512 tests (1.87 tests per mutation)
```

### Scoped run for a fast feedback loop

While working on one class, restrict scope from the command line instead of editing the
build file:

```bash
./gradlew pitest \
  -Ppitest.targetClasses=io.spring.core.article.Article \
  -Ppitest.targetTests=io.spring.core.article.*
```

(Or temporarily narrow `targetClasses` in `build.gradle`.) A single-class run finishes in
seconds, which is the loop you want while writing mutant-killing tests.

---

## 5. Step 4: Read the Report

Open the HTML report:

```bash
open build/reports/pitest/index.html        # macOS
xdg-open build/reports/pitest/index.html    # Linux
```

### Report anatomy

- **Package index** — per-package line coverage, mutation coverage, and test strength.
  Sort by mutation coverage; the worst packages are your starting point.
- **Class page** — the source code with line highlighting:
  - **Green line** — all mutants on this line were killed.
  - **Red line** — at least one mutant survived.
  - **Pale/pink** — mutants exist but no test covers the line (NO_COVERAGE).
- **Mutant list** (bottom of class page, or hover on the line number) — each entry names
  the mutator and status, e.g.:

  ```
  1. changed conditional boundary → SURVIVED
  2. negated conditional → KILLED (io.spring.core.article.ArticleTest.shouldUpdateTitle)
  3. removed call to io/spring/core/article/Article::toSlug → SURVIVED
  ```

### Triage each surviving mutant — the decision tree

```
Surviving mutant
 ├─ Is the mutated behavior something users/business depend on?
 │    └─ YES → write a test that pins it down (Step 5). Most survivors land here.
 ├─ Is the code dead / unnecessary?
 │    └─ YES → delete the code. (Mutation testing is great at finding dead code.)
 └─ Is the mutant equivalent (behaviorally identical)?
      └─ YES → ignore it, or exclude the mutator/line. Don't burn time chasing it.
```

---

## 6. Step 5: Kill Your First Surviving Mutant

A concrete end-to-end example using this repository's `Article` class.

### 6.1 The survivor

Suppose the report shows in `Article.update(...)`:

```
Line 55: removed call to io/spring/core/article/Article::toSlug → SURVIVED
```

Meaning: no test verifies that changing an article's **title** also regenerates its
**slug**. Since slugs are the article's URL identity, this is a real gap — a refactor
that dropped slug regeneration would ship silently.

### 6.2 Write the killing test

Add to the article unit tests:

```java
@Test
void updatingTitleRegeneratesSlug() {
  Article article =
      new Article("Old Title", "desc", "body", List.of("java"), "user-1");
  assertEquals("old-title", article.getSlug());

  article.update("Brand New Title", "", "");

  assertEquals("brand-new-title", article.getSlug());   // kills the mutant
  assertEquals("Brand New Title", article.getTitle());
}
```

Note the assertions are **exact**. `assertNotNull(article.getSlug())` would *not* kill
the mutant.

### 6.3 Verify the kill

```bash
./gradlew test  # confirm the new test passes on real code
./gradlew pitest
```

The report should now show that line green:

```
Line 55: removed call to io/spring/core/article/Article::toSlug
         → KILLED (ArticleTest.updatingTitleRegeneratesSlug)
```

### 6.4 More kill patterns (survivor → test technique)

| Surviving mutator | What it means | Killing technique |
|---|---|---|
| `changed conditional boundary` (`>=` → `>`) | Boundary value untested | Add a test with the *exact* boundary input |
| `negated conditional` | Only one branch asserted | Add a test for the other branch with real assertions |
| `replaced boolean return with true/false` | Return value never asserted for one outcome | Assert both `true` and `false` cases |
| `removed call to <void method>` | Side effect unverified | `verify(mock).method(...)` (Mockito) or assert resulting state |
| `replaced return value with ""/null/0` | Weak assertion (`notNull`, `>`) | Use exact-value assertions |
| `Substituted 1 with 0` (inline const) | Magic number unpinned | Assert on the exact computed result |
| `mutated increment` | Loop iteration count unasserted | Assert collection sizes / totals exactly |

---

## 7. Step 6: Iterate — The Mutation Testing Workflow

The day-to-day loop, per class or package you're hardening:

```
1. Scope PIT to the class/package        (fast runs)
2. Run ./gradlew pitest
3. Open the report, pick the top survivors
4. Triage: test it / delete dead code / ignore equivalent
5. Write behavior-focused tests (not implementation-mirroring tests)
6. Re-run, confirm kills
7. Repeat until survivors are only equivalents/low-value
8. Widen the scope; record the score
```

Once a package has a healthy stable score, lock it in with a threshold so it can't regress:

```groovy
pitest {
    // after hardening:
    mutationThreshold = 75   // build fails if score drops below 75%
}
```

Golden rules while iterating:

- **Write tests for behavior, not for the mutant.** The mutant tells you *where* the gap
  is; the test you write should express a business rule ("updating the title regenerates
  the slug"), so it stays valuable after refactors.
- **Prefer deleting dead code over testing it.**
- **Time-box equivalent-mutant investigations** to a couple of minutes each.

---

## 8. Step 7: Add It to CI

### Option A — Nightly full report (adoption phase)

GitHub Actions job:

```yaml
name: Mutation Testing (nightly)
on:
  schedule:
    - cron: '0 3 * * *'
  workflow_dispatch: {}

jobs:
  pitest:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '11' }
      - run: ./gradlew pitest -x jacocoTestCoverageVerification
      - uses: actions/upload-artifact@v4
        with:
          name: pitest-report
          path: build/reports/pitest/
```

### Option B — PR-scoped incremental run (steady state)

Keep `enableDefaultIncrementalAnalysis = true` so only mutants affected by the diff are
re-tested, and run `./gradlew pitest` in the PR workflow with a `mutationThreshold` on the
hardened packages. Cache the history file between runs for the speedup to apply
(`build/pitHistory.txt` by default with the Gradle plugin's default incremental setup).

> Per `AGENTS.md`: don't touch the existing JaCoCo gate; PIT thresholds are additive and
> separate.

---

## 9. Tuning and Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `All tests did not pass without mutation` | Red/flaky baseline | Fix or exclude the failing tests; PIT requires a green baseline |
| `No mutations found` | `targetClasses` doesn't match any classes | Check package globs; remember it matches **class** names, not directories |
| Run takes forever | Scope too broad, slow tests included | Narrow `targetClasses`, exclude integration tests, raise `threads`, enable incremental analysis |
| Tons of survivors in getters/equals | Lombok-generated code being mutated | `lombok.addLombokGeneratedAnnotation = true` in `lombok.config` |
| Survivors in DGS generated classes | Generated code in target scope | Add `excludedClasses = ['io.spring.graphql.types.*', ...]` (wherever codegen outputs) |
| Many TIMED_OUT mutants | Loop mutations; timeout too tight | Usually fine (they count as killed); raise `timeoutConstInMillis` if legit tests are slow |
| `MINION_DIED` / weird JVM errors | Non-viable mutants, memory pressure | Ignore occasional ones; raise heap via `jvmArgs = ['-Xmx1024m']` if frequent |
| Tests using static state kill randomly | Shared mutable state across tests | Fix test isolation first — mutation results are only as reliable as the suite |
| JUnit 5 tests silently not run | Missing junit5 plugin | Set `junit5PluginVersion` in the `pitest` block |

Useful extra options:

```groovy
pitest {
    verbose = true                          // debug what PIT is doing
    excludedMethods = ['toString', 'hashCode']
    avoidCallsTo = ['org.slf4j', 'java.util.logging']  // don't count logging-call removal
    features = ['+EXPORT']                  // export mutated bytecode for inspection
    historyInputLocation = file('build/pitHistory.txt')
    historyOutputLocation = file('build/pitHistory.txt')
}
```

---

## 10. Doing It in Other Ecosystems (Maven, JS/TS, Python)

### Maven (Java)

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.15.8</version>
  <dependencies>
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.1</version>
    </dependency>
  </dependencies>
  <configuration>
    <targetClasses><param>io.spring.core.*</param></targetClasses>
    <mutationThreshold>70</mutationThreshold>
  </configuration>
</plugin>
```

```bash
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

### JavaScript / TypeScript — Stryker (applies to `frontend/` here)

```bash
cd frontend
npm install --save-dev @stryker-mutator/core @stryker-mutator/jest-runner
npx stryker init      # generates stryker.config.json
npx stryker run
```

Key config (`stryker.config.json`):

```json
{
  "testRunner": "jest",
  "mutate": ["components/**/*.tsx", "lib/**/*.ts", "!**/*.test.*"],
  "thresholds": { "high": 80, "low": 60, "break": 50 }
}
```

Report lands in `reports/mutation/mutation.html`.

### Python — mutmut

```bash
pip install mutmut
mutmut run --paths-to-mutate src/
mutmut results          # list survivors
mutmut show 42          # show the diff of survivor #42
mutmut html             # HTML report
```

---

## 11. Quick Reference Cheat Sheet

```bash
# One-time setup (this repo)
#   build.gradle: add plugin 'info.solidsoft.pitest' 1.9.11
#   pitest { junit5PluginVersion = '1.1.2'; targetClasses = ['io.spring.core.*', ...] }
#   lombok.config: lombok.addLombokGeneratedAnnotation = true

# Verify green baseline
./gradlew test -x jacocoTestCoverageVerification

# Run mutation testing
./gradlew pitest

# Open report
xdg-open build/reports/pitest/index.html

# Statuses:  KILLED ✅   TIMED_OUT ✅   SURVIVED ❌   NO_COVERAGE ❌❌
# Score   = killed / all mutants
# Strength = killed / covered mutants

# Triage survivors: test the behavior → or delete dead code → or ignore equivalents
# Kill patterns: exact assertions, boundary-value tests, both branches, verify(mock)
```

---

**See also:** [Mutation Testing — The Complete Guide](mutation-testing-guide.md) for
concepts, all mutation operators, metrics, real-life scenarios from this codebase,
equivalent mutants, tooling comparisons, and best practices.
