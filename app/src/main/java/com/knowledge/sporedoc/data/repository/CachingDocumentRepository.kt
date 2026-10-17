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

/**
 * Memoizes thumbnail renders per document, bounded by an LRU limit, invalidated on
 * successful delete/rename. Decides what is already-rendered-and-still-valid before
 * asking [delegate] to do any work; every other member passes straight through.
 */
class CachingDocumentRepository(
    private val delegate: DocumentRepository,
    private val maxEntries: Int = 60
) : DocumentRepository {

    private val thumbnailCache = object : LinkedHashMap<DocumentId, PageImage>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<DocumentId, PageImage>): Boolean =
            size > maxEntries
    }

    override suspend fun listDocuments(): Result<List<Document>, DocumentError> = delegate.listDocuments()

    override suspend fun getPageCount(id: DocumentId): Result<Int, DocumentError> = delegate.getPageCount(id)

    override suspend fun getPageImage(
        id: DocumentId,
        pageIndex: Int,
        targetSize: PixelSize
    ): Result<PageImage, DocumentError> = delegate.getPageImage(id, pageIndex, targetSize)

    override suspend fun getThumbnail(id: DocumentId, targetSize: PixelSize): Result<PageImage, DocumentError> {
        thumbnailCache[id]?.let { return Result.Success(it) }

        return when (val result = delegate.getThumbnail(id, targetSize)) {
            is Result.Success -> {
                thumbnailCache[id] = result.value
                result
            }
            is Result.Failure -> result
        }
    }

    override suspend fun addDocument(
        source: DocumentSource,
        requestedName: String?
    ): Result<Document, DocumentError> = delegate.addDocument(source, requestedName)

    override suspend fun renameDocument(id: DocumentId, newName: String): Result<Document, DocumentError> =
        delegate.renameDocument(id, newName).also { result ->
            if (result is Result.Success) {
                thumbnailCache.remove(id)
            }
        }

    override suspend fun deleteDocument(id: DocumentId): Result<Unit, DocumentError> =
        delegate.deleteDocument(id).also { result ->
            if (result is Result.Success) {
                thumbnailCache.remove(id)
            }
        }

    override suspend fun mergeDocuments(orderedIds: List<DocumentId>): Result<Document, DocumentError> =
        delegate.mergeDocuments(orderedIds).also { result ->
            if (result is Result.Success) {
                orderedIds.forEach { thumbnailCache.remove(it) }
            }
        }

    override suspend fun rotatePages(id: DocumentId, rotationsByPageIndex: Map<Int, Int>): Result<Unit, DocumentError> =
        delegate.rotatePages(id, rotationsByPageIndex).also { result ->
            if (result is Result.Success) {
                thumbnailCache.remove(id)
            }
        }

    override suspend fun cropPage(id: DocumentId, pageIndex: Int, cropRect: CropRect): Result<Unit, DocumentError> =
        delegate.cropPage(id, pageIndex, cropRect).also { result ->
            if (result is Result.Success) {
                thumbnailCache.remove(id)
            }
        }
}
