# EOS-A10 - E2E Test Architecture

**EyeOnSite** - Manti Labs - June 2026 - v0.1-DRAFT

This document defines the first Kotlin end-to-end harness for EyeOnSite.

The goal is not to write a thin UI smoke test. The goal is to prove the business loop end to end:

`waitlist -> auth -> task post -> escrow -> match -> travel -> evidence capture -> review -> accept/revision/dispute -> payout`

The harness must exercise real product code paths, real Firebase-backed integration boundaries, and test-mode payment flows. Where live provider calls are not appropriate for CI, the harness must use emulator-backed or provider-test-mode substitutes behind the same interfaces.

## 1. Testing Philosophy

1. End-to-end tests validate user journeys, not isolated screens.
2. Shared logic still belongs in unit tests.
3. Cloud Functions logic still belongs in emulator or fixture-driven integration tests.
4. Payment providers must never be required for deterministic CI green builds.
5. Production endpoints are not the default e2e target.
6. The test harness must support at least two concurrent personas interacting with the same task.

## 2. Supported Environments

### 2.1 Emulator-first CI

Use when the test must be deterministic, repeatable, and safe to run on every commit.

Services:
- Firebase Auth Emulator
- Firestore Emulator
- Storage Emulator
- Functions Emulator

Payment behavior:
- Stripe test fixtures or a fake Stripe adapter
- Flutterwave test fixtures or a fake Flutterwave adapter
- Webhook events injected by test helper code, not by real provider callbacks

### 2.2 Staging integration smoke

Use when we want to validate the deployed stack against public/test credentials without touching production.

Services:
- Staging Firebase project
- Test keys or sandbox keys for providers
- Real deployed Cloud Functions endpoints in staging

Payment behavior:
- Test-mode Stripe PaymentIntent creation
- Test-mode Flutterwave flows
- Webhook verification still enforced

### 2.3 Production smoke

Use sparingly and manually.

Rules:
- No automatic money movement unless explicitly approved.
- No destructive cleanup.
- No broad regression suite against production.

## 3. Personas

The harness should model these personas explicitly.

### 3.1 anonymous_visitor

Purpose:
- lands on marketing pages
- reads value proposition
- submits waitlist or hiring interest

Primary surfaces:
- landing page
- waitlist endpoint

### 3.2 diaspora_client

Purpose:
- signs in
- posts a task
- funds escrow
- reviews submitted evidence
- accepts, requests revision, or disputes

Primary surfaces:
- mobile app
- web/admin if necessary for support
- payment endpoints

### 3.3 local_client

Purpose:
- same business role as a client, but uses local payment rails and local currency assumptions
- creates tasks and funds escrow through local payment mocks or test-mode provider flows

Primary surfaces:
- mobile app
- payment endpoints

### 3.4 inspector

Purpose:
- receives match ping
- accepts task
- travels to site
- captures GPS-stamped evidence in-app
- submits package
- responds to revision requests

Primary surfaces:
- mobile app
- evidence upload pipeline
- matching and review functions

### 3.5 ops_reviewer / admin

Purpose:
- monitors dispute and exception flows
- reviews failed evidence
- handles expired tasks, payout failures, or manual intervention

Primary surfaces:
- admin dashboard
- Firestore or functions administrative paths

### 3.6 founder

Purpose:
- receives waitlist and hiring interest
- monitors funnel health
- handles moderation/support flows

Primary surfaces:
- landing page
- waitlist notifications
- admin support paths

## 4. End-To-End User Journeys

These are the first harness scenarios that matter.

### 4.1 Waitlist and hiring interest

Flow:
1. anonymous_visitor lands on the site.
2. visitor clicks a CTA.
3. visitor submits waitlist or hiring interest.
4. backend stores the submission.
5. founder notification is generated.

Success assertions:
- request accepted
- data stored in Firestore
- notification trigger fires
- duplicate and bot traps are rejected or ignored

Failure assertions:
- invalid origin rejected
- invalid payload rejected
- cooldown/rate limiting enforced

### 4.2 Client posts task and funds escrow

Flow:
1. diaspora_client or local_client signs in.
2. client creates task with location, vertical, budget, and deadline.
3. client creates payment intent or funds escrow in test mode.
4. escrow state changes to funded.
5. task becomes eligible for matching.

Success assertions:
- authenticated user can create task
- task contains correct H3/geospatial fields
- payment intent metadata includes task ID and client UID
- escrow ledger entry is written

Failure assertions:
- unauthenticated user rejected
- wrong client cannot fund task
- invalid task state blocks funding
- payment provider failure leaves task unfunded

### 4.3 Inspector matched and accepts task

Flow:
1. task is created in a matching area.
2. inspector receives match or can discover the task.
3. inspector accepts task.
4. task transitions into in-progress state.

Success assertions:
- H3-based matching returns the expected inspector set
- accept call requires auth and App Check where enforced
- task state transition is valid

