// Copyright (C) 2024 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import dev.drewhamilton.poko.gradle.PokoFirIdeMode
import dev.zacsweers.metro.gradle.RequiresIdeSupport

// Bootstrap: add the Metro compiler plugin JAR to the buildscript classpath from Maven Central.
// Buildscript resolution is NOT subject to project-level composite build dependency substitution,
// which avoids the circular task dependency (compileKotlin → shadowJar → compileKotlin) that
// occurs when Gradle substitutes dev.zacsweers.metro:compiler with project(:compiler).
buildscript {
  repositories { mavenCentral() }
  val bootstrapVersion =
    extra.properties["METRO_BOOTSTRAP_VERSION"]?.toString()
      ?: error("METRO_BOOTSTRAP_VERSION not set in gradle.properties")
  dependencies {
    classpath("dev.zacsweers.metro:compiler:$bootstrapVersion") { isTransitive = false }
  }
}

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.plugin.serialization)
  alias(libs.plugins.poko)
  pluginDevKit("compiler-plugin")
  alias(libs.plugins.wire)
  alias(libs.plugins.shadow)
  id("metro.publish")
  // apply false to put metro on the classpath. Conditionally applied below.
  alias(libs.plugins.metro)
}

tasks.generateTests { enabled = false }

metroArtifact {
  artifactId.set("compiler")
  name.set("Metro Compiler")
}

metro {
  @OptIn(RequiresIdeSupport::class) generateAssistedFactories.set(true)
  // We embed and shade the runtime in the compiler's shadow JAR
  automaticallyAddRuntimeDependencies.set(false)
}

poko {
  firIdeMode.set(PokoFirIdeMode.NONE)
}

// Extract the bootstrap compiler JAR from the buildscript classpath
val bootstrapVersion = extra.properties["METRO_BOOTSTRAP_VERSION"]?.toString()!!
val bootstrapJar =
  buildscript.configurations.getByName("classpath").files.single {
    it.name == "compiler-$bootstrapVersion.jar"
  }

configurations
  .matching { it.name.startsWith("kotlinCompilerPluginClasspath") }
  .configureEach {
    exclude(group = "dev.zacsweers.metro", module = "compiler")
    dependencies.add(project.dependencies.create(files(bootstrapJar)))
  }

buildConfig {
  kotlin { useKotlinOutput { topLevelConstants = true } }
  sourceSets.named("main") {
    buildConfigField(
      "String",
      "METRO_VERSION",
      providers.gradleProperty("VERSION_NAME").map { "\"$it\"" },
    )
    buildConfigField("String", "PLUGIN_ID", libs.versions.pluginId.map { "\"$it\"" })
    // Metadata version written into Metro's custom metadata.
    buildConfigField("Int", "METADATA_VERSION", 1)
  }
  sourceSets.named("test") {
    buildConfigField("String", "JVM_TARGET", libs.versions.jvmTarget.map { "\"$it\"" })
  }
}

tasks.withType<Test> {
  maxParallelForks = Runtime.getRuntime().availableProcessors() * 2
  systemProperty("metro.buildDir", project.layout.buildDirectory.asFile.get().absolutePath)
  systemProperty("metro.diagnosticsRenderMode", "PLAIN")
  providers.gradleProperty("metro.testOmitRedundantMirrors").orNull?.let {
    systemProperty("metro.testOmitRedundantMirrors", it)
  }
}

val diagnosticsDocsFile = rootProject.layout.projectDirectory.file("docs/diagnostics.md")

// The compiler module's stdlib and kotlin-compiler are compileOnly (kotlinc provides them at
// runtime), so the doc generator needs them added back for plain JavaExec. kotlin-compiler is
// needed because MetroDiagnosticId entries reference their KtDiagnosticFactory transport.
val diagnosticsDocsRuntime =
  configurations.create("diagnosticsDocsRuntime") {
    isCanBeConsumed = false
  }

dependencies {
  diagnosticsDocsRuntime(libs.kotlin.stdlib)
  diagnosticsDocsRuntime(libs.kotlin.compiler)
}

