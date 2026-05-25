# EyeOnSite - Quality Gates

## Pre-Commit

Install hook:

```bash
./scripts/install-hooks.sh
```

The hook must pass all of these before a commit:

```bash
./gradlew spotlessCheck
./gradlew detektAll
./gradlew :app:shared:testAndroid
./gradlew :app:androidApp:assembleDebug
./gradlew :app:webApp:jsBrowserProductionWebpack
```

## CI Pipeline

On every PR and push:

1. `spotlessCheck` - formatting
2. `detektAll` - static analysis
3. `:app:shared:compileKotlinMetadata`, `:app:shared:compileAndroidMain`, `:app:shared:compileKotlinJs` - shared targets used on Linux
4. `:app:androidApp:assembleDebug` - Android APK
5. `:app:webApp:jsBrowserProductionWebpack` - web bundle
6. `:app:shared:testAndroid` - shared Android unit tests
7. Main branch only: deploy Firebase Hosting

## Linting

- Spotless: ktlint. Auto-fix with `./gradlew spotlessApply`.
- Detekt: baseline at `config/detekt/baseline.xml`.
- Max line length: 120 chars.

## Test Strategy

| Layer | Tool | Location |
|-------|------|----------|
| Domain/use cases | kotlin.test | `shared/commonTest` |
| Analytics events | MockAnalyticsService | `shared/commonTest` |
| Android UI | Compose UI Test | `androidApp/androidTest` |
| Web landing | browser visual QA | `webApp` |

## Definition Of Done

- [ ] Spec in `.kiro/specs/<feature>/` is complete.
- [ ] Tests are written first for non-trivial behavior.
- [ ] `@Preview` exists for new mobile Composables.
- [ ] `spotlessCheck` and `detektAll` pass.
- [ ] Android and web builds succeed.
- [ ] Analytics events exist for all CTAs and key screens.
- [ ] No production secrets are committed.

## Version Management

- release-please manages `CHANGELOG.md` from Conventional Commits.
- `feat:` triggers minor releases, `fix:` triggers patch releases, and `feat!:` triggers major releases.
- Current version: `0.1.0`.

## Firebase Services To Enable

Authentication, Firestore, Cloud Storage, Cloud Functions, Hosting, Analytics, App Check,
Cloud Messaging, Crashlytics, Performance Monitoring, Test Lab, App Distribution,
Secret Manager, Cloud Vision API, and Google Maps Platform APIs.
