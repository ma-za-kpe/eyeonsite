# 09 — Roadmap

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Version Philosophy

Each version has three things: scope (what's in), success metrics (how we know it works), and kill criteria (when to stop). If kill criteria are met, stop spending time and money. No sunk cost reasoning.

---

## V0 — Manual Validation (Weeks 1–4)

### What It Is
You are the inspector. You are also the platform. WhatsApp + Google Forms + manual Stripe invoice. Zero code. Pure demand validation.

### Scope
- Join 10+ diaspora Facebook/WhatsApp groups (UK-Ghana, US-Ghana, Canada-Ghana corridors)
- Post offering: "Building a house back home? I'll physically verify your construction progress for $20–$50"
- Client requests task via WhatsApp or Google Form (location, what to check, budget)
- You (Maku) travel to site, take GPS-stamped photos (manual GPS app), complete checklist (Google Form)
- Deliver evidence package as PDF via email/WhatsApp
- Invoice via Stripe (Manti Labs LLC) or manual mobile money
- Collect feedback: what was valuable? What was missing? Would they pay more? Would they use it monthly?

### Tools
- WhatsApp Business (client communication)
- Google Forms (task intake + checklist)
- Google Photos or GPS Camera app (GPS-stamped photos)
- Canva or Google Docs (evidence report template)
- Stripe invoicing (diaspora payment)
- Mobile money (local payment)
- Google Sheets (tracking: tasks, revenue, feedback)

### Success Metrics
- 10–15 completed manual tasks
- 5+ unique paying clients
- 3+ clients willing to use monthly/recurring
- Average task value: $20–$50
- Client NPS > 7 (out of 10)
- At least 2 enterprise inquiries (fintech, insurer, property manager)

### Kill Criteria
- < 5 people willing to try after reaching out to 20+ diaspora contacts
- Nobody willing to pay > $15 per task
- 0 repeat interest (nobody wants monthly verification)
- Feedback consistently says "I'd just ask my cousin / the contractor directly"

### What V0 Validates
1. Is there demand? (Will diaspora people pay for this?)
2. What's the price ceiling? (How much will they pay?)
3. What's the delivery format? (PDF report? Photo gallery? Real-time?)
4. Which vertical triggers most demand? (Construction? Property? Land?)
5. What information do clients actually want? (What questions do they ask?)

---

## V1 — MVP App (Months 2–5)

### What It Is
First KMP app. Accra only. Construction vertical only. Android-first with basic iOS. Real escrow. Real inspector matching. Real evidence capture.

### Scope

**Client features:**
- Phone OTP registration (Firebase Auth)
- Create construction progress task (location pin, instructions, budget)
- Fund escrow (Stripe for diaspora, Flutterwave MoMo for local)
- View matched inspector profile
- Review evidence package (photo gallery, checklist, GPS overlay)
- Accept / Request Revision / Dispute
- Rating and review

**Inspector features:**
- Phone OTP registration + ID upload + training module + test task
- Map view of available tasks
- Accept task with ETA
- In-app capture mode (CameraX/AVFoundation, GPS overlay, checklist)
- Submit evidence
- Earnings dashboard
- Payout to MTN MoMo

**Platform features:**
- Cloud Functions: matching, escrow, evidence validation, notifications
- Basic admin dashboard (Compose for Web): task list, dispute queue, inspector list
- Landing page (Compose for Web on Firebase Hosting)

**Technical:**
- KMP shared module: domain models, repository interfaces, use cases
- SQLDelight offline cache for evidence capture
- Firestore real-time sync for task status
- Cloud Vision API for photo quality scoring
- Basic fraud signals (GPS match, EXIF consistency)
- FCM push notifications
- GitHub Actions CI/CD

### Success Metrics
- 50–100 completed tasks
- 15+ active inspectors in Accra (at least 5 consistently active)
- < 20% dispute rate
- Average evidence quality score > 0.70
- Average client rating > 4.0
- 10+ repeat clients (2+ tasks)
- 5+ enterprise leads

### Kill Criteria
- > 30% dispute rate after 50 completed tasks
- Unable to recruit/retain 10 active inspectors after 2 months
- Average evidence quality score < 0.50 (inspectors aren't delivering)
- < 3 repeat clients (no stickiness)

---

## V2 — Multi-City, Multi-Vertical (Months 6–10)

### What It Is
Prove the model replicates. Launch city #2 (Lagos or Nairobi). Add property and agriculture verticals. Full iOS via Compose Multiplatform.

### Scope

**New markets:**
- City #2: Lagos (largest diaspora demand, competitive but bigger TAM) OR Nairobi (less competition, strong M-Pesa infrastructure)
- Decision based on V1 demand signals (which diaspora corridor showed most organic interest)

**New verticals:**
- Property verification (condition checks, occupancy verification, boundary markers)
- Agriculture/farm monitoring (crop checks, irrigation, input materials)
- Vertical template system: configurable checklists, photo requirements, report formats per vertical

**Product enhancements:**
- Video capture (up to 2 min, compressed, with GPS)
- Recurring tasks (monthly construction check = auto-scheduled)
- Project view (group related tasks for one property/building)
- WhatsApp notifications (opt-in, via Twilio/360dialog)
- Client web portal (Compose for Web — view tasks, review evidence, manage projects)
- Inspector web portal (fallback for inspectors without the app)

**Technical:**
- Compose Multiplatform iOS (full CMP, not just shared logic)
- Enhanced fraud detection (perceptual hashing, velocity checks)
- Report generation (PDF evidence report per task)
- Analytics dashboard (city-level, vertical-level metrics)
- Automated reconciliation

### Success Metrics
- 500+ total completed tasks (across both cities)
- City #2 reaches 50 tasks within 2 months of launch
- 50+ active inspectors (combined)
- 3+ verticals with 20+ tasks each
- < 15% dispute rate
- 3+ enterprise pilot clients
- Monthly revenue: $1,500–$3,000

### Kill Criteria
- City #2 fails to reach 50 tasks within 2 months
- Inspector recruitment in city #2 stalls below 10
- Dispute rate in city #2 exceeds 25% (market/cultural mismatch)

---

## V3 — Enterprise + API + Fundraise (Months 11–18)

### What It Is
Enterprise clients (fintechs, insurers, NGOs) get API access and subscription plans. Revenue scales beyond per-task consumer model. This is the fundraise version.

### Scope

**Enterprise features:**
- REST API for task creation and evidence retrieval (API keys, rate limiting, webhooks)
- Bulk task upload (CSV/API batch)
- Subscription tiers ($99/$299/$999 per month, includes task quotas)
- Custom reporting (branded reports, custom fields, SLA guarantees)
- Dedicated account manager (Maku, until ops hire)
- SLA: 24-hour task completion, 4-hour for urgent

**Consumer enhancements:**
- Live video inspection (WebRTC, client watches in real-time)
- AI-generated evidence summary (LLM summarizes checklist + photos into narrative report)
- Inspector profiles with portfolio (past work samples, redacted)
- In-app messaging (inspector ↔ client, moderated)

**Technical:**
- API gateway (Cloud Functions + Firebase App Check)
- Webhook delivery system (event notifications to enterprise clients)
- LLM integration for report generation (Vertex AI or Claude API)
- WebRTC for live video (V3.5, complex — may defer)
- Enhanced admin dashboard (revenue analytics, cohort analysis, LTV tracking)

**Business:**
- Fundraise: $200K–$500K pre-seed/angel
- Pitch: V1+V2 traction data, enterprise pipeline, unit economics proof
- Targets: MEST alumni network, Norrsken, Founders Factory Africa, DFS Lab, angel investors in diaspora fintech space

### Success Metrics
- 2,000+ total completed tasks
- 5+ paying enterprise clients
- $5,000+ MRR
- API generating 20%+ of task volume
- Fundraise closed
- 3+ cities operational
- Team: Maku + 1 ops hire + 1 engineering hire

### Kill Criteria
- Enterprise clients churn after pilot (< 3 retain after 3 months)
- MRR stalls below $2,000 for 3 consecutive months
- Fundraise fails after 50+ investor conversations

---

## V4 — Pan-African Scale (Year 2–3)

### What It Is
10+ cities. Smart evidence (drones, AR overlays, AI construction progress tracking). Data becomes a product.

### Scope
- Expand to 10+ cities across Ghana, Nigeria, Kenya, Uganda, Tanzania, South Africa
- Drone integration for large-site inspections (construction, agriculture, solar)
- AR overlay: inspector points phone at building, app overlays previous visit's photos for comparison
- AI construction progress tracking: auto-detect stage from photos (foundation, walls, roofing, finishing)
- EyeOnSite Score™: standardized verification score (like a credit score but for physical asset verification)
- Data API: aggregate anonymized verification data sold to insurers, lenders, real estate platforms
- White-label SDK: other platforms embed EyeOnSite verification into their flows

### Success Metrics
- 10,000+ tasks/month
- $50K+ MRR
- Data API revenue > 15% of total
- 200+ active inspectors
- Present in 4+ countries

---

## V5 — Verification Infrastructure Layer (Year 3–5)

### What It Is
EyeOnSite becomes the trust verification infrastructure for Africa. Every property purchase, every loan, every insurance claim, every development project uses EyeOnSite evidence.

### Scope
- Government partnerships (land registry verification, building permit compliance)
- Cross-border: expand beyond Africa (Southeast Asia, Latin America — same diaspora trust problem)
- White-label platform: property companies, banks, insurers run their own branded verification on EyeOnSite rails
- Insurance partnerships: inspection reports directly integrated into claims processing
- EyeOnSite as a verb: "Did you EyeOnSite that property before buying?"

---

## Version Dependencies

```
V0 validates → demand exists, pricing works, which vertical first
    │
    ▼
V1 validates → app loop works, inspectors deliver quality, escrow is trusted
    │
    ▼
V2 validates → model replicates to new city, new verticals work
    │
    ▼
V3 validates → enterprise revenue works, API scales, fundraise-ready
    │
    ▼
V4 → scale execution (requires capital from V3 fundraise)
    │
    ▼
V5 → platform play (requires V4 network effects)
```

---

*See [EOS-11-EXECUTION-PLAN.md](./EOS-11-EXECUTION-PLAN.md) for week-by-week execution details.*
*See [EOS-10-BUSINESS-MODEL.md](./EOS-10-BUSINESS-MODEL.md) for revenue model per version.*
