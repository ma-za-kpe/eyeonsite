# 05 — Manifest Specifications

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Overview

This document defines every data schema used across EyeOnSite. All schemas are expressed as Kotlin data classes (living in `shared/commonMain/domain/model/`) and map directly to Firestore documents.

## Enumerations

```kotlin
enum class VerticalType {
    CONSTRUCTION, PROPERTY, AGRICULTURE, FINTECH, INSURANCE, NGO, SUPPLY_CHAIN, SOLAR
}

enum class TaskStatus {
    DRAFT, POSTED, ESCROW_FUNDED, INSPECTOR_MATCHED,
    IN_PROGRESS, EVIDENCE_SUBMITTED, UNDER_REVIEW,
    ACCEPTED, DISPUTED, REVISION_REQUESTED,
    CANCELLED, EXPIRED, AUTO_ACCEPTED
}

enum class InspectorTier { BASIC, PROFESSIONAL, EXPERT }

enum class VerificationStatus { PENDING, ID_SUBMITTED, BACKGROUND_CHECK, TRAINING, TEST_TASK, VERIFIED, SUSPENDED, DEACTIVATED }

enum class EscrowStatus { PENDING, HELD, RELEASED, REFUNDED, DISPUTED, PARTIAL_REFUND }

enum class EvidenceStatus { CAPTURING, SUBMITTED, VALIDATED, ACCEPTED, REJECTED, REVISION_REQUESTED }

enum class PhotoAngle { FRONT, BACK, LEFT, RIGHT, INTERIOR, AERIAL, DETAIL, SIGNAGE, BOUNDARY, DOCUMENT }

enum class DisputeCategory { EVIDENCE_QUALITY, TASK_NOT_COMPLETED, INSPECTOR_NO_SHOW, WRONG_SITE, CLIENT_ABUSE, OTHER }

enum class DisputeStatus { OPEN, TIER1_AUTO, TIER2_MEDIATION, TIER3_ARBITRATION, RESOLVED, CLOSED }
```

## Task Manifest

```kotlin
@Serializable
data class TaskManifest(
    val id: String,                          // ULID
    val clientId: String,                    // Firebase Auth UID
    val verticalType: VerticalType,
    val templateId: String,
    val location: GeoPoint,
    val locationRadius: Int = 200,           // meters — inspector must be within this
    val address: String,
    val city: String,                        // For analytics and matching
    val country: String,                     // GH, NG, KE, UG
    val title: String,                       // "Monthly progress check — Tema house"
    val instructions: String,                // Client's specific instructions
    val checklist: List<ChecklistItem>,
    val requiredPhotos: Int = 5,
    val requiredPhotoAngles: List<PhotoAngle>,
    val videoRequired: Boolean = false,
    val videoDurationSeconds: Int = 120,     // max video duration
    val liveStreamRequired: Boolean = false, // V3+
    val deadlineAt: Instant,
    val budget: Money,
    val inspectorTierRequired: InspectorTier = InspectorTier.BASIC,
    val projectId: String? = null,
    val isUrgent: Boolean = false,           // same-day = 1.5x pricing
    val status: TaskStatus = TaskStatus.DRAFT,
    val matchedInspectorId: String? = null,
    val escrowTransactionId: String? = null,
    val evidencePackageId: String? = null,
    val autoAcceptAt: Instant? = null,       // 48hrs after evidence submission
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,                        // "Are foundation footings visible?"
    val type: ChecklistItemType,             // YES_NO, TEXT, NUMBER, PHOTO
    val required: Boolean = true,
    val photoRequired: Boolean = false,      // Must attach a photo to this item
    val order: Int,
)

enum class ChecklistItemType { YES_NO, TEXT, NUMBER, PHOTO, MULTI_SELECT }

@Serializable
data class Money(
    val amount: Long,                        // Amount in smallest unit (pesewas, kobo, cents)
    val currency: String,                    // "GHS", "NGN", "KES", "UGX", "USD", "GBP", "EUR"
)

@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)
```

## Evidence Package

