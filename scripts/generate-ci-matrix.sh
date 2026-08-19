#!/bin/bash

# Copyright (C) 2025 Zac Sweers
# SPDX-License-Identifier: Apache-2.0

set -euo pipefail

# Generate CI matrix for compiler-compat modules
# Both the version set and the default version come from the `:compiler-compat:compilerVersions`
# Gradle task, which reports whatever the compiler plugin devkit resolved from the
# `org.jetbrains.kotlin.compiler.plugin.devkit.*` properties in gradle.properties.

# --versions-only flag is for ./metrow check use to only print the versions and exit
versions_only=false
if [[ "${1:-}" == "--versions-only" ]]; then
    versions_only=true
fi

VERSIONS_TASK=":compiler-compat:compilerVersions"
VERSIONS_FILE="compiler-compat/build/ci/tested-compiler-versions.txt"
IDE_ONLY_VERSIONS_FILE="compiler-compat/build/ci/ide-only-compiler-versions.txt"
DEFAULT_VERSION_FILE="compiler-compat/build/ci/default-compiler-version.txt"

if [[ "$versions_only" != true ]]; then
    echo "🔍 Reading versions from $VERSIONS_TASK..."
fi

# On CI, go through the wrapper script so this job uses the same worker and heap limits as the rest
if [[ -n "${CI:-}" && -x scripts/run-ci-gradle.sh ]]; then
    gradle=(./scripts/run-ci-gradle.sh)
else
    gradle=(./gradlew)
fi

# Redirect Gradle's own output so it can't be mistaken for the version list in --versions-only mode
if ! "${gradle[@]}" --quiet "$VERSIONS_TASK" >&2; then
    echo "❌ $VERSIONS_TASK failed" >&2
    exit 1
fi

# Versions come out oldest-first, which CI relies on to pick the newest tested version
versions=$([ -f "$VERSIONS_FILE" ] && grep -v '^[[:space:]]*$' "$VERSIONS_FILE" || true)

if [ -z "$versions" ]; then
    echo "❌ No versions reported by $VERSIONS_TASK in $VERSIONS_FILE" >&2
    exit 1
fi

latest_kotlin_version=$(echo "$versions" | tail -n 1)

# Versions that only exist because an IDE build ships them. The devkit Gradle plugin
# won't register these as functionalTest targets, so the matrix flags them and CI skips that step.
ide_only_versions=$([ -f "$IDE_ONLY_VERSIONS_FILE" ] && grep -v '^[[:space:]]*$' "$IDE_ONLY_VERSIONS_FILE" || true)

# The version the plain `test`/`defaultTest` tasks run against, i.e. the build's own Kotlin unless
# `-Porg.jetbrains.kotlin.compiler.plugin.devkit.defaultTestVersion` overrides it
default_kotlin_version=$(head -n 1 "$DEFAULT_VERSION_FILE")

if [ -z "$default_kotlin_version" ]; then
    echo "❌ No default version reported by $VERSIONS_TASK in $DEFAULT_VERSION_FILE" >&2
    exit 1
fi

if [[ "$versions_only" == true ]]; then
    # Just output the versions, one per line
    echo "$versions"
    exit 0
fi

is_ide_only() {
    printf '%s\n' "$ide_only_versions" | grep -Fxq "$1"
}

echo "📦 Found versions:"
for version in $versions; do
    if is_ide_only "$version"; then
        echo "  - $version (IDE only)"
    elif [ "$version" = "$default_kotlin_version" ]; then
        echo "  - $version (default)"
    else
        echo "  - $version"
    fi
done

# Convert to a JSON matrix. Entries go under `include` so each can carry `for-ide` next to its
# version; with no other matrix keys GitHub still creates exactly one job per entry, and
# `matrix.kotlin-compiler` keeps working unchanged.
json_array="["
first=true
for version in $versions; do
    if [ "$first" = true ]; then
        first=false
    else
        json_array="$json_array,"
    fi

    # Quoted deliberately: GitHub's expression comparisons coerce across types, so a JSON boolean
    # here would hinge on whether it survives `fromJson` as a boolean or a string. A string is
    # unambiguous against `matrix.for-ide == 'true'`.
    if is_ide_only "$version"; then
        for_ide='"true"'
    else
        for_ide='"false"'
    fi

    json_array="$json_array{\"kotlin-compiler\":\"$version\",\"for-ide\":$for_ide}"
done
json_array="$json_array]"

matrix_json="{\"include\":$json_array}"

echo ""
echo "✅ Generated matrix JSON:"
echo "$matrix_json"

# Pretty print for better readability
if command -v jq >/dev/null 2>&1; then
    echo ""
    echo "📋 Pretty-printed matrix:"
    echo "$matrix_json" | jq .
fi

# Output for GitHub Actions (if running in CI)
if [ "${GITHUB_OUTPUT:-}" ]; then
    echo "matrix=$matrix_json" >> "$GITHUB_OUTPUT"
    echo "default_kotlin_version=$default_kotlin_version" >> "$GITHUB_OUTPUT"
    echo "latest_kotlin_version=$latest_kotlin_version" >> "$GITHUB_OUTPUT"
    echo "🚀 Matrix written to GITHUB_OUTPUT"
fi
