// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitTestGenerator
import org.jetbrains.kotlin.compiler.plugin.devkit.SourceSetName
import org.jetbrains.kotlin.compiler.plugin.devkit.sourceSetTestClass
import org.jetbrains.kotlin.generators.dsl.TestGroup

fun main(args: Array<String>) =
  DevKitTestGenerator.generate(args) {
    val commonModel: TestGroup.TestClass.(name: String) -> Unit = { name ->
      model(name, excludedPattern = null)
    }
    sourceSetTestClass<AbstractBoxTest> { commonModel("box") }
    sourceSetTestClass<AbstractFastInitBoxTest> { commonModel("box") }
    sourceSetTestClass<AbstractContributionProvidersBoxTest> { commonModel("box") }
    sourceSetTestClass<AbstractIrOnlyClassesBoxTest> { commonModel("box") }
    sourceSetTestClass<AbstractOmitRedundantMirrorsIrOnlyClassesBoxTest> { commonModel("box") }
    sourceSetTestClass<AbstractDiagnosticTest> { commonModel("diagnostic") }
    sourceSetTestClass<AbstractFirDumpTest> { commonModel("dump/fir") }
    sourceSetTestClass<AbstractIrDumpTest> { commonModel("dump/ir") }
    sourceSetTestClass<AbstractReportsTest> { commonModel("dump/reports") }
  }

context(_: SourceSetName)
expect fun TestGroup.extraClasses()
