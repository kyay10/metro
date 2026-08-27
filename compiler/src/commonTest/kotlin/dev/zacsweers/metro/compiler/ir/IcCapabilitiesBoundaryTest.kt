// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.ir

import com.google.common.truth.Truth.assertThat
import dev.zacsweers.metro.compiler.compat.loadCompilerVersionOrNull
import dev.zacsweers.metro.compiler.compat.supportsAnnotationArgumentInvalidation
import dev.zacsweers.metro.compiler.compat.supportsIrGeneratedClasses
import org.jetbrains.kotlin.compiler.plugin.devkit.KotlinToolingVersion
import org.junit.Test

class IcCapabilitiesBoundaryTest {
  @Test
  fun `compat capabilities match their compiler boundaries`() {
    val compilerVersion = loadCompilerVersionOrNull()!!

    assertThat(supportsAnnotationArgumentInvalidation)
      .isEqualTo(compilerVersion >= KotlinToolingVersion("2.4.0"))
    assertThat(supportsIrGeneratedClasses)
      .isEqualTo(compilerVersion > KotlinToolingVersion("2.4.19"))
  }
}
