# 10 — Business Model

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Revenue Streams

### V1 Revenue (Consumer Marketplace)

| Stream | Rate | Who Pays | When Active |
|--------|------|----------|-------------|
| **Platform commission** | 18% of task budget | Deducted from gross | V1 |
| **Escrow processing fee** | 2% of task budget | Deducted from gross | V1 |
| **Urgency surcharge** | 1.5x (same-day), 2x (4-hour) | Client pays premium | V1 |

### V2 Revenue (Multi-Vertical)

| Stream | Rate | Who Pays | When Active |
|--------|------|----------|-------------|
| All V1 streams | — | — | — |
| **Recurring task discount** | 15% commission (vs. 18%) | Client incentive | V2 |
| **Video task premium** | +$5 per task | Client | V2 |

### V3 Revenue (Enterprise + API)

| Stream | Rate | Who Pays | When Active |
|--------|------|----------|-------------|
| All V2 streams | — | — | — |
| **Enterprise subscription — Starter** | $99/mo (includes 10 tasks) | Enterprise client | V3 |
| **Enterprise subscription — Growth** | $299/mo (includes 50 tasks) | Enterprise client | V3 |
| **Enterprise subscription — Scale** | $999/mo (includes 200 tasks, dedicated support) | Enterprise client | V3 |
| **API overage** | $3–$8 per task above quota | Enterprise client | V3 |
| **Custom reports** | $15–$50 per branded report | Enterprise client | V3 |
| **Inspector certification** | $20 per certification course | Inspector | V3 |

### V4+ Revenue (Data + Platform)

| Stream | Rate | Who Pays | When Active |
|--------|------|----------|-------------|
| **Data API** | $0.10–$1.00 per query | Data consumers (insurers, lenders) | V4 |
| **White-label SDK** | $5,000–$20,000/year license | Platform partners | V4 |
| **EyeOnSite Score™** | $2–$5 per verification score | Lenders, insurers | V4 |

## Unit Economics

### Per-Task Economics (Consumer)

```
Average task budget:           GHS 60.00  (~$4.60 USD)

Revenue:
  Platform commission (18%):   GHS 10.80
  Escrow fee (2%):             GHS  1.20
  Gross revenue:               GHS 12.00

Costs:
  Stripe processing (~3.2%):   GHS  1.92
  Flutterwave payout (~1.5%):  GHS  0.90
  Cloud Vision API (~$0.01):   GHS  0.13
  Cloud Functions compute:     GHS  0.05
  Cloud Storage:               GHS  0.02
  Total COGS:                  GHS  3.02

Gross margin per task:         GHS  8.98  (~$0.69)
Gross margin %:                74.8%
```

### Per-Task Economics (Enterprise)

```
Average enterprise task:       GHS 120.00 (~$9.20 USD)

Revenue:
  Platform commission (15%):   GHS 18.00
  Escrow fee (2%):             GHS  2.40
  Gross revenue:               GHS 20.40

Costs:
  Payment processing:          GHS  3.60
  Flutterwave payout:          GHS  1.80
  Cloud services:              GHS  0.25
  Total COGS:                  GHS  5.65

Gross margin per task:         GHS 14.75  (~$1.13)
Gross margin %:                72.3%
```

## Revenue Projections

### Conservative Scenario

| Period | Tasks/Month | Avg Revenue/Task | MRR | Notes |
|--------|-------------|-----------------|-----|-------|
| V0 (Month 1) | 10 | $3 (manual, low price) | $30 | Proving demand |
| V1 (Month 3) | 30 | $4 | $120 | Accra, construction |
| V1 (Month 5) | 80 | $4.50 | $360 | Growing organically |
| V2 (Month 8) | 200 | $5 | $1,000 | 2 cities, 3 verticals |
| V2 (Month 10) | 400 | $5.50 | $2,200 | Enterprise pilots start |
| V3 (Month 14) | 800 | $6 (blended) | $4,800 | + subscription revenue |
| V3 (Month 18) | 1,500 | $7 (blended) | $10,500 | Enterprise scaling |

### Customer Acquisition

