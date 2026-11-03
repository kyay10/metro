// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
  }
  versionCatalogs { maybeCreate("libs").apply { from(files("../gradle/libs.versions.toml")) } }
}

rootProject.name = "build-logic"

enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")