Failure assertions:
- mismatched inspector rejected
- stale/expired task rejected
- App Check rejection blocks mutation

### 4.4 Evidence capture and submit

Flow:
1. inspector opens task.
2. inspector captures in-app photos.
3. GPS, EXIF, timestamp, and attestation data are attached.
4. evidence uploads to Storage.
5. evidence document is written.
6. trust pipeline computes quality, fraud, and provenance flags.

Success assertions:
- upload path is private and deterministic
- evidence document stores metadata, not public URLs
- GPS and EXIF are present
- trust verdict is computed

Failure assertions:
- mock location flagged
- low-accuracy GPS flagged
- duplicate pHash flagged
- timestamp drift flagged
- attestation failure flagged

### 4.5 Review, accept, revision, dispute

Flow:
1. client reviews evidence.
2. client accepts or requests revision or disputes.
3. task status changes accordingly.
4. inspector receives notification.

Success assertions:
- accepted task can release escrow
- revision returns task to a resubmission state
- dispute enters dispute handling path

Failure assertions:
- invalid status transition rejected
- wrong client cannot review task
- duplicate review events are idempotent

### 4.6 Payout and reconciliation

Flow:
1. accepted task triggers escrow release.
2. payout job runs.
3. provider test-mode payout or fake payout path is exercised.
4. ledger and task records are updated.

Success assertions:
- payout is idempotent
- ledger records are balanced
- payout failure is queued for retry or manual review

Failure assertions:
- webhook replay ignored
- missing signature rejected
- payout mismatch does not silently pass

## 5. Test Matrix

The table below is the minimum scenario matrix for the first harness version.

| ID | Persona(s) | Scenario | Endpoint Surface | Expected Result | Negative Coverage |
| --- | --- | --- | --- | --- | --- |
| E2E-01 | anonymous_visitor | Submit waitlist | `submitWaitlist` | submission stored, founder notified | bad origin, duplicate email, bot trap |
| E2E-02 | diaspora_client | Post task | Firestore write + task trigger | task created with geospatial contract | invalid payload, unauthenticated write |
| E2E-03 | diaspora_client | Fund escrow | `createPaymentIntent`, `stripeWebhook`, `fundTask` | task moves to funded, ledger written | wrong user, bad state, webhook signature fail |
| E2E-04 | inspector | Receive match and accept | `onTaskCreated`, `acceptTask` | inspector matched and task accepted | stale task, non-matched inspector, App Check reject |
| E2E-05 | inspector | Capture evidence | Storage upload + evidence write + `onEvidenceSubmitted` | private upload path, trust signals computed | mock GPS, EXIF mismatch, low quality |
| E2E-06 | diaspora_client | Review evidence and accept | `onTaskReviewed`, `releaseEscrow` | task accepted, payout triggered | invalid transition, unauthorized review |
| E2E-07 | diaspora_client | Request revision | `onTaskReviewed` | task reopens for resubmission | duplicate review, bad state |
| E2E-08 | diaspora_client + inspector | Dispute path | `onTaskReviewed`, dispute handling | dispute recorded, notifications sent | replayed dispute, wrong actor |
| E2E-09 | ops_reviewer | Exception handling | admin/dashboard paths | manual review state and queue visible | unauthorized admin action |
| E2E-10 | founder | Funnel monitoring | landing + analytics | CTA and section events recorded | PII leakage, analytics failure non-fatal |

## 6. Endpoint Map For The Harness

The harness should interact with the following surfaces.

### Public HTTP

- `submitWaitlist`
- `stripeWebhook`

### Callable

- `createPaymentIntent`
- `fundTask`
- `releaseEscrow`
- `acceptTask`
- `seedFirestore`

### Firestore/trigger-backed flows

- `onTaskCreated`
- `onEvidenceSubmitted`
- `onTaskReviewed`
- `onInspectorApplied`
- `expireStaleTasks`
- `notifyWaitlistSignup`

The harness must not confuse helpers with endpoints. For example, Flutterwave payout logic is a helper used by `releaseEscrow`, not a separate endpoint.

## 7. Kotlin Harness Structure

The first implementation should be an Android instrumentation suite under `app/androidApp/src/androidTest`.

Recommended structure:

```text
app/androidApp/src/androidTest/
  kotlin/com/maku/eyeonsite/e2e/
    E2ERunner.kt
    E2EEnvironment.kt
    personas/
      AnonymousVisitorPersona.kt
      DiasporaClientPersona.kt
      LocalClientPersona.kt
      InspectorPersona.kt
      OpsReviewerPersona.kt
      FounderPersona.kt
    scenarios/
      WaitlistScenario.kt
      FundAndMatchScenario.kt
      EvidenceCaptureScenario.kt
      ReviewAndPayoutScenario.kt
      DisputeScenario.kt
    fixtures/
      StripeFixtures.kt
      FlutterwaveFixtures.kt
      FirestoreSeedFixtures.kt
    assertions/
      TaskAssertions.kt
      EvidenceAssertions.kt
      EscrowAssertions.kt
      AnalyticsAssertions.kt
```

