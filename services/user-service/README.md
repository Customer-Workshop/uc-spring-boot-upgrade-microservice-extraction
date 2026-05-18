# User Service

Standalone microservice extracted from the RealWorld monolith, owning the **Users/Auth/Profiles** domain.

## Domain

This service handles:

- **User registration** — `POST /users`
- **User login** — `POST /users/login`
- **Get current user** — `GET /user`
- **Update current user** — `PUT /user`
- **Get profile** — `GET /profiles/{username}`
- **Follow user** — `POST /profiles/{username}/follow`
- **Unfollow user** — `DELETE /profiles/{username}/follow`

## Tech Stack

- Java 11
- Spring Boot 2.6.3
- MyBatis 2.2.2
- SQLite (file-based, `user-service.db`)
- Flyway for schema migrations
- JWT authentication (jjwt 0.11.2)
- BCrypt password encoding

## Prerequisites

- JDK 11+

## Build

```bash
cd services/user-service
./gradlew build
```

## Run

```bash
cd services/user-service
./gradlew bootRun
```

The service starts on **port 8081**.

## API Contract

The REST API contract is identical to the monolith's user/profile/auth endpoints, allowing the seam router to proxy requests transparently.

### Authentication

Authenticated endpoints require an `Authorization: Token <JWT>` header. The JWT secret and session time are configured in `application.properties`.

## Database

Uses an independent SQLite database file (`user-service.db`) with Flyway-managed schema containing:

- `users` — user accounts
- `follows` — follow relationships between users
