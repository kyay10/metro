// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitTestGroup
import org.jetbrains.kotlin.generators.dsl.TestGroup

actual fun DevKitTestGroup.extraClasses() {
  val nonJvmModel: TestGroup.TestClass.(name: String) -> Unit = { name ->
    model(
      name,
      excludedPattern = null,
      excludeDirsRecursively = listOf("interop", "circuit"),
    )
  }
  testClass<AbstractJsBoxTest> { nonJvmModel("box") }
  testClass<AbstractJsFastInitBoxTest> { nonJvmModel("box") }
  testClass<AbstractJsContributionProvidersBoxTest> { nonJvmModel("box") }
  testClass<AbstractJsDiagnosticTest> { nonJvmModel("diagnostic") }
}
