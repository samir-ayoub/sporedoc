package com.knowledge.sporedoc.ui.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.usecase.FormatDocumentMetaLine
import com.knowledge.sporedoc.domain.usecase.GroupDocumentsIntoSections
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
    private val documentSourceFor: (Uri) -> DocumentSource,
    private val shareUriFor: (DocumentId) -> Uri,
    private val displayNameFor: (Uri) -> String? = { null }
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<DocumentsEffect>(Channel.BUFFERED)
    val effects: Flow<DocumentsEffect> = _effects.receiveAsFlow()

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

    private fun stripPdfExtension(fileName: String): String =
        if (fileName.endsWith(".pdf")) fileName.removeSuffix(".pdf") else fileName

    private suspend fun refresh() {
        when (val result = documentRepository.listDocuments()) {
            is Result.Success -> _uiState.value = _uiState.value.copy(sections = buildSections(result.value))
            is Result.Failure -> Unit // keep the previous state; no AC in this delivery covers this case
        }
    }

    private fun buildSections(documents: List<com.knowledge.sporedoc.domain.model.Document>): List<DocumentSectionUi> =
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
