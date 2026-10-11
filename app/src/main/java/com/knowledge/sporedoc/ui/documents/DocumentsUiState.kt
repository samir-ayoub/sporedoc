package com.knowledge.sporedoc.ui.documents

import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.SectionLabel

/** The DocumentsScreen's whole visible state. An empty [sections] list is the "no documents" signal. */
data class DocumentsUiState(
    val sections: List<DocumentSectionUi> = emptyList()
)

/** One section rendered under a `SectionHeader` label, with its already-ordered rows. */
data class DocumentSectionUi(
    val label: SectionLabel,
    val rows: List<DocumentRowUi>
)

/** One `DocumentRow`'s display data: identity plus the strings it shows verbatim. */
data class DocumentRowUi(
    val id: DocumentId,
    val fileName: String,
    val metaLine: String
)
