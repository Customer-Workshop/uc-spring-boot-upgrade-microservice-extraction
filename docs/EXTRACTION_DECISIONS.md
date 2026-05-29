# Microservice Extraction Decisions

## Overview

This document explains the domain boundary choices and trade-offs made when extracting the **Article** bounded context from the RealWorld blogging monolith into a standalone microservice.

## Domain Boundary Analysis

### Identified Bounded Contexts

| Context | Entities | Owned Tables |
|---------|----------|-------------|
| **Article** (extracted) | Article, Tag, Comment, ArticleFavorite | `articles`, `tags`, `article_tags`, `comments`, `article_favorites` |
| **User** (remains in monolith) | User, FollowRelation | `users`, `follows` |

### Why This Boundary?

The Article bounded context was chosen based on several DDD principles:

1. **High Cohesion**: Articles, tags, comments, and favorites form a tightly coupled aggregate — comments belong to articles, tags classify articles, and favorites are article-specific. These entities share a lifecycle and are almost always queried together.

2. **Loose Coupling to User Context**: The Article entities reference users only by `user_id` (a foreign key string). There are no deep object-graph dependencies between articles and user entities — articles need user *profiles* for display, but never mutate user state.

3. **Independent Scalability**: Article read traffic (browsing, searching, feeds) is typically much higher than user management traffic. Extracting articles allows independent scaling of this read-heavy workload.

4. **Clear API Boundary**: The monolith's REST API naturally separates into article-related endpoints (`/articles/**`, `/tags`, `/articles/{slug}/comments`, `/articles/{slug}/favorite`) and user-related endpoints (`/users/**`, `/user`, `/profiles/**`).

## Cross-Service Communication

### article-service → user-service

The article-service calls the user-service via REST for:

| Endpoint | Purpose | When Used |
|----------|---------|-----------|
| `GET /api/internal/users/{id}/profile` | Fetch author profile for a single article | Article detail, create, update |
| `GET /api/internal/users/by-username/{username}` | Resolve username to user ID for filtering | List articles by author |
| `POST /api/internal/users/profiles` | Batch-fetch profiles for article lists | Article list, feed |
| `POST /api/internal/users/following-authors` | Check follow relationships for "following" flag | All article/comment responses |
| `GET /api/internal/users/{id}/followed` | Get followed users for feed | User feed |

### Shared JWT Authentication

Both services share the same JWT secret and validate tokens independently. The JWT `sub` claim contains the user ID, which the article-service uses directly without needing to call the user-service for authentication.

## Trade-offs

### Accepted Trade-offs

| Trade-off | Decision | Rationale |
|-----------|----------|-----------|
| **Network latency** | Accept additional REST calls for profile data | Profile data changes infrequently; can be cached later |
| **Eventual consistency** | Article-service stores `user_id` references that may become stale | User deletion is rare; can add event-driven cleanup later |
| **Data duplication** | Seed data duplicated in both services | Each service owns its schema; necessary for independent deployment |
| **Shared JWT secret** | Both services use the same signing key | Simplifies initial extraction; can migrate to OAuth2/OIDC later |

### What We Preserved

- **API Compatibility**: The article-service exposes the same REST API contract as the monolith for article-related endpoints, maintaining compatibility with existing frontends.
- **Authorization Logic**: Article/comment ownership checks remain in the article-service since they only need the `user_id` from the JWT token.
- **GraphQL**: GraphQL support remains in the monolith. A future step could add a GraphQL gateway that federates across both services.

### Future Improvements

1. **Caching**: Add Redis caching for user profiles in the article-service to reduce cross-service calls.
2. **Event-Driven**: Replace synchronous REST calls with an event bus (e.g., Kafka) for user profile updates and deletions.
3. **Service Discovery**: Replace hardcoded URLs with Spring Cloud Discovery (Eureka/Consul).
4. **API Gateway**: Add Spring Cloud Gateway to route traffic and handle cross-cutting concerns.
5. **Circuit Breaker**: Add Resilience4j circuit breakers to the UserServiceClient for fault tolerance.
6. **Database Migration**: Move from H2 to PostgreSQL for production use.

## Technology Choices

| Aspect | Monolith (user-service) | article-service |
|--------|------------------------|-----------------|
| Java version | 11 | 21 |
| Spring Boot | 2.6.3 | 3.4.1 |
| Persistence | MyBatis + SQLite | Spring Data JPA + H2 |
| Validation | javax.validation | jakarta.validation |
| Security | WebSecurityConfigurerAdapter | SecurityFilterChain (lambda DSL) |

The article-service uses modern Spring Boot 3 conventions as a reference for eventually upgrading the monolith.

## API Contracts

### Article Service (port 8081)

```
GET    /articles                          - List articles (public)
GET    /articles/feed                     - User feed (authenticated)
POST   /articles                          - Create article (authenticated)
GET    /articles/{slug}                   - Get article (public)
PUT    /articles/{slug}                   - Update article (authenticated, owner only)
DELETE /articles/{slug}                   - Delete article (authenticated, owner only)
POST   /articles/{slug}/comments          - Add comment (authenticated)
GET    /articles/{slug}/comments          - List comments (public)
DELETE /articles/{slug}/comments/{id}     - Delete comment (authenticated, owner only)
POST   /articles/{slug}/favorite          - Favorite article (authenticated)
DELETE /articles/{slug}/favorite          - Unfavorite article (authenticated)
GET    /tags                              - List all tags (public)
GET    /actuator/health                   - Health check
```

### User Service Internal API (port 8080)

```
GET    /api/internal/users/{id}/profile          - Get profile by user ID
GET    /api/internal/users/by-username/{username} - Get profile by username
POST   /api/internal/users/profiles              - Batch get profiles by IDs
POST   /api/internal/users/following-authors     - Check follow relationships
GET    /api/internal/users/{id}/followed         - Get followed user IDs
GET    /actuator/health                          - Health check
```

## Running the Services

### Local Development

```bash
# Start user-service (monolith) on port 8080
./gradlew bootRun

# Start article-service on port 8081
cd article-service && ./gradlew bootRun
```

### Docker Compose

```bash
docker-compose up --build
```

This starts both services with proper networking — the article-service connects to the user-service via Docker's internal DNS (`http://user-service:8080`).
