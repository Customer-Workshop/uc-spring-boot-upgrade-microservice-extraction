# Code Review — Top 10 Code Quality Concerns

Scope: `src/main/java/io/spring` (REST API, GraphQL datafetchers, application services, MyBatis
infrastructure) as of this review. Items are ordered roughly by risk. Each item names the concrete
code involved so it can be picked up as a work item; none of them are fixed by this review.

---

## 1. Credential handling: passwords stored unencoded on update, signing key committed

Two distinct defects, both in how the application treats credentials.

**Password updates bypass the encoder.** `UserService.createUser` encodes the password
(`passwordEncoder.encode(...)`), but `UserService.updateUser` forwards
`updateUserParam.getPassword()` straight into `User.update(...)`, which assigns it to the password
field verbatim. Any password changed through `PUT /user` (or the GraphQL `updateUser` mutation, which
routes through the same service) is persisted in cleartext, and the affected account can no longer log
in, because `UsersApi.userLogin` compares with `passwordEncoder.matches`. This is the highest-severity
item in this review.

**The JWT signing key is in version control.** `src/main/resources/application.properties` contains a
hard-coded 88-character `jwt.secret`, a fixed `jwt.sessionTime`, and a file-based SQLite URL
(`jdbc:sqlite:dev.db`) with no environment-variable override or profile separation. Every deployment
built from this repository shares the key, so token forgery requires nothing more than repository read
access. The same file enables `DEBUG` MyBatis logging unconditionally, which logs query parameters.

## 2. Unchecked `Optional.get()` and unguarded dereferences on the request path

`Optional.get()` is called without a presence check in several controllers and services:

- `ArticlesApi.createArticle` — `articleQueryService.findById(article.getId(), user).get()`
- `CurrentUserApi.currentUser` / `updateProfile` — `userQueryService.findById(...).get()`
- `UsersApi.createUser` / `userLogin` — `userQueryService.findById(...).get()`
- `CommentsApi.createComment` — `commentQueryService.findById(comment.getId(), user).get()`
- `ArticleFavoriteApi.favoriteArticle` / `unfavoriteArticle` — `articleQueryService.findBySlug(slug, user).get()`
- `ArticleApi.updateArticle` — `articleQueryService.findBySlug(updatedArticle.getSlug(), user).get()`

