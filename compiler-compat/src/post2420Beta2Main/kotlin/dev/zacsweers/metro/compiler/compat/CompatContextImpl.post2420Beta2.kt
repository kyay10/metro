// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.builder.buildResolvedQualifier
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

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
    qualifierSymbol = classSymbol
    resolvedToCompanionObject = false
    explicitParent = buildResolvedQualifier {
      packageFqName = classId.packageFqName
      resolvedToCompanionObject = false
    }
    coneTypeOrNull = classType
  }
}

@CompatApi(
  since = "2.4.20-Beta2",
  reason = CompatApi.Reason.ABI_CHANGE,
  message = "IrAnnotation arguments moved from getValueArgument(Name) to argumentMapping",
)
public actual fun IrAnnotation.getAnnotationArgument(name: Name): IrExpression? =
  argumentMapping[name]
