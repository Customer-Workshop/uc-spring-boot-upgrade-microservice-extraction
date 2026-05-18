# Comment Service

A standalone Spring Boot microservice extracted from the RealWorld monolith, handling the **Comments** domain as part of the Strangler Fig decomposition pattern.

## Domain

This microservice owns:
- **REST endpoints**: `/articles/{slug}/comments` (create, list) and `/articles/{slug}/comments/{id}` (delete)
- **Core entity**: `Comment`
- **Database table**: `comments`

It includes read-only copies of `articles`, `users`, `tags`, and `follows` tables for resolving article slugs, populating author profiles, and checking follow relationships.

## Prerequisites

- **Java 11** (or later)
- **Gradle** (the Gradle wrapper is available at the repo root)

## Build

```bash
cd services/comment-service
../../gradlew build
```

## Run

```bash
cd services/comment-service
../../gradlew bootRun
```

The service starts on **port 8083** by default.

## Configuration

Key configuration in `src/main/resources/application.properties`:

| Property | Default | Description |
|---|---|---|
| `server.port` | `8083` | HTTP listen port |
| `spring.datasource.url` | `jdbc:sqlite:comment-service.db` | SQLite database file |
| `jwt.secret` | *(shared with monolith)* | JWT signing key (must match the monolith) |
| `jwt.sessionTime` | `86400` | JWT token lifetime in seconds |

## API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/articles/{slug}/comments` | Required | Create a comment on an article |
| `GET` | `/articles/{slug}/comments` | Optional | List comments for an article |
| `DELETE` | `/articles/{slug}/comments/{id}` | Required | Delete a comment (author or article owner) |

## Architecture

The service uses the same technology stack as the monolith:
- **Spring Boot 2.6.3** with Spring Security (JWT-based stateless auth)
- **MyBatis** for data access with XML mapper files
- **SQLite** as the embedded database
- **Flyway** for schema migrations
- **Lombok** for boilerplate reduction
- **joda-time** for DateTime handling
