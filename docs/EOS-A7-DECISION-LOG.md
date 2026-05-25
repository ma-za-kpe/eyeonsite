# A7 — Decision Log

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Purpose

Every significant decision, why it was made, what was rejected, and when to revisit. This is the institutional memory. When a future team member asks "why did we do it this way?", this document is the answer.

---

## How to Use This Log

Each entry follows:
- **Decision:** What was decided
- **Date:** When
- **Context:** What problem triggered the decision
- **Options considered:** What alternatives were evaluated
- **Reasoning:** Why this option was chosen
- **Trade-offs:** What we gave up
- **Revisit trigger:** When to reconsider this decision
- **Status:** ACTIVE, REVISITED, REVERSED

---

## Architecture Decisions

### DEC-001: Kotlin Multiplatform (KMP) for All Surfaces

| Field | Value |
|-------|-------|
| **Decision** | Use KMP for shared logic, Jetpack Compose for Android, CMP for iOS, Compose for Web for web surfaces |
| **Date** | May 2026 |
| **Context** | Solo developer needs maximum code sharing. Must ship Android, iOS, and Web with one person. |
| **Options** | (A) KMP — Kotlin everywhere. (B) Flutter — Dart, single widget tree. (C) React Native — JavaScript, large ecosystem. (D) Native per platform — Swift + Kotlin + TypeScript. |
| **Reasoning** | Maku writes Kotlin daily at Farmerline. KMP is stable (since Nov 2023). Google co-investing. Shares 60-70% of code. CMP for iOS avoids learning SwiftUI from scratch. Compose for Web eliminates JavaScript entirely. |
| **Trade-offs** | Smaller ecosystem than Flutter/React Native. Compose for Web has limited UI libraries. CMP iOS is newer than SwiftUI. Fewer developers available for hiring. |
| **Revisit trigger** | If CMP iOS quality is unacceptable for specific screens (drop to SwiftUI hybrid). If Compose for Web becomes blocking for landing page SEO (consider static HTML alternative for landing page only). |
| **Status** | ACTIVE |

### DEC-002: Firebase/GCP Over Supabase/AWS

| Field | Value |
|-------|-------|
| **Decision** | Firebase (Firestore, Auth, Cloud Functions v2, Storage, FCM) on Google Cloud Platform exclusively |
| **Date** | May 2026 |
| **Context** | Need offline-first mobile database, phone OTP auth that works in Africa, serverless compute, and file storage with resumable uploads. |
| **Options** | (A) Firebase/GCP. (B) Supabase/Vercel. (C) AWS (DynamoDB, Lambda, Cognito, S3). |
| **Reasoning** | Firestore has built-in offline persistence on mobile. Firebase Auth phone OTP works in Ghana/Nigeria/Kenya out of the box. Cloud Functions v2 scales to zero. Resumable uploads critical for spotty connectivity. KMP Firebase SDK exists (dev.gitlive). |
| **Trade-offs** | Firestore is NoSQL (relational queries harder). No native full-text search (need Algolia or similar at scale). Vendor lock-in to Google. Firestore pricing can spike with unoptimized reads. |
| **Revisit trigger** | If Firestore costs exceed $500/month (optimize queries, consider read replicas). If relational queries become critical (consider PostgreSQL for analytics, keep Firestore for real-time). |
| **Status** | ACTIVE |

### DEC-003: Compose for Web Over Next.js

