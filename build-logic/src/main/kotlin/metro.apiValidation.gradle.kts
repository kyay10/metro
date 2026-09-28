// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
import kotlinx.validation.ExperimentalBCVApi

plugins {
  id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apiValidation {
  ignoredPackages += metroApiIgnoredPackages
  nonPublicMarkers += metroApiNonPublicMarkers
  @OptIn(ExperimentalBCVApi::class)
  klib {
    // This is only really possible to run on macOS
    // strictValidation = true
    enabled = true
  }
}
