# 06 — Inspector Specification

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## The Inspector Is the Product

EyeOnSite's quality is 100% determined by inspector quality. A brilliant app with bad inspectors is worthless. A basic app with excellent inspectors is a business.

## Onboarding Flow

```
Step 1: Registration (2 min)
  → Download app → Phone OTP (Firebase Auth) → Basic profile (name, city, education)

Step 2: ID Verification (1 day)
  → Upload national ID / passport photo → Selfie match (liveness check)
  → Automated: face match score via Cloud Vision API
  → Manual review for edge cases

Step 3: Background Check (2-3 days)
  → Partner with local verification service per country
  → Ghana: Criminal records check via Ghana Police Service
  → Nigeria: Police character certificate
  → Kenya: Certificate of Good Conduct
  → Cost: Platform covers for first 100 inspectors; inspector pays ($3-7) after scale

Step 4: Training Module (30 min)
  → In-app video course per vertical (construction = 15 min, general platform = 15 min)
  → Quiz at end (must score 80%+ to pass)
  → Topics: how to use capture mode, GPS requirements, photo quality standards,
    checklist completion, what makes a good inspection, common mistakes,
    safety protocols, code of conduct

Step 5: Test Task (1-2 hours)
  → Platform assigns a simulated task at a real location near inspector
  → Inspector completes full task flow (travel, capture, submit)
  → Platform team reviews evidence quality manually
  → Pass criteria: all required photos captured, GPS within radius,
    checklist complete, photo quality acceptable
  → Fail: specific feedback given, can retry once

Step 6: Activation
  → Status: VERIFIED → Inspector begins receiving real task pings
  → First 5 tasks: quality reviewed manually by platform team
  → After 5 tasks with no issues: enters normal automated pipeline
```

## Tier System

| Tier | Requirements | Unlocks | Payout Range |
|------|-------------|---------|-------------|
| **Basic** | Verified + training + test task passed | Photo-only tasks, simple checklists, all verticals where trained | $10–$30/task |
| **Professional** | Basic + trust_score >= 0.75 + 50 completed tasks + (engineering/surveying background OR platform certification course) | Construction progress assessment, property evaluation, agricultural monitoring, video tasks | $30–$80/task |
| **Expert** | Professional + trust_score >= 0.90 + 200 completed tasks + professional license (surveyor, engineer, architect) | Insurance claims assessment, legal property verification, structural assessments, live streaming tasks, enterprise client tasks | $80–$200/task |

### Tier Progression

```
BASIC ──────────────────────────────────▶ PROFESSIONAL
  Criteria:
  • 50+ completed tasks
  • Trust score >= 0.75
  • Average rating >= 4.2
  • Dispute rate < 8%
  • One of:
    - Engineering/surveying degree or diploma
    - Completed EyeOnSite Professional Certification (in-app, $20)
  Review: Automated check + manual review of 5 recent evidence packages

PROFESSIONAL ───────────────────────────▶ EXPERT
  Criteria:
  • 200+ completed tasks
  • Trust score >= 0.90
  • Average rating >= 4.5
  • Dispute rate < 5%
  • Professional license uploaded and verified
    (licensed surveyor, registered engineer, architect)
  Review: Manual review by platform team + license verification
```

## Payout Model

### Per-Task Payout

```
Inspector Payout = Task Budget × (1 - Platform Take Rate)

Example (consumer task):
  Client pays: GHS 60 ($4.60)
  Platform take: 18% = GHS 10.80
  Inspector receives: GHS 49.20

Example (enterprise task):
  Client pays: GHS 120 ($9.20)
  Platform take: 15% = GHS 18.00
  Inspector receives: GHS 102.00
```

### Bonus Structure

| Bonus | Amount | Criteria |
|-------|--------|----------|
| **Quality Bonus** | +10% of base payout | Evidence quality score >= 0.90 |
| **Speed Bonus** | +5% of base payout | Task completed within 2 hours of acceptance |
| **Streak Bonus** | +15% on 10th task | Complete 10 tasks in a row with no disputes |
| **Referral Bonus** | GHS 20 ($1.50) | Referred inspector completes their first 5 tasks |
| **Weekend/Holiday** | +20% of base payout | Tasks completed on weekends or public holidays |

### Payout Schedule

- **Instant payouts** (V1): After each task acceptance, payout initiated immediately via Flutterwave
- **Daily batch** (V2 alternative): All accepted tasks from previous day batched at 6:00 AM local time
- **Payout methods**: MTN MoMo (Ghana), M-Pesa (Kenya), bank transfer (Nigeria, Uganda)
- **Minimum payout**: None — even single-task payouts are processed
- **Payout processing time**: MoMo/M-Pesa: instant to 30 min. Bank transfer: 1-3 business days.

