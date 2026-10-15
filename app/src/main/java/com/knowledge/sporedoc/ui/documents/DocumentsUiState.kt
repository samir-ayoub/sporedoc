package com.knowledge.sporedoc.ui.documents

import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.SectionLabel

/** The DocumentsScreen's whole visible state. An empty [sections] list is the "no documents" signal. */
data class DocumentsUiState(
    val sections: List<DocumentSectionUi> = emptyList(),
    val renameDialog: RenameDialogUiState? = null,
    val pendingDeleteId: DocumentId? = null,
    val selectionMode: SelectionModeUiState? = null
)

/** State while the list is in multi-select mode (SD-17); null in [DocumentsUiState] means it's off. */
data class SelectionModeUiState(
    val selectedIds: Set<DocumentId>,
    val counterText: String,
    val summary: SelectionSummaryUi
)

/** The selected documents' totals, raw digits only (no unit, no label; see strings.xml). */
data class SelectionSummaryUi(
    val documentCount: Int,
    val totalPages: Int,
    val totalSizeMb: String
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
