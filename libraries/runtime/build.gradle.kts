// Copyright (C) 2024 Zac Sweers
// SPDX-License-Identifier: Apache-2.0

plugins {
  alias(libs.plugins.kotlin.multiplatform.published)
  id("metro.base")
  id("metro.publish")
}

metroArtifact {
  artifactId.set("runtime")
  name.set("Metro Runtime")
}

metroProject { configureCommonKmpTargets("metro-runtime") }

kotlin {
  sourceSets {
    commonTest {
      dependencies {
        implementation(libs.kotlin.test.published)
        implementation(libs.coroutines)
        implementation(libs.coroutines.test)
      }
    }
  }

  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
    optIn.add("kotlin.js.ExperimentalJsStatic")
  }
}
