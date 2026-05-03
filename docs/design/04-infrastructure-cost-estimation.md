# Relationship Manager — Infrastructure & Cost Estimation

> **Version:** 1.0 (Draft)
> **Date:** 2026-05-03
> **Status:** Proposed — Awaiting Review

---

## Table of Contents

1. [Capacity Planning Assumptions](#1-capacity-planning-assumptions)
2. [Infrastructure Components](#2-infrastructure-components)
3. [Compute Sizing](#3-compute-sizing)
4. [Data Storage Sizing](#4-data-storage-sizing)
5. [Network & CDN](#5-network--cdn)
6. [AI/ML Infrastructure](#6-aiml-infrastructure)
7. [Third-Party Service Costs](#7-third-party-service-costs)
8. [Cost Summary](#8-cost-summary)
9. [Elastic Scaling Strategy](#9-elastic-scaling-strategy)
10. [Cost Optimization Recommendations](#10-cost-optimization-recommendations)

---

## 1. Capacity Planning Assumptions

### 1.1 User Base

| Metric | Value | Notes |
|---|---|---|
| **Total Registered Users** | 500,000 | Year 1 target |
| **Daily Active Users (Normal)** | 70,000 | Base DAU |
| **Daily Active Users (Peak)** | 100,000 | Upper threshold |
| **Elastic Max DAU** | 200,000 | Designed to handle 2× peak |
| **New Registrations/Day** | 1,000–2,000 | Growth phase |

### 1.2 Traffic Patterns

| Metric | Value | Calculation |
|---|---|---|
| **Peak Concurrent Users** | 15,000 | ~15% of peak DAU online simultaneously |
| **Conversations/Day** | 100,000 | Mix of new and follow-up |
| **Messages/Day** | 2,000,000 | Avg 20 messages per conversation |
| **API Requests/Day** | 10,000,000 | Messages + dashboard + profile + misc |
| **Peak API RPS** | 300 | 80% of traffic in 8-hour business window |
| **WebSocket Connections (Peak)** | 15,000 | Concurrent active chats |
| **Notifications/Day** | 200,000 | Reminders + confirmations + recommendations |

### 1.3 Data Growth

| Data Type | Daily Growth | Monthly Growth | Annual Growth |
|---|---|---|---|
| **Conversation Data** | ~500 MB | ~15 GB | ~180 GB |
| **Customer Profiles** | ~50 MB | ~1.5 GB | ~18 GB |
| **Notifications Log** | ~100 MB | ~3 GB | ~36 GB |
| **Analytics/Events** | ~1 GB | ~30 GB | ~360 GB |
| **Documents (KYC, Reports)** | ~200 MB | ~6 GB | ~72 GB |
| **Total** | **~1.85 GB/day** | **~55.5 GB/mo** | **~666 GB/yr** |

---

## 2. Infrastructure Components

### 2.1 AWS Infrastructure Map

```
┌──────────────────────────────────────────────────────────────────────────┐
│                            AWS Cloud (Mumbai Region - ap-south-1)        │
│                                                                          │
│  ┌── Edge ─────────────────────────────────────────────────────────────┐ │
│  │  CloudFront CDN │ WAF │ Route 53 │ ACM (TLS)                       │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌── Compute ──────────────────────────────────────────────────────────┐ │
│  │  EKS Cluster (3 AZs)                                               │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │  Node Group: General Purpose                                │   │ │
│  │  │  6× m6i.xlarge (4 vCPU, 16 GB RAM)                         │   │ │
│  │  │  → All microservices except AI inference                    │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │  Node Group: Spot Instances                                 │   │ │
│  │  │  4× m6i.large (2 vCPU, 8 GB RAM) - Spot pricing            │   │ │
│  │  │  → Analytics, batch processing, non-critical workloads      │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │  ALB (Application Load Balancer) × 2                        │   │ │
│  │  │  → External (customer-facing) + Internal (service mesh)     │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌── Data ─────────────────────────────────────────────────────────────┐ │
│  │  RDS Aurora PostgreSQL (Multi-AZ)                                   │ │
│  │  ┌────────────────────────────────────────────────────────────┐    │ │
│  │  │  Writer: db.r6g.xlarge (4 vCPU, 32 GB)                    │    │ │
│  │  │  Reader: db.r6g.large (2 vCPU, 16 GB) × 2                 │    │ │
│  │  │  Storage: 500 GB (auto-scaling)                            │    │ │
│  │  └────────────────────────────────────────────────────────────┘    │ │
│  │                                                                     │ │
│  │  ElastiCache Redis Cluster                                          │ │
│  │  ┌────────────────────────────────────────────────────────────┐    │ │
│  │  │  3× cache.r6g.large (2 vCPU, 13 GB) - Multi-AZ            │    │ │
│  │  └────────────────────────────────────────────────────────────┘    │ │
│  │                                                                     │ │
│  │  Amazon MSK (Managed Kafka)                                         │ │
│  │  ┌────────────────────────────────────────────────────────────┐    │ │
│  │  │  3× kafka.m5.large (2 vCPU, 8 GB) - 3 AZs                 │    │ │
│  │  │  Storage: 500 GB per broker                                │    │ │
│  │  └────────────────────────────────────────────────────────────┘    │ │
│  │                                                                     │ │
│  │  OpenSearch (Elasticsearch)                                         │ │
│  │  ┌────────────────────────────────────────────────────────────┐    │ │
│  │  │  3× r6g.large.search (2 vCPU, 16 GB)                      │    │ │
│  │  │  Storage: 500 GB                                           │    │ │
│  │  └────────────────────────────────────────────────────────────┘    │ │
│  │                                                                     │ │
│  │  S3 (Object Storage)                                                │ │
│  │  ┌────────────────────────────────────────────────────────────┐    │ │
│  │  │  Documents, transcripts, reports, static assets            │    │ │
│  │  │  Estimated: 100 GB Year 1, lifecycle policies to Glacier   │    │ │
│  │  └────────────────────────────────────────────────────────────┘    │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌── Observability ────────────────────────────────────────────────────┐ │
│  │  CloudWatch │ X-Ray │ Grafana (AMG) │ Prometheus (AMP)              │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌── Security ─────────────────────────────────────────────────────────┐ │
│  │  Secrets Manager │ KMS │ GuardDuty │ IAM │ VPC (3 AZ)              │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Compute Sizing

### 3.1 EKS Cluster — Node Groups

| Node Group | Instance | Count | vCPU Total | RAM Total | Purpose | Pricing |
|---|---|---|---|---|---|---|
| **General** | m6i.xlarge | 6 | 24 | 96 GB | Microservices | On-Demand |
| **Spot** | m6i.large | 4 | 8 | 32 GB | Analytics, batch | Spot (~60% off) |
| **Total Base** | — | **10** | **32** | **128 GB** | — | — |

### 3.2 Pod Resource Allocation

| Service | Min Pods | Max Pods (HPA) | CPU Request | Memory Request | Total Base CPU | Total Base Memory |
|---|---|---|---|---|---|---|
| API Gateway (Kong) | 2 | 6 | 500m | 512 MB | 1.0 | 1 GB |
| Auth Service | 2 | 4 | 250m | 512 MB | 0.5 | 1 GB |
| Conversation Service | 4 | 12 | 1000m | 1 GB | 4.0 | 4 GB |
| Customer Profile Svc | 3 | 8 | 500m | 512 MB | 1.5 | 1.5 GB |
| Risk Profiling Svc | 2 | 6 | 500m | 1 GB | 1.0 | 2 GB |
| Product Catalog Svc | 2 | 4 | 250m | 512 MB | 0.5 | 1 GB |
| Recommendation Svc | 2 | 6 | 500m | 1 GB | 1.0 | 2 GB |
| Wealth Projection Svc | 2 | 4 | 1000m | 2 GB | 2.0 | 4 GB |
| Follow-Up Orchestrator | 2 | 4 | 500m | 512 MB | 1.0 | 1 GB |
| Notification Svc | 3 | 8 | 500m | 512 MB | 1.5 | 1.5 GB |
| Reminder Svc | 2 | 4 | 250m | 256 MB | 0.5 | 0.5 GB |
| Analytics Svc | 2 | 4 | 500m | 1 GB | 1.0 | 2 GB |
| Admin Svc | 1 | 2 | 250m | 512 MB | 0.25 | 0.5 GB |
| **Istio Sidecars** | — | — | 100m/pod | 128 MB/pod | ~3.0 | ~4 GB |
| **System (kube, monitoring)** | — | — | — | — | ~2.0 | ~4 GB |
| **TOTAL BASE** | **29** | **76** | — | — | **~20.75** | **~30 GB** |

**Headroom**: 32 vCPU available vs 20.75 used = **35% headroom** at base. HPA can scale pods up to 76, triggering Cluster Autoscaler to add nodes.

### 3.3 Compute Cost Breakdown (Monthly)

| Component | Configuration | Unit Price | Monthly Cost |
|---|---|---|---|
| EKS Cluster | 1 cluster | $0.10/hr | **$73** |
| General Nodes | 6× m6i.xlarge (On-Demand) | $0.192/hr each | **$830** |
| Spot Nodes | 4× m6i.large (Spot ~$0.038/hr) | $0.038/hr each | **$110** |
| ALB | 2× ALB | ~$22/mo + LCU | **$100** |
| **Compute Total** | | | **~$1,113/mo** |

---

## 4. Data Storage Sizing

### 4.1 Database Sizing

#### Aurora PostgreSQL

| Metric | Value |
|---|---|
| **Writer Instance** | db.r6g.xlarge (4 vCPU, 32 GB RAM) |
| **Reader Instances** | 2× db.r6g.large (2 vCPU, 16 GB RAM) |
| **Storage (Year 1)** | ~250 GB (auto-scaling up to 128 TB) |
| **IOPS** | Included with Aurora (up to 200K) |
| **Connections** | ~500 (with connection pooling via PgBouncer) |

#### ElastiCache Redis

| Metric | Value |
|---|---|
| **Cluster** | 3× cache.r6g.large (13 GB each) |
| **Total Memory** | 39 GB |
| **Usage**: Sessions | ~5 GB (15K concurrent × 350 bytes avg) |
| **Usage**: Conversation Context | ~15 GB (active conversation state) |
| **Usage**: API Cache | ~5 GB |
| **Headroom** | ~14 GB (36%) |

#### Amazon MSK (Kafka)

| Metric | Value |
|---|---|
| **Brokers** | 3× kafka.m5.large |
| **Storage per Broker** | 500 GB |
| **Retention** | 7 days for most topics; 30 days for analytics |
| **Throughput** | ~50 MB/s (well within m5.large capacity) |
| **Topics** | ~10 |
| **Partitions** | 6-12 per topic |

#### OpenSearch

| Metric | Value |
|---|---|
| **Nodes** | 3× r6g.large.search |
| **Storage** | 500 GB total (EBS gp3) |
| **Indices** | Conversation logs, audit trails, analytics |
| **Retention** | Hot: 30 days, Warm: 90 days, Cold: 1 year |

### 4.2 Storage Cost Breakdown (Monthly)

| Component | Configuration | Monthly Cost |
|---|---|---|
| Aurora PostgreSQL Writer | db.r6g.xlarge | **$576** |
| Aurora PostgreSQL Readers | 2× db.r6g.large | **$461** |
| Aurora Storage | 250 GB @ $0.10/GB | **$25** |
| Aurora Backups | 250 GB | **$5** |
| ElastiCache Redis | 3× cache.r6g.large | **$474** |
| Amazon MSK | 3× kafka.m5.large + 1.5 TB storage | **$650** |
| OpenSearch | 3× r6g.large.search + 500 GB | **$540** |
| S3 | 100 GB + requests | **$5** |
| **Storage Total** | | **~$2,736/mo** |

---

## 5. Network & CDN

### 5.1 CloudFront CDN

| Metric | Value |
|---|---|
| **Requests/Month** | ~50M (static assets, API cache) |
| **Data Transfer** | ~500 GB/mo |
| **Origin** | S3 (static) + ALB (API) |
| **Edge Locations** | India-focused + global fallback |

### 5.2 Network Cost Breakdown (Monthly)

| Component | Configuration | Monthly Cost |
|---|---|---|
| CloudFront | 500 GB data + 50M requests | **$60** |
| Data Transfer (Inter-AZ) | ~200 GB | **$2** |
| Data Transfer (Internet) | ~500 GB | **$45** |
| NAT Gateway | 2× (Multi-AZ) + data | **$130** |
| Route 53 | Hosted zones + queries | **$5** |
| **Network Total** | | **~$242/mo** |

---

## 6. AI/ML Infrastructure

### 6.1 LLM API Usage

| Model Use Case | Model | Requests/Day | Avg Tokens/Request | Monthly Tokens | Monthly Cost |
|---|---|---|---|---|---|
| **Conversation AI** | GPT-4o | 200,000 | 1,500 (in+out) | 9B tokens | **$4,500** |
| **Risk Explanation** | GPT-4o-mini | 5,000 | 500 | 75M tokens | **$15** |
| **Conversation Summary** | GPT-4o-mini | 10,000 | 800 | 240M tokens | **$48** |
| **Agenda Generation** | GPT-4o-mini | 5,000 | 600 | 90M tokens | **$18** |
| **Embeddings** | text-embedding-3-small | 50,000 | 500 | 750M tokens | **$15** |
| **LLM API Total** | | | | | **~$4,596/mo** |

> **Note**: GPT-4o pricing: ~$5/1M input + $15/1M output tokens. GPT-4o-mini: ~$0.15/1M input + $0.60/1M output. These are estimates that may vary with negotiated enterprise pricing.

### 6.2 ML Model Hosting

| Component | Configuration | Monthly Cost |
|---|---|---|
| SageMaker Endpoint (Risk Model) | 1× ml.m5.large (always-on) | **$100** |
| SageMaker Training | Weekly retraining (~4 hours) | **$30** |
| Vector DB (Pinecone) | s1 pod, 100K vectors | **$70** |
| **ML Hosting Total** | | **~$200/mo** |

### 6.3 AI Cost Breakdown (Monthly)

| Component | Monthly Cost |
|---|---|
| LLM API (OpenAI/Azure) | **$4,596** |
| ML Model Hosting | **$200** |
| **AI Total** | **~$4,796/mo** |

---

## 7. Third-Party Service Costs

### 7.1 Communication Providers

| Service | Volume/Month | Unit Price | Monthly Cost |
|---|---|---|---|
| **Twilio SMS** | 200K messages | $0.0075/msg (India) | **$1,500** |
| **WhatsApp Business API** | 150K conversations | $0.0042/msg (utility) | **$630** |
| **SendGrid Email** | 500K emails | Pro plan (100K/mo included) | **$90** |
| **Twilio Voice** | 5K minutes | $0.013/min (India) | **$65** |
| **Communication Total** | | | **~$2,285/mo** |

### 7.2 Monitoring & DevOps

| Service | Configuration | Monthly Cost |
|---|---|---|
| Grafana Cloud (AMG) | 1 workspace, 5 editors | **$0** (included in AWS) |
| Prometheus (AMP) | 5M samples/mo | **$50** |
| X-Ray Tracing | 5M traces/mo | **$25** |
| CloudWatch Logs | 50 GB/mo | **$25** |
| PagerDuty | Team plan, 5 users | **$100** |
| **Monitoring Total** | | **~$200/mo** |

### 7.3 Other Services

| Service | Configuration | Monthly Cost |
|---|---|---|
| Secrets Manager | 50 secrets | **$20** |
| KMS | 10K requests/mo | **$5** |
| ECR (Container Registry) | 20 images, 50 GB | **$5** |
| GitHub Actions | Team plan (included) | **$0** |
| **Other Total** | | **~$30/mo** |

---

## 8. Cost Summary

### 8.1 Monthly Cost Breakdown

| Category | Monthly Cost | % of Total |
|---|---|---|
| **Compute (EKS + ALB)** | $1,113 | 9.7% |
| **Data Storage (Aurora + Redis + Kafka + ES + S3)** | $2,736 | 23.9% |
| **Network & CDN** | $242 | 2.1% |
| **AI/ML (LLM APIs + Model Hosting)** | $4,796 | 41.9% |
| **Communication (SMS + WhatsApp + Email + Voice)** | $2,285 | 20.0% |
| **Monitoring & DevOps** | $200 | 1.7% |
| **Other (Secrets, KMS, ECR)** | $30 | 0.3% |
| **Subtotal** | **$11,402** | |
| **Contingency (15%)** | $1,710 | |
| **TOTAL MONTHLY** | **~$13,112** | |
| **TOTAL ANNUAL** | **~$157,350** | |

### 8.2 Cost Per User Metrics

| Metric | Value |
|---|---|
| **Cost per Registered User/Month** | $0.026 |
| **Cost per DAU/Month** | $0.187 (at 70K DAU) |
| **Cost per Conversation** | $0.131 |
| **Cost per Message** | $0.0066 |

### 8.3 Year 1 Total Cost of Ownership (TCO)

| Category | Annual Cost |
|---|---|
| **Infrastructure (AWS)** | $49,454 |
| **AI/ML Services** | $57,552 |
| **Communication Services** | $27,420 |
| **Monitoring & DevOps** | $2,760 |
| **Contingency (15%)** | $20,553 |
| **TOTAL YEAR 1** | **~$157,739** |

> **Note**: This excludes development team costs, licensing, and one-time setup fees.

### 8.4 Cost at Scale (200K DAU)

If traffic doubles to 200K DAU:

| Category | Base (70-100K DAU) | Scaled (200K DAU) | Scaling Factor |
|---|---|---|---|
| **Compute** | $1,113 | $2,500 | 2.2× (HPA + new nodes) |
| **Data Storage** | $2,736 | $4,100 | 1.5× (read replicas + storage) |
| **Network** | $242 | $450 | 1.9× |
| **AI/ML** | $4,796 | $9,600 | 2.0× (linear with conversations) |
| **Communication** | $2,285 | $4,600 | 2.0× (linear with notifications) |
| **Monitoring** | $200 | $300 | 1.5× |
| **TOTAL** | **$13,112** | **$24,600** | **~1.9×** |

---

## 9. Elastic Scaling Strategy

### 9.1 Horizontal Pod Autoscaler (HPA) Configuration

```yaml
# Example HPA for Conversation Service
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: conversation-service-hpa
  namespace: rm-production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: conversation-service
  minReplicas: 4
  maxReplicas: 12
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 75
    - type: Pods
      pods:
        metric:
          name: websocket_connections
        target:
          type: AverageValue
          averageValue: "3000"    # Scale if > 3000 WS connections per pod
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
        - type: Pods
          value: 2
          periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Pods
          value: 1
          periodSeconds: 120
```

### 9.2 Cluster Autoscaler Configuration

```yaml
# Cluster Autoscaler for EKS
apiVersion: autoscaling.k8s.io/v1
kind: ClusterAutoscaler
metadata:
  name: rm-cluster-autoscaler
spec:
  nodeGroups:
    - name: general-purpose
      minSize: 4
      maxSize: 12
      instanceType: m6i.xlarge
      labels:
        workload-type: general
    - name: spot-instances
      minSize: 2
      maxSize: 8
      instanceType: m6i.large
      spotPrice: "0.05"
      labels:
        workload-type: batch
  scaleDown:
    enabled: true
    delayAfterAdd: 10m
    delayAfterDelete: 1m
    unneededTime: 5m
```

### 9.3 Database Scaling Strategy

```
AURORA POSTGRESQL:
  - Auto-scaling storage (10 GB → 128 TB)
  - Add read replicas automatically when CPU > 70%
    - Current: 1 Writer + 2 Readers
    - Max: 1 Writer + 15 Readers
  - Aurora Serverless v2 as alternative for unpredictable workloads

REDIS:
  - Cluster mode with resharding support
  - Add shards when memory > 75%
  - Current: 3 nodes, Max: 15 nodes

KAFKA:
  - Add partitions to topics (no data loss)
  - Add brokers: Current 3, Max 12
  - Storage auto-scaling on MSK
```

### 9.4 Scaling Decision Matrix

| Traffic Level | DAU | EKS Nodes | DB Readers | Redis Nodes | Kafka Brokers | Est. Monthly Cost |
|---|---|---|---|---|---|---|
| **Low** | < 50K | 6+2 | 1 | 3 | 3 | ~$9,500 |
| **Normal** | 70K | 6+4 | 2 | 3 | 3 | ~$13,100 |
| **Peak** | 100K | 8+4 | 3 | 3 | 3 | ~$16,000 |
| **High** | 150K | 10+6 | 4 | 4 | 4 | ~$20,500 |
| **Max Elastic** | 200K | 12+8 | 5 | 5 | 5 | ~$24,600 |

---

## 10. Cost Optimization Recommendations

### 10.1 Reserved Instances / Savings Plans

| Resource | Commitment | Savings |
|---|---|---|
| **EKS Nodes (General)** | 1-year Compute Savings Plan | ~30% ($250/mo saved) |
| **Aurora PostgreSQL** | 1-year Reserved Instance | ~35% ($370/mo saved) |
| **ElastiCache Redis** | 1-year Reserved Nodes | ~35% ($166/mo saved) |
| **Total Annual Savings** | | **~$9,432/year** |

### 10.2 AI Cost Optimization

| Strategy | Potential Savings | Implementation |
|---|---|---|
| **Prompt Caching** | 20-30% on LLM costs | Cache common conversation patterns |
| **Model Tiering** | 40-50% for simple queries | Use GPT-4o-mini for simple extractions, GPT-4o for complex reasoning |
| **Batch Embeddings** | 50% on embedding costs | Batch product catalog updates |
| **Fine-Tuned Smaller Model** | 60-70% long-term | Fine-tune Llama 3 for conversation flow (after 6 months of data) |
| **Response Caching** | 10-15% | Cache identical question responses |

**Estimated AI savings with optimization**: $1,500-2,000/mo (30-40%)

### 10.3 Communication Cost Optimization

| Strategy | Potential Savings |
|---|---|
| **Template-based WhatsApp** | Use utility templates (cheaper than marketing) |
| **Email over SMS** | Shift non-urgent reminders to email (90% cheaper) |
| **Batch Notifications** | Combine multiple notifications into daily digests |
| **Channel Fallback** | Push notification first (free), then SMS/WhatsApp |

### 10.4 Optimized Cost Projection

| Scenario | Monthly Cost | Annual Cost |
|---|---|---|
| **Base (no optimization)** | $13,112 | $157,350 |
| **With Reserved Instances** | $12,325 | $147,900 |
| **With AI Optimization** | $10,625 | $127,500 |
| **With Communication Optimization** | $9,825 | **$117,900** |
| **Fully Optimized** | **~$9,800** | **~$117,600** |

### 10.5 Growth Cost Projection (Years 1-3)

| Year | Users | DAU | Monthly Cost (Optimized) | Annual Cost |
|---|---|---|---|---|
| **Year 1** | 500K | 70-100K | $9,800 - $13,100 | $117,600 - $157,200 |
| **Year 2** | 1M | 140-200K | $18,000 - $24,600 | $216,000 - $295,200 |
| **Year 3** | 2M | 280-400K | $32,000 - $45,000 | $384,000 - $540,000 |

> **Note**: Years 2 and 3 assume linear cost scaling, but economies of scale and optimization should reduce the actual ratio. Reserved Instance savings increase with larger commitments.

---

## Appendix A: Region Selection Rationale

**Primary Region: ap-south-1 (Mumbai)**
- Lowest latency for India-based customer base
- All required services available
- Data residency compliance (RBI guidelines)

**DR Region: ap-south-2 (Hyderabad)**
- Secondary region for disaster recovery
- Cross-region replication for Aurora, S3, and Redis
- Adds ~15% to infrastructure cost for DR

## Appendix B: Environment Matrix

| Environment | Purpose | Scale | Monthly Cost |
|---|---|---|---|
| **Production** | Live customer-facing | Full | $13,112 |
| **Staging** | Pre-production testing | 25% of prod | ~$3,278 |
| **Development** | Developer environments | 10% of prod | ~$1,311 |
| **Total All Environments** | | | **~$17,701** |

---

*This document should be reviewed alongside the [Architecture Document](./01-architecture-document.md), [High-Level Design](./02-high-level-design.md), and [Low-Level Design](./03-low-level-design.md).*
