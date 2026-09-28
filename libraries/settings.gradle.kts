// Copyright (C) 2024 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
  includeBuild("../build-logic")
  includeBuild("../build-logic-settings")
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
  }
}

plugins { id("devkit") }

pluginDevKit { createVersionCatalogFrom("..") }

rootProject.name = "libraries"

include(
  ":metrox-viewmodel",
  ":metrox-viewmodel-compose",
  ":runtime-coroutines",
  ":runtime",
)
