package com.knowledge.sporedoc.data.file

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.knowledge.sporedoc.domain.model.CropRect
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File

/**
 * Real `android.graphics.pdf.PdfRenderer` (read) + `android.graphics.pdf.PdfDocument` (write)
 * implementation of [PdfPageCropper]: renders every page of [sourceFile] at its own native
 * size, crops the bitmap for the page named by [pageIndex] to [cropRect] resolved against
 * that page's native size, and draws every page, cropped or not, into a fresh [PdfDocument]
 * written to [targetFile].
 */
class AndroidPdfPageCropper : PdfPageCropper {

    override fun cropPage(
        sourceFile: File,
        targetFile: File,
        pageIndex: Int,
        cropRect: CropRect
    ): Result<Unit, DocumentError> {
        if (!sourceFile.exists()) {
            return Result.Failure(DocumentError.FileMissing(DocumentId(sourceFile.name)))
        }

        val output = PdfDocument()
        try {
            ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    for (index in 0 until renderer.pageCount) {
                        renderer.openPage(index).use { page ->
                            val sourceBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(sourceBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                            val bitmap = if (index == pageIndex) cropBitmap(sourceBitmap, cropRect) else sourceBitmap

                            val outputPage = output.startPage(
                                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index).create()
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

    /** Resolves [cropRect]'s fractions against [source]'s native pixel size and crops to it. */
    private fun cropBitmap(source: Bitmap, cropRect: CropRect): Bitmap {
        val left = (cropRect.left * source.width).toInt().coerceIn(0, source.width - 1)
        val top = (cropRect.top * source.height).toInt().coerceIn(0, source.height - 1)
        val right = (cropRect.right * source.width).toInt().coerceIn(left + 1, source.width)
        val bottom = (cropRect.bottom * source.height).toInt().coerceIn(top + 1, source.height)
        return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
    }
}
