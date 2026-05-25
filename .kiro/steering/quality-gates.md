# EyeOnSite — Quality Gates

## Pre-Commit (must all pass before any commit)
```bash
./gradlew spotlessCheck
./gradlew detektAll
./gradlew :app:shared:testDebugUnitTest
```
Install hook: `./scripts/install-hooks.sh`

## CI Pipeline (GitHub Actions)
On every PR and push to main:
1. `spotlessCheck` — formatting
2. `detektAll` — static analysis
3. `:app:shared:build` — KMP shared compiles
4. `:app:androidApp:assembleDebug` — Android APK
5. `:app:webApp:jsBrowserProductionWebpack` — Web bundle
6. `:app:shared:testDebugUnitTest` — unit tests
7. (main only) `firebase deploy --only hosting`

## Linting
- **Spotless**: ktlint. Auto-fix: `./gradlew spotlessApply`
- **Detekt**: baseline at `config/detekt/baseline.xml`
- Max line length: 120 chars

## Test Strategy
| Layer | Tool | Location |
|-------|------|----------|
| Domain / use cases | kotlin.test | `shared/commonTest` |
| Analytics events | MockAnalyticsService | `shared/commonTest` |
| Android UI | Compose UI Test | `androidApp/androidTest` |

## Definition of Done
- [ ] Spec in `.kiro/specs/<feature>/` complete
- [ ] Tests written first (TDD)
- [ ] `@Preview` on every new Composable
- [ ] `spotlessCheck` + `detektAll` pass
- [ ] Android + Web builds succeed
- [ ] Analytics events on all CTAs

## Version Management
- release-please manages CHANGELOG automatically via Conventional Commits
- `feat:` → minor, `fix:` → patch, `feat!:` → major
- Current version: `0.1.0`

## Firebase Services to Enable
Authentication (Phone + Google), Firestore, Cloud Storage, Cloud Functions,
Hosting, Analytics, Crashlytics, Performance Monitoring, FCM
