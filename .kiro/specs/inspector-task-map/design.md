# Inspector Task Map — Design

## Screens
```
shared/commonMain/ui/inspector/
├── InspectorHomeScreen.kt    # map + bottom sheet
├── TaskDetailSheet.kt        # bottom sheet task detail + accept
└── InspectorHomeViewModel.kt
```

## InspectorHomeState
```kotlin
data class InspectorHomeState(
    val inspectorLocation: GeoPoint? = null,
    val availableTasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val isAvailable: Boolean = true,
    val earnings: InspectorEarnings = InspectorEarnings(),
)
```

## Firestore Query
```kotlin
// Real-time listener
firestore.collection("tasks")
    .whereEqualTo("status", "POSTED")
    .whereGreaterThan("location", geoQueryBounds.lower)
    .whereLessThan("location", geoQueryBounds.upper)
    .addSnapshotListener { ... }
```

## Accept Flow
1. Inspector taps Accept
2. Write: `tasks/{id} { status: MATCHED, matchedInspectorId: uid, inspectorEta: eta }`
3. Write: `inspectors/{uid} { activeTaskId: taskId }`
4. Navigate to CaptureScreen

## Analytics Events
- `task_map_viewed` | `task_detail_opened` | `task_accepted` (distance, budget params)
