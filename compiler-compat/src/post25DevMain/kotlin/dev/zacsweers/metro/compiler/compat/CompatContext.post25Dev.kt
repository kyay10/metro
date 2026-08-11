// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.createEmptyExternalPackageFragment as createEmptyExternalPackageFragmentNative
import org.jetbrains.kotlin.name.FqName

@CompatApi(
  since = "2.5.0-dev-498",
  reason = ABI_CHANGE,
  message =
    "createEmptyExternalPackageFragment now takes IrModuleFragment instead of ModuleDescriptor",
)
public actual fun IrModuleFragment.createEmptyExternalPackageFragmentCompat(
  packageName: String
): IrPackageFragment {
  return createEmptyExternalPackageFragmentNative(this, FqName(packageName))
}
