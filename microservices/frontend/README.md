# Conduit Frontend Microservice

Dockerized Next.js frontend for the Conduit (RealWorld) application.

## Build

From the **repository root** (build context must be the repo root since the Dockerfile copies from `frontend/`):

```bash
docker build -f microservices/frontend/Dockerfile -t conduit-frontend .
```

## Run

```bash
docker run -p 3000:3000 conduit-frontend
```

The frontend will be available at http://localhost:3000.

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | API gateway base URL (baked in at build time) | `/api` |
| `NODE_ENV` | Node environment | `production` |

### About `NEXT_PUBLIC_API_URL`

This variable is inlined at **build time** by Next.js (due to the `NEXT_PUBLIC_` prefix). To change it, you must rebuild the image with a different value:

```bash
docker build -f microservices/frontend/Dockerfile \
  --build-arg NEXT_PUBLIC_API_URL=http://custom-api:8080 \
  -t conduit-frontend .
```

The default value `/api` is a relative URL, which means API requests go to the same origin as the frontend. In the docker-compose architecture, nginx on port 80 serves as the API gateway and routes these requests to the backend.

## Docker Compose Architecture

In the full microservice stack, this frontend is one of several services:

```
nginx (port 80)
├── /* → frontend (port 3000)       ← this service
└── /api/* → backend (port 8080)
```

The nginx API gateway handles routing:
- All `/api/*` requests are proxied to the Spring Boot backend microservice
- All other requests (`/*`) are proxied to this Next.js frontend

### Example `docker-compose.yml` snippet

```yaml
frontend:
  build:
    context: .                              # repo root
    dockerfile: microservices/frontend/Dockerfile
  ports:
    - "3000:3000"
  environment:
    - NODE_ENV=production
```

## API Configuration

The frontend API base URL is configured via `NEXT_PUBLIC_API_URL`. See `api-config-patch.md` for details on what source change is needed to make the original `frontend/` code respect this variable.

## Tech Stack

- **Next.js 9.x** with React 16
- **SWR** for data fetching
- **Axios** for HTTP requests
- **Node 16** runtime (required by `frontend/package.json` engines field)