**Demand side (clients):**
- V0: Free — organic outreach to diaspora WhatsApp/Facebook groups
- V1: $0–$50/month — content marketing (LinkedIn, X, diaspora blogs), referrals
- V2: $200–$500/month — Facebook/Instagram ads targeting diaspora communities, partnerships with remittance apps
- V3: $1,000–$2,000/month — enterprise sales, conference sponsorships, content marketing
- Target CAC: $5–$15 per consumer client, $50–$200 per enterprise client

**Supply side (inspectors):**
- V0: N/A (you are the inspector)
- V1: University campus posters, WhatsApp gig worker groups, field agent network referrals. Cost: ~$2/applicant, $10/verified inspector
- V2: Referral bonus (GHS 20 per successful referral), partnerships with surveyor associations
- Target: 3:1 inspector-to-active-task ratio (always have inspectors available)

**Anti-Disintermediation Strategy:**

Why clients stay on platform (don't just WhatsApp inspectors directly):

1. **Escrow protection** — money is safe; no risk of inspector disappearing
2. **Structured evidence** — GPS-verified, time-stamped, fraud-checked reports vs. random WhatsApp photos
3. **Quality guarantee** — platform mediates disputes, ensures minimum quality standards
4. **Inspector replacement** — if an inspector doesn't show, platform auto-reassigns
5. **History and continuity** — all evidence stored, searchable, comparable across visits
6. **Trust badges** — inspector ratings, tier levels, verified identity
7. **Recurring task automation** — monthly checks auto-scheduled, no manual coordination
8. **Enterprise features** — API, bulk tasks, custom reports, SLAs — can't get these off-platform

## Sensitivity Analysis

### What Breaks the Model

| Scenario | Impact | Threshold | Mitigation |
|----------|--------|-----------|-----------|
| **Take rate too high** | Inspectors leave for direct relationships | > 25% | Cap at 20%; premium services justify the fee |
| **Average task value too low** | Can't cover COGS | < $2/task (GHS 26) | Minimum task budget: GHS 30. Focus on higher-value verticals. |
| **Dispute rate too high** | Refunds eat margin; reputation damage | > 20% | Better inspector training, clearer templates, faster mediation |
| **Payment processing costs spike** | Margin compressed | > 5% of gross | Negotiate volume discounts with Stripe/Flutterwave at V3 |
| **Inspector churn** | Quality drops, supply shortage | > 40% monthly churn | Better pay, faster payouts, gamification (streaks, bonuses), community |
| **Disintermediation** | Clients bypass platform | > 15% of tasks go off-platform | Strengthen escrow value prop, add features impossible off-platform |
| **FX volatility** | GHS/NGN depreciation eats USD-denominated margins | > 15% annual depreciation | Price in local currency, adjust periodically, FX hedging at V4 |

### Break-Even Analysis

```
Monthly fixed costs (V1):
  Firebase Blaze plan:             $25  (estimate, pay-as-you-go)
  Domain + hosting:                $12
  Stripe Atlas:                    $0   (already paid)
  Google Maps API:                 $50  (estimate, depends on volume)
  Flutterwave fees:                $0   (per-transaction only)
  Cloud Vision API:                $10  (estimate)
  Miscellaneous (SIM cards, transport for V0): $50
  Total fixed:                     ~$147/month

Variable cost per task:            ~$0.50

Break-even:
  $147 / ($4.50 avg revenue - $0.50 variable cost) = 37 tasks/month

V1 target is 80 tasks/month by Month 5 → above break-even.
```

Note: This excludes Maku's time (opportunity cost) which is the real cost. The business becomes self-sustaining only when revenue justifies full-time commitment.

---

*See [EOS-08-ESCROW-AND-PAYMENTS.md](./EOS-08-ESCROW-AND-PAYMENTS.md) for detailed fee implementation.*
*See [EOS-09-ROADMAP.md](./EOS-09-ROADMAP.md) for version-by-version scope.*
*See [EOS-11-EXECUTION-PLAN.md](./EOS-11-EXECUTION-PLAN.md) for budget allocation.*
