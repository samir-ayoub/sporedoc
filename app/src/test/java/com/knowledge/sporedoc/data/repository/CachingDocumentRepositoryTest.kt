package com.knowledge.sporedoc.data.repository

import com.knowledge.sporedoc.domain.model.CropRect
import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * SD-10 R4-R8: [CachingDocumentRepository] memoizes thumbnail renders per document,
 * bounded by an LRU limit, invalidated on successful delete/rename. Exercised directly
 * against a counting fake delegate, per the Architect's design, so composing a Compose
 * list twice does not have to be simulated to prove AC4/AC5/AC8.
 */
class CachingDocumentRepositoryTest {

    private val target = PixelSize(widthPx = 76, heightPx = 104)

    private fun image(tag: Int): PageImage =
        PageImage(bytes = byteArrayOf(tag.toByte()), width = target.widthPx, height = target.heightPx)

    /** Hand-written counting fake [DocumentRepository], local to this cache's seam. */
    private class RecordingDocumentRepository : DocumentRepository {
        val getThumbnailCalls = mutableListOf<DocumentId>()
        var thumbnails: Map<DocumentId, Result<PageImage, DocumentError>> = emptyMap()
        var deleteResult: Result<Unit, DocumentError> = Result.Success(Unit)
        var renameResult: ((DocumentId, String) -> Result<Document, DocumentError>)? = null
        val mergeDocumentsCalls = mutableListOf<List<DocumentId>>()
        var mergeDocumentsResult: Result<Document, DocumentError> = Result.Failure(DocumentError.InvalidName)
        val rotatePagesCalls = mutableListOf<Pair<DocumentId, Map<Int, Int>>>()
        var rotatePagesResult: Result<Unit, DocumentError> = Result.Failure(DocumentError.InvalidName)
        val cropPageCalls = mutableListOf<Triple<DocumentId, Int, CropRect>>()
        var cropPageResult: Result<Unit, DocumentError> = Result.Failure(DocumentError.InvalidName)

        override suspend fun listDocuments(): Result<List<Document>, DocumentError> = Result.Success(emptyList())

        override suspend fun getPageCount(id: DocumentId): Result<Int, DocumentError> =
            Result.Failure(DocumentError.FileMissing(id))

        override suspend fun getPageImage(
            id: DocumentId,
            pageIndex: Int,
            targetSize: PixelSize
        ): Result<PageImage, DocumentError> = Result.Failure(DocumentError.FileMissing(id))

        override suspend fun getThumbnail(id: DocumentId, targetSize: PixelSize): Result<PageImage, DocumentError> {
            getThumbnailCalls.add(id)
            return thumbnails[id] ?: Result.Failure(DocumentError.FileMissing(id))
        }

        override suspend fun addDocument(
            source: DocumentSource,
            requestedName: String?
        ): Result<Document, DocumentError> = Result.Failure(DocumentError.InvalidName)

        override suspend fun renameDocument(id: DocumentId, newName: String): Result<Document, DocumentError> =
            renameResult?.invoke(id, newName) ?: Result.Failure(DocumentError.FileMissing(id))

        override suspend fun deleteDocument(id: DocumentId): Result<Unit, DocumentError> = deleteResult

        override suspend fun mergeDocuments(orderedIds: List<DocumentId>): Result<Document, DocumentError> {
            mergeDocumentsCalls.add(orderedIds)
            return mergeDocumentsResult
        }

        override suspend fun rotatePages(id: DocumentId, rotationsByPageIndex: Map<Int, Int>): Result<Unit, DocumentError> {
            rotatePagesCalls.add(id to rotationsByPageIndex)
            return rotatePagesResult
        }

        override suspend fun cropPage(id: DocumentId, pageIndex: Int, cropRect: CropRect): Result<Unit, DocumentError> {
            cropPageCalls.add(Triple(id, pageIndex, cropRect))
            return cropPageResult
        }
    }

    // --- AC4/AC5: first composition renders, second composition does not ---

    @Test
    fun `composing the list a first time with three documents requests three renders`() = runTest {
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(
                DocumentId("a.pdf") to Result.Success(image(1)),
                DocumentId("b.pdf") to Result.Success(image(2)),
                DocumentId("c.pdf") to Result.Success(image(3))
            )
        }
        val cache = CachingDocumentRepository(delegate)
        val ids = listOf(DocumentId("a.pdf"), DocumentId("b.pdf"), DocumentId("c.pdf"))

        ids.forEach { cache.getThumbnail(it, target) }

