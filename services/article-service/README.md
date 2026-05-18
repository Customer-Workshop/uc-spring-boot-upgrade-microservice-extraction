# Article Microservice

A standalone Spring Boot microservice extracted from the RealWorld monolith that handles all article, tag, and favorite endpoints.

## Technology Stack

- **Java 11** with Spring Boot 2.6.3
- **MyBatis** for persistence
- **SQLite** with Flyway for database migrations
- **jjwt 0.11.2** for JWT authentication
- **Gradle** build system

## Endpoints

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/articles` | Create an article | Yes |
| GET | `/articles` | List articles (filters: tag, author, favorited, offset, limit) | No |
| GET | `/articles/feed` | Get feed (articles by followed users) | Yes |
| GET | `/articles/{slug}` | Get single article | No |
| PUT | `/articles/{slug}` | Update article | Yes (author only) |
| DELETE | `/articles/{slug}` | Delete article | Yes (author only) |
| POST | `/articles/{slug}/favorite` | Favorite an article | Yes |
| DELETE | `/articles/{slug}/favorite` | Unfavorite an article | Yes |
| GET | `/tags` | Get all tags | No |

## Running the Service

```bash
# From the services/article-service/ directory
# Set the JWT_SECRET environment variable (must match the monolith's secret for token interoperability)
export JWT_SECRET=<your-jwt-secret>
./gradlew bootRun
```

The service starts on **port 8082**.

## Building

```bash
./gradlew build
```

## Authentication

The service uses the same JWT secret and `"Token <jwt>"` header format as the monolith, so tokens are fully interoperable between services.

## Database

Uses SQLite with Flyway migrations. The database file (`article-service.db`) is created automatically on first run. Seed data matching the monolith is included via migration scripts.

Tables: `users` (read-only copy), `articles`, `tags`, `article_tags`, `article_favorites`, `follows`.
