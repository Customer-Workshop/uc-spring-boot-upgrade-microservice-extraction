# Spring Boot 2 → 3 Upgrade Notes

## Overview

Upgraded the RealWorld blogging platform from Spring Boot 2.6.3 to Spring Boot 3.2.2. This required Java 17+, the Jakarta EE namespace migration, and several breaking API changes across the dependency tree.

---

## Breaking Changes & Resolutions

### 1. Gradle Wrapper: 7.4 → 8.5

Spring Boot 3.2 requires Gradle 8+. Updated `gradle/wrapper/gradle-wrapper.properties` to use `gradle-8.5-bin.zip`.

### 2. Java Version: 11 → 17

Spring Boot 3 requires Java 17 as a minimum. Updated `build.gradle`:
```gradle
sourceCompatibility = '17'
targetCompatibility = '17'
```

### 3. `javax.*` → `jakarta.*` Namespace Migration

Spring Boot 3 moved from Java EE (`javax.*`) to Jakarta EE (`jakarta.*`). All imports were updated across 20+ source files:

| Old Package | New Package | Files Affected |
|---|---|---|
| `javax.validation.*` | `jakarta.validation.*` | Validators, constraint annotations, params |
| `javax.servlet.*` | `jakarta.servlet.*` | `JwtTokenFilter` |
| `javax.annotation.*` | `jakarta.annotation.*` | Various (if any `@PostConstruct`, etc.) |

### 4. Spring Security: `WebSecurityConfigurerAdapter` Removed

**Problem:** `WebSecurityConfigurerAdapter` was removed in Spring Security 6 (Spring Boot 3).

**Resolution:** Replaced with a `@Bean SecurityFilterChain` method using the lambda DSL:

- Removed `extends WebSecurityConfigurerAdapter`
- Removed `@Override protected void configure(HttpSecurity http)`
- Added `@Bean public SecurityFilterChain securityFilterChain(HttpSecurity http)`
- Changed `.and()` chaining to lambda configuration:
  - `.csrf(AbstractHttpConfigurer::disable)`
  - `.cors(cors -> cors.configurationSource(...))`
  - `.sessionManagement(sm -> sm.sessionCreationPolicy(...))`
  - `.exceptionHandling(ex -> ex.authenticationEntryPoint(...))`
- Changed `.authorizeRequests()` → `.authorizeHttpRequests()`
- Changed `.antMatchers()` → `.requestMatchers()`

### 5. JJWT API: 0.11.2 → 0.12.3

**Problem:** JJWT 0.12.x introduced a new builder API, deprecating the old one.

**Resolution:**

| Old API (0.11.x) | New API (0.12.x) |
|---|---|
| `new SecretKeySpec(bytes, algo.getJcaName())` | `Keys.hmacShaKeyFor(bytes)` |
| `Jwts.builder().setSubject(id)` | `Jwts.builder().subject(id)` |
| `Jwts.builder().setExpiration(date)` | `Jwts.builder().expiration(date)` |
| `Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)` | `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` |
| `claimsJws.getBody().getSubject()` | `claimsJws.getPayload().getSubject()` |

### 6. Netflix DGS GraphQL: 4.9.21 → 8.2.0

**Problem 1:** DGS codegen changed `PageInfo` from `graphql.relay.PageInfo` to a generated `io.spring.graphql.types.PageInfo`.

**Resolution:** Replaced `DefaultPageInfo` / `DefaultConnectionCursor` with the generated `PageInfo.newBuilder()` pattern using string cursors directly.

**Problem 2:** `DataFetcherExceptionHandler.onException()` changed to `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>`.

**Resolution:** Updated `GraphQLCustomizeExceptionHandler` to implement the new `handleException` method signature and return `CompletableFuture.completedFuture(...)`.

### 7. Spring MVC: `ResponseEntityExceptionHandler` Signature Change

**Problem:** `handleMethodArgumentNotValid` parameter changed from `HttpStatus` to `HttpStatusCode`.

**Resolution:** Updated `CustomizeExceptionHandler` to use `HttpStatusCode` in the method signature.

### 8. DGS Codegen: 5.0.6 → 6.0.3

Updated the codegen plugin to be compatible with DGS 8.x.

### 9. MyBatis Spring Boot Starter: 2.2.2 → 3.0.3

Updated for Jakarta EE compatibility. No code changes required.

### 10. Selenium WebDriverWait: Duration API

**Problem:** `WebDriverWait(driver, long)` constructor deprecated in favor of `WebDriverWait(driver, Duration)`.

**Resolution:** Changed to `new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))`.

### 11. Other Dependency Updates

| Dependency | Old Version | New Version |
|---|---|---|
| `spring-dependency-management` | 1.0.11.RELEASE | 1.1.4 |
| `rest-assured` | 4.5.1 | 5.4.0 |
| `mockito-inline` | 4.0.0 | 5.2.0 |
| `jacoco` | 0.8.7 | 0.8.11 |
| `spotless` | 6.2.1 | 6.25.0 |

### 12. New Dependency: `spring-boot-starter-actuator`

Added to provide `/actuator/health` endpoints for health checking in both the monolith and the extracted microservice.
