# 03 — Architecture

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Design Principles

1. **Kotlin everywhere** — one language across all surfaces (Android, iOS, Web, Backend where possible)
2. **Offline-first** — the app must work without connectivity; sync when back online
3. **Google-first infrastructure** — Firebase and GCP services exclusively; no AWS, no Azure
4. **Evidence pipeline is sacred** — the integrity of GPS, timestamp, and photo capture is the product
5. **Escrow is a ledger** — every money movement is double-entry, auditable, reconcilable

## Five-Plane Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     PLANE 1: CLIENT PLANE                       │
│                                                                 │
│  ┌──────────────┐  ┌────────────────┐  ┌─────────────────────┐  │
│  │   Android     │  │     iOS         │  │       Web            │  │
│  │   Jetpack     │  │  Compose        │  │  Compose for Web     │  │
│  │   Compose     │  │  Multiplatform  │  │  (Kotlin/JS)         │  │
│  │              │  │  (CMP)          │  │                     │  │
│  │  Platform:   │  │  Platform:      │  │  • Landing page     │  │
│  │  • CameraX   │  │  • AVFoundation │  │  • Admin dashboard  │  │
│  │  • Fused Loc │  │  • CoreLocation │  │  • Client portal    │  │
│  │  • Hilt DI   │  │                │  │                     │  │
│  └──────┬───────┘  └──────┬─────────┘  └──────────┬──────────┘  │
│         └─────────────┬───┘                       │             │
│                 ┌─────┴──────┐                    │             │
│                 │ KMP Shared  │────────────────────┘             │
│                 │ Module      │                                  │
│                 │ (commonMain)│ Ktor, SQLDelight, kotlinx.*     │
│                 └──────┬──────┘                                  │
│                        │                                        │
├────────────────────────┼────────────────────────────────────────┤
│                     PLANE 2: API PLANE                          │
│                        │                                        │
│  ┌─────────────────────┴───────────────────────────────────┐    │
│  │              Cloud Functions v2                           │    │
│  │                                                         │    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌────────────────┐  │    │
│  │  │ Task         │ │ Evidence     │ │ Notification   │  │    │
│  │  │ Matching     │ │ Validation   │ │ Dispatch       │  │    │
│  │  │              │ │              │ │                │  │    │
│  │  │ Geo-query    │ │ GPS verify   │ │ FCM push       │  │    │
│  │  │ Tier filter  │ │ EXIF check   │ │ Email (SES)    │  │    │
│  │  │ Score weight │ │ Vision AI    │ │ WhatsApp (V2)  │  │    │
│  │  └──────────────┘ └──────────────┘ └────────────────┘  │    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌────────────────┐  │    │
│  │  │ Escrow       │ │ Report       │ │ Inspector      │  │    │
│  │  │ Management   │ │ Generation   │ │ Management     │  │    │
│  │  │              │ │              │ │                │  │    │
│  │  │ Hold/Release │ │ Evidence →   │ │ Onboarding     │  │    │
│  │  │ Refund       │ │ PDF/HTML     │ │ Verification   │  │    │
│  │  │ Ledger       │ │ report       │ │ Tier mgmt      │  │    │
│  │  └──────────────┘ └──────────────┘ └────────────────┘  │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                     PLANE 3: DATA PLANE                         │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Cloud Firestore   │  │ Cloud Storage │  │ SQLDelight       │  │
│  │ (primary DB)      │  │ (media)       │  │ (offline cache)  │  │
│  │                  │  │              │  │                  │  │
│  │ Collections:     │  │ Buckets:     │  │ Tables:          │  │
│  │ • users          │  │ • evidence/  │  │ • task_cache     │  │
│  │ • inspectors     │  │   {taskId}/  │  │ • evidence_queue │  │
│  │ • tasks          │  │   photos/    │  │ • inspector_cache│  │
│  │ • evidence       │  │   videos/    │  │ • pending_sync   │  │
│  │ • escrow_txns    │  │ • reports/   │  │                  │  │
│  │ • ratings        │  │ • profiles/  │  │ Offline-first:   │  │
│  │ • disputes       │  │   id_docs/   │  │ Write locally →  │  │
│  │ • templates      │  │              │  │ Queue for sync → │  │
│  │ • notifications  │  │              │  │ Upload on connect│  │
│  └──────────────────┘  └──────────────┘  └──────────────────┘  │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                     PLANE 4: EVIDENCE PLANE                     │
│                                                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                  EVIDENCE PIPELINE                          │ │
│  │                                                            │ │
│  │  Photo captured   GPS + EXIF    Cloud Vision    Fraud      │ │
│  │  in-app        →  extracted  →  API scoring  →  scoring  → │ │
│  │  (CameraX /       + verified    (blur, quality,  (spoof,   │ │
│  │   AVFoundation)   against task  exposure)        duplicate, │ │
│  │                   location                      stock)     │ │
│  │                                                            │ │
│  │  → Evidence Package assembled → Quality Score computed     │ │
│  │  → Stored in Firestore + Cloud Storage                     │ │
│  │  → Client notified for review                              │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                     PLANE 5: PAYMENT PLANE                      │
│                                                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                                                            │ │
│  │  DIASPORA CLIENT                    INSPECTOR              │ │
│  │  (USD/GBP/EUR)                      (GHS/NGN/KES/UGX)     │ │
│  │       │                                    ▲               │ │
│  │       ▼                                    │               │ │
│  │  ┌──────────┐   ┌──────────────┐   ┌──────────────┐       │ │
│  │  │  Stripe   │──▶│   ESCROW     │──▶│ Flutterwave  │       │ │
│  │  │          │   │   LEDGER     │   │              │       │ │
│  │  │ Cards    │   │              │   │ MTN MoMo     │       │ │
│  │  │ Bank     │   │ Double-entry │   │ M-Pesa       │       │ │
│  │  │ transfer │   │ Firestore    │   │ Bank transfer│       │ │
│  │  └──────────┘   │ collection   │   └──────────────┘       │ │
│  │                 │              │                           │ │
│  │  LOCAL CLIENT   │ States:      │                           │ │
│  │  (GHS/NGN/KES) │ • HELD       │                           │ │
│  │       │        │ • RELEASED   │                           │ │
│  │       ▼        │ • REFUNDED   │                           │ │
│  │  ┌──────────┐  │ • DISPUTED   │                           │ │
│  │  │Flutterwave│─▶│ • PARTIAL   │                           │ │
│  │  │(fund)    │  └──────────────┘                           │ │
│  │  └──────────┘                                              │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## KMP Shared Module — Domain Objects

