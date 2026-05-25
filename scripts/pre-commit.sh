#!/bin/sh
# EyeOnSite pre-commit hook
# Runs formatting, static analysis, shared tests, Android build, and web build.
# Install: ./scripts/install-hooks.sh

set -e

echo "EyeOnSite pre-commit checks..."

echo "  [1/5] Spotless (formatting)..."
./gradlew spotlessCheck --daemon -q
echo "  Spotless passed"

echo "  [2/5] Detekt (static analysis)..."
./gradlew detektAll --daemon -q
echo "  Detekt passed"

echo "  [3/5] Shared Android tests..."
./gradlew :app:shared:testAndroid --daemon -q
echo "  Shared tests passed"

echo "  [4/5] Android debug build..."
./gradlew :app:androidApp:assembleDebug --daemon -q
echo "  Android build passed"

echo "  [5/5] Web production bundle..."
./gradlew :app:webApp:jsBrowserProductionWebpack --daemon -q
echo "  Web build passed"

echo "All pre-commit checks passed."
