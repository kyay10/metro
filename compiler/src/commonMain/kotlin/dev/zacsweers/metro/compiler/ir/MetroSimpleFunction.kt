// Copyright (C) 2024 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.ir

import dev.drewhamilton.poko.Poko
import dev.zacsweers.metro.compiler.MetroAnnotations
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.name.CallableId

/** Simple holder with resolved annotations to save us lookups. */
// TODO
//  cache these in a transformer context?
//  make this MetroCallable to support properties
@Poko
internal class MetroSimpleFunction(
  @Poko.Skip val ir: IrSimpleFunction,
  val annotations: MetroAnnotations<MetroIrAnnotation>,
  val callableId: CallableId = ir.callableId,
) : Comparable<MetroSimpleFunction> {
  override fun toString() = callableId.toString()

  override fun compareTo(other: MetroSimpleFunction): Int =
    callableId.toString().compareTo(other.callableId.toString())
}

fun DescriptorVisibility.isVisibleOutside() =
  this != DescriptorVisibilities.PRIVATE &&
    this != DescriptorVisibilities.PRIVATE_TO_THIS &&
    this != DescriptorVisibilities.INVISIBLE_FAKE

internal val MetroSimpleFunction.isAccessorCandidate: Boolean
  get() {
    return ir.visibility.isVisibleOutside() &&
      ir.regularParameters.isEmpty() &&
      !annotations.isBinds &&
      !annotations.isProvides &&
      !annotations.isMultibinds
  }

context(context: IrMetroContext)
internal fun metroFunctionOf(
  ir: IrSimpleFunction,
  annotations: MetroAnnotations<MetroIrAnnotation> = metroAnnotationsOf(ir),
): MetroSimpleFunction {
  return MetroSimpleFunction(ir, annotations)
}
