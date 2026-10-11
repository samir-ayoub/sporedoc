package com.knowledge.sporedoc.domain.model

/**
 * The render-target size, in device pixels, decided by the caller (the ui layer, from dp
 * tokens + density) rather than by the renderer. Crosses the domain boundary so
 * [com.knowledge.sporedoc.domain.repository.DocumentRepository] and
 * [com.knowledge.sporedoc.data.file.PdfFileAccess] can render directly at display size
 * instead of a PDF page's native resolution.
 */
data class PixelSize(val widthPx: Int, val heightPx: Int)
