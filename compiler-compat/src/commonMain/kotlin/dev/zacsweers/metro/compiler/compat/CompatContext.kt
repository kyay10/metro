// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirEvaluatorResult
import org.jetbrains.kotlin.fir.FirEvaluatorResult.CompileTimeException
import org.jetbrains.kotlin.fir.FirEvaluatorResult.Evaluated
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

private const val COMPILER_VERSION_FILE = "META-INF/compiler.version"

public fun loadCompilerVersionOrNull(): KotlinToolingVersion? {
  return loadCompilerVersionStringOrNull()?.let(::KotlinToolingVersion)
}

public fun loadCompilerVersionStringOrNull(): String? {
  val inputStream =
    FirExtensionRegistrar::class.java.classLoader?.getResourceAsStream(COMPILER_VERSION_FILE)
      ?: return null
  return inputStream.bufferedReader().use { it.readText() }.takeUnless { it.isBlank() }
}

@CompatApi(
  since = "2.4.0",
  reason = CompatApi.Reason.COMPAT,
  message = "2.4.0 invalidates incremental compilation when annotation arguments change",
)
public expect val supportsAnnotationArgumentInvalidation: Boolean

@CompatApi(
  since = "2.4.20-dev-6138",
  reason = CompatApi.Reason.COMPAT,
  message = "2.4.20-dev-6138 supports Metro's metadata-visible IR-generated classes",
)
public expect val supportsIrGeneratedClasses: Boolean

@CompatApi(
  since = "2.4.0",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "2.4 changed IrAnnotationContainer.annotations from IrConstructorCall to IrAnnotation",
)
public val IrAnnotationContainer.annotationsCompat: List<IrAnnotation>
  get() {
    val annotations: List<IrConstructorCall> = annotations
    @Suppress("UNCHECKED_CAST")
    return annotations as List<IrAnnotation>
  }

@CompatApi(
  since = "2.4.0",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "2.4 removed the session parameter from FirAnnotation argument helpers",
)
public expect fun FirAnnotation.getBooleanArgumentCompat(name: Name, session: FirSession): Boolean?

@CompatApi(
  since = "2.4.0",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "2.4 removed the session parameter from FirAnnotation argument helpers",
)
public expect fun FirAnnotation.getStringArgumentCompat(name: Name, session: FirSession): String?

/**
 * Version-safe access to Kotlin's plugin-generated fake source kind. Kotlin 2.4.20 split
 * `PluginGenerated` into nested variants such as `PluginGenerated.Default`, and direct constant
 * references can be inlined into Metro code that runs on older compilers.
 */
@CompatApi(
  since = "2.4.20-dev-3583",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "2.4.20-dev-3583 split PluginGenerated into nested source element kinds",
)
public expect val pluginGeneratedSourceElementKind: KtFakeSourceElementKind

@CompatApi(
  since = "2.4.20-dev-3583",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "2.4.20-dev-3583 upstreamed custom Kotlin-like IR name rendering",
)
public expect fun IrElement.dumpKotlinLikeCompat(
  options: KotlinLikeDumpOptions,
  classNameTransformer: (context: IrDeclaration?, declaration: IrDeclarationWithName) -> String,
): String

/**
 * Returns the compiler's configured [MessageCollector], or a non-silent fallback if no collector
 * was installed. Metro still needs a message sink before an IR/FIR diagnostic reporter exists, such
 * as while validating plugin options or reporting registrar-level debug output.
 */
@CompatApi(
  since = "2.4.20",
  reason = CompatApi.Reason.COMPAT,
  message = "MessageCollector access is being phased out in favor of diagnostic reporters",
)
public fun CompilerConfiguration.messageCollectorCompat(): MessageCollector {
  // Do not fall back to PrintingMessageCollector here. It (and MessageRenderer) are CLI-only
  // classes that IDE kotlinc distributions don't ship, so referencing them throws
  // NoClassDefFoundError when the IDE's KtCompilerPluginsCache loads Metro's registrar and
  // no collector is configured (the IDE never configures one).
  return get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY) ?: SystemErrMessageCollector()
}

/** A non-silent fallback collector that avoids CLI-only printing classes. */
private class SystemErrMessageCollector : MessageCollector {
  private var hasErrors = false

  override fun clear() {
    hasErrors = false
  }

  override fun report(
    severity: CompilerMessageSeverity,
    message: String,
    location: CompilerMessageSourceLocation?,
  ) {
    if (severity.isError) {
      hasErrors = true
    }
    val renderedLocation = location?.let { " ($it)" }.orEmpty()
    System.err.println("${severity.presentableName}: $message$renderedLocation")
  }

  override fun hasErrors(): Boolean = hasErrors
}

/** Builds a fully qualified resolved qualifier for [classSymbol]. */
@CompatApi(
  since = "2.4.20-Beta2",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "FirResolvedQualifier.symbol was renamed and isFullyQualified was removed",
)
public expect fun buildResolvedQualifierCompat(
  classId: ClassId,
  classSymbol: FirClassLikeSymbol<*>,
  classType: ConeKotlinType,
): FirResolvedQualifier

/** Creates an empty external package fragment using this module. */
@CompatApi(
  since = "2.5.0-dev-498",
  reason = CompatApi.Reason.ABI_CHANGE,
  message =
    "createEmptyExternalPackageFragment now takes IrModuleFragment instead of ModuleDescriptor",
)
public expect fun IrModuleFragment.createEmptyExternalPackageFragmentCompat(
  packageName: String
): IrPackageFragment

@CompatApi(
  since = "2.4.20-Beta2",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "IrAnnotation arguments moved from getValueArgument(Name) to argumentMapping",
)
public expect fun IrAnnotation.getAnnotationArgument(name: Name): IrExpression?

internal annotation class CompatApi(
  val since: String,
  val reason: Reason,
  val message: String = "",
) {
  enum class Reason {
    DELETED,
    RENAMED,
    ABI_CHANGE,
    COMPAT,
  }
}

public inline fun <reified T : FirElement> FirEvaluatorResult.unwrapOr(
  action: (CompileTimeException) -> Unit
): T? =
  when (this) {
    is CompileTimeException -> {
      action(this)
      null
    }
    is Evaluated -> this.result as? T
    else -> null
  }
