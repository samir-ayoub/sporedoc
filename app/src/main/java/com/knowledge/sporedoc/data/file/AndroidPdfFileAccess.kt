package com.knowledge.sporedoc.data.file

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.File
import java.nio.ByteBuffer

/** The real, Android/`PdfRenderer`-backed implementation of [PdfFileAccess]. */
class AndroidPdfFileAccess : PdfFileAccess {

    override fun pageCount(file: File): Result<Int, DocumentError> =
        withRenderer(file) { renderer -> Result.Success(renderer.pageCount) }

    override fun renderPage(
        file: File,
        pageIndex: Int,
        targetSize: PixelSize
    ): Result<PageImage, DocumentError> =
        withRenderer(file) { renderer ->
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                return@withRenderer Result.Failure(
                    DocumentError.PageIndexOutOfBounds(pageIndex, renderer.pageCount)
                )
            }
            renderer.openPage(pageIndex).use { page ->
                val bitmap = Bitmap.createBitmap(
                    targetSize.widthPx,
                    targetSize.heightPx,
                    Bitmap.Config.ARGB_8888
                )
                // Fill the target box while preserving the page's own aspect ratio, matching
                // the previous full-resolution-render-then-crop framing (ContentScale.Crop):
                // scale up to the larger of the two ratios so the page covers the whole box,
                // then center and let the bitmap's own bounds clip the overflow.
                val scaleX = targetSize.widthPx.toFloat() / page.width.toFloat()
                val scaleY = targetSize.heightPx.toFloat() / page.height.toFloat()
                val scale = maxOf(scaleX, scaleY)
                val matrix = Matrix().apply {
                    setScale(scale, scale)
                    postTranslate(
                        (targetSize.widthPx - page.width * scale) / 2f,
                        (targetSize.heightPx - page.height * scale) / 2f
                    )
                }
                page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val buffer = ByteBuffer.allocate(bitmap.byteCount)
                bitmap.copyPixelsToBuffer(buffer)
                Result.Success(PageImage(buffer.array(), bitmap.width, bitmap.height))
            }
        }

    private fun <T> withRenderer(
        file: File,
        block: (PdfRenderer) -> Result<T, DocumentError>
    ): Result<T, DocumentError> {
        if (!file.exists()) {
            return Result.Failure(DocumentError.UnreadableFile(DocumentId(file.name)))
        }
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> block(renderer) }
            }
        } catch (_: Exception) {
            Result.Failure(DocumentError.UnreadableFile(DocumentId(file.name)))
        }
    }
}
