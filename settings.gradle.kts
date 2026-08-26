// Copyright (C) 2024 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
  includeBuild("build-logic")
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
    mavenLocal()
  }
  plugins { id("com.gradle.develocity") version "4.5.0" }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
    mavenLocal()
  }
}

plugins {
  kotlin("compiler.plugin.devkit") version "0.0.3-dev-82c85b5"
  id("com.gradle.develocity")
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
  ":metrox-viewmodel",
  ":metrox-viewmodel-compose",
  ":runtime",
  ":runtime-coroutines",
)

val VERSION_NAME: String by extra.properties

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"

    tag(if (System.getenv("CI").isNullOrBlank()) "Local" else "CI")
    tag(VERSION_NAME)

    obfuscation {
      username { "Redacted" }
      hostname { "Redacted" }
      ipAddresses { addresses -> addresses.map { "0.0.0.0" } }
    }
  }
}

enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")
