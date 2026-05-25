# EyeOnSite — Tech Stack

## Versions (single source of truth: gradle/libs.versions.toml)
| Library | Version |
|---------|---------|
| Kotlin | 2.3.21 |
| Compose Multiplatform | 1.11.0 |
| AGP | 9.0.1 |
| Firebase BoM | 34.13.0 |
| Ktor | 3.4.3 |
| compileSdk | 36 |
| minSdk | 24 |

## Platform Stack
| Surface | Technology |
|---------|-----------|
| Android | Jetpack Compose + CameraX + Fused Location |
| iOS | Compose Multiplatform (CMP) + AVFoundation |
| Web | Compose for Web (Kotlin/JS) on Firebase Hosting |
| Backend | Cloud Functions v2 (Node.js) |
| Database | Cloud Firestore (offline-first) |
| Offline cache | SQLDelight (KMP-native) |
| Networking | Ktor Client (KMP) |
| Serialization | kotlinx.serialization |
| DI (Android) | Hilt |
| DI (KMP shared) | Koin |
| Auth | Firebase Authentication (phone OTP) |
| Storage | Firebase Cloud Storage (resumable uploads) |
| Push | Firebase Cloud Messaging (FCM) |
| Maps | Google Maps SDK |
| Payments (diaspora) | Stripe |
| Payments (local/payouts) | Flutterwave (MTN MoMo, M-Pesa) |
| AI | Cloud Vision API |
| CI/CD | GitHub Actions |
| Monitoring | Firebase Crashlytics + Analytics + Performance |
| Secrets | Google Secret Manager |

## Key Dependency Rules
- All versions in `gradle/libs.versions.toml` — never hardcode versions in build files
- Firebase dependencies use BoM — never specify Firebase versions directly
- Use `platform(libs.firebase.bom)` in androidApp, not in shared module
- SQLDelight for local storage — never Room (Android-only)
- Ktor Client for networking — never Retrofit (Android-only)

## Mock Strategy (Demo MVP / Testing)
- `AnalyticsService` interface → `FirebaseAnalyticsService` (prod) / `MockAnalyticsService` (test)
- Payment flows: simulated status changes, no real Stripe/Flutterwave in demo
- Cloud Vision: fake quality score (photo count + GPS match) until V1
- Firebase Emulator Suite for integration tests
