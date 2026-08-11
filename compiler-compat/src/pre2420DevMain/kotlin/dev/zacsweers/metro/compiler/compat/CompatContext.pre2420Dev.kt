// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions

@CompatApi(
  since = "2.4.20-dev-3583",
  reason = ABI_CHANGE,
  message = "2.4.20-dev-3583 split PluginGenerated into nested source element kinds",
)
public actual val pluginGeneratedSourceElementKind: KtFakeSourceElementKind
  get() = KtFakeSourceElementKind.PluginGenerated

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
