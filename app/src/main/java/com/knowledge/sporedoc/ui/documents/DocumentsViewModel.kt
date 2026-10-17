package com.knowledge.sporedoc.ui.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.usecase.FormatDocumentMetaLine
import com.knowledge.sporedoc.domain.usecase.FormatSelectionCounter
import com.knowledge.sporedoc.domain.usecase.GroupDocumentsIntoSections
import com.knowledge.sporedoc.domain.usecase.SummarizeSelectedDocuments
import com.knowledge.sporedoc.domain.util.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Loads and refreshes the document list, formats it for display, and handles the
 * result of an ML Kit capture or a system document-picker import. The scanner/picker
 * launch itself and the user-cancel path have no JVM-testable seam and are not modeled
 * here (see the Architect's design notes).
 */
class DocumentsViewModel(
    private val documentRepository: DocumentRepository,
    private val groupDocumentsIntoSections: GroupDocumentsIntoSections,
    private val formatDocumentMetaLine: FormatDocumentMetaLine,
    private val summarizeSelectedDocuments: SummarizeSelectedDocuments,
    private val formatSelectionCounter: FormatSelectionCounter,
    private val documentSourceFor: (Uri) -> DocumentSource,
    private val shareUriFor: (DocumentId) -> Uri,
    private val displayNameFor: (Uri) -> String? = { null }
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<DocumentsEffect>(Channel.BUFFERED)
    val effects: Flow<DocumentsEffect> = _effects.receiveAsFlow()

    private var loadedDocuments: List<Document> = emptyList()

    init {
        viewModelScope.launch { refresh() }
    }

    /** Called by the row's own lazy image-loading side effect; null on any failure. */
    suspend fun loadThumbnail(id: DocumentId, targetSize: PixelSize): PageImage? =
        when (val result = documentRepository.getThumbnail(id, targetSize)) {
            is Result.Success -> result.value
            is Result.Failure -> null
        }

    /** ML Kit returned a PDF: add it (no requested name) and refresh the list. */
    fun onScanSucceeded(uri: Uri) {
        viewModelScope.launch {
            val succeeded = try {
                val source = documentSourceFor(uri)
                when (documentRepository.addDocument(source, requestedName = null)) {
                    is Result.Success -> true
                    is Result.Failure -> false
                }
            } catch (_: Exception) {
                false
            }
            if (succeeded) {
                refresh()
            } else {
                _effects.send(DocumentsEffect.ShowCaptureError)
            }
        }
    }

    /** The system document picker returned a PDF: add it keeping its original display name. */
    fun onImportSelected(uri: Uri) {
        viewModelScope.launch {
            val succeeded = try {
                val source = documentSourceFor(uri)
                val requestedName = displayNameFor(uri)
                when (documentRepository.addDocument(source, requestedName = requestedName)) {
                    is Result.Success -> true
                    is Result.Failure -> false
                }
            } catch (_: Exception) {
                false
            }
            if (succeeded) {
                refresh()
            } else {
                _effects.send(DocumentsEffect.ShowImportError)
            }
        }
    }

    /** A PDF arrived from a system Intent (ACTION_VIEW/ACTION_SEND): add it keeping its original display name. */
    fun onExternalDocumentReceived(uri: Uri) {
        viewModelScope.launch {
            val added = try {
                val source = documentSourceFor(uri)
                val requestedName = displayNameFor(uri)
                when (val result = documentRepository.addDocument(source, requestedName = requestedName)) {
                    is Result.Success -> result.value
                    is Result.Failure -> null
                }
            } catch (_: Exception) {
                null
            }
            if (added != null) {
                refresh()
                _effects.send(DocumentsEffect.OpenDocument(added.id, added.fileName))
            } else {
                _effects.send(DocumentsEffect.ShowImportError)
            }
        }
    }

    /** ML Kit failed to produce a result (not a user cancel). */
    fun onScanFailed() {
        viewModelScope.launch {
            _effects.send(DocumentsEffect.ShowCaptureError)
        }
    }

    /** Call when the screen returns to the foreground. */
    fun onScreenResumed() {
        viewModelScope.launch { refresh() }
    }

    /** Requests the native share sheet be opened for this document's PDF. */
    fun onShareRequested(id: DocumentId) {
        viewModelScope.launch {
            _effects.send(DocumentsEffect.ShareDocument(shareUriFor(id)))
        }
    }

    /** Opens the rename dialog with the extension stripped from [fileName]. */
    fun onRenameRequested(id: DocumentId, fileName: String) {
        _uiState.value = _uiState.value.copy(
            renameDialog = RenameDialogUiState(
                documentId = id,
                initialName = stripPdfExtension(fileName)
            )
        )
    }

    /** Closes the rename dialog without renaming anything. */
    fun onRenameCancelled() {
        _uiState.value = _uiState.value.copy(renameDialog = null)
    }

    /** Attempts to rename the document currently open in the rename dialog, to [newName]. */
    fun onRenameConfirmed(newName: String) {
        val dialog = _uiState.value.renameDialog ?: return
        viewModelScope.launch {
            when (val result = documentRepository.renameDocument(dialog.documentId, newName)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(renameDialog = null)
                    refresh()
                }
                is Result.Failure -> when (result.error) {
                    is DocumentError.InvalidName -> _uiState.value = _uiState.value.copy(
                        renameDialog = dialog.copy(error = RenameDialogError.InvalidName)
                    )
                    is DocumentError.NameCollision -> _uiState.value = _uiState.value.copy(
                        renameDialog = dialog.copy(error = RenameDialogError.NameCollision)
                    )
                    else -> {
                        _uiState.value = _uiState.value.copy(renameDialog = null)
                        _effects.send(DocumentsEffect.ShowRenameError)
                    }
                }
            }
        }
    }

    /** Opens the shared delete-confirmation dialog for this document. */
    fun onDeleteRequested(id: DocumentId) {
        _uiState.value = _uiState.value.copy(pendingDeleteId = id)
    }

    /** Closes the delete-confirmation dialog without deleting anything. */
    fun onDeleteCancelled() {
        _uiState.value = _uiState.value.copy(pendingDeleteId = null)
    }

    /** Deletes the document pending confirmation, reloading the list either way. */
    fun onDeleteConfirmed() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            when (documentRepository.deleteDocument(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(pendingDeleteId = null)
                    refresh()
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value.copy(pendingDeleteId = null)
                    refresh()
                    _effects.send(DocumentsEffect.ShowDeleteError)
                }
            }
        }
    }

    /** Enters selection mode with no row selected (the `[ SELECT ]` action). */
    fun onSelectModeEntered() {
        setSelection(emptyList())
    }

    /** No-op if already in selection mode; otherwise enters it with [id] already selected. */
    fun onRowLongPressed(id: DocumentId) {
        if (_uiState.value.selectionMode != null) return
        setSelection(listOf(id))
    }

    /** No-op if not in selection mode; otherwise flips [id]'s membership in the selection. */
    fun onRowToggled(id: DocumentId) {
        val current = _uiState.value.selectionMode ?: return
        val updated = if (id in current.selectedIds) {
            current.selectedIds - id
        } else {
            current.selectedIds + id
        }
        setSelection(updated)
    }

    /** No-op if not in selection mode; otherwise toggles between all-selected and none-selected. */
    fun onSelectAllToggled() {
        val current = _uiState.value.selectionMode ?: return
        val allIds = loadedDocuments.map { it.id }
        val updated = if (current.selectedIds.toSet() == allIds.toSet()) emptyList() else allIds
        setSelection(updated)
    }

    /** Exits selection mode, clearing the whole selection (the `✕` action). */
    fun onSelectionCleared() {
        _uiState.value = _uiState.value.copy(selectionMode = null)
    }

    /** Opens the native share sheet for every selected document's PDF, then exits selection mode. */
    fun onShareSelectedRequested() {
        val selectedIds = _uiState.value.selectionMode?.selectedIds ?: return
        if (selectedIds.isEmpty()) return
        viewModelScope.launch {
            val uris = selectedIds.map(shareUriFor)
            _effects.send(DocumentsEffect.ShareSelectedDocuments(uris))
            _uiState.value = _uiState.value.copy(selectionMode = null)
        }
    }

    /** Opens the shared delete-confirmation dialog referring to every selected document. */
    fun onPurgeRequested() {
        val selectedIds = _uiState.value.selectionMode?.selectedIds ?: return
        if (selectedIds.isEmpty()) return
        _uiState.value = _uiState.value.copy(isPurgeConfirmationVisible = true)
    }

    /** Closes the purge-confirmation dialog without deleting anything or touching the selection. */
    fun onPurgeCancelled() {
        _uiState.value = _uiState.value.copy(isPurgeConfirmationVisible = false)
    }

    /** Deletes every selected document, reloading the list either way, and exits selection mode. */
    fun onPurgeConfirmed() {
        val selectedIds = _uiState.value.selectionMode?.selectedIds ?: return
        viewModelScope.launch {
            var anyFailed = false
            for (id in selectedIds) {
                when (documentRepository.deleteDocument(id)) {
                    is Result.Success -> Unit
                    is Result.Failure -> anyFailed = true
                }
            }
            refresh()
            _uiState.value = _uiState.value.copy(
                isPurgeConfirmationVisible = false,
                selectionMode = null
            )
            if (anyFailed) {
                _effects.send(DocumentsEffect.ShowDeleteError)
            }
        }
    }

    /**
     * Tapping MERGE. No-op below 2 selected documents (R7). On success, exits selection mode
     * and the list is refreshed. On failure, selection mode and its selection are left exactly
     * as they were, and ShowMergeError is sent.
     */
    fun onMergeRequested() {
        val selectedIds = _uiState.value.selectionMode?.selectedIds ?: return
        if (selectedIds.size < 2) return
        viewModelScope.launch {
            when (documentRepository.mergeDocuments(selectedIds)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(selectionMode = null)
                    refresh()
                }
                is Result.Failure -> {
                    _effects.send(DocumentsEffect.ShowMergeError)
                }
            }
        }
    }

    private fun setSelection(selectedIds: List<DocumentId>) {
        val selectedDocuments = loadedDocuments.filter { it.id in selectedIds }
        val summary = summarizeSelectedDocuments(selectedDocuments)
        _uiState.value = _uiState.value.copy(
            selectionMode = SelectionModeUiState(
                selectedIds = selectedIds,
                counterText = formatSelectionCounter(selectedIds.size),
                summary = SelectionSummaryUi(
                    documentCount = summary.documentCount,
                    totalPages = summary.totalPages,
                    totalSizeMb = summary.totalSizeMb
                )
            )
        )
    }

    private fun stripPdfExtension(fileName: String): String =
        if (fileName.endsWith(".pdf")) fileName.removeSuffix(".pdf") else fileName

    private suspend fun refresh() {
        when (val result = documentRepository.listDocuments()) {
            is Result.Success -> {
                loadedDocuments = result.value
                _uiState.value = _uiState.value.copy(sections = buildSections(result.value))
            }
            is Result.Failure -> Unit // keep the previous state; no AC in this delivery covers this case
        }
    }

    private fun buildSections(documents: List<Document>): List<DocumentSectionUi> =
        groupDocumentsIntoSections(documents).map { section ->
            DocumentSectionUi(
                label = section.label,
                rows = section.documents.map { document ->
                    DocumentRowUi(
                        id = document.id,
                        fileName = document.fileName,
                        metaLine = formatDocumentMetaLine(document)
                    )
                }
            )
        }
}