```kotlin
@Serializable
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
    val locationAccuracy: Float,             // meters
    val distanceFromTaskSite: Float,         // meters
    val deviceInfo: DeviceInfo,
    val fraudSignals: FraudSignals,
    val qualityScore: Float,                 // 0.0 - 1.0
    val status: EvidenceStatus,
    val submittedAt: Instant,
    val reviewedAt: Instant? = null,
    val reviewedBy: String? = null,          // clientId or "auto"
)

@Serializable
data class EvidencePhoto(
    val id: String,
    val storageUrl: String,                  // Cloud Storage signed URL
    val thumbnailUrl: String,
    val capturedAt: Instant,
    val gpsLocation: GeoPoint,
    val gpsAccuracy: Float,                  // meters
    val angle: PhotoAngle,
    val checklistItemId: String? = null,     // if attached to a checklist item
    val exifData: Map<String, String>,
    val capturedInApp: Boolean,              // MUST be true
    val widthPx: Int,
    val heightPx: Int,
    val fileSizeBytes: Long,
    val aiQualityScore: Float,              // Cloud Vision: blur, exposure
    val aiDuplicateScore: Float,            // perceptual hash similarity
)

@Serializable
data class EvidenceVideo(
    val id: String,
    val storageUrl: String,
    val thumbnailUrl: String,
    val capturedAt: Instant,
    val durationSeconds: Int,
    val gpsLocation: GeoPoint,
    val capturedInApp: Boolean,
    val fileSizeBytes: Long,
)

@Serializable
data class ChecklistResponse(
    val checklistItemId: String,
    val responseType: ChecklistItemType,
    val booleanValue: Boolean? = null,
    val textValue: String? = null,
    val numberValue: Double? = null,
    val selectedValues: List<String>? = null,
    val attachedPhotoIds: List<String> = emptyList(),
    val notes: String? = null,
)

@Serializable
data class DeviceInfo(
    val model: String,                       // "Samsung Galaxy A54"
    val os: String,                          // "Android 14" | "iOS 17.4"
    val appVersion: String,                  // "1.0.0"
    val deviceId: String,                    // hashed device fingerprint
)

@Serializable
data class FraudSignals(
    val gpsConsistency: Boolean,             // GPS matches task location
    val gpsNetworkMatch: Boolean,            // GPS consistent with network location
    val exifTimestampConsistency: Boolean,   // EXIF time matches submission time
    val deviceMotionDetected: Boolean,       // accelerometer shows movement during capture
    val capturedViaApp: Boolean,             // all photos from EyeOnSite camera module
    val aiStockPhotoScore: Float,            // 0.0-1.0, likelihood of stock/reused
    val aiDuplicateDetected: Boolean,        // perceptual hash match with previous submissions
    val velocityCheck: Boolean,              // no impossible travel between tasks
    val overallRiskScore: Float,             // 0.0 (safe) - 1.0 (likely fraud)
)
```

## Inspector Profile

```kotlin
@Serializable
data class InspectorProfile(
    val id: String,
    val userId: String,
    val displayName: String,
    val phone: String,
    val tier: InspectorTier,
    val trustScore: Float,
    val completedTasks: Int,
    val activeTaskId: String? = null,
    val location: GeoPoint? = null,
    val locationUpdatedAt: Instant? = null,
    val city: String,
    val country: String,
    val verticalSpecializations: List<VerticalType>,
    val verificationStatus: VerificationStatus,
    val idDocumentUrl: String? = null,
    val backgroundCheckStatus: String? = null,
    val trainingCompletedVerticals: List<VerticalType> = emptyList(),
    val payoutMethod: PayoutMethod,
    val totalEarnings: Money,
    val averageRating: Float,
    val totalRatings: Int,
    val joinedAt: Instant,
    val lastActiveAt: Instant,
)

@Serializable
data class PayoutMethod(
    val type: String,                        // "momo", "mpesa", "bank"
    val provider: String,                    // "MTN", "Safaricom", "GTBank"
    val accountNumber: String,               // phone number or account number
    val accountName: String,
    val currency: String,
)
```

## Escrow Transaction

