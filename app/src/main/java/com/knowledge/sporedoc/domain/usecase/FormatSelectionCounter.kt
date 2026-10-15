package com.knowledge.sporedoc.domain.usecase

/**
 * Turns a selected count into its display digits: 0 is a special case with no
 * leading zero (confirmed product decision), 1..9 are zero-padded to two digits,
 * and 10+ grow unpadded.
 */
class FormatSelectionCounter {

    operator fun invoke(selectedCount: Int): String = when {
        selectedCount == 0 -> "0"
        selectedCount < 10 -> "0$selectedCount"
        else -> selectedCount.toString()
    }
}
