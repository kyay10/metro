// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
plugins {
  pluginDevKit("gradle-plugin")
  id("metro.base")
}

// Nothing to do with the IDE tests, which the devkit pins to 21 by itself: this module inherits a
// functionalTest source set that resolves :gradle-plugin and the devkit's Gradle-side testFixtures,
// neither of which is consumable from Metro's default target of 11.
metroProject { jvmTarget.set("21") }

pluginDevKit {
  functionalTestProject(project(":gradle-plugin"))

  ideTest {
    gradlePluginId = "dev.zacsweers.metro"
    errorPatterns.addAll("metro", "dev.zacsweers.metro")
    // Metro logs this instead of failing when it can't work out which compiler it is running in,
    // which would otherwise show up as every expected diagnostic simply being absent.
    forbiddenLogPatterns.add("Skipping enabling Metro extensions")
  }
}
