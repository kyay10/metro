// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.builder.buildResolvedQualifier
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.createEmptyExternalPackageFragment as createEmptyExternalPackageFragmentNative
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

// TODO hacky: keep jvm separate for now
@CompatApi(
  since = "2.4.0",
  reason = ABI_CHANGE,
  message = "2.4 removed the session parameter from FirAnnotation argument helpers",
)
public actual fun FirAnnotation.getBooleanArgumentCompat(
  name: Name,
  session: FirSession,
): Boolean? {
  return getBooleanArgument(name)
}

@CompatApi(
  since = "2.4.0",
  reason = ABI_CHANGE,
  message = "2.4 removed the session parameter from FirAnnotation argument helpers",
)
public actual fun FirAnnotation.getStringArgumentCompat(
  name: Name,
  session: FirSession,
): String? {
  return getStringArgument(name)
}

@CompatApi(
  since = "2.4.0",
  reason = CompatApi.Reason.COMPAT,
  message = "2.4.0 invalidates incremental compilation when annotation arguments change",
)
public actual val supportsAnnotationArgumentInvalidation: Boolean
  get() = true

@CompatApi(
  since = "2.4.20-dev-3583",
  reason = ABI_CHANGE,
  message = "2.4.20-dev-3583 split PluginGenerated into nested source element kinds",
)
public actual val pluginGeneratedSourceElementKind: KtFakeSourceElementKind
  get() = KtFakeSourceElementKind.PluginGenerated

@CompatApi(
  since = "2.4.20-Beta2",
  reason = ABI_CHANGE,
  message = "FirResolvedQualifier.symbol was renamed and isFullyQualified was removed",
)
public actual fun buildResolvedQualifierCompat(
  classId: ClassId,
  classSymbol: FirClassLikeSymbol<*>,
  classType: ConeKotlinType,
): FirResolvedQualifier {
  return buildResolvedQualifier {
    packageFqName = classId.packageFqName
    relativeClassFqName = classId.relativeClassName
    symbol = classSymbol
    resolvedToCompanionObject = false
    isFullyQualified = true
    coneTypeOrNull = classType
  }
}

@CompatApi(
  since = "2.5.0-dev-498",
  reason = ABI_CHANGE,
  message =
    "createEmptyExternalPackageFragment now takes IrModuleFragment instead of ModuleDescriptor",
)
public actual fun IrModuleFragment.createEmptyExternalPackageFragmentCompat(
  packageName: String
): IrPackageFragment {
  return createEmptyExternalPackageFragmentNative(descriptor, FqName(packageName))
}

@CompatApi(
  since = "2.4.20-dev-3583",
  reason = ABI_CHANGE,
  message = "2.4.20-dev-3583 upstreamed custom Kotlin-like IR name rendering",
)
public actual fun IrElement.dumpKotlinLikeCompat(
  options: KotlinLikeDumpOptions,
  classNameTransformer: (context: IrDeclaration?, declaration: IrDeclarationWithName) -> String,
): String {
  return betterDumpKotlinLike(options, classNameTransformer)
}

@CompatApi(
  since = "2.4.20-dev-6138",
  reason = CompatApi.Reason.COMPAT,
  message = "2.4.20-dev-6138 supports Metro's metadata-visible IR-generated classes",
)
public actual val supportsIrGeneratedClasses: Boolean
  get() = false

@CompatApi(
  since = "2.4.20-Beta2",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "IrAnnotation arguments moved from getValueArgument(Name) to argumentMapping",
)
public actual fun IrAnnotation.getAnnotationArgument(name: Name): IrExpression? =
  getValueArgument(name)