## Fraud Detection Pipeline

### Signal Categories

**Location Fraud (GPS Spoofing):**
- Compare GPS coordinates with network-based location (cell tower triangulation)
- Compare with WiFi-based location (if available)
- Check device motion data (accelerometer) — was the device physically moving?
- Check altitude consistency (GPS altitude vs. expected terrain elevation)
- Flag: GPS and network locations differ by > 500m

**Photo Fraud:**
- In-app capture attestation: photos must be taken through EyeOnSite camera module
- EXIF timestamp must be within 5 minutes of submission timestamp
- Perceptual hashing: compare against database of previously submitted photos
- Perceptual hashing: compare against stock photo database (Cloud Vision API)
- AI duplicate detection: same scene from slightly different angles = okay; exact same image = fraud
- Resolution/metadata check: consistent with device camera capabilities

**Behavioral Fraud:**
- Velocity check: inspector can't complete tasks 50km apart within 30 minutes
- Time on site: if inspector submits evidence within 5 minutes of arriving at GPS radius, flag for review
- Pattern detection: same inspector + same contractor/property manager repeatedly = collusion risk
- Quality degradation: declining photo quality or checklist depth over time = fatigue or gaming

**Account Fraud:**
- Multiple accounts from same device fingerprint
- Phone number previously associated with deactivated account
- Rapid account creation pattern from same IP range

### Automated Actions

| Signal | Risk Level | Action |
|--------|-----------|--------|
| GPS spoofing detected (high confidence) | CRITICAL | Auto-reject evidence, freeze payout, flag for manual review |
| Stock photo detected | CRITICAL | Auto-reject evidence, suspend inspector pending review |
| Duplicate photo (exact match) | HIGH | Auto-reject evidence, warning to inspector |
| Velocity check failed | HIGH | Flag for manual review, hold payout |
| Time on site < 10 minutes | MEDIUM | Flag for quality review, no automated action |
| Quality score declining trend (5+ tasks) | MEDIUM | Notification to inspector with improvement suggestions |
| Device fingerprint matches deactivated account | CRITICAL | Block registration, alert platform team |

## Deactivation Criteria

**Immediate deactivation (pending review):**
- Confirmed GPS spoofing (evidence fabrication)
- Confirmed photo fraud (stock photos, reused photos)
- Safety incident (harassment, trespassing, threatening behavior)
- Identity fraud (using someone else's credentials)

**Progressive discipline:**
- 1st dispute: Warning + quality review feedback
- 2nd dispute (within 30 days): 48-hour task restriction + mandatory retraining module
- 3rd dispute (within 60 days): 7-day suspension + manual review of all recent tasks
- 4th dispute (within 90 days): Permanent deactivation

**Inactivity:**
- No tasks accepted in 90 days: Account moved to dormant (can reactivate)
- No tasks accepted in 180 days: Account deactivated (must re-onboard)

## Safety Protocols

1. **Emergency contact**: Every inspector must provide an emergency contact during onboarding
2. **Check-in system**: Inspector marks "arrived at site" and "leaving site" — if no "leaving" signal within expected time + 2 hours, automated check-in via SMS
3. **No-go zones**: Platform maintains a list of areas flagged as unsafe (community-reported, security alerts). Tasks in no-go zones require additional safety acknowledgment.
4. **Incident reporting**: In-app button to report safety concerns. Platform team responds within 1 hour during business hours.
5. **Insurance**: Platform provides basic accident/injury coverage for inspectors during active tasks (V2+, via partnership with local insurer)
6. **Never enter without permission**: Training emphasizes that inspectors photograph exteriors unless explicitly authorized for interior access. Interior tasks require client to arrange access.

## Inspector App UX Requirements

- **Task discovery**: Map view centered on inspector's location. Available tasks shown as pins with distance, budget, vertical type, deadline. Filter by vertical, distance, budget.
- **One active task at a time** (V1): Inspector can only accept one task. Must complete or cancel before accepting another.
- **Capture mode**: Full-screen camera with overlay showing required angles, checklist items, GPS status indicator (green/yellow/red), photo counter.
- **Offline capture**: All photos stored locally first. Upload queued. Progress bar shows upload status when back online.
- **Earnings dashboard**: Today's earnings, this week, this month. Pending payouts. Completed tasks with payout status.

---

*See [EOS-02-PRODUCT-DESIGN.md](./EOS-02-PRODUCT-DESIGN.md) for inspector persona details.*
*See [EOS-07-SECURITY-AND-COMPLIANCE.md](./EOS-07-SECURITY-AND-COMPLIANCE.md) for full threat model.*
