# Articles API performance report

Load testing of the articles REST API with [Gatling](https://gatling.io/). The simulation and how
to run it are documented in [`load-tests/README.md`](load-tests/README.md).

## Service level objectives

| SLO | Threshold | Enforcement |
| --- | --- | --- |
| p95 response time (global and per endpoint) | < 200 ms | Gatling assertion, fails the build |
| Error rate | < 1 % of requests | Gatling assertion, fails the build |

Both are asserted by `ArticlesLoadSimulation`, so `gatlingRun` exits non-zero on a breach and the
simulation can be used as a performance gate in a pipeline unchanged.

## Test design

* **Workload**: open model, `constantUsersPerSec`, one request per virtual user, so the injected
  rate is the request rate and a slow server queues instead of self-throttling.
* **Traffic mix**: 80 % reads (evenly split between `GET /articles` list pages and
  `GET /articles/{slug}`), 15 % creates (`POST /articles`), 5 % deletes
  (`DELETE /articles/{slug}`).
* **Auth**: single JWT for the seed user `john@example.com`, obtained during setup so login traffic
  is not measured.
* **Fixtures**: deletes consume a queue of throwaway articles created during setup (sized from the
  rate, duration and delete share), so the seed data is never destroyed and runs are repeatable.
* **Warm up**: 100 setup requests before measurement, keeping JIT compilation and cold connection
  setup out of the reported percentiles.

## Sustained run: 30 req/sec for 5 minutes

Environment: Spring Boot 2.6.3 on JDK 17, SQLite `dev.db`, application and load generator on the
same 8 vCPU / 31 GB Linux host (loopback networking, so numbers exclude real network latency).

Result: **PASS** — 9,000 requests, 0 failures.

| Scope | Requests | Throughput | p50 | p95 | p99 | max |
| --- | --- | --- | --- | --- | --- | --- |
| Global | 9,000 | 30.0 req/s | 5 ms | 9 ms | 10 ms | 15 ms |
| `GET /articles` (list) | 3,597 (40.0 %) | 12.0 req/s | 7 ms | 10 ms | 11 ms | 15 ms |
| `GET /articles/{slug}` (read) | 3,562 (39.6 %) | 11.9 req/s | 2 ms | 2 ms | 3 ms | 7 ms |
| `POST /articles` (create) | 1,367 (15.2 %) | 4.6 req/s | 5 ms | 7 ms | 8 ms | 11 ms |
| `DELETE /articles/{slug}` (delete) | 474 (5.3 %) | 1.6 req/s | 5 ms | 6 ms | 7 ms | 8 ms |

The realised mix (79.6 % / 15.2 % / 5.3 %) matches the 80/15/5 target, and the p95 SLO is met with
a ~20x margin at the target rate.

## Capacity probe

To find the head room behind the SLO, the same scenario was replayed for 60 s at increasing rates:

| Rate | Achieved | p50 | p95 | p99 | Errors | Verdict |
| --- | --- | --- | --- | --- | --- | --- |
| 30 req/s | 30 req/s | 5 ms | 9 ms | 10 ms | 0 % | pass |
| 60 req/s | 60 req/s | 4 ms | 6 ms | 7 ms | 0 % | pass |
| 120 req/s | 120 req/s | 5 ms | 11 ms | 14 ms | 0 % | pass |
| 240 req/s | 236 req/s | 13 ms | 65 ms | 135 ms | 0.15 % | pass, knee of the curve |
| 300 req/s | 189 req/s | 17.0 s | 33.3 s | 41.2 s | 2.7 % | **saturated** |
| 400 req/s | 261 req/s | 10.1 s | 29.8 s | 36.2 s | 2.4 % | **saturated** |

Saturation is abrupt: throughput stops tracking the injected rate above ~240 req/s, latency jumps
by three orders of magnitude and connections start being refused (the accept queue overflows as
Tomcat's worker threads all block). So the service holds the SLO up to roughly **240 req/sec — 8x
the required 30 req/sec** on this host, and there is no gradual degradation band to rely on:
alerting should trigger well below the knee.

## Findings

1. **The SLO is met comfortably at the target rate**; nothing needed fixing to pass it, so no
   production code was changed as part of this work.
2. **Listing is 3–5x more expensive than reading a single article** (p95 10 ms vs 2 ms). Each
   `GET /articles` runs a paged id query plus a separate `countArticle` aggregate over
   `articles ⋈ article_tags ⋈ tags ⋈ article_favorites ⋈ users`, and then three more queries to
   fill favourite counts, favourite flags and follow flags (`ArticleQueryService.fillExtraInfo`).
   That is five round trips per list page.
3. **Latency growth under load is dominated by the write path.** Creates and deletes are the
   requests whose p95 degrades first, which is expected with SQLite: writers take a database level
   lock, so concurrent writes serialise no matter how many Tomcat threads are available.
4. **The dataset grows during a run** (15 % of requests create an article; ~1,350 new rows in the
   5 minute run) and list latency stayed flat, so pagination is not sensitive to table size at this
   scale — `queryArticles` is `ORDER BY created_at DESC LIMIT ?, ?` over a few thousand rows.
5. **Failures at saturation are connection refusals, not 5xx**: the application never returned an
   error response, it simply stopped accepting connections. Availability monitoring based on HTTP
   status codes alone would miss this mode.

## Recommended optimisations (not implemented)

Ordered by expected value per unit of effort. None are needed for the 30 req/sec SLO; they matter if
the target rate moves toward the ~240 req/sec knee.

1. **Cache or drop the list `count`.** `countArticle` repeats the full five-way join on every list
   request only to render `articlesCount`. Switching the unfiltered case to a cheap
   `SELECT count(*) FROM articles`, or moving the endpoint to the existing cursor pagination
   (`findRecentArticlesWithCursor`) which needs no total, removes the most expensive query on the
   hottest endpoint.
2. **Collapse `fillExtraInfo` into the main query.** Favourite counts, favourite flags and follow
   flags are three extra round trips per page and can be `LEFT JOIN`ed / aggregated in the id query,
   cutting list round trips from five to two.
3. **Add indexes for the hot access paths.** `V1__create_tables.sql` declares primary keys only, so
   `articles(slug)` (single reads), `articles(created_at)` (list ordering), `follows(user_id)` and
   `article_tags(article_id)` are scans today — `article_favorites` is the one lookup already
   covered, by its composite primary key. Cheap at a few thousand rows, not at production volume.
4. **Tune SQLite for concurrency** — WAL journal mode and a `busy_timeout` on the JDBC URL let
   readers proceed during writes instead of contending on the database lock; this is what limits the
   write path today.
5. **Bound the queue instead of letting it grow.** At saturation requests waited tens of seconds.
   A request timeout plus load shedding turns a 30 s wait into a fast 503, which is far better for
   callers. Note `mybatis.configuration.default-statement-timeout=3000` in
   `application.properties` is 3000 *seconds*, i.e. effectively no timeout — it is very likely meant
   to be 3.
6. **Move off SQLite for anything multi-instance.** A single-writer file database is the structural
   ceiling here; PostgreSQL would also make the extracted-service topology in `AGENTS.md` viable.

## Reproducing

```bash
./gradlew bootRun                                                        # terminal 1
./gradlew -p load-tests gatlingRun-io.spring.gatling.ArticlesLoadSimulation  # terminal 2
```

The HTML report with per-request percentiles and latency-over-time charts is written to
`load-tests/build/reports/gatling/<simulation>-<timestamp>/index.html`.
