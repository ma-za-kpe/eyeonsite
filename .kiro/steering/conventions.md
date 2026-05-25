# EyeOnSite — Code Conventions

## Naming
| Thing | Convention | Example |
|-------|-----------|---------|
| Package | lowercase, dots | `com.maku.eyeonsite.ui.auth` |
| Kotlin files | PascalCase | `SignInScreen.kt` |
| Composables | PascalCase | `SignInScreen`, `LogoMark` |
| Domain models | PascalCase data classes | `EvidencePackage`, `TaskManifest` |
| Enums | SCREAMING_SNAKE_CASE values | `ESCROW_FUNDED`, `INSPECTOR_MATCHED` |
| Cloud Functions | camelCase | `onTaskFunded`, `releaseEscrow` |
| Firestore collections | snake_case | `escrow_transactions` |
| IDs | ULID for domain objects, Firebase UID for users | — |

## Brand Colors (always use named constants, never raw hex)
```kotlin
// From Color.kt — use these, never Color(0xFF...)
Forest, DeepForest, SavannaGold, SignalGreen, AlertCoral,
CautionAmber, InfoBlue, Ink, Ash, Stone, Sand, Ivory, White
```

## Compose Conventions
- Every screen wraps with `EyeOnSiteTheme { ... }`
- Every public `@Composable` has a `@Preview` in the androidMain preview file
- Use `MaterialTheme.colorScheme.*` for semantic colors, named constants for brand colors
- State hoisting: state up, events down
- No business logic in composables — use ViewModels / use cases

## Architecture (Clean Architecture + MVVM)
```
shared/commonMain/
  domain/
    model/       # Pure data classes (no Android/Firebase imports)
    usecase/     # Business logic (single responsibility)
    repository/  # Interfaces only
  data/
    remote/      # Ktor + Firebase wrappers
    local/       # SQLDelight DAOs
    repository/  # Implementations
  ui/
    theme/       # Color, Type, Theme
    auth/        # SignInScreen, etc.
```

## Offline-First Rule
Every user action MUST succeed offline:
1. Write to SQLDelight local cache first
2. Add to `pending_sync` queue
3. Background sync worker processes when online
4. Firestore is source of truth; local cache is write-ahead log

## Task Status Machine (valid transitions only)
```
DRAFT → POSTED → ESCROW_FUNDED → INSPECTOR_MATCHED → IN_PROGRESS
→ EVIDENCE_SUBMITTED → UNDER_REVIEW → ACCEPTED | DISPUTED | REVISION_REQUESTED
UNDER_REVIEW → AUTO_ACCEPTED (48hr timer)
```
Invalid transitions rejected server-side. Client never sets status directly.

## Commit Convention (Conventional Commits)
```
feat: add GPS overlay to capture mode
fix: correct escrow release on auto-accept
docs: update CLAUDE.md with validation pipeline
chore: bump Firebase BoM to 34.13.0
test: add unit tests for AnalyticsService mock
ci: add GitHub Actions workflow
```

## File Header Comments
Every new Kotlin file gets a one-line doc comment:
```kotlin
/** SignInScreen — phone OTP entry point, shared across Android/iOS/Web. */
```