val mainRuntimeClasspath =
  configurations.getByName(
    pluginDevKit.testAgainst.first().mainCompilation.runtimeDependencyConfigurationName
  )

val generateDiagnosticsDocs =
  tasks.register<JavaExec>("generateDiagnosticsDocs") {
    group = "documentation"
    description = "Generates docs/diagnostics.md from the MetroErrorCode registry."
    classpath = mainRuntimeClasspath + diagnosticsDocsRuntime
    mainClass.set("dev.zacsweers.metro.compiler.diagnostics.DiagnosticsDocGenerator")
    args(diagnosticsDocsFile.asFile.absolutePath)
  }

val checkDiagnosticsDocs =
  tasks.register<JavaExec>("checkDiagnosticsDocs") {
    group = "verification"
    description = "Verifies docs/diagnostics.md is up to date with the MetroErrorCode registry."
    classpath = mainRuntimeClasspath + diagnosticsDocsRuntime
    mainClass.set("dev.zacsweers.metro.compiler.diagnostics.DiagnosticsDocGenerator")
    args(diagnosticsDocsFile.asFile.absolutePath, "--check")
  }

tasks.named("check") { dependsOn(checkDiagnosticsDocs) }

wire { kotlin { javaInterop = false } }

val r8Libraries = configurations.dependencyScope("r8Libraries")

val r8LibraryClasspath =
  configurations.resolvable("r8LibraryClasspath") { extendsFrom(r8Libraries) }

tasks.shadowJar {
  //  minimize {
  //    exclude(dependency("dev.zacsweers.metro:compiler-compat.*:.*"))
  //    exclude(dependency("dev.zacsweers.metro:metro-common:.*"))
  //    r8 {
  //      // Compat implementations receive callbacks from Kotlin compiler classes on the library
  //      // classpath, so R8 cannot discover these usages itself.
  //      keepRules.add(
  //        """
  //        -keep class dev.zacsweers.metro.compiler.compat.** { *; }
  //        -keepattributes
  // AnnotationDefault,EnclosingMethod,Exceptions,InnerClasses,RuntimeInvisibleAnnotations,RuntimeVisibleAnnotations,Signature
  //        -dontwarn com.intellij.**
  //        -dontwarn org.intellij.**
  //        -dontwarn org.jetbrains.**
  //        """
  //          .trimIndent()
  //      )
  //      args.add("--no-minification")
  //      // Extracted dependency rules and relocated signatures emit expected info diagnostics.
  //      args.addAll(listOf("--map-diagnostics", "info", "none"))
  //      args.addAll(
  //        providers.provider {
  //          r8LibraryClasspath
  //            .get()
  //            .files
  //            .sortedBy { it.name }
  //            .flatMap {
  //              listOf("--lib", it.absolutePath)
  //            }
  //        }
  //      )
  //    }
  //  }
  // TODO these are relocated, do we need to/can we exclude these?
  //  exclude("META-INF/wire-runtime.kotlin_module")
  //  exclude("META-INF/okio.kotlin_module")
  dependencies { exclude(dependency("dev.drewhamilton.poko:.*")) }

  // Relocate the metro runtime while excluding the compiler's own package
  relocate("dev.zacsweers.metro", "dev.zacsweers.metro.compiler.shaded.metro") {
    exclude("dev.zacsweers.metro.compiler.**")
    // Metro's annotation lookup still uses string-built runtime package names. Relocate classes,
    // but keep those string constants pointed at the public runtime package so the shaded compiler
    // can run without an unshaded metro-common copy on the same classpath.
    skipStringConstants = true
  }
  // relocate string constrants to the same package, so enableAutoRelocate doesn't get to them
  relocate("dev.zacsweers.metro", "dev.zacsweers.metro")
}

