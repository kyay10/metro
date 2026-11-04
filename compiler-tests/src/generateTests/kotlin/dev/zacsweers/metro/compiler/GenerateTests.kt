// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitTestGroup
import org.jetbrains.kotlin.compiler.plugin.devkit.generateDevKitTestsWithJUnit5
import org.jetbrains.kotlin.generators.dsl.TestGroup

fun main() = generateDevKitTestsWithJUnit5 {
  val commonModel: TestGroup.TestClass.(name: String) -> Unit = { name ->
    model(name, excludedPattern = null)
  }
  testClass<AbstractBoxTest> { commonModel("box") }
  testClass<AbstractFastInitBoxTest> { commonModel("box") }
  testClass<AbstractContributionProvidersBoxTest> { commonModel("box") }
  testClass<AbstractIrOnlyClassesBoxTest> { commonModel("box") }
  testClass<AbstractOmitRedundantMirrorsIrOnlyClassesBoxTest> { commonModel("box") }
  testClass<AbstractDiagnosticTest> { commonModel("diagnostic") }
  testClass<AbstractFirDumpTest> { commonModel("dump/fir") }
  testClass<AbstractIrDumpTest> { commonModel("dump/ir") }
  testClass<AbstractReportsTest> { commonModel("dump/reports") }
  extraClasses()
}

expect fun DevKitTestGroup.extraClasses()
