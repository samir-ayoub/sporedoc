package com.knowledge.sporedoc.ui.documents

import android.net.Uri
import com.knowledge.sporedoc.data.repository.FakeDocumentSource
import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.model.SectionLabel
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.usecase.FormatDocumentMetaLine
import com.knowledge.sporedoc.domain.usecase.FormatSelectionCounter
import com.knowledge.sporedoc.domain.usecase.GroupDocumentsIntoSections
import com.knowledge.sporedoc.domain.usecase.SummarizeSelectedDocuments
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

    private val target = PixelSize(widthPx = 38, heightPx = 52)

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
        documentSourceFor: (Uri) -> DocumentSource = { FakeDocumentSource("PDF-OK:1") },
        shareUriFor: (DocumentId) -> Uri = { Uri.parse("content://sporedoc/${it.value}") },
        displayNameFor: (Uri) -> String? = { "picked.pdf" }
    ): DocumentsViewModel = DocumentsViewModel(
        documentRepository = repository,
        groupDocumentsIntoSections = groupUseCase(),
        formatDocumentMetaLine = formatDocumentMetaLine,
        summarizeSelectedDocuments = SummarizeSelectedDocuments(),
        formatSelectionCounter = FormatSelectionCounter(),
        documentSourceFor = documentSourceFor,
        shareUriFor = shareUriFor,
        displayNameFor = displayNameFor
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

        val result = viewModel.loadThumbnail(doc.id, target)

        assertEquals(image, result)
    }

    @Test
    fun `loadThumbnail returns null when the repository fails`() = runTest {
        val doc = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        val result = viewModel.loadThumbnail(doc.id, target)

        assertNull(result)
    }

    @Test
    fun `loadThumbnail forwards the caller's target size to the repository, not a fixed size`() = runTest {
        val doc = document("today.pdf", fixedNow)
        val image = PageImage(bytes = byteArrayOf(1, 2, 3), width = 10, height = 10)
        val repository = FakeDocumentRepository(listOf(doc)).apply {
            thumbnails = mapOf(doc.id to Result.Success(image))
        }
        val viewModel = newViewModel(repository)
        val requestedSize = PixelSize(widthPx = 76, heightPx = 104)

        viewModel.loadThumbnail(doc.id, requestedSize)

        assertEquals(listOf(doc.id to requestedSize), repository.getThumbnailCalls)
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

    // --- onImportSelected ---

    @Test
    fun `onImportSelected adds the picked pdf keeping the resolved display name and refreshes the list`() = runTest {
        val repository = FakeDocumentRepository(emptyList())
        var receivedUri: Uri? = null
        val viewModel = newViewModel(
            repository,
            displayNameFor = { uri ->
                receivedUri = uri
                "contrato.pdf"
            }
        )
        val pickedUri = Uri.parse("content://picker-result")

        viewModel.onImportSelected(pickedUri)
        runCurrent()

        assertEquals(pickedUri, receivedUri)
        assertEquals(1, repository.addDocumentCalls.size)
        assertEquals("contrato.pdf", repository.addDocumentCalls.single().second)
        val todaySection = viewModel.uiState.value.sections.single { it.label == SectionLabel.TODAY }
        assertEquals(listOf("Spore_20261011_1430.pdf"), todaySection.rows.map { it.fileName })
    }

    @Test
    fun `onImportSelected places the imported document at the top of TODAY among existing documents`() = runTest {
        val existing = document("older_today.pdf", fixedNow.minusSeconds(3600))
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(repository, displayNameFor = { "contrato.pdf" })

        viewModel.onImportSelected(Uri.parse("content://picker-result"))
        runCurrent()

        val todaySection = viewModel.uiState.value.sections.single { it.label == SectionLabel.TODAY }
        assertEquals(listOf("Spore_20261011_1430.pdf", "older_today.pdf"), todaySection.rows.map { it.fileName })
    }

    @Test
    fun `onImportSelected emits ShowImportError and leaves the list unchanged when addDocument fails`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing)).apply {
            addDocumentResult = Result.Failure(DocumentError.UnreadableFile(DocumentId("x")))
        }
        val viewModel = newViewModel(repository, displayNameFor = { "contrato.pdf" })
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onImportSelected(Uri.parse("content://picker-result"))
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowImportError), effects)
        assertEquals(listOf("today.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        job.cancel()
    }

    @Test
    fun `onImportSelected emits ShowImportError and leaves the list unchanged when reading the uri throws`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(
            repository,
            documentSourceFor = { throw java.io.IOException("resolver returned no stream") },
            displayNameFor = { "contrato.pdf" }
        )
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onImportSelected(Uri.parse("content://picker-result"))
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowImportError), effects)
        assertEquals(listOf("today.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        assertTrue(repository.addDocumentCalls.isEmpty())
        job.cancel()
    }

    // --- onExternalDocumentReceived (SD-12) ---

    @Test
    fun `onExternalDocumentReceived adds the document keeping the resolved display name and opens it in the viewer`() = runTest {
        val repository = FakeDocumentRepository(emptyList())
        var receivedUriForSource: Uri? = null
        var receivedUriForName: Uri? = null
        val viewModel = newViewModel(
            repository,
            documentSourceFor = { uri ->
                receivedUriForSource = uri
                FakeDocumentSource("PDF-OK:4")
            },
            displayNameFor = { uri ->
                receivedUriForName = uri
                "fatura.pdf"
            }
        )
        val externalUri = Uri.parse("content://external-pdf")
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onExternalDocumentReceived(externalUri)
        runCurrent()

        assertEquals(externalUri, receivedUriForSource)
        assertEquals(externalUri, receivedUriForName)
        assertEquals(1, repository.addDocumentCalls.size)
        assertEquals("fatura.pdf", repository.addDocumentCalls.single().second)
        val todaySection = viewModel.uiState.value.sections.single { it.label == SectionLabel.TODAY }
        assertEquals(listOf("Spore_20261011_1430.pdf"), todaySection.rows.map { it.fileName })
        assertEquals(
            listOf(DocumentsEffect.OpenDocument(DocumentId("Spore_20261011_1430.pdf"), "Spore_20261011_1430.pdf")),
            effects
        )
        job.cancel()
    }

    @Test
    fun `onExternalDocumentReceived places the added document at the top of TODAY among existing documents`() = runTest {
        val existing = document("older_today.pdf", fixedNow.minusSeconds(3600))
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(repository, displayNameFor = { "fatura.pdf" })

        viewModel.onExternalDocumentReceived(Uri.parse("content://external-pdf"))
        runCurrent()

        val todaySection = viewModel.uiState.value.sections.single { it.label == SectionLabel.TODAY }
        assertEquals(listOf("Spore_20261011_1430.pdf", "older_today.pdf"), todaySection.rows.map { it.fileName })
    }

    @Test
    fun `onExternalDocumentReceived passes a null requested name through when no display name is resolved`() = runTest {
        val repository = FakeDocumentRepository(emptyList())
        val viewModel = newViewModel(repository, displayNameFor = { null })

        viewModel.onExternalDocumentReceived(Uri.parse("file:///storage/emulated/0/unnamed"))
        runCurrent()

        assertEquals(1, repository.addDocumentCalls.size)
        assertNull(repository.addDocumentCalls.single().second)
    }

    @Test
    fun `onExternalDocumentReceived emits ShowImportError and leaves the list unchanged when addDocument fails`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing)).apply {
            addDocumentResult = Result.Failure(DocumentError.UnreadableFile(DocumentId("x")))
        }
        val viewModel = newViewModel(repository, displayNameFor = { "fatura.pdf" })
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onExternalDocumentReceived(Uri.parse("content://external-pdf"))
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowImportError), effects)
        assertEquals(listOf("today.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        job.cancel()
    }

    @Test
    fun `onExternalDocumentReceived emits ShowImportError and leaves the list unchanged when reading the uri throws`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(
            repository,
            documentSourceFor = { throw java.io.IOException("resolver returned no stream") },
            displayNameFor = { "fatura.pdf" }
        )
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onExternalDocumentReceived(Uri.parse("content://external-pdf"))
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowImportError), effects)
        assertEquals(listOf("today.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        assertTrue(repository.addDocumentCalls.isEmpty())
        job.cancel()
    }

    @Test
    fun `onExternalDocumentReceived emits ShowImportError and leaves the list unchanged when resolving the display name throws`() = runTest {
        val existing = document("today.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(existing))
        val viewModel = newViewModel(
            repository,
            displayNameFor = { throw java.io.IOException("resolver returned no cursor") }
        )
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onExternalDocumentReceived(Uri.parse("content://external-pdf"))
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowImportError), effects)
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

    // --- onShareRequested ---

    @Test
    fun `onShareRequested emits ShareDocument with the uri resolved for this document's id`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val expectedUri = Uri.parse("content://sporedoc/contrato.pdf")
        var requestedId: DocumentId? = null
        val viewModel = newViewModel(repository, shareUriFor = { id ->
            requestedId = id
            expectedUri
        })
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onShareRequested(doc.id)
        runCurrent()

        assertEquals(doc.id, requestedId)
        assertEquals(listOf(DocumentsEffect.ShareDocument(expectedUri)), effects)
        job.cancel()
    }

    // --- rename dialog: open/cancel ---

    @Test
    fun `onRenameRequested opens the rename dialog with the extension stripped from the file name`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onRenameRequested(doc.id, doc.fileName)

        val dialog = viewModel.uiState.value.renameDialog
        assertEquals(doc.id, dialog?.documentId)
        assertEquals("contrato", dialog?.initialName)
        assertNull(dialog?.error)
    }

    @Test
    fun `onRenameCancelled closes the rename dialog and renames nothing`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onRenameRequested(doc.id, doc.fileName)

        viewModel.onRenameCancelled()

        assertNull(viewModel.uiState.value.renameDialog)
        assertTrue(repository.renameDocumentCalls.isEmpty())
        assertEquals(listOf("contrato.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
    }

    // --- rename dialog: confirm ---

    @Test
    fun `onRenameConfirmed with a valid new name closes the dialog and the list shows the new name`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onRenameRequested(doc.id, doc.fileName)

        viewModel.onRenameConfirmed("acordo")
        runCurrent()

        assertNull(viewModel.uiState.value.renameDialog)
        assertEquals(listOf("acordo.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
    }

    @Test
    fun `onRenameConfirmed with an empty name keeps the dialog open with InvalidName and renames nothing`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onRenameRequested(doc.id, doc.fileName)

        viewModel.onRenameConfirmed("")
        runCurrent()

        val dialog = viewModel.uiState.value.renameDialog
        assertEquals(RenameDialogError.InvalidName, dialog?.error)
        assertEquals(listOf("contrato.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
    }

    @Test
    fun `onRenameConfirmed with a name made only of spaces keeps the dialog open with InvalidName`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onRenameRequested(doc.id, doc.fileName)

        viewModel.onRenameConfirmed("   ")
        runCurrent()

        val dialog = viewModel.uiState.value.renameDialog
        assertEquals(RenameDialogError.InvalidName, dialog?.error)
        assertEquals(listOf("contrato.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
    }

    @Test
    fun `onRenameConfirmed with a colliding name keeps the dialog open with a distinct NameCollision error`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(a, b))
        val viewModel = newViewModel(repository)
        viewModel.onRenameRequested(a.id, a.fileName)

        viewModel.onRenameConfirmed("b")
        runCurrent()

        val dialog = viewModel.uiState.value.renameDialog
        assertEquals(RenameDialogError.NameCollision, dialog?.error)
        assertEquals(
            listOf("a.pdf", "b.pdf").sorted(),
            viewModel.uiState.value.sections.single().rows.map { it.fileName }.sorted()
        )
    }

    @Test
    fun `onRenameConfirmed closes the dialog, emits ShowRenameError and leaves the list unchanged on an unexpected failure`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc)).apply {
            renameDocumentResult = Result.Failure(DocumentError.FileMissing(doc.id))
        }
        val viewModel = newViewModel(repository)
        viewModel.onRenameRequested(doc.id, doc.fileName)
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onRenameConfirmed("acordo")
        runCurrent()

        assertNull(viewModel.uiState.value.renameDialog)
        assertEquals(listOf(DocumentsEffect.ShowRenameError), effects)
        assertEquals(listOf("contrato.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
        job.cancel()
    }

    @Test
    fun `a renamed document keeps its section, position and meta line because modifiedAt is untouched`() = runTest {
        val recentModifiedAt =
            ZonedDateTime.of(2026, 10, 8, 14, 30, 0, 0, ZoneId.systemDefault()).toInstant()
        val renamedDoc = document("recent.pdf", recentModifiedAt)
        val older = document("older_recent.pdf", recentModifiedAt.minusSeconds(3600))
        val repository = FakeDocumentRepository(listOf(renamedDoc, older))
        val viewModel = newViewModel(repository)
        viewModel.onRenameRequested(renamedDoc.id, renamedDoc.fileName)

        viewModel.onRenameConfirmed("relatorio")
        runCurrent()

        val recentSection = viewModel.uiState.value.sections.single { it.label == SectionLabel.RECENT_DOCUMENTS }
        assertEquals(listOf("relatorio.pdf", "older_recent.pdf"), recentSection.rows.map { it.fileName })
        assertTrue(recentSection.rows.first().metaLine.contains("08.10.2026"))
    }

    // --- delete flow ---

    @Test
    fun `onDeleteRequested sets pendingDeleteId without deleting anything`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onDeleteRequested(doc.id)

        assertEquals(doc.id, viewModel.uiState.value.pendingDeleteId)
        assertTrue(repository.deleteDocumentCalls.isEmpty())
    }

    @Test
    fun `onDeleteCancelled clears pendingDeleteId and deletes nothing`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onDeleteRequested(doc.id)

        viewModel.onDeleteCancelled()

        assertNull(viewModel.uiState.value.pendingDeleteId)
        assertTrue(repository.deleteDocumentCalls.isEmpty())
        assertEquals(listOf("contrato.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
    }

    @Test
    fun `onDeleteConfirmed deletes the document, clears pendingDeleteId and the list no longer shows it`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onDeleteRequested(doc.id)

        viewModel.onDeleteConfirmed()
        runCurrent()

        assertEquals(listOf(doc.id), repository.deleteDocumentCalls)
        assertNull(viewModel.uiState.value.pendingDeleteId)
        assertTrue(viewModel.uiState.value.sections.isEmpty())
    }

    @Test
    fun `onDeleteConfirmed drops the section header when the deleted document was its only one`() = runTest {
        val onlyInToday = document("today.pdf", fixedNow)
        val recent = document("recent.pdf", fixedNow.minusSeconds(60L * 60 * 24 * 10))
        val repository = FakeDocumentRepository(listOf(onlyInToday, recent))
        val viewModel = newViewModel(repository)
        viewModel.onDeleteRequested(onlyInToday.id)

        viewModel.onDeleteConfirmed()
        runCurrent()

        val sections = viewModel.uiState.value.sections
        assertEquals(listOf(SectionLabel.RECENT_DOCUMENTS), sections.map { it.label })
    }

    @Test
    fun `onDeleteConfirmed on a document already missing on disk reloads the list without it and emits ShowDeleteError`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc)).apply {
            deleteDocumentResult = Result.Failure(DocumentError.FileMissing(doc.id))
        }
        val viewModel = newViewModel(repository)
        viewModel.onDeleteRequested(doc.id)
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()
        // simulate the file having actually disappeared from disk, independent of the stubbed result
        repository.removeDocument(doc.id)

        viewModel.onDeleteConfirmed()
        runCurrent()

        assertNull(viewModel.uiState.value.pendingDeleteId)
        assertEquals(listOf(DocumentsEffect.ShowDeleteError), effects)
        assertTrue(viewModel.uiState.value.sections.isEmpty())
        job.cancel()
    }

    // --- selection mode (SD-17) ---

    @Test
    fun `initial state is not in selection mode`() = runTest {
        val repository = FakeDocumentRepository(listOf(document("contrato.pdf", fixedNow)))
        val viewModel = newViewModel(repository)

        assertNull(viewModel.uiState.value.selectionMode)
    }

    @Test
    fun `onSelectModeEntered enters selection mode with no row selected and a 0 counter`() = runTest {
        val repository = FakeDocumentRepository(listOf(document("contrato.pdf", fixedNow)))
        val viewModel = newViewModel(repository)

        viewModel.onSelectModeEntered()

        val selectionMode = viewModel.uiState.value.selectionMode
        assertEquals(emptyList<DocumentId>(), selectionMode?.selectedIds)
        assertEquals("0", selectionMode?.counterText)
        assertEquals(0, selectionMode?.summary?.documentCount)
    }

    @Test
    fun `onRowLongPressed outside selection mode enters it with that row already selected`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onRowLongPressed(doc.id)

        val selectionMode = viewModel.uiState.value.selectionMode
        assertEquals(listOf(doc.id), selectionMode?.selectedIds)
        assertEquals("01", selectionMode?.counterText)
    }

    @Test
    fun `onRowLongPressed is a no-op when already in selection mode`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(a, b))
        val viewModel = newViewModel(repository)
        viewModel.onRowLongPressed(a.id)

        viewModel.onRowLongPressed(b.id)

        assertEquals(listOf(a.id), viewModel.uiState.value.selectionMode?.selectedIds)
    }

    @Test
    fun `onRowToggled is a no-op when not in selection mode`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onRowToggled(doc.id)

        assertNull(viewModel.uiState.value.selectionMode)
    }

    @Test
    fun `onRowToggled selects an unselected row and deselects it on a second toggle`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()

        viewModel.onRowToggled(doc.id)
        assertEquals(listOf(doc.id), viewModel.uiState.value.selectionMode?.selectedIds)
        assertEquals("01", viewModel.uiState.value.selectionMode?.counterText)

        viewModel.onRowToggled(doc.id)
        assertEquals(emptyList<DocumentId>(), viewModel.uiState.value.selectionMode?.selectedIds)
        assertEquals("0", viewModel.uiState.value.selectionMode?.counterText)
    }

    @Test
    fun `deselecting the last selected row one by one keeps selection mode active with 0 SELECTED`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val c = document("c.pdf", fixedNow.minusSeconds(120))
        val repository = FakeDocumentRepository(listOf(a, b, c))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onRowToggled(a.id)
        viewModel.onRowToggled(b.id)
        viewModel.onRowToggled(c.id)
        assertEquals("03", viewModel.uiState.value.selectionMode?.counterText)

        viewModel.onRowToggled(a.id)
        viewModel.onRowToggled(b.id)
        viewModel.onRowToggled(c.id)

        val selectionMode = viewModel.uiState.value.selectionMode
        assertEquals(emptyList<DocumentId>(), selectionMode?.selectedIds)
        assertEquals("0", selectionMode?.counterText)
        assertTrue(selectionMode != null)
    }

    @Test
    fun `onSelectAllToggled is a no-op when not in selection mode`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onSelectAllToggled()

        assertNull(viewModel.uiState.value.selectionMode)
    }

    @Test
    fun `onSelectAllToggled selects all documents when none are selected`() = runTest {
        val documents = (1..11).map { document("doc$it.pdf", fixedNow.minusSeconds(it.toLong())) }
        val repository = FakeDocumentRepository(documents)
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()

        viewModel.onSelectAllToggled()

        val selectionMode = viewModel.uiState.value.selectionMode
        assertEquals(documents.map { it.id }.toSet(), selectionMode?.selectedIds?.toSet())
        assertEquals(documents.size, selectionMode?.selectedIds?.distinct()?.size)
        assertEquals("11", selectionMode?.counterText)
    }

    @Test
    fun `onSelectAllToggled deselects all when every document is already selected, staying in selection mode`() = runTest {
        val documents = (1..11).map { document("doc$it.pdf", fixedNow.minusSeconds(it.toLong())) }
        val repository = FakeDocumentRepository(documents)
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onSelectAllToggled()

        viewModel.onSelectAllToggled()

        val selectionMode = viewModel.uiState.value.selectionMode
        assertTrue(selectionMode != null)
        assertEquals(emptyList<DocumentId>(), selectionMode?.selectedIds)
        assertEquals("0", selectionMode?.counterText)
    }

    @Test
    fun `onSelectionCleared exits selection mode and clears the selection`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(a, b))
        val viewModel = newViewModel(repository)
        viewModel.onSelectAllToggled()
        viewModel.onRowLongPressed(a.id)
        viewModel.onRowToggled(b.id)

        viewModel.onSelectionCleared()

        assertNull(viewModel.uiState.value.selectionMode)
    }

    @Test
    fun `the selection summary reflects only the selected documents' totals`() = runTest {
        val selectedA = document("a.pdf", fixedNow, pageCount = 3, sizeBytes = 2_000_000L)
        val selectedB = document("b.pdf", fixedNow.minusSeconds(60), pageCount = 4, sizeBytes = 3_000_000L)
        val selectedC = document("c.pdf", fixedNow.minusSeconds(120), pageCount = 4, sizeBytes = 2_654_605L)
        val notSelected = document("d.pdf", fixedNow.minusSeconds(180), pageCount = 50, sizeBytes = 50_000_000L)
        val repository = FakeDocumentRepository(listOf(selectedA, selectedB, selectedC, notSelected))
        val viewModel = newViewModel(repository)
        viewModel.onRowLongPressed(selectedA.id)
        viewModel.onRowToggled(selectedB.id)
        viewModel.onRowToggled(selectedC.id)

        val summary = viewModel.uiState.value.selectionMode?.summary

        assertEquals(3, summary?.documentCount)
        assertEquals(11, summary?.totalPages)
        assertEquals("7.3", summary?.totalSizeMb)
    }

    // --- batch share/purge (SD-18) ---

    @Test
    fun `onShareSelectedRequested emits ShareSelectedDocuments with the uris of every selected document`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val c = document("c.pdf", fixedNow.minusSeconds(120))
        val repository = FakeDocumentRepository(listOf(a, b, c))
        val uriFor = mapOf(
            a.id to Uri.parse("content://sporedoc/a.pdf"),
            b.id to Uri.parse("content://sporedoc/b.pdf"),
            c.id to Uri.parse("content://sporedoc/c.pdf")
        )
        val viewModel = newViewModel(repository, shareUriFor = { id -> uriFor.getValue(id) })
        viewModel.onSelectModeEntered()
        viewModel.onSelectAllToggled()
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onShareSelectedRequested()
        runCurrent()

        val shareEffect = effects.filterIsInstance<DocumentsEffect.ShareSelectedDocuments>().singleOrNull()
        assertEquals(uriFor.values.toSet(), shareEffect?.uris?.toSet())
        assertEquals(3, shareEffect?.uris?.size)
        job.cancel()
    }

    @Test
    fun `onShareSelectedRequested exits selection mode right after sending the effect`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(a, b))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onSelectAllToggled()

        viewModel.onShareSelectedRequested()
        runCurrent()

        assertNull(viewModel.uiState.value.selectionMode)
    }

    @Test
    fun `onShareSelectedRequested with 0 selected documents has no effect and stays in selection mode`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onShareSelectedRequested()
        runCurrent()

        assertTrue(effects.isEmpty())
        assertEquals(emptyList<DocumentId>(), viewModel.uiState.value.selectionMode?.selectedIds)
        job.cancel()
    }

    @Test
    fun `onShareSelectedRequested outside selection mode has no effect`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onShareSelectedRequested()
        runCurrent()

        assertTrue(effects.isEmpty())
        assertNull(viewModel.uiState.value.selectionMode)
        job.cancel()
    }

    @Test
    fun `onPurgeRequested with selected documents shows the confirmation dialog without deleting anything`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val c = document("c.pdf", fixedNow.minusSeconds(120))
        val repository = FakeDocumentRepository(listOf(a, b, c))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onSelectAllToggled()

        viewModel.onPurgeRequested()

        assertTrue(viewModel.uiState.value.isPurgeConfirmationVisible)
        assertTrue(repository.deleteDocumentCalls.isEmpty())
    }

    @Test
    fun `onPurgeRequested with 0 selected documents does not show the confirmation dialog`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()

        viewModel.onPurgeRequested()

        assertTrue(!viewModel.uiState.value.isPurgeConfirmationVisible)
    }

    @Test
    fun `onPurgeRequested outside selection mode does not show the confirmation dialog`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onPurgeRequested()

        assertTrue(!viewModel.uiState.value.isPurgeConfirmationVisible)
    }

    @Test
    fun `onPurgeCancelled hides the dialog, deletes nothing and keeps the selection as it was`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(a, b))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onSelectAllToggled()
        viewModel.onPurgeRequested()

        viewModel.onPurgeCancelled()

        assertTrue(!viewModel.uiState.value.isPurgeConfirmationVisible)
        assertTrue(repository.deleteDocumentCalls.isEmpty())
        assertEquals(setOf(a.id, b.id), viewModel.uiState.value.selectionMode?.selectedIds?.toSet())
        assertEquals(
            listOf("a.pdf", "b.pdf").sorted(),
            viewModel.uiState.value.sections.single().rows.map { it.fileName }.sorted()
        )
    }

    @Test
    fun `onPurgeConfirmed deletes every selected document, closes the dialog and exits selection mode`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val c = document("c.pdf", fixedNow.minusSeconds(120))
        val repository = FakeDocumentRepository(listOf(a, b, c))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onSelectAllToggled()
        viewModel.onPurgeRequested()

        viewModel.onPurgeConfirmed()
        runCurrent()

        assertEquals(setOf(a.id, b.id, c.id), repository.deleteDocumentCalls.toSet())
        assertTrue(!viewModel.uiState.value.isPurgeConfirmationVisible)
        assertNull(viewModel.uiState.value.selectionMode)
        assertTrue(viewModel.uiState.value.sections.isEmpty())
    }

    @Test
    fun `onPurgeConfirmed with a partial failure shows an error, reloads the list and still deletes the rest`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val missing = document("missing.pdf", fixedNow.minusSeconds(120))
        val repository = FakeDocumentRepository(listOf(a, b, missing)).apply {
            deleteDocumentResultsById = mapOf(missing.id to Result.Failure(DocumentError.FileMissing(missing.id)))
        }
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onSelectAllToggled()
        viewModel.onPurgeRequested()
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()
        // the file is actually gone from disk already, independent of the stubbed result
        repository.removeDocument(missing.id)

        viewModel.onPurgeConfirmed()
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowDeleteError), effects)
        assertEquals(setOf(a.id, b.id, missing.id), repository.deleteDocumentCalls.toSet())
        assertTrue(viewModel.uiState.value.sections.isEmpty())
        assertNull(viewModel.uiState.value.selectionMode)
        assertTrue(!viewModel.uiState.value.isPurgeConfirmationVisible)
        job.cancel()
    }

    @Test
    fun `onPurgeConfirmed with nothing selected is a no-op`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onPurgeConfirmed()
        runCurrent()

        assertTrue(repository.deleteDocumentCalls.isEmpty())
        assertEquals(listOf("contrato.pdf"), viewModel.uiState.value.sections.single().rows.map { it.fileName })
    }

    // --- selection order (SD-19 R1/AC9) ---

    @Test
    fun `selecting c then a preserves tap order regardless of each row's position in the list`() = runTest {
        val a = document("a.pdf", fixedNow)
        val c = document("c.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(a, c))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()

        viewModel.onRowToggled(c.id)
        viewModel.onRowToggled(a.id)

        assertEquals(listOf(c.id, a.id), viewModel.uiState.value.selectionMode?.selectedIds)
    }

    @Test
    fun `deselecting a row and reselecting it re-appends it at the end instead of its old position`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val c = document("c.pdf", fixedNow.minusSeconds(120))
        val repository = FakeDocumentRepository(listOf(a, b, c))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onRowToggled(a.id)
        viewModel.onRowToggled(b.id)
        viewModel.onRowToggled(c.id)

        viewModel.onRowToggled(b.id) // deselect
        viewModel.onRowToggled(b.id) // reselect

        assertEquals(listOf(a.id, c.id, b.id), viewModel.uiState.value.selectionMode?.selectedIds)
    }

    @Test
    fun `selectedIds never contains a duplicate id`() = runTest {
        val a = document("a.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(a))
        val viewModel = newViewModel(repository)
        viewModel.onRowLongPressed(a.id)

        val selectedIds = viewModel.uiState.value.selectionMode?.selectedIds
        assertEquals(selectedIds?.distinct(), selectedIds)
    }

    // --- onMergeRequested (SD-19 R6-R9) ---

    @Test
    fun `onMergeRequested with 2 selected documents merges them in selection order and exits selection mode`() = runTest {
        val b = document("b.pdf", fixedNow, pageCount = 2)
        val a = document("a.pdf", fixedNow.minusSeconds(60), pageCount = 1)
        val repository = FakeDocumentRepository(listOf(b, a))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onRowToggled(b.id)
        viewModel.onRowToggled(a.id)

        viewModel.onMergeRequested()
        runCurrent()

        assertEquals(listOf(listOf(b.id, a.id)), repository.mergeDocumentsCalls)
        assertNull(viewModel.uiState.value.selectionMode)
    }

    @Test
    fun `onMergeRequested with 3 selected documents merges all three in selection order`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val c = document("c.pdf", fixedNow.minusSeconds(120))
        val repository = FakeDocumentRepository(listOf(a, b, c))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onRowToggled(c.id)
        viewModel.onRowToggled(a.id)
        viewModel.onRowToggled(b.id)

        viewModel.onMergeRequested()
        runCurrent()

        assertEquals(listOf(listOf(c.id, a.id, b.id)), repository.mergeDocumentsCalls)
    }

    @Test
    fun `onMergeRequested after success shows the merged pdf in TODAY and the originals are gone`() = runTest {
        val b = document("b.pdf", fixedNow)
        val a = document("a.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(b, a))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onRowToggled(b.id)
        viewModel.onRowToggled(a.id)

        viewModel.onMergeRequested()
        runCurrent()

        val todaySection = viewModel.uiState.value.sections.single { it.label == SectionLabel.TODAY }
        assertEquals(listOf("Spore_20261011_1430.pdf"), todaySection.rows.map { it.fileName })
        assertTrue(viewModel.uiState.value.sections.none { section ->
            section.rows.any { it.fileName == "a.pdf" || it.fileName == "b.pdf" }
        })
    }

    @Test
    fun `onMergeRequested with 1 selected document is a no-op`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onRowToggled(doc.id)

        viewModel.onMergeRequested()
        runCurrent()

        assertTrue(repository.mergeDocumentsCalls.isEmpty())
        assertEquals(listOf(doc.id), viewModel.uiState.value.selectionMode?.selectedIds)
    }

    @Test
    fun `onMergeRequested with 0 selected documents is a no-op`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()

        viewModel.onMergeRequested()
        runCurrent()

        assertTrue(repository.mergeDocumentsCalls.isEmpty())
        assertEquals(emptyList<DocumentId>(), viewModel.uiState.value.selectionMode?.selectedIds)
    }

    @Test
    fun `onMergeRequested outside selection mode is a no-op`() = runTest {
        val doc = document("contrato.pdf", fixedNow)
        val repository = FakeDocumentRepository(listOf(doc))
        val viewModel = newViewModel(repository)

        viewModel.onMergeRequested()
        runCurrent()

        assertTrue(repository.mergeDocumentsCalls.isEmpty())
        assertNull(viewModel.uiState.value.selectionMode)
    }

    @Test
    fun `onMergeRequested on failure shows ShowMergeError and leaves selection mode and the selection untouched`() = runTest {
        val a = document("a.pdf", fixedNow)
        val b = document("b.pdf", fixedNow.minusSeconds(60))
        val repository = FakeDocumentRepository(listOf(a, b)).apply {
            mergeDocumentsResult = Result.Failure(DocumentError.FileMissing(a.id))
        }
        val viewModel = newViewModel(repository)
        viewModel.onSelectModeEntered()
        viewModel.onRowToggled(a.id)
        viewModel.onRowToggled(b.id)
        val effects = mutableListOf<DocumentsEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        runCurrent()

        viewModel.onMergeRequested()
        runCurrent()

        assertEquals(listOf(DocumentsEffect.ShowMergeError), effects)
        assertEquals(listOf(a.id, b.id), viewModel.uiState.value.selectionMode?.selectedIds)
        assertEquals(
            listOf("a.pdf", "b.pdf").sorted(),
            viewModel.uiState.value.sections.single().rows.map { it.fileName }.sorted()
        )
        job.cancel()
    }
}
