// Copyright (C) 2021 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import dev.zacsweers.metro.compiler.MetroCompilerPluginRegistrar.Companion.isIde
import dev.zacsweers.metro.compiler.circuit.CircuitIrDeclarationGenerationExtension
import dev.zacsweers.metro.compiler.circuit.CircuitIrExtension
import dev.zacsweers.metro.compiler.compat.loadCompilerVersionOrNull
import dev.zacsweers.metro.compiler.compat.messageCollectorCompat
import dev.zacsweers.metro.compiler.fir.MetroFirExtensionRegistrar
import dev.zacsweers.metro.compiler.ir.MetroIrGenerationExtension
import dev.zacsweers.metro.compiler.tracing.TraceContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitCompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.KotlinToolingVersion
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker

public class MetroCompilerPluginRegistrar :
  DevKitCompilerPluginRegistrar(MetroComponentRegistrar::class) {
  companion object {
    val isIde by lazy {
      try {
        // Try to look up an IntelliJ-only class
        Class.forName("org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession")
        true
      } catch (_: ClassNotFoundException) {
        false
      }
    }
  }

  public override val pluginId: String = PLUGIN_ID

  override val supportsK2: Boolean
    get() = true
}

public class MetroComponentRegistrar : DevKitComponentRegistrar {
  override fun CompilerPluginRegistrar.ExtensionStorage.registerExtensions(
    configuration: CompilerConfiguration
  ) {
    val options = MetroOptions.load(configuration)

    if (!options.enabled) return

    val version =
      options.compilerVersion?.let(::KotlinToolingVersion) ?: loadCompilerVersionOrNull()

    val enableFir = version != null || (isIde && options.forceEnableFirInIde)

    if (!enableFir) {
      // While the option is about FIR, this really also means we can't/don't enable IR
      System.err.println(
        "[METRO] Skipping enabling Metro extensions. Detected Kotlin version: $version"
      )
      return
    }

    val classIds = ClassIds.fromOptions(options)

    val realMessageCollector = configuration.messageCollectorCompat()
    val messageCollector =
      if (options.debug) {
        DebugMessageCollector(realMessageCollector)
      } else {
        realMessageCollector
      }

    if (options.debug) {
      messageCollector.report(
        CompilerMessageSeverity.INFO,
        "Metro mode: ${if (isIde) "IDE" else "CLI"}",
      )
      messageCollector.report(CompilerMessageSeverity.INFO, "Metro options:\n$options")
    }

    if (options.maxIrErrorsCount < 1) {
      messageCollector.report(
        CompilerMessageSeverity.ERROR,
        "maxIrErrorsCount must be greater than zero but was ${options.maxIrErrorsCount}",
      )
      return
    }

    if (options.keysPerGraphShard < 1) {
      messageCollector.report(
        CompilerMessageSeverity.ERROR,
        "keysPerGraphShard must be greater than zero but was ${options.keysPerGraphShard}",
      )
      return
    }

    if (options.parallelThreads < 0) {
      messageCollector.report(
        CompilerMessageSeverity.ERROR,
        "parallelMetroThreads must be non-negative but was ${options.parallelThreads}",
      )
      return
    }

    // When the parallel pool isn't engaged, drop memoize() down to LazyThreadSafetyMode.NONE
    memoizeThreadSafetyMode =
      if (options.parallelThreads > 0) {
        LazyThreadSafetyMode.PUBLICATION
      } else {
        LazyThreadSafetyMode.NONE
      }

    if (version != null) {
      val valid =
        options.validate(version, configuration) { error ->
          messageCollector.report(CompilerMessageSeverity.ERROR, error)
        }
      if (!valid) return
    }

    val traceContext = TraceContext(options)

    FirExtensionRegistrarAdapter.registerExtension(
      MetroFirExtensionRegistrar(classIds, options, isIde, traceContext)
    )

    if (!isIde) {
      val lookupTracker = configuration[CommonConfigurationKeys.LOOKUP_TRACKER]
      val expectActualTracker: ExpectActualTracker =
        configuration[CommonConfigurationKeys.EXPECT_ACTUAL_TRACKER, ExpectActualTracker.DoNothing]
      if (options.enableCircuitCodegen) {
        if (options.generateClassesInIr) {
          IrGenerationExtension.registerExtension(
            CircuitIrDeclarationGenerationExtension.create(classIds = classIds)
          )
        }
        // Register Circuit's body transformer before Metro's main IR pipeline.
        IrGenerationExtension.registerExtension(
          CircuitIrExtension(
            generateClassesInIr = options.generateClassesInIr,
            function0Types = classIds.function0Types,
            assistedFactoryAnnotations = classIds.assistedFactoryAnnotations,
            injectAnnotations = classIds.allInjectAnnotations,
            qualifierAnnotations = classIds.qualifierAnnotations,
          )
        )
      }
      IrGenerationExtension.registerExtension(
        MetroIrGenerationExtension(
          messageCollector = messageCollector,
          classIds = classIds,
          options = options,
          lookupTracker = lookupTracker,
          expectActualTracker = expectActualTracker,
          traceContext = traceContext,
        )
      )
    }
  }
}

private class DebugMessageCollector(private val delegate: MessageCollector) : MessageCollector {
  override fun clear() {
    delegate.clear()
  }

  override fun report(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageSourceLocation?,
  ) {
    // Render manually rather than with MessageRenderer, which is a CLI-only class that IDE
    // kotlinc distributions don't ship.
    val renderedLocation = location?.let { " ($it)" }.orEmpty()
    val message = "${severity.presentableName}: $message$renderedLocation"
    if (severity.isError) {
      System.err.println(message)
    } else {
      println(message)
    }
    delegate.report(severity, message, location)
  }

  override fun hasErrors(): Boolean {
    return delegate.hasErrors()
  }
}
