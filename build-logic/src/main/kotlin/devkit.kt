// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.compiler.plugin.devkit.BetaAndRc
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitPropertiesDsl

fun DevKitPropertiesDsl<*>.config() {
  cliVersions("2.3.20", betaAndRc = BetaAndRc.LATEST)
  ideaVersions("261", includeRc = true, includeEap = true)
  useLatestBootstrap()
  pluginPackage.set("dev.zacsweers.metro.compiler")
  companionLibrary("runtime")
}
