package com.knowledge.sporedoc.data.file

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Real `android.graphics.pdf.PdfRenderer` (read) + `android.graphics.pdf.PdfDocument` (write)
 * implementation of [PdfPageRotator]: renders every page of [sourceFile] at its own native
 * size, rotates the bitmap for pages named in [rotationsByPageIndex] and draws every page,
 * rotated or not, into a fresh [PdfDocument] written to [targetFile].
 */
class AndroidPdfPageRotator : PdfPageRotator {

    override fun rotatePages(
        sourceFile: File,
        targetFile: File,
        rotationsByPageIndex: Map<Int, Int>
    ): Result<Unit, DocumentError> {
        if (!sourceFile.exists()) {
            return Result.Failure(DocumentError.FileMissing(DocumentId(sourceFile.name)))
        }

        val output = PdfDocument()
        try {
            ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    for (pageIndex in 0 until renderer.pageCount) {
                        renderer.openPage(pageIndex).use { page ->
                            val sourceBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(sourceBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                            val degrees = rotationsByPageIndex[pageIndex] ?: 0
                            val bitmap = if (degrees == 0) sourceBitmap else rotateBitmap(sourceBitmap, degrees)

                            val outputPage = output.startPage(
                                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pageIndex).create()
                            )
                            outputPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                            output.finishPage(outputPage)

                            if (bitmap !== sourceBitmap) sourceBitmap.recycle()
                            bitmap.recycle()
                        }
                    }
                }
            }
            targetFile.outputStream().use { output.writeTo(it) }
            return Result.Success(Unit)
        } catch (_: Exception) {
            return Result.Failure(DocumentError.UnreadableFile(DocumentId(sourceFile.name)))
        } finally {
            output.close()
        }
    }

    /** Rotates by [degrees] counterclockwise, i.e. [Matrix.postRotate] with the negated angle. */
    private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(-degrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
