# IDE Integration Tests

Verifies that Metro's compiler plugin works inside a real IDE. Each test launches an actual IntelliJ
IDEA or Android Studio, imports a generated Gradle project that applies the Metro plugin, and checks
that FIR analysis with Metro's generators and checkers produces the diagnostics and inlays it should
— and nothing else.

Everything except the test sources comes from the dev kit, through the `ideTest { }` half of its
Gradle plugin support, so this module is just:

```
ide-integration-tests/
├── build.gradle.kts  # which IDEs to run against, and how to recognise a Metro error
└── src/ideTest       # MetroIdeSmokeTest: the project to analyse and what to expect of it
```

this module exists so multi-minute IDE runs stay well away from that project's own `check`, and applies the same dev kit
plugin for nothing but its `ideTest` half.

## Running

```bash
# One IDE
./gradlew :ide-integration-tests:ideaUltimate2026_1_1IdeTest

# Whichever IDE is the default (the last one registered)
./gradlew :ide-integration-tests:defaultIdeTest

# Every registered IDE. Slow: each one is a separate IDE download and launch.
./gradlew :ide-integration-tests:allIdeTests

# What is registered
./gradlew :ide-integration-tests:tasks --group=verification
```

Metro is published into the functional test repository first, by `:gradle-plugin:installForFunctionalTest`.

IDEs are downloaded on demand and cached under `out/ide-tests` at the repository root, so only the
first run of a given IDE pays for it.

## Which IDEs run

**Nothing here** — it's worked out from `gradle.properties`, the `metro.minIdeaVersion` family
(`minIdeaVersion`, `maxIdeaVersion`, `includeIdeaRc`, `includeIdeaEap`), the same properties that
decide which compilers Metro is built against. One IDE per platform baseline per channel, and that
covers Android Studio as well: its releases are recorded against IntelliJ platform builds too, with
Google's `beta` counting as RC and `canary` as EAP. Widening the range widens the IDE matrix with it.

`./gradlew :ide-integration-tests:listIdeTests` prints what that currently works out to.

A build the dev kit hasn't recorded can still be named outright in the `ideTest { }` block —
`intellijIdea("2026.1.1")` for a stable, `intellijIdea("261.27258.27", DevKitIdeBuildType.RC)` for a
prerelease (a platform build number, since prereleases aren't in the release API a marketing version
is looked up in), or `androidStudio("2026.1.1.8", "<installer url>", ...)`.

## Test assertions

`MetroIdeSmokeTest` describes the project the IDE opens the same way a functional test does, and
declares what the IDE should report about it with marker comments in the sources themselves:

```kotlin
// EXPECT_DIAGNOSTIC: DIAGNOSTIC_ID,SEVERITY,description
// EXPECT_INLAY: substring
```

Both apply to the code directly below them. A diagnostic has to match the severity exactly, contain
the description snippet, and be highlighted within a few lines of the marker; an inlay only has to
contain the substring. Any `ERROR` highlight *not* covered by an `EXPECT_DIAGNOSTIC` fails the test,
which is what catches unresolved references to generated code.

The test also fails if the IDE's error log names Metro, or if it contains
`Skipping enabling Metro extensions` — the message Metro logs when it declines to install its FIR
extensions, which would otherwise just look like every expected diagnostic going missing.

## Troubleshooting

**Tests timing out** — an IDE download plus a Gradle import can be slow on the first run. The
per-test timeout is 15 minutes (`pluginDevKit { testTimeout = ... }`).

**404 downloading Android Studio** — the installer file prefix in `build.gradle.kts` is wrong or
stale; check <https://jb.gg/android-studio-releases-list.xml>.

**Metro extensions not loaded** — the test sets `kotlin.k2.only.bundled.compiler.plugins.enabled` to
`false` for the IDE under test; without it the Kotlin plugin ignores third-party compiler plugins.
The test says so explicitly rather than reporting missing diagnostics.
