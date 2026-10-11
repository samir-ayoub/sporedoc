package com.knowledge.sporedoc.domain.model

import java.time.Instant

/** A single PDF document stored in the library. */
data class Document(
    val id: DocumentId,
    val fileName: String,
    val pageCount: Int,
    val sizeBytes: Long,
    val modifiedAt: Instant
)
