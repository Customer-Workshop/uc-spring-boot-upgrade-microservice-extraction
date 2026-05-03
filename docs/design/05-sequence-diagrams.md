# Relationship Manager — Sequence Diagrams (Non-AI Version)

> **Version:** 2.0 (Non-AI)
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
19. [UC-19: Input Parse Failure & Graceful Recovery](#uc-19-input-parse-failure--graceful-recovery)
20. [UC-20: End-to-End New Customer Journey (Complete Flow)](#uc-20-end-to-end-new-customer-journey-complete-flow)

---

## UC-01: Anonymous Customer Starts a Conversation

A new visitor lands on the platform and starts a conversation without logging in.

```mermaid
sequenceDiagram
    participant C as Customer (Browser)
    participant W as Web App (Next.js)
    participant GW as API Gateway (Kong)
    participant CS as Conv Svc
    participant R as Redis

    C->>W: Visit website
    C->>W: Click "Chat with RM"
    W->>GW: POST /api/v1/conversations {channel:"WEB"}
    GW->>CS: Create anonymous session
    CS->>R: Store session context
    R-->>CS: OK
    CS-->>GW: {conv_id, ws_url, session_token}
    GW-->>W: 201 Created + WS URL
    W-->>C: Chat UI opens + WS connect
    W->>CS: WebSocket Connected
    CS->>CS: Load greeting template
    CS-->>W: WS: greeting message
    W-->>C: "Hello! Welcome to ABC Bank. May I know your name?"
```

---

## UC-02: Conversational Data Collection (Personal Details)

The system collects customer name, age group, location, phone, and email through guided conversation.

```mermaid
sequenceDiagram
    participant C as Customer
    participant GW as API Gateway
    participant CS as Conv Svc
    participant TE as Template Engine
    participant PS as Profile Service

    C->>GW: WS: "Hi, I'm Ravi, 32 years from Hyderabad"
    GW->>CS: Forward message
    CS->>CS: Parse input (regex: name, age, location)
    Note over CS: Extracted: name="Ravi", age=32→"30-40", location="Hyderabad"
    CS->>PS: Save partial profile
    PS-->>CS: 200 OK
    CS->>TE: Load ask_contact template
    TE-->>CS: "Nice to meet you Ravi! Could you share your email and phone number?"
    CS-->>C: WS: response + phase progress (25%)

    C->>CS: WS: "Sure, my email is ravi@mail.com, phone 9876543210"
    CS->>CS: Parse input (regex: email, phone)
    Note over CS: Validated: email format OK, phone 10-digit OK
    CS->>PS: Update profile
    Note over CS: Phase transition: PERSONAL → FINANCIAL
    CS->>TE: Load financial phase intro
    TE-->>CS: "Great Ravi! Now, tell me about your income..."
    CS-->>C: WS: Phase change event + response
```

---

## UC-03: Financial Profile Collection

The system collects income source, income range, current investments, and savings.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant PS as Profile Service

    C->>CS: WS: "I work in IT, salaried, around 1.2L per month"
    CS->>CS: Parse: keyword match to SALARIED, amount 14.4L/yr maps to 100-150K
    CS->>PS: Save to profile
    CS->>TE: Load ask_investments template
    TE-->>CS: "That's great! Do you have any current investments like mutual funds, FDs, stocks?"
    CS-->>C: WS: response

    C->>CS: WS: "Yes, I have about 2L in MFs, 5L in FD, and 1L in stocks. Savings around 3L"
    CS->>CS: Parse amounts: MF=200000, FD=500000, equity=100000, savings=300000
    CS->>TE: Load confirmation template
    TE-->>CS: Confirmation message
    CS-->>C: WS: "Let me confirm: ₹2L in MFs, ₹5L in FDs, ₹1L in stocks, ₹3L savings. Is that right?"

    C->>CS: WS: "Yes, that's correct"
    CS->>PS: Confirm entities, Update profile
    Note over CS: Phase: FINANCIAL → GOALS
```

---

## UC-04: Retirement Goal Capture

The system collects retirement target amount and target age based on customer's age group.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant PS as Profile Service

    Note over CS,TE: Context: age group=30-40, income=100-150K
    CS->>TE: Load contextual goal template (age_group=30-40)
    TE-->>CS: Response
    CS-->>C: "Ravi, since you're in your early 30s, you have a great runway for building wealth. When would you like to retire? And how much would you like to have by then?"

    C->>CS: WS: "I want to retire at 55 with about 5 crores"
    CS->>CS: Parse: retirement_age=55, amount=50000000, years=20
    CS->>PS: Update profile
    Note over CS: Phase: GOALS → RISK_ASSESSMENT
    CS->>TE: Load risk intro template
    TE-->>CS: Response
    CS-->>C: "₹5 crores by 55 — great goal! Now let me understand your comfort with investment risk..."
```

---

## UC-05: Risk Profile Assessment

The system computes a risk score using weighted scoring and presents the result to the customer.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant PS as Profile Service
    participant RS as Risk Svc (Weighted Scoring)
    participant K as Kafka

    C->>CS: WS: "I'm okay with moderate risk, not too aggressive"
    CS->>CS: Parse risk preference: keyword "moderate" → stated_preference=0.5
    CS->>PS: Fetch full profile
    PS-->>CS: Full customer profile
    CS->>RS: Request risk assessment

    RS->>RS: Compute 15 features (weighted factors)
    RS->>RS: Weighted score calculation
    RS->>RS: Map to category
    RS-->>CS: risk_score=6.5, category="MODERATE", top_factors=[age, income_stability, savings_ratio]

    RS->>PS: Save risk profile
    RS->>K: Emit risk.assessed event

    CS->>TE: Load risk explanation template (category=MODERATE)
    TE-->>CS: "Based on your age (30-40), stable income, and existing investments, you're a Moderate investor (score: 6.5/10)..."
    CS-->>C: Risk profile explanation + score card
```

---

## UC-06: Product Recommendation & Wealth Projection

The recommendation engine builds a portfolio and the wealth projection engine calculates growth.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant REC as Recomm. Service
    participant PC as Product Catalog
    participant WP as Wealth Proj Svc

    Note over REC: Triggered by risk.assessed event
    REC->>PC: Fetch products by risk level (MODERATE)
    PC-->>REC: 25 matching products

    REC->>REC: Rule engine: filter by suitability (age, income, risk)
    REC->>REC: Rank by fit score (risk alignment + returns + tax benefits)
    REC->>REC: Build portfolio: Equity 40%, Debt 25%, FD 15%, Gold 10%, NPS 10%

    REC->>WP: Request wealth projection
    WP->>WP: Compound growth formula (3 scenarios)
    WP->>WP: Compute conservative / expected / optimistic per year
    WP-->>REC: Portfolio + Projection

    REC-->>CS: Recommendation ready (callback/event)
    CS->>TE: Load portfolio presentation template
    TE-->>CS: Rich card with portfolio allocation + projection chart data
    CS-->>C: Rich card: Portfolio allocation chart + wealth projection graph
```

---

## UC-07: Communication Channel Preference Selection

Customer selects their preferred channel for follow-up communications.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant PS as Profile Service

    CS->>TE: Load channel selection template
    TE-->>CS: Quick reply message with options
    CS-->>C: "How would you like me to reach you for follow-ups?" [SMS] [WhatsApp] [Email] [Phone]

    C->>CS: WS: Click "WhatsApp"
    CS->>PS: Save preference (preferred_channel=WHATSAPP, opt_in_whatsapp=true)
    PS-->>CS: 200 OK
    CS->>TE: Load time preference template
    TE-->>CS: Response
    CS-->>C: "Great choice! What time works best for follow-up messages? Weekday mornings or evenings?"

    C->>CS: WS: "Weekday mornings, around 10 AM"
    CS->>PS: Update prefs: time=10:00, days=MON-FRI
    Note over CS: Phase: CHANNEL → FOLLOWUP
```

---

## UC-08: Follow-Up Scheduling

The system creates a follow-up schedule and confirms with the customer.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant FU as FollowUp Orch.
    participant NS as Notific. Service
    participant K as Kafka

    CS->>TE: Load schedule suggestion template
    TE-->>CS: Response
    CS-->>C: "Would you like monthly check-ins? I can set up the first one for next month."

    C->>CS: WS: "Yes, monthly works"
    CS->>FU: Create schedule

    FU->>FU: Create schedule (freq=MONTHLY, channel=WA, time=10:00, first=Jun 3)
    FU->>FU: Create first instance
    FU-->>CS: Schedule created, next: Jun 3, 10 AM
    FU->>K: Emit followup.scheduled
    K->>NS: Event consumed
    NS->>NS: Schedule confirmation notification (WhatsApp)

    Note over CS: Phase: FOLLOWUP → COMPLETED
    CS->>TE: Load closing template
    TE-->>CS: Response
    CS-->>C: "All set, Ravi! First review on June 3 at 10 AM via WhatsApp. It was great chatting!"
    CS->>K: Emit conversation.completed
```

---

## UC-09: Anonymous-to-Authenticated Session Conversion

An anonymous user decides to register, and all data migrates seamlessly.

```mermaid
sequenceDiagram
    participant C as Customer
    participant W as Web App
    participant GW as API Gateway
    participant AUTH as Auth Svc
    participant CS as Conv Svc
    participant PS as Profile Service

    C->>W: Click "Register"
    W->>GW: POST /auth/register {email, pass, anon_session_id}
    GW->>AUTH: Register request
    AUTH->>AUTH: Create user account
    AUTH->>PS: Fetch anon profile data
    PS-->>AUTH: Anon profile data (name, age, income, investments, goals)
    AUTH->>PS: Migrate profile to user account
    AUTH->>CS: Migrate conversations
    CS->>CS: Update conv.customer_id
    CS->>CS: Delete anon session
    CS-->>AUTH: Migrated
    AUTH-->>GW: 201 {token, refresh, migrated:true}
    GW-->>W: JWT + profile
    W-->>C: Logged in, chat continues seamlessly
```

---

## UC-10: Follow-Up Reminder Delivery (Multi-Channel)

The reminder service triggers a notification 24 hours before a scheduled follow-up.

```mermaid
sequenceDiagram
    participant SCH as Scheduler (Cron)
    participant REM as Reminder Service
    participant FU as FollowUp Orch.
    participant PS as Profile Service
    participant NS as Notific. Service
    participant WA as WhatsApp API (Meta)

    SCH->>REM: Tick (every min)
    REM->>FU: Query due reminders (T-24hrs)
    FU-->>REM: Instance {id, cust_id, date, channel=WA}
    REM->>PS: Fetch customer name + prefs
    PS-->>REM: {name:"Ravi", preferred_channel:"WA", timezone:"Asia/Kolkata"}
    REM->>FU: Get agenda summary
    FU-->>REM: Agenda summary (from template)
    REM->>NS: Send reminder notification

    Note over NS: Template: followup_reminder_v1, Channel: WA
    NS->>WA: POST /v1/messages (interactive)
    WA-->>NS: 200 OK, msg_id=xyz

    Note over WA: Customer's phone receives WhatsApp:<br/>"Hi Ravi 👋 Your review is on Jun 3 at 10 AM"<br/>[Confirm] [Reschedule] [Cancel]

    WA-->>NS: Webhook: delivered
    REM->>FU: Update instance: REMINDER_SENT
```

---

## UC-11: Follow-Up Conversation Execution

A scheduled follow-up begins with a template-generated agenda from previous context.

```mermaid
sequenceDiagram
    participant C as Customer (WhatsApp/Web)
    participant FU as FollowUp Orch.
    participant CS as Conv Svc
    participant TE as Template Engine
    participant PS as Profile Service

    C->>FU: Click "Confirm"
    FU->>CS: Fetch previous conversation summary
    CS-->>FU: Last conversation transcript + summary
    FU->>PS: Fetch current profile
    PS-->>FU: Current profile + risk + recommendations
    FU->>FU: Build agenda from template + action items + profile changes
    FU->>CS: Create new conversation (follow-up type)
    CS->>TE: Load follow-up opening template with context
    TE-->>CS: Response
    CS-->>C: "Hi Ravi! Good to connect again. Last time we discussed your retirement portfolio. You were looking into NPS. Any updates?"
    Note over C,CS: Conversation continues with agenda topics
```

---

## UC-12: Human RM Handoff

When input cannot be parsed after 2 attempts or customer explicitly requests a human.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant RM as Human RM (Staff)
    participant NS as Notific. Service

    C->>CS: WS: "Can I talk to a real person?"
    CS->>CS: Detect handoff keyword: "real person" / "human" / "advisor"
    CS->>CS: Update state: HANDED_OFF
    CS->>CS: Generate conversation summary from stored data
    Note over CS: Summary built from: profile data + conversation phases completed + action items
    CS->>RM: Find available RM & assign
    CS->>NS: Notify RM (in-app + dashboard)
    NS-->>CS: Notified
    CS->>TE: Load handoff template
    TE-->>CS: Response
    CS-->>C: "Of course, Ravi! I'm connecting you with Priya, your dedicated advisor. One moment..."
    RM->>RM: RM views summary + full chat history
    RM->>CS: RM joins WS
    CS-->>C: "Hi Ravi, this is Priya. I've reviewed your profile. How can I help?"
```

---

## UC-13: WhatsApp Inbound Follow-Up Interaction

Customer responds to a follow-up via WhatsApp, triggering a structured conversation.

```mermaid
sequenceDiagram
    participant C as Customer (Phone)
    participant WA as WhatsApp API (Meta)
    participant GW as API Gateway Webhook
    participant NS as Notific. Service
    participant CS as Conv Svc
    participant TE as Template Engine

    C->>WA: Send WA message: "Hi, I've enrolled in NPS"
    WA->>GW: Webhook POST /webhooks/whatsapp
    GW->>NS: Route to Notification Service
    NS->>NS: Identify customer by phone number
    NS->>NS: Check for active follow-up
    NS->>CS: Route to Conv Service
    CS->>CS: Load context (last conv + follow-up agenda)
    CS->>CS: Parse input: keyword match → NPS enrollment detected
    CS->>TE: Load NPS follow-up template
    TE-->>CS: "That's great news! Which NPS fund did you choose? Auto Choice or Active Choice?"
    CS-->>NS: Reply message
    NS->>WA: POST /v1/messages
    WA-->>C: WhatsApp response
```

---

## UC-14: Wealth Projection Recalculation

Customer updates their investments, triggering a new projection.

```mermaid
sequenceDiagram
    participant C as Customer (Dashboard)
    participant W as Web App
    participant PS as Profile Service
    participant REC as Recomm. Service
    participant WP as Wealth Proj Svc
    participant K as Kafka

    C->>W: Update investment details on dashboard
    W->>PS: PATCH /customers/{id}/profile
    PS->>PS: Update financial profile
    PS->>K: Emit profile.updated
    PS-->>W: 200 OK

    K->>REC: Event consumed
    REC->>REC: Check if rebalancing needed (rule engine)
    REC->>WP: Recalculate projection
    WP->>WP: Compound growth formula (3 scenarios)
    WP-->>REC: Updated projection
    REC->>K: Emit recommendation.updated

    W-->>C: WS: Dashboard updated with new projection chart
```

---

## UC-15: Periodic Risk Profile Refresh

The system periodically re-assesses customer risk profiles.

```mermaid
sequenceDiagram
    participant SCH as Scheduler (Weekly)
    participant RS as Risk Svc (Weighted Scoring)
    participant PS as Profile Service
    participant TE as Template Engine
    participant REC as Recomm. Service
    participant K as Kafka

    SCH->>RS: Batch job: reassess expiring profiles
    RS->>PS: Query profiles expiring within 7d
    PS-->>RS: List of 200 profiles

    Note over RS,PS: For each profile:
    RS->>PS: Fetch full customer data
    PS-->>RS: Profile
    RS->>RS: Compute 15 features + weighted scoring
    RS->>RS: Compare new vs old score

    Note over RS,TE: If score changed:
    RS->>TE: Generate new explanation from template
    TE-->>RS: Updated explanation
    RS->>PS: Save new risk profile (is_current=true)
    RS->>K: Emit risk.reassessed

    K->>REC: Event consumed
    REC->>REC: Trigger recommendation update if category changed
```

---

## UC-16: Customer Dashboard Data Load

Customer views their dashboard with portfolio, projection, and upcoming follow-ups.

```mermaid
sequenceDiagram
    participant C as Customer (Browser)
    participant W as Web App (Next.js)
    participant GW as API Gateway (GraphQL)
    participant PS as Profile Service
    participant REC as Recomm. Service
    participant FU as FollowUp Orch.

    C->>W: Navigate to Dashboard
    W->>GW: GraphQL query { dashboard { profile, riskProfile, portfolio, projection, nextFollowUp } }
    GW->>GW: Parallel resolution:
    GW->>PS: getProfile
    GW->>PS: getRisk
    GW->>REC: getPortfolio
    GW->>REC: getProjection
    GW->>FU: getFollowUp
    PS-->>GW: Profile + Risk
    REC-->>GW: Portfolio + Projection
    FU-->>GW: Next follow-up
    GW-->>W: Unified GraphQL response
    W-->>C: Dashboard renders: Profile summary, Risk score gauge, Portfolio pie chart, Wealth projection line chart, Next follow-up card, Action items list
```

---

## UC-17: Admin Product Catalog Update

Bank admin adds or updates products in the catalog.

```mermaid
sequenceDiagram
    participant A as Admin (Staff)
    participant UI as Admin UI
    participant GW as API Gateway
    participant PC as Product Catalog
    participant K as Kafka

    A->>UI: Add new product: "Green Bond Fund"
    UI->>GW: POST /admin/products {name, cat, returns, risk_level}
    GW->>GW: Auth: admin role check
    GW->>PC: Create product
    PC->>PC: Save to DB
    PC->>PC: Update product index (Elasticsearch)
    PC->>K: Emit product.added
    PC-->>GW: Created
    GW-->>UI: 201 Created
    UI-->>A: Success
```

---

## UC-18: Conversation Resumption (Paused Session)

Customer returns to a previously paused conversation.

```mermaid
sequenceDiagram
    participant C as Customer
    participant W as Web App
    participant CS as Conv Svc
    participant TE as Template Engine
    participant R as Redis

    C->>W: Return to website (has session cookie)
    W->>CS: GET /api/v1/conversations?status=PAUSED
    CS->>R: Check Redis for active session
    R-->>CS: Session found: conv_id, phase=FINANCIAL, context={name:"Ravi",...}
    CS-->>W: Paused conversation found
    W-->>C: "Resume conversation?" prompt

    C->>W: Click "Resume"
    W->>CS: POST /api/v1/conversations/{id}/resume
    CS->>CS: Update status: PAUSED → ACTIVE
    CS->>TE: Load resumption template with context
    TE-->>CS: "Welcome back, Ravi! We were discussing your income and investments. Shall we continue?"
    CS-->>W: Resume message + chat history
    W-->>C: Chat UI with history and resumption message
```

---

## UC-19: Input Parse Failure & Graceful Recovery

When the system cannot parse customer input, it falls back to structured options.

```mermaid
sequenceDiagram
    participant C as Customer
    participant CS as Conv Svc
    participant TE as Template Engine
    participant MON as Monitoring (Alerting)

    C->>CS: WS: "What funds do you recommend?"
    CS->>CS: Parse input
    Note over CS: Current phase: FINANCIAL (expecting income data)
    CS->>CS: No matching entities found for current phase

    CS->>TE: Load clarification template (phase=FINANCIAL)
    TE-->>CS: Clarification + structured options
    CS-->>C: "I'd like to understand your income first. Could you select your income range? [60-70K] [70-80K] [80-100K] [100-150K] [150K+]"

    C->>CS: WS: Click "100-150K"
    CS->>CS: Parse selection: income_range="100-150K"
    CS-->>C: "Great! Now, do you have any current investments?"

    Note over CS: If 2 consecutive parse failures:
    CS->>TE: Load handoff offer template
    TE-->>CS: Response
    CS-->>C: "I'm having trouble understanding. Would you like me to connect you with a human advisor?"
    CS->>MON: Log parse failure pattern for review
```

---

## UC-20: End-to-End New Customer Journey (Complete Flow)

Complete journey from anonymous visit to scheduled follow-up.

```mermaid
sequenceDiagram
    participant C as Customer
    participant W as Web App
    participant CS as Conv Svc
    participant TE as Template Engine
    participant PS as Profile Svc
    participant RS as Risk Svc
    participant REC as Recomm Svc
    participant WP as Wealth Proj
    participant FU as FollowUp Orch
    participant NS as Notific Svc
    participant K as Kafka

    Note over C,W: PHASE 1: ANONYMOUS ENTRY
    C->>W: Visit website → Chat widget opens
    W->>CS: Anonymous session created
    CS->>TE: Load greeting template
    CS-->>C: "Hello! Welcome to ABC Bank..."

    Note over C,PS: PHASE 2: PERSONAL DATA COLLECTION
    C->>CS: "I'm Ravi, 32, from Hyderabad"
    CS->>CS: Parse input (regex extraction)
    Note over CS: name=Ravi, age_group=30-40, location=Hyderabad
    CS->>PS: Save partial profile
    CS-->>C: "Could you share your email and phone?"
    C->>CS: "ravi@mail.com, 9876543210"
    CS->>PS: Profile updated → Phase FINANCIAL

    Note over C,PS: PHASE 3: FINANCIAL PROFILE
    CS-->>C: "What do you do for a living?"
    C->>CS: "Salaried, IT, about 1.2L/month"
    CS->>PS: income_source=SALARIED, income_range=100-150K
    CS-->>C: "Any current investments?"
    C->>CS: "2L MFs, 5L FD, 1L stocks, 3L savings"
    CS->>PS: investments + savings saved → Phase GOALS

    Note over C,PS: PHASE 4: RETIREMENT GOALS
    CS-->>C: "When would you like to retire and with how much?"
    C->>CS: "55, about 5 crores"
    CS->>PS: retirement_age=55, target=50000000 → Phase RISK_ASSESSMENT

    Note over C,RS: PHASE 5: RISK ASSESSMENT
    CS-->>C: "How comfortable are you with investment risk?"
    C->>CS: "Moderate, not too aggressive"
    CS->>RS: Full profile for assessment
    RS->>RS: 15 weighted factors → score=6.5 (MODERATE)
    RS->>RS: Top factors = [age, income stability, savings ratio]
    RS->>TE: Generate explanation from template
    RS->>K: risk.assessed
    CS-->>C: Risk profile card → Phase RECOMMENDATION

    Note over REC,WP: PHASE 6: PRODUCT RECOMMENDATION
    K->>REC: risk.assessed event
    REC->>REC: Rule engine → Suitability filter → Rank by fit score
    REC->>REC: Portfolio: Equity 40%, Debt 25%, FD 15%, Gold 10%, NPS 10%
    REC->>WP: Compound growth projection (3 scenarios)
    WP-->>REC: Expected = ₹2.8Cr in 20 years
    REC-->>CS: Rich card (pie chart + line chart)
    CS-->>C: Portfolio + projection → Phase CHANNEL_PREF

    Note over C,NS: PHASE 7: CHANNEL PREFERENCE
    CS-->>C: "How would you like me to follow up?" [SMS] [WhatsApp] [Email] [Phone]
    C->>CS: Click "WhatsApp"
    CS-->>C: "What time works best?"
    C->>CS: "Weekday mornings, 10 AM"
    CS->>PS: Preferences saved → Phase FOLLOWUP_SCHEDULE

    Note over C,K: PHASE 8: FOLLOW-UP SCHEDULING
    CS-->>C: "Monthly check-ins work?"
    C->>CS: "Yes"
    CS->>FU: Schedule: MONTHLY, WhatsApp, 10 AM, first=Jun 3
    FU->>K: followup.scheduled
    K->>NS: Confirmation notification via WhatsApp
    CS-->>C: "All set! See you June 3. Happy investing!"
    CS->>K: conversation.completed

    Note over NS,K: POST-CONVERSATION
    K->>K: Analytics: funnel metrics updated
    CS->>CS: Generate summary from conversation data (template)

    Note over NS,C: FOLLOW-UP (June 2)
    FU->>NS: T-24hrs: send reminder
    NS-->>C: WhatsApp: "Review tomorrow at 10 AM" [Confirm/Reschedule]
    C->>NS: "Confirm"

    Note over FU,C: FOLLOW-UP (June 3)
    FU->>FU: Build agenda from template + action items
    FU-->>C: "Hi Ravi! Last time we discussed NPS. Any updates?"
    Note over C,FU: Interactive WhatsApp conversation continues → New action items tracked → Next follow-up: July 3
```

---

*These sequence diagrams complement the [Architecture Document](./01-architecture-document.md), [High-Level Design](./02-high-level-design.md), and [Low-Level Design](./03-low-level-design.md).*
