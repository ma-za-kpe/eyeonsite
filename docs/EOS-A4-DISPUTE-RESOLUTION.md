# A4 — Dispute Resolution Framework

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Design Principle

Disputes are not failures — they are trust calibration events. A dispute resolved well builds more trust than a task that went perfectly, because both parties see the platform working.

The goal is not zero disputes. The goal is fast, fair, transparent resolution with clear outcomes.

---

## Dispute Triggers

A client can dispute an evidence package within the 48-hour review window. After auto-accept, disputes are still possible but limited (must be filed within 7 days of auto-accept, and only for fraud — not quality).

### Dispute Categories

| Category | Description | Auto-Resolvable? | Typical Resolution |
|----------|------------|-------------------|-------------------|
| **EVIDENCE_QUALITY** | Photos blurry, wrong angles, incomplete checklist, insufficient detail | Partially | Revision request → inspector resubmits |
| **TASK_NOT_COMPLETED** | Inspector claims to have visited but evidence doesn't match expectations | Partially | GPS analysis → auto-resolve if clear |
| **INSPECTOR_NO_SHOW** | Inspector accepted but never visited the site, deadline passed | Yes | Auto-refund + reassign |
| **WRONG_SITE** | Inspector went to the wrong location | Yes | GPS mismatch → auto-refund + reassign |
| **EVIDENCE_FRAUD** | Suspected fabricated photos, GPS spoofing, recycled/stock images | No | Manual investigation |
| **CLIENT_ABUSE** | Inspector reports: client requested inappropriate actions, provided false information, harassed inspector | No | Manual investigation |
| **OTHER** | Doesn't fit above categories. Client describes issue in free text. | No | Manual review |

---

## Three-Tier Resolution System

### Tier 1: Automated Resolution (< 1 hour)

Automated checks run immediately when a dispute is filed. If the evidence is clear-cut, resolve without human intervention.

```
Client files dispute
    │
    ├── Category: INSPECTOR_NO_SHOW
    │   └── Check: Is there an evidence package submitted?
    │       ├── NO → Auto-refund. Auto-reassign task. Inspector gets no-show penalty.
    │       └── YES → Escalate to Tier 2 (evidence exists, need to evaluate)
    │
    ├── Category: WRONG_SITE
    │   └── Check: GPS distance between evidence and task location
    │       ├── > 500m → Auto-refund. Inspector flagged for review.
    │       ├── 200–500m → Warning to both parties. Escalate to Tier 2.
    │       └── < 200m → GPS matches. Escalate to Tier 2 (dispute is about something else).
    │
    ├── Category: EVIDENCE_QUALITY
    │   └── Check: Minimum requirements met?
    │       ├── Photo count below requirement → Auto-revision request (inspector must resubmit)
    │       ├── Checklist incomplete → Auto-revision request
    │       ├── Quality score < 0.30 → Auto-revision request
    │       └── All minimums met → Escalate to Tier 2 (subjective quality disagreement)
    │
    ├── Category: TASK_NOT_COMPLETED
    │   └── Check: Evidence package exists AND GPS matches AND photos present
    │       ├── No evidence → Auto-refund
    │       ├── GPS doesn't match → Auto-refund
    │       └── Evidence exists, GPS matches → Escalate to Tier 2
    │
    └── Category: EVIDENCE_FRAUD / CLIENT_ABUSE / OTHER
        └── Always escalate to Tier 2
```

**Tier 1 outcomes:**
- Auto-refund (full refund to client, inspector gets $0)
- Auto-revision request (escrow stays held, inspector has 24hrs to resubmit)
- Escalate to Tier 2

**Auto-resolution target:** 40% of disputes resolved at Tier 1.

### Tier 2: Team Mediation (< 72 hours)

Human review by platform team (Maku at V0–V2, ops lead at V3+).

**Process:**

