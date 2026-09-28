// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.gradle

import com.autonomousapps.kit.RootProject
import org.jetbrains.kotlin.compiler.plugin.devkit.test.AbstractDevKitGradleProject

abstract class MetroProject(
  private val debug: Boolean = false,
  private val metroOptions: MetroOptionOverrides = MetroOptionOverrides(),
  private val reportsEnabled: Boolean = true,
  kotlinVersion: String? = null,
  multiplatform: Boolean = true,
) : AbstractDevKitGradleProject(kotlinVersion, multiplatform) {

  override val defaultImports = listOf("dev.zacsweers.metro.*")

  override val pluginUnderTest = GradlePlugins.metro

  override val extraGradleProperties = super.extraGradleProperties + METRO_TESTKIT_GRADLE_PROPERTIES

  override fun pluginConfigBlock(): String = buildMetroBlock()

  /** Applies the Metro settings; used by fixtures that build custom project structures. */
  protected fun RootProject.Builder.withMetroSettings() = withDevKitSettings()

  /** Generates just the `metro { ... }` block content for use in custom build scripts. */
  fun buildMetroBlock(): String = buildString {
    appendLine(
      "@OptIn(dev.zacsweers.metro.gradle.DelicateMetroGradleApi::class, dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi::class)"
    )
    appendLine("metro {")
    appendLine("  debug.set($debug)")
    if (reportsEnabled) {
      appendLine("  reportsDestination.set(layout.buildDirectory.dir(\"metro\"))")
    }
    val options = buildList {
      metroOptions.enableFullBindingGraphValidation?.let {
        add("compilerOptions.enable(\"enable-full-binding-graph-validation\")")
      }
      metroOptions.generateContributionProviders?.let {
        add("generateContributionProviders.set($it)")
      }
      val omitRedundantMirrors =
        metroOptions.omitRedundantMirrors ?: getTestOmitRedundantMirrorsOverride()
      omitRedundantMirrors?.let {
        val method = if (it) "enable" else "disable"
        add("compilerOptions.$method(\"omit-redundant-mirrors\")")
      }
    }
    if (options.isNotEmpty()) {
      options.joinTo(this, separator = "\n", prefix = "  ")
    }
    appendLine("\n}")
  }

  private companion object {
    /**
     * Extra gradle.properties entries layered onto every generated TestKit project via
     * [RootProject.Builder.gradleProperties]. Order matters: these are appended after the
     * testkit-support defaults, so duplicate keys here take precedence.
     */
    private val METRO_TESTKIT_GRADLE_PROPERTIES =
      listOf(
        "org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError -XX:MaxMetaspaceSize=512m",
        "kotlin.daemon.jvmargs=-Xmx2g",
        "org.gradle.workers.max=4",
      )
  }
}
