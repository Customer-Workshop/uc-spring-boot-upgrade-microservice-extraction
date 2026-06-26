# Migration Proof: Spring Boot 2.6.3 to 3.2.5

## Java Version
| | Before | After |
|---|---|---|
| sourceCompatibility | 11 | 17 |
| targetCompatibility | 11 | 17 |

## Spring Boot Version
| | Before | After |
|---|---|---|
| Spring Boot | 2.6.3 | 3.2.5 |
| Spring Dependency Management | 1.0.11.RELEASE | 1.1.5 |

## Gradle
| | Before | After |
|---|---|---|
| Gradle | 7.4 | 8.7 |

## Dependency Versions

| Dependency | Before | After |
|---|---|---|
| mybatis-spring-boot-starter | 2.2.2 | 3.0.3 |
| graphql-dgs-spring-boot-starter | 4.9.21 | 8.7.1 (graphql-dgs-spring-graphql-starter) |
| jjwt-api / impl / jackson | 0.11.2 | 0.12.5 |
| joda-time | 2.10.13 | 2.12.7 |
| sqlite-jdbc | 3.36.0.3 | 3.45.1.0 |
| rest-assured (all modules) | 4.5.1 | 5.4.0 |
| mockito-inline | 4.0.0 | mockito-core 5.11.0 |
| selenium-java | 4.15.0 | 4.18.1 |
| webdrivermanager | 5.6.2 | 5.7.0 |
| testng | 7.8.0 | 7.9.0 |
| httpclient5 | 5.2.1 | 5.3.1 |
| jacoco | 0.8.7 | 0.8.11 |
| DGS Codegen | 5.0.6 | 6.2.1 |
| Spotless | 6.2.1 | 6.25.0 |
| extentreports | 5.1.1 | 5.1.1 (unchanged) |

## Test Results

| | Before | After |
|---|---|---|
| Total tests | 68 | 68 |
| Passed | 68 | 68 |
| Failed | 0 | 0 |
| Skipped | 0 | 0 |
| Build result | SUCCESS | SUCCESS |

## Key Migration Changes

### javax to jakarta namespace migration
- 20 source files migrated from `javax.validation.*` / `javax.servlet.*` to `jakarta.validation.*` / `jakarta.servlet.*`
- `javax.crypto.*` imports preserved (standard JDK, not Jakarta EE)
- Zero `javax.validation` or `javax.servlet` imports remaining in `src/`

### Spring Security 6.x migration
- Removed `WebSecurityConfigurerAdapter` (deleted in Spring Security 6)
- Migrated to `SecurityFilterChain` bean-based configuration
- Replaced `authorizeRequests()` / `antMatchers()` with `authorizeHttpRequests()` / `requestMatchers()`
- Replaced deprecated chained `.csrf().disable().cors().and()` with lambda DSL

### DGS Framework 8.x migration
- Migrated from `graphql-dgs-spring-boot-starter` to `graphql-dgs-spring-graphql-starter` (Spring for GraphQL integration)
- Updated `DataFetcherExceptionHandler.onException()` to `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>`
- Replaced `graphql.relay.DefaultPageInfo` / `DefaultConnectionCursor` with DGS-generated `io.spring.graphql.types.PageInfo` builder
- Configured `spring.graphql.schema.locations` and disabled schema inspection for custom connection types

### JJWT 0.12.x migration
- Replaced `Jwts.parserBuilder().setSigningKey()` with `Jwts.parser().verifyWith()`
- Replaced `parseClaimsJws()` with `parseSignedClaims()`
- Replaced `getBody()` with `getPayload()`
- Replaced `setSubject()` / `setExpiration()` with `subject()` / `expiration()`
- Replaced `SignatureAlgorithm` enum + `SecretKeySpec` with `Keys.hmacShaKeyFor()` (auto-selects algorithm from key length)
- Removed deprecated `SignatureAlgorithm` class usage

### Spring Framework 6.x migration
- Updated `ResponseEntityExceptionHandler.handleMethodArgumentNotValid()` signature: `HttpStatus` to `HttpStatusCode`

### Selenium 4.x compatibility
- Updated `WebDriverWait` constructor from `long` timeout to `Duration`

### Spotless configuration
- Fixed implicit dependency issue with Gradle 8.7 by scoping file tree to `src/**/*.java`

### Mockito 5.x
- Replaced deprecated `mockito-inline` artifact with `mockito-core` 5.11.0
