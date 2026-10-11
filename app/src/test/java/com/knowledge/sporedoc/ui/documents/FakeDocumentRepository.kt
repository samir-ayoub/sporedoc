package com.knowledge.sporedoc.ui.documents

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
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

    override suspend fun listDocuments(): Result<List<Document>, DocumentError> =
        listDocumentsResult ?: Result.Success(documents.toList())

    override suspend fun getPageCount(id: DocumentId): Result<Int, DocumentError> =
        Result.Failure(DocumentError.FileMissing(id))

    override suspend fun getPageImage(id: DocumentId, pageIndex: Int): Result<PageImage, DocumentError> =
        thumbnails[id] ?: Result.Failure(DocumentError.FileMissing(id))

    override suspend fun getThumbnail(id: DocumentId): Result<PageImage, DocumentError> =
        thumbnails[id] ?: Result.Failure(DocumentError.FileMissing(id))

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

    override suspend fun renameDocument(id: DocumentId, newName: String): Result<Document, DocumentError> =
        Result.Failure(DocumentError.InvalidName)

    override suspend fun deleteDocument(id: DocumentId): Result<Unit, DocumentError> =
        Result.Failure(DocumentError.FileMissing(id))

    /** Simulates a document disappearing from disk between two `listDocuments` calls. */
    fun removeDocument(id: DocumentId) {
        documents.removeAll { it.id == id }
    }
}
