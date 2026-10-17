package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Seam over the platform PdfRenderer/PdfDocument: assembles the pages of several existing
 * PDF files, in the given order and each file's own page order, into one new PDF file.
 */
interface PdfMerger {
    fun merge(sourceFiles: List<File>, targetFile: File): Result<Unit, DocumentError>
}
