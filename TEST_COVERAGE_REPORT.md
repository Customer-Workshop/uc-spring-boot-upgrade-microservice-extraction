# Test Coverage Report

## Coverage Summary

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Instruction Coverage | 33% | 94.0% | +61% |
| Line Coverage | 33% | 98.4% | +65.4% |
| Total Tests | ~60 | 265+ | +200+ tests |
| JaCoCo Verification (80% threshold) | FAIL | PASS | ✓ |

## Test Coverage Matrix — Tests to Business Requirements

| Business Requirement | Test Class(es) | Test Count | Coverage |
|---|---|---|---|
| **User Registration** | UserMutationTest, UserServiceUnitTest | 5 | createUser success, constraint violations, password encoding |
| **User Login/Auth** | UserMutationTest, SecurityUtilTest | 6 | login success, invalid password, email not found, JWT extraction |
| **User Profile Update** | UserMutationTest, UserServiceUnitTest | 5 | update success, anonymous access, null principal, partial updates |
| **Article CRUD** | ArticleMutationTest, ArticleCommandServiceTest | 12 | create, update (success/not-found/no-auth), delete, favorite/unfavorite |
| **Article Query/Feed** | ArticleDatafetcherTest, ArticleQueryServiceUnitTest | 20 | getFeed, userFeed, userFavorites, cursor pagination, slug lookup |
| **Article Listing (REST)** | ArticleQueryServiceTest (integration) | 9 | by tag, by author, by favorite, feed, pagination |
| **Comment CRUD** | CommentMutationTest, CommentDatafetcherTest | 15 | create/delete success, not-found, auth failures, cursor pagination |
| **Comment Query** | CommentQueryServiceUnitTest | 10 | findById, findByArticleId, cursor pagination, following info |
| **Follow/Unfollow** | RelationMutationTest | 6 | follow/unfollow success, not-found, auth failures |
| **User Profile View** | ProfileDatafetcherTest, ProfileQueryServiceTest | 8 | profile lookup, author resolution, following status |
| **Current User (Me)** | MeDatafetcherTest | 4 | authenticated, anonymous, null principal, token |
| **Tags** | TagDatafetcherTest, TagsApiTest | 4 | getTags success, empty tags |
| **GraphQL Error Handling** | GraphQLCustomizeExceptionHandlerTest | 8 | auth errors, constraint violations, default delegation |
| **REST Error Handling** | CustomizeExceptionHandlerTest, ErrorResourceSerializerTest | 8 | invalid request, auth, constraint violations, serialization |
| **Authorization** | AuthorizationServiceTest | 5 | article author, comment author, article owner, denial |
| **Pagination** | CursorPagerTest, CursorPageParameterTest, ParameterizedTests | 15 | limit capping, direction invariants, cursors, data-driven |
| **Data Integrity** | DataClassTest | 12 | equals, hashCode, toString for all data classes |
| **Date/Time Handling** | DateTimeHandlerTest | 6 | MyBatis type handler: set/get with null/non-null values |
| **Utility Functions** | UtilTest, ParameterizedTests | 8 | isEmpty with null/empty/non-empty, parameterized edge cases |

## Dead Code Analysis

The following methods have 0% instruction coverage and may be dead code:

| Class | Method | Instructions | Assessment |
|---|---|---|---|
| `RealWorldApplication` | `main()` | 5 | Spring Boot entry point — not testable in unit tests |
| `Util` | `<init>()` | 3 | Private constructor of utility class — expected |
| `SecurityUtil` | `<init>()` | 3 | Private constructor of utility class — expected |
| `AuthorizationService` | `<init>()` | 3 | Private constructor of utility class — expected |
| `MyBatisCommentRepository` | `remove()` | 6 | Repository method — may be unused in current codebase |
| `MyBatisUserRepository` | `findById()` | 6 | Repository method — may be unused (findByEmail/findByUsername used instead) |
| `UpdateUserValidator` | `lambda$isValid$1` | 5 | Partial branch in username validation |
| `UpdateUserParam$Builder` | `toString()` | 12 | Lombok-generated builder toString — rarely called |
| `NewArticleParam$Builder` | `toString()` | 11 | Lombok-generated builder toString — rarely called |
| `CursorPager` | `isNext()`, `isPrevious()` | 6 | Lombok-generated getters duplicating hasNext/hasPrevious |
| `Article` | `hashCode()` | 20 | Lombok-generated — only exercised if Article is used in hash-based collections |

### Recommendations
1. **`MyBatisCommentRepository.remove()`** — Verify if this is actually called anywhere in production code. If not, consider removing it.
2. **`MyBatisUserRepository.findById()`** — This appears unused; the codebase uses `findByEmail` and `findByUsername` for lookups.
3. **`CursorPager.isNext()`/`isPrevious()`** — These duplicate `hasNext()`/`hasPrevious()`. Consider removing the Lombok `@Getter` for these fields and keeping only the explicit methods.

## Parameterized & Property-Based Tests

The `ParameterizedTests` class includes:
- **@NullAndEmptySource** — Verifies `Util.isEmpty()` invariant for null and empty strings
- **@ValueSource(strings)** — Non-empty strings always return false from `isEmpty()`
- **@ValueSource(ints)** — CursorPageParameter limit boundaries (negative, zero, valid, over-max)
- **@CsvSource** — Article slug generation from titles
- **@MethodSource** — Page parameter preservation, CommentData cursor derivation
- Property invariants: CursorPager NEXT never has previous, PREV never has next