```kotlin
// shared/commonMain/domain/model/

data class User(
    val id: String,              // Firebase Auth UID
    val phone: String,
    val displayName: String,
    val role: UserRole,          // CLIENT, INSPECTOR, ADMIN
    val createdAt: Instant,
)

enum class UserRole { CLIENT, INSPECTOR, ADMIN }

data class Task(
    val id: String,              // ULID
    val clientId: String,
    val verticalType: VerticalType,
    val templateId: String,
    val location: GeoPoint,
    val locationRadius: Int,     // meters
    val address: String,
    val title: String,
    val instructions: String,
    val checklist: List<ChecklistItem>,
    val requiredPhotos: Int,
    val requiredPhotoAngles: List<PhotoAngle>,
    val videoRequired: Boolean,
    val deadlineAt: Instant,
    val budget: Money,
    val inspectorTierRequired: InspectorTier,
    val projectId: String?,      // nullable — standalone or part of a project
    val status: TaskStatus,
    val matchedInspectorId: String?,
    val escrowTransactionId: String?,
    val evidencePackageId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class Inspector(
    val id: String,              // Firebase Auth UID
    val userId: String,
    val tier: InspectorTier,
    val trustScore: Float,       // 0.0 - 1.0
    val completedTasks: Int,
    val activeTaskId: String?,
    val location: GeoPoint?,     // last known
    val verticalSpecializations: List<VerticalType>,
    val verificationStatus: VerificationStatus,
    val payoutMethod: PayoutMethod,
    val createdAt: Instant,
)

data class EvidencePackage(
    val id: String,
    val taskId: String,
    val inspectorId: String,
    val photos: List<EvidencePhoto>,
    val videos: List<EvidenceVideo>,
    val checklistResponses: List<ChecklistResponse>,
    val inspectorNotes: String,
    val captureStartedAt: Instant,
    val captureCompletedAt: Instant,
    val location: GeoPoint,
    val locationAccuracy: Float,
    val distanceFromTaskSite: Float,
    val deviceInfo: DeviceInfo,
    val fraudSignals: FraudSignals,
    val qualityScore: Float,
    val status: EvidenceStatus,
    val submittedAt: Instant,
)

data class EscrowTransaction(
    val id: String,
    val taskId: String,
    val clientId: String,
    val inspectorId: String?,
    val amount: Money,
    val platformFee: Money,
    val inspectorPayout: Money,
    val fundingMethod: String,   // "stripe" | "flutterwave"
    val payoutMethod: String,    // "momo" | "mpesa" | "bank"
    val status: EscrowStatus,    // HELD, RELEASED, REFUNDED, DISPUTED, PARTIAL
    val fundedAt: Instant?,
    val releasedAt: Instant?,
    val refundedAt: Instant?,
    val createdAt: Instant,
)
```

## Event Model (Firestore Triggers → Cloud Functions)

