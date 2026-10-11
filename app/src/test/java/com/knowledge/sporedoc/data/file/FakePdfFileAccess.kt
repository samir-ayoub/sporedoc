package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Hand-written test double for [PdfFileAccess], used instead of a real PdfRenderer
 * so repository tests run on the plain JVM.
 *
 * A "valid" pdf is any file whose content starts with "PDF-OK:<pageCount>".
 * Anything else is treated as an unreadable file. [renderPage] echoes the requested
 * [PixelSize] back as the result's width/height, so callers can assert the display
 * target size was forwarded instead of some fixed page resolution (SD-10 R1/AC1).
 */
class FakePdfFileAccess : PdfFileAccess {

    /** Record of every (file, pageIndex, targetSize) passed to [renderPage]. */
    val renderPageCalls = mutableListOf<Triple<File, Int, PixelSize>>()

    override fun pageCount(file: File): Result<Int, DocumentError> {
        val count = extractPageCount(file)
            ?: return Result.Failure(DocumentError.UnreadableFile(DocumentId(file.name)))
        return Result.Success(count)
    }

    override fun renderPage(file: File, pageIndex: Int, targetSize: PixelSize): Result<PageImage, DocumentError> {
        renderPageCalls.add(Triple(file, pageIndex, targetSize))
        val count = extractPageCount(file)
            ?: return Result.Failure(DocumentError.UnreadableFile(DocumentId(file.name)))
        if (pageIndex < 0 || pageIndex >= count) {
            return Result.Failure(DocumentError.PageIndexOutOfBounds(pageIndex, count))
        }
        return Result.Success(
            PageImage(
                bytes = byteArrayOf(pageIndex.toByte(), 0x7, 0x7),
                width = targetSize.widthPx,
                height = targetSize.heightPx
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