These are "should never happen" reads immediately after a write, but the write and the read go
through different code paths (repository vs. MyBatis read service) and are not in a shared
transaction (see #5). Any inconsistency surfaces as `NoSuchElementException` → HTTP 500 with no
diagnostic context, rather than a domain error. Prefer `orElseThrow(...)` with a domain exception,
or have the command services return the projected data directly.

Related: `CommentQueryService.findById` dereferences `user.getId()` with no null check, while its
sibling `findByArticleId` explicitly guards `user != null`. Any call path that reaches it
anonymously throws `NullPointerException`.

## 3. Authorization header parsed without validation

`CurrentUserApi` reads the raw header and indexes into the split result:

```java
authorization.split(" ")[1]
```

There is no check that the header is present in the expected `Bearer <token>` shape, so a request
that authenticates through a different mechanism, or a malformed header that still passes the
filter, yields `ArrayIndexOutOfBoundsException` → HTTP 500. `JwtTokenFilter.getTokenString` already
implements the safe version of this parse; the controller duplicates the logic instead of reusing
it. The endpoint also echoes the caller's existing token back rather than deriving it from
`JwtService`, which couples the response body to the transport header.

## 4. Missing validation on update payloads

- `UpdateArticleParam` has **no** constraint annotations at all: no `@NotBlank`, and — unlike
  `NewArticleParam` — no `@DuplicatedArticleConstraint`. Renaming an article to an existing title
  therefore produces a duplicate slug that `ArticleRepository.findBySlug` cannot disambiguate, even
  though creating that same title is rejected.
- `UpdateUserParam` defaults every field to `""` (via `@Builder.Default`), and `User.update` skips
  empty values. A client that sends an explicitly empty field gets a silent no-op with a 200
  response rather than a validation error — "ignored" and "applied" are indistinguishable to the
  caller.
- The two update-param classes disagree on their own null convention: `UpdateUserParam` declares
  `@Builder.Default` empty strings, `UpdateArticleParam` uses bare field initializers with no
  builder. Callers cannot tell from the types whether an omitted field arrives as `null` or `""`.

## 5. Transaction boundaries are effectively absent

`@Transactional` appears only on `MyBatisArticleRepository.save`. Multi-step operations run
unprotected:

- `CommentsApi.createComment` — save comment, then re-read it through a different service.
- `ProfileApi.follow` / `unfollow` — write the relation, then re-read the profile projection.
- `ArticleFavoriteApi` — write/remove the favorite, then re-read article data and favorite counts.
- `MyBatisArticleRepository.createNew` — inserts tags, tag relations, and the article; it is covered
  only because `save` happens to be annotated.

Partial failure leaves inconsistent state, and the read-after-write in each handler can observe a
half-applied change. Transaction management belongs at the application-service boundary, not
sporadically on one repository method.

## 6. Error handling swallows failures instead of reporting them

- `ErrorResourceSerializer.serialize` catches `IOException` and calls `e.printStackTrace()` inside a
  lambda, producing a truncated JSON error body with no server-side record beyond stdout.
- `DefaultJwtService.getSubFromToken` catches bare `Exception` and returns `Optional.empty()`, so an
  expired token, a tampered signature, and a misconfigured signing key are all indistinguishable —
  both to the caller (an undifferentiated 401) and to operators.
- `CustomizeExceptionHandler` handles only `InvalidRequestException`,
  `InvalidAuthenticationException`, `MethodArgumentNotValidException`, and
  `ConstraintViolationException`. There is no catch-all, so anything else falls through to the
  container's default error handling.
- No logging framework is used anywhere in `src/main/java` — there is not a single logger. Failures
  are invisible in production.

## 7. Application layer depends on infrastructure (wrong dependency direction)

`io.spring.application.ArticleQueryService`, `CommentQueryService`, `ProfileQueryService`,
`TagsQueryService`, and `UserQueryService` all import `io.spring.infrastructure.mybatis.readservice.*`
directly. The package layout advertises a ports-and-adapters structure (`core` / `application` /
`infrastructure`), but the read side bypasses it: there is no interface in `application` or `core`
that the MyBatis read services implement, unlike the write side where `core.article.ArticleRepository`
is implemented by `infrastructure.repository.MyBatisArticleRepository`.

Related inversion: `io.spring.core.service.JwtService` — a core interface — carries a Spring
`@Service` annotation, putting a framework dependency in the domain layer. This coupling is the main
obstacle to extracting any of these contexts into a separate service.

## 8. Response envelopes built with double-brace-initialized `HashMap`

Every controller constructs responses like:

```java
return ResponseEntity.ok(new HashMap<String, Object>() {{ put("article", articleData); }});
```

This anonymous-subclass idiom appears in all eight REST controllers. It creates a new class per call
site, holds an implicit reference to the enclosing controller instance, and defeats any static
guarantee about the response shape. Compounding it, most handlers return raw, unparameterized
`ResponseEntity` (or `ResponseEntity<?>`), so the API contract is not expressed in types at all and
cannot be introspected by tooling. Typed response records/DTOs would remove both problems.

## 9. Authorization rules are static, coarse, and partly implicit

`AuthorizationService` is a final-in-practice utility with two `static` methods. Consequences:

- It cannot be injected, mocked, or replaced, so controller tests exercise the real rule and
  alternative policies (moderators, admins) cannot be layered in without editing call sites.
- `canWriteComment` grants the *article* author the right to delete any comment on their article.
  That may be intended, but it is a non-obvious policy expressed only as a boolean expression.
- Nothing enforces it consistently: `ProfileApi.follow` permits a user to follow themselves, and
  `ArticleFavoriteApi.unfavoriteArticle` silently returns 200 when no favorite exists — both are
  authorization/idempotency decisions made implicitly by omission.

## 10. Naming, dead code, and inconsistent limits

- `ArticleQueryService.findUserFeedWithCursor` / `findUserFeed` use the misspelled local
  `followdUsers` (twice).
- `UserMutation` names its `PasswordEncoder` field `encryptService` — password *hashing* is not
  encryption, and the name misleads about reversibility.
- `ArticleDatafetcher.getFeed` throws
  `new IllegalArgumentException("first 和 last 必须只存在一个")` — a non-English message in an
  otherwise English codebase, surfaced to API clients.
- `io.spring.Util` is a catch-all class holding a single `isEmpty` helper that duplicates
  `StringUtils.isEmpty` / `String.isEmpty` semantics already available.
- Page-size caps disagree between the two pagination models: `Page.MAX_LIMIT` is 100 while
  `CursorPageParameter.MAX_LIMIT` is 1000, so the same logical query is capped ten times higher over
  GraphQL than over REST.
- `DuplicatedArticleValidator` calls `articleQueryService.findBySlug(slug, null)` with a `null`
  user, relying on the service's null-tolerance as an implicit contract rather than a documented one.

---

### Also noted (below the top 10)

- `ArticleMapper.xml` updates only non-empty article columns and never touches
  `article_tags`, so tag edits submitted through `PUT /articles/{slug}` are silently dropped; the
  same statement gates the `slug` assignment on `article.title != ''`, coupling slug persistence to
  the title field.
- `CommentReadService.xml`'s cursor query applies no `limit`, so cursor-based comment pagination can
  return an unbounded result set (the trim happens in Java, after the full fetch).
- Article/comment enrichment (`fillExtraInfo`) issues separate favorite-count, favorite-flag, and
  following-flag queries per page of results; batched, but still three extra round trips per list
  request and unbounded in the `IN (...)` clause size.
- `RealworldApplicationTests` and the Selenium suite aside, there is no test covering the
  `Authorization`-header parse in `CurrentUserApi` or the `Optional.get()` paths in #1.
- CORS is configured with `allowedOrigins("*")` for all paths; acceptable for a demo, but it should
  be profile-scoped before any real deployment.