```
Firestore Write: tasks/{taskId}.status = ESCROW_FUNDED
    → Trigger: onTaskFunded()
    → Action: Geo-query inspectors within radius, send FCM pings

Firestore Write: tasks/{taskId}.status = INSPECTOR_MATCHED
    → Trigger: onInspectorMatched()
    → Action: Notify client, start SLA timer

Firestore Write: evidence/{evidenceId}.status = SUBMITTED
    → Trigger: onEvidenceSubmitted()
    → Action: Run validation pipeline (GPS, EXIF, Vision AI), compute quality score, notify client

Firestore Write: tasks/{taskId}.status = ACCEPTED
    → Trigger: onTaskAccepted()
    → Action: Release escrow, process payout, notify inspector, prompt ratings

Firestore Write: tasks/{taskId}.status = DISPUTED
    → Trigger: onTaskDisputed()
    → Action: Freeze escrow, create dispute record, notify both parties, start mediation SLA

Scheduled: Every hour
    → Trigger: checkAutoAcceptTimers()
    → Action: Auto-accept evidence packages where 48hr review window has expired

Scheduled: Daily
    → Trigger: processPayoutBatch()
    → Action: Batch inspector payouts via Flutterwave
```

## Offline-First Sync Engine

```
┌─────────────────────────────────────────────────────┐
│                 OFFLINE-FIRST FLOW                   │
│                                                     │
│  User Action (e.g., capture photo, complete checklist)
│       │                                             │
│       ▼                                             │
│  ┌──────────────┐                                   │
│  │ Write to      │  SQLDelight local DB              │
│  │ local cache   │  (always succeeds, even offline)  │
│  └──────┬───────┘                                   │
│         │                                           │
│         ▼                                           │
│  ┌──────────────┐                                   │
│  │ Add to        │  pending_sync queue               │
│  │ sync queue    │  (operation, data, timestamp)     │
│  └──────┬───────┘                                   │
│         │                                           │
│         ▼                                           │
│  ┌──────────────┐     ┌──────────────┐              │
│  │ Connectivity  │────▶│ Background   │              │
│  │ monitor       │     │ sync worker  │              │
│  │ (online?)     │     │              │              │
│  └──────────────┘     │ Process queue│              │
│                       │ in order     │              │
│                       │              │              │
│                       │ Upload photos│              │
│                       │ to Storage   │              │
│                       │              │              │
│                       │ Write to     │              │
│                       │ Firestore    │              │
│                       │              │              │
│                       │ Retry on     │              │
│                       │ failure      │              │
│                       └──────────────┘              │
│                                                     │
│  Conflict Resolution:                               │
│  • Server timestamp wins (last-write-wins)          │
│  • Evidence uploads are idempotent (same photo ID   │
│    = no duplicate)                                  │
│  • Task status transitions are validated server-side│
│    (can't go ACCEPTED → IN_PROGRESS)                │
└─────────────────────────────────────────────────────┘
```

## Trust Boundaries

| Boundary | What's Trusted | What's Not |
|----------|---------------|------------|
| Client app → Cloud Functions | Firebase Auth token (verified UID) | All user input (validated server-side) |
| Inspector app → Cloud Functions | Firebase Auth token + device attestation | GPS data (cross-verified), photos (pipeline validated) |
| Cloud Functions → Firestore | Service account (admin SDK) | — |
| Cloud Functions → Stripe/Flutterwave | API keys (Secret Manager) | Webhook signatures (verified) |
| Cloud Storage → Client app | Signed URLs (time-limited) | Direct bucket access (denied) |
| Firestore Security Rules | Role-based: clients read own tasks, inspectors read matched tasks only | Cross-user data access (denied) |

## Firestore Collection Structure

```
users/
  {userId}/
    role: "CLIENT" | "INSPECTOR" | "ADMIN"
    phone, displayName, createdAt, ...

inspectors/
  {inspectorId}/
    userId, tier, trustScore, completedTasks, location, ...
    verificationStatus: "PENDING" | "VERIFIED" | "SUSPENDED"

tasks/
  {taskId}/
    clientId, verticalType, templateId, location, status, ...
    matchedInspectorId, escrowTransactionId, evidencePackageId

evidence/
  {evidenceId}/
    taskId, inspectorId, photos[], checklistResponses[], ...
    fraudSignals{}, qualityScore, status

escrow_transactions/
  {txnId}/
    taskId, clientId, inspectorId, amount, status, ...
    fundingMethod, payoutMethod, ledgerEntries[]

ratings/
  {ratingId}/
    taskId, fromUserId, toUserId, score, review, createdAt

disputes/
  {disputeId}/
    taskId, initiatedBy, category, notes, status, ...
    mediationNotes, resolution, resolvedAt

templates/
  {templateId}/
    verticalType, name, checklist[], requiredPhotos, ...
    requiredPhotoAngles[], reportFormat
```

---

*See [EOS-04-TECHNICAL-STACK.md](./EOS-04-TECHNICAL-STACK.md) for specific library versions and dependency choices.*
*See [EOS-05-MANIFEST-SPECS.md](./EOS-05-MANIFEST-SPECS.md) for complete Kotlin data class definitions.*
*See [EOS-07-SECURITY-AND-COMPLIANCE.md](./EOS-07-SECURITY-AND-COMPLIANCE.md) for Firestore Security Rules and threat model.*
