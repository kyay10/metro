// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.kotlin.multiplatform.published) apply false
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

tasks.installForFunctionalTest {
  dependsOn(childProjects.keys.map { ":$it:installForFunctionalTest" })
}
