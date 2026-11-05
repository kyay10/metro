// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.model.ValueDirective

internal actual val DUMP_IR_DIFFERENCE: ValueDirective<TargetBackend>?
  get() = CodegenTestDirectives.DUMP_IR_DIFFERENCE
