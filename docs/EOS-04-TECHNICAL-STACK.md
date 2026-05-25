# 04 — Technical Stack

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## The Boring-Stack Principle

Every technology choice optimizes for three things:
1. **Proven in production** — no beta, no experimental, no "just announced"
2. **You (Maku) can ship it solo** — no technology you haven't used before in a real project
3. **Works in low-connectivity African environments** — offline-capable, low-bandwidth, handles flaky networks

If a technology doesn't meet all three, it doesn't get in.

## Stack Overview

| Layer | Technology | Version | Why This, Not That |
|-------|-----------|---------|-------------------|
| **Shared Logic** | Kotlin Multiplatform (KMP) | 2.1.x | Stable since Nov 2023. Google co-investing. You write Kotlin daily. Share 60–70% of code. Not Flutter (Dart is a new language, widget tree doesn't feel native). Not React Native (JS bridge overhead, you're not a JS-first developer). |
| **Android UI** | Jetpack Compose | 1.7.x / BOM 2024.x | Native Android. You ship this at Farmerline. No debate. |
| **iOS UI** | Compose Multiplatform (CMP) | 1.7.x | Maximum code sharing with Android. Compiles to native iOS via Kotlin/Native + LLVM. Not SwiftUI — CMP lets you share UI code, not just logic. Re-evaluate at V2 if iOS quality needs native SwiftUI for specific screens. |
| **Web UI** | Compose for Web (Kotlin/JS) | 1.7.x | One language for landing page + admin dashboard + client portal. Hosted on Firebase Hosting. Not Next.js — eliminates TypeScript/React from the stack entirely. One language across every surface. Trade-off: smaller ecosystem, fewer UI libraries. Acceptable for admin dashboard and marketing page. |
| **Backend** | Cloud Functions v2 | Gen 2 | Serverless, scales to zero. Firestore triggers for event-driven logic. Node.js runtime (Firebase SDK is most mature here). Kotlin/JVM via GraalVM considered but Node.js has better Firebase Admin SDK support and faster cold starts. |
| **Auth** | Firebase Authentication | — | Phone OTP (primary). Email/password (enterprise clients). Google Sign-In (optional). Handles OTP delivery in Ghana, Nigeria, Kenya natively. |
| **Database** | Cloud Firestore | — | Real-time sync, offline persistence on mobile (built-in), scales without ops. Schemaless (flexible for rapid iteration). Security Rules for row-level access control. Not PostgreSQL/Supabase — would need to manage server, connection pooling, and doesn't have native offline sync for mobile. |
| **Offline Cache** | SQLDelight | 2.0.x | KMP-native SQL. Compiles to SQLite on Android/iOS, IndexedDB on Web. Type-safe queries generated from .sq files. Not Room (Android-only). Not Realm (being deprecated by MongoDB). |
| **Networking** | Ktor Client | 3.0.x | KMP-native HTTP client. Pluggable engines (OkHttp on Android, Darwin on iOS, Js on Web). kotlinx.serialization for JSON. |
| **Serialization** | kotlinx.serialization | 1.7.x | KMP-native. No reflection (important for iOS/Native). JSON and Protobuf support. |
| **Coroutines** | kotlinx.coroutines | 1.9.x | KMP-native async. Flows for reactive streams. Structured concurrency. |
| **Date/Time** | kotlinx.datetime | 0.6.x | KMP-native. Instant, LocalDateTime, TimeZone. No java.time dependency. |
| **DI (Android)** | Hilt / Dagger | 2.52.x | Standard Android DI. For KMP shared module, manual DI or Koin. |
| **DI (KMP)** | Koin | 4.0.x | Lightweight, KMP-compatible. Service locator pattern. Good enough for shared module. |
| **Cloud Storage** | Firebase Cloud Storage | — | Photo/video upload with resumable uploads (critical for spotty networks). Signed URLs for secure access. Automatic CDN. |
| **Push Notifications** | Firebase Cloud Messaging (FCM) | — | Works on Android natively. iOS via APNs bridge. Web via service workers. |
| **Maps** | Google Maps SDK | — | Android: Maps Compose. iOS: Google Maps SDK (native, wrapped via expect/actual). Web: Google Maps JS API. Reliable geocoding in Africa. |
| **Camera (Android)** | CameraX | 1.4.x | Modern camera API. Photo capture, video recording, image analysis. Lifecycle-aware. |
| **Camera (iOS)** | AVFoundation | — | Native iOS camera. Wrapped via expect/actual in KMP. |
| **Location (Android)** | Fused Location Provider | — | Google Play Services. Best available location (GPS + network + WiFi). |
| **Location (iOS)** | CoreLocation | — | Native iOS. Wrapped via expect/actual. |
| **Payments (Diaspora)** | Stripe | — | Manti Labs LLC already on Stripe Atlas. Cards, bank transfers in USD/GBP/EUR. Stripe Connect for marketplace model (future). |
| **Payments (Local)** | Flutterwave | — | MTN MoMo (Ghana), M-Pesa (Kenya), bank transfers (Nigeria, Uganda). Best coverage across target markets. Not Paystack — Flutterwave has broader African coverage beyond Nigeria. |
| **AI/ML** | Cloud Vision API | — | Photo quality scoring (blur, exposure). OCR (document scanning). SafeSearch (inappropriate content). Later: custom models on Vertex AI for fraud detection. |
| **CI/CD** | GitHub Actions | — | Your existing infrastructure. Build KMP, run tests, deploy Cloud Functions, deploy web to Firebase Hosting, build Android APK/AAB. |
| **Hosting (Web)** | Firebase Hosting | — | Compose for Web output (static JS/HTML/CSS) served from Firebase CDN. Custom domain. SSL automatic. Not Vercel — keeping everything in Google ecosystem. |
| **Monitoring** | Firebase Crashlytics + Analytics + Performance Monitoring | — | Free. Integrated. Crashlytics for crash reporting. Analytics for user behavior. Performance Monitoring for network/render times. Sufficient for V0–V2. |
| **Secrets** | Google Secret Manager | — | API keys (Stripe, Flutterwave) stored securely. Accessed by Cloud Functions via IAM. Never in code. |

