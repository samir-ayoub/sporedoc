package com.knowledge.sporedoc.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.knowledge.sporedoc.domain.model.PageImage
import java.nio.ByteBuffer

/**
 * Reconstructs a Compose [ImageBitmap] from a domain [PageImage]'s raw ARGB_8888 pixel
 * buffer; null if the bytes don't match the declared dimensions. No PNG (or other
 * compressed format) encoding/decoding happens on this path. Shared by
 * [com.knowledge.sporedoc.ui.documents.DocumentRow]'s thumbnail and the viewer's page stage.
 */
fun PageImage.toImageBitmapOrNull(): ImageBitmap? {
    if (width <= 0 || height <= 0) return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    if (bytes.size != bitmap.byteCount) return null
    return try {
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes))
        bitmap.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}
