# Mutation Testing — The Complete Guide

> A comprehensive reference covering everything you need to know about mutation testing:
> what it is, why it matters, how it works under the hood, all the core concepts,
> mutation operators, metrics, tooling across languages, real-life scenarios drawn from
> this very codebase, limitations, and best practices.
>
> For a step-by-step, hands-on walkthrough of *running* mutation testing on this
> repository, see the companion guide: [How to Do Mutation Testing](how-to-do-mutation-testing.md).

---

## Table of Contents

1. [What Is Mutation Testing?](#1-what-is-mutation-testing)
2. [Why Code Coverage Is Not Enough](#2-why-code-coverage-is-not-enough)
3. [How Mutation Testing Works](#3-how-mutation-testing-works)
4. [Core Terminology](#4-core-terminology)
5. [Mutation Operators (The Catalog of Mutators)](#5-mutation-operators-the-catalog-of-mutators)
6. [Metrics: Mutation Score and Test Strength](#6-metrics-mutation-score-and-test-strength)
7. [Worked Examples](#7-worked-examples)
8. [Real-Life Scenarios from This Codebase](#8-real-life-scenarios-from-this-codebase)
9. [Equivalent Mutants — The Fundamental Limitation](#9-equivalent-mutants--the-fundamental-limitation)
10. [Tooling Across Languages](#10-tooling-across-languages)
11. [Performance: Why Mutation Testing Is Slow and How Tools Cope](#11-performance-why-mutation-testing-is-slow-and-how-tools-cope)
12. [Mutation Testing in CI/CD](#12-mutation-testing-in-cicd)
13. [Best Practices](#13-best-practices)
14. [Common Pitfalls and Anti-Patterns](#14-common-pitfalls-and-anti-patterns)
15. [Mutation Testing vs. Other Techniques](#15-mutation-testing-vs-other-techniques)
16. [History and Theory](#16-history-and-theory)
17. [FAQ](#17-faq)
18. [Further Reading](#18-further-reading)

---

## 1. What Is Mutation Testing?

Mutation testing is a technique for **measuring the quality of your test suite** — not the
quality of your production code.

The idea is simple and slightly devious:

1. Take your working production code.
2. Deliberately introduce a small bug (a **mutation**), e.g. change `>` to `>=`, or delete
   a method call.
3. Run your test suite against the buggy version (the **mutant**).
4. If at least one test **fails**, your suite noticed the bug — the mutant is **killed**. Good.
5. If all tests still **pass**, your suite is blind to that bug — the mutant **survived**. Bad.

Repeat this for hundreds or thousands of small, systematic changes and you get a precise,
actionable map of where your tests are strong and where they are just *executing* code
without actually *verifying* it.

**In one sentence:** mutation testing is "testing your tests" by checking whether they can
detect artificial bugs.

### The key insight

A test suite has exactly one job: **fail when the code is wrong**. Code coverage tells you
your tests *ran* the code. Mutation testing tells you your tests would *catch a bug* in the
code. Those are very different guarantees.

---

## 2. Why Code Coverage Is Not Enough

This repository enforces an 80% JaCoCo line-coverage gate. That's useful, but coverage has
a well-known blind spot: **it measures execution, not assertion.**

Consider this test:

```java
@Test
void shouldUpdateArticle() {
  Article article = new Article("title", "desc", "body", List.of("java"), "user-1");
  article.update("new title", "new desc", "new body");
  // no assertions at all!
}
```

This test achieves **100% line coverage** of `Article.update(...)`. It executes every line.
It also verifies **nothing**. You could replace the entire body of `update` with `return;`
and this test would still pass.

Coverage answers: *"Did my tests run this line?"*
Mutation testing answers: *"If this line were wrong, would any test fail?"*

### The assertion-free test problem, quantified

| Test suite property | Line coverage detects it? | Mutation testing detects it? |
|---|---|---|
| Code never executed by tests | ✅ Yes | ✅ Yes (mutants there are "no coverage") |
| Code executed but never asserted on | ❌ No | ✅ Yes (mutants survive) |
| Weak assertions (e.g. only checking not-null) | ❌ No | ✅ Yes |
| Boundary conditions untested (`>` vs `>=`) | ❌ No | ✅ Yes |
| Missing negative/error-path tests | ⚠️ Partially | ✅ Yes |

A codebase can sit at 90%+ line coverage with a test suite that would miss most real bugs.
Mutation testing is the antidote.

---

## 3. How Mutation Testing Works

### The pipeline

```
 ┌────────────┐    ┌───────────────┐    ┌──────────────┐    ┌───────────────┐
 │  Original  │───▶│   Generate    │───▶│  Run tests   │───▶│   Classify    │
 │    code    │    │   mutants     │    │ against each │    │   & report    │
 └────────────┘    └───────────────┘    │    mutant    │    └───────────────┘
                    one small change     └──────────────┘     killed/survived/
                    per mutant            (per mutant)        timed out/...
```

1. **Baseline run** — the tool first runs your full test suite against the *unmodified*
   code. All tests must pass (a "green" baseline); otherwise results would be meaningless.
2. **Mutant generation** — the tool applies **mutation operators** (see §5) to your code.
   Each mutant contains exactly **one** small change. Modern tools (PIT, Stryker) mutate
   the compiled bytecode or AST in memory rather than editing source files on disk.
3. **Test selection** — for each mutant, the tool runs only the tests that actually cover
   the mutated line (determined via per-test coverage data). This is the single biggest
   performance optimization.
4. **Execution & classification** — each mutant gets one of the statuses in §4.
5. **Reporting** — an HTML/XML report shows every mutant, its location, its operator, and
   whether it was killed, with an aggregate **mutation score**.

### First-order vs. higher-order mutants

- **First-order mutant (FOM):** exactly one change. This is what all mainstream tools do.
- **Higher-order mutant (HOM):** two or more changes combined. Used in research to create
  "subtler" bugs and reduce equivalent mutants, but rarely used in practice because the
  number of combinations explodes.

### The two hypotheses that make it valid

Mutation testing rests on two empirically supported assumptions:

- **Competent Programmer Hypothesis:** programmers write code that is *close to correct*.
  Real bugs are usually small deviations (an off-by-one, a flipped condition, a missing
  call) — exactly the kind of change mutation operators make.
- **Coupling Effect:** tests that catch all simple (single) faults will also catch most
  complex (compound) faults, because complex faults are "coupled" to simple ones. This is
  why first-order mutants are enough in practice.

---

## 4. Core Terminology

| Term | Meaning |
|---|---|
| **Mutant** | A copy of the program with exactly one small change applied. |
| **Mutation operator / mutator** | A rule that produces mutants, e.g. "replace `+` with `-`". |
| **Killed** | At least one test failed when run against the mutant. ✅ The desired outcome. |
| **Survived** | All covering tests passed against the mutant. ❌ Your tests missed the bug. |
| **No coverage** | No test even executes the mutated line. Worse than survived — the code is completely untested. |
| **Timed out** | The mutant caused an infinite loop (e.g. mutating a loop condition); the test run was killed after a timeout. Counted as killed — the behavior change *was* detected, just via non-termination. |
| **Non-viable / run error** | The mutant produced invalid bytecode or crashed the JVM. Excluded from scoring. |
| **Memory error** | The mutant caused the JVM to exhaust memory. Usually counted as killed. |
| **Equivalent mutant** | A mutant that is syntactically different but **behaviorally identical** to the original. It can never be killed. See §9. |
| **Stubborn mutant** | A non-equivalent mutant that is very hard to kill (needs a very specific input). |
| **Mutation score** | `killed / (total − equivalent)` in theory; in practice `killed / total detected mutants`. See §6. |
| **Test strength** | `killed / covered mutants` — score ignoring uncovered code. See §6. |
| **Baseline / clean run** | Initial run of the suite on unmutated code; must be green. |

---

## 5. Mutation Operators (The Catalog of Mutators)

Mutation operators are the heart of the technique. Below is a catalog of the most common
ones, with Java examples. (Names follow PIT's conventions; other tools use near-identical
sets.)

### 5.1 Conditionals Boundary (`CONDITIONALS_BOUNDARY`)

Replaces relational operators with their boundary counterpart.

| Original | Mutated |
|---|---|
| `<` | `<=` |
| `<=` | `<` |
| `>` | `>=` |
| `>=` | `>` |

```java
// Original
if (age >= 18) { allowVoting(); }
// Mutant
if (age > 18)  { allowVoting(); }   // 18-year-olds can no longer vote
```

**Kills require:** a test with the exact boundary value (`age == 18`). This operator is
*the* off-by-one-bug detector.

### 5.2 Negate Conditionals (`NEGATE_CONDITIONALS`)

Inverts a conditional operator.

| Original | Mutated |
|---|---|
| `==` | `!=` |
| `<` | `>=` |
| `>` | `<=` |

```java
// Original
if (user.isAdmin()) { showAdminPanel(); }
// Mutant
if (!user.isAdmin()) { showAdminPanel(); }   // admins locked out, everyone else in
```

**Kills require:** tests for both the true and false branches.

### 5.3 Math (`MATH`)

Replaces binary arithmetic operators.

| Original | Mutated |
|---|---|
| `+` | `-` |
| `-` | `+` |
| `*` | `/` |
| `/` | `*` |
| `%` | `*` |

```java
// Original
int total = price * quantity;
// Mutant
int total = price / quantity;
```

**Kills require:** asserting on the actual computed value (not just "no exception").

### 5.4 Increments (`INCREMENTS`)

Swaps `++` and `--` (and `+=`/`-=`).

```java
// Original
for (int i = 0; i < n; i++) { ... }
// Mutant
for (int i = 0; i < n; i--) { ... }   // often becomes an infinite loop → TIMED_OUT
```

### 5.5 Invert Negatives (`INVERT_NEGS`)

Removes unary minus.

```java
return -balance;   →   return balance;
```

### 5.6 Return Values

A family of operators that corrupt return values:

| Operator | Effect |
|---|---|
| `TRUE_RETURNS` | Any boolean return becomes `true` |
| `FALSE_RETURNS` | Any boolean return becomes `false` |
| `PRIMITIVE_RETURNS` | Numeric returns become `0` |
| `EMPTY_RETURNS` | Returns "empty" value: `""`, `Optional.empty()`, `List.of()`, `0` |
| `NULL_RETURNS` | Returns `null` |

```java
// Original
public boolean isEmpty(String value) { return value == null || value.equals(""); }
// TRUE_RETURNS mutant
public boolean isEmpty(String value) { return true; }   // everything is "empty" now
```

**Kills require:** asserting on returned values for both outcomes.

### 5.7 Void Method Calls (`VOID_METHOD_CALLS`)

Removes a call to a void method entirely.

```java
// Original
articleRepository.save(article);
emailService.notifyFollowers(article);
// Mutant (call removed)
articleRepository.save(article);
// emailService.notifyFollowers(article);   ← gone
```

**Kills require:** verifying side effects — e.g. `verify(emailService).notifyFollowers(...)`
in Mockito, or asserting on the resulting state.

### 5.8 Non-Void Method Calls / Remove Calls

Removes a non-void call and substitutes a default value for its result. More aggressive;
often disabled by default.

### 5.9 Constructor Calls (`CONSTRUCTOR_CALLS`)

Replaces `new X(...)` with `null`.

### 5.10 Experimental / extended operators (PIT)

- `EXPERIMENTAL_SWITCH` — mutates `switch` statements (swaps case labels with default).
- `REMOVE_CONDITIONALS` — forces a condition to always-true or always-false.
- `INLINE_CONSTS` — changes inline constants (`1` → `0`, `"a"` → `""`).
- `EXPERIMENTAL_MEMBER_VARIABLE` — removes field assignments.
- `EXPERIMENTAL_ARGUMENT_PROPAGATION` — replaces a call with one of its arguments.

### Operator groups in PIT

PIT bundles operators into named groups you can activate:

| Group | Contents | Use case |
|---|---|---|
| `DEFAULTS` | The balanced core set (boundary, negate, math, increments, returns, void calls...) | Day-to-day use |
| `STRONGER` | Defaults + a few more aggressive ones | Higher assurance |
| `ALL` | Everything including experimental | Research / maximum paranoia |

**Rule of thumb:** start with `DEFAULTS`. More operators = more mutants = slower runs and
more noise, with diminishing returns.

---

## 6. Metrics: Mutation Score and Test Strength

### Mutation score (mutation coverage)

```
Mutation Score = Killed Mutants / Total Mutants × 100%
```

where "total" includes NO_COVERAGE mutants. This is the headline number.

### Test strength

```
Test Strength = Killed Mutants / Covered Mutants × 100%
```

Ignores mutants in uncovered code. This isolates the question *"of the code my tests do
run, how well do they assert on it?"*

### Worked example

Suppose PIT reports for a class:

```
Generated 40 mutations
  Killed:      28
  Survived:     6
  No coverage:  4
  Timed out:    2
```

- Killed (incl. timeouts): 28 + 2 = 30
- **Mutation score:** 30 / 40 = **75%**
- Covered mutants: 40 − 4 = 36
- **Test strength:** 30 / 36 ≈ **83%**

A big gap between line coverage and mutation score = lots of assertion-free or
weakly-asserted tests. A big gap between mutation score and test strength = lots of
entirely untested code.

### What's a "good" score?

There is no universal number, but common guidance:

| Score | Interpretation |
|---|---|
| < 50% | Tests mostly execute code without verifying it |
| 60–75% | Typical for a decent suite that never optimized for mutation score |
| 75–90% | Strong suite; boundaries and side effects are checked |
| > 90% | Excellent; usually only achieved on critical, deliberately hardened modules |
| 100% | Rarely worth chasing globally — equivalent mutants make it partly unattainable |

Aim for **high scores on critical business logic** (money, auth, domain invariants) and
accept lower scores on glue code, DTOs, and configuration.

---

## 7. Worked Examples

### Example 1: The boundary bug (banking)

Production code — a bank overdraft check:

```java
public class AccountService {
  public boolean canWithdraw(Account account, long amountCents) {
    return account.getBalanceCents() - amountCents >= 0;
  }
}
```

A "good coverage" test suite:

```java
@Test
void allowsWithdrawalWhenFundsAvailable() {
  Account acc = accountWithBalance(10_000);
  assertTrue(service.canWithdraw(acc, 5_000));
}

@Test
void rejectsWithdrawalWhenInsufficientFunds() {
  Account acc = accountWithBalance(10_000);
  assertFalse(service.canWithdraw(acc, 20_000));
}
```

100% line and branch coverage. Now the `CONDITIONALS_BOUNDARY` mutant:

```java
return account.getBalanceCents() - amountCents > 0;   // >= became >
```

Both tests still pass → **mutant survives**. The suite never tests withdrawing the *exact
balance* — a real business rule ("you may empty your account") is unverified. The fix:

```java
@Test
void allowsWithdrawingExactBalance() {
  Account acc = accountWithBalance(10_000);
  assertTrue(service.canWithdraw(acc, 10_000));   // kills the boundary mutant
}
```

This is the classic mutation-testing win: it forces you to write the boundary test that
catches real off-by-one bugs.

### Example 2: The unverified side effect (e-commerce)

```java
public class OrderService {
  public Order placeOrder(Cart cart, Customer customer) {
    Order order = new Order(cart, customer);
    orderRepository.save(order);
    inventoryService.reserve(cart.getItems());
    emailService.sendConfirmation(customer, order);
    return order;
  }
}
```

Test:

```java
@Test
void placesOrder() {
  Order order = orderService.placeOrder(cart, customer);
  assertNotNull(order);
  assertEquals(customer, order.getCustomer());
}
```

`VOID_METHOD_CALLS` mutants:

- remove `orderRepository.save(order)` → test passes → **survives**
- remove `inventoryService.reserve(...)` → test passes → **survives** (overselling bug!)
- remove `emailService.sendConfirmation(...)` → test passes → **survives**

Three critical behaviors, zero verification. The fix:

```java
@Test
void placesOrder_persistsReservesAndNotifies() {
  Order order = orderService.placeOrder(cart, customer);
  verify(orderRepository).save(order);
  verify(inventoryService).reserve(cart.getItems());
  verify(emailService).sendConfirmation(customer, order);
}
```

### Example 3: The weak assertion (return values)

```java
public int applyDiscount(int priceCents, int percent) {
  return priceCents - (priceCents * percent / 100);
}
```

Test:

```java
@Test
void discountReducesPrice() {
  assertTrue(applyDiscount(10_000, 20) < 10_000);   // weak!
}
```

`MATH` mutant `priceCents * percent / 100` → `priceCents * percent * 100`... actually makes
the result negative, still `< 10_000` → **survives**. `PRIMITIVE_RETURNS` (return `0`) also
survives. The fix is an exact assertion:

```java
assertEquals(8_000, applyDiscount(10_000, 20));
```

**Lesson:** prefer exact-value assertions over "directional" ones (`<`, `!= null`,
`isNotEmpty`) wherever the exact result is knowable.

### Example 4: The timed-out mutant

```java
public int sum(int[] xs) {
  int total = 0;
  for (int i = 0; i < xs.length; i++) total += xs[i];
  return total;
}
```

`INCREMENTS` mutant `i++` → `i--` never terminates. The tool kills the run after its
timeout and records **TIMED_OUT**, which counts as killed — the mutation *was* detected,
via divergence rather than a failing assertion.

---

## 8. Real-Life Scenarios from This Codebase

These scenarios use actual code from this repository
(`src/main/java/io/spring/...`) to show what mutation testing finds in a real Spring Boot
application.

### Scenario A: `Article.update(...)` — conditional updates and timestamps

`io.spring.core.article.Article`:

```java
public void update(String title, String description, String body) {
  if (!Util.isEmpty(title)) {
    this.title = title;
    this.slug = toSlug(title);
    this.updatedAt = new DateTime();
  }
  if (!Util.isEmpty(description)) {
    this.description = description;
    this.updatedAt = new DateTime();
  }
  if (!Util.isEmpty(body)) {
    this.body = body;
    this.updatedAt = new DateTime();
  }
}
```

Interesting mutants and the tests they demand:

| Mutant | Surviving means... | Killing test |
|---|---|---|
| `NEGATE_CONDITIONALS` on `!Util.isEmpty(title)` | No test updates the title *and* checks it changed, or passes empty title and checks it did *not* change | Update with a real title, assert `getTitle()`; update with `""`, assert title unchanged |
| `VOID_METHOD_CALLS` removing `this.slug = toSlug(title)` assignment (via member-variable mutator) | No test asserts the slug is regenerated on title change | After `update("New Title", ...)`, assert `getSlug().equals("new-title")` |
| `EMPTY_RETURNS`/member mutants on `updatedAt = new DateTime()` | No test asserts the timestamp moves | Assert `updatedAt` is after the original `createdAt` |

Note the *partial update* semantics (empty string = "don't change") is a genuine business
rule here — mutation testing forces you to pin it down with tests.

### Scenario B: `Article.toSlug(...)` — string transformation

```java
public static String toSlug(String title) {
  return title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\’|\\”|\\s\\?\\,\\.]+", "-");
}
```

Mutants: remove the `toLowerCase()` call, replace the regex/replacement constants
(`INLINE_CONSTS`), return the argument directly (`ARGUMENT_PROPAGATION`).

A test like `assertNotNull(Article.toSlug("Hello World"))` kills almost nothing. A test
like `assertEquals("hello-world", Article.toSlug("Hello World?"))` kills all of them.
Since slugs are URL identifiers across the whole app (REST routes, GraphQL queries), a
surviving mutant here maps directly to broken links in production.

### Scenario C: `Util.isEmpty(...)` — the tiny utility everyone depends on

```java
public static boolean isEmpty(String value) {
  return value == null || value.equals("");
}
```

Mutants: `TRUE_RETURNS`, `FALSE_RETURNS`, `NEGATE_CONDITIONALS` on `==`/`equals`. Killing
all of them requires exactly three tests: `isEmpty(null)` → true, `isEmpty("")` → true,
`isEmpty("x")` → false. Utilities like this are used by dozens of call sites (all the
`update` methods above), so a surviving `FALSE_RETURNS` mutant here means the partial-update
logic of *every entity* is unverified.

### Scenario D: Read services and pagination (`application/` layer, CQRS reads)

Cursor pagination code is a boundary-condition minefield: `<` vs `<=` on cursors decides
whether the last-seen item is duplicated or skipped across pages. `CONDITIONALS_BOUNDARY`
mutants in `CursorPager`/query services survive unless a test paginates across a boundary
and asserts the exact item sets of consecutive pages — precisely the bug class users report
as "I saw the same article twice on page 2."

### Scenario E: JWT auth (`infrastructure/service`)

Token validation logic (`expiration < now`, signature check calls) is where
`NEGATE_CONDITIONALS` and `VOID_METHOD_CALLS` survivors are *security bugs*: a surviving
"negate the expiry check" mutant means no test verifies that expired tokens are rejected.
Security-critical modules are the highest-ROI target for mutation testing.

---

## 9. Equivalent Mutants — The Fundamental Limitation

An **equivalent mutant** is syntactically different but semantically identical to the
original program. No test can ever kill it, so it drags the score down through no fault of
your tests.

### Classic examples

```java
// Original
for (int i = 0; i < 10; i++) { ... }
// Mutant: i != 10 — behaves identically since i only ever increments by 1
for (int i = 0; i != 10; i++) { ... }
```

```java
// Original — index is never used after the loop
int index = findIndex(xs);
doSomething();
// Mutant that changes 'index' is equivalent if the value is dead
```

```java
// Original
return a * 1;    // Mutant: return a / 1;  — identical result
```

### Why it matters

- Detecting equivalence is **undecidable** in general (it reduces to program equivalence).
- Empirically, roughly 5–20% of surviving mutants are equivalent, varying by codebase.
- Consequence: **don't chase 100%**. Treat surviving mutants as *leads to investigate*,
  not defects to eliminate at all costs.

### How tools mitigate

- Careful operator design (PIT's default set is chosen to minimize equivalents).
- Bytecode-level analysis eliminates some trivially equivalent mutants.
- You can annotate/exclude: PIT supports `@Generated`/`@DoNotMutate`-style exclusions and
  per-class/per-method filters for code where equivalents cluster (logging, toString).

---

## 10. Tooling Across Languages

| Language / stack | Tool | Notes |
|---|---|---|
| **Java / JVM** (this repo) | **[PIT / pitest](https://pitest.org)** | The industry standard. Bytecode mutation, per-test coverage targeting, Gradle/Maven plugins, JUnit/TestNG support, incremental analysis. |
| Java (research) | muJava, Major, Javalanche | Mostly academic. |
| JavaScript / TypeScript | **[Stryker](https://stryker-mutator.io)** (StrykerJS) | AST mutation; supports Jest, Vitest, Mocha, Karma. Would apply to `frontend/` (Next.js) here. |
| C# / .NET | Stryker.NET | Same family as StrykerJS. |
| Scala | Stryker4s | |
| Python | mutmut, cosmic-ray, mutpy | mutmut is the most maintained. |
| Ruby | mutant | Very mature, used heavily in the Ruby community. |
| Go | go-mutesting, gremlins | |
| PHP | Infection | Popular and well maintained. |
| Rust | cargo-mutants | |
| Kotlin | PIT works (JVM bytecode) | |
| C/C++ | Mull (LLVM-based), Dextool | |

### PIT specifics (most relevant for this repo)

- Mutates **JVM bytecode in memory** — no source files touched, no recompilation per mutant.
- Uses **per-test line coverage** to run only relevant tests per mutant.
- **Incremental analysis** (`withHistory`/`historyInputLocation`): caches results and only
  re-examines mutants affected by code/test changes — makes CI use practical.
- Output formats: HTML (human), XML (machine/CI), CSV.
- Thresholds: `mutationThreshold`, `coverageThreshold`, `testStrengthThreshold` fail the
  build below a floor — the mutation-testing analog of this repo's JaCoCo gate.

---

## 11. Performance: Why Mutation Testing Is Slow and How Tools Cope

Naively: `cost ≈ (number of mutants) × (test suite runtime)`. A 10-second suite and 2,000
mutants = 5.5 hours. Tools attack every factor:

| Optimization | How it works | Supported by |
|---|---|---|
| **Coverage-based test targeting** | Only run tests that execute the mutated line | PIT, Stryker |
| **Test prioritization** | Run the fastest/most-likely-to-kill tests first; stop at first kill | PIT |
| **Bytecode/in-memory mutation** | Skip recompilation entirely | PIT |
| **Mutant schemata** | Compile all mutants into one binary, switch via flags | Research, partially Stryker |
| **Parallel execution** | Multiple JVMs/workers (`threads` option) | PIT, Stryker |
| **Incremental analysis** | Only re-test mutants affected by the diff | PIT, Stryker |
| **Scope restriction** | `targetClasses` / changed-files-only runs | All |
| **Mutant sampling** | Test a random subset of mutants | Research/config |

### Practical speed checklist

1. Keep unit tests fast and isolated (no Spring context where avoidable — plain JUnit on
   `core/` classes is ideal mutation-testing fodder).
2. Exclude slow integration/E2E tests from the mutation run (in this repo: exclude the
   Selenium TestNG suite; PIT should run against the JUnit unit tests only).
3. Restrict `targetClasses` to business logic (`io.spring.core.*`, `io.spring.application.*`),
   excluding DTOs, config, and generated DGS classes.
4. Use `threads = <cores>` and incremental analysis in CI.

---

## 12. Mutation Testing in CI/CD

### Strategy options

| Strategy | Description | Best for |
|---|---|---|
| **Full run, nightly** | Whole-project mutation testing on a schedule | Trend tracking, small/medium repos |
| **Incremental on PR** | Only mutate changed classes/lines in the PR | The sweet spot for most teams |
| **Gated threshold** | Fail build if score < X% | Critical modules |
| **Report-only** | Publish the report, never fail | Adoption phase |

### Recommended adoption path

1. **Week 1 — Observe:** run PIT locally on `core/`, look at survivors, fix the worst.
2. **Week 2–4 — Report in CI:** nightly job publishes HTML report as a build artifact.
3. **Month 2 — Gate new code:** PR job with incremental analysis; threshold on changed code.
4. **Later — Gate critical packages:** e.g. `mutationThreshold = 85` for `io.spring.core.*`.

### Anti-pattern warning

Do **not** set a global hard threshold on day one. Teams respond to unachievable gates by
excluding packages or writing mutant-killing-only tests, which destroys the signal.

---

## 13. Best Practices

1. **Target business logic first.** Domain entities, services, validators, calculators —
   not DTOs, config, or generated code.
2. **Use exact assertions.** `assertEquals(expected, actual)` kills mutants;
   `assertNotNull(actual)` does not.
3. **Verify side effects.** Every important void call needs a `verify(...)` or a state
   assertion, or `VOID_METHOD_CALLS` mutants will survive.
4. **Test boundaries explicitly.** For every `<`/`<=`/`>`/`>=`, have a test at the exact
   boundary value.
5. **Treat survivors as leads, not bugs.** Ask: (a) is this behavior worth testing? →
   write the test; (b) is the code dead/unneeded? → delete it; (c) is the mutant
   equivalent? → exclude/ignore and move on.
6. **Run it incrementally and often** rather than in giant infrequent batches.
7. **Keep the suite green and fast** — mutation testing multiplies your suite's runtime.
8. **Don't chase 100%.** Diminishing returns plus equivalent mutants make it a bad target.
9. **Pair with code review.** A surviving mutant on a PR diff is a superb, concrete review
   comment: "if this line were wrong, no test would fail."
10. **Track score trends, not absolutes.** A dropping score on a module is the actionable
    signal.

---

## 14. Common Pitfalls and Anti-Patterns

| Pitfall | Consequence | Remedy |
|---|---|---|
| Running against a red baseline | Meaningless results | Fix/skip failing tests first (PIT refuses to run otherwise) |
| Mutating generated code (DGS codegen, Lombok internals) | Noise, wasted time | Exclude via `targetClasses`/`excludedClasses`; PIT skips Lombok-`@Generated` code |
| Including E2E/Selenium tests in the mutation run | Hours-long runs, flaky kills | Restrict to fast unit tests |
| Writing tests that only kill mutants ("assert the implementation") | Brittle, refactor-hostile tests | Always assert *behavior/contract*, use the mutant only as a pointer |
| Global 100% score mandate | Gaming, exclusions, resentment | Module-appropriate thresholds |
| Ignoring NO_COVERAGE mutants because "score is fine" | Untested code hides | Watch both score *and* test strength |
| Mutation testing flaky tests | Random kills/survivals | Fix flakiness first |
| One-shot adoption on a huge legacy codebase | Overwhelming report, abandonment | Start with one critical package |

---

## 15. Mutation Testing vs. Other Techniques

| Technique | Question it answers | Relationship to mutation testing |
|---|---|---|
| **Line/branch coverage** | Did tests execute this code? | Necessary but insufficient; mutation testing subsumes it (NO_COVERAGE mutants) |
| **Property-based testing** | Does the code satisfy invariants across many inputs? | Complementary — property tests are excellent mutant killers |
| **Fuzzing** | Does the code crash/misbehave on random inputs? | Different goal (robustness of code, not of tests) |
| **Fault injection / chaos engineering** | Does the *system* survive infrastructure failures at runtime? | Runtime cousin; mutation testing is build-time and targets test quality |
| **Static analysis / linters** | Does the code contain known-bad patterns? | Orthogonal; finds bugs directly rather than measuring tests |
| **Formal verification** | Is the code provably correct against a spec? | Far stronger and far more expensive; mutation testing is the pragmatic middle |
| **TDD** | Are tests driving the design? | Mutation testing audits the result: TDD suites usually score high |

---

## 16. History and Theory

- **1971** — Richard Lipton proposes the idea in a class paper, "Fault Diagnosis of
  Computer Programs".
- **1978** — DeMillo, Lipton, and Sayward publish *"Hints on Test Data Selection: Help for
  the Practicing Programmer"*, articulating the Competent Programmer Hypothesis and the
  Coupling Effect.
- **1980s–90s** — Mothra (Fortran) and other academic systems; the technique is considered
  computationally impractical for industry.
- **2000s** — CPU advances + per-test coverage + bytecode mutation make it feasible; muJava
  and Javalanche appear.
- **2010s** — **PIT** (Henry Coles, ~2011) makes it genuinely practical on the JVM;
  Stryker brings the same to JS/C#/Scala.
- **Today** — used at scale in industry: Google published work on productionizing mutation
  testing across their codebase (surfacing surviving mutants directly in code review, with
  heavy suppression of unproductive mutants), and Meta has reported similar systems.

---

## 17. FAQ

**Q: Does mutation testing find bugs in my code?**
Not directly — the baseline requires your tests to pass. It finds *gaps in your tests*,
which is where tomorrow's bugs will slip through. (Occasionally investigating a survivor
does reveal a real bug or dead code.)

**Q: How is a timed-out mutant "killed"?**
The behavior diverged so much the program didn't terminate. Divergence detected = killed.

**Q: Should I write a test for every surviving mutant?**
No. Triage: worth testing → test it; dead code → delete it; equivalent → ignore/exclude.

**Q: How long will it take on this repo?**
With scope restricted to `io.spring.core.*` + `io.spring.application.*` and only unit
tests, typically minutes. Unscoped with all tests, much longer — see §11.

**Q: Does it work with Mockito / Spring / Lombok?**
Yes. Mockito `verify` is a primary mutant-killer. PIT skips Lombok-generated code when
`lombok.addLombokGeneratedAnnotation = true` is set in `lombok.config`. Prefer mutating
logic that doesn't need the Spring context.

**Q: Mutation score vs. code coverage — which gate should we keep?**
Both measure different things. Coverage gates catch entirely untested code cheaply;
mutation gates catch assertion-free tests. If forced to pick one for critical logic, pick
mutation score.

---

## 18. Further Reading

- PIT documentation: https://pitest.org — quick start, mutator reference, Gradle plugin
- Stryker handbook: https://stryker-mutator.io/docs/
- DeMillo, Lipton, Sayward (1978), *Hints on Test Data Selection*
- Petrović & Ivanković (Google, 2018), *State of Mutation Testing at Google*
- Coles et al. (2016), *PIT: A Practical Mutation Testing Tool for Java*
- Papadakis et al. (2019), *Mutation Testing Advances: An Analysis and Survey*

---

**Next step:** the hands-on companion guide — [How to Do Mutation Testing](how-to-do-mutation-testing.md) —
walks through setting up PIT in this repository, running it, reading the report, and
killing your first surviving mutants.
