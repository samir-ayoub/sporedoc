package com.knowledge.sporedoc.domain.repository

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.util.Result

/** The library's contract: list/read/add/rename/delete/merge documents stored as PDF files. */
interface DocumentRepository {
    suspend fun listDocuments(): Result<List<Document>, DocumentError>
    suspend fun getPageCount(id: DocumentId): Result<Int, DocumentError>
    suspend fun getPageImage(id: DocumentId, pageIndex: Int, targetSize: PixelSize): Result<PageImage, DocumentError>
    suspend fun getThumbnail(id: DocumentId, targetSize: PixelSize): Result<PageImage, DocumentError>
    suspend fun addDocument(source: DocumentSource, requestedName: String?): Result<Document, DocumentError>
    suspend fun renameDocument(id: DocumentId, newName: String): Result<Document, DocumentError>
    suspend fun deleteDocument(id: DocumentId): Result<Unit, DocumentError>

    /**
     * Builds one new PDF whose pages are, in order, every page of [orderedIds]' documents
     * in that document order, names it by the library's existing auto-name + collision rule,
     * and — only once that file exists on disk — deletes every document in [orderedIds].
     * A failure (e.g. a source file missing) leaves the library untouched: no new file,
     * no deleted originals.
     */
    suspend fun mergeDocuments(orderedIds: List<DocumentId>): Result<Document, DocumentError>
}
