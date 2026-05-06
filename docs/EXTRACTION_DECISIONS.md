# Article Service Extraction Decisions

## Overview

This document explains the domain boundary choices made when extracting the Article bounded context from the RealWorld blogging platform monolith into a standalone microservice.

---

## Bounded Context Identification

### What is the Article Bounded Context?

The Article bounded context encompasses all functionality related to content management in the blogging platform:

- **Articles** — CRUD operations for blog posts (create, read, update, delete)
- **Tags** — Categorization/labeling of articles
- **Comments** — User comments on articles
- **Favorites** — User-article favorite/unfavorite relationships

### Why This Boundary?

1. **Cohesion:** Articles, tags, comments, and favorites are tightly coupled — comments and favorites are meaningless without articles, and tags exist solely to classify articles. They share the same database tables and lifecycle.

2. **Loose Coupling with Users:** The article context references users only by `user_id` (foreign key). It does not need to know about user passwords, authentication logic, or profile management. This makes it a natural seam for extraction.

3. **Independent Scalability:** Article reads (listing, searching, feed) are typically the highest-traffic operations in a blogging platform. Extracting them allows independent scaling.

4. **Single Responsibility:** The remaining monolith (user-service) focuses purely on identity: registration, login, JWT issuance, profile management, and follow relationships.

---

## Domain Boundaries

### Article Service (Extracted)

| Entity | Tables | Responsibilities |
|---|---|---|
| Article | `articles` | Create, read, update, delete articles |
| Tag | `tags`, `article_tags` | Tag management, article-tag relationships |
| Comment | `comments` | Create, read, delete comments on articles |
| ArticleFavorite | `article_favorites` | Favorite/unfavorite articles |

**Endpoints owned:**
- `GET/POST /articles` — List/create articles
- `GET/PUT/DELETE /articles/{slug}` — Single article CRUD
- `GET /articles/feed` — User feed
- `POST/DELETE /articles/{slug}/favorite` — Favorite management
- `GET/POST/DELETE /articles/{slug}/comments` — Comment management
- `GET /tags` — List all tags
- `GET /health` — Service health check
- `GET /actuator/health` — Spring Actuator health

### User Service (Remaining Monolith)

| Entity | Tables | Responsibilities |
|---|---|---|
| User | `users` | Registration, login, profile updates |
| FollowRelation | `follows` | User follow/unfollow relationships |

**Endpoints owned:**
- `POST /users` — Registration
- `POST /users/login` — Login
- `GET/PUT /user` — Current user profile
- `GET /profiles/{username}` — Public profiles
- `POST/DELETE /profiles/{username}/follow` — Follow management
- `GET /internal/users/{id}` — Internal API for cross-service user lookup
- `GET /internal/users/by-username/{username}` — Internal API for cross-service user lookup by username
- `/graphql`, `/graphiql` — GraphQL API (retained in monolith)

---

## Cross-Service Communication

### Pattern: Synchronous REST

The article-service communicates with the user-service via a `UserServiceClient` (REST client using `RestTemplate`). This is used to:

1. Resolve `user_id` → user profile data (username, bio, image) for article author display
2. Validate user existence for operations requiring user context

### API Contract

**User Service Internal API:**

```
GET /internal/users/{id}
Response: { "id": "...", "email": "...", "username": "...", "bio": "...", "image": "..." }

GET /internal/users/by-username/{username}
Response: { "id": "...", "email": "...", "username": "...", "bio": "...", "image": "..." }
```

These endpoints are unauthenticated (internal network only) and added to the user-service's security config as `.requestMatchers("/internal/**").permitAll()`.

### Authentication

Both services share the same JWT secret (`jwt.secret`). The article-service validates JWT tokens independently to extract the `user_id` (subject claim) without calling the user-service. This avoids a round-trip for every authenticated request.

---

## Data Ownership

Each service owns its own database schema:

- **User Service DB:** `users`, `follows`
- **Article Service DB:** `articles`, `article_tags`, `tags`, `article_favorites`, `comments`

The article service stores `user_id` as a plain string foreign key referencing users. There is no database-level foreign key constraint between services — referential integrity is maintained at the application level.

---

## Deployment

Both services are containerized with multi-stage Docker builds and orchestrated via Docker Compose:

- `user-service` runs on port **8080**
- `article-service` runs on port **8081**
- The article-service starts only after the user-service is healthy (Docker Compose `depends_on` with health check)
- Services communicate over a shared Docker bridge network (`realworld-net`)

---

## Trade-offs & Future Considerations

1. **Data Duplication:** Article author profile data could be cached in the article-service to reduce cross-service calls. Currently, profile data is fetched on-demand.

2. **Event-Driven Communication:** For production, consider replacing synchronous REST calls with an event bus (e.g., Kafka) for user profile updates to avoid cascading failures.

3. **GraphQL:** The GraphQL API remains in the monolith. A future step could add a GraphQL gateway that federates queries across both services.

4. **Shared JWT Secret:** Both services use the same JWT secret. In production, consider a centralized auth service or asymmetric key validation.

5. **Database Migration:** Currently using SQLite for simplicity. Production deployments should use PostgreSQL or MySQL with proper connection pooling.
