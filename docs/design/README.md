# Relationship Manager Platform — Design Documents

This directory contains the complete design documentation for the **Banking Relationship Manager (RM)** platform — an AI-augmented, multi-channel conversational system for customer onboarding, risk profiling, product recommendation, and ongoing relationship management.

## Documents

| # | Document | Description |
|---|---|---|
| 1 | [Architecture Document](./01-architecture-document.md) | System overview, component architecture, AI integration, security, data architecture |
| 2 | [High-Level Design (HLD)](./02-high-level-design.md) | Service decomposition, technology stack, communication patterns, data flows, deployment architecture |
| 3 | [Low-Level Design (LLD)](./03-low-level-design.md) | Database schemas, API contracts, domain model, sequence diagrams, algorithm details |
| 4 | [Infrastructure & Cost Estimation](./04-infrastructure-cost-estimation.md) | Capacity planning, AWS sizing, cost breakdown, elastic scaling strategy, optimization |
| 5 | [Sequence Diagrams](./05-sequence-diagrams.md) | 20 detailed sequence diagrams covering all use cases: onboarding, data collection, risk profiling, recommendations, follow-ups, handoff, multi-channel, error handling |

## Key Highlights

- **Microservices Architecture**: 13 services with event-driven communication via Kafka
- **AI-Powered**: Conversational AI (LLM), risk profiling (XGBoost), recommendation engine (hybrid), wealth projection (Monte Carlo)
- **Multi-Channel**: Web, Mobile, SMS, WhatsApp, Email, Phone — unified behind a channel adapter pattern
- **Scale Targets**: 500K users, 70-100K DAU, elastic to 200K+ DAU
- **Estimated Cost**: ~$13,100/mo base → ~$9,800/mo optimized (Year 1)

## Reading Order

Start with the **Architecture Document** for the big picture, then **HLD** for service-level design, **LLD** for implementation details, **Infrastructure** for deployment and costs, and **Sequence Diagrams** for detailed flow walkthroughs of all 20 use cases.
