# EOS-A9 — CI/CD and Distribution

**EyeOnSite** · Manti Labs · May 2026 · v0.1-DRAFT

---

## Overview

EyeOnSite ships two Android APK flavors (client, inspector) from a single KMP monorepo. This document covers the complete pipeline from code push to testers' devices: lint, build, test, artifact upload, and Firebase App Distribution.

---

## Android Product Flavors

The `androidApp` module defines two flavors under the `variant` dimension:

| Flavor | App Name | `BuildConfig.FLAVOR` | Purpose |
|--------|----------|----------------------|---------|
| `client` | EyeOnSite | `"client"` | Diaspora investor app |
| `inspector` | EyeOnSite Inspector | `"inspector"` | On-ground verifier app |

Both flavors share `applicationId = "com.maku.eyeonsite"` — a single Firebase Android app registration covers both. Separate `applicationId` values (which would require separate Firebase registrations) are deferred to production release.

Flavor-specific resources live in:

```
app/androidApp/src/
├── client/res/values/strings.xml   # app_name = "EyeOnSite"
└── inspector/res/values/strings.xml  # app_name = "EyeOnSite Inspector"
```

The shared `App.kt` composable accepts `forcedVariant: AppVariant?`. `MainActivity` derives this from `BuildConfig.FLAVOR` and passes it down, which:
- Locks the theme to the correct variant
- Auto-assigns the role in `NeedsRole` state (skips `RoleSelectionScreen`)

`buildFeatures { buildConfig = true }` is required in AGP 8+ to generate `BuildConfig`.

---

## Gradle Build Tasks

| Task | Description |
|------|-------------|
| `assembleClientDebug` | Build client debug APK |
| `assembleInspectorDebug` | Build inspector debug APK |
| `assembleClientRelease` | Build client release APK |
| `assembleInspectorRelease` | Build inspector release APK |
| `appDistributionUploadClientDebug` | Build + upload client debug to Firebase App Distribution |
| `appDistributionUploadInspectorDebug` | Build + upload inspector debug to Firebase App Distribution |

Output paths:
```
app/androidApp/build/outputs/apk/client/debug/androidApp-client-debug.apk
app/androidApp/build/outputs/apk/inspector/debug/androidApp-inspector-debug.apk
```

---

## Firebase App Distribution

### Distribution Method: Firebase CLI

Distribution uses the Firebase CLI (`firebase appdistribution:distribute`) rather than the Firebase App Distribution Gradle plugin. The Gradle plugin (`com.google.firebase.appdistribution`) uses the legacy AGP `AppExtension` API that was removed in AGP 8+. Since this project targets AGP 9.0.1, the CLI is the compatible path.

### Tester Group

The tester group `internal` was created in the Firebase console under **App Distribution → Groups**. Add testers by email in the console. Testers receive an install link via email once an APK is uploaded.

### Release Notes

`RELEASE_NOTES.txt` in the project root is the default release notes file. The CI workflow overwrites it at runtime with the commit SHA and timestamp (or the manually entered release notes when triggered via `workflow_dispatch`). Locally, edit the file before running the upload task.

### Local Distribution

Prerequisites:
- Firebase CLI installed: `npm install -g firebase-tools`
- Logged in: `firebase login` (check with `firebase projects:list`)
- APKs built (see Gradle Build Tasks above)

```bash
FIREBASE_APP_ID="1:922315268903:android:9a262a3db6364511fb7f02"

# Edit release notes first (optional)
echo "Testing the inspector map improvements" > RELEASE_NOTES.txt

# Build + upload client APK
./gradlew :app:androidApp:assembleClientDebug
firebase appdistribution:distribute \
  app/androidApp/build/outputs/apk/client/debug/androidApp-client-debug.apk \
  --app "$FIREBASE_APP_ID" \
  --groups "internal" \
  --release-notes-file RELEASE_NOTES.txt

# Build + upload inspector APK
./gradlew :app:androidApp:assembleInspectorDebug
firebase appdistribution:distribute \
  app/androidApp/build/outputs/apk/inspector/debug/androidApp-inspector-debug.apk \
  --app "$FIREBASE_APP_ID" \
  --groups "internal" \
  --release-notes-file RELEASE_NOTES.txt
```

The CLI reads your local Firebase credentials from `~/.config/firebase/`. No token env var needed when running locally as an authenticated user.

### CI Distribution (GitHub Actions)

To activate the distribution workflow in CI:

