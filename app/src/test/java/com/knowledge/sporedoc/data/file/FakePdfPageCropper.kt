package com.knowledge.sporedoc.data.file

import com.knowledge.sporedoc.domain.model.CropRect
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Hand-written test double for [PdfPageCropper]. Records the (sourceFile, targetFile,
 * pageIndex, cropRect) passed to [cropPage], so repository tests can assert a crop was
 * requested for the right page/rect without a real PdfRenderer/PdfDocument.
 *
 * On success (the default), copies the source's "PDF-OK:<pageCount>" marker content to
 * [targetFile] unchanged, since [FakePdfFileAccess] does not model per-page crop state;
 * repository tests only need the rewritten file to exist with the same page count.
 */
class FakePdfPageCropper : PdfPageCropper {

    /** Record of every (sourceFile, targetFile, pageIndex, cropRect) call, in call order. */
    val cropPageCalls = mutableListOf<CropPageCall>()

    /** When set, returned by [cropPage] instead of the default write-and-succeed behavior. */
    var result: Result<Unit, DocumentError>? = null

    override fun cropPage(sourceFile: File, targetFile: File, pageIndex: Int, cropRect: CropRect): Result<Unit, DocumentError> {
        cropPageCalls.add(CropPageCall(sourceFile, targetFile, pageIndex, cropRect))
        result?.let { return it }

        targetFile.writeText(sourceFile.readText())
        return Result.Success(Unit)
    }

    data class CropPageCall(val sourceFile: File, val targetFile: File, val pageIndex: Int, val cropRect: CropRect)
}
