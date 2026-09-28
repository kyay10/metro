// Copyright (C) 2024 Zac Sweers
// SPDX-License-Identifier: Apache-2.0

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.android.kmp) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.poko) apply false
  alias(libs.plugins.wire) apply false
  id("metro.yarnNode")
  id("metro.dokka")
  id("metro.apiValidation")
}

// Autoconfigure git to use project-specific config (hooks)
if (file(".git").exists()) {
  val expectedIncludePath = "../config/git/.gitconfig"
  val includePath =
    providers
      .exec { commandLine("git", "config", "--local", "--default", "", "--get", "include.path") }
      .standardOutput
      .asText
      .map { it.trim() }
      .getOrElse("")
  if (includePath != expectedIncludePath) {
    providers
      .exec { commandLine("git", "config", "--local", "include.path", expectedIncludePath) }
      .result
      .get()
  }
}

apiValidation {
  ignoredProjects += buildList {
    add("compiler")
    add("metro-common")
    add("compiler-tests")
    add("compiler-compat")
  }
}

subprojects {
  apply(plugin = "metro.base")
}

dependencies {
  dokka(project(":gradle-plugin"))
  dokka(project(":interop-dagger"))
  dokka(project(":interop-guice"))
  dokka(project(":interop-jakarta"))
  dokka(project(":interop-javax"))
  dokka(project(":metro-trace"))
  dokka(project(":metrox-android"))
  dokka("$group:metrox-viewmodel:$version")
  dokka("$group:metrox-viewmodel-compose:$version")
  dokka("$group:runtime:$version")
  dokka("$group:runtime-coroutines:$version")
}
