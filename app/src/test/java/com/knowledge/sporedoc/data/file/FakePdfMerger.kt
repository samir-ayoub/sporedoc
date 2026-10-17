package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Hand-written test double for [PdfMerger]. Records the ordered list of source files and the
 * target file passed to [merge], so repository tests can assert a merge was requested with the
 * documents (and so their pages) in the right order, without a real PdfRenderer/PdfDocument.
 *
 * On success (the default), it writes a merged "PDF-OK:<totalPages>" marker to [targetFile],
 * summing each source's own page count, so [FakePdfFileAccess] can read the resulting page
 * count back through the same convention used everywhere else in these tests.
 */
class FakePdfMerger : PdfMerger {

    /** Record of every (sourceFiles, targetFile) call, in the order [merge] was invoked. */
    val mergeCalls = mutableListOf<Pair<List<File>, File>>()

    /** When set, returned by [merge] instead of the default write-and-succeed behavior. */
    var result: Result<Unit, DocumentError>? = null

    override fun merge(sourceFiles: List<File>, targetFile: File): Result<Unit, DocumentError> {
        mergeCalls.add(sourceFiles to targetFile)
        result?.let { return it }

        val totalPages = sourceFiles.sumOf { file ->
            val text = runCatching { file.readText() }.getOrNull().orEmpty()
            Regex("""^PDF-OK:(\d+)""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        }
        targetFile.writeText("PDF-OK:$totalPages")
        return Result.Success(Unit)
    }
}