1. **Get a CI token** — run locally:
   ```bash
   firebase login:ci
   ```
   Copy the printed token.

2. **Add the secret to GitHub** — go to:
   `Settings → Secrets and variables → Actions → New repository secret`
   Name: `FIREBASE_TOKEN`, Value: the token from step 1.

3. **Trigger the workflow** — the workflow at `.github/workflows/distribute.yml` runs:
   - Automatically on every push to `main`
   - Manually via **Actions → Distribute → Run workflow** (with an optional release notes text field)

---

## GitHub Actions Workflows

### `ci.yml` — Continuous Integration

Runs on every push and pull request. Jobs:

| Job | Runs on | Depends on | What it does |
|-----|---------|------------|--------------|
| `lint` | ubuntu-latest | — | Spotless check + Detekt |
| `build-shared` | ubuntu-latest | lint | Compile KMP metadata, Android, JS targets |
| `build-android` | ubuntu-latest | build-shared | `assembleClientDebug` + `assembleInspectorDebug`; uploads both APKs as artifacts (7-day retention) |
| `build-web` | ubuntu-latest | build-shared | JS production webpack bundle; uploads as artifact |
| `test` | ubuntu-latest | build-shared | `testAndroid` on shared module; uploads test reports |
| `deploy-web` | ubuntu-latest | build-web + test | Firebase Hosting deploy (main branch only; needs `FIREBASE_SERVICE_ACCOUNT` secret) |

Artifact names:
- `client-debug-apk` → `app/androidApp/build/outputs/apk/client/debug/*.apk`
- `inspector-debug-apk` → `app/androidApp/build/outputs/apk/inspector/debug/*.apk`

### `distribute.yml` — Firebase App Distribution

Runs on push to `main` and `workflow_dispatch`. Single job:

1. Write `RELEASE_NOTES.txt` (commit SHA + timestamp, or user-supplied notes)
2. `assembleClientDebug` → `appDistributionUploadClientDebug`
3. `assembleInspectorDebug` → `appDistributionUploadInspectorDebug`

Requires: `FIREBASE_TOKEN` repository secret.

### `release-please.yml` — Automated Release PRs

Manages changelog generation and version bump PRs via Release Please.

---

## Secrets Reference

| Secret Name | Where Set | Used By | How to Get |
|-------------|-----------|---------|------------|
| `FIREBASE_TOKEN` | GitHub repo secrets | `distribute.yml` | `firebase login:ci` |
| `FIREBASE_SERVICE_ACCOUNT` | GitHub repo secrets | `ci.yml` deploy-web job | Firebase console → Service accounts (quota-limited; defer if needed) |
| `MAPS_API_KEY` | `local.properties` (local only) | Android build manifest | Google Cloud Console → Credentials |
| `STRIPE_SECRET_KEY` | Firebase Secret Manager | Cloud Functions | Stripe dashboard → Developers → API keys |
| `STRIPE_WEBHOOK_SECRET` | Firebase Secret Manager | Cloud Functions | Stripe dashboard → Webhooks |
| `FLW_SECRET_KEY` | Firebase Secret Manager | Cloud Functions | Flutterwave dashboard → API |

`local.properties` is gitignored. Production secrets use Google Secret Manager accessed via `defineSecret()` in Cloud Functions v2 config.

---

## Lint and Code Quality

| Tool | Config | Runs |
|------|--------|-------|
| Spotless + ktlint 1.5.0 | `build.gradle.kts` per module | Pre-commit hook + CI |
| Detekt 1.23.8 | `config/detekt/detekt.yml` + `config/detekt/baseline.xml` | Pre-commit hook + CI |

Known detekt 1.23.8 limitation: crashes with an NPE on files that directly reference Firebase Android SDK types. Affected files are excluded via glob patterns in the root `build.gradle.kts` detekt config.

Pre-commit pipeline (run before every commit):
```bash
./gradlew spotlessApply
./gradlew detektAll
./gradlew :app:shared:testAndroid
./gradlew :app:androidApp:assembleClientDebug :app:androidApp:assembleInspectorDebug
./gradlew :app:webApp:jsBrowserProductionWebpack
```

All 5 must pass before committing.

---

## Firebase Project Reference

| Property | Value |
|----------|-------|
| Project ID | `eyeonsite-3b8e0` |
| Hosting URL | https://eyeonsite-3b8e0.web.app |
| Android app package | `com.maku.eyeonsite` |
| Functions region | `africa-south1` |
| App Distribution group | `internal` |
