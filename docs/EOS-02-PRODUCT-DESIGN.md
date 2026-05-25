# 02 — Product Design

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Core Concepts

| Concept | Definition |
|---------|------------|
| **Task** | A discrete verification job with location, instructions, checklist, deadline, and budget |
| **Evidence Package** | The structured deliverable: GPS-stamped photos, video, checklist responses, inspector notes |
| **Inspector** | A verified local agent who completes tasks. Tiered: Basic, Professional, Expert |
| **Client** | The person or organization posting tasks. Diaspora individual or enterprise (fintech, insurer, NGO) |
| **Escrow** | Client funds held by the platform. Released to inspector upon evidence acceptance |
| **Trust Score** | Composite rating for inspectors: completion rate + evidence quality + client rating + dispute history |
| **Vertical Template** | Pre-configured task structure for a specific use case (construction, property, farm, collateral) |
| **Project** | A container for multiple related tasks (e.g., monthly construction progress checks for one building) |

## User Personas and Jobs-to-be-Done

### Client Personas

**Persona 1: Ama — UK-based Ghanaian building a house in Tema**
- Age 38, nurse in London. Sending money monthly to brother managing construction.
- JTBD: "I need proof my house is actually being built before I send the next tranche."
- Current behavior: WhatsApp photos from brother (unreliable, infrequent, unverifiable).
- Switching cost: Admitting she doesn't trust her brother. Social friction.
- Trigger: Missed 3 months of updates. Hears about contractor fraud from church friend.
- "Done" looks like: Monthly GPS-stamped photo report with checklist showing construction stage.

**Persona 2: Chidi — US-based Nigerian buying land in Lagos**
- Age 45, software engineer in Houston. Found a plot via Instagram ad.
- JTBD: "I need someone to physically verify this land exists, has clean title, and isn't disputed."
- Current behavior: Trusting the developer's glossy marketing materials. Wiring money and hoping.
- Switching cost: Low — he has no existing verification method.
- Trigger: Read about ₦21M fraud case. Colleague lost money on off-plan development.
- "Done" looks like: One-time verification report with photos, boundary markers, title cross-reference.

**Persona 3: Wanjiku — Kenyan diaspora in Toronto managing rental property in Nairobi**
- Age 52, accountant. Owns two rental units managed by a local agent.
- JTBD: "I need to know my property is maintained, tenants haven't damaged it, and rent is being collected."
- Current behavior: Annual trip home to check. Trusts agent's word between visits.
- Trigger: Agent claimed maintenance cost that seemed inflated. Can't verify remotely.
- "Done" looks like: Quarterly property condition report with interior/exterior photos, occupancy verification.

