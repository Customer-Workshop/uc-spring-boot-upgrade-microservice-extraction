# Article Service

Standalone microservice extracted from the RealWorld monolith, owning the **Articles**, **Tags**, and **Favorites** domain.

## Domain

This service handles:
- **Articles**: CRUD operations (`/articles`, `/articles/{slug}`)
- **Article Feed**: personalized feed (`/articles/feed`)
- **Favorites**: favorite/unfavorite articles (`/articles/{slug}/favorite`)
- **Tags**: list all tags (`/tags`)

## Tech Stack

- Java 11
- Spring Boot 2.6.3
- MyBatis 2.2.2
- SQLite 3.36.0.3
- Flyway (schema migrations)
- jjwt 0.11.2 (JWT authentication)
- Lombok

## Build

```bash
cd services/article-service
./gradlew build
```

## Run

```bash
cd services/article-service
./gradlew bootRun
```

The service starts on **port 8082**.

## Configuration

Configuration is in `src/main/resources/application.properties`. Key settings:
- `server.port=8082`
- SQLite database file: `article-service.db`
- JWT secret and session time (must match the monolith for transparent auth)

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/articles` | Optional | List articles (filter by tag, author, favorited) |
| POST | `/articles` | Required | Create article |
| GET | `/articles/feed` | Required | Get personalized feed |
| GET | `/articles/{slug}` | Optional | Get single article |
| PUT | `/articles/{slug}` | Required | Update article |
| DELETE | `/articles/{slug}` | Required | Delete article |
| POST | `/articles/{slug}/favorite` | Required | Favorite article |
| DELETE | `/articles/{slug}/favorite` | Required | Unfavorite article |
| GET | `/tags` | No | List all tags |

## Authentication

Uses the same JWT token format as the monolith (`Authorization: Token <jwt>`). The JWT secret must match across services for transparent proxy routing via the seam router.
