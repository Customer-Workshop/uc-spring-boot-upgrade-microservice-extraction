# Testing Gaps

Test scenarios that could not be verified in this environment.

## Integration Tests

| Scenario | Reason |
|----------|--------|
| Full Selenium E2E tests (browser-based) | Selenium tests require a running frontend + backend with Chrome WebDriver; excluded from unit test task (`io/spring/selenium/**` excluded) |
| GraphQL endpoint integration (live HTTP) | Unit tests mock the web layer; no full HTTP integration test with the DGS Spring GraphQL transport was executed |
| Database migration on real persistent storage | Tests use in-memory/file-based SQLite created fresh each run; no validation against a persistent production-like DB |
| Multi-user concurrent access | No concurrent request testing performed |

## E2E Tests

| Scenario | Reason |
|----------|--------|
| Frontend ↔ Backend full flow | Frontend app not started during build; only backend API tested |
| Browser-based authentication flow | Selenium tests excluded from standard build; require TestNG runner and live server |
| Cross-origin requests | CORS configuration not validated via actual browser requests |

## Performance Tests

| Scenario | Reason |
|----------|--------|
| Response time under load | No load testing tools (JMeter, Gatling) configured |
| Connection pool exhaustion (HikariCP) | No stress test simulating pool limits |
| JWT token generation throughput | No benchmark testing of crypto operations under Java 17 |
| Memory footprint comparison (Java 11 vs 17) | No profiling tools executed |

## Security Scans

| Scenario | Reason |
|----------|--------|
| Dependency vulnerability scan (OWASP/Snyk) | No CVE scanning tool in build pipeline |
| SAST (static analysis security testing) | No security-focused static analysis configured |
| JWT algorithm confusion attack resistance | Not tested with jjwt 0.12.x key handling changes |
| Spring Security filter chain ordering validation | Only validated by existing unit tests; no dedicated security penetration testing |

## Compatibility Tests

| Scenario | Reason |
|----------|--------|
| JDK 17 specific runtime behavior (sealed classes, records interop) | Code does not use Java 17 language features yet; runtime validated via existing tests only |
| Gradle 8.5 plugin compatibility edge cases | Only tested with current plugin set; no matrix testing with alternative plugin versions |
| Spring Boot Actuator endpoints | No actuator dependency present; not applicable |
| Production deployment (JAR startup, graceful shutdown) | Only `./gradlew build` tested; no `java -jar` startup validation performed |

## Notes

- All 68 existing unit tests pass with zero failures
- JaCoCo coverage gate (80%) verified via `jacocoTestReport` task
- Spotless formatting check passes for all source files
- The `seleniumTest` Gradle task exists for browser-based E2E testing but requires a running application instance and Chrome WebDriver
