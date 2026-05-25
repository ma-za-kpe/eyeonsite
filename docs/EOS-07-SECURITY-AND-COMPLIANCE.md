# 07 — Security and Compliance

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Three-Layer Trust Model

EyeOnSite's trust architecture operates across three layers. A failure in any layer breaks the product.

### Layer 1: Identity Trust

"Is this person who they claim to be?"

| Control | Implementation | When |
|---------|---------------|------|
| Phone OTP | Firebase Authentication | Registration |
| National ID upload | Cloud Storage + manual review | Onboarding Step 2 |
| Selfie liveness check | Cloud Vision face match + liveness detection | Onboarding Step 2 |
| Device fingerprinting | Hashed device ID stored per account | Every session |
| Multi-account detection | Same device fingerprint → block registration | Registration |
| Session management | Firebase Auth tokens, 1-hour expiry, auto-refresh | Every request |

### Layer 2: Evidence Trust

"Is this evidence authentic and captured at the stated location and time?"

| Control | Implementation | When |
|---------|---------------|------|
| In-app capture only | Camera module enforces — no gallery uploads | Photo/video capture |
| GPS multi-signal verification | GPS vs. network location vs. WiFi vs. cell tower | Every capture |
| GPS spoofing detection | Compare location sources; altitude check; mock location API flag | Every capture |
| EXIF consistency | Capture timestamp vs. submission timestamp (±5 min tolerance) | Evidence validation |
| Device motion attestation | Accelerometer data confirms physical movement to/at site | During capture window |
| Perceptual hash duplicate detection | Compare against all previous submissions + stock photo DB | Post-submission pipeline |
| Cloud Vision AI quality scoring | Blur detection, exposure, resolution, SafeSearch | Post-submission pipeline |
| Photo originality | Metadata confirms camera sensor matches declared device model | Post-submission pipeline |
| Geofence enforcement | Inspector must be within configurable radius (default 200m) of task site | Capture mode entry |

### Layer 3: Financial Trust

"Is money moving correctly and only when conditions are met?"

| Control | Implementation | When |
|---------|---------------|------|
| Escrow hold | Funds locked in platform account before inspector is matched | Task funding |
| Release conditions | Server-side validation: evidence ACCEPTED or AUTO_ACCEPTED status | Escrow release |
| Double-entry ledger | Every money movement has a debit and credit in Firestore | All transactions |
| Webhook signature verification | Stripe and Flutterwave webhook signatures verified | Payment events |
| Refund flow | Only via Cloud Functions (admin SDK) — no client-side refund capability | Disputes |
| Fraud hold | Auto-hold payout if fraud signals exceed threshold | Post-validation |
| Reconciliation | Daily automated reconciliation: Stripe/Flutterwave ledger vs. Firestore ledger | Scheduled function |

## Threat Model — Top 10 Attack Vectors

