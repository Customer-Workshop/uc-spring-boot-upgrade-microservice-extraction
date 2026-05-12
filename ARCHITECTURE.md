# Conduit Microservices Architecture

## Overview

This project decomposes the RealWorld (Conduit) Spring Boot monolith into a set of independently deployable microservices, each owning a specific bounded context.

## Service Topology

```
                          ┌──────────────┐
                          │   Client     │
                          │  (Browser)   │
                          └──────┬───────┘
                                 │
                                 │ :80
                          ┌──────▼───────┐
                          │   Gateway    │
                          │   (nginx)    │
                          └──┬──┬──┬──┬──┘
                 ┌───────────┘  │  │  └───────────┐
                 │              │  │               │
          ┌──────▼──────┐ ┌────▼──▼────┐  ┌───────▼──────┐
          │   user-     │ │  article-  │  │  frontend    │
          │   service   │ │  service   │  │  (Next.js)   │
          │   :8081     │ │  :8082     │  │  :3000       │
          └──────┬──────┘ └────┬───────┘  └──────────────┘
                 │              │
          ┌──────▼──────┐ ┌────▼───────┐
          │  comment-   │ │ favorite-  │
          │  service    │ │ service    │
          │  :8083      │ │ :8084      │
          └─────────────┘ └────────────┘
```

Each Spring Boot service has its own SQLite database. All services share a JWT secret for token validation.

## Bounded Contexts

| Service            | Port | Bounded Context                                          |
|--------------------|------|----------------------------------------------------------|
| **user-service**   | 8081 | User registration, authentication, profiles, follows     |
| **article-service**| 8082 | Articles CRUD, feed generation, tags                     |
| **comment-service**| 8083 | Article comments (create, list, delete)                  |
| **favorite-service**| 8084| Article favorites (favorite/unfavorite, counts)          |
| **frontend**       | 3000 | Next.js web application                                  |
| **gateway**        | 80   | Nginx reverse proxy, CORS, request routing               |

## API Routing Table

The nginx gateway routes requests based on URL path:

| Route Pattern                      | Target Service   | Upstream Path                      |
|------------------------------------|------------------|------------------------------------|
| `/api/users`, `/api/users/login`   | user-service     | `/users`, `/users/login`           |
| `/api/user`                        | user-service     | `/user`                            |
| `/api/profiles/**`                 | user-service     | `/profiles/**`                     |
| `/api/articles/{slug}/comments/**` | comment-service  | `/articles/{slug}/comments/**`     |
| `/api/articles/{slug}/favorite`    | favorite-service | `/articles/{slug}/favorite`        |
| `/api/articles/**`                 | article-service  | `/articles/**`                     |
| `/api/tags`                        | article-service  | `/tags`                            |
| `/**` (everything else)            | frontend         | `/**`                              |

The gateway strips the `/api` prefix when proxying to backend services.

## Running with Docker Compose

### Prerequisites
- Docker and Docker Compose installed
- Copy `.env.example` to `.env` and configure the JWT secret

### Start all services

```bash
cp .env.example .env
# Edit .env and set a strong JWT_SECRET

docker-compose up --build
```

### Access the application

- **Frontend**: http://localhost:3000 (direct) or http://localhost (via gateway)
- **API via Gateway**: http://localhost/api/...
- **Individual services** (for debugging):
  - User service: http://localhost:8081
  - Article service: http://localhost:8082
  - Comment service: http://localhost:8083
  - Favorite service: http://localhost:8084

### Stop all services

```bash
docker-compose down
```

### Stop and remove data volumes

```bash
docker-compose down -v
```

## Inter-Service Communication

Services communicate via **synchronous HTTP REST** using Docker Compose service names as hostnames:

- `http://user-service:8081` — resolve user data, validate tokens
- `http://article-service:8082` — article lookups
- `http://comment-service:8083` — comment operations
- `http://favorite-service:8084` — favorite counts

All services are on the shared `conduit-network` Docker network, which provides DNS-based service discovery.

### Authentication Flow

1. Client sends login request to gateway → routed to user-service
2. User-service validates credentials, returns JWT
3. Client includes JWT in `Authorization: Token <jwt>` header on subsequent requests
4. Gateway forwards the header to the appropriate backend service
5. Each backend service validates the JWT independently using the shared `JWT_SECRET`

### Example: Creating an Article

```
Client → POST /api/articles
  → Gateway (nginx :80)
    → article-service:8082/articles
      → (optionally) GET http://user-service:8081/users/validate (verify author)
      → Save article to SQLite
      → Return response
    ← Gateway
  ← Client
```

## Technology Stack

| Component      | Technology             | Version |
|----------------|------------------------|---------|
| Backend        | Java / Spring Boot     | 11 / 2.6.3 |
| Build Tool     | Gradle (wrapper)       | 7.4     |
| ORM            | MyBatis                | 2.2.2   |
| Database       | SQLite                 | 3.36.x  |
| Migrations     | Flyway                 | —       |
| Auth           | JWT (jjwt)             | 0.9.1   |
| Frontend       | Next.js / React        | —       |
| Gateway        | Nginx                  | 1.24    |
| Containers     | Docker / Docker Compose| 3.8     |

## Directory Structure

```
├── docker-compose.yml              # Service orchestration
├── .env.example                    # Environment variable template
├── ARCHITECTURE.md                 # This file
├── microservices/
│   ├── common/
│   │   ├── Dockerfile.spring-boot  # Shared multi-stage build for Java services
│   │   ├── build.gradle.template   # Gradle dependency template
│   │   └── application.properties.template  # Shared Spring Boot config
│   ├── gateway/
│   │   ├── Dockerfile              # Nginx gateway image
│   │   └── nginx.conf              # API routing configuration
│   ├── user-service/               # User bounded context (placeholder)
│   ├── article-service/            # Article bounded context (placeholder)
│   ├── comment-service/            # Comment bounded context (placeholder)
│   ├── favorite-service/           # Favorite bounded context (placeholder)
│   └── frontend/
│       └── Dockerfile              # Next.js production image
├── src/                            # Original monolith source
├── frontend/                       # Original monolith frontend
└── build.gradle                    # Original monolith build
```