| Field | Value |
|-------|-------|
| **Decision** | Use Compose for Web (Kotlin/JS) for landing page, admin dashboard, and client portal. Hosted on Firebase Hosting. |
| **Date** | May 2026 |
| **Context** | Need web surfaces for marketing and admin. Question is whether to introduce JavaScript/TypeScript into the stack. |
| **Options** | (A) Compose for Web — one language. (B) Next.js — best web framework, large ecosystem, great SEO. (C) Static HTML — simplest, best SEO. |
| **Reasoning** | One language across every surface eliminates context switching. Compose for Web shares components with mobile. Admin dashboard doesn't need SEO. Landing page SEO mitigated by prerender.io. |
| **Trade-offs** | Larger JS bundle size. Weaker SEO (CSR vs SSR). Smaller ecosystem (fewer UI component libraries). Harder to hire web developers for this stack. |
| **Revisit trigger** | If landing page search rankings matter (organic search becomes a channel) → consider static HTML for landing page only. If web dashboard needs complex UI that CfW can't handle → consider Next.js for dashboard only. |
| **Status** | ACTIVE |

### DEC-004: Cloud Functions v2 (Node.js) Over Kotlin/JVM Backend

| Field | Value |
|-------|-------|
| **Decision** | Cloud Functions v2 with Node.js runtime for all backend logic |
| **Date** | May 2026 |
| **Context** | Backend needs to handle: matching, escrow, evidence validation, notifications. Could use Kotlin/JVM (Ktor server) or Node.js (Firebase Functions). |
| **Options** | (A) Cloud Functions v2 (Node.js). (B) Ktor server on Cloud Run (Kotlin/JVM). (C) Cloud Functions v2 (Java/Kotlin via GraalVM). |
| **Reasoning** | Firebase Admin SDK is most mature in Node.js. Firestore triggers work natively with Cloud Functions. Scales to zero (no idle cost). Cold start is fastest with Node.js. GraalVM Kotlin option has longer cold starts and more complexity. |
| **Trade-offs** | Backend is Node.js while everything else is Kotlin. Can't share code between backend and clients. TypeScript type safety weaker than Kotlin. |
| **Revisit trigger** | If backend complexity grows (V3+ API, complex matching algorithm) → consider Ktor on Cloud Run for specific microservices. Keep Cloud Functions for Firestore triggers and event-driven logic. |
| **Status** | ACTIVE |

---

## Product Decisions

### DEC-005: Accra First, Not Lagos

