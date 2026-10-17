package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Hand-written test double for [PdfPageRotator]. Records the (sourceFile, targetFile,
 * rotationsByPageIndex) passed to [rotatePages], so repository tests can assert a rotation
 * was requested for the right pages/degrees without a real PdfRenderer/PdfDocument.
 *
 * On success (the default), copies the source's "PDF-OK:<pageCount>" marker content to
 * [targetFile] unchanged, since [FakePdfFileAccess] does not model per-page rotation state;
 * repository tests only need the rewritten file to exist with the same page count.
 */
class FakePdfPageRotator : PdfPageRotator {

    /** Record of every (sourceFile, targetFile, rotationsByPageIndex) call, in call order. */
    val rotatePagesCalls = mutableListOf<Triple<File, File, Map<Int, Int>>>()

    /** When set, returned by [rotatePages] instead of the default write-and-succeed behavior. */
    var result: Result<Unit, DocumentError>? = null

    override fun rotatePages(sourceFile: File, targetFile: File, rotationsByPageIndex: Map<Int, Int>): Result<Unit, DocumentError> {
        rotatePagesCalls.add(Triple(sourceFile, targetFile, rotationsByPageIndex))
        result?.let { return it }

        targetFile.writeText(sourceFile.readText())
        return Result.Success(Unit)
    }
}
