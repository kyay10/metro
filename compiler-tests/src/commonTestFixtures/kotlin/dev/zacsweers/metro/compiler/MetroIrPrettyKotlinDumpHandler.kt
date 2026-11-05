// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import dev.zacsweers.metro.compiler.ir.metroDumpKotlinLike
import org.jetbrains.kotlin.compiler.plugin.devkit.TestDumpDirectives
import org.jetbrains.kotlin.compiler.plugin.devkit.assertEqualsToDump
import org.jetbrains.kotlin.ir.util.FakeOverridesStrategy
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.handlers.AbstractIrHandler
import org.jetbrains.kotlin.test.backend.handlers.IrTextDumpHandler.Companion.groupWithTestFiles
import org.jetbrains.kotlin.test.backend.ir.IrBackendInput
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.EXTERNAL_FILE
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.SKIP_KT_DUMP
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.ValueDirective
import org.jetbrains.kotlin.test.model.BackendKind
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.defaultsProvider
import org.jetbrains.kotlin.test.services.moduleStructure
import org.jetbrains.kotlin.test.utils.MultiModuleInfoDumper

/**
 * Like [org.jetbrains.kotlin.test.backend.handlers.IrPrettyKotlinDumpHandler] but uses
 * [metroDumpKotlinLike] so that nested class names are fully qualified in the dump output.
 *
 * Triggered by [MetroDirectives.METRO_DUMP_KT_IR] instead of `DUMP_KT_IR` to avoid conflicts with
 * the built-in handler.
 */
class MetroIrPrettyKotlinDumpHandler(
  testServices: TestServices,
  artifactKind: BackendKind<IrBackendInput>,
) : AbstractIrHandler(testServices, artifactKind) {
  companion object {
    const val DUMP_EXTENSION = "kt.txt"
  }

  private val dumper = MultiModuleInfoDumper("// MODULE: %s")

  // CHANGED: Added MetroDirectives
  override val directiveContainers: List<DirectivesContainer>
    get() =
      listOf(TestDumpDirectives, CodegenTestDirectives, FirDiagnosticsDirectives, MetroDirectives)

  override fun processModule(module: TestModule, info: IrBackendInput) {
    // CHANGED: METRO_DUMP_KT_IR instead of DUMP_KT_IR
    if (MetroDirectives.METRO_DUMP_KT_IR !in module.directives || SKIP_KT_DUMP in module.directives)
      return
    dumpModuleKotlinLike(
      module,
      testServices.moduleStructure.modules,
      info,
      dumper,
      KotlinLikeDumpOptions(
        printFilePath = false,
        printFakeOverridesStrategy = FakeOverridesStrategy.NONE,
        // CHANGED: use original names
        normalizeNames = false,
        stableOrder = true,
        // Expect declarations exist in K1 IR just before serialization, but won't be serialized.
        // Though, dumps should be same before and after
        printExpectDeclarations = module.languageVersionSettings.languageVersion.usesK2,
        inferElseBranches = true,
      ),
      testServices,
    )
  }

  override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
    // When target backend is specified in DUMP_IR_DIFFERENCE test directive, the actual KT-like
    // dump is expected to differ from the golden data
    // KT-like dumps are not that very useful, so it's simpler just to skip this handler to not
    // create unnecessary `*.kt.<backend>.patch` files
    if (DUMP_IR_DIFFERENCE?.let(testServices::getMatchedBackendFromDirective) != null) return
    val actualDump = if (dumper.isEmpty()) null else dumper.generateResultingDump()
    assertEqualsToDump(DUMP_EXTENSION, actualDump)
  }
}

internal fun dumpModuleKotlinLike(
  module: TestModule,
  allModules: List<TestModule>,
  info: IrBackendInput,
  multiModuleInfoDumper: MultiModuleInfoDumper,
  options: KotlinLikeDumpOptions,
  testServices: TestServices,
) {
  val irFiles = info.irModuleFragment.files
  val builder = multiModuleInfoDumper.builderForModule(module.name)
  val filteredIrFiles =
    irFiles
      .groupWithTestFiles(testServices, ordered = true)
      .filterNot { (moduleAndFile, _) ->
        moduleAndFile?.second?.let { EXTERNAL_FILE in it.directives || it.isAdditional } ?: false
      }
      .map { it.second }
  val printFileName = filteredIrFiles.size > 1 || allModules.size > 1
  val modifiedOptions = options.copy(printFileName = printFileName)
  for (irFile in filteredIrFiles) {
    // CHANGED: metroDumpKotlinLike instead of dumpKotlinLike
    builder.append(irFile.metroDumpKotlinLike(modifiedOptions))
  }
}

internal fun TestServices.getMatchedBackendFromDirective(
  directive: ValueDirective<TargetBackend>
): TargetBackend? {
  val backendsInDirective = moduleStructure.allDirectives[directive].toSet()
  var current = defaultsProvider.targetBackend ?: return null
  while (current != TargetBackend.ANY) {
    if (current in backendsInDirective) return current
    current = current.compatibleWith
  }
  return null
}

internal expect val DUMP_IR_DIFFERENCE: ValueDirective<TargetBackend>?
