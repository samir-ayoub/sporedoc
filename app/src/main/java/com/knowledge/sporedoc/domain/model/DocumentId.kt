package com.knowledge.sporedoc.domain.model

/**
 * Opaque identifier for a [Document]. The data layer decides what the value encodes
 * (today, the file name); domain code only relies on equality.
 */
@JvmInline
value class DocumentId(val value: String)