| # | Threat | Probability | Impact | Countermeasure |
|---|--------|-------------|--------|----------------|
| 1 | **Inspector-contractor collusion** — inspector and site contractor stage fake progress for photos | HIGH | CRITICAL | Randomized checklist items per visit, require different inspector for repeat tasks on same site (V2), client can request specific inspector exclusions |
| 2 | **GPS spoofing** — inspector submits photos from home, spoofs GPS to match task site | MEDIUM | CRITICAL | Multi-signal GPS verification (GPS vs. network vs. WiFi), device mock location API detection, accelerometer motion data, altitude cross-reference |
| 3 | **Photo fraud** — inspector submits stock photos, recycled photos, or AI-generated images | MEDIUM | CRITICAL | In-app capture enforcement, perceptual hashing against previous submissions, Cloud Vision duplicate/stock detection, EXIF device sensor matching |
| 4 | **Escrow manipulation** — attacker attempts to release escrow without valid evidence | LOW | CRITICAL | Server-side only release (Cloud Functions with admin SDK), multi-condition validation (evidence status + task status + fraud score), webhook signature verification |
| 5 | **Account takeover** — attacker gains access to client or inspector account | MEDIUM | HIGH | Firebase Auth phone OTP (SIM-swap risk exists — mitigated by device fingerprinting), session management, unusual activity detection (login from new device → re-verify) |
| 6 | **Data exfiltration** — unauthorized access to evidence photos, personal data, or financial records | LOW | HIGH | Firestore Security Rules (row-level access), Cloud Storage signed URLs (time-limited), no direct bucket access, TLS in transit, Google-managed encryption at rest |
| 7 | **Client abuse** — client repeatedly disputes valid evidence to avoid payment | MEDIUM | MEDIUM | Client Trust Score tracks dispute rate, pattern detection (3+ disputes in 30 days → manual review), inspector can appeal disputes, platform reserves right to ban abusive clients |
| 8 | **Inspector safety incident** — inspector harassed, threatened, or injured at task site | MEDIUM | HIGH | Check-in/check-out system, emergency contact on file, no-go zone list, training on when to leave, accident insurance (V2+), incident response within 1 hour |
| 9 | **API abuse** — malicious actor hammers Cloud Functions endpoints | LOW | MEDIUM | Firebase App Check (device attestation), rate limiting on Cloud Functions, API key restrictions, CORS configuration |
| 10 | **Insider threat** — platform team member accesses data inappropriately | LOW | HIGH | IAM least-privilege (separate service accounts per function group), audit logging (Cloud Audit Logs), no production Firestore access from developer machines (staging environment only) |

## Evidence Forensics Pipeline

```
Photo captured in-app
    │
    ├── Step 1: Device-level checks (on-device, before upload)
    │   ├── Mock location API enabled? → Block capture, show warning
    │   ├── GPS accuracy > 50m? → Warning: "Move to open area for better GPS"
    │   ├── Photo captured via EyeOnSite camera module? → MUST be true
    │   └── Store: GPS, network location, WiFi location, cell tower, accelerometer snapshot
    │
    ├── Step 2: Upload + server-side validation (Cloud Function trigger)
    │   ├── EXIF extraction: timestamp, camera model, resolution, GPS in EXIF
    │   ├── EXIF timestamp vs. submission timestamp: within ±5 min?
    │   ├── EXIF camera model matches declared device model?
    │   ├── GPS in photo EXIF matches reported GPS coordinates (±50m)?
    │   └── File size and resolution consistent with declared device camera?
    │
    ├── Step 3: AI analysis (Cloud Vision API)
    │   ├── Blur detection: sharpness score 0.0–1.0
    │   ├── Exposure: over/under-exposed detection
    │   ├── SafeSearch: block inappropriate content
    │   ├── Label detection: does content match task vertical? (construction → building materials, scaffolding)
    │   └── Web detection: is this image found elsewhere on the internet? (stock photo check)
    │
    ├── Step 4: Duplicate detection (custom pipeline)
    │   ├── Generate perceptual hash (pHash/dHash) of photo
    │   ├── Compare against all previous hashes from this inspector
    │   ├── Compare against all previous hashes from this task site
    │   ├── Hamming distance < threshold → FLAG as potential duplicate
    │   └── Store hash in Firestore for future comparisons
    │
    └── Step 5: Fraud score computation
        ├── Aggregate all signals into FraudSignals object
        ├── Compute overallRiskScore (0.0 = safe, 1.0 = certain fraud)
        ├── Risk > 0.7 → Auto-reject, freeze payout, notify platform team
        ├── Risk 0.4–0.7 → Hold for manual review
        └── Risk < 0.4 → Pass (normal flow)
```

## Per-Country Regulatory Map

### Data Protection

