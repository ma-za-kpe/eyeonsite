# 08 — Escrow and Payments Specification

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Payment Architecture Overview

EyeOnSite operates dual payment rails because the two sides of the marketplace use different currencies and payment methods.

```
DIASPORA CLIENT (UK/US/EU)              LOCAL CLIENT (Ghana/Nigeria/Kenya)
    │                                        │
    │ USD / GBP / EUR                        │ GHS / NGN / KES
    │ Credit card, bank transfer             │ MoMo, M-Pesa, bank transfer
    │                                        │
    ▼                                        ▼
┌──────────────────┐               ┌──────────────────┐
│     STRIPE        │               │   FLUTTERWAVE     │
│                  │               │   (fund side)     │
│ Manti Labs LLC   │               │                  │
│ (Delaware) is    │               │ Local payment    │
│ the merchant     │               │ collection       │
└────────┬─────────┘               └────────┬─────────┘
         │                                  │
         └──────────┬───────────────────────┘
                    │
                    ▼
         ┌──────────────────┐
         │   ESCROW LEDGER   │
         │                  │
         │ Firestore        │
         │ collection:      │
         │ escrow_transactions
         │                  │
         │ Status: HELD     │
         │                  │
         │ Holds funds      │
         │ until evidence   │
         │ accepted or      │
         │ dispute resolved │
         └────────┬─────────┘
                  │
                  │ Evidence ACCEPTED → Release
                  │
                  ▼
         ┌──────────────────┐
         │   FLUTTERWAVE     │
         │   (payout side)   │
         │                  │
         │ Inspector payout: │
         │ • MTN MoMo (GH)  │
         │ • M-Pesa (KE)    │
         │ • Bank (NG, UG)  │
         └──────────────────┘
```

## End-to-End Money Flow

### Step 1: Client Funds Escrow

**Diaspora client (Stripe path):**
1. Client creates task, selects budget (displayed in their currency: USD/GBP/EUR)
2. Client enters payment method (card or bank transfer)
3. Frontend creates Stripe PaymentIntent via Cloud Function `createEscrowPaymentIntent()`
4. Stripe captures payment immediately
5. Cloud Function creates `EscrowTransaction` in Firestore with status `HELD`
6. Task status moves from `POSTED` → `ESCROW_FUNDED`

