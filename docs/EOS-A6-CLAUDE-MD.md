# A6 — CLAUDE.md

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

This document IS the CLAUDE.md file for the EyeOnSite monorepo. Copy this verbatim to `eyeonsite/CLAUDE.md` when the repo is initialized.

---

```markdown
# CLAUDE.md — EyeOnSite

## What This Project Is

EyeOnSite is an on-demand verification marketplace connecting diaspora investors with verified local inspectors in African cities. Think Bolt/Uber but for physical verification — GPS-stamped, escrow-backed evidence that construction projects, property, farms, and business collateral are real.

**Built by:** Manti Labs (Maku Mazakpe)
**Stage:** V0 → V1 (pre-product, validating demand manually before building app)

## Architecture

- **Kotlin Multiplatform (KMP)** across ALL surfaces. One language everywhere.
- **Android:** Jetpack Compose
- **iOS:** Compose Multiplatform (CMP)
- **Web (landing page, admin dashboard, client portal):** Compose for Web (Kotlin/JS) on Firebase Hosting
- **Backend:** Cloud Functions v2 (Node.js runtime — Firebase Admin SDK is most mature here)
- **Database:** Cloud Firestore (offline-first with native mobile persistence)
- **Offline cache:** SQLDelight (KMP-native, compiles to SQLite on mobile, IndexedDB on web)
- **Networking:** Ktor Client (KMP-native)
- **Serialization:** kotlinx.serialization
- **Auth:** Firebase Authentication (phone OTP primary)
- **Storage:** Firebase Cloud Storage (resumable uploads for evidence photos)
- **Push:** Firebase Cloud Messaging (FCM)
- **Maps:** Google Maps SDK (Android, iOS, Web)
- **Payments — diaspora clients:** Stripe (Manti Labs LLC, Delaware)
- **Payments — inspector payouts:** Flutterwave (MTN MoMo, M-Pesa, bank transfer)
- **AI:** Cloud Vision API (photo quality, fraud detection)
- **CI/CD:** GitHub Actions
- **Monitoring:** Firebase Crashlytics + Analytics + Performance Monitoring

## Monorepo Structure

```
eyeonsite/
├── CLAUDE.md                  ← YOU ARE HERE
├── docs/                      ← Product docs (EOS-01 through EOS-A7)
├── shared/                    ← KMP shared module
│   └── src/
│       ├── commonMain/        ← Shared code (domain, data, use cases)
│       │   ├── domain/
│       │   │   ├── model/     ← Data classes: Task, Evidence, Inspector, Escrow, etc.
│       │   │   ├── usecase/   ← Business logic: CreateTask, MatchInspector, ValidateEvidence
│       │   │   └── repository/ ← Repository interfaces
│       │   ├── data/
│       │   │   ├── remote/    ← Ktor API clients, Firebase wrappers
│       │   │   ├── local/     ← SQLDelight DAOs
│       │   │   └── repository/ ← Repository implementations
│       │   └── sync/          ← Offline sync engine
│       ├── androidMain/       ← Android-specific: OkHttp engine, SQLite driver
│       ├── iosMain/           ← iOS-specific: Darwin engine, Native driver
│       └── jsMain/            ← Web-specific: JS engine, IndexedDB driver
├── androidApp/                ← Android app (Jetpack Compose)
│   └── src/main/
│       ├── ui/                ← Compose screens and components
│       ├── di/                ← Hilt modules
│       ├── camera/            ← CameraX integration for evidence capture
│       ├── location/          ← Fused Location Provider wrapper
│       └── notification/      ← FCM handler
├── iosApp/                    ← iOS app (CMP)
│   └── src/
├── webApp/                    ← Compose for Web (Kotlin/JS)
│   └── src/jsMain/
│       ├── pages/
│       │   ├── Landing.kt     ← Marketing landing page
│       │   ├── Dashboard.kt   ← Admin dashboard
│       │   ├── ClientPortal.kt ← Client task management
│       │   └── InspectorPortal.kt ← Inspector fallback web portal
│       └── components/
├── functions/                 ← Cloud Functions v2 (Node.js)
│   ├── matching/              ← Geo-query + ping + acceptance
│   ├── escrow/                ← Hold, release, refund
│   ├── evidence/              ← Validation pipeline (GPS, EXIF, Vision AI, fraud scoring)
│   ├── notifications/         ← FCM dispatch, email, WhatsApp (V2)
│   ├── reports/               ← PDF evidence report generation
│   └── inspectors/            ← Onboarding, verification, tier management
├── firebase/
│   ├── firestore.rules        ← Security rules (see EOS-07)
│   ├── storage.rules          ← Cloud Storage rules
│   └── firebase.json          ← Hosting + functions config
└── gradle/
    └── libs.versions.toml     ← Version catalog (SINGLE SOURCE OF TRUTH for all dependencies)
