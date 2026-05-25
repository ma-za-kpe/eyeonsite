# Evidence Review — Design

## Screens
```
shared/commonMain/ui/client/
├── EvidenceReviewScreen.kt   # THE money screen
└── EvidenceReviewViewModel.kt
```

## EvidenceReviewState
```kotlin
data class EvidenceReviewState(
    val task: Task,
    val evidence: EvidencePackage,
    val autoAcceptDeadline: Instant,
    val timeRemainingMs: Long,
    val isActing: Boolean = false,
    val action: ReviewAction? = null,  // ACCEPTED, REVISION_REQUESTED, DISPUTED
)
```

## Screen Layout (top → bottom)
1. Quality score badge + "Evidence Ready" header
2. Photo grid (2-col) — tap → full-screen with GPS overlay
3. Map card — task pin + inspector GPS track + distance
4. Checklist responses (scrollable)
5. Inspector notes
6. Auto-accept countdown timer
7. Action buttons: [Dispute] [Request Revision] [Accept ✓]

## Analytics Events
- `evidence_review_opened` | `evidence_accepted` | `evidence_revision_requested` | `evidence_disputed`
