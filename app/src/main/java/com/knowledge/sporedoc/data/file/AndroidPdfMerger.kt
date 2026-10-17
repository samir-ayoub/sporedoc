package com.knowledge.sporedoc.data.file

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Real `android.graphics.pdf.PdfRenderer` (read) + `android.graphics.pdf.PdfDocument` (write)
 * implementation of [PdfMerger]: renders every page of every source file at that page's own
 * native size and draws it into a page of a new [PdfDocument], in order.
 */
class AndroidPdfMerger : PdfMerger {

    override fun merge(sourceFiles: List<File>, targetFile: File): Result<Unit, DocumentError> {
        val output = PdfDocument()
        try {
            for (sourceFile in sourceFiles) {
                if (!sourceFile.exists()) {
                    return Result.Failure(DocumentError.FileMissing(DocumentId(sourceFile.name)))
                }
                val appended = appendPages(sourceFile, output)
                if (!appended) {
                    return Result.Failure(DocumentError.UnreadableFile(DocumentId(sourceFile.name)))
                }
            }
            targetFile.outputStream().use { output.writeTo(it) }
            return Result.Success(Unit)
        } catch (_: Exception) {
            return Result.Failure(DocumentError.UnreadableFile(DocumentId(targetFile.name)))
        } finally {
            output.close()
        }
    }

    private fun appendPages(sourceFile: File, output: PdfDocument): Boolean =
        try {
            ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    for (pageIndex in 0 until renderer.pageCount) {
                        renderer.openPage(pageIndex).use { page ->
                            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            val outputPage = output.startPage(
                                PdfDocument.PageInfo.Builder(page.width, page.height, pageIndex).create()
                            )
                            outputPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                            output.finishPage(outputPage)
                            bitmap.recycle()
                        }
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
}
