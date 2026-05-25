#!/bin/sh
# EyeOnSite pre-commit hook
# Runs: spotlessCheck → detektAll → shared unit tests
# Install: ./scripts/install-hooks.sh

set -e

echo "🔍 EyeOnSite pre-commit checks..."

echo "  [1/3] Spotless (formatting)..."
./gradlew spotlessCheck --daemon -q
echo "  ✅ Spotless passed"

echo "  [2/3] Detekt (static analysis)..."
./gradlew detektAll --daemon -q
echo "  ✅ Detekt passed"

echo "  [3/3] Unit tests..."
./gradlew :app:shared:testDebugUnitTest --daemon -q
echo "  ✅ Tests passed"

echo "✅ All pre-commit checks passed."
