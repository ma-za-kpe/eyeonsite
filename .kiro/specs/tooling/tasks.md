# Tooling - Tasks

## Status Legend

- [ ] Not started
- [x] Complete

## Tasks

### T1: Add Spotless and Detekt to version catalog

- [x] Add `spotless`, `detekt`, and `ktlint` versions to `libs.versions.toml`
- [x] Add plugin aliases

### T2: Wire plugins into root `build.gradle.kts`

- [x] Apply Spotless and Detekt plugins with `apply false` at root
- [x] Register `detektAll` aggregate task

### T3: Configure Spotless in each module

- [x] `app/shared/build.gradle.kts`
- [x] `app/androidApp/build.gradle.kts`
- [x] `app/webApp/build.gradle.kts`
- [x] `core/build.gradle.kts`

### T4: Detekt config file

- [x] `config/detekt/detekt.yml` - rules config
- [x] `config/detekt/baseline.xml` - generated baseline file

### T5: Pre-commit hook

- [x] `scripts/pre-commit.sh`
- [x] `scripts/install-hooks.sh`
- [x] `.git/hooks/pre-commit` installed locally

### T6: Release Please config

- [x] `release-please-config.json`
- [x] `.release-please-manifest.json`
- [x] `CHANGELOG.md` initial entry

### T7: GitHub Actions CI workflow

- [x] `.github/workflows/ci.yml`
- [x] `.github/workflows/release-please.yml`

### T8: Verify all checks pass

- [x] `./gradlew spotlessCheck`
- [x] `./gradlew detektAll`
- [x] `./gradlew :app:shared:testAndroid`
- [x] `./gradlew :app:androidApp:assembleDebug`
- [x] `./gradlew :app:webApp:jsBrowserProductionWebpack`
- [x] `./gradlew :app:shared:compileKotlinMetadata :app:shared:compileAndroidMain :app:shared:compileKotlinJs`
- [x] `./gradlew :server:test`
