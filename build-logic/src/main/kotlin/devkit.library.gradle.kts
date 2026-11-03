// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
import org.gradle.kotlin.dsl.kotlin

plugins {
  kotlin("compiler.plugin.devkit.compiler-library")
}

pluginDevKit { config() }
