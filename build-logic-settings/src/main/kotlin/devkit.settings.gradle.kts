// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.compiler.plugin.devkit.BetaAndRc

plugins {
  kotlin("compiler.plugin.devkit")
  id("com.gradle.develocity")
}

pluginDevKit {
  cliVersions("2.3.20", betaAndRc = BetaAndRc.LATEST)
  ideaVersions("261", includeRc = true, includeEap = true)
  useLatestDev()
  pluginPackage("dev.zacsweers.metro.compiler")
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
  }
}

val VERSION_NAME: String by extra.properties

gradle.lifecycle.beforeProject {
  group = project.property("GROUP") as String
  version = project.property("VERSION_NAME") as String
}

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
