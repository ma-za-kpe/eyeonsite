# 12 — Risks and Mitigations

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Risk Register

Every risk is scored honestly. Probability and impact are assessed at V1 stage (Accra, construction vertical, 50–100 tasks).

### Marketplace Risks

| # | Risk | Probability | Impact | Owner | Countermeasure | Review |
|---|------|-------------|--------|-------|----------------|--------|
| M1 | **Disintermediation** — clients and inspectors bypass platform after first task | HIGH | HIGH | Maku | Escrow protection (money safety), structured evidence reports (can't replicate via WhatsApp), dispute resolution, recurring task automation, trust badges. Make the platform more valuable than the relationship. | Monthly |
| M2 | **Chicken-and-egg** — can't attract clients without inspectors, can't attract inspectors without tasks | HIGH | CRITICAL | Maku | V0 solves this: you are the inspector. V1: pre-recruit 10-15 inspectors before marketing to clients. Guarantee minimum earnings for first month ($50/inspector even if tasks are low). | Weekly (V0-V1) |
| M3 | **Quality death spiral** — bad inspectors → bad reviews → clients leave → good inspectors leave → worse quality | MEDIUM | CRITICAL | Maku | Strict onboarding (test task required). Quality score tracking. Progressive discipline (3 strikes). Manual quality review of first 50 evidence packages. Fast deactivation of bad actors. | Weekly |
| M4 | **Pricing wrong** — too expensive for clients OR too cheap for inspectors | HIGH | HIGH | Maku | V0 price testing ($20-$50). Flexible pricing model. Monitor inspector retention (are they leaving for better-paying gigs?). Monitor client conversion (are they balking at price?). | Bi-weekly |
| M5 | **Low task density** — not enough tasks in one area to keep inspectors busy/engaged | HIGH | MEDIUM | Maku | Start hyper-local (Accra only). Focus marketing on corridors that map to Accra (UK-Ghana). Inspector expectations managed: this is supplemental income, not primary. | Monthly |

### Operational Risks

| # | Risk | Probability | Impact | Owner | Countermeasure | Review |
|---|------|-------------|--------|-------|----------------|--------|
| O1 | **Inspector safety** — inspector harassed, threatened, or injured at task site | MEDIUM | HIGH | Maku | Check-in/check-out system. Emergency contact. Training: never enter without permission, leave if unsafe. No-go zones. Insurance (V2). Incident response < 1hr. | Per incident |
| O2 | **Regulatory surprise** — new law in Ghana/Nigeria/Kenya affecting escrow, gig work, or data | LOW | HIGH | Maku | Monitor regulatory developments. Join fintech association (Ghana Fintech & Payment Association). Legal counsel before V2 expansion. Structure as marketplace (not payment processor) to stay below licensing thresholds. | Quarterly |
| O3 | **FX volatility** — GHS, NGN, or KES depreciate significantly against USD | HIGH (esp. NGN) | MEDIUM | Maku | Price in local currency where possible. Convert at funding time (locked rate). Accept small FX spread as cost of business. At V3+: FX hedging, multi-currency accounts. | Monthly |
| O4 | **Mobile money downtime** — MTN MoMo or M-Pesa has outages | MEDIUM | MEDIUM | Maku | Offer bank transfer as fallback payout. Queue payouts and retry. Communicate delays to inspectors proactively. | Per incident |
| O5 | **Inspector no-shows** — inspector accepts task but doesn't complete | MEDIUM | MEDIUM | Maku | SLA timer (must complete within X hours). Auto-cancel and reassign after timeout. No-show penalty (3 no-shows → suspension). Over-index on inspector pool (3:1 ratio). | Weekly |

### Technical Risks

| # | Risk | Probability | Impact | Owner | Countermeasure | Review |
|---|------|-------------|--------|-------|----------------|--------|
| T1 | **Offline sync bugs** — evidence captured offline gets corrupted, duplicated, or lost during sync | MEDIUM | HIGH | Maku | Idempotent uploads (same evidence ID = no duplicate). SQLDelight local queue with retry logic. Sync status visible to inspector ("3 photos pending upload"). End-to-end testing of offline → online flow. | Per sprint |
| T2 | **GPS spoofing undetected** — sophisticated spoofing bypasses multi-signal check | MEDIUM | CRITICAL | Maku | Layer defenses: GPS + network + WiFi + accelerometer + altitude. Device attestation (mock location API detection). Statistical analysis (impossible travel patterns). Manual review of flagged evidence. Accept that some spoofing will get through — focus on making it harder, not impossible. | Monthly |
| T3 | **Evidence pipeline false positives** — legitimate evidence flagged as fraud | MEDIUM | HIGH | Maku | Conservative thresholds initially (low false positive, accept some false negatives). Manual review queue for borderline cases. Inspector appeal process. Tune thresholds based on real data. | Bi-weekly |
| T4 | **Firebase cost spike** — unexpected Firestore reads/writes or Cloud Storage egress | LOW | MEDIUM | Maku | Set up GCP budget alerts ($50, $100, $200 thresholds). Optimize Firestore queries (avoid full-collection reads). Use Firestore offline cache to reduce server reads. Paginate evidence photo loading. | Monthly |
| T5 | **Compose for Web limitations** — landing page or dashboard hits a wall with Kotlin/JS | MEDIUM | LOW | Maku | Landing page is relatively simple (marketing content). Dashboard is internal tool (doesn't need to be polished). If CfW truly blocks something critical, fall back to a static HTML landing page and keep CfW for dashboard only. | Per sprint |
| T6 | **CMP iOS quality** — Compose Multiplatform iOS has rendering or performance issues | MEDIUM | MEDIUM | Maku | V1 is Android-first. iOS via CMP is secondary. If CMP quality is unacceptable for specific screens, drop to native SwiftUI for those screens (hybrid approach). Monitor JetBrains CMP release notes. | Per sprint |

### Business Risks

| # | Risk | Probability | Impact | Owner | Countermeasure | Review |
|---|------|-------------|--------|-------|----------------|--------|
| B1 | **Funding gap** — can't sustain development without revenue or investment | HIGH | CRITICAL | Maku | Keep Farmerline job through V0-V1. EyeOnSite cost is < $500/month. Don't quit until revenue or committed funding. Apply to grants (MEST, Google for Startups Africa, Norrsken). Angel investors in diaspora fintech space. | Monthly |
| B2 | **Single founder risk** — Maku gets sick, burns out, or gets a competing offer | MEDIUM | CRITICAL | Maku | Document everything (these docs exist for this reason). CLAUDE.md enables onboarding a developer quickly. Don't over-commit: V0 is designed to be low-effort validation, not a 6-month build. If Anthropic offer comes through, EyeOnSite needs a co-founder or pause. | Ongoing |
| B3 | **Competition launches** — NALA, LemFi, or a well-funded startup launches verification | LOW | HIGH | Maku | First-mover advantage in Accra. Network effects (inspector supply is defensible). Deep vertical knowledge. If a big player launches, consider: (a) acquisition target, (b) pivot to enterprise/API (they'll likely go consumer), (c) niche down to construction vertical and own it. | Quarterly |
| B4 | **NALA or LemFi builds this internally** — remittance platforms add verification | LOW | CRITICAL | Maku | They have different core competencies (payments, not physical operations). Building an inspector network is hard. More likely: they partner with EyeOnSite than build it. Position for partnership early. | Quarterly |

### Reputational Risks

| # | Risk | Probability | Impact | Owner | Countermeasure | Review |
|---|------|-------------|--------|-------|----------------|--------|
| R1 | **Viral negative experience** — client shares bad inspection on social media | MEDIUM | HIGH | Maku | Respond within 2 hours. Full refund + free re-inspection. Public accountability ("we messed up, here's what we're fixing"). Over-invest in quality for first 100 tasks. | Per incident |
| R2 | **Inspector misconduct** — inspector behaves unprofessionally, trespasses, or harasses | LOW | HIGH | Maku | Background checks. Training. Code of conduct with clear consequences. Immediate deactivation and investigation. If criminal: cooperate with authorities. | Per incident |
| R3 | **Data breach** — evidence photos, personal data, or payment info leaked | LOW | CRITICAL | Maku | Firebase Security Rules. Signed URLs (time-limited). TLS everywhere. No direct database access from developer machines. Incident response: notify affected users within 24hrs, notify DPA within 72hrs. | Quarterly |
| R4 | **Escrow loss** — platform error causes loss of client funds | LOW | CRITICAL | Maku | Double-entry ledger (every movement has debit + credit). Daily reconciliation. Server-side only escrow operations. Reserve fund: set aside 5% of revenue for escrow insurance. | Weekly |

## Risk Heat Map

```
         │ LOW IMPACT │ MED IMPACT │ HIGH IMPACT │ CRITICAL    │
─────────┼────────────┼────────────┼─────────────┼─────────────┤
HIGH     │            │ M4, M5     │ M1, O3      │ M2, B1      │
PROB     │            │            │             │             │
─────────┼────────────┼────────────┼─────────────┼─────────────┤
MEDIUM   │ T5         │ T6, O4, O5 │ M3, O1, T1, │ T2          │
PROB     │            │            │ T3, R1, B2  │             │
─────────┼────────────┼────────────┼─────────────┼─────────────┤
LOW      │            │ T4         │ O2, B3, R2  │ B4, R3, R4  │
PROB     │            │            │             │             │
```

**Top 5 risks to monitor actively:**
1. **M2 (Chicken-and-egg)** — existential at V0-V1. Solved by being Inspector #1.
2. **B1 (Funding gap)** — mitigated by keeping day job. Risk increases at V1-V2 transition.
3. **M1 (Disintermediation)** — the forever marketplace risk. Escrow is the moat.
4. **T2 (GPS spoofing)** — technical integrity of the product. Layer defenses.
5. **M3 (Quality death spiral)** — strict onboarding and fast deactivation are the answer.

## Quarterly Review Process

Every quarter (or after each version transition), review this document:
1. Re-score probability and impact based on real data
2. Add new risks discovered during operations
3. Archive risks that are no longer relevant
4. Update countermeasures based on what actually worked
5. Present to any investors/advisors

---

*See [EOS-07-SECURITY-AND-COMPLIANCE.md](./EOS-07-SECURITY-AND-COMPLIANCE.md) for technical threat model.*
*See [EOS-A4-DISPUTE-RESOLUTION.md](./EOS-A4-DISPUTE-RESOLUTION.md) for dispute-specific risk handling.*
