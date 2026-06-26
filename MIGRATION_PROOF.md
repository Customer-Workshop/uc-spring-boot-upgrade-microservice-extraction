# Migration Proof: Java 11 + Spring Boot 2.6.3 → Java 17 + Spring Boot 3.2.5

## Before State

| Component | Version |
|-----------|---------|
| Java | 11 |
| Spring Boot | 2.6.3 |
| Spring Dependency Management | 1.0.11.RELEASE |
| Gradle | 7.4 |
| DGS Framework | 4.9.21 |
| DGS Codegen Plugin | 5.0.6 |
| MyBatis Spring Boot | 2.2.2 |
| jjwt | 0.11.2 |
| SQLite JDBC | 3.36.0.3 |
| Joda-Time | 2.10.13 |
| Rest-Assured | 4.5.1 |
| Mockito | 4.0.0 (mockito-inline) |
| Spotless | 6.2.1 |
| JaCoCo | 0.8.7 |

**Build Result:** BUILD SUCCESSFUL  
**Tests:** 68 passed, 0 failures, 0 errors

## After State

| Component | Version |
|-----------|---------|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Dependency Management | 1.1.5 |
| Gradle | 8.5 |
| DGS Framework | 8.5.0 (Spring GraphQL integration) |
| DGS Codegen Plugin | 6.2.1 |
| MyBatis Spring Boot | 3.0.3 |
| jjwt | 0.12.5 |
| SQLite JDBC | 3.45.1.0 |
| Joda-Time | 2.12.7 |
| Rest-Assured | 5.4.0 |
| Mockito | 5.11.0 (mockito-core) |
| Spotless | 6.25.0 |
| JaCoCo | 0.8.11 |

**Build Result:** BUILD SUCCESSFUL  
**Tests:** 68 passed, 0 failures, 0 errors

## Migration Summary

### Namespace Migration (javax → jakarta)
- 40 imports migrated across 21 source files
- `javax.validation.*` → `jakarta.validation.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.crypto.*` retained (JDK package, not Jakarta EE)

### Spring Security 6 Migration
- Removed `WebSecurityConfigurerAdapter` (deleted in Spring Security 6)
- Replaced with `SecurityFilterChain` @Bean method
- Migrated `antMatchers()` → `requestMatchers()`
- Migrated chaining DSL → lambda DSL (`csrf(AbstractHttpConfigurer::disable)`, etc.)
- Migrated `authorizeRequests()` → `authorizeHttpRequests()`

### DGS Framework Migration (4.x → 8.x)
- Migrated from standalone DGS starter to Spring for GraphQL integration (`graphql-dgs-spring-graphql-starter`)
- Added DGS Platform BOM for version management
- Replaced `graphql.relay.DefaultPageInfo` usage with DGS codegen-generated `PageInfo` type
- Updated `DataFetcherExceptionHandler.onException()` → `handleException()` (returns `CompletableFuture`)
- Disabled Spring GraphQL schema inspection (`spring.graphql.schema.inspection.enabled=false`) for DGS compatibility

### JWT (jjwt) Migration (0.11.x → 0.12.x)
- Replaced `SignatureAlgorithm` enum with automatic key-based algorithm selection via `Keys.hmacShaKeyFor()`
- `Jwts.builder().setSubject()` → `.subject()`
- `Jwts.builder().setExpiration()` → `.expiration()`
- `Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws()` → `Jwts.parser().verifyWith(key).build().parseSignedClaims()`
- `.getBody()` → `.getPayload()`

### Spring MVC Changes
- `ResponseEntityExceptionHandler.handleMethodArgumentNotValid()` signature: `HttpStatus` → `HttpStatusCode`

### Gradle Changes
- Upgraded wrapper from 7.4 to 8.5
- Fixed Spotless plugin target to `src/**/*.java` (avoids implicit task dependency issues with Gradle 8.x)

### Selenium Test Fix
- `WebDriverWait(driver, long)` → `WebDriverWait(driver, Duration.ofSeconds(long))` (deprecated constructor removed)