/**
 * The poko plugin adds their dependencies automatically. This is not needed because we can either
 * ignore or embed them, so we remove them.
 *
 * Note: this is done in `afterEvaluate` to run after poko:
 * https://github.com/drewhamilton/Poko/blob/7bde5b23cc65a95a894e0ba0fb305704c49382f0/poko-gradle-plugin/src/main/kotlin/dev/drewhamilton/poko/gradle/PokoGradlePlugin.kt#L19
 */
project.afterEvaluate {
  kotlin {
    sourceSets {
      commonMain {
        configurations.named(implementationConfigurationName) {
          dependencies.removeIf { it is ExternalDependency && it.group == "dev.drewhamilton.poko" }
        }
      }
    }
  }
}

val versionAliases =
  rootProject.isolated.projectDirectory
    .dir("compiler-compat")
    .file("version-aliases.txt")
    .asFile
    .readLines()
    .filterNot { it.isBlank() || it.startsWith('#') }

pluginDevKit {
  pluginPackage = "$group.compiler"
  versionAliases.forEach { testAgainst(it) }
  defaultTestVersion(
    providers.gradleProperty("metro.testCompilerVersion").getOrElse(libs.versions.kotlin.get())
  )
  componentRegistrar = "dev.zacsweers.metro.compiler.MetroComponentRegistrar"
  commandLineProcessor = "dev.zacsweers.metro.compiler.MetroCommandLineProcessor"
}

dependencies {
  add(r8Libraries.name, libs.kotlin.stdlib)
  add(r8Libraries.name, libs.kotlin.reflect)
}

kotlin {
  sourceSets {
    entryPoint.dependencies {
      compileOnly(project(":runtime"))
      compileOnly(libs.poko.annotations)
    }
    commonMain.dependencies {
      compileOnly(libs.kotlin.stdlib)
      compileOnly(libs.poko.annotations)
      implementation(project(":metro-common"))
      implementation(project(":runtime"))
      implementation(libs.androidx.collection)
      implementation(libs.androidx.tracing.wire)
      implementation(libs.picnic)
      implementation(libs.mordant.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(project(":compiler-compat"))
    }

    commonTest.dependencies {
      compileOnly(libs.poko.annotations)

      implementation(project(":interop-dagger"))
      implementation(libs.kotlin.reflect)
      implementation(libs.kotlin.stdlib)

      // Cover for https://github.com/tschuchortdev/kotlin-compile-testing/issues/274
      implementation(libs.kotlin.aptEmbeddable)
      implementation("dev.zacsweers.kctfork:core:0.13.0")
      implementation("dev.zacsweers.kctfork:ksp:0.13.0")
      pluginDevKit {
        testAgainst.configureEach {
          test {
            dependencies {
              if (version.major == 2 && version.minor == 4) {
                implementation("dev.zacsweers.kctfork:core:0.13.0")
                implementation("dev.zacsweers.kctfork:ksp:0.13.0")
              } else {
                implementation(libs.kct)
                implementation(libs.kct.ksp)
              }
            }
          }
        }
      }
      implementation(libs.okio)
      implementation(libs.junit)
      implementation(libs.kotlin.test)
      implementation(libs.truth)
      implementation(libs.coroutines)
      implementation(libs.coroutines.test)
      implementation(libs.dagger.compiler)
      implementation(libs.dagger.runtime)
      implementation(libs.anvil.annotations)
    }
  }
}

// Metro's compiler tests are JUnit 4 based: `kotlin.test.Test` maps to JUnit 4 and the shared
// `MetroCompilerTest` base class relies on JUnit 4 `@Rule`s (e.g. TemporaryFolder). The devkit's
// test fixtures pull in `kotlin-test-junit5` and switch the test tasks to `useJUnitPlatform()`,
// which remaps `kotlin.test.Test` to JUnit 5 Jupiter and silently skips the JUnit 4 rules (tests
// then fail with "the temporary folder has not yet been created"). Keep these tests on JUnit 4.
configurations.configureEach {
  val lower = name.lowercase()
  if (lower.endsWith("testcompileclasspath") || lower.endsWith("testruntimeclasspath")) {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit5")
  }
}

tasks.withType<Test>().configureEach { useJUnit() }
