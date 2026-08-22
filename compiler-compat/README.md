# Metro Compiler Compatibility Layer

This module provides a compatibility layer for Metro's compiler plugin to work across different Kotlin compiler versions. As the Kotlin compiler APIs evolve and change between versions, this layer abstracts away version-specific differences.

This collection of artifacts is not published and is instead shaded into Metro's compiler plugin.

## Overview

The Kotlin compiler plugin APIs are not stable and can change between versions. Some APIs get deprecated, renamed, or removed entirely. This compatibility layer gives Metro's compiler one set of declarations to call regardless of the underlying Kotlin version.

## IDE Plugin

The Kotlin IDE plugin bundles its own compiler copy and can be checked at `lib/kotlinc.kotlin-compiler-common.jar/META-INF/compiler.version`.

IDE plugins can be downloaded from https://plugins.jetbrains.com/plugin/6954-kotlin/versions/stable.

Note this version may not have published artifacts anywhere, so it may require picking the nearest one and declaring the appropriate `pre`/`post` boundary in `build.gradle.kts`.

### Extracting Compiler Version from IDE

Use the provided script to extract the bundled Kotlin compiler version from an Android Studio or IntelliJ installation:

```bash
./extract-kotlin-compiler-txt.sh "/path/to/Android Studio.app"
```

This prints the compiler version (e.g., `2.2.255-dev-255`) to stdout.

### Fake IDE Compiler Versions

Android Studio canary builds report a fake compiler version such as `2.3.255-dev-255`. Mapping those back to the real Kotlin version is handled by the compiler plugin devkit, which ships its own IDE-build-to-Kotlin-version table and resolves it from the running IDE's build number.

## Architecture

Compatibility is expressed as `expect` declarations in `commonMain`, with an `actual` per version bracket. Each bracket is a source set created by the `versionHierarchy` block in `build.gradle.kts`, which declares boundaries as `pre`/`post` pairs around a Kotlin version:

```
pre24Main/       // < 2.4.0
post24Main/      // >= 2.4.0
pre2420Beta2Main // < 2.4.20-Beta2
...
```

`preDev`/`postDev` variants split on a dev-build boundary rather than a release. Declarations carry a `@CompatApi(since = ..., reason = ...)` annotation recording which Kotlin version forced the split and why.

### Adding Support for a New Kotlin Version

Most versions need nothing at all — they compile against the nearest existing bracket. When a version does break an API:

1. Add the `pre`/`post` boundary for it to the `versionHierarchy` block in `build.gradle.kts`, nested at the right point in the existing chain.
2. Add the `expect` declaration to `commonMain` with a `@CompatApi` annotation, and an `actual` in each new source set.
3. Run the compiler tests against that version, e.g. `./metrow test --version 2.4.20-Beta1`.

Which versions CI tests against is a separate concern — the devkit resolves that from the `kotlin.compiler.plugin.devkit.*` properties in the root `gradle.properties`. Run `./gradlew :compiler-compat:compilerVersions` to print the current set.

### Runtime Selection

The compiler-compat artifacts are shaded into Metro's compiler plugin as a multi-release jar. At runtime the devkit's `VersionResolution` picks the bracket matching the running compiler, so a single Metro release supports every version in the range without separate builds.

dev track versions are special-cased there, because Kotlin's release process creates divergent tracks: `2.3.20-dev-7791` is cut from trunk while `2.3.20-Beta1` is cut from a stable branch, so plain maturity ordering (`dev < BETA`) would pick the wrong bracket. Resolution prefers same-base dev builds for a dev compiler before falling back across base versions.

## Development Notes

- Provide an `actual` in every bracket an `expect` covers, even if some are no-ops for certain versions
- Record the version and reason on each declaration with `@CompatApi`
- Test thoroughly with the target Kotlin version before releasing
- Keep the surface focused and minimal - only add a declaration here when a version difference forces it