```

## Key Patterns and Conventions

### Naming
- Package: `com.mantilabs.eyeonsite`
- Firestore collections: lowercase, snake_case (`escrow_transactions`)
- Domain models: PascalCase data classes (`EvidencePackage`, `TaskManifest`)
- Enums: SCREAMING_SNAKE_CASE values (`ESCROW_FUNDED`, `INSPECTOR_MATCHED`)
- Cloud Functions: camelCase (`onTaskFunded`, `releaseEscrow`)
- IDs: ULID for domain objects (time-sortable), Firebase UID for users

### Offline-First Rule
Every user action MUST succeed offline:
1. Write to SQLDelight local cache first (always succeeds)
2. Add to pending_sync queue
3. Background sync worker processes queue when online
4. Firestore is the source of truth; local cache is a write-ahead log
5. Conflict resolution: server timestamp wins (last-write-wins)
6. Evidence photos: captured to local storage → queued for upload → uploaded to Cloud Storage when online

### Evidence Pipeline Rule
Evidence integrity is the product. Never compromise:
- ALL photos MUST be captured through the in-app camera module (CameraX on Android, AVFoundation on iOS)
- NO camera roll / gallery uploads allowed
- GPS coordinates captured at EVERY photo (multi-signal: GPS + network + WiFi)
- EXIF data preserved and cross-verified server-side
- Device fingerprint (model, OS, app version) attached to every evidence package
- Perceptual hash generated for every photo (duplicate/stock detection)

### Escrow Rule
Money is sacred. Every money movement:
- Server-side only (Cloud Functions with Firebase Admin SDK)
- Double-entry ledger (every debit has a credit)
- Webhook signature verified (Stripe, Flutterwave)
- State machine: PENDING → HELD → RELEASED|REFUNDED|DISPUTED
- No backwards transitions (can't go RELEASED → HELD)
- Daily reconciliation (Stripe/Flutterwave vs. Firestore ledger)

### Task Status Machine
Valid transitions ONLY:
```
DRAFT → POSTED → ESCROW_FUNDED → INSPECTOR_MATCHED → IN_PROGRESS
→ EVIDENCE_SUBMITTED → UNDER_REVIEW → ACCEPTED | DISPUTED | REVISION_REQUESTED
REVISION_REQUESTED → EVIDENCE_SUBMITTED (re-enters review)
UNDER_REVIEW → AUTO_ACCEPTED (48hr timer)
DRAFT → CANCELLED (client cancels before posting)
POSTED → CANCELLED (client cancels before escrow)
ESCROW_FUNDED → EXPIRED (no inspector matched within deadline)
```
Invalid transitions are rejected server-side. Client/inspector apps never set status directly.

## What NOT to Do

- **Do NOT use Next.js, React, Vercel, or any JavaScript framework for web.** Web is Compose for Web (Kotlin/JS) on Firebase Hosting.
- **Do NOT use AWS or Azure services.** Google Cloud / Firebase only.
- **Do NOT use Room for local storage.** Room is Android-only. Use SQLDelight (KMP-native).
- **Do NOT allow gallery/camera roll uploads for evidence.** In-app capture only.
- **Do NOT write escrow logic on the client side.** All money movement is Cloud Functions only.
- **Do NOT store API keys in code.** Use Google Secret Manager. Access via Cloud Functions IAM.
- **Do NOT skip the offline sync layer.** Every feature must work without connectivity.

## Testing Strategy

- **Unit tests:** shared/commonMain for domain logic, use cases, repositories (mock data sources)
- **Integration tests:** Cloud Functions tested with Firebase Emulator Suite
- **UI tests:** Compose UI tests (androidTest) for critical flows (task creation, evidence capture, evidence review)
- **End-to-end:** Full task lifecycle test against Firebase Emulator (create → fund → match → capture → submit → review → accept → payout)

## Environment

- Development: Firebase Emulator Suite (Auth, Firestore, Storage, Functions)
- Staging: `eyeonsite-staging` Firebase project
- Production: `eyeonsite-prod` Firebase project
- CI/CD: GitHub Actions (build shared → build Android → build web → deploy functions → deploy hosting)

## Document References

Full product documentation in `docs/`:
- EOS-01: Vision and Market Context
- EOS-02: Product Design (personas, task lifecycle, evidence model)
- EOS-03: Architecture (five-plane system, KMP structure, offline sync)
- EOS-04: Technical Stack (every library with rationale)
- EOS-05: Manifest Specifications (all Kotlin data classes)
- EOS-06: Inspector Specification (onboarding, tiers, fraud detection)
- EOS-07: Security and Compliance (Firestore rules, threat model, regulatory)
- EOS-08: Escrow and Payments (Stripe + Flutterwave, ledger design)
- EOS-09: Roadmap (V0–V5 with kill criteria)
- EOS-10: Business Model (unit economics, revenue projections)
- EOS-11: Execution Plan (week-by-week)
- EOS-12: Risks and Mitigations
- EOS-13: Sources and Glossary
- EOS-A1–A7: Appendices (competitive intel, city playbook, verticals, disputes, matching, this file, decision log)
```

---

*Copy the content between the triple-backtick fences to `eyeonsite/CLAUDE.md` when initializing the repository.*
