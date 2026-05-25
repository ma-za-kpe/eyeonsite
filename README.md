# EyeOnSite

**On-demand verification marketplace for Africa.** Connecting diaspora investors with verified local inspectors — GPS-stamped, escrow-backed evidence within hours.

Built by [Manti Labs](https://mantilabs.com) (Maku Mazakpe).

> "Trust, verified."

---

## What It Is

Africa receives ~$100B in remittances annually. Diaspora families send money home to build houses, buy land, fund farms — and have no way to verify it's being used correctly. EyeOnSite fixes that.

- **Client** posts a verification task (construction progress check, property visit, etc.)
- **Inspector** (verified local) goes to the site, captures GPS-stamped photos in-app
- **Evidence** is reviewed; escrow released when client accepts
- **Platform** earns 18% commission + 2% escrow fee

**Stage:** V0 (manual validation, Accra, Ghana). V1 (KMP app) in development.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Android | Jetpack Compose |
| iOS | Compose Multiplatform |
| Web | Compose for Web (Kotlin/JS) — Firebase Hosting |
| Shared logic | Kotlin Multiplatform (KMP) |
| Backend | Cloud Functions v2 (Node.js) |
| Database | Cloud Firestore + SQLDelight (offline cache) |
| Auth | Firebase Authentication (phone OTP) |
| Storage | Firebase Cloud Storage |
| Payments (clients) | Stripe |
| Payments (inspectors) | Flutterwave (MTN MoMo, M-Pesa) |
| AI | Cloud Vision API |

**Principle:** Kotlin everywhere. No Flutter, no React, no AWS.

---

## Module Structure

```
EyeOnSite/
├── app/
│   ├── shared/          # KMP shared UI + business logic (Compose Multiplatform)
│   │   └── src/commonMain/kotlin/com/maku/eyeonsite/
│   │       ├── ui/
│   │       │   ├── theme/       # EyeOnSiteTheme — colors, typography, Material3
│   │       │   └── auth/        # SignInScreen (phone OTP flow)
│   │       └── App.kt           # Root composable (entry point for all platforms)
│   ├── androidApp/      # Android app (Jetpack Compose)
│   │   └── google-services.json
│   ├── webApp/          # Compose for Web (Kotlin/JS)
│   └── iosApp/          # iOS app (CMP)
├── core/                # Pure Kotlin shared utilities
├── server/              # Ktor server (placeholder; backend = Cloud Functions)
├── docs/                # Product documentation (EOS-00 through EOS-B5)
├── firebase.json        # Firebase Hosting config
├── .firebaserc          # Firebase project alias
└── gradle/
    └── libs.versions.toml  # Single source of truth for all dependency versions
```

---

## Brand & Theme

### Colors

| Token | Hex | Role |
|-------|-----|------|
| `Forest` | `#0B6E4F` | Primary — buttons, links, logo |
| `DeepForest` | `#022B1E` | Dark background, auth screens |
| `SavannaGold` | `#D4A843` | Accent, highlights, taglines |
| `SignalGreen` | `#34D399` | Success states, verified |
| `AlertCoral` | `#E8593C` | Errors, disputes |
| `CautionAmber` | `#F59E0B` | Warnings, pending |
| `Ivory` | `#F5F4F0` | Light backgrounds |
| `Ink` | `#1A1A19` | Primary text (light mode) |

### Fonts

| Role | Font | Weights |
|------|------|---------|
| All UI text | DM Sans | Regular (400), Medium (500) |
| Data / IDs / GPS | JetBrains Mono | Regular (400) |

Fonts are embedded as CMP resources at:
`app/shared/src/commonMain/composeResources/font/`

### Using the Theme

```kotlin
// Wrap any composable with EyeOnSiteTheme
EyeOnSiteTheme {
    // Colors via MaterialTheme.colorScheme
    // Typography via MaterialTheme.typography
    // Direct access: Forest, DeepForest, SavannaGold, etc.
}
```

---

## Firebase Setup

### Project

| Setting | Value |
|---------|-------|
| Project ID | `eyeonsite-3b8e0` |
| Live web URL | https://eyeonsite-3b8e0.web.app |
| Auth domain | `eyeonsite-3b8e0.firebaseapp.com` |

### Android

`app/androidApp/google-services.json` is in place (registered Android app in Firebase Console).

### Secrets

Firebase config lives in `local.properties` (gitignored). Never commit that file.

---

## Running Locally

### Prerequisites

- Android Studio Meerkat or later
- JDK 17+
- Firebase CLI (`npm install -g firebase-tools`)

### Android

```bash
./gradlew :app:androidApp:assembleDebug
```

Or use the Android Studio run config.

### Web (development)

```bash
./gradlew :app:webApp:jsBrowserDevelopmentRun
# Opens at http://localhost:8080
```

### Web (production build + deploy)

```bash
./gradlew :app:webApp:jsBrowserDistribution
firebase deploy --only hosting
```

### iOS

Open `app/iosApp` in Xcode and run.

### Ktor server (local)

```bash
./gradlew :server:run
# Starts at http://localhost:8080
```

---

## Tests

```bash
# Shared (KMP) unit tests
./gradlew :app:shared:testAndroid

# Server tests
./gradlew :server:test

# Full local validation used by the pre-commit hook and CI
./gradlew spotlessCheck
./gradlew detektAll
./gradlew :app:androidApp:assembleDebug
./gradlew :app:webApp:jsBrowserProductionWebpack
```

---

## Docs

Full product documentation is in `docs/`. Start here:

| Doc | What it covers |
|-----|---------------|
| [EOS-00](./docs/EOS-00-DOCUMENT-INDEX.md) | Document index — table of contents |
| [EOS-01](./docs/EOS-01-VISION-AND-MARKET.md) | Vision, $100B remittance market, why now |
| [EOS-02](./docs/EOS-02-PRODUCT-DESIGN.md) | Personas, task lifecycle, evidence model |
| [EOS-03](./docs/EOS-03-ARCHITECTURE.md) | Five-plane architecture, KMP structure |
| [EOS-05](./docs/EOS-05-MANIFEST-SPECS.md) | All Kotlin data classes and Firestore rules |
| [EOS-09](./docs/EOS-09-ROADMAP.md) | V0 → V5 roadmap |
| [EOS-B1](./docs/EOS-B1-BRAND-IDENTITY.md) | Colors, fonts, logo, brand voice |
| [EOS-A8](./docs/EOS-A8-INTEGRATION-MOCK-STRATEGY.md) | Stripe, Flutterwave, Firebase Analytics, and AI mock strategy |

**For developers joining:** read `CLAUDE.md` first, then EOS-03 → EOS-04 → EOS-05.

---

## Roadmap (short version)

- **V0 (now):** Manual validation — Maku is the inspector, WhatsApp + Google Forms
- **V1 (months 2–5):** This KMP app — Accra only, construction vertical, real escrow
- **V2 (months 6–10):** Multi-city (Lagos/Nairobi), video evidence
- **V3 (months 11–18):** Enterprise API, subscriptions, pre-seed raise

---

*Manti Labs LLC (Delaware) · makpalyy@gmail.com*