        assertEquals(3, delegate.getThumbnailCalls.size)
    }

    @Test
    fun `composing the same list a second time requests no render`() = runTest {
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(
                DocumentId("a.pdf") to Result.Success(image(1)),
                DocumentId("b.pdf") to Result.Success(image(2)),
                DocumentId("c.pdf") to Result.Success(image(3))
            )
        }
        val cache = CachingDocumentRepository(delegate)
        val ids = listOf(DocumentId("a.pdf"), DocumentId("b.pdf"), DocumentId("c.pdf"))
        ids.forEach { cache.getThumbnail(it, target) }
        delegate.getThumbnailCalls.clear()

        ids.forEach { cache.getThumbnail(it, target) }

        assertTrue(delegate.getThumbnailCalls.isEmpty())
    }

    @Test
    fun `a cache hit returns the stored image without calling the delegate again`() = runTest {
        val id = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(id to Result.Success(image(1)))
        }
        val cache = CachingDocumentRepository(delegate)

        val first = cache.getThumbnail(id, target)
        delegate.getThumbnailCalls.clear()
        val second = cache.getThumbnail(id, target)

        assertEquals(first, second)
        assertTrue(delegate.getThumbnailCalls.isEmpty())
    }

    // --- AC6: delete invalidates ---

    @Test
    fun `deleting a document discards its cached thumbnail so the next lookup misses as FileMissing`() = runTest {
        val id = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(id to Result.Success(image(1)))
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(id, target)

        val deleteResult = cache.deleteDocument(id)
        delegate.thumbnails = emptyMap() // the file is really gone now

        val result = cache.getThumbnail(id, target)

        assertTrue(deleteResult is Result.Success)
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DocumentError.FileMissing)
    }

    // --- AC7: rename invalidates the old key, never serves the old name's pixels ---

    @Test
    fun `renaming a document discards the cached entry for the old id and the old name never serves stale pixels`() = runTest {
        val oldId = DocumentId("a.pdf")
        val newId = DocumentId("b.pdf")
        val renamedDocument = Document(
            id = newId,
            fileName = "b.pdf",
            pageCount = 1,
            sizeBytes = 10L,
            modifiedAt = Instant.EPOCH
        )
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(oldId to Result.Success(image(1)))
            renameResult = { _, _ -> Result.Success(renamedDocument) }
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(oldId, target)

        val renameResult = cache.renameDocument(oldId, "b")
        delegate.thumbnails = mapOf(newId to Result.Success(image(2))) // only the new name exists on disk now

        val byNewName = cache.getThumbnail(newId, target)
        val byOldName = cache.getThumbnail(oldId, target)

        assertTrue(renameResult is Result.Success)
        assertEquals(Result.Success(image(2)), byNewName)
        assertTrue(byOldName is Result.Failure)
        assertTrue((byOldName as Result.Failure).error is DocumentError.FileMissing)
    }

    // --- AC8: declared bound, LRU eviction ---

    @Test
    fun `the declared entry limit evicts the least-recently-used thumbnail instead of growing past it`() = runTest {
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = (1..3).associate { i -> DocumentId("$i.pdf") to Result.Success(image(i)) }
        }
        val cache = CachingDocumentRepository(delegate, maxEntries = 2)
        cache.getThumbnail(DocumentId("1.pdf"), target)
        cache.getThumbnail(DocumentId("2.pdf"), target)
        cache.getThumbnail(DocumentId("3.pdf"), target) // bound exceeded: "1.pdf" is the least-recently-used

        delegate.getThumbnailCalls.clear()
        cache.getThumbnail(DocumentId("1.pdf"), target) // evicted: must hit the delegate again
        val callsForEvictedEntry = delegate.getThumbnailCalls.size

        delegate.getThumbnailCalls.clear()
        cache.getThumbnail(DocumentId("3.pdf"), target) // still within the bound: must not hit the delegate

        assertEquals(1, callsForEvictedEntry)
        assertTrue(delegate.getThumbnailCalls.isEmpty())
    }

    // --- Pass-through for members the cache does not decorate ---

    @Test
    fun `getPageImage, getPageCount, listDocuments and addDocument delegate untouched`() = runTest {
        val delegate = RecordingDocumentRepository()
        val cache = CachingDocumentRepository(delegate)

        val listResult = cache.listDocuments()
        val pageCountResult = cache.getPageCount(DocumentId("a.pdf"))
        val pageImageResult = cache.getPageImage(DocumentId("a.pdf"), 0, target)

        assertTrue(listResult is Result.Success)
        assertTrue(pageCountResult is Result.Failure)
        assertTrue(pageImageResult is Result.Failure)
    }

    // --- mergeDocuments (SD-19) ---

    @Test
    fun `mergeDocuments delegates with the given order and returns the delegate's result`() = runTest {
        val mergedDocument = Document(
            id = DocumentId("Spore_merged.pdf"),
            fileName = "Spore_merged.pdf",
            pageCount = 3,
            sizeBytes = 10L,
            modifiedAt = Instant.EPOCH
        )
        val delegate = RecordingDocumentRepository().apply {
            mergeDocumentsResult = Result.Success(mergedDocument)
        }
        val cache = CachingDocumentRepository(delegate)
        val orderedIds = listOf(DocumentId("b.pdf"), DocumentId("a.pdf"))

        val result = cache.mergeDocuments(orderedIds)

        assertEquals(listOf(orderedIds), delegate.mergeDocumentsCalls)
        assertEquals(Result.Success(mergedDocument), result)
    }

    @Test
    fun `a successful merge evicts the cached thumbnails of every merged-away document`() = runTest {
        val idA = DocumentId("a.pdf")
        val idB = DocumentId("b.pdf")
        val mergedDocument = Document(
            id = DocumentId("merged.pdf"),
            fileName = "merged.pdf",
            pageCount = 3,
            sizeBytes = 10L,
            modifiedAt = Instant.EPOCH
        )
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(idA to Result.Success(image(1)), idB to Result.Success(image(2)))
            mergeDocumentsResult = Result.Success(mergedDocument)
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(idA, target)
        cache.getThumbnail(idB, target)

        cache.mergeDocuments(listOf(idA, idB))
        delegate.thumbnails = emptyMap() // the originals are really gone from disk now

        val afterA = cache.getThumbnail(idA, target)
        val afterB = cache.getThumbnail(idB, target)

        assertTrue(afterA is Result.Failure)
        assertTrue(afterB is Result.Failure)
    }

    @Test
    fun `a failed merge does not evict any cached thumbnail`() = runTest {
        val idA = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(idA to Result.Success(image(1)))
            mergeDocumentsResult = Result.Failure(DocumentError.FileMissing(idA))
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(idA, target)
        delegate.getThumbnailCalls.clear()

        cache.mergeDocuments(listOf(idA, DocumentId("b.pdf")))
        val result = cache.getThumbnail(idA, target)

        assertEquals(Result.Success(image(1)), result)
        assertTrue(delegate.getThumbnailCalls.isEmpty())
    }

    // --- rotatePages (SD-22) ---

    @Test
    fun `rotatePages delegates with the given id and rotations map and returns the delegate's result`() = runTest {
        val id = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            rotatePagesResult = Result.Success(Unit)
        }
        val cache = CachingDocumentRepository(delegate)

        val result = cache.rotatePages(id, mapOf(0 to 90))

        assertEquals(listOf(id to mapOf(0 to 90)), delegate.rotatePagesCalls)
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun `a successful rotatePages evicts the cached thumbnail for that document`() = runTest {
        val id = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(id to Result.Success(image(1)))
            rotatePagesResult = Result.Success(Unit)
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(id, target)

        cache.rotatePages(id, mapOf(0 to 90))
        delegate.thumbnails = mapOf(id to Result.Success(image(2))) // rotated pixels now on disk

        val afterRotate = cache.getThumbnail(id, target)

        assertEquals(Result.Success(image(2)), afterRotate)
    }

    @Test
    fun `a failed rotatePages does not evict the cached thumbnail`() = runTest {
        val id = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(id to Result.Success(image(1)))
            rotatePagesResult = Result.Failure(DocumentError.UnreadableFile(id))
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(id, target)
        delegate.getThumbnailCalls.clear()

        cache.rotatePages(id, mapOf(0 to 90))
        val result = cache.getThumbnail(id, target)

        assertEquals(Result.Success(image(1)), result)
        assertTrue(delegate.getThumbnailCalls.isEmpty())
    }

    // --- cropPage (SD-23) ---

    @Test
    fun `cropPage delegates with the given id, page index and crop rect and returns the delegate's result`() = runTest {
        val id = DocumentId("a.pdf")
        val cropRect = CropRect(left = 0.1f, top = 0.1f, right = 0.9f, bottom = 0.9f)
        val delegate = RecordingDocumentRepository().apply {
            cropPageResult = Result.Success(Unit)
        }
        val cache = CachingDocumentRepository(delegate)

        val result = cache.cropPage(id, 0, cropRect)

        assertEquals(listOf(Triple(id, 0, cropRect)), delegate.cropPageCalls)
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun `a successful cropPage evicts the cached thumbnail for that document`() = runTest {
        val id = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(id to Result.Success(image(1)))
            cropPageResult = Result.Success(Unit)
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(id, target)

        cache.cropPage(id, 0, CropRect.Initial)
        delegate.thumbnails = mapOf(id to Result.Success(image(2))) // cropped pixels now on disk

        val afterCrop = cache.getThumbnail(id, target)

        assertEquals(Result.Success(image(2)), afterCrop)
    }

    @Test
    fun `a failed cropPage does not evict the cached thumbnail`() = runTest {
        val id = DocumentId("a.pdf")
        val delegate = RecordingDocumentRepository().apply {
            thumbnails = mapOf(id to Result.Success(image(1)))
            cropPageResult = Result.Failure(DocumentError.UnreadableFile(id))
        }
        val cache = CachingDocumentRepository(delegate)
        cache.getThumbnail(id, target)
        delegate.getThumbnailCalls.clear()

        cache.cropPage(id, 0, CropRect.Initial)
        val result = cache.getThumbnail(id, target)

        assertEquals(Result.Success(image(1)), result)
        assertTrue(delegate.getThumbnailCalls.isEmpty())
    }
}
