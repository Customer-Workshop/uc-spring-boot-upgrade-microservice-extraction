# Gatling load tests

Load tests for the articles REST API. They live in a standalone Gradle build so the Gatling
plugin and its Scala/Netty stack stay out of the application build.

## Prerequisites

Start the application first (it seeds the database on every `bootRun`):

```bash
./gradlew bootRun
```

## Run

From the repository root:

```bash
# Sustained SLO run: 30 req/sec for 5 minutes (the defaults)
./gradlew -p load-tests gatlingRun-io.spring.gatling.ArticlesLoadSimulation

# Quick smoke run
./gradlew -p load-tests gatlingRun-io.spring.gatling.ArticlesLoadSimulation -Prate=5 -PdurationSeconds=20
```

The HTML report (including p50/p75/p95/p99 per request and over time) is written to
`load-tests/build/reports/gatling/<simulation>-<timestamp>/index.html`.

## Parameters

| Property | Default | Meaning |
| --- | --- | --- |
| `baseUrl` | `http://localhost:8080` | API under test |
| `rate` | `30` | Requests per second (open workload, one request per virtual user) |
| `durationSeconds` | `300` | Duration of the sustained phase |
| `readWeight` | `80` | Share of reads (split evenly between list and read single) |
| `writeWeight` | `15` | Share of article creates |
| `deleteWeight` | `5` | Share of article deletes |
| `p95Ms` | `200` | SLO: p95 response time must be below this |
| `errorRatePercent` | `1` | SLO: failed request percentage must be below this |
| `userEmail` / `userPassword` | seed user `john@example.com` | Credentials used to obtain the JWT |

The SLO thresholds are Gatling assertions, so the Gradle build fails when they are breached —
the simulation can be wired into a pipeline as a performance gate as is.

## How the traffic is shaped

* One request per virtual user, injected with `constantUsersPerSec`, so the configured rate is the
  request rate regardless of response times (open model — a slow server queues, it does not
  self-throttle).
* Authentication, read fixtures and the JVM/connection warm up happen in the simulation
  constructor over a plain JDK HTTP client, so setup traffic is never measured.
* Deletes consume a queue of throwaway articles created during setup, sized from
  `rate × durationSeconds × deleteWeight` with head room, so seed articles are never destroyed and
  the run stays repeatable.

Findings from the sustained run are in [`../PERFORMANCE_REPORT.md`](../PERFORMANCE_REPORT.md).
