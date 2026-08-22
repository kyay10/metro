// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package dev.zacsweers.metro.compiler.compat

@CompatApi(
  since = "2.4.0",
  reason = CompatApi.Reason.COMPAT,
  message = "2.4.0 invalidates incremental compilation when annotation arguments change",
)
public actual val supportsAnnotationArgumentInvalidation: Boolean
  get() = false
