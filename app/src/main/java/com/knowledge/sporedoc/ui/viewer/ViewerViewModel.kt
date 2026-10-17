package com.knowledge.sporedoc.ui.viewer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.usecase.AccumulateRotation
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

    private val accumulateRotation = AccumulateRotation()

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
    suspend fun loadPage(pageIndex: Int, targetSize: PixelSize): PageImage? =
        when (val result = documentRepository.getPageImage(documentId, pageIndex, targetSize)) {
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

    /** Enters the dedicated rotate-mode state (ROTATE LEFT/SAVE/CANCEL), no rotation pending yet. */
    fun onRotateRequested() {
        _uiState.value = _uiState.value.copy(isRotateModeActive = true, pageRotationDegrees = emptyMap())
    }

    /** Rotates the currently visible page only, accumulating 90° counterclockwise per tap. */
    fun onRotateLeftClicked() {
        val pageIndex = _uiState.value.currentPage - 1
        val current = _uiState.value.pageRotationDegrees[pageIndex] ?: 0
        val next = accumulateRotation(current)
        val updated = _uiState.value.pageRotationDegrees.toMutableMap()
        if (next == 0) updated.remove(pageIndex) else updated[pageIndex] = next
        _uiState.value = _uiState.value.copy(pageRotationDegrees = updated)
    }

    /** Persists every non-zero pending rotation, then exits rotate mode; a no-op write if none are pending. */
    fun onRotateSaveClicked() {
        val pending = _uiState.value.pageRotationDegrees.filterValues { it != 0 }
        if (pending.isEmpty()) {
            _uiState.value = _uiState.value.copy(isRotateModeActive = false, pageRotationDegrees = emptyMap())
            return
        }

        viewModelScope.launch {
            when (documentRepository.rotatePages(documentId, pending)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRotateModeActive = false,
                        pageRotationDegrees = emptyMap(),
                        contentRevision = _uiState.value.contentRevision + 1
                    )
                }
                is Result.Failure -> {
                    _effects.send(ViewerEffect.RotateSaveFailed)
                }
            }
        }
    }

    /** Discards any accumulated rotation and exits rotate mode without touching the repository. */
    fun onRotateCancelClicked() {
        _uiState.value = _uiState.value.copy(isRotateModeActive = false, pageRotationDegrees = emptyMap())
    }
}
