package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.model.CropRect
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Seam over PdfRenderer (read) + PdfDocument (write), the same technique [PdfMerger] (SD-19)
 * and [PdfPageRotator] (SD-22) use: rebuilds [sourceFile]'s pages into [targetFile], cropping
 * the page at [pageIndex] to [cropRect] (fractions resolved against that page's own native
 * size) and copying every other page unchanged.
 */
interface PdfPageCropper {
    fun cropPage(sourceFile: File, targetFile: File, pageIndex: Int, cropRect: CropRect): Result<Unit, DocumentError>
}
