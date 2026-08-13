// Copyright (C) 2021 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitCLP
import org.jetbrains.kotlin.config.CompilerConfiguration

public class MetroCommandLineProcessor : DevKitCLP {
  override val pluginOptions: Collection<AbstractCliOption> =
    MetroOption.entries.map { it.raw.cliOption }

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration,
  ) {
    when (val metroOption = MetroOption.entriesByOptionName[option.optionName]) {
      null -> throw CliOptionProcessingException("Unknown plugin option: ${option.optionName}")
      else -> metroOption.raw.put(configuration, value)
    }
  }
}