**Local client (Flutterwave path):**
1. Client selects MoMo/M-Pesa/bank transfer
2. Frontend initiates Flutterwave payment via Cloud Function `createLocalEscrowPayment()`
3. Flutterwave processes payment (MoMo USSD prompt on client's phone)
4. Flutterwave webhook confirms payment → Cloud Function updates Firestore
5. Task status → `ESCROW_FUNDED`

### Step 2: Escrow Hold Period

- Funds sit in platform's Stripe/Flutterwave account (not in a separate escrow bank account — that would require a banking license)
- `EscrowTransaction.status = HELD`
- Timer starts when evidence is submitted: `autoAcceptAt = submittedAt + 48 hours`
- During hold: funds cannot be touched by platform, client, or inspector

### Step 3: Evidence Review and Release

**Normal flow (client accepts):**
1. Client reviews evidence, clicks "Accept"
2. Cloud Function `releaseEscrow()` triggered
3. Validates: evidence status = ACCEPTED, fraud score < threshold
4. Computes: platform fee (18% of gross), inspector payout (82% of gross)
5. Initiates Flutterwave transfer to inspector's MoMo/bank account
6. Updates ledger: HELD → RELEASED
7. Both parties prompted to rate each other

**Auto-accept flow (48hr timer expires):**
1. Scheduled Cloud Function `checkAutoAcceptTimers()` runs hourly
2. Finds evidence packages where `now > autoAcceptAt` and `status = UNDER_REVIEW`
3. Auto-transitions to ACCEPTED
4. Same release flow as above

**Revision flow:**
1. Client clicks "Request Revision" with specific feedback
2. Escrow remains HELD
3. Inspector notified with revision details
4. Inspector resubmits → new 48hr timer starts
5. Maximum 2 revision cycles before platform mediates

**Dispute flow:**
1. Client clicks "Dispute" → escrow frozen at DISPUTED status
2. Platform mediates (see EOS-A4)
3. Resolution: full release to inspector / full refund to client / partial (split)
4. Partial refund: compute amounts, refund via original payment method (Stripe refund or Flutterwave reversal), release remainder to inspector

### Step 4: Inspector Payout

```
Inspector payout = grossAmount - platformFee - escrowFee

Example (GHS 60 task):
  grossAmount:    GHS 60.00
  platformFee:    GHS 10.80 (18%)
  escrowFee:      GHS 1.20  (2%)
  inspectorPayout: GHS 48.00

Payout via Flutterwave Transfer API:
  POST /v3/transfers
  {
    "account_bank": "MTN",
    "account_number": "0551234567",  // Inspector's MoMo number
    "amount": 48.00,
    "currency": "GHS",
    "reference": "eos_payout_{txnId}",
    "narration": "EyeOnSite task payment — Task #{taskId}"
  }
```

## Fee Structure

| Fee | Who Pays | Amount | Notes |
|-----|----------|--------|-------|
| **Platform commission** | Deducted from task budget | 15–20% (18% default) | Lower for enterprise subscriptions (15%). Higher for urgent (20%). |
| **Escrow processing fee** | Deducted from task budget | 2–3% | Covers Stripe/Flutterwave processing costs |
| **Stripe processing** | Platform absorbs (from commission) | 2.9% + $0.30 (cards) | Baked into the escrow fee |
| **Flutterwave processing (fund)** | Platform absorbs | 1.4% (local cards), varies for MoMo | Baked into the escrow fee |
| **Flutterwave transfer (payout)** | Platform absorbs | Flat fee per transfer (GHS ~1.00) | Baked into the escrow fee |
| **Currency conversion** | Client bears spread | Stripe/Flutterwave FX rate | Displayed to client before confirming payment |
| **Urgency surcharge** | Client pays | 1.5x (same-day) or 2x (4-hour) | Added to base task budget |

### Revenue per Task (Unit Economics)

```
Consumer task (GHS 60 / ~$4.60):
  Gross revenue:      GHS 60.00
  Inspector payout:   GHS 48.00 (80%)
  Platform fee:       GHS 10.80 (18%)
  Escrow/processing:  GHS  1.20 (2%)
  Net to platform:    GHS 10.80
  Less: Stripe/FW:    GHS  2.50 (est. processing)
  Net margin:         GHS  8.30 ($0.64) → ~14% net margin

Enterprise task (GHS 120 / ~$9.20):
  Gross revenue:      GHS 120.00
  Inspector payout:   GHS 102.00 (85%)
  Platform fee:       GHS  18.00 (15%)
  Less: processing:   GHS   4.50
  Net margin:         GHS  13.50 ($1.04) → ~11% net margin

Target: $6-9 net revenue per task at scale (mix of consumer + enterprise)
```

## Multi-Currency Handling

**Decision: Convert at funding time, not release time.**

Rationale: The client sees a price in their currency (USD/GBP/EUR). The inspector expects a payout in local currency (GHS/KES/NGN). If we convert at release time (48+ hours later), FX fluctuation creates unpredictable payouts. Converting at funding time means the inspector payout is deterministic from the moment escrow is funded.

```
Client pays: $5.00 USD via Stripe
  → Stripe converts to platform's settlement currency (USD)
  → Platform holds $5.00 in escrow
  → At release: Platform instructs Flutterwave to pay GHS 60.00
    (using current FX rate at release time for the GHS amount)
  → FX risk: Platform absorbs small spread between funding and release rates
  → Acceptable at low volume; hedge at V3+ scale
```

**Alternative considered:** Hold in local currency from the start. Rejected because Stripe settles in USD/GBP/EUR to Manti Labs LLC (Delaware), and converting immediately would require a GHS-denominated bank account with Flutterwave receiving funds.

## Double-Entry Ledger Design

Every money movement in EyeOnSite has a corresponding debit and credit entry. The ledger lives in Firestore as entries within each `EscrowTransaction`.

```
Task created, escrow funded:
  DEBIT:  client_account    GHS 60.00  "Escrow fund — Task #abc"
  CREDIT: escrow_hold       GHS 60.00  "Escrow hold — Task #abc"

Evidence accepted, escrow released:
  DEBIT:  escrow_hold       GHS 60.00  "Escrow release — Task #abc"
  CREDIT: inspector_payout  GHS 48.00  "Inspector payout — Task #abc"
  CREDIT: platform_revenue  GHS 10.80  "Platform commission — Task #abc"
  CREDIT: processing_fees   GHS  1.20  "Processing fee — Task #abc"

Dispute — full refund:
  DEBIT:  escrow_hold       GHS 60.00  "Dispute refund — Task #abc"
  CREDIT: client_refund     GHS 60.00  "Client refund — Task #abc"

Dispute — partial (60/40 split):
  DEBIT:  escrow_hold       GHS 60.00  "Dispute resolution — Task #abc"
  CREDIT: client_refund     GHS 36.00  "Partial refund (60%) — Task #abc"
  CREDIT: inspector_payout  GHS 24.00  "Partial payout (40%) — Task #abc"
```

## Stripe Integration Details

**Stripe entity:** Manti Labs LLC (Delaware) — already on Stripe Atlas.

**V1 approach: Direct charges.**
- Client pays Manti Labs directly via Stripe PaymentIntent
- Platform holds funds and manages payout to inspectors separately via Flutterwave
- Simple. No Stripe Connect complexity.

**V3+ approach: Stripe Connect (marketplace model).**
- Inspectors don't need Stripe accounts (they're paid via Flutterwave)
- But Stripe Connect enables: separate escrow accounting, automatic 1099 reporting for US clients, better dispute handling
- Evaluate when volume justifies the engineering effort

