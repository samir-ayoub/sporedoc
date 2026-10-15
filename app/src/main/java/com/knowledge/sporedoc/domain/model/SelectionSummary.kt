package com.knowledge.sporedoc.domain.model

/** One selection's totals: count, pages, and formatted size (no unit, no label). */
data class SelectionSummary(
    val documentCount: Int,
    val totalPages: Int,
    val totalSizeMb: String
)
