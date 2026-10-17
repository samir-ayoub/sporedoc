package com.knowledge.sporedoc.domain.model

/**
 * An axis-aligned crop rectangle, expressed as fractions of the page's width/height
 * (0f..1f), with [left] < [right] and [top] < [bottom]. Fractions, not pixels or dp, are
 * the contract: the same rectangle describes both the on-screen page box (whatever dp size
 * it renders at) and the PDF's native page size at save time, without this model touching
 * either.
 */
data class CropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    companion object {
        /**
         * Fixed, deterministic starting rectangle (SD-23 R3): the same value every time crop
         * mode is entered, with no automatic margin detection.
         */
        val Initial: CropRect = CropRect(left = 0.05f, top = 0.05f, right = 0.95f, bottom = 0.95f)
    }
}
