// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.name.Name

@CompatApi(
  since = "2.4.0",
  reason = ABI_CHANGE,
  message = "2.4 removed the session parameter from FirAnnotation argument helpers",
)
public actual fun FirAnnotation.getBooleanArgumentCompat(
  name: Name,
  session: FirSession,
): Boolean? {
  return getBooleanArgument(name, session)
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
  return getStringArgument(name, session)
}
