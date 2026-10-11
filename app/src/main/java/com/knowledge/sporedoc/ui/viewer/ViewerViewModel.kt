package com.knowledge.sporedoc.ui.viewer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.util.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Loads the document's page count, tracks the current page, and drives export/delete
 * decisions. Navigation itself (back icon, system back, opening a document from the list)
 * has no decision to make and is wired directly in `ui/navigation` instead.
 */
class ViewerViewModel(
    private val documentId: DocumentId,
    fileName: String,
    private val documentRepository: DocumentRepository,
    private val shareUriFor: (DocumentId) -> Uri
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewerUiState(fileName = fileName))
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ViewerEffect>(Channel.BUFFERED)
    val effects: Flow<ViewerEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            when (val result = documentRepository.getPageCount(documentId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    pageCount = result.value,
                    currentPage = 1
                )
                is Result.Failure -> _effects.send(ViewerEffect.LoadFailed)
            }
        }
    }

    /** Lazily fetches one page's image, mirroring DocumentsViewModel.loadThumbnail; null on any failure. */
    suspend fun loadPage(pageIndex: Int): PageImage? =
        when (val result = documentRepository.getPageImage(documentId, pageIndex)) {
            is Result.Success -> result.value
            is Result.Failure -> null
        }

    /** Reported by the pager when the visible page settles (0-based index). */
    fun onPageChanged(pageIndex: Int) {
        _uiState.value = _uiState.value.copy(currentPage = pageIndex + 1)
    }

    fun onExportRequested() {
        viewModelScope.launch {
            _effects.send(ViewerEffect.ShareDocument(shareUriFor(documentId)))
        }
    }

    fun onDeleteRequested() {
        _uiState.value = _uiState.value.copy(isDeleteDialogVisible = true)
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            when (documentRepository.deleteDocument(documentId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isDeleteDialogVisible = false)
                    _effects.send(ViewerEffect.DocumentDeleted)
                }
                is Result.Failure -> _uiState.value = _uiState.value.copy(isDeleteDialogVisible = false)
            }
        }
    }

    fun onDeleteCancelled() {
        _uiState.value = _uiState.value.copy(isDeleteDialogVisible = false)
    }
}
