package com.knowledge.sporedoc.domain.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Shared size-formatting rule: one decimal place, HALF_UP rounding, minimum "0.1" so a
 * tiny or empty file is never shown as "0.0 MB". No unit, no label: callers add those.
 */
fun formatSizeMb(sizeBytes: Long): String {
    val raw = BigDecimal(sizeBytes).divide(BYTES_PER_MB, 4, RoundingMode.HALF_UP)
    var rounded = raw.setScale(1, RoundingMode.HALF_UP)
    if (rounded < MIN_SIZE) rounded = MIN_SIZE
    return rounded.toPlainString()
}

private val BYTES_PER_MB: BigDecimal = BigDecimal(1_048_576)
private val MIN_SIZE: BigDecimal = BigDecimal("0.1")
