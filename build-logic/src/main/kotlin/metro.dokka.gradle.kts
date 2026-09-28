// Copyright (C) 2026 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
plugins {
  id("org.jetbrains.dokka")
}

dokka {
  dokkaPublications.html {
    // NOTE: This path must be in sync with `mkdocs.yml`'s API nav config path
    outputDirectory.set(rootDir.resolve("docs/api"))
    includes.from(project.layout.projectDirectory.file("README.md"))
  }
}
