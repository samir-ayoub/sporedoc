package com.knowledge.sporedoc.data.repository

import com.knowledge.sporedoc.data.file.FakePdfFileAccess
import com.knowledge.sporedoc.data.file.validPdfContent
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class FileDocumentRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val fixedNow: Instant =
        ZonedDateTime.of(2026, 10, 11, 14, 30, 0, 0, ZoneId.systemDefault()).toInstant()

    private fun newRepository(clock: () -> Instant = { fixedNow }): FileDocumentRepository =
        FileDocumentRepository(
            documentsDir = tempFolder.root,
            pdfFileAccess = FakePdfFileAccess(),
            clock = clock
        )

    private fun writeRawFile(name: String, content: String, lastModifiedMillis: Long? = null): File {
        val file = File(tempFolder.root, name)
        file.writeText(content)
        if (lastModifiedMillis != null) {
            file.setLastModified(lastModifiedMillis)
        }
        return file
    }

    private fun <T, E> Result<T, E>.asSuccess(): T {
        assertTrue("expected Success but was $this", this is Result.Success)
        return (this as Result.Success).value
    }

    private fun <T, E> Result<T, E>.asFailure(): E {
        assertTrue("expected Failure but was $this", this is Result.Failure)
        return (this as Result.Failure).error
    }

    // --- listDocuments ---

    @Test
    fun `listDocuments returns all documents from most recent to oldest with full metadata`() = runTest {
        val repository = newRepository()
        val oldest = writeRawFile("oldest.pdf", validPdfContent(2), lastModifiedMillis = 1_000_000_000_000L)
        val middle = writeRawFile("middle.pdf", validPdfContent(3), lastModifiedMillis = 1_200_000_000_000L)
        val newest = writeRawFile("newest.pdf", validPdfContent(5), lastModifiedMillis = 1_400_000_000_000L)

        val documents = repository.listDocuments().asSuccess()

        assertEquals(listOf("newest.pdf", "middle.pdf", "oldest.pdf"), documents.map { it.fileName })
        val byName = documents.associateBy { it.fileName }
        assertEquals(2, byName["oldest.pdf"]!!.pageCount)
        assertEquals(3, byName["middle.pdf"]!!.pageCount)
        assertEquals(5, byName["newest.pdf"]!!.pageCount)
        assertEquals(oldest.length(), byName["oldest.pdf"]!!.sizeBytes)
        assertEquals(middle.length(), byName["middle.pdf"]!!.sizeBytes)
        assertEquals(newest.length(), byName["newest.pdf"]!!.sizeBytes)
        assertEquals(oldest.lastModified() / 1000, byName["oldest.pdf"]!!.modifiedAt.epochSecond)
        assertEquals(newest.lastModified() / 1000, byName["newest.pdf"]!!.modifiedAt.epochSecond)
    }

    @Test
    fun `listDocuments on an empty library returns an empty list without error`() = runTest {
        val repository = newRepository()

        val documents = repository.listDocuments().asSuccess()

        assertTrue(documents.isEmpty())
    }

    // --- page count / page image / thumbnail ---

    @Test
    fun `getPageCount returns six for a six-page pdf`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(6)), requestedName = null).asSuccess()

        val pageCount = repository.getPageCount(added.id).asSuccess()

        assertEquals(6, pageCount)
    }

    @Test
    fun `getPageImage returns a non-empty image for a valid page index`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(3)), requestedName = null).asSuccess()

        val image = repository.getPageImage(added.id, 2).asSuccess()

        assertTrue(image.bytes.isNotEmpty())
    }

    @Test
    fun `getPageImage returns out-of-bounds error for an index past the last page`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(3)), requestedName = null).asSuccess()

        val error = repository.getPageImage(added.id, 3).asFailure()

        assertTrue(error is DocumentError.PageIndexOutOfBounds)
        error as DocumentError.PageIndexOutOfBounds
        assertEquals(3, error.index)
        assertEquals(3, error.pageCount)
    }

    @Test
    fun `getThumbnail returns the first page image`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(4)), requestedName = null).asSuccess()

        val thumbnail = repository.getThumbnail(added.id).asSuccess()
        val firstPage = repository.getPageImage(added.id, 0).asSuccess()

        assertTrue(thumbnail.bytes.contentEquals(firstPage.bytes))
    }

    @Test
    fun `getPageCount returns an unreadable-file error for a corrupted pdf`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()
        File(tempFolder.root, added.fileName).writeText("NOT_A_PDF")

        val error = repository.getPageCount(added.id).asFailure()

        assertTrue(error is DocumentError.UnreadableFile)
    }

    @Test
    fun `getPageImage returns an unreadable-file error for a corrupted pdf`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()
        File(tempFolder.root, added.fileName).writeText("NOT_A_PDF")

        val error = repository.getPageImage(added.id, 0).asFailure()

        assertTrue(error is DocumentError.UnreadableFile)
    }

    // --- addDocument naming ---

    @Test
    fun `addDocument without requested name uses the Spore pattern with the current time`() = runTest {
        val repository = newRepository()

        val document = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()

        assertEquals("Spore_20261011_1430.pdf", document.fileName)
        assertTrue(File(tempFolder.root, "Spore_20261011_1430.pdf").exists())
    }

    @Test
    fun `addDocument without requested name appends _2 when the generated name collides`() = runTest {
        val repository = newRepository()
        repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()

        val second = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()

        assertEquals("Spore_20261011_1430_2.pdf", second.fileName)
    }

    @Test
    fun `addDocument without requested name appends _3 when _2 also collides`() = runTest {
        val repository = newRepository()
        repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()
        repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()

        val third = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = null).asSuccess()

        assertEquals("Spore_20261011_1430_3.pdf", third.fileName)
    }

    @Test
    fun `addDocument with requested name keeps that name when it is free`() = runTest {
        val repository = newRepository()

        val document = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        assertEquals("contrato.pdf", document.fileName)
    }

    @Test
    fun `addDocument with requested name appends _2 when it collides, keeping the original file intact`() = runTest {
        val repository = newRepository()
        repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        val second = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        assertEquals("contrato_2.pdf", second.fileName)
        assertEquals(validPdfContent(1), File(tempFolder.root, "contrato.pdf").readText())
    }

    @Test
    fun `addDocument returns an unreadable-file error and leaves no partial file when the content is not a valid pdf`() = runTest {
        val repository = newRepository()

        val error = repository.addDocument(FakeDocumentSource("NOT_A_PDF"), requestedName = "contrato.pdf").asFailure()

        assertTrue(error is DocumentError.UnreadableFile)
        assertFalse(File(tempFolder.root, "contrato.pdf").exists())
    }

    @Test
    fun `addDocument with an unreadable file leaves an existing document with the same requested name intact`() = runTest {
        val repository = newRepository()
        repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        val error = repository.addDocument(FakeDocumentSource("NOT_A_PDF"), requestedName = "contrato.pdf").asFailure()

        assertTrue(error is DocumentError.UnreadableFile)
        assertEquals(validPdfContent(1), File(tempFolder.root, "contrato.pdf").readText())
    }

    @Test
    fun `addDocument sets the copied file's lastModified from the injected clock, not the file system time`() = runTest {
        val repository = newRepository()

        repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        val file = File(tempFolder.root, "contrato.pdf")
        assertEquals(fixedNow.epochSecond, file.lastModified() / 1000)
    }

    @Test
    fun `addDocument returns a document whose modifiedAt matches the injected clock`() = runTest {
        val repository = newRepository()

        val document = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        assertEquals(fixedNow.epochSecond, document.modifiedAt.epochSecond)
    }

    // --- renameDocument ---

    @Test
    fun `renameDocument adds the pdf extension when the new name omits it`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        val renamed = repository.renameDocument(added.id, "acordo").asSuccess()

        assertEquals("acordo.pdf", renamed.fileName)
        assertFalse(File(tempFolder.root, "contrato.pdf").exists())
        assertTrue(File(tempFolder.root, "acordo.pdf").exists())
    }

    @Test
    fun `renameDocument does not duplicate the pdf extension when the new name already has it`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        val renamed = repository.renameDocument(added.id, "acordo.pdf").asSuccess()

        assertEquals("acordo.pdf", renamed.fileName)
    }

    @Test
    fun `renameDocument rejects an empty name and keeps the original file`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        val error = repository.renameDocument(added.id, "").asFailure()

        assertTrue(error is DocumentError.InvalidName)
        assertTrue(File(tempFolder.root, "contrato.pdf").exists())
    }

    @Test
    fun `renameDocument rejects a blank name made only of spaces`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        val error = repository.renameDocument(added.id, "   ").asFailure()

        assertTrue(error is DocumentError.InvalidName)
        assertTrue(File(tempFolder.root, "contrato.pdf").exists())
    }

    @Test
    fun `renameDocument rejects a name colliding with another document without touching either file`() = runTest {
        val repository = newRepository()
        val docA = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "a.pdf").asSuccess()
        repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "b.pdf").asSuccess()

        val error = repository.renameDocument(docA.id, "b").asFailure()

        assertTrue(error is DocumentError.NameCollision)
        assertFalse(error is DocumentError.InvalidName)
        assertTrue(File(tempFolder.root, "a.pdf").exists())
        assertTrue(File(tempFolder.root, "b.pdf").exists())
    }

    // --- deleteDocument ---

    @Test
    fun `deleteDocument removes the file so subsequent listings no longer include it`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()

        repository.deleteDocument(added.id).asSuccess()

        assertFalse(File(tempFolder.root, "contrato.pdf").exists())
        val documents = repository.listDocuments().asSuccess()
        assertTrue(documents.none { it.fileName == "contrato.pdf" })
    }

    @Test
    fun `deleteDocument returns a file-missing error when the file is already gone`() = runTest {
        val repository = newRepository()
        val added = repository.addDocument(FakeDocumentSource(validPdfContent(1)), requestedName = "contrato.pdf").asSuccess()
        File(tempFolder.root, "contrato.pdf").delete()

        val error = repository.deleteDocument(added.id).asFailure()

        assertTrue(error is DocumentError.FileMissing)
    }
}
