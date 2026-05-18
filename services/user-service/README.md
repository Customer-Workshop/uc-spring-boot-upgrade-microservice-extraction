# User Service

Standalone Spring Boot microservice extracted from the RealWorld monolith. Handles all user registration, authentication, and profile/follow endpoints.

## Technology Stack

- Java 11
- Spring Boot 2.6.3
- MyBatis (persistence)
- SQLite + Flyway (database & migrations)
- jjwt 0.11.2 (JWT authentication)
- BCrypt (password encoding)

## Endpoints

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/users` | Register a new user | No |
| POST | `/users/login` | Login | No |
| GET | `/user` | Get current user | Yes |
| PUT | `/user` | Update current user | Yes |
| GET | `/profiles/{username}` | Get profile | No |
| POST | `/profiles/{username}/follow` | Follow a user | Yes |
| DELETE | `/profiles/{username}/follow` | Unfollow a user | Yes |

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | JWT signing secret (must match the monolith for token interoperability) | Yes |

## Running

```bash
# From the services/user-service/ directory
export JWT_SECRET="<shared-jwt-secret>"
./gradlew bootRun
```

The service starts on **port 8081**.

## Building

```bash
./gradlew build
```

## API Contract

Uses the same JSON shapes and `"Token <jwt>"` auth header format as the monolith, ensuring full interoperability. JWT tokens signed with the same shared secret are valid across both the monolith and this microservice.

### Authentication

Include the JWT token in the `Authorization` header:
```
Authorization: Token <jwt-token>
```

### Example: Register

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"user": {"email": "test@test.com", "username": "testuser", "password": "password123"}}'
```

### Example: Login

```bash
curl -X POST http://localhost:8081/users/login \
  -H "Content-Type: application/json" \
  -d '{"user": {"email": "john@example.com", "password": "password"}}'
```

## Database

Uses a standalone SQLite database (`user-service.db`) with Flyway migrations. Tables: `users`, `follows`. Seed data includes three users (johndoe, janedoe, bobsmith) with pre-hashed passwords.

## Configuration

Key properties in `application.properties`:

| Property | Value |
|----------|-------|
| `server.port` | 8081 |
| `jwt.secret` | Shared with monolith for token interoperability |
| `jwt.sessionTime` | 86400 (24 hours) |
| `image.default` | `https://static.productionready.io/images/smiley-cyrus.jpg` |
