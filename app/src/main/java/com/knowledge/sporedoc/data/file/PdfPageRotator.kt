package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Seam over PdfRenderer (read) + PdfDocument (write), the same technique [PdfMerger] (SD-19)
 * uses: rebuilds [sourceFile]'s pages into [targetFile], rotating each page named in
 * [rotationsByPageIndex] (0-based index -> degrees counterclockwise, one of 90/180/270) and
 * copying every other page unchanged.
 */
interface PdfPageRotator {
    fun rotatePages(sourceFile: File, targetFile: File, rotationsByPageIndex: Map<Int, Int>): Result<Unit, DocumentError>
}