```javascript
// Cloud Function: createEscrowPaymentIntent
const paymentIntent = await stripe.paymentIntents.create({
  amount: taskBudgetInCents,        // e.g., 500 for $5.00
  currency: clientCurrency,          // 'usd', 'gbp', 'eur'
  payment_method_types: ['card'],
  metadata: {
    taskId: task.id,
    clientId: task.clientId,
    type: 'escrow_fund',
  },
  description: `EyeOnSite escrow — Task ${task.id}`,
});
```

## Flutterwave Integration Details

**Two uses:**
1. **Fund side (local clients):** Collect GHS/NGN/KES via MoMo, M-Pesa, bank transfer
2. **Payout side (inspectors):** Disburse to MoMo/M-Pesa/bank account

```javascript
// Cloud Function: initiateInspectorPayout
const transfer = await flw.Transfer.initiate({
  account_bank: inspector.payoutMethod.provider,  // "MTN" for MoMo
  account_number: inspector.payoutMethod.accountNumber,
  amount: payoutAmount,
  currency: inspector.payoutMethod.currency,       // "GHS"
  reference: `eos_payout_${escrowTxn.id}`,
  narration: `EyeOnSite payment — Task #${task.id}`,
  callback_url: `https://us-central1-eyeonsite-prod.cloudfunctions.net/flutterwavePayoutWebhook`,
});
```

**Webhook handling:**
```javascript
// Cloud Function: flutterwavePayoutWebhook
exports.flutterwavePayoutWebhook = onRequest(async (req, res) => {
  // Verify webhook hash
  const hash = req.headers['verif-hash'];
  if (hash !== process.env.FLUTTERWAVE_WEBHOOK_HASH) {
    return res.status(401).send('Unauthorized');
  }

  const payload = req.body;
  if (payload.event === 'transfer.completed') {
    // Update escrow transaction status
    // Update inspector earnings
    // Send FCM notification to inspector
  }
  res.status(200).send('OK');
});
```

## Reconciliation

**Daily automated reconciliation (scheduled Cloud Function, 2:00 AM UTC):**

1. Fetch all Stripe charges from previous day via Stripe API
2. Fetch all Flutterwave collections from previous day via Flutterwave API
3. Fetch all Flutterwave transfers (payouts) from previous day
4. Compare against Firestore `escrow_transactions` ledger
5. Flag discrepancies: amount mismatch, missing entries, status mismatch
6. Generate reconciliation report → stored in Cloud Storage → emailed to admin

**Manual reconciliation:** Monthly by Maku (V0–V2). Outsource to bookkeeper at V3.

---

*See [EOS-07-SECURITY-AND-COMPLIANCE.md](./EOS-07-SECURITY-AND-COMPLIANCE.md) for financial trust controls and escrow licensing.*
*See [EOS-10-BUSINESS-MODEL.md](./EOS-10-BUSINESS-MODEL.md) for fee structure rationale and unit economics.*
