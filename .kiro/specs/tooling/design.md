# Tooling — Design

## Gradle Plugin Versions
```toml
# libs.versions.toml additions
spotless = "7.0.4"
detekt = "1.23.8"
```

## Root build.gradle.kts Changes
```kotlin
plugins {
    id("com.diffplug.spotless") version libs.versions.spotless apply false
    id("io.gitlab.arturbosch.detekt") version libs.versions.detekt apply false
}
```

## Spotless Config (applied in each module's build.gradle.kts)
```kotlin
spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}
```

## Detekt Config (root-level task aggregating all modules)
```kotlin
// root build.gradle.kts
tasks.register("detektAll") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("detekt") })
}
```

## Pre-Commit Hook Script
```bash
#!/bin/sh
# scripts/pre-commit.sh
set -e
echo "Running pre-commit checks..."
./gradlew spotlessCheck --daemon
./gradlew detektAll --daemon
./gradlew :app:shared:testDebugUnitTest --daemon
echo "All checks passed."
```

## GitHub Actions Structure
```yaml
jobs:
  lint:        spotlessCheck + detektAll
  build:       shared + androidApp + webApp (depends on lint)
  test:        shared unit tests (depends on build)
  deploy-web:  firebase deploy (depends on test, only on main)
```

## Secrets Required
- `FIREBASE_SERVICE_ACCOUNT`: Firebase service account JSON for CI deploy
  - Generate: Firebase Console → Project Settings → Service Accounts → Generate new private key
  - Add to GitHub: Settings → Secrets → Actions → New repository secret
