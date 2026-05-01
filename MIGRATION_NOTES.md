# Migration Notes: Java 11 + Spring Boot 2.6.3 → Java 17 + Spring Boot 3.2.5

## Summary

This document records all breaking changes encountered during the framework upgrade
and how each was resolved.

---

## 1. Build Toolchain

| Component | Before | After |
|-----------|--------|-------|
| Java | 11 | 17 |
| Gradle wrapper | 7.4 | 8.7 |
| Spring Boot | 2.6.3 | 3.2.5 |
| Spring Dependency Management plugin | 1.0.11.RELEASE | 1.1.5 |

**Breaking change:** Gradle 7.4 does not support Spring Boot 3.x plugin.
Upgraded the wrapper to 8.7 via `gradle-wrapper.properties`.

---

## 2. Jakarta EE Namespace Migration (`javax.*` → `jakarta.*`)

Spring Boot 3 requires Jakarta EE 9+. All `javax.validation.*` and `javax.servlet.*`
imports were replaced with their `jakarta.*` equivalents across 21 source files.

`javax.crypto.*` imports (JDK standard library) were left unchanged — they are
**not** part of the Jakarta EE migration.

**Files affected:** All files under `api/`, `application/`, `graphql/`, and
`infrastructure/service/` that referenced validation constraints, servlet filters,
or the Servlet API.

---

## 3. Spring Security — Removal of `WebSecurityConfigurerAdapter`

`WebSecurityConfigurerAdapter` was removed in Spring Security 6 (Spring Boot 3).

**Before:**
```java
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception { ... }
}
```

**After:**
```java
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(...)
                .permitAll()
                ...
            );
        return http.build();
    }
}
```

Key API changes:
- `.csrf().disable()` → `.csrf(AbstractHttpConfigurer::disable)`
- `.cors().and()` → `.cors(cors -> cors.configurationSource(...))`
- `.authorizeRequests()` → `.authorizeHttpRequests()`
- `.antMatchers()` → `.requestMatchers()`
- Method chaining with `.and()` replaced by lambda-based DSL

---

## 4. Spring MVC — `ResponseEntityExceptionHandler` Signature Change

`handleMethodArgumentNotValid` parameter type changed from `HttpStatus` to
`HttpStatusCode` in Spring Framework 6.

**Before:** `HttpStatus status`
**After:** `HttpStatusCode status`

---

## 5. Netflix DGS Framework Upgrade

| Component | Before | After |
|-----------|--------|-------|
| DGS starter | `graphql-dgs-spring-boot-starter:4.9.21` | `graphql-dgs-spring-graphql-starter` (BOM 8.7.1) |
| DGS codegen plugin | 5.0.6 | 6.2.1 |

**Breaking changes:**

### 5a. Starter artifact renamed
DGS 8.x integrates with Spring GraphQL. The artifact changed from
`graphql-dgs-spring-boot-starter` to `graphql-dgs-spring-graphql-starter`,
managed via the `graphql-dgs-platform-dependencies` BOM.

### 5b. Schema file location
Spring GraphQL loads schemas from `classpath:graphql/` by default.
The schema file was moved from `src/main/resources/schema/` to
`src/main/resources/graphql/`.

### 5c. Schema inspection disabled
Spring GraphQL's `ConnectionTypeDefinitionConfigurer` auto-detects types ending
in "Connection" and expects matching edge type names (e.g., `ArticlesConnection`
→ `ArticlesEdge`). Our schema uses `ArticleEdge` (singular), causing a
"No node type for 'ArticlesConnection'" error.

**Resolution:** Set `spring.graphql.schema.inspection.enabled=false`.

### 5d. `DataFetcherExceptionHandler` interface change
The `onException` method was replaced by `handleException` returning
`CompletableFuture<DataFetcherExceptionHandlerResult>`.

### 5e. `graphql.relay.PageInfo` → generated `PageInfo` type
DGS-generated builder types (`ArticlesConnection`, `CommentsConnection`) now
expect the DGS-generated `io.spring.graphql.types.PageInfo` instead of
`graphql.relay.DefaultPageInfo`. Both `ArticleDatafetcher` and
`CommentDatafetcher` were updated to use `PageInfo.newBuilder()`.

---

## 6. JJWT Library Upgrade

| Component | Before | After |
|-----------|--------|-------|
| jjwt-api / impl / jackson | 0.11.2 | 0.12.5 |

**Breaking changes:**

- `SignatureAlgorithm` enum removed; key algorithm is inferred from the key type
- `Jwts.builder().setSubject()` → `.subject()`
- `Jwts.builder().setExpiration()` → `.expiration()`
- `Jwts.parserBuilder().setSigningKey().build().parseClaimsJws()` →
  `Jwts.parser().verifyWith().build().parseSignedClaims()`
- `claimsJws.getBody()` → `claimsJws.getPayload()`
- **Minimum key length enforced:** HS512 now requires keys ≥ 64 bytes.
  Test fixture secret was extended to meet this requirement.

---

## 7. MyBatis Spring Boot Starter

| Component | Before | After |
|-----------|--------|-------|
| mybatis-spring-boot-starter | 2.2.2 | 3.0.3 |

MyBatis 3.0.x is the Jakarta EE-compatible release. No code changes were needed
beyond the version bump — mapper XML and configuration properties are unchanged.

---

## 8. Other Dependency Updates

| Dependency | Before | After | Reason |
|------------|--------|-------|--------|
| SQLite JDBC | 3.36.0.3 | 3.45.3.0 | Compatibility with Java 17 |
| Joda-Time | 2.10.13 | 2.12.7 | Latest stable |
| rest-assured | 4.5.1 | 5.4.0 | Jakarta servlet compatibility |
| JaCoCo | 0.8.7 | 0.8.11 | Java 17 bytecode support |
| Spotless | 6.2.1 | 6.25.0 | Gradle 8.x compatibility |
| mockito-inline | 4.0.0 | (removed) | Bundled in spring-boot-starter-test for Boot 3 |

---

## 9. Selenium / WebDriverWait

`WebDriverWait(WebDriver, long)` constructor was deprecated and removed in
Selenium 4.x. Replaced with `WebDriverWait(WebDriver, Duration.ofSeconds(...))`.

---

## 10. Flyway Migrations

No changes were required to the Flyway migration scripts (`V1__create_tables.sql`,
`V2__seed_data.sql`). SQLite + Flyway continues to work without modification.

---

## 11. Spotless Configuration

The `spotless` Gradle task's `target` was updated to exclude `build/**` instead of
only `build/generated/**` to avoid a Gradle 8.x task dependency validation error.

---

## Recommendations

1. **Joda-Time → `java.time`**: Consider migrating from Joda-Time to
   `java.time` (JSR-310), which has been the standard since Java 8.
2. **Schema naming convention**: Rename `ArticleEdge` → `ArticlesEdge` to follow
   Relay conventions and re-enable Spring GraphQL schema inspection.
3. **SQLite for production**: Consider migrating to PostgreSQL or MySQL for
   production workloads; SQLite is best suited for development/testing.
4. **Spring Boot 3.3+**: When ready, upgrade to Spring Boot 3.3+ for virtual
   threads support and further improvements.
