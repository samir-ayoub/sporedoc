package com.knowledge.sporedoc.ui.components

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.knowledge.sporedoc.domain.model.PageImage

/**
 * Decodes a domain [PageImage] into a Compose [ImageBitmap]; null if the bytes are not a
 * decodable image. Shared by [com.knowledge.sporedoc.ui.documents.DocumentRow]'s thumbnail
 * and the viewer's page stage.
 */
fun PageImage.toImageBitmapOrNull(): ImageBitmap? =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
