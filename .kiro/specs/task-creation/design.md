# Task Creation — Design

## Screens
```
shared/commonMain/ui/client/
├── CreateTaskScreen.kt     # multi-step form
└── CreateTaskViewModel.kt  # state + Firestore write
```

## CreateTaskState
```kotlin
data class CreateTaskState(
    val location: GeoPoint? = null,
    val address: String = "",
    val templateId: String = "construction_v1",
    val instructions: String = "",
    val budgetGhs: Int = 60,
    val isPosting: Boolean = false,
    val error: String? = null,
    val postedTaskId: String? = null,  // non-null = success
)
```

## Firestore Write
```kotlin
// tasks/{ulid}
Task(
    id = ulid(),
    clientId = currentUid,
    verticalType = VerticalType.CONSTRUCTION,
    templateId = "construction_v1",
    location = state.location,
    address = state.address,
    instructions = state.instructions,
    budget = Money(state.budgetGhs, "GHS"),
    status = TaskStatus.POSTED,
    createdAt = now(),
)
```

## Map Integration (expect/actual)
```kotlin
// commonMain
expect class MapLocationPicker {
    @Composable
    fun LocationPickerView(onLocationSelected: (GeoPoint, String) -> Unit)
}
// androidMain — Maps Compose
// jsMain — Google Maps JS API
```

## Analytics Events
- `task_creation_started` | `task_location_set` | `task_posted` (budget, vertical params)
