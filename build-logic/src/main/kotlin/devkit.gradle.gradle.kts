// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("compiler.plugin.devkit.gradle-plugin")
}

pluginDevKit {
  config()
  compilerPlugin("compiler")
}
