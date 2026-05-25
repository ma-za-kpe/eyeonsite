# Evidence Capture — Design

## Screens
```
shared/commonMain/ui/inspector/
├── CaptureScreen.kt          # camera viewfinder + GPS + progress
├── ChecklistScreen.kt        # post-capture checklist
├── EvidenceSummaryScreen.kt  # review + submit
└── CaptureViewModel.kt       # state + upload queue
```

## CaptureState
```kotlin
data class CaptureState(
    val taskId: String,
    val template: Template,
    val capturedPhotos: List<CapturedPhoto> = emptyList(),
    val currentAngleIndex: Int = 0,
    val gpsStatus: GpsStatus = GpsStatus.ACQUIRING,  // ACQUIRING, GOOD, FAIR, POOR
    val currentLocation: GeoPoint? = null,
    val checklistResponses: List<ChecklistResponse> = emptyList(),
    val notes: String = "",
    val uploadProgress: Float = 0f,
    val isSubmitting: Boolean = false,
    val submittedEvidenceId: String? = null,
)

data class CapturedPhoto(
    val localPath: String,
    val gpsLat: Double,
    val gpsLng: Double,
    val gpsAccuracyMeters: Float,
    val capturedAt: Instant,
    val angle: PhotoAngle,
)
```

## Camera (expect/actual)
```kotlin
// commonMain
expect class InAppCamera {
    @Composable
    fun CameraPreview(onPhotoCaptured: (CapturedPhoto) -> Unit)
}
// androidMain — CameraX + FusedLocationProvider
// iosMain — AVFoundation + CoreLocation
```

## Offline Upload Queue (SQLDelight)
```sql
CREATE TABLE pending_upload (
    id TEXT PRIMARY KEY,
    task_id TEXT NOT NULL,
    local_path TEXT NOT NULL,
    gps_lat REAL NOT NULL,
    gps_lng REAL NOT NULL,
    captured_at INTEGER NOT NULL,
    uploaded INTEGER NOT NULL DEFAULT 0
);
```

## Evidence Firestore Doc
```
evidence/{evidenceId}: {
  taskId, inspectorId,
  photos: [{ url, gpsLat, gpsLng, gpsAccuracy, angle, capturedAt }],
  checklistResponses: [{ itemId, type, value }],
  inspectorNotes, qualityScore,
  captureLocation, distanceFromTask,
  submittedAt
}
```

## Quality Score (Demo — simplified)
```kotlin
fun computeQualityScore(photos: List<CapturedPhoto>, task: Task): Float {
    val photoScore = (photos.size.toFloat() / task.requiredPhotos).coerceAtMost(1f)
    val gpsScore = if (photos.all { it.gpsAccuracyMeters < 50f }) 1f else 0.5f
    return (photoScore * 0.6f) + (gpsScore * 0.4f)
}
```
