# Relationship Manager — Architecture Document

> **Version:** 1.0 (Draft)
> **Date:** 2026-05-03
> **Status:** Proposed — Awaiting Review

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Context](#2-business-context)
3. [System Overview](#3-system-overview)
4. [Architecture Principles](#4-architecture-principles)
5. [Component Architecture](#5-component-architecture)
6. [AI Integration Architecture](#6-ai-integration-architecture)
7. [Security Architecture](#7-security-architecture)
8. [Data Architecture](#8-data-architecture)
9. [Integration Architecture](#9-integration-architecture)
10. [Cross-Cutting Concerns](#10-cross-cutting-concerns)

---

## 1. Executive Summary

The **Relationship Manager (RM)** platform is a conversational, AI-augmented banking application that engages customers — both anonymous and authenticated — through intelligent, multi-channel conversations. The platform captures customer financial profiles, builds risk assessments, generates wealth portfolio projections, and orchestrates periodic follow-ups across SMS, WhatsApp, Email, and Phone channels.

The system is designed as an **event-driven microservices architecture** deployed on cloud infrastructure, capable of serving **500,000 registered users** with **70,000–100,000 daily active interactions**, and elastic enough to scale beyond those thresholds.

---

## 2. Business Context

### 2.1 Problem Statement

Banks need a scalable, intelligent way to:
- Acquire new customers through personalized digital conversations
- Understand customer financial profiles and risk appetite
- Recommend suitable banking and investment products
- Maintain ongoing relationships through periodic, multi-channel follow-ups
- Automate repetitive advisory tasks while maintaining a human-like experience

### 2.2 Key Business Capabilities

| Capability | Description |
|---|---|
| **Customer Onboarding** | Conversational intake of demographics, income, investments, and retirement goals |
| **Risk Profiling** | AI-driven assessment of risk tolerance and investment suitability |
| **Product Recommendation** | Matching customer profiles to banking/investment products |
| **Wealth Projection** | Modelling portfolio growth based on customer inputs and market data |
| **Follow-Up Orchestration** | Scheduling and executing periodic reviews across chosen channels |
| **Reminder Service** | Multi-channel nudges (SMS, WhatsApp, Calendar, Email) |
| **Conversation Continuity** | Seamless continuation from previous interactions |

### 2.3 User Personas

| Persona | Description |
|---|---|
| **Anonymous Visitor** | Lands on the platform, explores without logging in; may convert to a registered user |
| **Registered Customer** | Authenticated user with a full profile, active conversations, and scheduled follow-ups |
| **Relationship Manager (Human)** | Bank staff who can review AI recommendations and intervene when needed |
| **System Admin** | Manages products, conversation templates, and system configuration |

---

## 3. System Overview

### 3.1 Architectural Style

**Event-Driven Microservices** with a **Conversational AI Core**

```mermaid
graph TB
    subgraph CLIENT["CLIENT TIER"]
        WEB["Web App<br/>(React/Next)"]
        MOB["Mobile App<br/>(React Native)"]
        CHAN["Channel Adapters<br/>(WhatsApp, SMS, Email, Phone)"]
    end

    subgraph APIGW["API GATEWAY / BFF"]
        KONG["Kong / AWS API Gateway<br/>Rate Limiting, Auth, Routing"]
    end

    subgraph CORE["CORE SERVICES"]
        CONV["Conversation Service<br/>Chat Engine, Session Mgmt, NLP"]
        PROF["Customer Profile Service<br/>Demographics, KYC, Risk Profile"]
        PROD["Product & Recommendation<br/>Catalog, AI Recommendation, Projection"]
    end

    subgraph EVENTBUS["EVENT BUS"]
        KAFKA["Kafka / Amazon EventBridge"]
    end

    subgraph ASYNC["ASYNC SERVICES"]
        FU["Follow-Up Orchestrator<br/>Scheduling, Agenda, Review"]
        NOTIF["Notification Service<br/>SMS, WhatsApp, Email, Calendar, Voice"]
        ANALYTICS["Analytics & Reporting<br/>Dashboards, Funnels, AI Monitoring"]
    end

    subgraph DATA["DATA TIER"]
        PG["PostgreSQL<br/>(Primary)"]
        REDIS["Redis<br/>(Cache & Session)"]
        S3["S3 / Blob<br/>(Documents & Media)"]
        ES["Elasticsearch<br/>(Search & Analytics)"]
    end

    WEB --> KONG
    MOB --> KONG
    CHAN --> KONG
    KONG --> CONV
    KONG --> PROF
    KONG --> PROD
    CONV --> KAFKA
    PROF --> KAFKA
    PROD --> KAFKA
    KAFKA --> FU
    KAFKA --> NOTIF
    KAFKA --> ANALYTICS
    FU --> PG
    NOTIF --> REDIS
    ANALYTICS --> ES
    CONV --> PG
    CONV --> REDIS
    PROF --> PG
    PROD --> PG
    PROD --> S3
```

### 3.2 Key Architectural Decisions

| Decision | Rationale |
|---|---|
| **Microservices over Monolith** | Independent scaling of conversation engine vs. notification services; team autonomy |
| **Event-Driven Communication** | Decoupled services; natural fit for async operations (notifications, scheduling) |
| **Conversational AI Core** | LLM-powered NLP reduces rigid dialog tree maintenance; supports natural conversation |
| **Multi-Channel Abstraction** | Single conversation model adapts to SMS, WhatsApp, Web, Voice — no channel-specific logic in core |
| **CQRS for Read/Write** | High read-to-write ratio (dashboards, analytics) benefits from separated models |
| **API Gateway** | Centralized auth, rate limiting, routing; simplifies client integration |

---

## 4. Architecture Principles

| # | Principle | Description |
|---|---|---|
| 1 | **Cloud-Native** | All services containerized (Docker), orchestrated (Kubernetes), and cloud-agnostic |
| 2 | **API-First** | All inter-service and external communication via well-defined REST/gRPC APIs |
| 3 | **Event-Driven** | Asynchronous event propagation for loose coupling and resilience |
| 4 | **Security by Design** | Zero-trust networking, encryption at rest and in transit, PII handling compliant with banking regulations |
| 5 | **Elastic Scalability** | Horizontal pod autoscaling (HPA) and cluster autoscaling for burst traffic |
| 6 | **AI-Augmented** | AI for conversation, risk profiling, recommendations, and follow-up intelligence |
| 7 | **Observability** | Distributed tracing, structured logging, and metrics for all services |
| 8 | **Resilience** | Circuit breakers, retries, bulkheads, and graceful degradation |
| 9 | **Data Privacy** | GDPR/PCI-DSS compliant; PII encrypted, consent-managed, right-to-erasure supported |
| 10 | **Progressive Engagement** | Anonymous users can interact; data persists and merges upon registration |

---

## 5. Component Architecture

### 5.1 Conversation Service

The heart of the platform. Manages all customer interactions.

**Responsibilities:**
- Manage conversation sessions (anonymous and authenticated)
- Drive the conversational flow using an AI-powered dialog engine
- Extract structured data from natural language inputs
- Maintain conversation state and context across sessions
- Hand off to human RM when confidence is low or customer requests it

**Sub-components:**

| Component | Technology | Purpose |
|---|---|---|
| Dialog Engine | LLM (GPT-4 / Claude) + LangChain | Drives conversation flow, extracts entities |
| Session Manager | Redis + PostgreSQL | Manages active sessions and persists history |
| NLP Pipeline | LLM + Custom Prompts | Intent classification, entity extraction, sentiment analysis |
| Flow Orchestrator | Custom State Machine | Manages conversation phases (greeting → data collection → profiling → recommendation) |
| Human Handoff | WebSocket + Queue | Routes to human RM when AI confidence < threshold |

### 5.2 Customer Profile Service

Manages all customer data and financial profiles.

**Responsibilities:**
- Store and manage customer demographics
- Maintain financial profile (income, investments, savings)
- Store risk profile assessment results
- Handle anonymous-to-authenticated profile merging
- Enforce data privacy and consent management

### 5.3 Product & Recommendation Service

AI-powered product matching and wealth projection.

**Responsibilities:**
- Maintain product catalog (savings accounts, FDs, mutual funds, insurance, etc.)
- Match customer risk profiles to suitable products
- Generate wealth portfolio projections using financial models
- Track product performance and update recommendations

**AI Components:**
- **Risk Assessment Model**: Classifies customers into risk categories (Conservative, Moderate, Aggressive, Very Aggressive) based on age, income, goals, and stated preferences
- **Recommendation Engine**: Collaborative filtering + rule-based engine for product suggestions
- **Wealth Projection Model**: Monte Carlo simulation for portfolio growth projections

### 5.4 Follow-Up Orchestrator

Schedules and manages periodic customer reviews.

**Responsibilities:**
- Schedule follow-ups based on customer preferences and RM availability
- Build agendas from previous conversation context and market changes
- Track action items for both RM and customer
- Trigger reminders via the Notification Service

### 5.5 Notification Service

Multi-channel communication gateway.

**Responsibilities:**
- Route notifications to the customer's preferred channel
- Manage provider integrations (Twilio for SMS/Voice, WhatsApp Business API, SendGrid for Email)
- Handle delivery receipts and retry logic
- Manage calendar integrations (Google Calendar, Outlook)

### 5.6 Analytics & Reporting Service

Business intelligence and AI model monitoring.

**Responsibilities:**
- Track conversion funnels (visitor → conversation → profile → product)
- Monitor AI model performance (accuracy, latency, hallucination rate)
- Generate RM performance dashboards
- Compliance and audit reporting

---

## 6. AI Integration Architecture

### 6.1 AI Flows Overview

```mermaid
graph TB
    subgraph AI_LAYER["AI ORCHESTRATION LAYER"]
        subgraph ROW1[" "]
            direction LR
            A1["Conversational AI Agent<br/>• Greeting<br/>• Data Capture<br/>• Contextual Responses<br/>• Sentiment Detection"]
            A2["Risk Profiling AI Model<br/>• Questionnaire Analysis<br/>• Behavioral Scoring<br/>• Category Assignment"]
            A3["Recommendation Engine<br/>• Product Match<br/>• Portfolio Build<br/>• Projection Simulation<br/>• Rebalancing Suggestions"]
        end
        subgraph ROW2[" "]
            direction LR
            A4["Follow-Up Intelligence<br/>• Optimal Time Prediction<br/>• Agenda Generation<br/>• Channel Selection"]
            A5["Summarization Agent<br/>• Conversation Summary<br/>• Action Items Extraction<br/>• Brief Builder"]
            A6["Anomaly Detection<br/>• Unusual Patterns<br/>• Fraud Signals<br/>• Compliance Violations"]
        end
    end
```

### 6.2 AI Flow Details

#### Flow 1: Conversational AI Agent
- **Purpose**: Drive natural, multi-turn conversations to collect customer data
- **Model**: LLM (GPT-4o / Claude 3.5) with banking-specific system prompts
- **Technique**: Retrieval-Augmented Generation (RAG) with product knowledge base
- **Guardrails**: Content filtering, PII redaction in logs, hallucination detection
- **Fallback**: Graceful handoff to human RM when confidence < 0.7

#### Flow 2: Risk Profiling Model
- **Purpose**: Classify customers into risk categories based on collected data
- **Model**: Gradient Boosted Trees (XGBoost) trained on historical customer data
- **Inputs**: Age group, income, investments, savings, retirement goals, stated preferences
- **Output**: Risk score (1-10) + Category (Conservative / Moderate / Aggressive / Very Aggressive)
- **Retraining**: Weekly batch retraining on new customer data

#### Flow 3: Product Recommendation Engine
- **Purpose**: Match customer profiles to suitable products
- **Approach**: Hybrid (Collaborative Filtering + Rule-Based + LLM Reasoning)
- **Rules**: Regulatory suitability rules (e.g., don't recommend high-risk products to conservative profiles)
- **Personalization**: User behavior signals (click-through, time-on-page)

#### Flow 4: Wealth Projection Model
- **Purpose**: Simulate portfolio growth over time
- **Model**: Monte Carlo simulation with configurable market scenarios
- **Inputs**: Current savings, monthly contribution capacity, risk tolerance, target retirement age/amount
- **Output**: Projected portfolio value at 25th, 50th, 75th percentile with year-by-year breakdown

#### Flow 5: Follow-Up Intelligence
- **Purpose**: Optimize follow-up timing, channel, and content
- **Model**: Reinforcement Learning agent trained on engagement data
- **Predictions**: Best time to contact, optimal channel, conversation agenda priority

#### Flow 6: Conversation Summarization Agent
- **Purpose**: Generate briefs for follow-up conversations
- **Model**: LLM with structured output
- **Output**: Previous conversation summary, action items status, agenda for next interaction

---

## 7. Security Architecture

### 7.1 Authentication & Authorization

| Layer | Mechanism |
|---|---|
| **Anonymous Sessions** | Ephemeral session tokens (JWT, short-lived) with limited scope |
| **User Authentication** | OAuth 2.0 / OpenID Connect with MFA support |
| **Service-to-Service** | mTLS + API keys within the service mesh |
| **Human RM Access** | Role-based access control (RBAC) with bank SSO integration |

### 7.2 Data Protection

| Concern | Solution |
|---|---|
| **PII Encryption** | AES-256 encryption at rest; TLS 1.3 in transit |
| **Data Masking** | Dynamic masking for non-privileged access (e.g., phone: ***-***-1234) |
| **Consent Management** | Explicit opt-in for data collection; granular consent per data type |
| **Right to Erasure** | Automated PII purge pipeline triggered by customer request |
| **Audit Trail** | Immutable audit log for all data access and modifications |
| **PCI-DSS Compliance** | No storage of card details; tokenized references only |

### 7.3 API Security

- Rate limiting per client (anonymous: 30 req/min; authenticated: 120 req/min)
- WAF (Web Application Firewall) at API Gateway level
- Input validation and sanitization at every service boundary
- CORS configured per-environment

---

## 8. Data Architecture

### 8.1 Data Stores

| Store | Technology | Purpose | Data |
|---|---|---|---|
| **Primary DB** | PostgreSQL (Aurora) | Transactional data | Customers, conversations, profiles, products |
| **Cache** | Redis Cluster | Session, cache, rate limits | Active sessions, conversation context, API cache |
| **Document Store** | S3 / Azure Blob | Documents and media | KYC documents, conversation transcripts, reports |
| **Search & Analytics** | Elasticsearch | Full-text search, analytics | Conversation logs, product search, audit trails |
| **Event Store** | Kafka (with retention) | Event sourcing | All domain events for replay and analytics |
| **ML Feature Store** | Redis / DynamoDB | Feature serving | Pre-computed features for real-time model inference |

### 8.2 Data Flow

```mermaid
flowchart TD
    A["Customer Input"] --> B["Conversation Service"]
    B --> C["NLP Pipeline"]
    C --> D["Structured Data"]
    D --> E["Customer Profile DB"]
    D --> F["Event Bus (Kafka)"]
    E --> G["Risk Profiling Model"]
    F --> H["Follow-Up Orchestrator"]
    G --> I["Recommendation Engine"]
    H --> J["Notification Service"]
    I --> K["Wealth Projection"]
    J --> L["SMS / WhatsApp / Email"]
    K --> M["Customer Dashboard"]
```

---

## 9. Integration Architecture

### 9.1 External Integrations

| Integration | Provider | Purpose | Protocol |
|---|---|---|---|
| **SMS** | Twilio | SMS notifications and OTP | REST API |
| **WhatsApp** | WhatsApp Business API (Meta) | Interactive messaging | REST + Webhook |
| **Email** | SendGrid / Amazon SES | Email notifications | REST API |
| **Voice/IVR** | Twilio Voice | Phone-based follow-ups | REST + WebSocket |
| **Calendar** | Google Calendar / Microsoft Graph | Meeting scheduling and reminders | REST (OAuth) |
| **LLM Provider** | OpenAI / Anthropic / Azure OpenAI | Conversational AI | REST API |
| **Market Data** | Bloomberg / Alpha Vantage | Real-time and historical market data | REST / WebSocket |
| **KYC/AML** | Bank's internal system | Identity verification | REST / SOAP |
| **Core Banking** | Bank's CBS | Account and product data | REST / ISO 20022 |

### 9.2 Integration Patterns

- **Synchronous**: API Gateway → Service (for real-time conversations)
- **Asynchronous**: Event Bus → Consumer (for notifications, scheduling, analytics)
- **Webhook**: External → API Gateway (for delivery receipts, WhatsApp messages)
- **Polling**: Follow-Up Orchestrator checks scheduled tasks every minute

---

## 10. Cross-Cutting Concerns

### 10.1 Observability

| Concern | Tool | Purpose |
|---|---|---|
| **Distributed Tracing** | Jaeger / AWS X-Ray | End-to-end request tracing across services |
| **Logging** | ELK Stack (Elasticsearch, Logstash, Kibana) | Centralized structured logging |
| **Metrics** | Prometheus + Grafana | Service health, latency, throughput dashboards |
| **Alerting** | PagerDuty / CloudWatch Alarms | SLA breach and error rate alerting |

### 10.2 Resilience Patterns

| Pattern | Implementation |
|---|---|
| **Circuit Breaker** | Resilience4j on all inter-service calls |
| **Retry with Backoff** | Exponential backoff for transient failures |
| **Bulkhead** | Thread pool isolation per downstream dependency |
| **Timeout** | Configurable per-call (default 5s for sync, 30s for AI calls) |
| **Dead Letter Queue** | Failed events routed to DLQ for manual inspection |
| **Graceful Degradation** | If AI is unavailable, fall back to rule-based flows |

### 10.3 CI/CD Pipeline

```mermaid
flowchart LR
    A["Code Push"] --> B["Lint/Format"]
    B --> C["Unit Tests"]
    C --> D["Integration Tests"]
    D --> E["Security Scan"]
    E --> F["Container Build"]
    F --> G["Staging Deploy"]
    G --> H["E2E Tests"]
    H --> I["Production Deploy<br/>(Blue/Green)"]
```

- **Infrastructure as Code**: Terraform for cloud resources
- **GitOps**: ArgoCD for Kubernetes deployments
- **Feature Flags**: LaunchDarkly / Flagsmith for progressive rollouts

---

*Next: See [02-high-level-design.md](./02-high-level-design.md) for detailed service decomposition and data flows.*