## Dependency Graph

```
gradle/libs.versions.toml (version catalog — single source of truth)
    │
    ├── shared/ (KMP module)
    │   ├── commonMain
    │   │   ├── org.jetbrains.kotlinx:kotlinx-coroutines-core
    │   │   ├── org.jetbrains.kotlinx:kotlinx-serialization-json
    │   │   ├── org.jetbrains.kotlinx:kotlinx-datetime
    │   │   ├── io.ktor:ktor-client-core
    │   │   ├── io.ktor:ktor-client-content-negotiation
    │   │   ├── io.ktor:ktor-serialization-kotlinx-json
    │   │   ├── app.cash.sqldelight:runtime
    │   │   ├── io.insert-koin:koin-core
    │   │   └── dev.gitlive:firebase-* (KMP Firebase SDK)
    │   ├── androidMain
    │   │   ├── io.ktor:ktor-client-okhttp
    │   │   └── app.cash.sqldelight:android-driver
    │   ├── iosMain
    │   │   ├── io.ktor:ktor-client-darwin
    │   │   └── app.cash.sqldelight:native-driver
    │   └── jsMain
    │       ├── io.ktor:ktor-client-js
    │       └── app.cash.sqldelight:web-worker-driver
    │
    ├── androidApp/
    │   ├── androidx.compose:compose-bom
    │   ├── androidx.camera:camera-camera2
    │   ├── androidx.camera:camera-lifecycle
    │   ├── com.google.android.gms:play-services-location
    │   ├── com.google.android.gms:play-services-maps
    │   ├── com.google.dagger:hilt-android
    │   ├── com.stripe:stripe-android
    │   └── com.flutterwave:flutterwave-android
    │
    ├── iosApp/
    │   └── (CMP dependencies come through shared module)
    │
    ├── webApp/
    │   ├── org.jetbrains.compose:compose-html-core
    │   └── org.jetbrains.compose:compose-html-svg
    │
    └── functions/ (Cloud Functions — Node.js)
        ├── firebase-admin
        ├── firebase-functions (v2)
        ├── stripe
        ├── flutterwave-node-v3
        └── @google-cloud/vision
```

## Key Technical Decisions

### Why KMP over Flutter

| Factor | KMP | Flutter |
|--------|-----|---------|
| Language | Kotlin (you write it daily) | Dart (new language to learn) |
| Native feel | Compiles to native, uses native UI frameworks | Custom rendering engine (Skia/Impeller) |
| Camera/GPS | Direct native API access via expect/actual | Plugin ecosystem (varies in quality) |
| Offline | SQLDelight (KMP-native, compiles to SQLite) | sqflite or drift (good but Dart-only) |
| iOS quality | CMP is maturing rapidly; can drop to SwiftUI for specific screens | Consistent but sometimes non-native feel |
| Web | Compose for Web (experimental but functional) | Flutter Web (heavy, slow initial load) |
| Community | Smaller but growing fast; JetBrains + Google backing | Larger; Google-backed |
| **Decision** | **KMP — your core skill, native quality, one language** | — |

