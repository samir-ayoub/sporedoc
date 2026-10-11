package com.knowledge.sporedoc.domain.repository

import com.knowledge.sporedoc.domain.model.DocumentId

/** The closed set of typed failure reasons callers of [DocumentRepository] observe. */
sealed class DocumentError {
    data class FileMissing(val id: DocumentId) : DocumentError()
    data class UnreadableFile(val id: DocumentId) : DocumentError()
    data class PageIndexOutOfBounds(val index: Int, val pageCount: Int) : DocumentError()
    object InvalidName : DocumentError()
    data class NameCollision(val name: String) : DocumentError()
}
