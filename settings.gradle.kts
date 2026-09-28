// Copyright (C) 2024 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
  includeBuild("build-logic")
  includeBuild("build-logic-settings")
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
  }
}

plugins {
  id("devkit")
}

pluginDevKit {
  includeBuildWithChecks("libraries")
  companionLibrary("runtime")
}

rootProject.name = "metro"

include(
  ":compiler",
  ":compiler-compat",
  ":compiler-tests",
  ":gradle-plugin",
  ":interop-dagger",
  ":interop-javax",
  ":interop-jakarta",
  ":interop-guice",
  ":metro-trace",
  ":metro-common",
  ":metrox-android",
)

includeBuild(".")