| Field | Value |
|-------|-------|
| **Decision** | Launch in Accra, Ghana as V1 city |
| **Date** | May 2026 |
| **Context** | Need to pick first city. Lagos has larger market, Accra has operational advantages. |
| **Options** | (A) Accra. (B) Lagos. (C) Nairobi. |
| **Reasoning** | Maku is physically in Accra (can be Inspector #1). MEST network provides instant credibility. Less competition (no verification startups in Ghana). UK→Ghana corridor is strong and accessible. Accra is compact (inspectors can reach most areas quickly). |
| **Trade-offs** | Lagos is 5x bigger market. Nairobi has better M-Pesa infrastructure. Accra may have lower task density. |
| **Revisit trigger** | V1 success metrics met → expand to Lagos or Nairobi (V2). Decision between them based on V1 demand signals. |
| **Status** | ACTIVE |

### DEC-006: Construction as V1 Vertical

| Field | Value |
|-------|-------|
| **Decision** | Construction/housing progress monitoring as the launch vertical |
| **Date** | May 2026 |
| **Context** | Multiple verticals possible (construction, property, agriculture, fintech, insurance, NGO). Need to pick one for focused V1. |
| **Options** | (A) Construction. (B) Property/land. (C) Agriculture. |
| **Reasoning** | Highest emotional stakes (life savings). Recurring need (monthly checks over 12-24 months = highest LTV). Clear evidence criteria (construction stages are visible). Biggest fraud stories resonate with diaspora audience. Personal network access. |
| **Trade-offs** | Construction tasks take longer (site may be in suburb/outskirts). Requires construction knowledge in inspectors. Lower per-task price ceiling than enterprise verticals. |
| **Revisit trigger** | If V0 demand signals show property/land verification is stronger → pivot V1 vertical. |
| **Status** | ACTIVE |

### DEC-007: 18% Platform Commission

| Field | Value |
|-------|-------|
| **Decision** | Default take rate of 18% for consumer marketplace transactions |
| **Date** | May 2026 |
| **Context** | Need to set commission that's sustainable for platform, fair to inspectors, and not prohibitive for clients. |
| **Options** | (A) 10% — aggressive, inspector-friendly. (B) 15% — moderate. (C) 18% — balanced. (D) 20-25% — Bolt/Uber territory. |
| **Reasoning** | 18% is below ride-hailing (20-25%) which helps with inspector retention narrative. Above payment-only platforms (3-5%). Yields ~$0.69 net per GHS 60 consumer task after processing costs. Enough to cover COGS and contribute to overhead. |
| **Trade-offs** | Lower than Bolt/Uber means slower path to profitability. Inspector still loses nearly 1/5 of task budget. |
| **Revisit trigger** | If inspector churn > 30% → reduce to 15%. If margins too thin at scale → increase to 20% for non-enterprise. Enterprise always at 15%. |
| **Status** | ACTIVE |

### DEC-008: Stripe + Flutterwave Dual Payment Rails

| Field | Value |
|-------|-------|
| **Decision** | Stripe for diaspora client payments (USD/GBP/EUR), Flutterwave for local client payments AND all inspector payouts |
| **Date** | May 2026 |
| **Context** | Need to accept international cards from diaspora AND pay inspectors locally via mobile money. No single provider covers both well. |
| **Options** | (A) Stripe only (global). (B) Flutterwave only (Africa). (C) Stripe + Flutterwave split. (D) Paystack + Stripe. |
| **Reasoning** | Stripe: Manti Labs LLC already on Stripe Atlas. Best diaspora card acceptance. But Stripe doesn't do MoMo/M-Pesa payouts well. Flutterwave: covers MTN MoMo (Ghana), M-Pesa (Kenya), bank transfers (Nigeria, Uganda). Best African payout coverage across all 4 target markets. Paystack rejected: only strong in Nigeria, not multi-country. |
| **Trade-offs** | Two payment integrations to maintain. FX management between Stripe (USD) and Flutterwave (local currencies). Reconciliation across two systems. |
| **Revisit trigger** | If Stripe launches native MoMo/M-Pesa payouts in target markets → consolidate to Stripe only. If Flutterwave's international card acceptance improves → consider consolidating to Flutterwave only. |
| **Status** | ACTIVE |

### DEC-009: 48-Hour Auto-Accept Window

| Field | Value |
|-------|-------|
| **Decision** | Evidence auto-accepts if client doesn't review within 48 hours |
| **Date** | May 2026 |
| **Context** | Inspector needs to be paid. Client may be in a different timezone, busy, or forget to review. Can't hold inspector's money indefinitely. |
| **Options** | (A) 24 hours. (B) 48 hours. (C) 72 hours. (D) No auto-accept (client must explicitly act). |
| **Reasoning** | 48 hours balances inspector payment speed with client review time. Diaspora clients are in different timezones (UK/US vs Ghana) — 24hrs may be too fast. 72hrs delays inspector payment too long. No auto-accept creates indefinite holds and inspector distrust. |
| **Trade-offs** | Client may miss the window and have no recourse (mitigated: can still dispute within 7 days for fraud). Inspector may submit poor quality knowing auto-accept will save them (mitigated: trust score still affected by low quality scores). |
| **Revisit trigger** | If > 30% of tasks auto-accept (clients aren't reviewing) → add more aggressive reminders or extend to 72hrs with multiple nudges. If < 5% auto-accept → could shorten to 24hrs. |
| **Status** | ACTIVE |

### DEC-010: In-App Capture Only (No Gallery Uploads)

| Field | Value |
|-------|-------|
| **Decision** | All evidence photos and videos must be captured through the EyeOnSite in-app camera module. No camera roll or gallery uploads permitted. |
| **Date** | May 2026 |
| **Context** | Evidence integrity is the product. If inspectors can upload gallery photos, they can submit old photos, photos from Google, photos taken by someone else, or AI-generated images. |
| **Options** | (A) In-app only. (B) Allow gallery with EXIF verification. (C) Allow gallery with AI authenticity check. |
| **Reasoning** | In-app capture lets us: control EXIF data, embed GPS at capture time, embed device fingerprint, ensure camera sensor matches declared device, and provide visual proof of in-app capture via metadata attestation. Gallery uploads bypass all of this — even with EXIF checks, metadata can be spoofed. This is the single most important product integrity decision. |
| **Trade-offs** | Inspector can't use a better camera app. Inspector can't take photos first and upload later from gallery. If in-app camera crashes, inspector must restart. Slightly worse camera UX than native camera app. |
| **Revisit trigger** | Never. This is a permanent decision. Evidence integrity is non-negotiable. |
| **Status** | ACTIVE — PERMANENT |

---

## Rejected Ideas

### REJECTED: Separate iOS App in SwiftUI

**Why considered:** Native SwiftUI would give the best iOS experience.
**Why rejected:** Would require maintaining two complete UI codebases (Compose for Android, SwiftUI for iOS). Solo developer can't sustain this. CMP provides acceptable iOS quality with maximum code sharing.
**Would reconsider if:** CMP iOS quality is demonstrably poor in user testing AND we have hired a second developer who knows SwiftUI.

### REJECTED: PostgreSQL + Supabase for Backend

**Why considered:** SQL is more powerful for analytics. Supabase is developer-friendly.
**Why rejected:** No native offline sync for mobile. Phone OTP in Africa requires additional Twilio setup. Firestore's offline persistence is a killer feature for an app that must work in low-connectivity environments.
**Would reconsider if:** Analytics needs outgrow Firestore AND we add a dedicated analytics database (PostgreSQL for analytics, Firestore for real-time).

### REJECTED: Escrow in Separate Bank Account

**Why considered:** True escrow (funds in a segregated bank account) is the gold standard.
**Why rejected:** Requires banking/e-money license in each operating country. Adds massive regulatory burden. V1 approach: funds held in Stripe/Flutterwave merchant accounts with logical escrow tracking in Firestore. This is how Uber/Bolt hold ride fares before paying drivers — no separate bank account.
**Would reconsider if:** Volume exceeds $100K/month held in escrow AND regulators require formal segregation AND we've raised sufficient capital for licensing.

### REJECTED: WhatsApp-First (No App)

**Why considered:** WhatsApp is ubiquitous in Africa. Could build inspection flow via WhatsApp Business API + chatbot.
**Why rejected:** Can't control camera capture via WhatsApp (no GPS overlay, no in-app-only enforcement, no EXIF control). Evidence integrity impossible without a native app. WhatsApp works for V0 manual validation but can't be the V1 product.
**Would reconsider if:** WhatsApp launches verified camera capture with GPS attestation (extremely unlikely).

### REJECTED: AI-Generated Evidence Reports (V1)

**Why considered:** LLM summarizes photos + checklist into a narrative report.
**Why rejected for V1:** Adds complexity, cost, and potential hallucination risk. V1 evidence package is the raw data — photos, checklist, GPS overlay. The client sees the evidence directly. AI-generated summaries are a V3 enhancement when we have enough data to validate accuracy.
**Would reconsider if:** Clients consistently request narrative summaries in V1 feedback.

---

## Log Maintenance Rules

1. **Every new decision gets an entry.** If it's worth discussing for > 5 minutes, it's worth logging.
2. **Never delete entries.** Mark as REVISITED or REVERSED with explanation.
3. **Review quarterly.** Check revisit triggers against actual data.
4. **Link to source docs.** If a decision is detailed in another EOS doc, reference it.
5. **Date everything.** Future-you will thank present-you.

---

*This is a living document. Update with every significant decision.*
