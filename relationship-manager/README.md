# Banking Relationship Manager — AI Version

AI-powered banking relationship manager platform built with Spring Boot 3 microservices architecture.

## Architecture

13 microservices communicating via REST + Kafka events:

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Spring Cloud Gateway — routing, rate limiting, CORS |
| Auth Service | 8081 | JWT authentication, registration, anonymous session conversion |
| Customer Profile | 8082 | Customer data, financial profile, risk profile, communication prefs |
| Conversation Service | 8083 | AI conversational engine (LangChain4j + GPT-4o), WebSocket chat |
| Risk Profiling | 8084 | XGBoost-based risk scoring with SHAP explanations |
| Product Catalog | 8085 | Product management, risk-category mappings |
| Recommendation | 8086 | AI-powered product matching and LLM re-ranking |
| Wealth Projection | 8087 | Monte Carlo simulation (10K scenarios) |
| Follow-Up Orchestrator | 8088 | Scheduled review management, AI agenda generation |
| Notification Service | 8089 | Multi-channel routing (SMS/WhatsApp/Email/Push via Twilio/SendGrid) |
| Analytics | 8090 | Real-time event processing, dashboard metrics |
| Admin | 8091 | Configuration management, system health |
| Reminder | 8092 | Cron-based reminder scheduling and delivery |

## Tech Stack

- **Language**: Kotlin 1.9 + Java 17
- **Framework**: Spring Boot 3.2.3
- **AI**: LangChain4j 0.28.0 + OpenAI GPT-4o
- **Database**: PostgreSQL 16 (per-service DBs)
- **Cache**: Redis 7
- **Messaging**: Apache Kafka
- **Build**: Gradle 8.x (Kotlin DSL)
- **Frontend**: Next.js 14 + React 18 + Tailwind CSS

## Quick Start

### Prerequisites
- JDK 17+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### Infrastructure
```bash
docker-compose up -d postgres redis zookeeper kafka
```

### Build & Run
```bash
./gradlew build
./gradlew :auth-service:bootRun
./gradlew :conversation-service:bootRun
# ... repeat for each service
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## AI Integration Points

1. **Conversational AI** — LLM-driven multi-phase conversation with entity extraction
2. **Risk Profiling** — XGBoost scoring + LLM-generated SHAP explanations
3. **Product Recommendation** — Rule-based matching + LLM re-ranking with rationale
4. **Wealth Projection** — Monte Carlo simulation with AI narrative generation
5. **Follow-Up Intelligence** — AI-generated review agendas based on history
6. **Summarization** — LLM conversation summaries for RM handoff

## Event-Driven Architecture

All services publish domain events to Kafka topics:
- `rm.conversation.events` — Conversation started/completed
- `rm.profile.events` — Customer profile updates
- `rm.risk.events` — Risk assessments
- `rm.recommendation.events` — Recommendation generation
- `rm.followup.events` — Follow-up scheduling
- `rm.reminder.events` — Reminder triggers
- `rm.notification.events` — Notification delivery
- `rm.product.events` — Product catalog changes

## Conversation Flow

8-phase state machine: GREETING → PERSONAL → FINANCIAL → GOALS → RISK_ASSESSMENT → RECOMMENDATION → CHANNEL_PREF → FOLLOWUP_SCHEDULE → COMPLETED

Each phase has dedicated prompts, required entity extraction, and validation rules.
