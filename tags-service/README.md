# Tags Microservice

A standalone microservice extracted from the RealWorld monolith that owns the **Tags** bounded context.

## Overview

This service is responsible for:
- Storing and retrieving tags (`tags` table)
- Managing article-tag associations (`article_tags` table)
- Providing tag creation (upsert) and read APIs

## Endpoints

| Method | Path                 | Description                                      |
|--------|----------------------|--------------------------------------------------|
| GET    | `/tags`              | Returns all tag names: `{ "tags": ["java", ...] }` |
| POST   | `/tags`              | Find-or-create a tag by name. Request: `{ "name": "java" }`. Response: `{ "id": "...", "name": "java" }` |
| POST   | `/tags/article-tags` | Create an article-tag relationship. Request: `{ "articleId": "...", "tagId": "..." }` |

## Technology Stack

- Java 11
- Spring Boot 2.6.3
- MyBatis (mybatis-spring-boot-starter 2.2.2)
- SQLite (development) — can be swapped for any JDBC-compatible database
- Flyway for database migrations

## Running the Service

```bash
cd tags-service
./gradlew bootRun
```

The service starts on **port 8081** by default (configurable in `application.properties`).

## Running Tests

```bash
cd tags-service
./gradlew test
```

## Database

The service manages two tables:
- `tags` — `id` (PK), `name`
- `article_tags` — `article_id`, `tag_id`

Migrations are in `src/main/resources/db/migration/`.

## Relationship to the Monolith

The monolith previously contained the Tags API, query service, read service, and MyBatis mappers for tags. These have been extracted into this microservice. The monolith now communicates with this service via HTTP REST calls through `TagServiceClient`.

During the transition period, both services may point to the same database. Eventually, the Tags microservice should have its own dedicated database.

### Monolith Changes

- `TagsApi`, `TagsQueryService`, `TagReadService`, and `TagReadService.xml` were **removed** from the monolith.
- `MyBatisArticleRepository.createNew()` now delegates tag creation and article-tag association to this microservice via `TagServiceClient`.
- The GraphQL `TagDatafetcher` now uses `TagServiceClient` to fetch tags.
- The monolith's `application.properties` includes `tags.service.url` to configure the connection.
