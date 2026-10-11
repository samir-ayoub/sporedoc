package com.knowledge.sporedoc.ui.documents

import android.net.Uri
import com.knowledge.sporedoc.data.repository.FakeDocumentSource
import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.SectionLabel
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.usecase.FormatDocumentMetaLine
import com.knowledge.sporedoc.domain.usecase.GroupDocumentsIntoSections
import com.knowledge.sporedoc.domain.util.Result
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Tests for [DocumentsViewModel]: list loading/formatting, lazy thumbnails, the
 * ML Kit capture result handling (success/failure) and the resume reload.
 *
 * The scanner launch itself (R12/AC7) and a user cancel (AC9) have no JVM-testable
 * seam (they require the real `GmsDocumentScanning` client and Play Services) and are
 * exercised only through the "Validação manual" steps in the spec.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentsViewModelTest {

    private val fixedNow: Instant =
        ZonedDateTime.of(2026, 10, 11, 14, 30, 0, 0, ZoneId.systemDefault()).toInstant()

    private val formatDocumentMetaLine = FormatDocumentMetaLine()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun groupUseCase(): GroupDocumentsIntoSections = GroupDocumentsIntoSections { fixedNow }

    private fun newViewModel(
        repository: FakeDocumentRepository,
        documentSourceFor: (Uri) -> DocumentSource = { FakeDocumentSource("PDF-OK:1") }
    ): DocumentsViewModel = DocumentsViewModel(
        documentRepository = repository,
        groupDocumentsIntoSections = groupUseCase(),
        formatDocumentMetaLine = formatDocumentMetaLine,
        documentSourceFor = documentSourceFor
    )

    private fun document(
        name: String,
        modifiedAt: Instant,
        pageCount: Int = 2,
        sizeBytes: Long = 1_468_006L
    ): Document = Document(
        id = DocumentId(name),
        fileName = name,
        pageCount = pageCount,
        sizeBytes = sizeBytes,
        modifiedAt = modifiedAt
    )

    // --- initial load / grouping / formatting ---

    @Test
    fun `initial state groups documents into TODAY, RECENT and ARCHIVE in that order`() = runTest {
        val today = document("today.pdf", fixedNow)
        val recent = document("recent.pdf", fixedNow.minusSeconds(60L * 60 * 24 * 10))
        val archive = document("archive.pdf", fixedNow.minusSeconds(60L * 60 * 24 * 40))
        val repository = FakeDocumentRepository(listOf(today, recent, archive))

        val viewModel = newViewModel(repository)

        val sections = viewModel.uiState.value.sections
        assertEquals(
            listOf(SectionLabel.TODAY, SectionLabel.RECENT_DOCUMENTS, SectionLabel.ARCHIVE),
            sections.map { it.label }
        )
        assertEquals(listOf("today.pdf"), sections[0].rows.map { it.fileName })
        assertEquals(listOf("recent.pdf"), sections[1].rows.map { it.fileName })
        assertEquals(listOf("archive.pdf"), sections[2].rows.map { it.fileName })
    }

    @Test
    fun `a row's meta line matches FormatDocumentMetaLine's output for that document`() = runTest {
        val doc = document("today.pdf", fixedNow, pageCount = 2, sizeBytes = 1_468_006L)
        val repository = FakeDocumentRepository(listOf(doc))

        val viewModel = newViewModel(repository)

        val row = viewModel.uiState.value.sections.single().rows.single()
        assertEquals(formatDocumentMetaLine(doc), row.metaLine)
        assertEquals("2 PP  /  1.4 MB  /  11.10.2026 14:30", row.metaLine)
    }

    @Test
    fun `a library with only today's documents produces only the TODAY section`() = runTest {
        val today = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(today))

        val viewModel = newViewModel(repository)

        val sections = viewModel.uiState.value.sections
        assertEquals(listOf(SectionLabel.TODAY), sections.map { it.label })
    }

    @Test
    fun `an empty library produces no sections`() = runTest {
        val repository = FakeDocumentRepository(emptyList())

        val viewModel = newViewModel(repository)

        assertTrue(viewModel.uiState.value.sections.isEmpty())
    }

    // --- thumbnails ---

    @Test
    fun `loadThumbnail returns the page image when the repository succeeds`() = runTest {
        val doc = document("today.pdf", fixedNow)
        val image = PageImage(bytes = byteArrayOf(1, 2, 3), width = 10, height = 10)
        val repository = FakeDocumentRepository(listOf(doc)).apply {
            thumbnails = mapOf(doc.id to Result.Success(image))
        }
        val viewModel = newViewModel(repository)

        val result = viewModel.loadThumbnail(doc.id)

        assertEquals(image, result)
    }

    @Test
    fun `loadThumbnail returns null when the repository fails`() = runTest {
        val doc = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        val result = viewModel.loadThumbnail(doc.id)

        assertNull(result)
    }

    // --- onScanSucceeded ---

    @Test
    fun `onScanSucceeded adds the scanned pdf without a requested name and refreshes the list`() = runTest {
        val repository = FakeDocumentRepository(emptyList())
        var receivedUri: Uri? = null
        val viewModel = newViewModel(repository, documentSourceFor = { uri ->
            receivedUri = uri
            FakeDocumentSource("PDF-OK:3")
        })
        val scanResultUri = Uri.parse("content://scan-result")

        viewModel.onScanSucceeded(scanResultUri)

        assertEquals(scanResultUri, receivedUri)
        assertEquals(1, repository.addDocumentCalls.size)
        assertNull(repository.addDocumentCalls.single().second)
        val todaySection = viewModel.uiState.value.sections.single { it.label == SectionLabel.TODAY }
        assertEquals(listOf("Spore_20261011_1430.pdf"), todaySection.rows.map { it.fileName })
    }

    @Test
    fun `onScanSucceeded puts the new document at the top of TODAY among existing documents`() = runTest {
        val existing = document("older_today.pdf", fixedNow.minusSeconds(3600))
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(repository)
        val scanResultUri = Uri.parse("content://scan-result")

        viewModel.onScanSucceeded(scanResultUri)

        val todaySection = viewModel.uiState.value.sections.single { it.label == SectionLabel.TODAY }
        assertEquals(listOf("Spore_20261011_1430.pdf", "older_today.pdf"), todaySection.rows.map { it.fileName })
    }

    @Test
    fun `onScanSucceeded emits ShowCaptureError and leaves the list unchanged when addDocument fails`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing)).apply {
            addDocumentResult = Result.Failure(DocumentError.UnreadableFile(DocumentId("x")))
        }
        val viewModel = newViewModel(repository)
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onScanSucceeded(Uri.parse("content://scan-result"))
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowCaptureError), effects)
        assertEquals(listOf("today.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        job.cancel()
    }

    @Test
    fun `onScanSucceeded emits ShowCaptureError and leaves the list unchanged when reading the uri throws`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(repository, documentSourceFor = {
            throw java.io.IOException("resolver returned no stream")
        })
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onScanSucceeded(Uri.parse("content://scan-result"))
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowCaptureError), effects)
        assertEquals(listOf("today.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        assertTrue(repository.addDocumentCalls.isEmpty())
        job.cancel()
    }

    // --- onScanFailed ---

    @Test
    fun `onScanFailed emits ShowCaptureError without touching the list`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(repository)
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onScanFailed()
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowCaptureError), effects)
        assertEquals(listOf("today.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        assertTrue(repository.addDocumentCalls.isEmpty())
        job.cancel()
    }

    // --- onScreenResumed ---

    @Test
    fun `onScreenResumed reloads the list so a document removed from disk disappears`() = runTest {
        val removedLater = document("gone.pdf", fixedNow)
        val staying = document("staying.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(removedLater, staying))
        val viewModel = newViewModel(repository)
        assertEquals(2, viewModel.uiState.value.sections.single().rows.size)

        repository.removeDocument(removedLater.id)
        viewModel.onScreenResumed()

        val fileNames = viewModel.uiState.value.sections.single().rows.map { it.fileName }
        assertEquals(listOf("staying.pdf"), fileNames)
    }

    @Test
    fun `onScreenResumed keeps the previous state when listDocuments fails`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(repository)
        val before = viewModel.uiState.value

        repository.listDocumentsResult = Result.Failure(DocumentError.FileMissing(DocumentId("today.pdf")))
        viewModel.onScreenResumed()

        assertEquals(before, viewModel.uiState.value)
    }
}
