package com.knowledge.sporedoc.data.repository

import com.knowledge.sporedoc.data.file.PdfFileAccess
import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.util.Clock
import com.knowledge.sporedoc.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Disk-backed implementation of [DocumentRepository]: listing, naming/suffix rule, rename
 * validation and delete, all against PDF files living under [documentsDir].
 */
class FileDocumentRepository(
    private val documentsDir: File,
    private val pdfFileAccess: PdfFileAccess,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DocumentRepository {

    override suspend fun listDocuments(): Result<List<Document>, DocumentError> =
        withContext(ioDispatcher) {
            val files = documentsDir.listFiles { file -> file.isFile && file.name.endsWith(".pdf") }
                ?: emptyArray()
            val documents = files.map { file -> toDocument(file) }.sortedByDescending { it.modifiedAt }
            Result.Success(documents)
        }

    override suspend fun getPageCount(id: DocumentId): Result<Int, DocumentError> =
        withContext(ioDispatcher) {
            val file = findFile(id) ?: return@withContext Result.Failure(DocumentError.FileMissing(id))
            pdfFileAccess.pageCount(file)
        }

    override suspend fun getPageImage(id: DocumentId, pageIndex: Int): Result<PageImage, DocumentError> =
        withContext(ioDispatcher) {
            val file = findFile(id) ?: return@withContext Result.Failure(DocumentError.FileMissing(id))
            pdfFileAccess.renderPage(file, pageIndex)
        }

    override suspend fun getThumbnail(id: DocumentId): Result<PageImage, DocumentError> =
        withContext(ioDispatcher) {
            val file = findFile(id) ?: return@withContext Result.Failure(DocumentError.FileMissing(id))
            pdfFileAccess.renderPage(file, 0)
        }

    override suspend fun addDocument(
        source: DocumentSource,
        requestedName: String?
    ): Result<Document, DocumentError> = withContext(ioDispatcher) {
        val now = clock()
        val desiredName = requestedName ?: generateName(now)
        val finalName = resolveAvailableName(desiredName)
        val targetFile = File(documentsDir, finalName)

        source.openInputStream().use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
        }

        when (pdfFileAccess.pageCount(targetFile)) {
            is Result.Success -> {
                targetFile.setLastModified(now.toEpochMilli())
                Result.Success(toDocument(targetFile))
            }
            is Result.Failure -> {
                targetFile.delete()
                Result.Failure(DocumentError.UnreadableFile(DocumentId(finalName)))
            }
        }
    }

    override suspend fun renameDocument(id: DocumentId, newName: String): Result<Document, DocumentError> =
        withContext(ioDispatcher) {
            if (newName.isBlank()) {
                return@withContext Result.Failure(DocumentError.InvalidName)
            }

            val file = findFile(id) ?: return@withContext Result.Failure(DocumentError.FileMissing(id))

            val trimmed = newName.trim()
            val finalName = if (trimmed.endsWith(".pdf")) trimmed else "$trimmed.pdf"
            val targetFile = File(documentsDir, finalName)

            if (finalName != file.name && targetFile.exists()) {
                return@withContext Result.Failure(DocumentError.NameCollision(finalName))
            }

            if (finalName != file.name) {
                file.renameTo(targetFile)
            }

            Result.Success(toDocument(targetFile))
        }

    override suspend fun deleteDocument(id: DocumentId): Result<Unit, DocumentError> =
        withContext(ioDispatcher) {
            val file = findFile(id) ?: return@withContext Result.Failure(DocumentError.FileMissing(id))
            if (!file.delete()) {
                return@withContext Result.Failure(DocumentError.FileMissing(id))
            }
            Result.Success(Unit)
        }

    private fun findFile(id: DocumentId): File? {
        val file = File(documentsDir, id.value)
        return if (file.exists()) file else null
    }

    private fun toDocument(file: File): Document {
        val pageCount = when (val result = pdfFileAccess.pageCount(file)) {
            is Result.Success -> result.value
            is Result.Failure -> 0
        }
        return Document(
            id = DocumentId(file.name),
            fileName = file.name,
            pageCount = pageCount,
            sizeBytes = file.length(),
            modifiedAt = Instant.ofEpochMilli(file.lastModified())
        )
    }

    private fun generateName(now: Instant): String {
        val formatted = now.atZone(ZoneId.systemDefault()).format(NAME_PATTERN)
        return "Spore_$formatted.pdf"
    }

    private fun resolveAvailableName(desiredName: String): String {
        if (!File(documentsDir, desiredName).exists()) return desiredName

        val dotIndex = desiredName.lastIndexOf('.')
        val base = if (dotIndex >= 0) desiredName.substring(0, dotIndex) else desiredName
        val extension = if (dotIndex >= 0) desiredName.substring(dotIndex) else ""

        var counter = 2
        while (true) {
            val candidate = "${base}_$counter$extension"
            if (!File(documentsDir, candidate).exists()) return candidate
            counter++
        }
    }

    private companion object {
        val NAME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    }
}