1. **Gather context (30 min)**
   - Review full evidence package (all photos, checklist, GPS data, fraud signals)
   - Read client's dispute reason and notes
   - Read inspector's automatic rebuttal (inspector is prompted to respond within 24hrs)
   - Check inspector's history (previous disputes, trust score, completion rate)
   - Check client's history (previous disputes, is this a pattern?)

2. **Assess (30 min)**
   - Is the client's complaint legitimate?
   - Did the inspector follow the template requirements?
   - Is there any fraud signal?
   - Would a reasonable person be satisfied with this evidence?

3. **Request additional information (if needed, +24hrs)**
   - Ask inspector to provide additional context (e.g., "why is there no photo of the south side?")
   - Ask client to clarify expectations (e.g., "what specifically were you expecting to see?")

4. **Decide**
   - Full release to inspector — evidence meets requirements, client dispute is unjustified
   - Full refund to client — evidence is genuinely inadequate, inspector didn't deliver
   - Partial split — both parties bear some responsibility. Common split: 60/40 or 70/30.
   - Re-task — assign a new inspector to re-do the task at platform's cost (extreme cases)
   - Revision — inspector gets one more chance to address specific feedback (escrow held)

5. **Communicate (within 72hrs)**
   - Notify both parties of decision with clear reasoning
   - Reasoning is 3–5 sentences, specific: "The evidence package included all required photos and checklist items. The GPS data confirms the inspector was at the correct location. While the photo quality could be improved, it meets platform minimum standards. We are releasing the escrow to the inspector."
   - If partial: explain the split reasoning

**Tier 2 SLAs:**
- Inspector rebuttal requested: within 24hrs of dispute
- Decision made: within 72hrs of dispute filing
- Both parties notified: immediately upon decision

### Tier 3: External Arbitration (< 7 days)

Either party can appeal a Tier 2 decision within 48hrs of notification.

**Process:**
1. Appeal filed with reason ("I disagree because...")
2. Independent reviewer assigned (contracted professional, not EyeOnSite team)
3. Reviewer gets: full evidence package, dispute notes, Tier 2 decision and reasoning, both parties' statements
4. Reviewer makes final, binding decision
5. Both parties notified

**Tier 3 eligibility:**
- Task value must be > $20 (not worth arbitration for small tasks)
- Appeals limited: each user gets 2 Tier 3 appeals per quarter
- Appeal fee: none (platform absorbs cost, but reserves right to add fee if abuse detected)

**Tier 3 implementation timeline:** V2+ (V0–V1: Maku makes final decisions)

---

## Dispute Outcomes and Escrow Actions

| Outcome | Escrow Action | Client Impact | Inspector Impact |
|---------|--------------|---------------|-----------------|
| **Full release** | 100% released to inspector | No refund. Can rate inspector. | Full payout. Trust score unaffected (dispute was invalid). |
| **Full refund** | 100% refunded to client | Full refund via original payment method. | $0 payout. Counts as negative event on trust score. |
| **Partial (e.g., 60/40)** | 60% refunded, 40% released | Partial refund. | Partial payout. Counts as partial negative on trust score. |
| **Revision** | Held (no movement) | Evidence will be resubmitted. New 48hr timer. | Must resubmit within 24hrs. No payout until accepted. |
| **Re-task** | Refunded to client, new escrow created at platform cost | New task auto-created. New inspector assigned. | Original inspector: $0, flagged for quality review. |

---

## Client Abuse Detection

Clients can game disputes to get free inspections. Detect and penalize:

| Signal | Detection | Action |
|--------|-----------|--------|
| High dispute rate | > 3 disputes in 30 days | Manual review of all disputes. Client trust score drops. |
| Pattern: always disputes, then accepts revision | > 50% of tasks have revision requests | Flag as potential quality gaming. Adjust auto-accept behavior. |
| Disputes with high-rated inspectors | Client disputes tasks from inspectors with trust_score > 0.85 | Strong signal of client abuse. Manual review. |
| Disputes after auto-accept period | Files fraud claims right before 7-day window closes | Suspicious timing. Require specific fraud evidence. |
| Abusive language toward inspector | Detected in dispute notes or messages | Warning → suspension. Zero tolerance for harassment. |

