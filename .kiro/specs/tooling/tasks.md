# Tooling — Tasks

## Tasks

### T1: Add Spotless + Detekt to version catalog
- [ ] Add `spotless`, `detekt`, `ktlint` versions to `libs.versions.toml`
- [ ] Add plugin aliases

### T2: Wire plugins into root build.gradle.kts
- [ ] Apply Spotless + Detekt plugins (apply false at root)
- [ ] Register `detektAll` aggregate task

### T3: Configure Spotless in each module
- [ ] `app/shared/build.gradle.kts`
- [ ] `app/androidApp/build.gradle.kts`
- [ ] `app/webApp/build.gradle.kts`
- [ ] `core/build.gradle.kts`

### T4: Detekt config file
- [ ] `config/detekt/detekt.yml` — rules config
- [ ] `config/detekt/baseline.xml` — generated from existing code

### T5: Pre-commit hook
- [ ] `scripts/pre-commit.sh`
- [ ] `scripts/install-hooks.sh`

### T6: Release-please config
- [ ] `release-please-config.json`
- [ ] `.release-please-manifest.json`
- [ ] `CHANGELOG.md` (initial)

### T7: GitHub Actions CI workflow
- [ ] `.github/workflows/ci.yml`
- [ ] `.github/workflows/release-please.yml`

### T8: Verify all checks pass
- [ ] `./gradlew spotlessCheck` — green
- [ ] `./gradlew detektAll` — green
- [ ] `./gradlew :app:androidApp:assembleDebug` — green
- [ ] `./gradlew :app:webApp:jsBrowserProductionWebpack` — green
