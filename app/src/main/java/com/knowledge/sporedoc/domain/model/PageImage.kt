package com.knowledge.sporedoc.domain.model

/**
 * A pure pixel payload crossing the repository boundary. Domain cannot hold
 * `android.graphics.Bitmap`, so pages and thumbnails are expressed as raw bytes plus
 * dimensions; the ui layer decodes them into a platform image.
 *
 * [bytes] holds a raw, uncompressed ARGB_8888 pixel buffer at exactly [width] x [height]
 * pixels (the same byte layout `Bitmap.copyPixelsToBuffer`/`copyPixelsFromBuffer` use),
 * never PNG or any other compressed format. [width]/[height] are the actually rendered
 * size, which is the caller-requested [PixelSize], not the PDF page's native size.
 */
data class PageImage(
    val bytes: ByteArray,
    val width: Int,
    val height: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PageImage) return false
        return width == other.width && height == other.height && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
