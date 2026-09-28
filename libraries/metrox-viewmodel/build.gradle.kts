// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0

plugins {
  alias(libs.plugins.kotlin.multiplatform.published)
  id("metro.base")
  id("metro.publish")
}

metroArtifact {
  artifactId.set("metrox-viewmodel")
  name.set("Metrox ViewModel")
}

metroProject { configureCommonKmpTargets("metrox-viewmodel", requiresAndroidXDeps = true) }

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(project(":runtime"))
        api(libs.jetbrains.lifecycle.viewmodel)
      }
    }
    commonTest { dependencies { implementation(libs.kotlin.test.published) } }
  }
}
