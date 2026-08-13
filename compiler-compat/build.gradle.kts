import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  pluginDevKit("compiler-plugin")
  id("metro.publish")
}

metroArtifact {
  artifactId.set("compiler-compat")
  name.set("Metro Compiler Compat")
}

buildConfig {
  packageName("dev.zacsweers.metro.compiler.compat")
  kotlin {
    useKotlinOutput {
      internalVisibility = true
      topLevelConstants = true
    }
  }
  buildConfigField(
    "kotlin.collections.Map<String, String>",
    "BUILT_IN_COMPILER_VERSION_ALIASES",
    providers
      .fileContents(layout.projectDirectory.file("ide-mappings.txt"))
      .asText
      // Known tags-to-real version mappings for IDE builds.
      // Android Studio canary builds report a fake version like "2.3.255-dev-255".
      // The real version can be found by checking the IntelliJ tag for the studio build number:
      // https://github.com/JetBrains/intellij-community/blob/idea/<intellij-version>/.idea/libraries/kotlinc_kotlin_compiler_common.xml
      .map { text ->
        text
          .lineSequence()
          .filter { it.isNotBlank() && !it.startsWith("#") }
          .joinToString(prefix = "mapOf(\n", postfix = "\n)", separator = "\n,") { line ->
            val (from, to) = line.split('=', limit = 2)
            "  \"$from\" to \"$to\""
          }
      },
  )
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

val versionAliases =
  isolated.projectDirectory.file("version-aliases.txt").asFile.readLines().filterNot {
    it.isBlank() || it.startsWith('#')
  }

pluginDevKit {
  versionAliases.forEach { testAgainst(it) }
  componentRegistrar.unsetConvention()
  commandLineProcessor.unsetConvention()
}