**Client penalties:**
- 1st abuse detection: Warning + manual review
- 2nd: 30-day restriction (can post tasks but disputes require Tier 2 approval)
- 3rd: Account suspension pending review
- 4th: Permanent ban

---

## Inspector Dispute Protections

Inspectors are the more vulnerable party (they've already invested time traveling and working). Protections:

1. **48-hour guarantee:** If client doesn't review within 48hrs, evidence auto-accepts. Inspector always gets paid eventually.
2. **Revision limit:** Maximum 2 revision cycles. After 2 revisions, platform mediates (Tier 2). Prevents infinite revision loops.
3. **Rebuttal right:** Inspector always gets 24hrs to respond to a dispute before any decision is made.
4. **Minimum payout:** Even in partial-refund disputes, inspector receives at minimum the cost of transport (estimated GHS 10–20).
5. **Dispute doesn't affect trust score until resolved:** A dispute filing alone doesn't hurt the inspector. Only a resolved-against-inspector outcome affects the score.
6. **Rate protection:** If an inspector is consistently rated unfairly by one client (> 3 tasks, all below 3 stars when platform average is 4.2), flag the client for review.

---

## Dispute Data Model

```kotlin
@Serializable
data class Dispute(
    val id: String,
    val taskId: String,
    val evidencePackageId: String,
    val initiatedBy: String,               // userId — usually client
    val category: DisputeCategory,
    val clientNotes: String,
    val inspectorRebuttal: String? = null,
    val inspectorRebuttalAt: Instant? = null,
    val tier: Int,                          // 1, 2, or 3
    val status: DisputeStatus,
    val resolution: DisputeResolution? = null,
    val resolutionNotes: String? = null,
    val resolvedBy: String? = null,         // admin userId or "auto" or "arbitrator"
    val clientRefundAmount: Money? = null,
    val inspectorPayoutAmount: Money? = null,
    val createdAt: Instant,
    val escalatedAt: Instant? = null,
    val resolvedAt: Instant? = null,
)

enum class DisputeResolution {
    FULL_RELEASE,           // inspector gets 100%
    FULL_REFUND,            // client gets 100%
    PARTIAL_SPLIT,          // negotiated split
    REVISION_REQUESTED,     // inspector resubmits
    RETASK,                 // new inspector assigned
    WITHDRAWN,              // client withdraws dispute
}
```

---

## Communication Templates

### Dispute Filed — Notification to Inspector
> Your evidence for Task #[id] has been disputed by the client. Reason: [category]. Client's notes: "[first 100 chars of clientNotes]". You have 24 hours to respond with your perspective. Your payout is held until this is resolved.

### Tier 1 Auto-Resolution — Revision Required
> We've reviewed the dispute on Task #[id]. [Specific reason, e.g., "Your evidence package had 4 photos but the task required 8."] Please resubmit within 24 hours with: [specific requirements]. Your payout will be released once the revised evidence is accepted.

### Tier 2 Decision — Full Release
> After reviewing both sides on Task #[id], we've decided to release the full payout to the inspector. [2-3 sentence reasoning.] If you disagree, you may appeal within 48 hours.

### Tier 2 Decision — Full Refund
> After reviewing both sides on Task #[id], we've decided to issue a full refund. [2-3 sentence reasoning.] The inspector's trust score has been adjusted. If the inspector disagrees, they may appeal within 48 hours.

---

*See [EOS-02-PRODUCT-DESIGN.md](./EOS-02-PRODUCT-DESIGN.md) for dispute flow overview.*
*See [EOS-08-ESCROW-AND-PAYMENTS.md](./EOS-08-ESCROW-AND-PAYMENTS.md) for escrow actions during disputes.*
