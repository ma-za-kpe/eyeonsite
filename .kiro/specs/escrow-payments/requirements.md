# Escrow & Payments — Requirements

**Spec ref:** EOS-08 | EOS-10 unit economics | DEC-008

## Functional Requirements

### FR-1: Demo MVP (simulated)
- "Post Task" → status change only, no real payment
- "Accept Evidence" → "Payment released — GHS X" status message
- No Stripe or Flutterwave integration in demo

### FR-2: V1 — Client Funding (Escrow)
- Diaspora clients: Stripe (USD/GBP/EUR cards + bank transfer)
- Local clients: Flutterwave (MTN MoMo, bank transfer)
- On payment success: `tasks/{id}.status` → `ESCROW_FUNDED`
- Funds held in Stripe/Flutterwave merchant account (logical escrow in Firestore)

### FR-3: V1 — Inspector Payout
- On `ACCEPTED`: Cloud Function releases payout via Flutterwave
- Payout methods: MTN MoMo (GH), M-Pesa (KE), bank transfer (NG/UG)
- Inspector receives: `budget × (1 - 0.20)` (platform takes 18% + 2% escrow fee)
- Payout within 24 hours (batched daily)

### FR-4: Fee Structure
- Platform commission: 18% of task budget
- Escrow processing fee: 2% of task budget
- Inspector receives: 80% of task budget
- Urgency surcharge: 1.5× (same-day), 2× (4-hour)

### FR-5: Refunds
- Client cancels before ESCROW_FUNDED: full refund
- Dispute resolved in client's favour: full refund
- Dispute partial resolution: partial refund

## Non-Functional
- All money movement server-side only (Cloud Functions)
- Double-entry ledger in Firestore `escrow_transactions`
- Webhook signatures verified (Stripe + Flutterwave)
- Daily reconciliation job
