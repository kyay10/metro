// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.RegisteredDirectivesBuilder
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.configuration.setupJvmPipelineSteps as realSetupJvmPipelineSteps

actual fun RegisteredDirectivesBuilder.firIdenticalCompat() = Unit

actual fun TestConfigurationBuilder.setupJvmPipelineSteps(parser: FirParser) =
  realSetupJvmPipelineSteps(parser)
