# Comment Microservice

A standalone Spring Boot microservice extracted from the RealWorld monolith, handling all comment-related endpoints.

## Endpoints

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/articles/{slug}/comments` | Add a comment to an article | Yes |
| GET | `/articles/{slug}/comments` | Get comments for an article | No |
| DELETE | `/articles/{slug}/comments/{id}` | Delete a comment | Yes |

## Technology Stack

- Spring Boot 2.6.3
- Java 11
- MyBatis for persistence
- SQLite + Flyway for database migrations
- jjwt 0.11.2 for JWT authentication
- Lombok

## Running the Service

```bash
cd services/comment-service
./gradlew bootRun
```

The service starts on **port 8083**.

## Building

```bash
./gradlew build
```

## Database

The service uses its own SQLite database (`comment-service.db`) with Flyway migrations that create the required tables and seed data on first run.

## Authentication

Uses the same JWT secret and `"Token <jwt>"` header format as the monolith, ensuring token interoperability.

## API Response Format

**Single comment:**
```json
{
  "comment": {
    "id": "uuid",
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z",
    "body": "Comment text",
    "author": {
      "username": "johndoe",
      "bio": "Bio text",
      "image": "https://...",
      "following": false
    }
  }
}
```

**Multiple comments:**
```json
{
  "comments": [...]
}
```