| Country | Law | Key Requirements | EyeOnSite Implications |
|---------|-----|-----------------|----------------------|
| **Ghana** | Data Protection Act 2012 (Act 843) | Registration with Data Protection Commission, consent for processing, data minimization, cross-border transfer restrictions | Register with DPC before launch. Consent screen during onboarding. Inspector photos processed in-region (GCP europe-west1 or nearest). |
| **Nigeria** | Nigeria Data Protection Act 2023 (NDPA) | NDPC registration, lawful basis, DPO appointment for >200 data subjects, data breach notification within 72hrs | Appoint DPO (can be Maku initially). Register with NDPC before Lagos launch. Breach notification procedure documented. |
| **Kenya** | Data Protection Act 2019 | ODPC registration, consent-based processing, data localization considerations, DPO for large processors | Register with ODPC. Consent flows. Consider Kenya-resident data storage for Kenyan user data. |
| **Uganda** | Data Protection and Privacy Act 2019 (DPPA) | Registration with NITA, consent, restrictions on cross-border transfer | Register with NITA before Kampala launch. |
| **UK** | UK GDPR + DPA 2018 | Lawful basis, DPIA, international transfers (adequacy or SCCs), subject access rights | UK clients trigger UK GDPR. Standard Contractual Clauses for UK→Ghana data transfer. Privacy policy must be UK GDPR compliant. |
| **EU** | GDPR | Same as UK GDPR essentially | EU clients (less common initially) — same SCCs approach. |

### Payment and Escrow Licensing

| Country | Requirement | EyeOnSite Approach |
|---------|------------|-------------------|
| **Ghana** | Bank of Ghana e-money issuer license for holding funds | V0–V1: Stripe holds escrow (Manti Labs LLC is the merchant). No GHS-denominated escrow holding by EyeOnSite directly. Flutterwave handles MoMo payouts as a licensed entity. V3+: Evaluate whether escrow volume requires own license. |
| **Nigeria** | CBN Payment Service Provider (PSP) license for payment processing | Same approach: Stripe/Flutterwave are licensed PSPs. EyeOnSite is the marketplace, not the payment processor. |
| **Kenya** | CBK oversight for payment services | Same approach. M-Pesa payouts via Flutterwave (licensed). |

### Gig Worker Classification

| Country | Risk | Mitigation |
|---------|------|-----------|
| **Ghana** | Low — gig economy classification not yet heavily regulated | Inspector agreement clearly states independent contractor relationship. No exclusivity. Inspector controls schedule, tools, methods. |
| **Nigeria** | Medium — labour law reforms in discussion | Same independent contractor approach. Monitor regulatory developments. |
| **Kenya** | Medium — Employment Act 2007 has broad definitions | Clear IC agreement. Inspector uses own device, sets own hours, can reject tasks, works for multiple platforms. Document that EyeOnSite does not: set hours, provide tools, require exclusivity, provide benefits. |

## Legal Document Templates Required (Pre-V1)

1. **Terms of Service** — Platform terms for all users (clients + inspectors)
2. **Privacy Policy** — UK GDPR and Ghana DPA compliant
3. **Inspector Agreement** — Independent contractor terms, code of conduct, fraud consequences
4. **Client Agreement** — Task posting terms, escrow terms, dispute resolution process
5. **Cookie Policy** — For web properties (Compose for Web landing page + dashboard)

**When to get a lawyer vs. DIY:**
- DIY: Initial drafts using proven templates (Stripe Atlas provides TOS/privacy templates). Sufficient for V0–V1.
- Lawyer: Before V2 multi-country expansion. Ghana-based lawyer for DPA compliance. UK solicitor for UK GDPR compliance. Nigeria lawyer for NDPA.
- Budget: $2,000–$5,000 for initial legal review across Ghana + Nigeria.

