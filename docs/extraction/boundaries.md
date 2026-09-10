# Extraction boundaries: comments-service and favorites-service

Authoritative plan for the first two cuts out of the RealWorld monolith
(Spring Boot 2.6.3 / Java 11). Two child sessions implement it, one per service.
This document is the contract: a child that must deviate reports the deviation
instead of silently changing the plan.

## 0. Ground rules

- The monolith stays on Spring Boot 2.6.3 / Java 11. Its build files, sources,
  migrations and tests are **not** edited by the children.
- Each service is an **independent Gradle project** under `services/<name>-service/`
  on **Spring Boot 3.x / Java 21**, with its own `settings.gradle`, `build.gradle`
  and Gradle wrapper. It is not added to the monolith's `settings.gradle`.
  (Deviation from `AGENTS.md`, which says "use the same Spring Boot version (2.6.3)
  as the monolith" — the program owner overrode this: new services are born on the
  current LTS stack. Everything else in `AGENTS.md` applies as written.)
- Each service owns one SQLite file and one Flyway `V1__` migration containing only
  its own tables plus the matching seed rows lifted from `V2__seed_data.sql`.
- Package root per service: `io.spring.comments` / `io.spring.favorites`, keeping the
  monolith's internal layering (`api/`, `core/`, `application/`, `infrastructure/`).
- Response bodies must be **byte-for-byte identical** to the monolith's. The copied
  API tests are the proof.

## 1. What moves

### 1.1 comments-service (`services/comments-service/`, default port 8081)

| Monolith file | Destination | Notes |
| --- | --- | --- |
| `core/comment/Comment.java` | `core/comment/Comment.java` | unchanged (joda `DateTime`, UUID id) |
| `core/comment/CommentRepository.java` | same | unchanged interface |
| `infrastructure/repository/MyBatisCommentRepository.java` | same | unchanged |
| `infrastructure/mybatis/mapper/CommentMapper.java` | same | unchanged |
| `resources/mapper/CommentMapper.xml` | same | unchanged |
| `infrastructure/mybatis/readservice/CommentReadService.java` | same | unchanged signature |
| `resources/mapper/CommentReadService.xml` | same | **rewritten**: no `left join users`, no `ArticleReadService.profileColumns` include; select comment columns only and map to `CommentData` with a null `profileData`. Author profiles are filled in by `CommentQueryService` from `MonolithClient`. |
| `application/CommentQueryService.java` | same | `UserRelationshipQueryService` dependency replaced by `MonolithClient` (profile + `following` in one call) |
| `application/data/CommentData.java`, `ProfileData.java` | `application/data/` | unchanged field order and Jackson annotations |
| `application/CursorPager`, `CursorPageParameter`, `DateTimeCursor`, `PageCursor`, `Node` | `application/` | needed by `findByArticleIdWithCursor` (GraphQL path, see §4) |
| `api/CommentsApi.java` (+ package-private `NewCommentParam`) | `api/CommentsApi.java` | `ArticleRepository` replaced by `MonolithClient` |
| `api/exception/*` (`CustomizeExceptionHandler`, `ErrorResource`, `ErrorResourceSerializer`, `FieldErrorResource`, `InvalidRequestException`, `NoAuthorizationException`, `ResourceNotFoundException`) | `api/exception/` | unchanged; `javax.validation` → `jakarta.validation` |
| `api/security/JwtTokenFilter.java`, `WebSecurityConfig.java` | `api/security/` | `WebSecurityConfigurerAdapter` → `SecurityFilterChain` bean; principal loaded via `MonolithClient` instead of `UserRepository` |
| `core/service/JwtService.java`, `infrastructure/service/DefaultJwtService.java` | same | same `jwt.secret` / `jwt.sessionTime` as the monolith (config, not hard-coded) |
| `core/service/AuthorizationService.java` | `core/service/AuthorizationService.java` | reduced to `canWriteComment(userId, articleAuthorId, comment)` — the service has no `Article`/`User` aggregate |
| `JacksonCustomizations.java` | `io/spring/comments/JacksonCustomizations.java` | unchanged — joda `DateTime` → `ISODateTimeFormat.dateTime().withZoneUTC()` |
| `infrastructure/mybatis/DateTimeHandler.java` | same | unchanged |

