// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.backend.common.extensions.IrGeneratedDeclarationsRegistrar
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty

// TODO use expect/actual instead of source-compatibility tricks like this one
public fun IrGeneratedDeclarationsRegistrar.registerClassAsMetadataVisible(irClass: IrClass) {
  error("registerClassAsMetadataVisible is not supported by this Kotlin compiler version.")
}

public fun IrGeneratedDeclarationsRegistrar.registerPropertyAsMetadataVisible(
  irProperty: IrProperty
) {
  error("registerPropertyAsMetadataVisible is not supported by this Kotlin compiler version.")
}