### 7.1 Core runner

`E2ERunner` should:
- boot the app under a known environment profile
- inject test configuration
- authenticate the relevant persona
- execute the scenario steps
- collect logs, screenshots, and Firestore snapshots
- fail fast on invariant violations

### 7.2 Environment abstraction

`E2EEnvironment` should describe:
- `EMULATOR`
- `STAGING`
- `PRODUCTION_SMOKE`

It should also hold:
- Firebase project ID
- Functions base URL
- auth mode
- payment mode
- cleanup policy

### 7.3 Persona abstraction

Each persona should be a small scripted actor, not a giant test case.

Example responsibilities:
- sign in
- seed or retrieve identity
- call the right UI or service path
- assert role-specific access

### 7.4 Scenario DSL

The first DSL should read like domain language:

```kotlin
clientPostsTask()
  .fundEscrow()
  .waitForMatch()
  .asInspectorAccepts()
  .captureEvidence()
  .submitEvidence()
  .asClientReviews()
  .acceptOrRequestRevision()
```

Keep the DSL thin. It should orchestrate existing app code and backend calls, not reimplement business rules.

## 8. Authenticated User Model

The harness should authenticate through the same mechanisms the product uses.

Preferred order:
1. Firebase Auth emulator or staging auth
2. test phone OTP or custom test token flow
3. app-level persona bootstrap for local-only runs

For instrumented tests, the harness should be able to represent:
- one client session
- one inspector session
- optional admin session

The suite should support switching identities without restarting the whole run when possible.

## 9. Payment Simulation Model

Do not use real money in automated e2e.

Required behavior:
- Stripe should be test-mode or fixture-driven
- Flutterwave should be test-mode or fixture-driven
- webhook verification should still be exercised
- payment state changes must come from provider-shaped events, not direct client mutation

Minimum payment assertions:
- PaymentIntent created with expected metadata
- webhook signature failure rejected
- escrow held only after provider success
- payout request created only after release conditions are met
- payout failure handled without corrupting ledger state

## 10. Evidence Trust Assertions

The e2e harness should verify trust outcomes, not just upload success.

At minimum assert:
- GPS coordinates present
- location accuracy within threshold when expected
- EXIF timestamp present
- perceptual hash stored
- server-side provenance manifest created
- mock location detected
- duplicated or suspicious evidence flagged
- auto-accept eligibility depends on trust score

## 11. Failure Injection

The harness should be able to force failures intentionally.

Supported injection points:
- auth token missing or expired
- App Check missing or invalid
- Stripe webhook signature invalid
- Flutterwave callback invalid
- GPS spoofed or mock location flagged
- EXIF mismatch
- evidence upload failure
- Firestore write denied by rules
- escrow state invalid
- payout API error

This is important because a happy-path-only suite will miss the real risk surface.

## 12. Assertions By Layer

### UI assertions
- correct screen shown
- button enabled/disabled state matches business rules
- error and success banners appear

### Backend assertions
- correct Firestore documents written
- correct function triggered
- correct status transition persisted
- correct ledger entry created

### Security assertions
- unauthorized caller rejected
- App Check enforced where enabled
- webhook signature verification enforced
- private storage paths used

### Observability assertions
- analytics event emitted with safe fields only
- failure does not crash the app
- logs contain enough context for debugging

## 13. CI Policy

The suite should be split into tiers.

### Tier 1
- unit tests
- emulator integration tests
- a short Android e2e smoke path

### Tier 2
- staging smoke suite
- full persona matrix

### Tier 3
- manual production smoke only

This keeps the fast path reliable while still allowing realistic testing.

## 14. Implementation Order

1. Create the e2e test package layout.
2. Build the environment/profile abstraction.
3. Add persona wrappers for client and inspector first.
4. Add waitlist, post-task, fund-escrow, match, evidence, review, payout flows.
5. Add failure injection utilities.
6. Add assertion helpers for Firestore, Storage, and analytics.
7. Wire CI targets for emulator and staging runs.
8. Expand to admin/founder flows after the core buyer-inspector loop is stable.

## 15. Definition Of Done

The first Kotlin e2e harness is complete when:
- a client and inspector can be simulated in one run
- the main task lifecycle is exercised end to end
- provider test modes or emulator fixtures cover payment steps
- at least one success and one failure path exist per critical flow
- the suite runs from CI without real money
- the suite produces useful logs and state snapshots on failure

## 16. Practical Guidance

1. Keep the first suite small and valuable.
2. Prefer deterministic fixtures over live dependencies in CI.
3. Model roles explicitly rather than passing raw user IDs around.
4. Treat analytics as part of the funnel, but never leak private data into it.
5. Expand to more vertical-specific scenarios only after the generic loop is stable.

