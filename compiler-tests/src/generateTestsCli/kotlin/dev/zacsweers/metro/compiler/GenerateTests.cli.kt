// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.compiler.plugin.devkit.SourceSetName
import org.jetbrains.kotlin.compiler.plugin.devkit.sourceSetTestClass
import org.jetbrains.kotlin.generators.dsl.TestGroup

context(_: SourceSetName)
actual fun TestGroup.extraClasses() {
  val nonJvmModel: TestGroup.TestClass.(name: String) -> Unit = { name ->
    model(
      name,
      excludedPattern = null,
      excludeDirsRecursively = listOf("interop", "circuit"),
    )
  }
  sourceSetTestClass<AbstractJsBoxTest> { nonJvmModel("box") }
  sourceSetTestClass<AbstractJsFastInitBoxTest> { nonJvmModel("box") }
  sourceSetTestClass<AbstractJsContributionProvidersBoxTest> { nonJvmModel("box") }
  sourceSetTestClass<AbstractJsDiagnosticTest> { nonJvmModel("diagnostic") }
}
