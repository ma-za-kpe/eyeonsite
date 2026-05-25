# EyeOnSite — Product Bible

**On-demand trust verification for Africa's diaspora economy**
Manti Labs · May 2026 · v0.1-DRAFT

---

## Document Index

| #  | Document | Purpose | Status |
|----|----------|---------|--------|
| 00 | README (this document) | Navigation, principles, naming, status | DRAFT |
| 01 | Vision & Market Context | Why EyeOnSite exists, $100B remittance trust gap, competitive landscape | OUTLINED |
| 02 | Product Design | Core concepts, task lifecycle, evidence model, trust graph, vertical templates | OUTLINED |
| 03 | Architecture | KMP module structure, Firebase services, offline-first design, evidence pipeline | OUTLINED |
| 04 | Technical Stack | Kotlin Multiplatform, Compose Multiplatform, Firebase, Stripe/Flutterwave, GCP | OUTLINED |
| 05 | Task & Evidence Specifications | Task manifest, evidence schema, GPS/EXIF verification, report templates | OUTLINED |
| 06 | Inspector Specification | Inspector onboarding, tiers, verification, payout model, fraud detection | OUTLINED |
| 07 | Security, Trust & Compliance | Trust model, threat model, escrow regulations, data privacy (Ghana DPA, NDPR, Kenya DPA) | OUTLINED |
| 08 | Vertical Playbooks | Per-vertical specs: construction, real estate, agriculture, fintech, insurance, NGO | OUTLINED |
| 09 | Roadmap (V0 → V5) | Versioned milestones, success metrics, kill criteria | DETAILED |
| 10 | Business Model | Pricing, GTM, customer segments, unit economics, anti-leakage | OUTLINED |
| 11 | Execution Plan | Week 1, Month 1, Month 3, hiring plan, budget | OUTLINED |
| 12 | Risks & Mitigations | Honest risk register with probability, impact, and countermeasures | OUTLINED |
| 13 | Sources & Glossary | Data sources, acronyms, definitions | OUTLINED |

---

## 00 — README

### What Is EyeOnSite?

EyeOnSite is an on-demand trust verification marketplace that connects diaspora investors and enterprise clients with verified local inspectors across African cities. Think Bolt/Uber mechanics — geo-matching, real-time dispatch, escrow, ratings — applied to physical verification tasks: construction progress monitoring, property inspections, farm checks, collateral verification, and more.

### Core Thesis

Africa receives ~$100B in annual remittances. NALA, LemFi, and Flutterwave solved the money movement. Nobody has solved what happens after the money lands. 56% of diaspora investors cite corruption as the #1 barrier to investing back home. EyeOnSite closes the trust gap by providing verified, GPS-stamped, time-stamped, on-the-ground evidence that money is being used as intended.

### Naming

**EyeOnSite** — your eyes on the ground, on the site, on your investment. Simple, memorable, communicates the core value proposition in the name itself. Works across English-speaking African diaspora markets (UK, US, Canada, Australia → Ghana, Nigeria, Kenya, Uganda).

### Principles

1. **Evidence over promises** — every claim is backed by GPS-stamped, time-stamped, in-app-captured media
2. **Inspector-first** — inspectors are the product; their quality determines everything
3. **Offline-capable** — African mobile networks are unreliable; the app must work without connectivity and sync when back online
4. **Escrow-native** — money never moves without verified evidence
5. **Vertical-agnostic core, vertical-specific templates** — the platform is one; the task templates are many
6. **Boring stack** — proven technologies, no bleeding edge in production (KMP is stable since Nov 2023, Firebase is battle-tested, Stripe/Flutterwave handle payments)

---

## 01 — Vision & Market Context

### The $100B Trust Gap

- Africa receives ~$95-100B in annual remittances (World Bank/KNOMAD 2024), growing at ~10% CAGR
- Nigeria: ~$20-23B, Ghana: $4.7-6.6B, Kenya: $4.94B, Egypt: $22.7B
- 600,000 families in Ghana alone depend on remittances for housing, education, healthcare
- Ghanaian diaspora: ~1.5-3M abroad across 50+ countries; 135,000+ in UK alone; ~500K across OECD
- Remittance flows have overtaken FDI in importance for countries like Nigeria and Kenya

### The Pain (Research-Verified)

- Commonwealth Secretariat survey: 56%+ of diaspora investors cite corruption as primary barrier
- Nigeria: estate agent collected ₦21M from 81 people for the same apartment
- Lagos State recovered ₦478.13M and 18 properties from real estate fraud in 2024
- Kenya: diaspora buyer paid Sh4.25M for a never-built maisonette (Mizizi Africa Homes)
- Kenya: US-based investor discovered $57M property had fabricated land registration numbers
- Zimbabwe: construction company received ~$1M, diverted funds entirely
- No mandatory escrow accounts for developers in Kenya — zero accountability on funds
- 70% of real estate purchases in Lagos in 2021 were by diaspora Nigerians — mostly sight unseen

### Competitive Landscape

**Managed Services (Africa-focused, not marketplace model):**
- LandSafe (Nigeria) — property verification in Abuja, Lagos, Ogun; 24-48hr reports; AI + vetted lawyers
- Mantel Verify (Nigeria) — premium diaspora verification; technology + legal + on-ground
- LANDIZ (Nigeria) — diaspora team, remote verification, escrow + diaspora mortgages
- The Diaspora Access (Kenya) — property management, searches, inspections for overseas clients