### Why Firebase over Supabase

| Factor | Firebase | Supabase |
|--------|----------|----------|
| Offline sync | Built-in on Android/iOS (Firestore offline persistence) | No native mobile offline sync |
| Real-time | Native real-time listeners | Real-time via WebSockets (good but no offline) |
| Auth | Phone OTP works in Ghana/Nigeria/Kenya out of the box | Phone OTP via Twilio (extra cost, configuration) |
| Serverless | Cloud Functions v2 (scales to zero) | Edge Functions (Deno-based, less mature) |
| Storage | Resumable uploads (critical for spotty networks) | S3-compatible (no resumable upload built-in) |
| Cost | Free tier generous; Blaze plan pay-as-you-go | Free tier generous; Pro plan $25/mo |
| KMP SDK | dev.gitlive/firebase-kotlin-sdk (community, active) | No official KMP SDK |
| **Decision** | **Firebase — offline-first, proven in Africa, KMP SDK exists** | — |

### Why Compose for Web over Next.js

| Factor | Compose for Web | Next.js |
|--------|----------------|---------|
| Language | Kotlin (same as everything else) | TypeScript (second language in stack) |
| Code sharing | Shares components/logic with mobile apps | Separate codebase entirely |
| Ecosystem | Small; fewer UI libraries | Massive; React ecosystem |
| SEO | Client-side rendered (CSR) — weaker SEO | SSR/SSG — strong SEO |
| Performance | Larger bundle size (Kotlin/JS) | Optimized, tree-shaken |
| **Decision** | **Compose for Web — one language wins. SEO trade-off acceptable for dashboard/portal. Landing page SEO risk mitigated by Firebase Hosting + prerender.** | — |

**SEO Mitigation for Landing Page:** Use `prerender.io` or similar service to generate static HTML snapshots for search engines. The landing page is primarily a conversion tool for diaspora users who find EyeOnSite through social media/WhatsApp (not organic search), so SEO is a secondary concern.

### Why Flutterwave over Paystack for Inspector Payouts

| Factor | Flutterwave | Paystack |
|--------|-------------|----------|
| Ghana (MTN MoMo) | Supported | Limited |
| Kenya (M-Pesa) | Supported | Not available |
| Uganda | Supported | Not available |
| Nigeria | Supported | Supported (strong) |
| Multi-country | 4/4 target markets | 1/4 target markets |
| **Decision** | **Flutterwave — covers all four launch markets** | — |

## Environment Configuration

```
# .env.local (never committed)

# Firebase
FIREBASE_PROJECT_ID=eyeonsite-prod
FIREBASE_API_KEY=...
FIREBASE_AUTH_DOMAIN=eyeonsite-prod.firebaseapp.com

# Stripe (via Secret Manager in production)
STRIPE_SECRET_KEY=sk_live_...
STRIPE_PUBLISHABLE_KEY=pk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Flutterwave (via Secret Manager in production)
FLUTTERWAVE_SECRET_KEY=FLWSECK_...
FLUTTERWAVE_PUBLIC_KEY=FLWPUBK_...
FLUTTERWAVE_WEBHOOK_HASH=...

# Google Cloud
GOOGLE_CLOUD_PROJECT=eyeonsite-prod
CLOUD_VISION_API_KEY=... (or use default credentials)

# Maps
GOOGLE_MAPS_API_KEY=...
```

## Build and Deploy

```bash
# Build shared module
./gradlew :shared:build

# Build and run Android app
./gradlew :androidApp:installDebug

# Build iOS (from Xcode, using shared framework)
# xcodebuild or open iosApp/iosApp.xcworkspace

# Build web app (landing page + dashboard)
./gradlew :webApp:jsBrowserProductionWebpack
# Output in webApp/build/dist/js/productionExecutable/

# Deploy web to Firebase Hosting
firebase deploy --only hosting

# Deploy Cloud Functions
firebase deploy --only functions

# Run all tests
./gradlew :shared:allTests
./gradlew :androidApp:testDebugUnitTest
./gradlew :webApp:jsTest
```

---

*See [EOS-03-ARCHITECTURE.md](./EOS-03-ARCHITECTURE.md) for system design and data flow.*
*See [EOS-A7-DECISION-LOG.md](./EOS-A7-DECISION-LOG.md) for the reasoning behind each technology choice.*
