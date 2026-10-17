package com.knowledge.sporedoc.ui.documents

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.util.Result

/**
 * Hand-written test double for [DocumentRepository], used to drive [DocumentsViewModel]
 * tests without touching the filesystem.
 */
class FakeDocumentRepository(
    initialDocuments: List<Document> = emptyList()
) : DocumentRepository {

    private val documents = initialDocuments.toMutableList()

    /** Record of every call made to [addDocument], for assertions on `requestedName`. */
    val addDocumentCalls = mutableListOf<Pair<DocumentSource, String?>>()

    /** When set, returned by the next [addDocument] call(s) instead of the default success. */
    var addDocumentResult: Result<Document, DocumentError>? = null

    /** When set, returned by [listDocuments] instead of the current in-memory snapshot. */
    var listDocumentsResult: Result<List<Document>, DocumentError>? = null

    /** Thumbnails keyed by document id; missing entries fail with [DocumentError.FileMissing]. */
    var thumbnails: Map<DocumentId, Result<PageImage, DocumentError>> = emptyMap()

    /** Page counts keyed by document id; missing entries fail with [DocumentError.FileMissing]. */
    var pageCountResults: Map<DocumentId, Result<Int, DocumentError>> = emptyMap()

    /** Page images keyed by (document id, page index); missing entries fail with [DocumentError.FileMissing]. */
    var pageImages: Map<Pair<DocumentId, Int>, Result<PageImage, DocumentError>> = emptyMap()

    /** When set, returned by every [deleteDocument] call instead of the default remove-and-succeed. */
    var deleteDocumentResult: Result<Unit, DocumentError>? = null

    /** When an id is present here, its result overrides both [deleteDocumentResult] and the default, for per-id outcomes in batch-delete tests. */
    var deleteDocumentResultsById: Map<DocumentId, Result<Unit, DocumentError>> = emptyMap()

    /** Record of every id passed to [deleteDocument], for assertions on when it was (not) called. */
    val deleteDocumentCalls = mutableListOf<DocumentId>()

    /** When set, returned by every [renameDocument] call instead of the default rename-in-place. */
    var renameDocumentResult: Result<Document, DocumentError>? = null

    /** Record of every (id, newName) pair passed to [renameDocument]. */
    val renameDocumentCalls = mutableListOf<Pair<DocumentId, String>>()

    /** Record of every (id, targetSize) pair passed to [getThumbnail]. */
    val getThumbnailCalls = mutableListOf<Pair<DocumentId, PixelSize>>()

    /** Record of every (id, pageIndex, targetSize) triple passed to [getPageImage]. */
    val getPageImageCalls = mutableListOf<Triple<DocumentId, Int, PixelSize>>()

    override suspend fun listDocuments(): Result<List<Document>, DocumentError> =
        listDocumentsResult ?: Result.Success(documents.toList())

    override suspend fun getPageCount(id: DocumentId): Result<Int, DocumentError> =
        pageCountResults[id] ?: Result.Failure(DocumentError.FileMissing(id))

    override suspend fun getPageImage(id: DocumentId, pageIndex: Int, targetSize: PixelSize): Result<PageImage, DocumentError> {
        getPageImageCalls.add(Triple(id, pageIndex, targetSize))
        return pageImages[id to pageIndex] ?: thumbnails[id] ?: Result.Failure(DocumentError.FileMissing(id))
    }

    override suspend fun getThumbnail(id: DocumentId, targetSize: PixelSize): Result<PageImage, DocumentError> {
        getThumbnailCalls.add(id to targetSize)
        return thumbnails[id] ?: Result.Failure(DocumentError.FileMissing(id))
    }

    override suspend fun addDocument(
        source: DocumentSource,
        requestedName: String?
    ): Result<Document, DocumentError> {
        addDocumentCalls.add(source to requestedName)
        addDocumentResult?.let { return it }
        val added = Document(
            id = DocumentId("Spore_20261011_1430.pdf"),
            fileName = "Spore_20261011_1430.pdf",
            pageCount = 3,
            sizeBytes = 1_000L,
            modifiedAt = java.time.Instant.parse("2026-10-11T14:30:00Z")
        )
        documents.add(added)
        return Result.Success(added)
    }

    override suspend fun renameDocument(id: DocumentId, newName: String): Result<Document, DocumentError> {
        renameDocumentCalls.add(id to newName)
        renameDocumentResult?.let { return it }

        if (newName.isBlank()) return Result.Failure(DocumentError.InvalidName)

        val existing = documents.find { it.id == id } ?: return Result.Failure(DocumentError.FileMissing(id))
        val finalName = if (newName.endsWith(".pdf")) newName else "$newName.pdf"

        if (finalName != existing.fileName && documents.any { it.fileName == finalName }) {
            return Result.Failure(DocumentError.NameCollision(finalName))
        }

        val renamed = existing.copy(id = DocumentId(finalName), fileName = finalName)
        documents.remove(existing)
        documents.add(renamed)
        return Result.Success(renamed)
    }

    override suspend fun deleteDocument(id: DocumentId): Result<Unit, DocumentError> {
        deleteDocumentCalls.add(id)
        deleteDocumentResultsById[id]?.let { return it }
        deleteDocumentResult?.let { return it }
        documents.removeAll { it.id == id }
        return Result.Success(Unit)
    }

    /** Simulates a document disappearing from disk between two `listDocuments` calls. */
    fun removeDocument(id: DocumentId) {
        documents.removeAll { it.id == id }
    }
}