**Tables:** `comments` only. `V1__init.sql` = the `comments` DDL from
`V1__create_tables.sql` + the five `comment-1..comment-5` rows from `V2__seed_data.sql`.
No `users`, no `articles`, no `follows` copies.

**Tests that move:** `api/CommentsApiTest.java` and `api/TestWithCurrentUser.java`
(adapted: `@MockBean MonolithClient` in place of `ArticleRepository`/`UserRepository`/
`UserReadService`), plus `infrastructure/comment/MyBatisCommentRepositoryTest.java`
and `application/comment/CommentQueryServiceTest.java` and the `DbTestBase`/`TestHelper`
support they need. All five `CommentsApiTest` cases must pass unchanged in intent:
201 create, 422 empty body, 200 list, 204 delete, 403 delete by a third party.
The monolith keeps its own copies — nothing is deleted from the monolith in this wave.

### 1.2 favorites-service (`services/favorites-service/`, default port 8082)

| Monolith file | Destination | Notes |
| --- | --- | --- |
| `core/favorite/ArticleFavorite.java` | `core/favorite/ArticleFavorite.java` | unchanged |
| `core/favorite/ArticleFavoriteRepository.java` | same | unchanged |
| `infrastructure/repository/MyBatisArticleFavoriteRepository.java` | same | unchanged |
| `infrastructure/mybatis/mapper/ArticleFavoriteMapper.java` + `.xml` | same | unchanged |
| `infrastructure/mybatis/readservice/ArticleFavoritesReadService.java` + `.xml` | same | **rewritten**: `articlesFavoriteCount` and `userFavorites` must not join `articles` (that table does not exist here) — group over `article_favorites` filtered by the requested ids; `userFavorites` takes a `viewerId` string instead of a `User` |
| `application/data/ArticleFavoriteCount.java` | `application/data/` | unchanged |
| `application/data/ArticleData.java`, `ProfileData.java` | `application/data/` | unchanged field order and Jackson annotations — this is the response envelope |
| `api/ArticleFavoriteApi.java` | `api/ArticleFavoriteApi.java` | `ArticleRepository` + `ArticleQueryService` replaced by `MonolithClient` + local favorite counts (see §3.2) |
| `api/exception/*`, `api/security/*`, `JwtService`/`DefaultJwtService`, `JacksonCustomizations`, `DateTimeHandler` | as for comments-service | same adaptations |

**Tables:** `article_favorites` only. `V1__init.sql` = that DDL + the six favorite rows
from `V2__seed_data.sql`.

**Tests that move:** `api/ArticleFavoriteApiTest.java` + `api/TestWithCurrentUser.java`
(adapted) and `infrastructure/favorite/MyBatisArticleFavoriteRepositoryTest.java`.
Both API cases must pass: favorite → 200 with `article.id`, unfavorite → 200 with
`article.id`, and the repository save/remove interactions.

## 2. Dependencies that become HTTP calls

Both services replace `ArticleRepository` / `UserRepository` / `UserRelationshipQueryService`
with a `MonolithClient` (`infrastructure/monolith/MonolithClient.java`, `RestTemplate` or
`RestClient`, base URL from `monolith.base-url`, connect/read timeouts from config, DTOs in
`infrastructure/monolith/dto/`). Failures map to domain exceptions in the service:

- HTTP 404 → `ResourceNotFoundException` (→ 404 to the caller, same as today)
- connect/read timeout, 5xx, malformed body → `MonolithUnavailableException`
  (`@ResponseStatus(SERVICE_UNAVAILABLE)`, message names the endpoint that failed)
