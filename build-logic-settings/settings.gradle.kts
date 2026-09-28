// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
  }
  versionCatalogs { maybeCreate("libs").apply { from(files("../gradle/libs.versions.toml")) } }
}

rootProject.name = "build-logic-settings"
