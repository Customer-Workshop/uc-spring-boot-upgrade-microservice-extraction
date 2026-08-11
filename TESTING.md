# Testing Conventions

JUnit 5 (Jupiter) + Mockito, with a real SQLite database for the persistence layer. There are no
TestContainers and no Docker requirement — everything runs in-process with `./gradlew test`.

## Commands

```bash
./gradlew test                                          # run the suite
./gradlew test --tests 'io.spring.graphql.*'            # run one package
./gradlew test jacocoTestReport -x jacocoTestCoverageVerification   # suite + coverage report
./gradlew spotlessApply                                 # google-java-format, required before commit
```

Coverage report: `build/reports/jacoco/test/html/index.html`. The repo-wide
`jacocoTestCoverageVerification` gate requires 80% line coverage; do not modify or work around it.

## Which pattern for which layer

### REST controllers — MockMvc

`@WebMvcTest(XxxApi.class)` with collaborators as `@MockBean`, plus `@Import` for the pieces the
controller genuinely needs (`WebSecurityConfig`, `JwtTokenFilter`, ...). Requests go through
RestAssured MockMvc. See `src/test/java/io/spring/api/ProfileApiTest.java`.

```java
@WebMvcTest(ProfileApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;
  @MockBean private ProfileQueryService profileQueryService;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
  }
}
```

For a `@ControllerAdvice` on its own, skip the Spring context and use a throwaway controller:

```java
MockMvcBuilders.standaloneSetup(new TestController())
    .setControllerAdvice(new CustomizeExceptionHandler())
    .build();
```

See `src/test/java/io/spring/api/exception/CustomizeExceptionHandlerTest.java`.

### Services, GraphQL datafetchers and mutations — Mockito

Plain `@ExtendWith(MockitoExtension.class)`, no Spring context. Collaborators are `@Mock`, the
subject is built with its constructor.

```java
@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {
  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }
}
```

GraphQL tests extend `GraphQLTestBase`, which owns the `SecurityContext`
(`setCurrentUser(user)` / `setAnonymousUser()` / `setNullPrincipal()`) and clears it after each
test. It installs an anonymous token by default: `SecurityUtil.getCurrentUser()` throws an NPE when
the context holds no `Authentication` at all, and the real filter chain always populates one.

### Repositories and query services — integration tests

Extend `DbTestBase` (`@MybatisTest`, `@ActiveProfiles("test")`, real SQLite, rolled back per test)
and `@Import` the MyBatis repository implementations you need. Build real domain objects and assert
through the repository/service interface rather than mocking mappers.

```java
@Import({ArticleQueryService.class, MyBatisUserRepository.class, MyBatisArticleRepository.class})
public class ArticleQueryServiceTest extends DbTestBase {
  @Autowired private ArticleQueryService queryService;
}
```

Two traps in this layer:

- Each test method is its own MyBatis session. Within a method, an identical query is served from
  the session cache — and `CommentQueryService.findByArticleIdWithCursor` trims the extra record
  in place (`comments.remove(page.getLimit())`) on that cached list. Put each cursor scenario in
  its own test method instead of chaining queries.
- Cursor SQL applies `limit` to joined rows, not to articles, so an article with two tags consumes
  two rows. Use single-tag fixtures when asserting on page size or `hasNext()`.

## Conventions for all tests

- Method names read as `should_do_something()`; class name is `<ClassUnderTest>Test` in the same
  package as the class under test.
- Use `Assertions.assertX(...)` qualified (the codebase does not static-import assertions);
  Mockito's `when`/`verify`/matchers are static-imported.
- Shared fixtures: `io.spring.TestHelper` for `ArticleData` fixtures, `TestWithCurrentUser` for
  authenticated REST tests, `GraphQLTestBase` for GraphQL security context,
  `io.spring.ValidationTestHelper` for real `ConstraintViolationException`s (bean-level and
  method-parameter-level, e.g. `register.param.email`) — prefer these over mocking validation.
- Every test class covers the negative paths alongside the happy path:
  - unauthenticated access (anonymous / null principal) → `AuthenticationException`,
  - missing entities → `ResourceNotFoundException`,
  - acting on someone else's resource → `NoAuthorizationException`,
  - invalid input → real Bean Validation violations and the resulting 422 payload.
- Run `./gradlew spotlessApply` before committing; CI enforces google-java-format.
