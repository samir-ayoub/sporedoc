package com.knowledge.sporedoc.data.file

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.repository.DocumentError
import com.knowledge.sporedoc.domain.util.Result
import java.io.ByteArrayOutputStream
import java.io.File

/** The real, Android/`PdfRenderer`-backed implementation of [PdfFileAccess]. */
class AndroidPdfFileAccess : PdfFileAccess {

    override fun pageCount(file: File): Result<Int, DocumentError> =
        withRenderer(file) { renderer -> Result.Success(renderer.pageCount) }

    override fun renderPage(file: File, pageIndex: Int): Result<PageImage, DocumentError> =
        withRenderer(file) { renderer ->
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                return@withRenderer Result.Failure(
                    DocumentError.PageIndexOutOfBounds(pageIndex, renderer.pageCount)
                )
            }
            renderer.openPage(pageIndex).use { page ->
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Result.Success(PageImage(outputStream.toByteArray(), bitmap.width, bitmap.height))
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
