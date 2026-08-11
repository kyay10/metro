// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.util.CustomKotlinLikeDumpStrategy
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions
import org.jetbrains.kotlin.ir.util.dumpKotlinLike

@CompatApi(
  since = "2.4.20-dev-6138",
  reason = CompatApi.Reason.COMPAT,
  message = "2.4.20-dev-6138 supports Metro's metadata-visible IR-generated classes",
)
public actual val supportsIrGeneratedClasses: Boolean = true

@CompatApi(
  since = "2.4.20-dev-3583",
  reason = ABI_CHANGE,
  message = "2.4.20-dev-3583 split PluginGenerated into nested source element kinds",
)
public actual val pluginGeneratedSourceElementKind: KtFakeSourceElementKind
  get() = KtFakeSourceElementKind.PluginGenerated.Default

@CompatApi(
  since = "2.4.20-dev-3583",
  reason = ABI_CHANGE,
  message = "2.4.20-dev-3583 upstreamed custom Kotlin-like IR name rendering",
)
public actual fun IrElement.dumpKotlinLikeCompat(
  options: KotlinLikeDumpOptions,
  classNameTransformer: (context: IrDeclaration?, declaration: IrDeclarationWithName) -> String,
): String {
  val customDumpStrategy = options.customDumpStrategy
  return dumpKotlinLike(
    options =
      options.copy(
        customDumpStrategy =
          object : CustomKotlinLikeDumpStrategy by customDumpStrategy {
            override fun nameOf(
              container: IrDeclaration?,
              declaration: IrDeclarationWithName,
            ): String {
              return classNameTransformer(container, declaration)
            }
          }
      )
  )
}
