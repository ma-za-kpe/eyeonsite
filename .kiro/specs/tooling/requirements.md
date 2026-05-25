# Tooling — Requirements

**Feature:** Production engineering toolchain  
**Covers:** Spotless, Detekt, pre-commit hooks, release-please, GitHub Actions CI

## Requirements

### TR-1: Code Formatting (Spotless + ktlint)
- All Kotlin files formatted with ktlint rules
- `./gradlew spotlessCheck` fails CI on violations
- `./gradlew spotlessApply` auto-fixes locally
- Applied to: shared, androidApp, webApp, core, server

### TR-2: Static Analysis (Detekt)
- Detekt runs on all Kotlin source sets
- Baseline file at `config/detekt/baseline.xml` (existing code excluded)
- New violations block CI
- Config at `config/detekt/detekt.yml`

### TR-3: Pre-Commit Hook
- Script at `scripts/pre-commit.sh`
- Runs: spotlessCheck → detektAll → shared unit tests
- Installed via `scripts/install-hooks.sh`
- Fails fast: stops on first error

### TR-4: Release Please
- `release-please-config.json` at repo root
- `.release-please-manifest.json` tracks current version
- `CHANGELOG.md` auto-updated on release PR merge
- Conventional Commits drive version bumps

### TR-5: GitHub Actions CI
- Workflow: `.github/workflows/ci.yml`
- Triggers: push to any branch, PR to main
- Jobs: lint → build-shared → build-android → build-web → test
- Deploy job: triggers on push to main, deploys web to Firebase Hosting
- Uses secrets: `FIREBASE_SERVICE_ACCOUNT` for deploy

### TR-6: Detekt Config
- Max complexity: 15
- Max line length: 120
- No magic numbers (exceptions: 0, 1, -1, 2)
- No wildcard imports (except Compose `.*`)
