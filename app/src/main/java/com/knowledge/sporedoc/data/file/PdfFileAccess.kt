package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Seam over the platform PDF renderer (`android.graphics.pdf.PdfRenderer`), so
 * [com.knowledge.sporedoc.data.repository.FileDocumentRepository] can be unit-tested on the
 * plain JVM with a hand-written fake.
 */
interface PdfFileAccess {
    fun pageCount(file: File): Result<Int, DocumentError>
    fun renderPage(file: File, pageIndex: Int): Result<PageImage, DocumentError>
}
