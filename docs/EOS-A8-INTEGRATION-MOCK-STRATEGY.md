# A8 - External Integration Mock Strategy

**EyeOnSite** - Manti Labs - May 2026 - v0.1-DRAFT

This document defines how EyeOnSite integrates external services during Demo MVP and V1 without leaking keys, blocking tests on vendor uptime, or hard-coding provider SDK details into product code.

## Principles

- Production code depends on EyeOnSite interfaces, not provider SDKs directly.
- Unit tests use fakes and fixtures only.
- Integration tests use provider test mode only when explicitly enabled with environment variables.
- No live or test secret key is committed to git.
- Cloud Functions read secrets from Google Secret Manager.
- Mobile and web clients only receive publishable/public configuration.
- Web landing analytics must not expose private operational data or user PII.

## Stripe

Official docs:
- Payment docs: `https://docs.stripe.com/payments`
- Webhooks: `https://docs.stripe.com/webhooks`
- Testing: `https://docs.stripe.com/testing`

Demo MVP:
- Use a simulated escrow state machine.
- Store `paymentStatus = SIMULATED_HELD | SIMULATED_RELEASED | SIMULATED_REFUNDED`.
- Do not call Stripe from Android, iOS, or web.

V1 mock shape:
- `EscrowPaymentGateway` interface in shared/domain or Cloud Functions boundary.
- `FakeEscrowPaymentGateway` for unit tests.
- `StripeEscrowPaymentGateway` behind Cloud Functions only.
- Fixtures cover `payment_intent.succeeded`, `payment_intent.payment_failed`, refund success, and refund failure.

Required safeguards:
- Use PaymentIntent IDs as idempotency keys for escrow ledger writes.
- Verify webhook signatures using Stripe's official library.
- Preserve the raw request body for signature verification.
- Separate test and live webhook endpoints/secrets.

## Flutterwave

Official docs:
- Testing: `https://developer.flutterwave.com/v3.0/docs/testing`
- Quick start: `https://developer.flutterwave.com/docs/quick-start`
- Webhooks: `https://developer.flutterwave.com/docs/webhooks`

Demo MVP:
- No live collection or payout.
- Inspector payout displays as simulated release after client acceptance.

V1 mock shape:
- `LocalPaymentGateway` for local client collection.
- `InspectorPayoutGateway` for inspector payouts.
- `FakeLocalPaymentGateway` and `FakeInspectorPayoutGateway` for tests.
- `FlutterwavePaymentGateway` and `FlutterwavePayoutGateway` live only in Cloud Functions.

Test-mode scenarios to encode in fixtures:
- Mobile money success with OTP `123456`.
- Failed mobile money numbers from Flutterwave test docs.
- Transfer success reference suffix `_PMCK`.
- Transfer failure reference suffix `_PMCK_ST_F`.
- Delayed transfer simulation with `DU_{minutes}` suffix.

Required safeguards:
- Verify webhook/callback signatures before ledger mutation.
- Treat provider status as input, not truth; reconcile against the EyeOnSite escrow ledger.
- Payouts are batchable and idempotent by `escrowTransactionId`.

## Firebase Analytics Web

Official docs:
- Web setup: `https://firebase.google.com/docs/web/setup`
- Analytics JS reference: `https://firebase.google.com/docs/reference/js/analytics`

Current state:
- Android uses Firebase Analytics through `FirebaseAnalyticsService`.
- Web uses `JsAnalyticsService`, which logs events locally until Firebase Analytics JS or gtag is configured.

V1 web analytics target:
- Initialize Firebase Web SDK through the Kotlin/JS bundle or a small checked-in bootstrap that reads public Firebase config.
- Use `AnalyticsService` everywhere so screens and CTAs never call Firebase directly.
- Track `screen_view`, `section_viewed`, `cta_clicked`, auth funnel events, task creation funnel events, and evidence review funnel events.

Required safeguards:
- Do not log phone numbers, exact addresses, payment details, uploaded evidence URLs, or precise GPS coordinates to analytics.
- Use stable IDs such as `cta_id`, `section`, `task_vertical`, `role`, and `funnel_step`.
- Keep analytics failures non-fatal.

## Cloud Vision AI

Demo MVP:
- Use a deterministic fake quality score from local rules.
- Store the score with `scoreSource = FAKE_DEMO`.

V1 mock shape:
- `EvidenceQualityScorer` interface.
- `FakeEvidenceQualityScorer` for tests.
- `CloudVisionEvidenceQualityScorer` in Cloud Functions.

Required safeguards:
- Never block evidence submission on AI provider failure.
- Persist raw AI signals separately from the final user-visible quality score.
- Add manual review fallback for low confidence or provider errors.

## Firebase Service Boundary

Client apps:
- Auth state, Firestore reads/writes allowed by security rules, Storage upload, FCM token registration, analytics.

Cloud Functions:
- Matching, fraud checks, escrow state changes, webhooks, payout batches, Vision AI calls, admin-only mutation.

Tests:
- Shared unit tests use fake repositories.
- Cloud Function tests use emulator or provider fixtures.
- End-to-end demo tests use Firebase emulators where possible and a dedicated test project when provider integration is required.
