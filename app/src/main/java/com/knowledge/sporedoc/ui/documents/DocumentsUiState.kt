package com.knowledge.sporedoc.ui.documents

import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.SectionLabel

/** The DocumentsScreen's whole visible state. An empty [sections] list is the "no documents" signal. */
data class DocumentsUiState(
    val sections: List<DocumentSectionUi> = emptyList(),
    val renameDialog: RenameDialogUiState? = null,
    val pendingDeleteId: DocumentId? = null
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

/** State of the rename dialog, when open; null in [DocumentsUiState] means it's hidden. */
data class RenameDialogUiState(
    val documentId: DocumentId,
    val initialName: String,
    val error: RenameDialogError? = null
)

/** The two distinct rename validation failures the dialog must tell apart. */
enum class RenameDialogError { InvalidName, NameCollision }
