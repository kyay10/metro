// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.gradle

fun getTestOmitRedundantMirrorsOverride(): Boolean? =
  System.getProperty("metro.testOmitRedundantMirrors")?.toBooleanStrict()

fun getTestCircuitVersion(): String = System.getProperty("metro.circuitVersion")