## Firebase Security Rules (Complete)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper: is the request from an authenticated user?
    function isAuthenticated() {
      return request.auth != null;
    }

    // Helper: is this the document owner?
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    // Helper: is this user an admin?
    function isAdmin() {
      return isAuthenticated() &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN';
    }

    // Users
    match /users/{userId} {
      allow read: if isOwner(userId) || isAdmin();
      allow create: if isOwner(userId);
      allow update: if isOwner(userId);
      allow delete: if false; // soft delete only
    }

    // Inspector profiles
    match /inspectors/{inspectorId} {
      allow read: if isAuthenticated(); // clients need to see matched inspector
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }

    // Tasks
    match /tasks/{taskId} {
      allow create: if isAuthenticated() && request.resource.data.clientId == request.auth.uid;
      allow read: if isAuthenticated() && (
        resource.data.clientId == request.auth.uid ||
        resource.data.matchedInspectorId == request.auth.uid ||
        isAdmin()
      );
      allow update: if isAdmin(); // all status transitions via Cloud Functions
    }

    // Evidence
    match /evidence/{evidenceId} {
      allow create: if isAuthenticated();
      allow read: if isAuthenticated() && (
        get(/databases/$(database)/documents/tasks/$(resource.data.taskId)).data.clientId == request.auth.uid ||
        resource.data.inspectorId == request.auth.uid ||
        isAdmin()
      );
      allow update: if isAdmin();
    }

    // Escrow - read only for involved parties; all writes via admin SDK
    match /escrow_transactions/{txnId} {
      allow read: if isAuthenticated() && (
        resource.data.clientId == request.auth.uid ||
        resource.data.inspectorId == request.auth.uid ||
        isAdmin()
      );
      allow write: if false; // Cloud Functions only
    }

    // Ratings
    match /ratings/{ratingId} {
      allow create: if isAuthenticated() && request.resource.data.fromUserId == request.auth.uid;
      allow read: if isAuthenticated();
      allow update: if false; // ratings are immutable
    }

    // Disputes
    match /disputes/{disputeId} {
      allow create: if isAuthenticated();
      allow read: if isAuthenticated() && (
        get(/databases/$(database)/documents/tasks/$(resource.data.taskId)).data.clientId == request.auth.uid ||
        get(/databases/$(database)/documents/tasks/$(resource.data.taskId)).data.matchedInspectorId == request.auth.uid ||
        isAdmin()
      );
      allow update: if isAdmin();
    }

    // Templates - public read
    match /templates/{templateId} {
      allow read: if true;
      allow write: if isAdmin();
    }
  }
}
```

## Cloud Storage Security Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {

    // Evidence photos/videos — inspector can upload, involved parties can read
    match /evidence/{taskId}/{allPaths=**} {
      allow write: if request.auth != null;
      allow read: if request.auth != null;
      // Fine-grained: Cloud Functions generate signed URLs for client access
    }

    // Profile documents (ID uploads) — owner only
    match /profiles/{userId}/{allPaths=**} {
      allow write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null && request.auth.uid == userId;
      // Admin access via admin SDK (bypasses rules)
    }

    // Reports (generated PDFs) — involved parties
    match /reports/{taskId}/{allPaths=**} {
      allow read: if request.auth != null;
      // Generated by Cloud Functions (admin SDK write)
    }
  }
}
```

## Incident Response Playbook

| Severity | Example | Response Time | Actions |
|----------|---------|---------------|---------|
| **P0 — Critical** | Data breach, escrow theft, inspector serious injury | < 1 hour | Maku alerted immediately. Affected systems isolated. Users notified within 24hrs if data compromised. Relevant DPA authority notified within 72hrs. |
| **P1 — High** | GPS spoofing ring detected, payment processing outage, widespread fraud pattern | < 4 hours | Affected accounts frozen. Manual review of all flagged evidence. Payment partner contacted. |
| **P2 — Medium** | Single inspector fraud, single client abuse, minor service degradation | < 24 hours | Account suspended. Evidence reviewed. Resolution per standard dispute process. |
| **P3 — Low** | Bug report, minor UI issue, feature request | < 72 hours | Logged in GitHub Issues. Prioritized in next sprint. |

---

*See [EOS-06-INSPECTOR-SPEC.md](./EOS-06-INSPECTOR-SPEC.md) for fraud detection pipeline details.*
*See [EOS-08-ESCROW-AND-PAYMENTS.md](./EOS-08-ESCROW-AND-PAYMENTS.md) for financial trust implementation.*
