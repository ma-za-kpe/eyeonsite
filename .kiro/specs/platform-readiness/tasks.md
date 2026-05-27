# Platform Readiness - Tasks

Status legend:
- [ ] Not started
- [~] In progress
- [x] Complete

## P0 - Restore A Green Foundation

- [x] Capture audit findings in a consolidated task list.
- [x] Fix Android/shared Firebase dependency wiring so `:app:androidApp:assembleDebug` compiles.
- [x] Fix web landing dependencies so `:app:webApp:jsBrowserProductionWebpack` compiles.
- [x] Wire the web entry point to a real landing page instead of the shared sign-in app.
- [x] Fix `detektAll` so static analysis runs without Gradle configuration errors.
- [x] Replace invalid pre-commit test command with an existing shared test task.
- [x] Install the local Git pre-commit hook.
- [x] Add Firebase Hosting security headers.
- [x] Run and record `spotlessCheck`, `detektAll`, Android build, web build, shared tests, shared compile, and server tests.

Validated on 2026-05-25:
- `./gradlew spotlessCheck`
- `./gradlew detektAll`
- `./gradlew :app:shared:testAndroid`
- `./gradlew :app:androidApp:assembleDebug`
- `./gradlew :app:webApp:jsBrowserProductionWebpack`
- `./gradlew :app:shared:compileKotlinMetadata :app:shared:compileAndroidMain :app:shared:compileKotlinJs`
- `./gradlew :server:test`

## P1 - Landing Page First

- [x] Build `LandingPage.kt` as the web root with nav, scroll container, analytics on load, and section orchestration.
- [x] Complete hero, problem, how-it-works, evidence preview, verticals, trust, pricing, and footer sections.
- [x] Ensure every CTA emits `cta_clicked` with stable `cta_id` and `section`.
- [x] Ensure each landing section emits `section_viewed` when displayed.
- [x] Replace web analytics console stub with Firebase Analytics JS or gtag integration.
- [x] Add responsive layouts for desktop/tablet/mobile.
- [ ] Add visual QA screenshots for desktop and mobile before deploy.
- [ ] Deploy Firebase Hosting only after build and checks are green.

## P2 - Demo MVP Golden Path

- [ ] Auth: real Firebase phone OTP, OTP code entry, auth state observer, sign out.
- [ ] Auth: role selection persisted to `users/{uid}`.
- [ ] Domain: add shared models for `User`, `Task`, `Inspector`, `EvidencePackage`, `Template`, and status enums.
- [ ] Data: repository interfaces in shared common code.
- [ ] Data: Firebase-backed Android implementations for Auth, Firestore, Storage, and FCM token registration.
- [ ] Client: task dashboard with realtime Firestore listener.
- [ ] Client: create task flow with map pin, construction template, instructions, and budget.
- [ ] Client: task detail with realtime status, inspector profile, map, and evidence entry point.
- [ ] Inspector: task map with location, nearby tasks, task sheet, and accept action.
- [ ] Inspector: capture mode with CameraX, GPS status, required photo counter, angle guide, and thumbnails.
- [ ] Inspector: checklist flow and evidence summary.
- [ ] Evidence: upload photos to Cloud Storage and write `evidence/{id}`.
- [ ] Evidence review: photo grid, GPS verification, checklist, quality score, inspector notes, accept action.
- [ ] Demo payment: simulated escrow status changes only.
- [ ] Notifications: new task, inspector matched, evidence ready, evidence accepted.
- [ ] Seed data: construction template, sample inspectors, and sample tasks.
- [ ] Firestore rules for the five demo collections.
- [ ] Cloud Functions: `onTaskCreated`, `onEvidenceSubmitted`, `sendNotification`.
- [ ] End-to-end demo test plan using two Android devices.

## P3 - V1 Deferred Work

- [x] Document external integration mock strategy for Stripe, Flutterwave, Firebase Analytics Web, and Cloud Vision AI.
- [ ] Real escrow ledger and money movement via Stripe and Flutterwave.
- [ ] Stripe test-mode adapter, webhook signature verification, and PaymentIntent fixtures.
- [ ] Flutterwave test-mode adapter, transfer scenario keys, webhook/callback verification, and payout fixtures.
- [~] SQLDelight offline-first sync engine and pending upload queue.
- [~] Inspector onboarding: ID upload, liveness/selfie check, training, test task.
- [~] Cloud Vision quality scoring behind a replaceable interface.
- [ ] Fraud pipeline: GPS spoof checks, EXIF verification, device attestation, perceptual hashes.
- [~] Firebase Storage rules for evidence photos, KYC documents, path ownership, size limits, and content types.
- [~] Firestore and Storage emulator tests for rules, task ownership, evidence access, waitlist writes, and onboarding data.
- [~] Firebase Emulator Suite local workflow with mocked Functions, Firestore, Storage, Auth, deterministic seed data, and JDK 21+ runtime.
- [ ] Environment separation: local, staging, and production Firebase projects with explicit Secret Manager bindings.
- [ ] Cloud Functions region strategy and deploy inventory so orphaned/stale functions cannot block release.
- [x] Firestore index plan for matching, evidence review, admin queues, onboarding, and waitlist operations.
- [ ] Observability: Crashlytics, Performance Monitoring, structured Function logs, alerts, and error budgets.
- [ ] Resolve Functions npm audit moderate `uuid` advisory through safe upstream Firebase/Google Cloud dependency updates.
- [ ] Admin review queues for onboarding approvals, fraud flags, disputes, and payout exceptions.
- [ ] KYC data retention, PII minimisation, private media access, and deletion policy.
- [ ] Resolve text encoding issues before merge so source comments and user-facing strings stay clean ASCII or intentional Unicode.
- [ ] Dispute and revision workflows.
- [ ] Rating and trust-score system.
- [ ] Full inspector tier system.
- [ ] iOS production hardening and AVFoundation/CoreLocation actuals.
- [ ] Admin dashboard in Compose for Web.
- [ ] Release validation: changelog, release-please, tagged builds, release notes, rollback plan.

