package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Hand-written test double for [PdfFileAccess], used instead of a real PdfRenderer
 * so repository tests run on the plain JVM.
 *
 * A "valid" pdf is any file whose content starts with "PDF-OK:<pageCount>".
 * Anything else is treated as an unreadable file.
 */
class FakePdfFileAccess : PdfFileAccess {

    override fun pageCount(file: File): Result<Int, DocumentError> {
        val count = extractPageCount(file)
            ?: return Result.Failure(DocumentError.UnreadableFile(DocumentId(file.name)))
        return Result.Success(count)
    }

    override fun renderPage(file: File, pageIndex: Int): Result<PageImage, DocumentError> {
        val count = extractPageCount(file)
            ?: return Result.Failure(DocumentError.UnreadableFile(DocumentId(file.name)))
        if (pageIndex < 0 || pageIndex >= count) {
            return Result.Failure(DocumentError.PageIndexOutOfBounds(pageIndex, count))
        }
        return Result.Success(
            PageImage(
                bytes = byteArrayOf(pageIndex.toByte(), 0x7, 0x7),
                width = 10,
                height = 10
            )
        )
    }

    private fun extractPageCount(file: File): Int? {
        if (!file.exists()) return null
        val text = runCatching { file.readText() }.getOrNull() ?: return null
        val match = Regex("""^PDF-OK:(\d+)""").find(text) ?: return null
        return match.groupValues[1].toIntOrNull()
    }
}

/** Content accepted by [FakePdfFileAccess] as a valid pdf with the given page count. */
fun validPdfContent(pageCount: Int): String = "PDF-OK:$pageCount"