- never swallow and never substitute a fabricated article/profile.

The monolith endpoints below are **added by the program owner in the strangler PR**
(`/internal/**`, unauthenticated, intended for the service network only, registered only
when `internal.api.enabled=true`). Children code against this contract and stub
`MonolithClient` in tests; they do not need a running monolith to go green.

```
GET /internal/articles/{slug}[?viewerId={userId}]
200 {"article":{"id","slug","title","description","body",
                "createdAt","updatedAt","tagList":[...],
                "author":{"id","username","bio","image","following"}}}
404 when the slug is unknown

GET /internal/users/{userId}[?viewerId={userId}]
200 {"profile":{"id","username","bio","image","following"}}
404 when unknown

GET /internal/users?ids=a,b,c[&viewerId={userId}]     # batch, for comment lists
200 {"profiles":[{"id","username","bio","image","following"}, ...]}
```

Timestamps in `/internal/**` use the same ISO-8601 millis-UTC format as the public API,
so services can deserialize straight into joda `DateTime`.

### 2.1 Who calls what

- **comments-service**: `GET /internal/articles/{slug}` (needs `article.id` for the
  comment key and `author.id` for `canWriteComment`); `GET /internal/users/{id}` for
  the single-comment response; `GET /internal/users?ids=` for the list response
  (one call, no N+1).
- **favorites-service**: `GET /internal/articles/{slug}?viewerId=` for every field of
  the `article` envelope except `favorited` / `favoritesCount`, which come from its own
  `article_favorites` table.

### 2.2 Endpoints each service must expose back to the monolith

Needed by the strangler PR so that the monolith's remaining read paths and the GraphQL
resolvers stay correct while the flags are on (§4). Same JSON conventions:

```
comments-service
GET  /internal/comments?articleId=&cursor=&limit=&direction=NEXT|PREV
200 {"comments":[<CommentData>...],"hasNext":bool,"hasPrevious":bool,
     "startCursor":"<millis>","endCursor":"<millis>"}

favorites-service
GET  /internal/favorites?articleIds=a,b,c[&viewerId=]
200 {"favorites":[{"articleId","favoritesCount","favorited"}, ...]}
```

## 3. Exact JSON contract to preserve

Lifted from `CommentsApiTest` / `ArticleFavoriteApiTest` and the serializers
(`JacksonCustomizations`, `ErrorResourceSerializer`, `@JsonRootName`, `@JsonIgnore`).

Global:
- `spring.jackson.deserialization.UNWRAP_ROOT_VALUE=true` (request bodies are root-wrapped:
  `{"comment":{"body":"..."}}`).
- All `DateTime` values serialize as `ISODateTimeFormat.dateTime().withZoneUTC()`,
  e.g. `2021-09-01T12:00:00.000Z`.
- Auth header is `Authorization: Token <jwt>`; unauthenticated write → **401**
  (`HttpStatusEntryPoint`), not 403.
- `GET` on comments is anonymous-allowed; `POST`/`DELETE` require a token.

### 3.1 Comments

```
POST /articles/{slug}/comments      201
{"comment":{"id","body","createdAt","updatedAt",
            "author":{"username","bio","image","following"}}}
GET  /articles/{slug}/comments      200
{"comments":[ <comment object as above>, ... ]}          # [] when none
DELETE /articles/{slug}/comments/{id}   204, empty body
```
- `CommentData.articleId` is `@JsonIgnore` — it must **not** appear.
- `ProfileData.id` is `@JsonIgnore` — the author object has exactly
  `username`, `bio`, `image`, `following`.
- `updatedAt` mirrors `createdAt` (the monolith writes `created_at` into both columns
  and `CommentData` is built with `createdAt` twice) — keep that behaviour.
- Empty body → **422** `{"errors":{"body":["can't be empty"]}}`.
- Unknown slug → 404. Comment id not on that article → 404.
- Deleting someone else's comment on someone else's article → **403**;
  the article author and the comment author may both delete.