**Gig Marketplace Model (US-focused, not Africa):**
- WeGoLook (Crawford & Company) — 45,000+ "Lookers," enterprise insurance claims. Founded 2009, acquired 2016 for undisclosed amount after raising $3.35M. The exact model we're adapting.
- iVueit — 30,000+ "Vuers," commercial property/facilities management across US
- ProxyPics — on-demand property photography, $22-$125/task, LiDAR/3D/360° support

**White Space:** No one is running the Uber/Bolt marketplace model for physical verification in African markets, serving diaspora consumers AND enterprise clients. Every African player is a managed service (hire us, we'll send someone). Every marketplace player is US/Western-focused.

### Why Now

- KMP is stable (declared stable Nov 2023 by JetBrains; Google co-investing since 2022)
- Mobile money penetration across Africa makes inspector payouts viable
- Diaspora digital marketing is highly targetable (Facebook/Instagram groups, WhatsApp communities)
- Post-COVID normalization of remote oversight
- Africa's insurance market ($98.5B in 2025, growing to $166B by 2034) needs field verification
- Africa's fintech lending explosion (Nigeria: 40M MSMEs, Ethiopia: 50B+ ETB in digital MSME loans) needs collateral checks

---

## 02 — Product Design

### Core Concepts

| Concept | Definition |
|---------|------------|
| **Task** | A discrete verification job posted by a client with specific instructions, location, deadline, and budget |
| **Evidence Package** | The structured deliverable: GPS-stamped photos, video, structured report, inspector notes |
| **Inspector** | A verified local agent who completes tasks; tiered by capability (Basic, Professional, Expert) |
| **Client** | The person or organization posting tasks; can be diaspora individual or enterprise (fintech, insurer, NGO) |
| **Escrow** | Client funds held by the platform; released to inspector upon client acceptance of evidence |
| **Vertical Template** | Pre-configured task structure for a specific use case (construction checklist, property assessment, crop report) |
| **Trust Score** | Composite rating for inspectors: task completion rate, evidence quality, client ratings, dispute history |

### Task Lifecycle

```
CLIENT POSTS TASK
    ↓
[Task Created] → Location + Instructions + Budget + Deadline + Template
    ↓
[Escrow Funded] → Client funds locked (Stripe for diaspora, MoMo for local)
    ↓
[Inspector Matched] → Geo-proximity ping → Inspector accepts
    ↓
[In Progress] → Inspector travels to site, opens in-app capture
    ↓
[Evidence Captured] → GPS-stamped photos (in-app only), structured checklist, video, notes
    ↓
[Evidence Submitted] → Auto-validated (GPS match, EXIF check, timestamp, AI quality check)
    ↓
[Client Review] → Client has 48hrs to accept, request revision, or dispute
    ↓
[Accepted] → Escrow released to inspector → Both parties rate each other
    ↓
[Disputed] → Platform mediation → Resolution within 7 days
```

### Evidence Model

Every evidence package contains:
- **Mandatory:** GPS coordinates (verified against task location within 200m radius), capture timestamp (server-verified), device fingerprint, 5+ photos from required angles (per template), structured checklist completion
- **Optional:** Video walkthrough, live stream recording, document scans, audio notes, 360° photos
- **Fraud Signals:** GPS spoofing detection, EXIF consistency check, AI duplicate/stock photo detection, device motion data during capture, network location cross-reference

### Trust Graph

```
Inspector Trust Score = weighted(
  completion_rate: 0.25,      // % of accepted tasks completed
  evidence_quality: 0.25,     // AI-scored photo/report quality
  client_rating: 0.20,        // Average client rating (1-5)
  dispute_rate: 0.15,         // Inverse of disputes/total tasks
  response_time: 0.10,        // Average time to accept tasks
  tenure: 0.05                // Time on platform
)
```

Client Trust Score tracks: payment reliability, dispute initiation rate, review fairness.

---

## 03 — Architecture

### High-Level Architecture

```
┌──────────────────────────────────────────────┐
│                 CLIENTS                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ Android  │ │   iOS    │ │  Web (Admin)  │  │
│  │ (Compose)│ │(SwiftUI) │ │  (Next.js)   │  │
│  └────┬─────┘ └────┬─────┘ └──────┬───────┘  │
│       └─────────┬──┘              │           │
│           ┌─────┴─────┐          │           │
│           │  KMP Shared │          │           │
│           │   Module    │          │           │
│           └─────┬─────┘          │           │
└─────────────────┼────────────────┼───────────┘
                  │                │
┌─────────────────┼────────────────┼───────────┐
│              BACKEND (Firebase + GCP)         │
│  ┌──────────────┐ ┌────────────────────────┐  │
│  │ Firestore    │ │ Cloud Functions (v2)   │  │
│  │ (tasks, users│ │ - Task matching        │  │
│  │  evidence,   │ │ - Escrow management    │  │
│  │  ratings)    │ │ - Evidence validation  │  │
│  └──────────────┘ │ - Notification dispatch│  │
│  ┌──────────────┐ │ - Fraud detection      │  │
│  │ Cloud Storage│ │ - Report generation    │  │
│  │ (photos,     │ └────────────────────────┘  │
│  │  videos,     │ ┌────────────────────────┐  │
│  │  reports)    │ │ Payments               │  │
│  └──────────────┘ │ - Stripe (diaspora)    │  │
│  ┌──────────────┐ │ - Flutterwave (local)  │  │
│  │ Firebase Auth│ │ - MTN MoMo / M-Pesa   │  │
│  │ (phone OTP)  │ └────────────────────────┘  │
│  └──────────────┘ ┌────────────────────────┐  │
│  ┌──────────────┐ │ Firebase Cloud         │  │
│  │ Realtime DB  │ │ Messaging (FCM)        │  │
│  │ (live chat,  │ │ - Task alerts          │  │
│  │  presence)   │ │ - Status updates       │  │
│  └──────────────┘ └────────────────────────┘  │
└───────────────────────────────────────────────┘
```

### KMP Shared Module Structure

```
shared/
├── commonMain/
│   ├── domain/
│   │   ├── model/          (Task, Inspector, Evidence, Client, EscrowTransaction)
│   │   ├── repository/     (interfaces: TaskRepository, EvidenceRepository, etc.)
│   │   └── usecase/        (CreateTask, AcceptTask, SubmitEvidence, ReleaseEscrow)
│   ├── data/
│   │   ├── remote/         (Firebase Firestore, Cloud Functions API)
│   │   ├── local/          (SQLDelight for offline cache)
│   │   └── sync/           (Offline queue, conflict resolution)
│   ├── evidence/
│   │   ├── validation/     (GPS verification, EXIF checks, quality scoring)
│   │   └── capture/        (Photo capture flow, checklist engine)
│   └── util/               (DateUtils, GeoUtils, CurrencyConverter)
├── androidMain/
│   ├── camera/             (CameraX integration)
│   ├── location/           (Fused Location Provider)
│   └── payment/            (Flutterwave Android SDK)
├── iosMain/
│   ├── camera/             (AVFoundation)
│   ├── location/           (CoreLocation)
│   └── payment/            (Stripe iOS SDK)
└── jsMain/                 (Web admin dashboard shared logic)
```

### Offline-First Design

Critical for African mobile networks:
- **SQLDelight** local database for task queue, evidence drafts, inspector profiles
- **Evidence capture works fully offline** — photos stored locally with GPS/timestamp, queued for upload
- **Sync engine** — background upload when connectivity returns, conflict resolution via server timestamp
- **Optimistic UI** — task acceptance, evidence submission reflected immediately, reconciled on sync

---

## 04 — Technical Stack

### The Boring Stack Principle

Every technology choice optimizes for: proven in production, strong community, you (Maku) can ship it solo, works in low-connectivity African environments.

| Layer | Technology | Why |
|-------|-----------|-----|
| **Shared Logic** | Kotlin Multiplatform (KMP) | Stable since Nov 2023. You write Kotlin daily at Farmerline. Share 60-70% of code across platforms. |
| **Android UI** | Jetpack Compose | Native Android, you know it cold from farmmanagement module |
| **iOS UI** | Compose Multiplatform (or SwiftUI) | CMP for maximum sharing; SwiftUI if native feel is priority for V2 |
| **Web Admin** | Next.js + TypeScript | Your proven stack (SikaWise, StartupTribunal, GapSense) |
| **Backend** | Firebase (Auth, Firestore, Functions v2, Storage, FCM) | Serverless, scales to zero cost at low volume, you've shipped with it before |
| **Database** | Cloud Firestore + SQLDelight (offline) | Real-time sync + offline persistence |
| **Payments (Diaspora)** | Stripe | Manti Labs LLC already on Stripe Atlas |
| **Payments (Local)** | Flutterwave | Covers MTN MoMo, M-Pesa, bank transfers across Ghana/Nigeria/Kenya |
| **Maps** | Google Maps SDK (KMP wrapper) | Reliable across Africa, good geocoding |
| **CI/CD** | GitHub Actions | Your existing infrastructure |
| **Hosting (Web)** | Vercel | Your proven deployment target |
| **Cloud Functions** | GCP Cloud Functions v2 (Python/Node) | Task matching, escrow logic, fraud detection, report generation |
| **AI/ML** | Cloud Vision API | Photo quality scoring, duplicate detection, OCR for documents |
| **Monitoring** | Firebase Crashlytics + Analytics | Free, integrated, sufficient for V0-V2 |

---

## 05 — Task & Evidence Specifications

### Task Manifest Schema

```kotlin
data class TaskManifest(
    val id: String,                    // ULID
    val clientId: String,
    val verticalType: VerticalType,    // CONSTRUCTION, PROPERTY, AGRICULTURE, FINTECH, INSURANCE, NGO
    val templateId: String,            // Links to VerticalTemplate
    val location: GeoPoint,            // Latitude/Longitude of site
    val locationRadius: Int,           // Meters — inspector must be within this radius to capture
    val address: String,               // Human-readable address
    val title: String,                 // "Monthly progress check — Tema house build"
    val instructions: String,          // Client's specific instructions
    val checklist: List<ChecklistItem>,// Structured items to verify
    val requiredPhotos: Int,           // Minimum photo count
    val requiredPhotoAngles: List<PhotoAngle>, // e.g., FRONT, BACK, LEFT, RIGHT, INTERIOR, AERIAL
    val videoRequired: Boolean,
    val liveStreamRequired: Boolean,
    val deadlineAt: Instant,
    val budget: Money,                 // Amount in escrow
    val currency: Currency,            // GHS, NGN, KES, UGX, USD, GBP, EUR
    val inspectorTierRequired: InspectorTier, // BASIC, PROFESSIONAL, EXPERT
    val status: TaskStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class TaskStatus {
    DRAFT, POSTED, ESCROW_FUNDED, INSPECTOR_MATCHED,
    IN_PROGRESS, EVIDENCE_SUBMITTED, UNDER_REVIEW,
    ACCEPTED, DISPUTED, REVISION_REQUESTED, CANCELLED, EXPIRED
}
```

### Evidence Package Schema

```kotlin
data class EvidencePackage(
    val id: String,
    val taskId: String,
    val inspectorId: String,
    val captureStartedAt: Instant,
    val captureCompletedAt: Instant,
    val location: GeoPoint,            // Inspector's GPS at time of capture
    val locationAccuracy: Float,       // Meters
    val distanceFromTaskSite: Float,   // Meters — must be < task.locationRadius
    val photos: List<EvidencePhoto>,
    val videos: List<EvidenceVideo>,
    val checklistResponses: List<ChecklistResponse>,
    val inspectorNotes: String,
    val deviceInfo: DeviceInfo,        // Device model, OS, app version
    val fraudSignals: FraudSignals,    // Auto-populated by validation pipeline
    val qualityScore: Float,           // 0.0-1.0, AI-assessed
    val status: EvidenceStatus,
)

data class EvidencePhoto(
    val id: String,
    val storageUrl: String,
    val thumbnailUrl: String,
    val capturedAt: Instant,
    val gpsLocation: GeoPoint,
    val angle: PhotoAngle,
    val exifData: Map<String, String>,
    val capturedInApp: Boolean,        // Must be true — no camera roll uploads
    val aiQualityScore: Float,         // Blur, exposure, framing
    val aiDuplicateScore: Float,       // Similarity to stock/previous photos
)

data class FraudSignals(
    val gpsConsistency: Boolean,       // GPS matches task location
    val exifConsistency: Boolean,      // EXIF timestamp matches submission time
    val deviceMotionDuringCapture: Boolean, // Accelerometer data shows physical movement
    val networkLocationMatch: Boolean,  // IP geolocation consistent with GPS
    val aiStockPhotoScore: Float,      // 0.0-1.0 — likelihood of stock/reused photo
    val capturedViaApp: Boolean,       // Photos taken within EyeOnSite app
)
```

---

## 06 — Inspector Specification

### Inspector Tiers

| Tier | Requirements | Task Types | Payout Range |
|------|-------------|------------|-------------|
| **Basic** | Phone + ID verification, background check, training video completion, test task | Photo tasks, simple checklists, delivery confirmation | $10-$30/task |
| **Professional** | Basic + engineering/surveying background OR 50+ completed Basic tasks with 4.5+ rating | Construction progress, property assessments, agricultural monitoring | $30-$80/task |
| **Expert** | Professional + licensed surveyor/engineer/architect certificate + platform certification | Legal property verification, insurance claims, structural assessments | $80-$200/task |

### Inspector Onboarding Flow

1. Download app → Phone OTP registration (Firebase Auth)
2. ID verification (national ID/passport scan + selfie match)
3. Background check (partnered with local verification service)
4. Training module completion (15-min video + quiz per vertical)
5. Test task (assigned, simulated, evaluated by platform team)
6. Activation → begins receiving task pings

### Fraud Detection (Inspector Side)

- Multiple account detection (device fingerprint, phone number linkage)
- GPS spoofing detection (compare GPS, network location, WiFi triangulation)
- Photo reuse detection (perceptual hashing against previous submissions + stock photo databases)
- Velocity checks (impossible travel between task locations)
- Collusion detection (same inspector repeatedly matched with same client, unusual acceptance patterns)
- Quality degradation alerts (declining photo quality, shorter time on site)

---

## 07 — Security, Trust & Compliance

### Trust Model

Three layers of trust:
1. **Identity Trust** — verified real person (ID + biometric + background check)
2. **Evidence Trust** — verified real evidence (GPS + timestamp + in-app capture + AI validation)
3. **Financial Trust** — verified money safety (escrow + dispute resolution + insurance)

### Regulatory Considerations

| Jurisdiction | Key Regulation | Implication |
|-------------|---------------|-------------|
| Ghana | Data Protection Act 2012 | DPA registration required; consent for location/photo data |
| Nigeria | NDPR 2019 / Nigeria Data Protection Act 2023 | Data processing registration; cross-border transfer rules |
| Kenya | Data Protection Act 2019 | Registration with ODPC; data localization considerations |
| Uganda | Data Protection and Privacy Act 2019 | Registration with PDPO |
| UK/EU (diaspora users) | UK GDPR / EU GDPR | Consent, data transfer mechanisms (SCCs), right to erasure |
| Escrow/Payments | Each country's payment regulations | May need PSP license or operate through licensed partner (Flutterwave/Stripe) |

### Threat Model (Top 5)

1. **Inspector-contractor collusion** — inspector fakes positive report in exchange for kickback → Mitigation: random re-inspections, AI anomaly detection, whistleblower incentives
2. **GPS spoofing** — inspector submits evidence without being on site → Mitigation: multi-signal location (GPS + network + WiFi + motion), server-side verification
3. **Photo fraud** — stock photos, reused photos, AI-generated images → Mitigation: in-app-only capture, perceptual hashing, EXIF analysis, Cloud Vision API
4. **Escrow manipulation** — client refuses to accept valid evidence to avoid payment → Mitigation: auto-accept after 48hrs if no dispute filed, platform mediation SLA
5. **Account takeover** — compromised inspector accounts used for fraudulent activity → Mitigation: biometric re-verification for high-value tasks, device binding, suspicious activity alerts

---

## 08 — Vertical Playbooks

### Construction Progress Monitoring (V1 Launch Vertical)

**Checklist Template:**
- Foundation stage: footings visible, rebar placement, concrete pour evidence
- Wall stage: block count estimation, mortar quality, plumb/level check
- Roofing stage: truss installation, roofing material verification, drainage
- Finishing: plastering, painting, electrical rough-in, plumbing rough-in
- Each stage: photo from N/S/E/W, interior photo, materials on site, worker count

**Reporting Cadence:** Weekly or bi-weekly, aligned with construction milestones

### Property Verification

**Checklist:** Exterior condition (4 sides), interior (room by room), occupancy status, visible damage, surrounding area, title board/signage, boundary markers

### Agriculture & Farm Monitoring

**Checklist:** Crop stage (seedling/vegetative/flowering/harvest), irrigation status, pest/disease visible, input bags/containers on site, worker activity, storage condition, harvest quantity estimation

### Fintech Collateral Verification

**Checklist:** Business exists at stated address, signage visible, inventory visible, customer activity, equipment condition, owner/manager present, business registration certificate visible

### Insurance Claims Assessment

**Checklist:** Damage type and extent (photos from multiple angles), cause assessment, surrounding area condition, existing repairs, claimant present, witnesses

### NGO Project Monitoring

**Checklist:** Project exists at location, construction/installation stage, materials on site, community engagement visible, beneficiary count, signage/branding, quality assessment

---

## 09 — Roadmap (V0 → V5)

This is the core versioning document. Each version has: scope, success metrics, kill criteria (when to stop and reassess), and what unlocks the next version.

---

### V0 — Manual Validation ("Be the Inspector")

**Timeline:** Weeks 1-4
**What it is:** You, Maku, are the inspector. No app. WhatsApp + Google Forms + manual escrow.
**Goal:** Validate willingness to pay and task quality expectations before writing a line of code.

**Scope:**
- Source 10-15 diaspora clients manually (UK-Ghana WhatsApp groups, MEST alumni, Ghanaian churches in London)
- Accept task requests via WhatsApp
- Visit construction sites in Accra personally
- Capture GPS-stamped photos with a smartphone
- Deliver evidence via WhatsApp photo albums + Google Docs structured report
- Collect payment via manual Stripe invoice (Manti Labs LLC)
- Document every interaction, pain point, question, objection

**Deliverables:**
- 10-15 completed manual inspections with full evidence packages
- Client interview recordings (what did they expect? what surprised them? would they pay again?)
- Pricing validation (test $25, $50, $75 price points)
- First 3-5 case studies with real photos and testimonials
- Standardized checklist (what clients actually care about vs. what you assumed)

**Success Metrics:**
- 10+ completed tasks
- 70%+ would pay again (self-reported)
- Average willingness to pay > $30/task
- 3+ unsolicited referrals

**Kill Criteria:**
- < 5 people willing to try the service after 20+ outreach attempts → pivot thesis
- 0 repeat requests → pain isn't recurring enough for a marketplace
- Average WTP < $15 → unit economics won't work

**What it unlocks:** Proven demand + real evidence templates → justify building V1

---

### V1 — MVP App (Single City, Single Vertical)

**Timeline:** Months 2-5
**What it is:** First KMP mobile app. Android-first (dominant in Africa), with basic iOS. Accra only. Construction monitoring only.
**Goal:** Prove the marketplace mechanics work — matching, evidence capture, escrow, ratings.

**Scope — Client App:**
- Phone OTP registration (Firebase Auth)
- Task creation with construction template (location pin, instructions, budget, deadline)
- Escrow funding (Stripe for diaspora in UK/US, Flutterwave for MTN MoMo)
- Real-time task status tracking
- Evidence review with accept/revise/dispute actions
- Inspector rating (1-5 stars + written review)
- In-app messaging with assigned inspector

**Scope — Inspector App:**
- Phone OTP + ID verification registration
- Location-based task discovery (nearby available tasks)
- Task acceptance with ETA commitment
- In-app photo capture (GPS-stamped, time-stamped, mandatory)
- Structured construction checklist with photo attachment per item
- Evidence submission with summary notes
- Payout tracking (Flutterwave to MoMo/bank account)
- Earnings dashboard

**Scope — Platform (Admin Web Dashboard):**
- Task monitoring (all active tasks, statuses)
- Dispute queue with mediation tools
- Inspector management (onboarding, activation, suspension)
- Basic analytics (tasks/day, completion rate, dispute rate, revenue)
- Manual escrow release override

**Scope — Backend (Firebase + Cloud Functions):**
- Geo-proximity task matching (notify inspectors within X km)
- Escrow hold/release logic
- Evidence validation pipeline (GPS check, EXIF check, photo count check)
- Push notifications (FCM) for task alerts, status updates
- Basic fraud signals (GPS consistency, capture-in-app verification)

**NOT in V1:**
- iOS Compose Multiplatform UI (use basic SwiftUI wrapper around shared KMP logic)
- Video capture
- Live streaming
- AI photo quality scoring
- Multiple verticals
- Enterprise API
- Multiple cities

**Success Metrics:**
- 50-100 completed tasks
- < 10% dispute rate
- 60%+ client repeat rate within 90 days
- Inspector acceptance-to-completion rate > 85%
- NPS > 50
- Average task completion time < 24 hours from inspector match

**Kill Criteria:**
- Dispute rate > 30% after 50 tasks → evidence quality model is broken
- Inspector retention < 40% after month 2 → payout/experience is wrong
- < 20 organic tasks in month 3 (excluding subsidized) → demand isn't there

**What it unlocks:** Working marketplace mechanics + real user data → expand to V2

---

### V2 — Multi-City, Multi-Vertical, Video

**Timeline:** Months 6-10
**What it is:** Expand beyond Accra. Add second vertical. Add video evidence. Improve iOS experience.
**Goal:** Prove the model replicates across cities and verticals.

**Scope — New Markets:**
- City #2: Lagos (Nigeria) or Nairobi (Kenya) — decision based on V1 inbound demand data
- City #3: Kampala (Uganda) — your Ugandan roots + diaspora corridor
- Each city launch: recruit 20-30 inspectors, partner with local university/polytechnic, target diaspora groups in UK/US

**Scope — New Verticals:**
- Real Estate / Property Management (natural extension — same inspectors, same diaspora clients)
- Agriculture / Farm Monitoring (strong in Kenya/Uganda; you understand this from Farmerline)
- Each vertical: dedicated checklist template, inspector training module, vertical-specific report format

**Scope — Product Enhancements:**
- Video capture and upload (max 2 min per task, compressed on-device)
- Compose Multiplatform iOS UI (replace basic SwiftUI wrapper with shared Compose)
- Inspector tier system: Basic → Professional (50+ tasks, 4.5+ rating)
- Recurring tasks (schedule weekly/monthly inspections for ongoing projects)
- Multi-milestone projects (client creates a project with multiple sequential tasks, e.g., foundation → walls → roof → finish)
- Improved offline sync (evidence upload queue with retry, progress indicator)
- Client-side project dashboard (all tasks for one property/project in one view)

**Scope — Trust & Safety:**
- AI photo quality scoring (Cloud Vision API — blur detection, exposure, framing)
- Random re-inspection program (5% of tasks get a second inspector for quality assurance)
- Inspector certification badges (visible in profile, unlocks higher-tier tasks)
- Dispute resolution SLA: < 72 hours for standard disputes

**Scope — Backend:**
- Multi-currency escrow (handle GHS ↔ USD, NGN ↔ GBP, KES ↔ USD conversions)
- Task matching v2 (factor in inspector tier, rating, vertical specialization, distance, availability)
- Analytics v2 (cohort analysis, city-level metrics, vertical-level metrics)
- WhatsApp integration (task status updates via WhatsApp for clients who prefer it)

**Success Metrics:**
- 500+ completed tasks across 2-3 cities
- 3 verticals active with tasks in each
- < 8% dispute rate
- Monthly revenue > $5,000
- 1+ enterprise pilot initiated
- Inspector network > 100 across cities

**Kill Criteria:**
- City #2 fails to reach 50 tasks in first 2 months → replication model is broken
- New vertical < 20% of task volume after 3 months → market pull isn't there
- Monthly burn rate > 3x monthly revenue with no improvement trend → unsustainable

**What it unlocks:** Multi-city proof + vertical diversification → ready for enterprise + fundraise

---

### V3 — Enterprise B2B + API + Fundraise

**Timeline:** Months 11-18
**What it is:** Add enterprise clients (fintechs, insurers, NGOs). Build API for integration. Raise pre-seed/seed.
**Goal:** Prove enterprise revenue model alongside consumer marketplace. Achieve seed-stage metrics.

**Scope — Enterprise Features:**
- Enterprise dashboard (multi-user, role-based access, bulk task creation)
- API for programmatic task creation (fintech lender posts 50 collateral checks via API)
- Webhook notifications (task status updates pushed to client's system)
- Enterprise subscription plans ($99-$999/month based on volume)
- Custom report formats (branded reports for enterprise clients)
- SLA tiers (guaranteed response times: 4hr, 12hr, 24hr)
- Inspector certification for enterprise verticals (insurance-specific, banking-specific)

**Scope — New Verticals:**
- Fintech / MSME Lending Verification (B2B — partner with 2-3 fintechs in Nigeria/Kenya)
- Insurance Claims Assessment (B2B — partner with 1-2 insurers)
- NGO / Development Project Monitoring (partner with 1-2 diaspora organizations or foundations)

**Scope — Product Enhancements:**
- Live video streaming (client watches inspector on site in real-time via WebRTC)
- AI-generated report summaries (LLM processes checklist + photos → readable report for client)
- Inspector leaderboard and gamification (monthly top performers, bonus pool)
- Referral program (inspectors recruit inspectors, clients recruit clients)
- Multi-language support (English, French — for West Africa francophone expansion)

**Scope — Trust & Safety:**
- AI photo fraud detection (stock photo matching, AI-generated image detection, perceptual hashing against historical evidence)
- Inspector background check v2 (criminal records where available, professional license verification)
- Escrow insurance (platform backs a portion of disputed amounts to maintain client trust)
- Automated quality scoring per inspector (trend analysis, early warning for declining quality)

**Scope — Data Product (Early):**
- Anonymized inspection data dashboard (aggregate construction progress, property conditions, agricultural yields by region)
- API for data access (sell to real estate platforms, credit bureaus, development agencies)

**Fundraise Target:** $200K-$500K pre-seed or seed on:
- Multi-city traction (3-5 cities, 1,000+ tasks)
- Blended consumer + enterprise revenue
- Clear unit economics (revenue per task > fully-loaded cost)
- Replicable city launch playbook

**Success Metrics:**
- 2,000+ completed tasks across 3-5 cities
- 5+ enterprise contracts (recurring revenue)
- Monthly revenue > $15,000
- Enterprise revenue > 30% of total
- API processing > 100 tasks/month
- Inspector network > 300

**Kill Criteria:**
- < 3 enterprise contracts after 6 months of outreach → enterprise model doesn't work
- Monthly revenue plateaus below $8,000 for 3 consecutive months → growth engine is broken
- Unable to raise seed after 3+ investor conversations → market doesn't believe in the model

**What it unlocks:** Enterprise revenue + funding → scale team + infrastructure for V4

---

### V4 — Pan-African Scale + Smart Evidence

**Timeline:** Months 19-30 (Year 2-3)
**What it is:** Scale to 10+ cities, add AI-powered smart evidence, build the data moat.
**Goal:** Become the default verification infrastructure for diaspora investment and enterprise field operations in Africa.

**Scope — Scale:**
- 10+ cities across West and East Africa
- Francophone expansion (Abidjan, Dakar, Douala)
- City launch playbook fully automated (inspector recruitment pipeline, diaspora marketing templates, local partnerships)
- Regional operations leads in West Africa and East Africa

**Scope — Smart Evidence:**
- AR-guided inspection (phone camera overlays checklist on live view — "photograph this angle")
- AI construction progress estimation (compare photos over time, estimate % completion)
- AI crop health assessment (detect pest/disease from photos)
- AI structural concern flagging (identify visible cracks, water damage, settlement issues)
- Meta/smart glasses integration (inspector wears glasses, client sees live POV)
- Drone integration for large-scale sites (partner with local drone operators)

**Scope — Platform:**
- EyeOnSite Score™ — a standardized verification score for properties/projects (like a credit score for physical assets)
- Data API v2 — anonymized, geo-tagged verification data layer across African cities
- Insurance product — offer inspection-linked coverage (property verified by EyeOnSite gets better rates)
- White-label API — banks, insurance companies, property platforms embed EyeOnSite verification into their own products

**Success Metrics:**
- 20,000+ completed tasks
- 10+ cities, 3+ countries
- Monthly revenue > $100,000
- Data API revenue > 10% of total
- Inspector network > 2,000
- EyeOnSite Score adopted by 1+ external platform

---

### V5 — The Verification Layer for Africa

**Timeline:** Year 3-5
**What it is:** EyeOnSite becomes infrastructure — the default trust layer for any physical verification need in Africa.
**Goal:** Platform company, not a marketplace. Other products are built on top of your verification network.

**Scope — Vision:**
- Any business or individual that needs to verify something physical in Africa uses EyeOnSite
- White-label SDK: embed EyeOnSite verification into any app (banking apps, insurance apps, e-commerce apps, government portals)
- EyeOnSite Score™ becomes a standard: property listings without an EyeOnSite Score are suspicious
- Government/institutional partnerships: land registries, municipal inspection departments, development finance institutions
- Cross-border: expand beyond Africa to other diaspora corridors (India, Philippines, Latin America — same trust gap, same model)
- Inspector becomes a professional career path: certified EyeOnSite inspectors are recognized professionals

**What this looks like at scale:**
- 100,000+ inspectors across 50+ cities
- Millions of verification records — the most comprehensive physical data layer in Africa
- Revenue: marketplace commissions + enterprise subscriptions + data licensing + white-label SDK fees + insurance partnerships
- Series A/B company valued on network density + data asset + enterprise contracts

---

## 10 — Business Model

### Revenue Streams

**Primary (V1-V2):**
| Stream | Rate | Description |
|--------|------|-------------|
| Task Commission | 15-20% | On every completed task, charged to client |
| Escrow Fee | 2-3% | Processing fee on escrow hold/release |

**Secondary (V3+):**
| Stream | Rate | Description |
|--------|------|-------------|
| Enterprise Subscription | $99-$999/mo | Volume plans for B2B clients |
| Urgency Pricing | 1.5-2x | Same-day / 4-hour rush inspections |
| Inspector Certification | $20-$50 | Training + certification fee per vertical |
| Data API | Per-query pricing | Anonymized verification data access |
| White-Label SDK | Revenue share | Embed EyeOnSite in third-party apps |

### Unit Economics (Target at V2 Scale)

| Metric | Consumer Task | Enterprise Task |
|--------|--------------|-----------------|
| Average Task Price | $40 | $75 |
| Platform Take Rate | 18% | 15% |
| Revenue Per Task | $7.20 | $11.25 |
| Inspector Payout | $32.80 | $63.75 |
| Payment Processing | ~2.5% ($1.00) | ~2.5% ($1.88) |
| Net Revenue Per Task | $6.20 | $9.37 |
| Support Cost Per Task | ~$1.00 | ~$0.50 |
| Gross Margin Per Task | $5.20 (72%) | $8.87 (79%) |

### Anti-Disintermediation (Leakage Prevention)

The #1 risk in any gig marketplace: client finds a good inspector and goes direct. Countermeasures:
1. **Escrow protection** — no platform = no money safety guarantee
2. **Structured evidence reports** — timestamped, GPS-verified reports serve as legal documentation; can't replicate off-platform
3. **Dispute resolution** — platform mediates conflicts; off-platform, you're on your own
4. **Inspector trust badges** — verified, background-checked, rated; badge only valid on-platform
5. **Recurring task management** — multi-milestone projects, scheduled recurring inspections; painful to coordinate via WhatsApp
6. **Insurance/guarantees** — platform backs a portion of disputes; gone if you go off-platform

---

## 11 — Execution Plan

### Week 1 (You, solo)

- [ ] Join 5 UK-Ghana diaspora Facebook/WhatsApp groups
- [ ] Post in 3 groups about construction oversight pain (conversation starter, not selling)
- [ ] Identify 3 Ghanaian churches in London with active building projects in Ghana
- [ ] Cold-message 10 people who've complained about construction projects online
- [ ] Register eyeonsite.com (or closest available)
- [ ] Set up landing page with waitlist (Next.js on Vercel, Mailchimp for emails)
- [ ] Do your FIRST inspection — pick a construction site in Accra, document it with GPS-stamped photos, write the report
- [ ] Post the case study on LinkedIn + X (your existing audience)

### Month 1 (V0 — Manual)

- [ ] Complete 10-15 manual inspections
- [ ] Interview every client (15 min, recorded)
- [ ] Test 3 price points ($25, $50, $75)
- [ ] Document the standardized checklist (what clients actually ask for)
- [ ] Identify and recruit 5 potential inspectors (engineering students, surveyors, gig workers)
- [ ] Validate escrow mechanics (manual Stripe invoicing → conditional release)
- [ ] Publish 3 case studies with real photos and testimonials
- [ ] Decision: Go/No-Go on V1 development

### Month 2-5 (V1 — MVP Development)

- [ ] KMP project setup (shared module, Android app, iOS shell)
- [ ] Firebase project setup (Auth, Firestore, Functions, Storage, FCM)
- [ ] Core task lifecycle implementation
- [ ] Evidence capture flow (in-app camera, GPS verification)
- [ ] Escrow integration (Stripe + Flutterwave)
- [ ] Inspector onboarding flow
- [ ] Alpha testing with 5-10 existing manual clients
- [ ] Beta launch with 20-30 clients
- [ ] First 50 tasks through the app

### Hiring Plan (When Revenue Justifies)

| Role | When | Why |
|------|------|-----|
| Operations Lead (Accra) | V2 (Month 6) | Inspector recruitment, quality assurance, dispute mediation |
| Frontend Developer | V2 (Month 7) | Accelerate web dashboard, iOS improvements |
| Growth Marketer | V2 (Month 8) | Diaspora acquisition, content marketing, partnerships |
| Backend Engineer | V3 (Month 12) | API, enterprise features, data pipeline |
| City Launcher | V3 (Month 14) | Replicate playbook in new cities |

---

## 12 — Risks & Mitigations

| # | Risk | Probability | Impact | Mitigation |
|---|------|------------|--------|------------|
| 1 | Inspector fraud (fake photos, collusion) | HIGH | CRITICAL | GPS/EXIF verification, in-app capture only, AI duplicate detection, random re-inspections |
| 2 | Marketplace leakage (clients go direct) | HIGH | HIGH | Escrow protection, structured reports, ongoing task management, trust badges |
| 3 | Low inspector quality kills trust | MEDIUM | CRITICAL | Rigorous onboarding, tier system, quality scoring, quick deactivation |
| 4 | Chicken-and-egg (no inspectors = no clients = no inspectors) | HIGH | HIGH | Start manual (you're inspector #1), hyper-focus one city, subsidize early tasks |
| 5 | Regulatory risk (escrow/payments licensing) | MEDIUM | HIGH | Operate through licensed payment partners (Stripe, Flutterwave); legal review before each market |
| 6 | Client disputes over subjective quality | HIGH | MEDIUM | Structured templates (objective checklist), auto-accept timer, clear mediation process |
| 7 | Safety (inspector enters dangerous area or confrontation) | LOW | CRITICAL | Safety protocols, emergency contact system, insurance for inspectors, no-go zone registry |
| 8 | Currency risk (diaspora pays in USD/GBP, inspector paid in GHS/NGN) | MEDIUM | MEDIUM | Settle in local currency at time of escrow funding; absorb FX risk in take rate |
| 9 | Farmerline IP clause conflict | LOW | HIGH | EyeOnSite is clearly outside Farmerline scope; list it formally at hire-time with any new employer |
| 10 | Single founder, single point of failure | HIGH | HIGH | Document everything (this doc), build in public, recruit co-founder or early team member by V2 |

---

## 13 — Sources & Glossary

### Key Data Sources

- World Bank/KNOMAD — Remittance data, bilateral remittance matrices
- Commonwealth Secretariat — Diaspora investment barriers survey
- OECD — Ghana Migration Review (emigrant populations, destinations)
- IMARC Group — Africa Insurance Market Report 2025-2034
- Ghana Statistical Service — Population and migration data
- Nigeria EFCC — Real estate fraud cases
- Kenya Daily Nation — Diaspora property fraud investigations
- Afridigest — Africa remittance market analysis
- ISS Africa Futures — Remittance projections and policy analysis
- TechCrunch — NALA, fintech fundraising data
- WeGoLook / Crawford & Company — Operational model reference
- iVueit / ProxyPics — Gig inspection marketplace mechanics

### Glossary

| Term | Definition |
|------|-----------|
| **KMP** | Kotlin Multiplatform — JetBrains technology for sharing Kotlin code across platforms |
| **CMP** | Compose Multiplatform — shared UI framework built on Jetpack Compose |
| **Escrow** | Third-party holding of funds, released upon task completion verification |
| **Take Rate** | Percentage of transaction value retained by the platform |
| **EXIF** | Exchangeable Image File Format — metadata embedded in photos (GPS, timestamp, camera info) |
| **GPS Spoofing** | Faking GPS coordinates; a key fraud vector for location-verified tasks |
| **NPS** | Net Promoter Score — measure of customer satisfaction/loyalty |
| **WTP** | Willingness To Pay |
| **MSME** | Micro, Small, and Medium Enterprise |
| **NPL** | Non-Performing Loan |
| **MoMo** | MTN Mobile Money — dominant mobile money platform in Ghana |
| **M-Pesa** | Safaricom's mobile money platform — dominant in Kenya |
| **DPA** | Data Protection Act/Authority |
| **NDPR** | Nigeria Data Protection Regulation |
| **FCM** | Firebase Cloud Messaging — push notification service |
| **ULID** | Universally Unique Lexicographically Sortable Identifier |
| **Disintermediation** | Users bypassing the platform to transact directly; "leakage" |
| **Omo Onile** | Yoruba term for land grabbers in Nigeria; major fraud vector in Lagos real estate |

---

*This document is a living spec. Version 0.1 — May 2026. Maintained by Maku Mazakpe / Manti Labs.*