```kotlin
@Serializable
data class EscrowTransaction(
    val id: String,
    val taskId: String,
    val clientId: String,
    val inspectorId: String? = null,
    val grossAmount: Money,                  // Total client payment
    val platformFee: Money,                  // Commission (15-20%)
    val escrowFee: Money,                    // Processing fee (2-3%)
    val inspectorPayout: Money,              // What inspector receives
    val fundingMethod: String,               // "stripe" | "flutterwave"
    val fundingReference: String,            // Stripe PaymentIntent ID or Flutterwave tx_ref
    val payoutMethod: String,                // "momo" | "mpesa" | "bank"
    val payoutReference: String? = null,     // Flutterwave transfer ID
    val status: EscrowStatus,
    val fundedAt: Instant? = null,
    val releasedAt: Instant? = null,
    val refundedAt: Instant? = null,
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val createdAt: Instant,
)

@Serializable
data class LedgerEntry(
    val id: String,
    val type: String,                        // "FUND", "HOLD", "RELEASE", "REFUND", "FEE"
    val amount: Money,
    val description: String,
    val createdAt: Instant,
)
```

## Vertical Templates

### Construction Template (V1 Launch)

```kotlin
val constructionTemplate = VerticalTemplate(
    id = "tmpl_construction_progress_v1",
    verticalType = VerticalType.CONSTRUCTION,
    name = "Construction Progress Check",
    description = "Monthly or bi-weekly progress verification for building projects",
    requiredPhotos = 8,
    requiredPhotoAngles = listOf(
        PhotoAngle.FRONT, PhotoAngle.BACK, PhotoAngle.LEFT, PhotoAngle.RIGHT,
        PhotoAngle.INTERIOR, PhotoAngle.DETAIL, PhotoAngle.DETAIL, PhotoAngle.AERIAL
    ),
    videoRequired = false,
    checklist = listOf(
        ChecklistItem("c1", "What construction stage is the project at?", MULTI_SELECT, required = true, order = 1),
        // Options: Foundation, Walls/Blockwork, Roofing, Plastering/Finishing, Painting, Complete
        ChecklistItem("c2", "Are workers present on site?", YES_NO, required = true, order = 2),
        ChecklistItem("c3", "Estimated number of workers on site", NUMBER, required = false, order = 3),
        ChecklistItem("c4", "Are building materials visible on site?", YES_NO, required = true, photoRequired = true, order = 4),
        ChecklistItem("c5", "Describe materials observed", TEXT, required = true, order = 5),
        ChecklistItem("c6", "Is the construction quality acceptable? (straight walls, level surfaces, proper mortar)", YES_NO, required = true, order = 6),
        ChecklistItem("c7", "Any visible defects or concerns?", TEXT, required = false, order = 7),
        ChecklistItem("c8", "Is the site secured? (fencing, gate, security)", YES_NO, required = true, order = 8),
        ChecklistItem("c9", "Estimated percentage complete", NUMBER, required = true, order = 9),
        ChecklistItem("c10", "Additional observations", TEXT, required = false, order = 10),
    ),
    inspectorTierRequired = InspectorTier.BASIC,
    estimatedDurationMinutes = 45,
    suggestedBudget = Money(5000, "GHS"), // GHS 50.00
)
```

### Property Verification Template

```kotlin
val propertyTemplate = VerticalTemplate(
    id = "tmpl_property_verification_v1",
    verticalType = VerticalType.PROPERTY,
    name = "Property Condition Verification",
    requiredPhotos = 10,
    requiredPhotoAngles = listOf(
        PhotoAngle.FRONT, PhotoAngle.BACK, PhotoAngle.LEFT, PhotoAngle.RIGHT,
        PhotoAngle.INTERIOR, PhotoAngle.INTERIOR, PhotoAngle.INTERIOR, PhotoAngle.INTERIOR,
        PhotoAngle.BOUNDARY, PhotoAngle.SIGNAGE
    ),
    checklist = listOf(
        ChecklistItem("p1", "Is the property occupied?", YES_NO, order = 1),
        ChecklistItem("p2", "Property type (house, apartment, land, commercial)", MULTI_SELECT, order = 2),
        ChecklistItem("p3", "Overall exterior condition (1-5)", NUMBER, order = 3),
        ChecklistItem("p4", "Overall interior condition (1-5)", NUMBER, order = 4),
        ChecklistItem("p5", "Are boundary markers/fencing visible?", YES_NO, photoRequired = true, order = 5),
        ChecklistItem("p6", "Any visible damage or maintenance issues?", TEXT, order = 6),
        ChecklistItem("p7", "Are neighboring properties encroaching?", YES_NO, order = 7),
        ChecklistItem("p8", "Is the address consistent with provided details?", YES_NO, order = 8),
        ChecklistItem("p9", "Is signage visible (title board, for-sale, etc)?", YES_NO, photoRequired = true, order = 9),
        ChecklistItem("p10", "Additional observations", TEXT, required = false, order = 10),
    ),
)
```

