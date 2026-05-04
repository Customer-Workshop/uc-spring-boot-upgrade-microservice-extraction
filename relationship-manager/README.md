# Relationship Manager — AI-Integrated Banking Platform

Java 17 + Spring Boot 3.2.3 + Maven implementation of an AI-powered banking relationship management system.

## Architecture

13 microservices with event-driven communication via Kafka:

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Spring Cloud Gateway, routing, rate limiting |
| Auth Service | 8081 | JWT authentication, anonymous sessions |
| Customer Profile | 8082 | Customer data, financial info, preferences |
| Conversation | 8083 | AI chat engine (LangChain4j + GPT-4o), WebSocket, 8-phase state machine |
| Risk Profiling | 8084 | Weighted scoring (15 factors), SHAP explanations via LLM |
| Product Catalog | 8085 | Banking products CRUD |
| Recommendation | 8086 | Rule-based matching + LLM re-ranking |
| Wealth Projection | 8087 | Monte Carlo simulation (10K scenarios) |
| Follow-Up Orchestrator | 8088 | Scheduling, AI agenda generation |
| Notification | 8089 | Multi-channel delivery (SMS/WhatsApp/Email/Push) |
| Analytics | 8090 | Real-time metrics aggregation |
| Admin | 8091 | System configuration |
| Reminder | 8092 | Scheduled reminder processing |

Frontend: Next.js 14 (port 3000)

## Tech Stack

- **Java 17** / Spring Boot 3.2.3 / Maven
- **AI**: LangChain4j 0.28.0 (OpenAI GPT-4o, with template fallbacks)
- **Database**: PostgreSQL 16 (per-service) + Flyway migrations
- **Messaging**: Apache Kafka (Confluent 7.5.3)
- **Cache**: Redis 7
- **Frontend**: Next.js 14.1.0 / React 18 / TypeScript

## Prerequisites

- JDK 17+ (JDK 21 compatible)
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ (frontend)

## Quick Start

### Docker Compose (recommended)

```bash
# Start all services
docker-compose up -d

# Access:
# - Frontend: http://localhost:3000
# - API Gateway: http://localhost:8080
# - Admin health: http://localhost:8091/api/v1/admin/health
```

### Local Development

```bash
# 1. Start infrastructure
docker-compose up -d postgres redis kafka zookeeper

# 2. Build all modules
mvn clean install -DskipTests

# 3. Run individual services
cd auth-service && mvn spring-boot:run
cd conversation-service && mvn spring-boot:run  # requires OPENAI_API_KEY

# 4. Frontend
cd frontend && npm install && npm run dev
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key for AI features | (empty — falls back to templates) |
| `AI_MODEL` | LLM model name | `gpt-4o` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `JWT_SECRET` | JWT signing secret | dev default |
| `KAFKA_BOOTSTRAP` | Kafka bootstrap servers | `localhost:9092` |

## AI Features

All AI features gracefully degrade to rule-based fallbacks when LLM is unavailable:

- **Conversational AI**: GPT-4o powers the relationship manager dialog; falls back to template responses
- **Entity Extraction**: LLM extracts structured data from natural language; regex fallback
- **Risk Explanation**: SHAP values explained in plain English via LLM; template fallback
- **Recommendation Ranking**: LLM re-ranks products with rationale; rule-based order fallback
- **Follow-Up Agendas**: AI-generated review agendas; static template fallback

## Design Documents

See [docs/design/](../docs/design/) for architecture, HLD, LLD, and infrastructure cost estimation.
