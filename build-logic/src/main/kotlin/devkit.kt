// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.compiler.plugin.devkit.BetaAndRc
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitPropertiesDsl
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

// CI sets METRO_CI_SINGLE_VERSION in lanes that test one Kotlin version. config() then skips the
// bulk registrations below, and the devkit registers only what the
// `kotlin.compiler.plugin.devkit.*` command-line properties request, or the build's own Kotlin
// when they request nothing.
fun DevKitPropertiesDsl<*>.config() {
  val singleVersion =
    project.providers.environmentVariable("METRO_CI_SINGLE_VERSION").getOrElse("").toBoolean()
  if (!singleVersion) {
    cliVersions("2.3.20", betaAndRc = BetaAndRc.LATEST)
    ideaVersions("261", includeRc = true, includeEap = true)
    useLatestBootstrap()
  }
  pluginPackage.set("dev.zacsweers.metro.compiler")
  companionLibrary("runtime")

  // The DevKit (correctly) identifies the version as 2.4.10 because Circuit uses it
  // But most our tests don't use it, so we set the version to the minimum here
  companionLibrariesKotlinVersion.set(KotlinToolingVersion("2.3.20"))
}
