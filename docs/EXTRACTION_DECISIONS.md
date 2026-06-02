# Article Service Extraction Decisions

## Bounded Contexts Identified

| Context | Domain Objects | Responsibilities |
|---------|---------------|-----------------|
| **Articles/Tags** | `Article`, `Tag`, `article_tags` | CRUD for articles and tag management |
| **Comments** | `Comment` | Creating, listing, and deleting comments on articles |
| **Favorites** | `ArticleFavorite` | Favoriting/unfavoriting articles, favorite counts |
| **Users/Profiles** | `User`, `FollowRelation` | Registration, auth, profiles, follow relationships |

## What Moves to the Article Service

The Article service owns the **Articles/Tags**, **Comments**, and **Favorites** bounded contexts:

### Domain Objects
- `Article` (core entity with slug, title, description, body, timestamps)
- `Tag` (tag names linked to articles via `article_tags`)
- `Comment` (body, article reference, user reference, timestamps)
- `ArticleFavorite` (article-user join for favorites)

### Database Tables
- `articles` — article content and metadata
- `tags` — tag definitions
- `article_tags` — article-to-tag join table
- `comments` — comments on articles
- `article_favorites` — user-article favorite join table

### API Endpoints (REST)
- `GET/POST /articles` — list/create articles
- `GET/PUT/DELETE /articles/{slug}` — single article CRUD
- `POST/DELETE /articles/{slug}/favorite` — favorite/unfavorite
- `GET/POST /articles/{slug}/comments` — list/create comments
- `DELETE /articles/{slug}/comments/{id}` — delete comment
- `GET /tags` — list all tags

### API Endpoints (GraphQL)
- Article queries and mutations
- Comment queries and mutations
- Tag queries

## What Stays in the Monolith (User Service)

- `User` entity and `UserRepository`
- `FollowRelation` and follow/unfollow logic
- `GET/POST /users` — registration and login
- `GET/PUT /user` — current user CRUD
- `GET /profiles/{username}` — profile retrieval
- `POST/DELETE /profiles/{username}/follow` — follow/unfollow
- JWT token issuance and validation
- GraphQL user/profile mutations and queries

## Coupling Points

### 1. Article ownership (`Article.userId`)
Articles store the author's `userId`. The Article service needs user profile data (username, bio, image) to populate the `ProfileData` in article/comment responses.

### 2. Comment ownership (`Comment.userId`)
Comments store the commenter's `userId`. Profile data is needed for comment responses.

### 3. Favorites (`ArticleFavorite.userId`)
Favorites reference `userId` to track which user favorited an article.

### 4. Follow relationships for feed/following status
- The article feed endpoint requires knowing which users the current user follows.
- Article and comment responses include `following` status for the author's profile.

### 5. Authorization
- `AuthorizationService.canWriteArticle` compares `user.getId()` with `article.getUserId()`
- `AuthorizationService.canWriteComment` compares user IDs for both article owner and comment author

### 6. JWT Authentication
The `JwtTokenFilter` resolves a `User` entity from the JWT subject claim. The Article service needs to authenticate requests using the same JWT secret.

## Cross-Service Communication Strategy

### Approach: REST Client with DTOs

The Article service replaces direct `User` domain object usage with:

1. **`UserServiceClient`** — A REST client (using `RestTemplate`) that calls the monolith's user endpoints to fetch profile data by user ID.

2. **`UserProfileDto`** — A lightweight DTO containing only the fields the Article service needs: `id`, `username`, `bio`, `image`.

3. **Simplified JWT Authentication** — The Article service validates JWTs independently (shared secret) and extracts only the user ID from the token. It does NOT load the full `User` entity locally. Instead, it passes the user ID to internal logic and fetches profile data from the User service only when building API responses.

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Shared JWT secret | Avoids token exchange complexity; both services validate tokens independently |
| User ID as the coupling key | Minimal data needed for authorization; profile data fetched on-demand |
| REST over messaging | Synchronous profile lookups are acceptable for read-heavy API responses |
| Denormalized `ProfileData` in responses | Article service fetches and caches profile data per request |
| `UserRelationshipQueryService` stays in monolith | Follow relationships belong to the User/Profile context; Article service calls user service for follow status |

### Sequence: Article Read with Profile Enrichment

```
Client -> Article Service: GET /articles/{slug}
Article Service -> DB: Load article + tags + favorites
Article Service -> User Service: GET /api/users/{userId}/profile
User Service -> Article Service: { id, username, bio, image }
Article Service -> User Service: GET /api/users/{userId}/following?target={currentUserId}
User Service -> Article Service: { following: true/false }
Article Service -> Client: Full article response with author profile
```

### Fallback Behavior
If the User service is unavailable, the Article service returns articles with a placeholder profile (`username: "unknown"`) rather than failing the request entirely.
