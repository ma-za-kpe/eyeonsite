#!/bin/sh
# Installs EyeOnSite git hooks.

cp scripts/pre-commit.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
echo "Pre-commit hook installed."