### 3.2 Favorites

```
POST   /articles/{slug}/favorite    200
DELETE /articles/{slug}/favorite    200
{"article":{"id","slug","title","description","body","favorited","favoritesCount",
            "createdAt","updatedAt","tagList":[...],
            "author":{"username","bio","image","following"}}}
```
- Field order and names come from `ArticleData`; `author` is `ProfileData` with
  `id` ignored, exactly as above.
- `favorited` = a row exists in this service's `article_favorites` for
  (article, current user); `favoritesCount` = count for the article. Every other field
  is echoed from `/internal/articles/{slug}?viewerId=<current user>`.
- Favoriting twice is idempotent (existing `MyBatisArticleFavoriteRepository.save`
  guard) and still returns 200 with the unchanged count.
- Unfavoriting something not favorited returns 200 (no error), same as today.
- Unknown slug → 404. No token → 401.

## 4. GraphQL resolvers for comments and favorites — decision

**Keep them, routed through the new services.** They are not retired in this wave.

Reasoning: the DGS resolvers (`CommentDatafetcher`, `CommentMutation`, the
`favoriteArticle` / `unfavoriteArticle` mutations in `ArticleMutation`) are the only
consumers of cursor-paginated comment reads, and the frontend's GraphQL page still
uses them; retiring them would be a user-visible removal that this wave does not need.

How it stays true when the strangler flags are on:
- comment/favorite **mutations** in GraphQL go through the same monolith-side gateway
  as the REST controllers (§5), so they hit the services when the flags are set.
- `Article.comments` (cursor connection) is served by
  `GET /internal/comments?...` on comments-service — that is why comments-service ports
  `CursorPager` / `DateTimeCursor` and `findByArticleIdWithCursor`.
- `Article.favorited` / `Article.favoritesCount` (and the same fields on the REST
  article endpoints) are served by `GET /internal/favorites?...` on favorites-service.

Consequence for the children: those two `/internal/**` endpoints are **in scope** and
must be tested. Without them the GraphQL path would silently diverge from REST once the
flags are on, which is the failure mode this decision exists to avoid.

## 5. Monolith-side work (program owner, not the children)

Third PR, after both service PRs are open:
- `/internal/**` read endpoints of §2 behind `internal.api.enabled`.
- `CommentGateway` / `FavoriteGateway` ports with an in-process implementation (today's
  repositories and query services) and a remote implementation selected when
  `COMMENTS_SERVICE_URL` / `FAVORITES_SERVICE_URL` are set. `CommentsApi`,
  `ArticleFavoriteApi`, `CommentMutation`, `CommentDatafetcher` and the favorite
  mutations depend on the port, not the repositories.
- Proof: `CommentsApiTest` and `ArticleFavoriteApiTest` run green twice — flags unset
  (in-process) and flags set against the two running services — both outputs in the PR.

## 6. File ownership

| Path | Owner |
| --- | --- |
| `services/comments-service/**` | child A (comments) |
| `services/favorites-service/**` | child B (favorites) |
| `src/**`, `build.gradle`, `settings.gradle`, `frontend/**` | program owner only |
| `docs/**`, `.migration/**` | program owner only |
| `AGENTS.md` | program owner only |

No child edits monolith source, monolith migrations, `docs/`, `.migration/`, or the other
child's folder. Each child opens exactly one PR into `main` touching only its own folder.

## 7. Definition of done per child

1. `./gradlew build -x jacocoTestCoverageVerification` passes inside
   `services/<name>-service/`.
2. The monolith still builds and its full suite still passes
   (`./gradlew build -x jacocoTestCoverageVerification` at the repo root) — unchanged,
   because no monolith file was touched.
3. The copied API tests pass against the new service and assert the §3 envelope.
4. One PR into `main`, Devin Review findings addressed, at most two review rounds.
5. Report back: PR link, and every place the implementation deviated from this document.
