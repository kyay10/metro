// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.compiler.plugin.devkit.SourceSetName
import org.jetbrains.kotlin.generators.dsl.TestGroup

context(_: SourceSetName)
actual fun TestGroup.extraClasses() {}
