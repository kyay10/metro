// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.test

import org.jetbrains.kotlin.compiler.plugin.devkit.services.TEST_COMPILER_VERSION

val JVM_TARGET: String by lazy { System.getProperty("metro.test.jvmTarget") }

val COMPILER_VERSION: KotlinVersion by lazy {
  val toolingVersion = TEST_COMPILER_VERSION
  KotlinVersion(toolingVersion.major, toolingVersion.minor, toolingVersion.patch)
}