### Agriculture Template

```kotlin
val agricultureTemplate = VerticalTemplate(
    id = "tmpl_farm_monitoring_v1",
    verticalType = VerticalType.AGRICULTURE,
    name = "Farm/Crop Monitoring",
    requiredPhotos = 6,
    checklist = listOf(
        ChecklistItem("a1", "Crop type observed", TEXT, order = 1),
        ChecklistItem("a2", "Growth stage (seedling, vegetative, flowering, fruiting, harvest-ready)", MULTI_SELECT, order = 2),
        ChecklistItem("a3", "Estimated area under cultivation (acres)", NUMBER, order = 3),
        ChecklistItem("a4", "Is irrigation system visible and functional?", YES_NO, photoRequired = true, order = 4),
        ChecklistItem("a5", "Any signs of pest or disease?", YES_NO, photoRequired = true, order = 5),
        ChecklistItem("a6", "Are input materials visible (fertilizer bags, seed containers)?", YES_NO, photoRequired = true, order = 6),
        ChecklistItem("a7", "Is storage facility present and in good condition?", YES_NO, order = 7),
        ChecklistItem("a8", "Number of workers observed", NUMBER, order = 8),
        ChecklistItem("a9", "General crop health assessment (1-5)", NUMBER, order = 9),
        ChecklistItem("a10", "Additional observations", TEXT, required = false, order = 10),
    ),
)
```

### Fintech Collateral Template

```kotlin
val fintechTemplate = VerticalTemplate(
    id = "tmpl_collateral_verification_v1",
    verticalType = VerticalType.FINTECH,
    name = "MSME Collateral/Business Verification",
    requiredPhotos = 6,
    checklist = listOf(
        ChecklistItem("f1", "Does the business exist at the stated address?", YES_NO, photoRequired = true, order = 1),
        ChecklistItem("f2", "Is business signage visible?", YES_NO, photoRequired = true, order = 2),
        ChecklistItem("f3", "Is the business currently operational?", YES_NO, order = 3),
        ChecklistItem("f4", "Is inventory/stock visible?", YES_NO, photoRequired = true, order = 4),
        ChecklistItem("f5", "Estimated inventory level (low, medium, high)", MULTI_SELECT, order = 5),
        ChecklistItem("f6", "Is equipment/machinery consistent with declared collateral?", YES_NO, order = 6),
        ChecklistItem("f7", "Is the owner/manager present?", YES_NO, order = 7),
        ChecklistItem("f8", "Are customers/foot traffic observed?", YES_NO, order = 8),
        ChecklistItem("f9", "Is business registration certificate visible?", YES_NO, photoRequired = true, order = 9),
        ChecklistItem("f10", "Overall business viability assessment (1-5)", NUMBER, order = 10),
    ),
)
```

## Firestore Security Rules (Summary)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users can read/write their own profile
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Clients can create tasks; read their own tasks
    match /tasks/{taskId} {
      allow create: if request.auth != null && request.resource.data.clientId == request.auth.uid;
      allow read: if request.auth != null && (
        resource.data.clientId == request.auth.uid ||
        resource.data.matchedInspectorId == request.auth.uid
      );
      // Status updates via Cloud Functions only (admin SDK bypasses rules)
    }

    // Evidence readable by task client and assigned inspector
    match /evidence/{evidenceId} {
      allow read: if request.auth != null; // further filtered by app logic
      allow create: if request.auth != null; // validated by Cloud Function trigger
    }

    // Escrow transactions readable by involved parties only
    match /escrow_transactions/{txnId} {
      allow read: if request.auth != null && (
        resource.data.clientId == request.auth.uid ||
        resource.data.inspectorId == request.auth.uid
      );
      // All writes via Cloud Functions (admin SDK)
    }

    // Templates are public read
    match /templates/{templateId} {
      allow read: if true;
    }
  }
}
```

---

*See [EOS-03-ARCHITECTURE.md](./EOS-03-ARCHITECTURE.md) for Firestore collection structure overview.*
*See [EOS-07-SECURITY-AND-COMPLIANCE.md](./EOS-07-SECURITY-AND-COMPLIANCE.md) for complete security rules and threat model.*
