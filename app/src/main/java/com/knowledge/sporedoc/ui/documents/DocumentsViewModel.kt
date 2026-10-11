package com.knowledge.sporedoc.ui.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
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
 * result of an ML Kit capture. The scanner launch itself and the user-cancel path have
 * no JVM-testable seam and are not modeled here (see the Architect's design notes).
 */
class DocumentsViewModel(
    private val documentRepository: DocumentRepository,
    private val groupDocumentsIntoSections: GroupDocumentsIntoSections,
    private val formatDocumentMetaLine: FormatDocumentMetaLine,
    private val documentSourceFor: (Uri) -> DocumentSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<DocumentsEffect>(Channel.BUFFERED)
    val effects: Flow<DocumentsEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    /** Called by the row's own lazy image-loading side effect; null on any failure. */
    suspend fun loadThumbnail(id: DocumentId): PageImage? =
        when (val result = documentRepository.getThumbnail(id)) {
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

    private suspend fun refresh() {
        when (val result = documentRepository.listDocuments()) {
            is Result.Success -> _uiState.value = DocumentsUiState(sections = buildSections(result.value))
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
