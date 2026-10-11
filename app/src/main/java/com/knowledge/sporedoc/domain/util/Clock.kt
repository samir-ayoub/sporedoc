package com.knowledge.sporedoc.domain.util

import java.time.Instant

/** The injectable "now" seam shared by name generation and grouping. */
typealias Clock = () -> Instant
