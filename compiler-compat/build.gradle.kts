import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  pluginDevKit("compiler-library")
  id("metro.publish")
}

metroArtifact {
  artifactId.set("compiler-compat")
  name.set("Metro Compiler Compat")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  compilerOptions {
    optIn.add("org.jetbrains.kotlin.config.MessageCollectorAccess")
  }
  pluginDevKit {
    applyPluginDevKitHierarchyTemplate {
      postDev(2, 4, "post24Dev") {
        post("2.4.0", "post24") {
          postDev(2, 4, 20, "post2420Dev") {
            post("2.4.20-Beta2", "post2420Beta2") {
              postDev(2, 5, "post25Dev")
            }
          }
        }
      }

      preDev(2, 5, "pre25Dev") {
        pre("2.4.20-Beta2", "pre2420Beta2") {
          preDev(2, 4, 20, "pre2420Dev") {
            pre("2.4.0", "pre24") {
              preDev(2, 4, "pre24Dev")
            }
          }
        }
      }
    }
  }
}

// ignore tests provided by devkit
tasks.generateTests { enabled = false }

tasks.withType<Test> { failOnNoDiscoveredTests = false }

// Reports the Kotlin compiler versions this build tests against and which of them the plain
// `defaultTest` tasks use, as the devkit resolved them from the
// `kotlin.compiler.plugin.devkit.*` properties in the root `gradle.properties`.
// `scripts/generate-ci-matrix.sh` (and through it `./metrow` and the compatibility docs) reads them
// back out of here, so the version set only ever lives in one place.
tasks.register("compilerVersions") {
  group = HelpTasksPlugin.HELP_GROUP
  description = "Writes the Kotlin compiler versions this build tests against, oldest first."

  val targets = pluginDevKit.testAgainst.sortedBy { it.version }
  val versions = targets.map { "${it.version}" }
  // Ide version. The devkit gradle plugin refuses to register these as test targets at all,
  // so we group them here and skip them in functionalTest
  val ideOnlyVersions = targets.filter { it.forIde }.map { "${it.version}" }
  val defaultVersion = pluginDevKit.defaultTestTarget.map { "${it.version}" }
  val versionsFile = layout.buildDirectory.file("ci/tested-compiler-versions.txt")
  val ideOnlyVersionsFile = layout.buildDirectory.file("ci/ide-only-compiler-versions.txt")
  val defaultVersionFile = layout.buildDirectory.file("ci/default-compiler-version.txt")
  inputs.property("versions", versions)
  inputs.property("ideOnlyVersions", ideOnlyVersions)
  inputs.property("defaultVersion", defaultVersion)
  outputs.files(versionsFile, ideOnlyVersionsFile, defaultVersionFile)
  doLast {
    val defaultVersion = defaultVersion.get()
    logger.lifecycle(
      versions.joinToString("\n") {
        when {
          it == defaultVersion -> "$it (default)"
          it in ideOnlyVersions -> "$it (IDE only)"
          else -> it
        }
      }
    )
    versionsFile.get().asFile.apply {
      parentFile.mkdirs()
      writeText(versions.joinToString("\n", postfix = "\n"))
    }
    ideOnlyVersionsFile
      .get()
      .asFile
      .writeText(ideOnlyVersions.joinToString("\n").let { if (it.isEmpty()) it else "$it\n" })
    defaultVersionFile.get().asFile.writeText("$defaultVersion\n")
  }
}
