// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.ir

import com.google.common.truth.Truth.assertThat
import dev.zacsweers.metro.compiler.compat.CompatContext
import org.jetbrains.kotlin.compiler.plugin.devkit.KotlinToolingVersion
import org.junit.Test

class IcCapabilitiesBoundaryTest {
  @Test
  fun `compat capabilities match their compiler boundaries`() {
    val compilerVersion = CompatContext.Factory.loadCompilerVersionOrNull()!!
    val context = CompatContext.create()

    assertThat(context.supportsAutomaticDeclarationFinderTracking)
      .isEqualTo(compilerVersion >= KotlinToolingVersion("2.3.20"))
    assertThat(context.supportsAnnotationArgumentInvalidation)
      .isEqualTo(compilerVersion >= KotlinToolingVersion("2.4.0"))
    assertThat(context.supportsIrGeneratedClasses)
      .isEqualTo(compilerVersion >= KotlinToolingVersion("2.4.20-dev-6138"))
  }
}
