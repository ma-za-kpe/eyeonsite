# CLAUDE.md — EyeOnSite

## What This Project Is

EyeOnSite is an on-demand verification marketplace connecting diaspora investors with verified local inspectors in African cities. Think Bolt/Uber but for physical verification — GPS-stamped, escrow-backed evidence that construction projects, property, farms, and business collateral are real.

**Built by:** Manti Labs (Maku Mazakpe)
**Stage:** V0 → V1 (pre-product, validating demand manually before building app)
**Package:** `com.maku.eyeonsite`

## Architecture

- **Kotlin Multiplatform (KMP)** across ALL surfaces. One language everywhere.
- **Android:** Jetpack Compose
- **iOS:** Compose Multiplatform (CMP)
- **Web:** Compose for Web (Kotlin/JS) on Firebase Hosting
- **Backend:** Cloud Functions v2 (Node.js runtime)
- **Database:** Cloud Firestore (offline-first with native mobile persistence)
- **Offline cache:** SQLDelight (KMP-native)
- **Networking:** Ktor Client (KMP-native)
- **Auth:** Firebase Authentication (phone OTP primary)
- **Storage:** Firebase Cloud Storage (resumable uploads for evidence photos)
- **Push:** Firebase Cloud Messaging (FCM)
- **Payments — diaspora clients:** Stripe (Manti Labs LLC, Delaware)
- **Payments — inspector payouts:** Flutterwave (MTN MoMo, M-Pesa, bank transfer)
- **AI:** Cloud Vision API (photo quality, fraud detection)

## Module Structure

```
app/shared/src/commonMain/kotlin/com/maku/eyeonsite/
├── ui/
│   ├── theme/          # EyeOnSiteTheme, Color.kt, Type.kt
│   └── auth/           # SignInScreen
└── App.kt              # Root composable

app/androidApp/         # Android entry point + google-services.json
app/webApp/             # Compose for Web entry point
app/iosApp/             # iOS entry point
core/                   # Pure shared Kotlin utilities
server/                 # Ktor (placeholder; real backend = Cloud Functions)
```

## Key Patterns and Conventions

### Naming
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

### Evidence Pipeline Rule
Evidence integrity is the product. Never compromise:
- ALL photos MUST be captured through the in-app camera (CameraX on Android, AVFoundation on iOS)
- NO camera roll / gallery uploads ever (DEC-010 — PERMANENT)
- GPS coordinates captured at every photo (multi-signal)
- EXIF data preserved and cross-verified server-side

### Escrow Rule
Money is sacred. Every money movement:
- Server-side only (Cloud Functions with Firebase Admin SDK)
- Double-entry ledger (every debit has a credit)
- State machine: PENDING → HELD → RELEASED|REFUNDED|DISPUTED

### Task Status Machine
```
DRAFT → POSTED → ESCROW_FUNDED → INSPECTOR_MATCHED → IN_PROGRESS
→ EVIDENCE_SUBMITTED → UNDER_REVIEW → ACCEPTED | DISPUTED | REVISION_REQUESTED

POSTED → EXPIRED  (Cloud Scheduler: expireStaleTasks fires when expiresAtMs elapses)
```
Invalid transitions are rejected server-side. Clients never set status directly.

## Brand & Theme

Colors are in `Color.kt`, typography in `Type.kt`, theme in `Theme.kt` — all in `app/shared/src/commonMain/kotlin/com/maku/eyeonsite/ui/theme/`.

Always wrap screens with `EyeOnSiteTheme { }`. Never use raw `MaterialTheme { }`.

Key colors: `Forest` (#0B6E4F), `DeepForest` (#022B1E), `SavannaGold` (#D4A843).

Fonts: DM Sans (all UI text), JetBrains Mono (data / IDs / GPS coordinates).

## What NOT to Do

- **Do NOT use Next.js, React, Vercel, or any JavaScript framework.** Web is Compose for Web (Kotlin/JS).
- **Do NOT use AWS or Azure.** Google Cloud / Firebase only.
- **Do NOT use Room.** Use SQLDelight (KMP-native).
- **Do NOT allow gallery uploads for evidence.** In-app capture only — this is PERMANENT.
- **Do NOT write escrow logic on the client side.** Cloud Functions only.
- **Do NOT store secrets in code.** Use `local.properties` locally; Google Secret Manager in production.
- **Do NOT skip the offline sync layer.**

## Firebase

- Project: `eyeonsite-3b8e0`
- Live web: https://eyeonsite-3b8e0.web.app
- `local.properties` holds Firebase config (gitignored — never commit)
- Android: `app/androidApp/google-services.json` (registered Android app)

## Build Commands

```bash
# Web: build + deploy
./gradlew :app:webApp:jsBrowserDistribution
firebase deploy --only hosting

# Android debug APK
./gradlew :app:androidApp:assembleDebug

# Web: dev server
./gradlew :app:webApp:jsBrowserDevelopmentRun
```

## Document References

Full product docs in `docs/`:
- EOS-01: Vision and Market Context
- EOS-02: Product Design (personas, task lifecycle, evidence model)
- EOS-03: Architecture (five-plane system, KMP structure, offline sync)
- EOS-04: Technical Stack (every library with rationale)
- EOS-05: Manifest Specifications (all Kotlin data classes)
- EOS-06: Inspector Specification
- EOS-07: Security and Compliance (Firestore rules, threat model)
- EOS-08: Escrow and Payments (Stripe + Flutterwave, ledger)
- EOS-09: Roadmap (V0–V5)
- EOS-A6: This CLAUDE.md source
- EOS-A7: Decision Log (10 founding decisions + rejected ideas)
- EOS-B1: Brand Identity (colors, fonts, logo, voice)
