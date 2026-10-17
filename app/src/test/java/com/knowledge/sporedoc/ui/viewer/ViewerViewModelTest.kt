package com.knowledge.sporedoc.ui.viewer

import android.net.Uri
import com.knowledge.sporedoc.domain.model.CropCorner
import com.knowledge.sporedoc.domain.model.CropRect
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import com.knowledge.sporedoc.ui.documents.FakeDocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [ViewerViewModel]: initial load (success/failure), page navigation state,
 * export, and the delete-confirmation flow.
 *
 * Covers (from SD-4's acceptance criteria):
 * - "Dado um documento de 6 páginas aberto no visualizador, quando o ecrã é composto,
 *   então o indicador mostra PAGE 1 / 6 e a página apresentada é a primeira."
 * - "Dado um documento de 6 páginas, quando se avança duas páginas no paginador, então
 *   o indicador mostra PAGE 3 / 6."
 * - "Dado um documento de 1 página, quando o ecrã é composto, então o indicador mostra
 *   PAGE 1 / 1."
 * - "Dado um documento aberto no visualizador, quando se toca no botão de exportar,
 *   então é pedida a abertura da folha de partilha nativa com o ficheiro PDF desse documento."
 * - "Dado um documento aberto no visualizador, quando se toca no ícone de apagar, então
 *   aparece o diálogo de confirmação e o documento ainda não foi apagado."
 * - "Dado o diálogo de confirmação aberto, quando se confirma, então o documento é
 *   apagado [...]."
 * - "Dado o diálogo de confirmação aberto, quando se cancela, então o diálogo fecha [...]
 *   e o documento continua a existir."
 * - "Dado um identificador de um documento que já não existe no disco [...] então o ecrã
 *   volta à lista e é mostrada uma mensagem de erro." (FileMissing)
 * - "Dado um ficheiro que não é um PDF válido [...] então o ecrã volta à lista e é
 *   mostrada uma mensagem de erro." (UnreadableFile)
 *
 * Navigation itself (tap-to-open, back icon, system back), the share-sheet intent, the
 * dialog's visuals and TalkBack announcements have no JVM-testable seam in this project
 * (no Robolectric, no `androidx.compose.ui.test` dependency) and are exercised only
 * through "Validação manual" / the NavHost wiring, per the Architect's design notes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewerViewModelTest {

    private val documentId = DocumentId("contrato.pdf")
    private val fileName = "contrato.pdf"
    private val target = PixelSize(widthPx = 282, heightPx = 398)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel(
        repository: FakeDocumentRepository,
        shareUriFor: (DocumentId) -> Uri = { Uri.parse("content://sporedoc/${it.value}") }
    ): ViewerViewModel = ViewerViewModel(
        documentId = documentId,
        fileName = fileName,
        documentRepository = repository,
        shareUriFor = shareUriFor
    )

    private fun repositoryWithPageCount(count: Int): FakeDocumentRepository =
        FakeDocumentRepository().apply {
            pageCountResults = mapOf(documentId to Result.Success(count))
        }

    // --- initial load ---

    @Test
    fun `initial state carries the file name and starts on page 1 of the loaded page count`() = runTest {
        val repository = repositoryWithPageCount(6)

        val viewModel = newViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(fileName, state.fileName)
        assertEquals(6, state.pageCount)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `a single-page document reports PAGE 1 of 1`() = runTest {
        val repository = repositoryWithPageCount(1)

        val viewModel = newViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(1, state.pageCount)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `a missing document emits LoadFailed and does not populate page state`() = runTest {
        val repository = FakeDocumentRepository().apply {
            pageCountResults = mapOf(documentId to Result.Failure(DocumentError.FileMissing(documentId)))
        }
        val effects = mutableListOf<ViewerEffect>()
        val viewModel = newViewModel(repository)
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        assertEquals(listOf(ViewerEffect.LoadFailed), effects)
        assertEquals(0, viewModel.uiState.value.pageCount)
        job.cancel()
    }

    @Test
    fun `a PDF that cannot be read emits LoadFailed`() = runTest {
        val repository = FakeDocumentRepository().apply {
            pageCountResults = mapOf(documentId to Result.Failure(DocumentError.UnreadableFile(documentId)))
        }
        val effects = mutableListOf<ViewerEffect>()
        val viewModel = newViewModel(repository)
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        assertEquals(listOf(ViewerEffect.LoadFailed), effects)
        job.cancel()
    }

    // --- page navigation ---

    @Test
    fun `onPageChanged with a 0-based index 2 moves the 1-based current page to 3`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)

        viewModel.onPageChanged(2)

        assertEquals(3, viewModel.uiState.value.currentPage)
        assertEquals(6, viewModel.uiState.value.pageCount)
    }

    @Test
    fun `loadPage returns the page image when the repository succeeds`() = runTest {
        val image = PageImage(bytes = byteArrayOf(1, 2, 3), width = 10, height = 20)
        val repository = repositoryWithPageCount(6).apply {
            pageImages = mapOf((documentId to 0) to Result.Success(image))
        }
        val viewModel = newViewModel(repository)

        val result = viewModel.loadPage(0, target)

        assertEquals(image, result)
    }

    @Test
    fun `loadPage returns null when the repository fails`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)

        val result = viewModel.loadPage(0, target)

        assertNull(result)
    }

    @Test
    fun `loadPage forwards the caller's target size to the repository, not a fixed size`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        val requestedSize = PixelSize(widthPx = 282, heightPx = 398)

        viewModel.loadPage(2, requestedSize)

        assertEquals(listOf(Triple(documentId, 2, requestedSize)), repository.getPageImageCalls)
    }

    // --- export ---

    @Test
    fun `onExportRequested emits ShareDocument with the uri resolved for this document's id`() = runTest {
        val repository = repositoryWithPageCount(6)
        val expectedUri = Uri.parse("content://sporedoc/contrato.pdf")
        var requestedId: DocumentId? = null
        val viewModel = newViewModel(repository, shareUriFor = { id ->
            requestedId = id
            expectedUri
        })
        val effects = mutableListOf<ViewerEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onExportRequested()
        runCurrent()

        assertEquals(documentId, requestedId)
        assertEquals(listOf(ViewerEffect.ShareDocument(expectedUri)), effects)
        job.cancel()
    }

    // --- delete flow ---

    @Test
    fun `onDeleteRequested shows the confirmation dialog without deleting anything`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)

        viewModel.onDeleteRequested()

        assertTrue(viewModel.uiState.value.isDeleteDialogVisible)
        assertTrue(repository.deleteDocumentCalls.isEmpty())
    }

    @Test
    fun `onDeleteConfirmed deletes the document, closes the dialog and emits DocumentDeleted`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onDeleteRequested()
        val effects = mutableListOf<ViewerEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDeleteConfirmed()
        runCurrent()

        assertEquals(listOf(documentId), repository.deleteDocumentCalls)
        assertEquals(listOf(ViewerEffect.DocumentDeleted), effects)
        assertEquals(false, viewModel.uiState.value.isDeleteDialogVisible)
        job.cancel()
    }

    @Test
    fun `onDeleteCancelled closes the dialog and deletes nothing`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onDeleteRequested()

        viewModel.onDeleteCancelled()

        assertEquals(false, viewModel.uiState.value.isDeleteDialogVisible)
        assertTrue(repository.deleteDocumentCalls.isEmpty())
    }

    // --- rotate flow (SD-22) ---

    @Test
    fun `onRotateRequested enters rotate mode with no pending rotations`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)

        viewModel.onRotateRequested()

        val state = viewModel.uiState.value
        assertTrue(state.isRotateModeActive)
        assertEquals(emptyMap<Int, Int>(), state.pageRotationDegrees)
    }

    @Test
    fun `one ROTATE LEFT tap accumulates 90 degrees for the current page index`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onRotateRequested()

        viewModel.onRotateLeftClicked()

        assertEquals(90, viewModel.uiState.value.pageRotationDegrees[0])
    }

    @Test
    fun `four consecutive ROTATE LEFT taps on the same page return its accumulated rotation to absent or zero`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onRotateRequested()

        repeat(4) { viewModel.onRotateLeftClicked() }

        val pending = viewModel.uiState.value.pageRotationDegrees[0] ?: 0
        assertEquals(0, pending)
    }

    @Test
    fun `rotating one page, visiting another page and rotating it does not affect the first page's accumulated rotation`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onRotateRequested()

        viewModel.onRotateLeftClicked() // page index 0 -> 90
        viewModel.onPageChanged(1) // swipe to page index 1
        viewModel.onRotateLeftClicked() // page index 1 -> 90
        viewModel.onPageChanged(0) // swipe back to page index 0

        val state = viewModel.uiState.value
        assertEquals(90, state.pageRotationDegrees[0])
        assertEquals(90, state.pageRotationDegrees[1])
    }

    @Test
    fun `onRotateSaveClicked with pending rotations calls rotatePages with only the non-zero entries, then exits rotate mode`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onRotateRequested()
        viewModel.onRotateLeftClicked() // page index 0 -> 90

        viewModel.onRotateSaveClicked()
        runCurrent()

        assertEquals(listOf(documentId to mapOf(0 to 90)), repository.rotatePagesCalls)
        val state = viewModel.uiState.value
        assertEquals(emptyMap<Int, Int>(), state.pageRotationDegrees)
        assertEquals(false, state.isRotateModeActive)
        assertEquals(1, state.contentRevision)
    }

    @Test
    fun `onRotateSaveClicked on failure emits RotateSaveFailed and keeps rotate mode and pending rotations`() = runTest {
        val repository = repositoryWithPageCount(6).apply {
            rotatePagesResult = Result.Failure(DocumentError.FileMissing(documentId))
        }
        val viewModel = newViewModel(repository)
        viewModel.onRotateRequested()
        viewModel.onRotateLeftClicked()
        val effects = mutableListOf<ViewerEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onRotateSaveClicked()
        runCurrent()

        assertEquals(listOf(ViewerEffect.RotateSaveFailed), effects)
        val state = viewModel.uiState.value
        assertTrue(state.isRotateModeActive)
        assertEquals(90, state.pageRotationDegrees[0])
        assertEquals(0, state.contentRevision)
        job.cancel()
    }

    @Test
    fun `onRotateSaveClicked with nothing rotated exits rotate mode without calling the repository`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onRotateRequested()

        viewModel.onRotateSaveClicked()
        runCurrent()

        assertTrue(repository.rotatePagesCalls.isEmpty())
        assertEquals(false, viewModel.uiState.value.isRotateModeActive)
    }

    @Test
    fun `onRotateCancelClicked discards pending rotations, exits rotate mode and calls no repository method`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onRotateRequested()
        viewModel.onRotateLeftClicked()

        viewModel.onRotateCancelClicked()

        val state = viewModel.uiState.value
        assertEquals(emptyMap<Int, Int>(), state.pageRotationDegrees)
        assertEquals(false, state.isRotateModeActive)
        assertEquals(0, state.contentRevision)
        assertTrue(repository.rotatePagesCalls.isEmpty())
    }

    // --- crop flow (SD-23) ---

    @Test
    fun `onCropRequested enters crop mode with the fixed initial rectangle`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)

        viewModel.onCropRequested()

        val state = viewModel.uiState.value
        assertTrue(state.isCropModeActive)
        assertEquals(CropRect.Initial, state.cropRect)
    }

    @Test
    fun `onCropNodeDragged moves the dragged corner's coordinates while the opposite corner stays as it was`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onCropRequested()
        val initial = CropRect.Initial
        val requestedX = initial.left + 0.01f
        val requestedY = initial.top + 0.01f

        viewModel.onCropNodeDragged(CropCorner.TOP_LEFT, requestedX, requestedY)

        val rect = viewModel.uiState.value.cropRect!!
        assertEquals(requestedX, rect.left, 0.0001f)
        assertEquals(requestedY, rect.top, 0.0001f)
        assertEquals(initial.right, rect.right, 0.0001f)
        assertEquals(initial.bottom, rect.bottom, 0.0001f)
    }

    @Test
    fun `onCropNodeDragged before crop mode is entered does nothing, cropRect stays null`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)

        viewModel.onCropNodeDragged(CropCorner.TOP_LEFT, 0.3f, 0.3f)

        assertNull(viewModel.uiState.value.cropRect)
        assertEquals(false, viewModel.uiState.value.isCropModeActive)
    }

    @Test
    fun `onCropSaveClicked crops the current page with the pending rect, then exits crop mode`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onCropRequested()
        val pendingRect = CropRect(left = 0.1f, top = 0.1f, right = 0.9f, bottom = 0.9f)
        viewModel.onCropNodeDragged(CropCorner.TOP_LEFT, pendingRect.left, pendingRect.top)
        viewModel.onCropNodeDragged(CropCorner.BOTTOM_RIGHT, pendingRect.right, pendingRect.bottom)

        viewModel.onCropSaveClicked()
        runCurrent()

        assertEquals(listOf(Triple(documentId, 0, pendingRect)), repository.cropPageCalls)
        val state = viewModel.uiState.value
        assertEquals(false, state.isCropModeActive)
        assertNull(state.cropRect)
        assertEquals(1, state.contentRevision)
    }

    @Test
    fun `onCropSaveClicked targets the current page's 0-based index at the moment SAVE is tapped`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onPageChanged(3) // 0-based index 3, 1-based page 4
        viewModel.onCropRequested()

        viewModel.onCropSaveClicked()
        runCurrent()

        assertEquals(listOf(Triple(documentId, 3, CropRect.Initial)), repository.cropPageCalls)
    }

    @Test
    fun `onCropSaveClicked on failure emits CropSaveFailed and keeps crop mode and the pending rect`() = runTest {
        val repository = repositoryWithPageCount(6).apply {
            cropPageResult = Result.Failure(DocumentError.FileMissing(documentId))
        }
        val viewModel = newViewModel(repository)
        viewModel.onCropRequested()
        val effects = mutableListOf<ViewerEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onCropSaveClicked()
        runCurrent()

        assertEquals(listOf(ViewerEffect.CropSaveFailed), effects)
        val state = viewModel.uiState.value
        assertTrue(state.isCropModeActive)
        assertEquals(CropRect.Initial, state.cropRect)
        assertEquals(0, state.contentRevision)
        job.cancel()
    }

    @Test
    fun `onCropCancelClicked discards the pending rect, exits crop mode and calls no repository method`() = runTest {
        val repository = repositoryWithPageCount(6)
        val viewModel = newViewModel(repository)
        viewModel.onCropRequested()
        viewModel.onCropNodeDragged(CropCorner.TOP_LEFT, 0.3f, 0.3f)

        viewModel.onCropCancelClicked()

        val state = viewModel.uiState.value
        assertEquals(false, state.isCropModeActive)
        assertNull(state.cropRect)
        assertEquals(0, state.contentRevision)
        assertTrue(repository.cropPageCalls.isEmpty())
    }
}
