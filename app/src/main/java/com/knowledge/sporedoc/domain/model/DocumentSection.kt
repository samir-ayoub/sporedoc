package com.knowledge.sporedoc.domain.model

/** A group of documents rendered under one `SectionHeader` label. */
data class DocumentSection(
    val label: SectionLabel,
    val documents: List<Document>
)

/** The section labels defined by `components.SectionHeader` in the design handover. */
enum class SectionLabel { TODAY, RECENT_DOCUMENTS, ARCHIVE }