## EOS-14 - Productionization Sequence

Implementation order for the post-demo hardening pass. Keep these items mocked or test-mode until the corresponding production provider is ready.

- [ ] Establish DDD boundaries and shared contracts for H3, evidence trust, KYC, escrow ledger, and fraud signals.
- [ ] Enforce TDD for the first pass of each new module: red test, green implementation, refactor.
- [ ] Keep local development on mock adapters and test keys only.
- [~] Add validation gates for every milestone: build, lint, detekt, unit tests, shared tests, and E2E checks.
- [x] Validate current in-progress onboarding, offline queue, Storage upload, and Vision code before building the next layer.
- [ ] Add schema and domain fields for H3 cells, EXIF metadata, attestation verdicts, pHash, quality score, and escrow state.
- [ ] Replace shared no-op paths with Firebase-backed repositories and a deterministic storage uploader where needed.
- [~] Add Storage rules and rules tests before enabling real evidence or KYC media in production.
- [~] Add emulator-first integration tests for Auth, Firestore, Storage, Functions, FCM stubs, and offline sync recovery.
- [ ] Add immutable audit events for task state changes, evidence processing, KYC decisions, escrow ledger entries, and payouts.
- [ ] Implement H3-based task and inspector matching in Cloud Functions, with haversine as the final tie-breaker.
- [~] Add required Firestore composite indexes for H3/geospatial matching and admin review queues.
- [ ] Implement the evidence trust pipeline: GPS validation, EXIF verification, timestamp drift checks, speed plausibility, pHash, and fraud scoring.
- [ ] Add capture integrity controls: in-app-only capture, mock-location detection, accuracy thresholds, provider source, clock skew, and device motion where available.
- [ ] Add private media access controls: store paths instead of public URLs where possible, generate short-lived access for authorized review, and preserve original bytes.
- [ ] Add Play Integrity plus Firebase App Check enforcement for Android submissions.
- [ ] Enforce App Check or equivalent attestation on callable/HTTP Functions that mutate production data.
- [ ] Build Stripe and Flutterwave mock adapters first, then promote them to test-mode adapters with webhook verification, idempotency, and replay protection.
- [~] Add a double-entry escrow ledger model with invariants, reconciliation jobs, and explicit fee constants.
- [ ] Complete payment and escrow compliance review for UK, US, Canada, Ghana, Nigeria, Kenya, and Uganda launch paths.
- [ ] Add Smile ID onboarding flows behind server-side adapters and webhook handlers.
- [ ] Add Smile ID mock and sandbox adapters, webhook signature verification, retry handling, and KYC provider audit trail.
- [ ] Add SQLDelight offline queueing and pending upload sync for unreliable network conditions.
- [ ] Add offline failure UX and telemetry for queued photos, retry count, exhausted retries, and manual retry.
- [ ] Add C2PA-style provenance manifests and server-side signing for evidence bundles.
- [~] Add release environment checks: project ID guardrails, required secrets, rules version, index deployment, and rollback notes.
- [ ] Finish with dispute, revision, rating, tiering, and release-readiness validation.

### Delivery Principles

- [ ] Keep code clean, small, and explicit; prefer simple composition over hidden coupling.
- [ ] Keep domain logic in shared models and use cases, not in UI or transport layers.
- [ ] Mock external services for local and CI runs; use real providers only in dedicated test-mode or production environments.
- [ ] Run integration tests before merging any provider, sync, or security change.
- [ ] Run E2E checks before release candidates, including at least one happy path and one failure path.
- [ ] Keep release notes and changelog entries in the same pull request as the change.

## Firebase Console Checklist

- [ ] Enable Authentication and Phone provider.
- [ ] Enable Cloud Firestore.
- [ ] Enable Cloud Storage for Firebase.
- [ ] Enable Cloud Functions for Firebase.
- [ ] Enable Firebase Cloud Messaging.
- [ ] Enable Firebase Hosting.
- [ ] Enable Google Analytics for Firebase.
- [ ] Enable Firebase App Check.
- [ ] Enable Crashlytics.
- [ ] Enable Performance Monitoring.
- [ ] Enable Test Lab and App Distribution for mobile QA.
- [ ] Enable Google Secret Manager for Stripe and Flutterwave secrets.
- [ ] Enable Cloud Vision API for V1 evidence scoring.
- [ ] Enable Google Maps Platform APIs needed by Android, iOS, and web.
