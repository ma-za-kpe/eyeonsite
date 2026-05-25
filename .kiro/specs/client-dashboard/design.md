# Client Dashboard — Design

## Screens
```
shared/commonMain/ui/client/
├── ClientHomeScreen.kt       # task list + FAB
├── TaskDetailScreen.kt       # live status + inspector card + map
└── ClientHomeViewModel.kt
```

## ClientHomeState
```kotlin
data class ClientHomeState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val unreadNotifications: Int = 0,
)
```

## Task Card
- Icon by vertical (🏗️ 🏠 🌾)
- Title + address
- Status chip (color-coded per TaskStatus)
- Budget + date

## TaskDetailScreen Layout
1. Status card (animated status transitions)
2. Inspector profile card (appears after MATCHED)
3. Map card (task pin + inspector dot)
4. Instructions
5. "Review Evidence" button (appears at EVIDENCE_SUBMITTED)

## Analytics Events
- `client_home_viewed` | `task_detail_opened` | `review_evidence_tapped`