**Persona 4: Enterprise — KreditPay (fictional fintech lender in Lagos)**
- MSME lending platform. Needs to verify that collateral declared on loan applications is real.
- JTBD: "We need to physically confirm that the shop, inventory, and equipment listed on a loan application actually exist before disbursing."
- Current behavior: Loan officers visit in person (expensive, slow, doesn't scale).
- Trigger: NPL rate increased because borrowers falsified business details.
- "Done" looks like: API-triggered verification task → evidence package → automated loan decision input.

### Inspector Personas

**Persona 5: Kwame — Accra engineering student**
- Age 23, studying civil engineering at KNUST. Looking for side income.
- Motivation: Earn GHS 100–200/week (significant student income), gain field experience.
- Skills: Understands construction stages, can identify materials and workmanship.
- Risk: May rush tasks for quick payout. Quality degradation over time.

**Persona 6: Amina — Nairobi certified surveyor**
- Age 34, freelance surveyor. Wants to fill gaps between contract jobs.
- Motivation: Earn KES 3,000–8,000/task, build professional reputation.
- Skills: Licensed, can do boundary verification, title cross-referencing.
- Risk: Over-qualified for basic tasks. May find Basic tier tasks beneath her.

**Persona 7: Emeka — Lagos gig worker**
- Age 28, does Bolt delivery, field agent work. Owns a motorcycle.
- Motivation: Diversify income streams. GIS verification pays more than delivery.
- Skills: Knows Lagos geography intimately. Fast on the ground.
- Risk: No construction/property background. Needs structured templates to deliver quality.

## Task Lifecycle

```
┌──────────────────────────────────────────────────────────────────┐
│                        TASK LIFECYCLE                             │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────┐     ┌──────────────┐     ┌───────────────────┐     │
│  │  DRAFT   │────▶│ POSTED       │────▶│ ESCROW_FUNDED     │     │
│  │          │     │ (visible to  │     │ (money locked)    │     │
│  │ Client   │     │  inspectors) │     │                   │     │
│  │ creates  │     │              │     │ Client funds via  │     │
│  │ task     │     │              │     │ Stripe/Flutterwave│     │
│  └─────────┘     └──────────────┘     └────────┬──────────┘     │
│                                                 │                │
│                                                 ▼                │
│                                       ┌───────────────────┐     │
│                                       │ INSPECTOR_MATCHED  │     │
│                                       │                   │     │
│                                       │ Geo-ping sent →   │     │
│                                       │ Inspector accepts │     │
│                                       │ ETA committed     │     │
│                                       └────────┬──────────┘     │
│                                                │                │
│                                                ▼                │
│                                       ┌───────────────────┐     │
│                                       │ IN_PROGRESS        │     │
│                                       │                   │     │
│                                       │ Inspector travels │     │
│                                       │ to site, opens    │     │
│                                       │ in-app capture    │     │
│                                       └────────┬──────────┘     │
│                                                │                │
│                                                ▼                │
│                                       ┌───────────────────┐     │
│                                       │ EVIDENCE_SUBMITTED │     │
│                                       │                   │     │
│                                       │ Auto-validated:   │     │
│                                       │ GPS match, EXIF,  │     │
│                                       │ photo count,      │     │
│                                       │ checklist complete │     │
│                                       └────────┬──────────┘     │
│                                                │                │
│                                                ▼                │
│                                       ┌───────────────────┐     │
│                                       │ UNDER_REVIEW       │     │
│                                       │                   │     │
│                                       │ Client has 48hrs  │     │
│                                       │ to review         │     │
│                                       └───┬─────┬────┬────┘     │
│                                           │     │    │          │
│                          ┌────────────────┘     │    └────────┐ │
│                          ▼                      ▼             ▼ │
│                 ┌──────────────┐  ┌──────────────┐ ┌──────────┐│
│                 │   ACCEPTED   │  │  REVISION    │ │ DISPUTED ││
│                 │              │  │  REQUESTED   │ │          ││
│                 │ Escrow       │  │              │ │ Platform ││
│                 │ released to  │  │ Inspector    │ │ mediates ││
│                 │ inspector.   │  │ gets specific│ │ within   ││
│                 │ Both rate.   │  │ feedback,    │ │ 7 days   ││
│                 │              │  │ resubmits    │ │          ││
│                 └──────────────┘  └──────────────┘ └──────────┘│
│                                                                 │
│  Also: CANCELLED (by client before match)                       │
│        EXPIRED (no inspector accepted within deadline)          │
│        AUTO_ACCEPTED (48hr timer expired, no client action)     │
└─────────────────────────────────────────────────────────────────┘
```

## Evidence Model

### What Constitutes Valid Evidence

Every evidence package must contain:

**Mandatory (all tasks):**
- GPS coordinates verified against task location (within configurable radius, default 200m)
- Capture timestamp (server-verified, not device-only)
- Device fingerprint (model, OS version, app version)
- Minimum photo count per template (default 5, varies by vertical)
- Photos captured in-app only (no camera roll uploads)
- Structured checklist completion (all required items answered)

**Per-Vertical Requirements:**
- Construction: N/S/E/W exterior shots, interior if accessible, materials on site, worker activity
- Property: Room-by-room interior, 4-side exterior, boundary markers, signage, occupancy indicators
- Agriculture: Crop stage photo, irrigation system, storage conditions, input materials
- Fintech: Business exterior with signage, interior with inventory, owner/manager present
- Insurance: Damage from multiple angles, surrounding area, before/after comparison if available
- NGO: Project site, materials, beneficiary activity, signage/branding

**Fraud Signals (auto-computed):**
- GPS spoofing score (multi-signal: GPS vs. network location vs. WiFi vs. cell tower)
- EXIF consistency (timestamp in EXIF matches submission time within tolerance)
- Device motion (accelerometer data confirms physical movement during capture window)
- Photo originality (perceptual hash compared against stock photo databases and previous submissions)
- In-app capture attestation (photo metadata confirms capture through EyeOnSite camera module)

### Evidence Quality Score

```
quality_score = weighted_average(
  photo_clarity:      0.25,  // Cloud Vision: blur, exposure, resolution
  photo_completeness: 0.25,  // All required angles/items captured
  checklist_depth:    0.20,  // Response detail (not just yes/no but notes)
  gps_accuracy:       0.15,  // How close to task site, GPS accuracy in meters
  timeliness:         0.15,  // Submitted within expected time window
)
```

## Trust Graph

### Inspector Trust Score

```
trust_score = weighted_average(
  completion_rate:    0.25,  // Tasks completed / tasks accepted
  evidence_quality:   0.25,  // Average quality_score across completed tasks
  client_rating:      0.20,  // Average 1-5 star rating from clients
  dispute_rate:       0.15,  // Inverse: (1 - disputes/completed_tasks)
  response_time:      0.10,  // Average time to accept task after ping
  tenure:             0.05,  // Months active on platform (caps at 12mo)
)
```

**Tier Progression:**
- Basic: trust_score >= 0.0 (entry), 0+ completed tasks
- Professional: trust_score >= 0.75 AND 50+ completed tasks AND relevant background
- Expert: trust_score >= 0.90 AND 200+ completed tasks AND professional certification

### Client Trust Score

```
client_trust = weighted_average(
  payment_reliability: 0.35,  // Escrow funded on time, no chargebacks
  review_fairness:     0.25,  // Doesn't consistently rate low vs. platform average
  dispute_rate:        0.25,  // Inverse of disputes initiated / tasks completed
  response_time:       0.15,  // Time to review evidence (faster = better)
)
```

## App Flows

### Client App — Core Screens

1. **Home / Dashboard** — Active tasks, recent tasks, project overview
2. **Create Task** — Select vertical template → pin location on map → write instructions → set budget → set deadline → fund escrow
3. **Task Detail** — Live status, inspector profile, messaging, evidence review
4. **Evidence Review** — Photo gallery with GPS overlay, checklist responses, inspector notes. Accept / Request Revision / Dispute buttons
5. **Project View** — All tasks for one property/project, timeline, progress visualization
6. **Profile / Settings** — Payment methods, notification preferences, transaction history

### Inspector App — Core Screens

1. **Home / Available Tasks** — Map view of nearby tasks with distance, budget, deadline, vertical type
2. **Task Detail** — Instructions, checklist preview, budget, deadline, client rating. Accept button with ETA commitment
3. **Capture Mode** — In-app camera with checklist overlay. GPS indicator (green = within radius, red = too far). Photo counter. Required angles guide
4. **Evidence Summary** — Review captured photos, complete checklist, add notes. Submit button
5. **Earnings** — Balance, pending payouts, completed tasks, weekly/monthly summary
6. **Profile** — Trust Score, tier, badges, ratings received, training modules

### Admin Dashboard (Web — Compose for Web)

1. **Operations Overview** — Active tasks, completion rate, dispute rate, revenue, inspector utilization
2. **Task Management** — All tasks with filters (status, city, vertical, date)
3. **Dispute Queue** — Open disputes with evidence from both sides, mediation tools, resolution actions
4. **Inspector Management** — Onboarding pipeline, active inspectors, flagged inspectors, deactivation queue
5. **Analytics** — Cohort analysis, city-level metrics, vertical-level metrics, revenue trends
6. **Escrow Management** — Held funds, pending releases, refund queue, reconciliation

## Notification System

| Event | Client Notification | Inspector Notification |
|-------|-------------------|----------------------|
| Task posted | "Your task is live. Searching for inspectors..." | — |
| Inspector matched | "Inspector [Name] accepted your task. ETA: [time]" | "New task available: [title] — [distance] away" |
| Inspector en route | "Inspector is heading to your site" | — |
| Evidence submitted | "Evidence ready for review" | "Evidence submitted. Awaiting client review." |
| Evidence accepted | "Task complete. Escrow released." | "Payment of [amount] released to your [MoMo/bank]." |
| Revision requested | "Client requested changes: [notes]" | "Revision requested: [specific feedback]" |
| Dispute opened | "Dispute filed. We'll review within 72hrs." | "Client disputed. Platform reviewing." |
| 48hr auto-accept warning | "Review by [time] or evidence auto-accepts" | — |
| Payout processed | — | "₵[amount] sent to your MTN MoMo" |

Notifications via: FCM push (primary), in-app notification center, email (critical events), WhatsApp (opt-in, V2).

## Dispute Flow

```
Client clicks "Dispute" with reason + notes
    │
    ▼
Platform auto-categorizes:
├── Evidence quality (photos blurry, wrong location, incomplete checklist)
├── Task not completed (inspector never visited site)
├── Inspector no-show (accepted but didn't complete within deadline)
├── Wrong site (inspector went to wrong location)
└── Other (client describes issue)
    │
    ▼
Tier 1: Automated Resolution (< 1 hour)
├── GPS data clearly shows inspector wasn't at site → Auto-refund
├── Photo count below minimum → Auto-revision request
├── Checklist incomplete → Auto-revision request
└── All automated checks pass → Escalate to Tier 2
    │
    ▼
Tier 2: Team Mediation (< 72 hours)
├── Review evidence package, client complaint, inspector response
├── May request additional evidence from inspector
├── Decision: Full refund / Partial refund / Release to inspector / Re-task
└── Both parties notified with reasoning
    │
    ▼
Tier 3: External Arbitration (rare, < 7 days)
├── Client or inspector appeals Tier 2 decision
├── Independent reviewer (contracted)
└── Final, binding decision
```

---

*See [EOS-05-MANIFEST-SPECS.md](./EOS-05-MANIFEST-SPECS.md) for full Kotlin data class schemas.*
*See [EOS-06-INSPECTOR-SPEC.md](./EOS-06-INSPECTOR-SPEC.md) for inspector onboarding and tier details.*
*See [EOS-A4-DISPUTE-RESOLUTION.md](./EOS-A4-DISPUTE-RESOLUTION.md) for full dispute resolution framework.*
