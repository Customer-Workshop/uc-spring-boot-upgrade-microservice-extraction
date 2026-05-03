# Relationship Manager — Sequence Diagrams

> **Version:** 1.0 (Draft)
> **Date:** 2026-05-03
> **Status:** Proposed — Awaiting Review

---

## Table of Contents

1. [UC-01: Anonymous Customer Starts a Conversation](#uc-01-anonymous-customer-starts-a-conversation)
2. [UC-02: Conversational Data Collection (Personal Details)](#uc-02-conversational-data-collection-personal-details)
3. [UC-03: Financial Profile Collection](#uc-03-financial-profile-collection)
4. [UC-04: Retirement Goal Capture](#uc-04-retirement-goal-capture)
5. [UC-05: Risk Profile Assessment](#uc-05-risk-profile-assessment)
6. [UC-06: Product Recommendation & Wealth Projection](#uc-06-product-recommendation--wealth-projection)
7. [UC-07: Communication Channel Preference Selection](#uc-07-communication-channel-preference-selection)
8. [UC-08: Follow-Up Scheduling](#uc-08-follow-up-scheduling)
9. [UC-09: Anonymous-to-Authenticated Session Conversion](#uc-09-anonymous-to-authenticated-session-conversion)
10. [UC-10: Follow-Up Reminder Delivery (Multi-Channel)](#uc-10-follow-up-reminder-delivery-multi-channel)
11. [UC-11: Follow-Up Conversation Execution](#uc-11-follow-up-conversation-execution)
12. [UC-12: Human RM Handoff](#uc-12-human-rm-handoff)
13. [UC-13: WhatsApp Inbound Follow-Up Interaction](#uc-13-whatsapp-inbound-follow-up-interaction)
14. [UC-14: Wealth Projection Recalculation](#uc-14-wealth-projection-recalculation)
15. [UC-15: Periodic Risk Profile Refresh](#uc-15-periodic-risk-profile-refresh)
16. [UC-16: Customer Dashboard Data Load](#uc-16-customer-dashboard-data-load)
17. [UC-17: Admin Product Catalog Update](#uc-17-admin-product-catalog-update)
18. [UC-18: Conversation Resumption (Paused Session)](#uc-18-conversation-resumption-paused-session)
19. [UC-19: AI Failure & Graceful Degradation](#uc-19-ai-failure--graceful-degradation)
20. [UC-20: End-to-End New Customer Journey (Complete Flow)](#uc-20-end-to-end-new-customer-journey-complete-flow)

---

## UC-01: Anonymous Customer Starts a Conversation

A new visitor lands on the platform and starts a conversation without logging in.

```
┌──────────┐      ┌──────────┐      ┌────────────┐      ┌───────────┐      ┌─────────┐
│ Customer │      │ Web App  │      │ API Gateway│      │  Conv Svc │      │  Redis  │
│ (Browser)│      │ (Next.js)│      │  (Kong)    │      │           │      │         │
└────┬─────┘      └────┬─────┘      └─────┬──────┘      └─────┬─────┘      └────┬────┘
     │                  │                  │                    │                  │
     │  Visit website   │                  │                    │                  │
     │─────────────────►│                  │                    │                  │
     │                  │                  │                    │                  │
     │  Click "Chat     │                  │                    │                  │
     │  with RM"        │                  │                    │                  │
     │─────────────────►│                  │                    │                  │
     │                  │                  │                    │                  │
     │                  │  POST /api/v1/   │                    │                  │
     │                  │  conversations   │                    │                  │
     │                  │  {channel:"WEB"} │                    │                  │
     │                  │─────────────────►│                    │                  │
     │                  │                  │                    │                  │
     │                  │                  │  Create anonymous  │                  │
     │                  │                  │  session           │                  │
     │                  │                  │───────────────────►│                  │
     │                  │                  │                    │                  │
     │                  │                  │                    │  Store session   │
     │                  │                  │                    │  context         │
     │                  │                  │                    │─────────────────►│
     │                  │                  │                    │                  │
     │                  │                  │                    │  ◄─── OK ────────│
     │                  │                  │                    │                  │
     │                  │                  │  ◄── {conv_id,     │                  │
     │                  │                  │       ws_url,      │                  │
     │                  │                  │       session_     │                  │
     │                  │  ◄── 201 Created │       token}       │                  │
     │                  │      + WS URL    │                    │                  │
     │                  │                  │                    │                  │
     │  ◄── Chat UI    │                  │                    │                  │
     │      opens +    │                  │                    │                  │
     │      WS connect │                  │                    │                  │
     │                  │                  │                    │                  │
     │                  │═══ WebSocket ════╪═══════════════════►│                  │
     │                  │    Connected     │                    │                  │
     │                  │                  │                    │                  │
     │  ◄── "Hello!    │  ◄══ WS: greeting message ════════════│                  │
     │      Welcome to │                  │                    │                  │
     │      ABC Bank.  │                  │                    │                  │
     │      May I know │                  │                    │                  │
     │      your name?"│                  │                    │                  │
     │                  │                  │                    │                  │
```

---

## UC-02: Conversational Data Collection (Personal Details)

The AI collects customer name, age group, location, phone, and email through natural conversation.

```
┌──────────┐      ┌────────────┐      ┌───────────┐      ┌───────────┐      ┌──────────┐
│ Customer │      │ API Gateway│      │  Conv Svc │      │    LLM    │      │ Profile  │
│          │      │            │      │           │      │ (GPT-4o)  │      │ Service  │
└────┬─────┘      └─────┬──────┘      └─────┬─────┘      └─────┬─────┘      └────┬─────┘
     │                   │                   │                   │                  │
     │  WS: "Hi, I'm    │                   │                   │                  │
     │  Ravi, 32 years  │                   │                   │                  │
     │  from Hyderabad" │                   │                   │                  │
     │══════════════════►│                   │                   │                  │
     │                   │══════════════════►│                   │                  │
     │                   │                   │                   │                  │
     │                   │                   │  Extract entities │                  │
     │                   │                   │  from message     │                  │
     │                   │                   │──────────────────►│                  │
     │                   │                   │                   │                  │
     │                   │                   │  ◄── Entities:    │                  │
     │                   │                   │  name="Ravi"      │                  │
     │                   │                   │  age_group="30-40"│                  │
     │                   │                   │  location=        │                  │
     │                   │                   │   "Hyderabad"     │                  │
     │                   │                   │  confidence=0.96  │                  │
     │                   │                   │                   │                  │
     │                   │                   │  Save partial     │                  │
     │                   │                   │  profile          │                  │
     │                   │                   │────────────────────────────────────►│
     │                   │                   │                   │                  │
     │                   │                   │  ◄──── 200 OK ────────────────────│
     │                   │                   │                   │                  │
     │                   │                   │  Generate next    │                  │
     │                   │                   │  question         │                  │
     │                   │                   │──────────────────►│                  │
     │                   │                   │                   │                  │
     │                   │                   │  ◄── "Nice to     │                  │
     │                   │                   │  meet you Ravi!   │                  │
     │                   │                   │  Could you share  │                  │
     │                   │                   │  your email and   │                  │
     │                   │                   │  phone number?"   │                  │
     │                   │                   │                   │                  │
     │  ◄═══ WS: AI     │◄══════════════════│                   │                  │
     │  response + phase │                   │                   │                  │
     │  progress (25%)  │                   │                   │                  │
     │                   │                   │                   │                  │
     │  WS: "Sure, my   │                   │                   │                  │
     │  email is ravi@   │                   │                   │                  │
     │  mail.com, phone  │                   │                   │                  │
     │  9876543210"      │                   │                   │                  │
     │══════════════════►│══════════════════►│                   │                  │
     │                   │                   │                   │                  │
     │                   │                   │  Extract + validate                  │
     │                   │                   │──────────────────►│                  │
     │                   │                   │                   │                  │
     │                   │                   │  ◄── email, phone │                  │
     │                   │                   │  both valid       │                  │
     │                   │                   │                   │                  │
     │                   │                   │  Update profile   │                  │
     │                   │                   │────────────────────────────────────►│
     │                   │                   │                   │                  │
     │                   │                   │  Phase transition:│                  │
     │                   │                   │  PERSONAL →       │                  │
     │                   │                   │  FINANCIAL        │                  │
     │                   │                   │                   │                  │
     │                   │                   │  Generate         │                  │
     │                   │                   │  transition msg   │                  │
     │                   │                   │──────────────────►│                  │
     │                   │                   │                   │                  │
     │                   │                   │  ◄── "Great Ravi! │                  │
     │                   │                   │  Now, tell me     │                  │
     │                   │                   │  about your       │                  │
     │                   │                   │  income..."       │                  │
     │                   │                   │                   │                  │
     │  ◄═══ WS: Phase  │◄══════════════════│                   │                  │
     │  change event +   │                   │                   │                  │
     │  AI response      │                   │                   │                  │
     │                   │                   │                   │                  │
```

---

## UC-03: Financial Profile Collection

The AI collects income source, income range, current investments, and savings.

```
┌──────────┐      ┌───────────┐      ┌───────────┐      ┌──────────┐
│ Customer │      │  Conv Svc │      │    LLM    │      │ Profile  │
│          │      │           │      │ (GPT-4o)  │      │ Service  │
└────┬─────┘      └─────┬─────┘      └─────┬─────┘      └────┬─────┘
     │                   │                   │                  │
     │  WS: "I work in  │                   │                  │
     │  IT, salaried,   │                   │                  │
     │  around 1.2L     │                   │                  │
     │  per month"      │                   │                  │
     │══════════════════►│                   │                  │
     │                   │                   │                  │
     │                   │  Extract:         │                  │
     │                   │  income_source,   │                  │
     │                   │  income_range     │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │                   │  ◄── income_source│                  │
     │                   │  = "SALARIED"     │                  │
     │                   │  income_range     │                  │
     │                   │  = "100-150K"     │                  │
     │                   │  (1.2L/mo =       │                  │
     │                   │   14.4L/yr)       │                  │
     │                   │                   │                  │
     │                   │  Save to profile  │                  │
     │                   │────────────────────────────────────►│
     │                   │                   │                  │
     │                   │  Next question    │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │  ◄═══ "That's    │  ◄── Response     │                  │
     │  great! Do you   │                   │                  │
     │  have any current│                   │                  │
     │  investments like │                   │                  │
     │  mutual funds,   │                   │                  │
     │  FDs, stocks?"   │                   │                  │
     │                   │                   │                  │
     │  WS: "Yes, I have│                   │                  │
     │  about 2L in MFs,│                   │                  │
     │  5L in FD, and   │                   │                  │
     │  1L in stocks.   │                   │                  │
     │  Savings around  │                   │                  │
     │  3L"             │                   │                  │
     │══════════════════►│                   │                  │
     │                   │                   │                  │
     │                   │  Extract          │                  │
     │                   │  structured data  │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │                   │  ◄── investments: │                  │
     │                   │  {mutual_funds:   │                  │
     │                   │   200000,         │                  │
     │                   │   fd: 500000,     │                  │
     │                   │   equity: 100000} │                  │
     │                   │  savings: 300000  │                  │
     │                   │                   │                  │
     │                   │  Confirm with     │                  │
     │                   │  customer         │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │  ◄═══ "Let me    │  ◄── Confirmation │                  │
     │  confirm: ₹2L in │  message          │                  │
     │  MFs, ₹5L in FDs,│                   │                  │
     │  ₹1L in stocks,  │                   │                  │
     │  ₹3L savings.    │                   │                  │
     │  Is that right?" │                   │                  │
     │                   │                   │                  │
     │  WS: "Yes,       │                   │                  │
     │  that's correct" │                   │                  │
     │══════════════════►│                   │                  │
     │                   │                   │                  │
     │                   │  Confirm entities │                  │
     │                   │  Update profile   │                  │
     │                   │────────────────────────────────────►│
     │                   │                   │                  │
     │                   │  Phase: FINANCIAL │                  │
     │                   │  → GOALS          │                  │
     │                   │                   │                  │
```

---

## UC-04: Retirement Goal Capture

The AI collects retirement target amount and target age based on customer's age group.

```
┌──────────┐      ┌───────────┐      ┌───────────┐      ┌──────────┐
│ Customer │      │  Conv Svc │      │    LLM    │      │ Profile  │
│          │      │           │      │ (GPT-4o)  │      │ Service  │
└────┬─────┘      └─────┬─────┘      └─────┬─────┘      └────┬─────┘
     │                   │                   │                  │
     │                   │  Context: age     │                  │
     │                   │  group=30-40,     │                  │
     │                   │  income=100-150K  │                  │
     │                   │                   │                  │
     │                   │  Generate         │                  │
     │                   │  contextual       │                  │
     │                   │  question         │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │  ◄═══ "Ravi,     │  ◄── Response     │                  │
     │  since you're in │                   │                  │
     │  your early 30s, │                   │                  │
     │  you have a great│                   │                  │
     │  runway for      │                   │                  │
     │  building wealth.│                   │                  │
     │  When would you  │                   │                  │
     │  like to retire? │                   │                  │
     │  And how much    │                   │                  │
     │  would you like  │                   │                  │
     │  to have by      │                   │                  │
     │  then?"          │                   │                  │
     │                   │                   │                  │
     │  WS: "I want to  │                   │                  │
     │  retire at 55    │                   │                  │
     │  with about 5    │                   │                  │
     │  crores"         │                   │                  │
     │══════════════════►│                   │                  │
     │                   │                   │                  │
     │                   │  Extract goals    │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │                   │  ◄── retirement   │                  │
     │                   │  _target_age=55   │                  │
     │                   │  retirement_target│                  │
     │                   │  _amount=50000000 │                  │
     │                   │  years_to_retire  │                  │
     │                   │  =20              │                  │
     │                   │                   │                  │
     │                   │  Update profile   │                  │
     │                   │────────────────────────────────────►│
     │                   │                   │                  │
     │                   │  Phase: GOALS →   │                  │
     │                   │  RISK_ASSESSMENT  │                  │
     │                   │                   │                  │
     │                   │  Generate         │                  │
     │                   │  transition +     │                  │
     │                   │  risk context msg │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │  ◄═══ "₹5 crores │  ◄── Response     │                  │
     │  by 55 — great   │                   │                  │
     │  goal! Now let me│                   │                  │
     │  understand your │                   │                  │
     │  comfort with    │                   │                  │
     │  investment      │                   │                  │
     │  risk..."        │                   │                  │
     │                   │                   │                  │
```

---

## UC-05: Risk Profile Assessment

The system computes a risk score using the ML model and presents the result to the customer.

```
┌──────────┐   ┌───────────┐   ┌───────────┐   ┌──────────┐   ┌─────────┐   ┌───────┐
│ Customer │   │  Conv Svc │   │    LLM    │   │ Profile  │   │Risk Svc │   │ Kafka │
│          │   │           │   │ (GPT-4o)  │   │ Service  │   │(XGBoost)│   │       │
└────┬─────┘   └─────┬─────┘   └─────┬─────┘   └────┬─────┘   └────┬────┘   └───┬───┘
     │               │               │               │               │            │
     │  WS: "I'm     │               │               │               │            │
     │  okay with     │               │               │               │            │
     │  moderate risk,│               │               │               │            │
     │  not too       │               │               │               │            │
     │  aggressive"   │               │               │               │            │
     │═══════════════►│               │               │               │            │
     │               │               │               │               │            │
     │               │  Extract risk │               │               │            │
     │               │  preferences  │               │               │            │
     │               │──────────────►│               │               │            │
     │               │               │               │               │            │
     │               │  ◄── stated   │               │               │            │
     │               │  preference:  │               │               │            │
     │               │  "moderate"   │               │               │            │
     │               │  score=0.5    │               │               │            │
     │               │               │               │               │            │
     │               │  Fetch full   │               │               │            │
     │               │  profile      │               │               │            │
     │               │──────────────────────────────►│               │            │
     │               │               │               │               │            │
     │               │  ◄── Full customer profile ───│               │            │
     │               │               │               │               │            │
     │               │  Request risk │               │               │            │
     │               │  assessment   │               │               │            │
     │               │──────────────────────────────────────────────►│            │
     │               │               │               │               │            │
     │               │               │               │  Feature      │            │
     │               │               │               │  engineering  │            │
     │               │               │               │  (15 features)│            │
     │               │               │               │      │        │            │
     │               │               │               │      ▼        │            │
     │               │               │               │  XGBoost      │            │
     │               │               │               │  prediction   │            │
     │               │               │               │      │        │            │
     │               │               │               │      ▼        │            │
     │               │               │               │  SHAP         │            │
     │               │               │               │  explanation  │            │
     │               │               │               │               │            │
     │               │  ◄── risk_score=6.5 ──────────────────────────│            │
     │               │      category="MODERATE"      │               │            │
     │               │      top_factors=[age,income,  │               │            │
     │               │      savings_ratio]            │               │            │
     │               │               │               │               │            │
     │               │               │               │  Save risk    │            │
     │               │               │               │  profile      │            │
     │               │               │               │──────────────►│            │
     │               │               │               │               │            │
     │               │               │               │  Emit event   │            │
     │               │               │               │───────────────────────────►│
     │               │               │               │  risk.assessed│            │
     │               │               │               │               │            │
     │               │  Generate     │               │               │            │
     │               │  explanation  │               │               │            │
     │               │──────────────►│               │               │            │
     │               │               │               │               │            │
     │               │  ◄── "Based   │               │               │            │
     │               │  on your age  │               │               │            │
     │               │  (30-40),     │               │               │            │
     │               │  stable       │               │               │            │
     │               │  income, and  │               │               │            │
     │               │  existing     │               │               │            │
     │               │  investments, │               │               │            │
     │               │  you're a     │               │               │            │
     │               │  Moderate     │               │               │            │
     │               │  investor..." │               │               │            │
     │               │               │               │               │            │
     │  ◄═══ Risk    │               │               │               │            │
     │  profile      │               │               │               │            │
     │  explanation  │               │               │               │            │
     │  + score card │               │               │               │            │
     │               │               │               │               │            │
```

---

## UC-06: Product Recommendation & Wealth Projection

The recommendation engine builds a portfolio and the wealth projection model simulates growth.

```
┌──────────┐  ┌───────────┐  ┌───────────┐  ┌──────────┐  ┌─────────┐  ┌─────────┐
│ Customer │  │  Conv Svc │  │    LLM    │  │ Recomm.  │  │ Product │  │ Wealth  │
│          │  │           │  │           │  │ Service  │  │ Catalog │  │Proj Svc │
└────┬─────┘  └─────┬─────┘  └─────┬─────┘  └────┬─────┘  └────┬────┘  └────┬────┘
     │               │               │              │              │           │
     │               │  (Triggered by risk.assessed event)         │           │
     │               │               │              │              │           │
     │               │               │  ◄── Event: risk.assessed   │           │
     │               │               │              │              │           │
     │               │               │  Fetch       │              │           │
     │               │               │  products by │              │           │
     │               │               │  risk level  │              │           │
     │               │               │  (MODERATE)  │              │           │
     │               │               │─────────────►│              │           │
     │               │               │              │              │           │
     │               │               │              │  Query       │           │
     │               │               │              │  suitable    │           │
     │               │               │              │  products    │           │
     │               │               │              │─────────────►│           │
     │               │               │              │              │           │
     │               │               │              │  ◄── 25      │           │
     │               │               │              │  matching    │           │
     │               │               │              │  products    │           │
     │               │               │              │              │           │
     │               │               │  Rule engine:│              │           │
     │               │               │  filter by   │              │           │
     │               │               │  suitability │              │           │
     │               │               │      │       │              │           │
     │               │               │      ▼       │              │           │
     │               │               │  Collaborative              │           │
     │               │               │  filtering:  │              │           │
     │               │               │  rank by     │              │           │
     │               │               │  similar     │              │           │
     │               │               │  customers   │              │           │
     │               │               │      │       │              │           │
     │               │               │      ▼       │              │           │
     │               │               │  LLM re-rank │              │           │
     │               │               │  by context  │              │           │
     │               │               │      │       │              │           │
     │               │               │      ▼       │              │           │
     │               │               │  Build       │              │           │
     │               │               │  portfolio:  │              │           │
     │               │               │  Equity 40%  │              │           │
     │               │               │  Debt 25%    │              │           │
     │               │               │  FD 15%      │              │           │
     │               │               │  Gold 10%    │              │           │
     │               │               │  NPS 10%     │              │           │
     │               │               │              │              │           │
     │               │               │  Request     │              │           │
     │               │               │  wealth      │              │           │
     │               │               │  projection  │              │           │
     │               │               │─────────────────────────────────────────►│
     │               │               │              │              │           │
     │               │               │              │              │  Monte    │
     │               │               │              │              │  Carlo    │
     │               │               │              │              │  (10K     │
     │               │               │              │              │  sims)    │
     │               │               │              │              │     │     │
     │               │               │              │              │     ▼     │
     │               │               │              │              │  Compute  │
     │               │               │              │              │  P25/P50/ │
     │               │               │              │              │  P75 per  │
     │               │               │              │              │  year     │
     │               │               │              │              │           │
     │               │               │  ◄── Portfolio + Projection ────────────│
     │               │               │              │              │           │
     │               │  ◄── Recommendation ready    │              │           │
     │               │      (callback/event)        │              │           │
     │               │               │              │              │           │
     │               │  Generate     │              │              │           │
     │               │  presentation │              │              │           │
     │               │──────────────►│              │              │           │
     │               │               │              │              │           │
     │  ◄═══ Rich    │  ◄── Rich card│              │              │           │
     │  card:        │  with portfolio              │              │           │
     │  Portfolio    │  allocation + │              │              │           │
     │  allocation   │  projection   │              │              │           │
     │  chart +      │  chart data   │              │              │           │
     │  wealth       │               │              │              │           │
     │  projection   │               │              │              │           │
     │  graph        │               │              │              │           │
     │               │               │              │              │           │
```

---

## UC-07: Communication Channel Preference Selection

Customer selects their preferred channel for follow-up communications.

```
┌──────────┐      ┌───────────┐      ┌───────────┐      ┌──────────┐
│ Customer │      │  Conv Svc │      │    LLM    │      │ Profile  │
│          │      │           │      │ (GPT-4o)  │      │ Service  │
└────┬─────┘      └─────┬─────┘      └─────┬─────┘      └────┬─────┘
     │                   │                   │                  │
     │                   │  Generate channel │                  │
     │                   │  selection prompt │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │  ◄═══ "How would │  ◄── Quick reply  │                  │
     │  you like me to  │  message with     │                  │
     │  reach you for   │  options          │                  │
     │  follow-ups?"    │                   │                  │
     │                   │                   │                  │
     │  [Quick Reply     │                   │                  │
     │   Buttons:]       │                   │                  │
     │  [SMS] [WhatsApp] │                   │                  │
     │  [Email] [Phone]  │                   │                  │
     │                   │                   │                  │
     │  WS: Click        │                   │                  │
     │  "WhatsApp"       │                   │                  │
     │══════════════════►│                   │                  │
     │                   │                   │                  │
     │                   │  Save preference  │                  │
     │                   │────────────────────────────────────►│
     │                   │                   │                  │
     │                   │                   │  Update:         │
     │                   │                   │  preferred_      │
     │                   │                   │  channel=        │
     │                   │                   │  WHATSAPP        │
     │                   │                   │  opt_in_whatsapp │
     │                   │                   │  =true           │
     │                   │                   │                  │
     │                   │  ◄── 200 OK ──────────────────────│
     │                   │                   │                  │
     │                   │  Ask for time     │                  │
     │                   │  preferences      │                  │
     │                   │──────────────────►│                  │
     │                   │                   │                  │
     │  ◄═══ "Great     │  ◄── Response     │                  │
     │  choice! What    │                   │                  │
     │  time works best │                   │                  │
     │  for follow-up   │                   │                  │
     │  messages?        │                   │                  │
     │  Weekday mornings │                   │                  │
     │  or evenings?"   │                   │                  │
     │                   │                   │                  │
     │  WS: "Weekday    │                   │                  │
     │  mornings,       │                   │                  │
     │  around 10 AM"   │                   │                  │
     │══════════════════►│                   │                  │
     │                   │                   │                  │
     │                   │  Update prefs:    │                  │
     │                   │  time=10:00,      │                  │
     │                   │  days=MON-FRI     │                  │
     │                   │────────────────────────────────────►│
     │                   │                   │                  │
     │                   │  Phase: CHANNEL   │                  │
     │                   │  → FOLLOWUP       │                  │
     │                   │                   │                  │
```

---

## UC-08: Follow-Up Scheduling

The system creates a follow-up schedule and confirms with the customer.

```
┌──────────┐   ┌───────────┐   ┌───────────┐   ┌──────────┐   ┌─────────┐   ┌───────┐
│ Customer │   │  Conv Svc │   │    LLM    │   │ FollowUp │   │Notific. │   │ Kafka │
│          │   │           │   │           │   │ Orch.    │   │ Service │   │       │
└────┬─────┘   └─────┬─────┘   └─────┬─────┘   └────┬─────┘   └────┬────┘   └───┬───┘
     │               │               │               │               │            │
     │               │  Suggest       │               │               │            │
     │               │  schedule      │               │               │            │
     │               │──────────────►│               │               │            │
     │               │               │               │               │            │
     │  ◄═══ "Would │  ◄── Response │               │               │            │
     │  you like     │               │               │               │            │
     │  monthly      │               │               │               │            │
     │  check-ins?   │               │               │               │            │
     │  I can set up │               │               │               │            │
     │  the first one│               │               │               │            │
     │  for next     │               │               │               │            │
     │  month."      │               │               │               │            │
     │               │               │               │               │            │
     │  WS: "Yes,    │               │               │               │            │
     │  monthly      │               │               │               │            │
     │  works"       │               │               │               │            │
     │═══════════════►│               │               │               │            │
     │               │               │               │               │            │
     │               │  Create       │               │               │            │
     │               │  schedule     │               │               │            │
     │               │──────────────────────────────►│               │            │
     │               │               │               │               │            │
     │               │               │  Create:      │               │            │
     │               │               │  schedule(    │               │            │
     │               │               │   freq=MONTHLY│               │            │
     │               │               │   channel=WA  │               │            │
     │               │               │   time=10:00  │               │            │
     │               │               │   first=Jun 3)│               │            │
     │               │               │               │               │            │
     │               │               │  Create first │               │            │
     │               │               │  instance     │               │            │
     │               │               │               │               │            │
     │               │  ◄── Schedule created ────────│               │            │
     │               │      next: Jun 3, 10 AM       │               │            │
     │               │               │               │               │            │
     │               │               │               │  Emit event   │            │
     │               │               │               │───────────────────────────►│
     │               │               │               │  followup.    │            │
     │               │               │               │  scheduled    │            │
     │               │               │               │               │            │
     │               │               │               │  ◄── Event consumed ───────│
     │               │               │               │               │            │
     │               │               │               │  Schedule     │            │
     │               │               │               │  confirmation │            │
     │               │               │               │  notification │            │
     │               │               │               │──────────────►│            │
     │               │               │               │               │            │
     │               │               │               │               │  Send      │
     │               │               │               │               │  WhatsApp  │
     │               │               │               │               │  confirm.  │
     │               │               │               │               │            │
     │               │  Phase: FOLLOWUP → COMPLETED  │               │            │
     │               │               │               │               │            │
     │               │  Generate     │               │               │            │
     │               │  closing msg  │               │               │            │
     │               │──────────────►│               │               │            │
     │               │               │               │               │            │
     │  ◄═══ "All    │  ◄── Response │               │               │            │
     │  set, Ravi!   │               │               │               │            │
     │  First review │               │               │               │            │
     │  on June 3 at │               │               │               │            │
     │  10 AM via    │               │               │               │            │
     │  WhatsApp.    │               │               │               │            │
     │  It was great │               │               │               │            │
     │  chatting!"   │               │               │               │            │
     │               │               │               │               │            │
     │               │  Emit event:  │               │               │            │
     │               │  conversation │               │               │            │
     │               │  .completed   │               │               │            │
     │               │───────────────────────────────────────────────────────────►│
     │               │               │               │               │            │
```

---

## UC-09: Anonymous-to-Authenticated Session Conversion

An anonymous user decides to register, and all data migrates seamlessly.

```
┌──────────┐   ┌──────────┐   ┌────────────┐   ┌───────────┐   ┌──────────┐   ┌─────────┐
│ Customer │   │ Web App  │   │ API Gateway│   │ Auth Svc  │   │ Conv Svc │   │ Profile │
│          │   │          │   │            │   │           │   │          │   │ Service │
└────┬─────┘   └────┬─────┘   └─────┬──────┘   └─────┬─────┘   └────┬─────┘   └────┬────┘
     │              │               │                 │               │              │
     │  Click       │               │                 │               │              │
     │  "Register"  │               │                 │               │              │
     │─────────────►│               │                 │               │              │
     │              │               │                 │               │              │
     │              │  POST /auth/  │                 │               │              │
     │              │  register     │                 │               │              │
     │              │  {email,pass, │                 │               │              │
     │              │  anon_session │                 │               │              │
     │              │  _id}         │                 │               │              │
     │              │──────────────►│                 │               │              │
     │              │               │                 │               │              │
     │              │               │  Register       │               │              │
     │              │               │  request        │               │              │
     │              │               │────────────────►│               │              │
     │              │               │                 │               │              │
     │              │               │                 │  Create user  │              │
     │              │               │                 │  account      │              │
     │              │               │                 │               │              │
     │              │               │                 │  Fetch anon   │              │
     │              │               │                 │  profile data │              │
     │              │               │                 │──────────────────────────────►│
     │              │               │                 │               │              │
     │              │               │                 │  ◄── Anon profile data ──────│
     │              │               │                 │  (name, age, income,         │
     │              │               │                 │   investments, goals)        │
     │              │               │                 │               │              │
     │              │               │                 │  Migrate      │              │
     │              │               │                 │  profile to   │              │
     │              │               │                 │  user account │              │
     │              │               │                 │──────────────────────────────►│
     │              │               │                 │               │              │
     │              │               │                 │  Migrate      │              │
     │              │               │                 │  conversations│              │
     │              │               │                 │──────────────►│              │
     │              │               │                 │               │              │
     │              │               │                 │               │  Update      │
     │              │               │                 │               │  conv.       │
     │              │               │                 │               │  customer_id │
     │              │               │                 │               │              │
     │              │               │                 │               │  Delete anon │
     │              │               │                 │               │  session     │
     │              │               │                 │               │              │
     │              │               │                 │  ◄── Migrated │              │
     │              │               │                 │               │              │
     │              │               │  ◄── 201 {token,│               │              │
     │              │  ◄── JWT +    │  refresh,       │               │              │
     │              │  profile      │  migrated:true} │               │              │
     │              │               │                 │               │              │
     │  ◄── Logged  │               │                 │               │              │
     │  in, chat    │               │                 │               │              │
     │  continues   │               │                 │               │              │
     │  seamlessly  │               │                 │               │              │
     │              │               │                 │               │              │
```

---

## UC-10: Follow-Up Reminder Delivery (Multi-Channel)

The reminder service triggers a notification 24 hours before a scheduled follow-up.

```
┌──────────┐   ┌─────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│ Scheduler│   │Reminder │   │FollowUp  │   │ Profile  │   │Notific.  │   │ WhatsApp │
│ (Cron)   │   │ Service │   │ Orch.    │   │ Service  │   │ Service  │   │ API(Meta)│
└────┬─────┘   └────┬────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘
     │              │              │              │               │               │
     │  Tick        │              │              │               │               │
     │  (every min) │              │              │               │               │
     │─────────────►│              │              │               │               │
     │              │              │              │               │               │
     │              │  Query       │              │               │               │
     │              │  due         │              │               │               │
     │              │  reminders   │              │               │               │
     │              │  (T-24hrs)   │              │               │               │
     │              │─────────────►│              │               │               │
     │              │              │              │               │               │
     │              │  ◄── Instance│              │               │               │
     │              │  {id, cust_  │              │               │               │
     │              │  id, date,   │              │               │               │
     │              │  channel=WA} │              │               │               │
     │              │              │              │               │               │
     │              │  Fetch       │              │               │               │
     │              │  customer    │              │               │               │
     │              │  name +      │              │               │               │
     │              │  prefs       │              │               │               │
     │              │──────────────────────────►│               │               │
     │              │              │              │               │               │
     │              │  ◄── {name:"Ravi",          │               │               │
     │              │   preferred_channel:"WA",   │               │               │
     │              │   timezone:"Asia/Kolkata"}  │               │               │
     │              │              │              │               │               │
     │              │  Get agenda  │              │               │               │
     │              │  summary     │              │               │               │
     │              │─────────────►│              │               │               │
     │              │              │              │               │               │
     │              │  ◄── Agenda  │              │               │               │
     │              │  summary     │              │               │               │
     │              │              │              │               │               │
     │              │  Send        │              │               │               │
     │              │  reminder    │              │               │               │
     │              │  notification│              │               │               │
     │              │──────────────────────────────────────────►│               │
     │              │              │              │               │               │
     │              │              │              │  Template:    │               │
     │              │              │              │  followup_    │               │
     │              │              │              │  reminder_v1  │               │
     │              │              │              │  Channel: WA  │               │
     │              │              │              │               │               │
     │              │              │              │               │  POST /v1/    │
     │              │              │              │               │  messages     │
     │              │              │              │               │  (interactive)│
     │              │              │              │               │──────────────►│
     │              │              │              │               │               │
     │              │              │              │               │  ◄── 200 OK  │
     │              │              │              │               │  msg_id=xyz   │
     │              │              │              │               │               │
     │              │              │              │               │               │
     │              │              │              │     CUSTOMER'S PHONE:         │
     │              │              │              │     ┌────────────────────┐     │
     │              │              │              │     │ 📱 WhatsApp        │     │
     │              │              │              │     │                    │     │
     │              │              │              │     │ ABC Bank RM        │     │
     │              │              │              │     │ Hi Ravi 👋         │     │
     │              │              │              │     │ Your review is on  │     │
     │              │              │              │     │ Jun 3 at 10 AM.    │     │
     │              │              │              │     │                    │     │
     │              │              │              │     │ [Confirm]          │     │
     │              │              │              │     │ [Reschedule]       │     │
     │              │              │              │     │ [Cancel]           │     │
     │              │              │              │     └────────────────────┘     │
     │              │              │              │               │               │
     │              │              │              │  Webhook:      │               │
     │              │              │              │  delivered     │               │
     │              │              │              │               │◄──────────────│
     │              │              │              │               │               │
     │              │  Update      │              │               │               │
     │              │  instance:   │              │               │               │
     │              │  REMINDER_   │              │               │               │
     │              │  SENT        │              │               │               │
     │              │─────────────►│              │               │               │
     │              │              │              │               │               │
```

---

## UC-11: Follow-Up Conversation Execution

A scheduled follow-up begins with an AI-generated agenda from previous context.

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌───────────┐   ┌───────────┐
│ Customer │   │FollowUp  │   │  Conv Svc│   │    LLM    │   │ Profile   │
│ (WhatsApp│   │ Orch.    │   │          │   │ (GPT-4o)  │   │ Service   │
│  /Web)   │   │          │   │          │   │           │   │           │
└────┬─────┘   └────┬─────┘   └────┬─────┘   └─────┬─────┘   └─────┬─────┘
     │              │              │                 │                │
     │  Customer    │              │                 │                │
     │  clicks      │              │                 │                │
     │  "Confirm"   │              │                 │                │
     │═════════════►│              │                 │                │
     │              │              │                 │                │
     │              │  Fetch       │                 │                │
     │              │  previous    │                 │                │
     │              │  conversation│                 │                │
     │              │  summary     │                 │                │
     │              │─────────────►│                 │                │
     │              │              │                 │                │
     │              │  ◄── Last    │                 │                │
     │              │  conversation│                 │                │
     │              │  transcript  │                 │                │
     │              │  + summary   │                 │                │
     │              │              │                 │                │
     │              │  Fetch       │                 │                │
     │              │  current     │                 │                │
     │              │  profile     │                 │                │
     │              │──────────────────────────────────────────────►│
     │              │              │                 │                │
     │              │  ◄── Current profile + risk + recommendations ─│
     │              │              │                 │                │
     │              │  Generate    │                 │                │
     │              │  agenda      │                 │                │
     │              │──────────────────────────────►│                │
     │              │              │                 │                │
     │              │  ◄── Agenda: │                 │                │
     │              │  1. Review NPS decision        │                │
     │              │  2. Market update              │                │
     │              │  3. Tax-saving options          │                │
     │              │  4. Rebalancing check           │                │
     │              │              │                 │                │
     │              │  Create new  │                 │                │
     │              │  conversation│                 │                │
     │              │  (follow-up  │                 │                │
     │              │  type)       │                 │                │
     │              │─────────────►│                 │                │
     │              │              │                 │                │
     │              │              │  Generate       │                │
     │              │              │  opening with   │                │
     │              │              │  context        │                │
     │              │              │────────────────►│                │
     │              │              │                 │                │
     │  ◄═══ "Hi   │              │  ◄── Response   │                │
     │  Ravi! Good │              │                 │                │
     │  to connect  │              │                 │                │
     │  again. Last │              │                 │                │
     │  time we     │              │                 │                │
     │  discussed   │              │                 │                │
     │  your        │              │                 │                │
     │  retirement  │              │                 │                │
     │  portfolio.  │              │                 │                │
     │  You were    │              │                 │                │
     │  looking into│              │                 │                │
     │  NPS. Any    │              │                 │                │
     │  updates?"   │              │                 │                │
     │              │              │                 │                │
     │  ... (conversation continues with agenda topics) ...         │
     │              │              │                 │                │
```

---

## UC-12: Human RM Handoff

When AI confidence drops below threshold or customer explicitly requests a human.

```
┌──────────┐   ┌───────────┐   ┌───────────┐   ┌──────────┐   ┌──────────┐
│ Customer │   │  Conv Svc │   │    LLM    │   │ Human RM │   │Notific.  │
│          │   │           │   │           │   │ (Staff)  │   │ Service  │
└────┬─────┘   └─────┬─────┘   └─────┬─────┘   └────┬─────┘   └────┬─────┘
     │               │               │               │               │
     │  WS: "Can I   │               │               │               │
     │  talk to a    │               │               │               │
     │  real person?" │               │               │               │
     │═══════════════►│               │               │               │
     │               │               │               │               │
     │               │  Detect       │               │               │
     │               │  handoff      │               │               │
     │               │  intent       │               │               │
     │               │──────────────►│               │               │
     │               │               │               │               │
     │               │  ◄── Intent:  │               │               │
     │               │  HUMAN_HANDOFF│               │               │
     │               │  confidence:  │               │               │
     │               │  0.99         │               │               │
     │               │               │               │               │
     │               │  Update state:│               │               │
     │               │  HANDED_OFF   │               │               │
     │               │               │               │               │
     │               │  Generate     │               │               │
     │               │  conversation │               │               │
     │               │  summary for  │               │               │
     │               │  human RM     │               │               │
     │               │──────────────►│               │               │
     │               │               │               │               │
     │               │  ◄── Summary: │               │               │
     │               │  "Ravi, 30-40,│               │               │
     │               │  salaried,    │               │               │
     │               │  100-150K,    │               │               │
     │               │  moderate risk│               │               │
     │               │  Discussed    │               │               │
     │               │  portfolio    │               │               │
     │               │  allocation.  │               │               │
     │               │  Customer     │               │               │
     │               │  wants human  │               │               │
     │               │  advisor."    │               │               │
     │               │               │               │               │
     │               │  Find available RM            │               │
     │               │  & assign     │               │               │
     │               │───────────────────────────────►│               │
     │               │               │               │               │
     │               │               │  Notify RM    │               │
     │               │               │  (in-app +    │               │
     │               │               │  dashboard)   │               │
     │               │               │───────────────────────────────►│
     │               │               │               │               │
     │               │               │               │  ◄── Notified│
     │               │               │               │               │
     │  ◄═══ "Of    │               │               │               │
     │  course, Ravi!│               │               │               │
     │  I'm          │               │               │               │
     │  connecting   │               │               │               │
     │  you with     │               │               │               │
     │  Priya, your  │               │               │               │
     │  dedicated    │               │               │               │
     │  advisor.     │               │               │               │
     │  One moment..." │               │               │               │
     │               │               │               │               │
     │               │               │               │  RM views    │
     │               │               │               │  summary +   │
     │               │               │               │  full chat   │
     │               │               │               │  history     │
     │               │               │               │              │
     │               │               │               │  RM joins WS │
     │               │◄══════════════════════════════│              │
     │               │               │               │              │
     │  ◄═══ "Hi    │               │               │              │
     │  Ravi, this  │               │               │              │
     │  is Priya.   │               │               │              │
     │  I've reviewed│               │               │              │
     │  your profile.│               │               │              │
     │  How can I    │               │               │              │
     │  help?"       │               │               │              │
     │               │               │               │              │
```

---

## UC-13: WhatsApp Inbound Follow-Up Interaction

Customer responds to a follow-up via WhatsApp, triggering an interactive conversation.

```
┌──────────┐   ┌──────────┐   ┌────────────┐   ┌──────────┐   ┌───────────┐   ┌───────────┐
│ Customer │   │ WhatsApp │   │ API Gateway│   │Notific.  │   │  Conv Svc │   │    LLM    │
│ (Phone)  │   │ API(Meta)│   │  Webhook   │   │ Service  │   │           │   │ (GPT-4o)  │
└────┬─────┘   └────┬─────┘   └─────┬──────┘   └────┬─────┘   └─────┬─────┘   └─────┬─────┘
     │              │               │                │               │               │
     │  Send WA     │               │                │               │               │
     │  message:    │               │                │               │               │
     │  "Hi, I've   │               │                │               │               │
     │  enrolled    │               │                │               │               │
     │  in NPS"     │               │                │               │               │
     │─────────────►│               │                │               │               │
     │              │               │                │               │               │
     │              │  Webhook POST │                │               │               │
     │              │  /webhooks/   │                │               │               │
     │              │  whatsapp     │                │               │               │
     │              │──────────────►│                │               │               │
     │              │               │                │               │               │
     │              │               │  Route to      │               │               │
     │              │               │  Notification  │               │               │
     │              │               │  Service       │               │               │
     │              │               │───────────────►│               │               │
     │              │               │                │               │               │
     │              │               │                │  Identify     │               │
     │              │               │                │  customer by  │               │
     │              │               │                │  phone number │               │
     │              │               │                │               │               │
     │              │               │                │  Check for    │               │
     │              │               │                │  active       │               │
     │              │               │                │  follow-up    │               │
     │              │               │                │               │               │
     │              │               │                │  Route to     │               │
     │              │               │                │  Conv Service │               │
     │              │               │                │──────────────►│               │
     │              │               │                │               │               │
     │              │               │                │               │  Load context │
     │              │               │                │               │  (last conv + │
     │              │               │                │               │  follow-up    │
     │              │               │                │               │  agenda)      │
     │              │               │                │               │               │
     │              │               │                │               │  Generate     │
     │              │               │                │               │  response     │
     │              │               │                │               │──────────────►│
     │              │               │                │               │               │
     │              │               │                │               │  ◄── "That's  │
     │              │               │                │               │  great news!  │
     │              │               │                │               │  Which NPS    │
     │              │               │                │               │  fund did you │
     │              │               │                │               │  choose?      │
     │              │               │                │               │  Auto Choice  │
     │              │               │                │               │  or Active    │
     │              │               │                │               │  Choice?"     │
     │              │               │                │               │               │
     │              │               │                │  ◄── Reply    │               │
     │              │               │                │  message      │               │
     │              │               │                │               │               │
     │              │               │                │  Send via WA  │               │
     │              │               │                │  API          │               │
     │              │  ◄── POST /v1/messages ────────│               │               │
     │              │               │                │               │               │
     │  ◄───────────│               │                │               │               │
     │  WhatsApp    │               │                │               │               │
     │  response    │               │                │               │               │
     │              │               │                │               │               │
```

---

## UC-14: Wealth Projection Recalculation

Customer updates their investments, triggering a new projection.

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌─────────┐   ┌───────┐
│ Customer │   │ Web App  │   │ Profile  │   │ Recomm.  │   │ Wealth  │   │ Kafka │
│ (Dashboard│   │          │   │ Service  │   │ Service  │   │Proj Svc │   │       │
└────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬────┘   └───┬───┘
     │              │              │              │               │            │
     │  Update      │              │              │               │            │
     │  investment  │              │              │               │            │
     │  details on  │              │              │               │            │
     │  dashboard   │              │              │               │            │
     │─────────────►│              │              │               │            │
     │              │              │              │               │            │
     │              │  PATCH       │              │               │            │
     │              │  /customers/ │              │               │            │
     │              │  {id}/profile│              │               │            │
     │              │─────────────►│              │               │            │
     │              │              │              │               │            │
     │              │              │  Update      │               │            │
     │              │              │  financial   │               │            │
     │              │              │  profile     │               │            │
     │              │              │              │               │            │
     │              │              │  Emit event  │               │            │
     │              │              │──────────────────────────────────────────►│
     │              │              │  profile.    │               │            │
     │              │              │  updated     │               │            │
     │              │              │              │               │            │
     │              │  ◄── 200 OK │              │               │            │
     │              │              │              │               │            │
     │              │              │  ◄── Event consumed ─────────────────────│
     │              │              │              │               │            │
     │              │              │              │  Check if     │            │
     │              │              │              │  rebalancing  │            │
     │              │              │              │  needed       │            │
     │              │              │              │               │            │
     │              │              │              │  Recalculate  │            │
     │              │              │              │  projection   │            │
     │              │              │              │──────────────►│            │
     │              │              │              │               │            │
     │              │              │              │               │  Run Monte│
     │              │              │              │               │  Carlo    │
     │              │              │              │               │  (10K     │
     │              │              │              │               │  sims)    │
     │              │              │              │               │            │
     │              │              │              │  ◄── Updated  │            │
     │              │              │              │  projection   │            │
     │              │              │              │               │            │
     │              │              │              │  Emit event   │            │
     │              │              │              │───────────────────────────►│
     │              │              │              │  recommendation           │
     │              │              │              │  .updated     │            │
     │              │              │              │               │            │
     │  ◄══ WS:    │  ◄── Push notification       │               │            │
     │  Dashboard   │  (new projection available)  │               │            │
     │  updated     │              │              │               │            │
     │  with new    │              │              │               │            │
     │  projection  │              │              │               │            │
     │  chart       │              │              │               │            │
     │              │              │              │               │            │
```

---

## UC-15: Periodic Risk Profile Refresh

The system periodically re-assesses customer risk profiles.

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌─────────┐   ┌───────┐
│ Scheduler│   │ Risk Svc │   │ Profile  │   │    LLM   │   │ Recomm. │   │ Kafka │
│ (Weekly) │   │ (XGBoost)│   │ Service  │   │(GPT-4o-m)│   │ Service │   │       │
└────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬────┘   └───┬───┘
     │              │              │              │               │            │
     │  Batch job:  │              │              │               │            │
     │  reassess    │              │              │               │            │
     │  expiring    │              │              │               │            │
     │  profiles    │              │              │               │            │
     │─────────────►│              │              │               │            │
     │              │              │              │               │            │
     │              │  Query       │              │               │            │
     │              │  profiles    │              │               │            │
     │              │  expiring    │              │               │            │
     │              │  within 7d   │              │               │            │
     │              │─────────────►│              │               │            │
     │              │              │              │               │            │
     │              │  ◄── List of │              │               │            │
     │              │  200 profiles│              │               │            │
     │              │              │              │               │            │
     │              │  [For each profile]:        │               │            │
     │              │              │              │               │            │
     │              │  Fetch full  │              │               │            │
     │              │  customer    │              │               │            │
     │              │  data        │              │               │            │
     │              │─────────────►│              │               │            │
     │              │              │              │               │            │
     │              │  ◄── Profile │              │               │            │
     │              │              │              │               │            │
     │              │  Feature     │              │               │            │
     │              │  engineering │              │               │            │
     │              │  + XGBoost   │              │               │            │
     │              │  prediction  │              │               │            │
     │              │              │              │               │            │
     │              │  Compare new │              │               │            │
     │              │  vs old score│              │               │            │
     │              │              │              │               │            │
     │              │  [If score changed]:         │               │            │
     │              │              │              │               │            │
     │              │  Generate    │              │               │            │
     │              │  new         │              │               │            │
     │              │  explanation │              │               │            │
     │              │─────────────────────────────►│               │            │
     │              │              │              │               │            │
     │              │  ◄── Updated │              │               │            │
     │              │  explanation │              │               │            │
     │              │              │              │               │            │
     │              │  Save new    │              │               │            │
     │              │  risk profile│              │               │            │
     │              │  (is_current │              │               │            │
     │              │  = true)     │              │               │            │
     │              │─────────────►│              │               │            │
     │              │              │              │               │            │
     │              │  Emit event  │              │               │            │
     │              │──────────────────────────────────────────────────────────►│
     │              │  risk.       │              │               │            │
     │              │  reassessed  │              │               │            │
     │              │              │              │               │            │
     │              │              │              │  ◄── Event ────────────────│
     │              │              │              │               │            │
     │              │              │              │  Trigger      │            │
     │              │              │              │  recommendation│           │
     │              │              │              │  update if     │           │
     │              │              │              │  category      │           │
     │              │              │              │  changed       │           │
     │              │              │              │               │            │
```

---

## UC-16: Customer Dashboard Data Load

Customer views their dashboard with portfolio, projection, and upcoming follow-ups.

```
┌──────────┐   ┌──────────┐   ┌────────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│ Customer │   │ Web App  │   │ API Gateway│   │ Profile  │   │ Recomm.  │   │ FollowUp │
│ (Browser)│   │ (Next.js)│   │ (GraphQL)  │   │ Service  │   │ Service  │   │ Orch.    │
└────┬─────┘   └────┬─────┘   └─────┬──────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘
     │              │               │                │               │               │
     │  Navigate    │               │                │               │               │
     │  to Dashboard│               │                │               │               │
     │─────────────►│               │                │               │               │
     │              │               │                │               │               │
     │              │  GraphQL      │                │               │               │
     │              │  query {      │                │               │               │
     │              │   dashboard { │                │               │               │
     │              │    profile    │                │               │               │
     │              │    riskProfile│                │               │               │
     │              │    portfolio  │                │               │               │
     │              │    projection │                │               │               │
     │              │    nextFollowUp                │               │               │
     │              │   }           │                │               │               │
     │              │  }            │                │               │               │
     │              │──────────────►│                │               │               │
     │              │               │                │               │               │
     │              │               │  Parallel      │               │               │
     │              │               │  resolution:   │               │               │
     │              │               │                │               │               │
     │              │               │── getProfile ──►│               │               │
     │              │               │── getRisk ─────►│               │               │
     │              │               │── getPortfolio ────────────────►│               │
     │              │               │── getProjection ───────────────►│               │
     │              │               │── getFollowUp ─────────────────────────────────►│
     │              │               │                │               │               │
     │              │               │  ◄── Profile ──│               │               │
     │              │               │  ◄── Risk ─────│               │               │
     │              │               │  ◄── Portfolio ────────────────│               │
     │              │               │  ◄── Projection ──────────────│               │
     │              │               │  ◄── Next follow-up ──────────────────────────│
     │              │               │                │               │               │
     │              │  ◄── Unified  │                │               │               │
     │              │  GraphQL      │                │               │               │
     │              │  response     │                │               │               │
     │              │               │                │               │               │
     │  ◄── Dashboard renders:     │                │               │               │
     │  - Profile summary          │                │               │               │
     │  - Risk score gauge         │                │               │               │
     │  - Portfolio pie chart      │                │               │               │
     │  - Wealth projection        │                │               │               │
     │    line chart               │                │               │               │
     │  - Next follow-up card      │                │               │               │
     │  - Action items list        │                │               │               │
     │              │               │                │               │               │
```

---

## UC-17: Admin Product Catalog Update

Bank admin adds or updates products in the catalog.

```
┌──────────┐   ┌──────────┐   ┌────────────┐   ┌──────────┐   ┌─────────┐   ┌───────┐
│ Admin    │   │ Admin UI │   │ API Gateway│   │ Product  │   │ Vector  │   │ Kafka │
│ (Staff)  │   │          │   │            │   │ Catalog  │   │ DB      │   │       │
└────┬─────┘   └────┬─────┘   └─────┬──────┘   └────┬─────┘   └────┬────┘   └───┬───┘
     │              │               │                │               │            │
     │  Add new     │               │                │               │            │
     │  product:    │               │                │               │            │
     │  "Green Bond │               │                │               │            │
     │  Fund"       │               │                │               │            │
     │─────────────►│               │                │               │            │
     │              │               │                │               │            │
     │              │  POST /admin/ │                │               │            │
     │              │  products     │                │               │            │
     │              │  {name, cat,  │                │               │            │
     │              │  returns,     │                │               │            │
     │              │  risk_level}  │                │               │            │
     │              │──────────────►│                │               │            │
     │              │               │                │               │            │
     │              │               │  Auth: admin   │               │            │
     │              │               │  role check    │               │            │
     │              │               │                │               │            │
     │              │               │  Create        │               │            │
     │              │               │  product       │               │            │
     │              │               │───────────────►│               │            │
     │              │               │                │               │            │
     │              │               │                │  Save to DB   │            │
     │              │               │                │               │            │
     │              │               │                │  Generate     │            │
     │              │               │                │  embedding    │            │
     │              │               │                │  for RAG      │            │
     │              │               │                │──────────────►│            │
     │              │               │                │               │            │
     │              │               │                │  ◄── Embedded │            │
     │              │               │                │               │            │
     │              │               │                │  Emit event   │            │
     │              │               │                │───────────────────────────►│
     │              │               │                │  product.     │            │
     │              │               │                │  added        │            │
     │              │               │                │               │            │
     │              │  ◄── 201     │  ◄── Created   │               │            │
     │  ◄── Success │  Created     │                │               │            │
     │              │               │                │               │            │
```

---

## UC-18: Conversation Resumption (Paused Session)

Customer returns to a previously paused conversation.

```
┌──────────┐   ┌──────────┐   ┌───────────┐   ┌───────────┐   ┌─────────┐
│ Customer │   │ Web App  │   │  Conv Svc │   │    LLM    │   │  Redis  │
│          │   │          │   │           │   │ (GPT-4o)  │   │         │
└────┬─────┘   └────┬─────┘   └─────┬─────┘   └─────┬─────┘   └────┬────┘
     │              │               │                │               │
     │  Return to   │               │                │               │
     │  website     │               │                │               │
     │  (has session│               │                │               │
     │  cookie)     │               │                │               │
     │─────────────►│               │                │               │
     │              │               │                │               │
     │              │  GET /api/v1/ │                │               │
     │              │  conversations│                │               │
     │              │  ?status=     │                │               │
     │              │  PAUSED       │                │               │
     │              │──────────────►│                │               │
     │              │               │                │               │
     │              │               │  Check Redis   │               │
     │              │               │  for active    │               │
     │              │               │  session        │               │
     │              │               │───────────────────────────────►│
     │              │               │                │               │
     │              │               │  ◄── Session found:            │
     │              │               │  conv_id, phase=FINANCIAL,     │
     │              │               │  context={name:"Ravi",...}     │
     │              │               │                │               │
     │              │  ◄── Paused  │                │               │
     │              │  conversation │                │               │
     │              │  found        │                │               │
     │              │               │                │               │
     │  ◄── "Resume │               │                │               │
     │  conversation│               │                │               │
     │  ?" prompt   │               │                │               │
     │              │               │                │               │
     │  Click       │               │                │               │
     │  "Resume"    │               │                │               │
     │─────────────►│               │                │               │
     │              │               │                │               │
     │              │  POST /api/v1│                │               │
     │              │  /conversations               │               │
     │              │  /{id}/resume │                │               │
     │              │──────────────►│                │               │
     │              │               │                │               │
     │              │               │  Update status:│               │
     │              │               │  PAUSED →      │               │
     │              │               │  ACTIVE        │               │
     │              │               │                │               │
     │              │               │  Generate      │               │
     │              │               │  resumption    │               │
     │              │               │  message with  │               │
     │              │               │  context       │               │
     │              │               │───────────────►│               │
     │              │               │                │               │
     │              │               │  ◄── "Welcome  │               │
     │              │               │  back, Ravi!   │               │
     │              │               │  We were       │               │
     │              │               │  discussing    │               │
     │              │               │  your income   │               │
     │              │               │  and           │               │
     │              │               │  investments.  │               │
     │              │               │  Shall we      │               │
     │              │               │  continue?"    │               │
     │              │               │                │               │
     │  ◄═══ Resume │◄═════════════│                │               │
     │  message +   │               │                │               │
     │  chat history│               │                │               │
     │              │               │                │               │
```

---

## UC-19: AI Failure & Graceful Degradation

When the LLM fails, the system falls back to rule-based responses.

```
┌──────────┐   ┌───────────┐   ┌───────────┐   ┌──────────┐   ┌──────────┐
│ Customer │   │  Conv Svc │   │    LLM    │   │ Fallback │   │Monitoring│
│          │   │           │   │ (GPT-4o)  │   │ Engine   │   │(Alerting)│
└────┬─────┘   └─────┬─────┘   └─────┬─────┘   └────┬─────┘   └────┬─────┘
     │               │               │               │               │
     │  WS: "What    │               │               │               │
     │  funds do you │               │               │               │
     │  recommend?"  │               │               │               │
     │═══════════════►│               │               │               │
     │               │               │               │               │
     │               │  Call LLM     │               │               │
     │               │──────────────►│               │               │
     │               │               │               │               │
     │               │  ◄── TIMEOUT  │               │               │
     │               │  (10s)        │               │               │
     │               │               │               │               │
     │               │  Retry (1/1)  │               │               │
     │               │──────────────►│               │               │
     │               │               │               │               │
     │               │  ◄── 503     │               │               │
     │               │  Service      │               │               │
     │               │  Unavailable  │               │               │
     │               │               │               │               │
     │               │  Circuit      │               │               │
     │               │  breaker OPEN │               │               │
     │               │               │               │               │
     │               │  Log error +  │               │               │
     │               │  alert        │               │               │
     │               │──────────────────────────────────────────────►│
     │               │               │               │               │
     │               │  Fallback to  │               │               │
     │               │  rule engine  │               │               │
     │               │──────────────────────────────►│               │
     │               │               │               │               │
     │               │               │  Determine    │               │
     │               │               │  phase +      │               │
     │               │               │  context      │               │
     │               │               │               │               │
     │               │               │  Generate     │               │
     │               │               │  template     │               │
     │               │               │  response     │               │
     │               │               │               │               │
     │               │  ◄── "I'd love│               │               │
     │               │  to discuss   │               │               │
     │               │  investment   │               │               │
     │               │  options with │               │               │
     │               │  you. Let me  │               │               │
     │               │  first       │               │               │
     │               │  understand  │               │               │
     │               │  your risk   │               │               │
     │               │  comfort.    │               │               │
     │               │  On a scale  │               │               │
     │               │  of 1-10,    │               │               │
     │               │  how          │               │               │
     │               │  comfortable │               │               │
     │               │  are you with│               │               │
     │               │  investment  │               │               │
     │               │  risk?"      │               │               │
     │               │               │               │               │
     │  ◄═══ Fallback│               │               │               │
     │  response     │               │               │               │
     │  (still       │               │               │               │
     │  natural,     │               │               │               │
     │  template-    │               │               │               │
     │  based)       │               │               │               │
     │               │               │               │               │
     │               │  [5 min later, circuit breaker half-open]     │
     │               │               │               │               │
     │               │  Probe LLM   │               │               │
     │               │──────────────►│               │               │
     │               │               │               │               │
     │               │  ◄── 200 OK  │               │               │
     │               │  (recovered)  │               │               │
     │               │               │               │               │
     │               │  Circuit      │               │               │
     │               │  breaker      │               │               │
     │               │  CLOSED       │               │               │
     │               │               │               │               │
     │               │  Resume LLM   │               │               │
     │               │  responses    │               │               │
     │               │               │               │               │
```

---

## UC-20: End-to-End New Customer Journey (Complete Flow)

Complete journey from anonymous visit to scheduled follow-up.

```
PHASE 1: ANONYMOUS ENTRY
═══════════════════════════════════════════════════════════════
│ Customer visits website → Chat widget opens
│ → Anonymous session created (Redis + PostgreSQL)
│ → WS connection established
│ → AI greeting: "Hello! Welcome to ABC Bank..."
│
PHASE 2: PERSONAL DATA COLLECTION
═══════════════════════════════════════════════════════════════
│ Customer: "I'm Ravi, 32, from Hyderabad"
│ → LLM extracts: name=Ravi, age_group=30-40, location=Hyderabad
│ → Partial profile saved
│ → AI: "Could you share your email and phone?"
│ Customer: "ravi@mail.com, 9876543210"
│ → Profile updated, phase → FINANCIAL
│
PHASE 3: FINANCIAL PROFILE
═══════════════════════════════════════════════════════════════
│ AI: "What do you do for a living?"
│ Customer: "Salaried, IT, about 1.2L/month"
│ → income_source=SALARIED, income_range=100-150K
│ AI: "Any current investments?"
│ Customer: "2L MFs, 5L FD, 1L stocks, 3L savings"
│ → investments + savings saved
│ → AI confirms data, phase → GOALS
│
PHASE 4: RETIREMENT GOALS
═══════════════════════════════════════════════════════════════
│ AI: "When would you like to retire and with how much?"
│ Customer: "55, about 5 crores"
│ → retirement_age=55, target=50000000
│ → Phase → RISK_ASSESSMENT
│
PHASE 5: RISK ASSESSMENT
═══════════════════════════════════════════════════════════════
│ AI: "How comfortable are you with investment risk?"
│ Customer: "Moderate, not too aggressive"
│ → Full profile sent to Risk Service
│ → XGBoost: 15 features → score=6.5 (MODERATE)
│ → SHAP: top factors = [age, income stability, savings ratio]
│ → LLM generates explanation
│ → Event: risk.assessed → Kafka
│ → AI presents risk profile card to customer
│ → Phase → RECOMMENDATION
│
PHASE 6: PRODUCT RECOMMENDATION
═══════════════════════════════════════════════════════════════
│ risk.assessed event → Recommendation Service
│ → Rule engine: filter by suitability
│ → Collaborative filtering: rank by similar customers
│ → LLM re-rank: context-aware ordering
│ → Portfolio: Equity 40%, Debt 25%, FD 15%, Gold 10%, NPS 10%
│ → Wealth Projection Service: Monte Carlo (10K sims)
│ → Projection: P50 = ₹2.8Cr in 20 years
│ → Rich card sent to customer (pie chart + line chart)
│ → Phase → CHANNEL_PREF
│
PHASE 7: CHANNEL PREFERENCE
═══════════════════════════════════════════════════════════════
│ AI: "How would you like me to follow up?"
│ [SMS] [WhatsApp] [Email] [Phone]
│ Customer clicks "WhatsApp"
│ AI: "What time works best?"
│ Customer: "Weekday mornings, 10 AM"
│ → Preferences saved
│ → Phase → FOLLOWUP_SCHEDULE
│
PHASE 8: FOLLOW-UP SCHEDULING
═══════════════════════════════════════════════════════════════
│ AI: "Monthly check-ins work?"
│ Customer: "Yes"
│ → Schedule created: MONTHLY, WhatsApp, 10 AM, first=Jun 3
│ → Event: followup.scheduled → Kafka
│ → Confirmation notification sent via WhatsApp
│ → AI: "All set! See you June 3. Happy investing!"
│ → Event: conversation.completed → Kafka
│
POST-CONVERSATION
═══════════════════════════════════════════════════════════════
│ Analytics Service: updates funnel metrics
│ Summarization Agent: generates conversation summary
│ Customer registers (optional) → UC-09 migration
│
FOLLOW-UP (June 2)
═══════════════════════════════════════════════════════════════
│ Reminder Service: T-24hrs check → send reminder
│ → WhatsApp: "Review tomorrow at 10 AM" [Confirm/Reschedule]
│ Customer: "Confirm"
│
FOLLOW-UP (June 3)
═══════════════════════════════════════════════════════════════
│ Follow-Up Orchestrator: generate agenda from context
│ → AI: "Hi Ravi! Last time we discussed NPS. Any updates?"
│ → Interactive WhatsApp conversation continues
│ → New action items tracked
│ → Next follow-up: July 3
│
═══════════════════════════════════════════════════════════════
                    CYCLE CONTINUES
═══════════════════════════════════════════════════════════════
```

---

*These sequence diagrams complement the [Architecture Document](./01-architecture-document.md), [High-Level Design](./02-high-level-design.md), and [Low-Level Design](./03-low-level-design.md).*
