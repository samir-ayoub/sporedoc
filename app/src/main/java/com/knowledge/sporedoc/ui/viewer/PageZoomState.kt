package com.knowledge.sporedoc.ui.viewer

/**
 * Pure zoom/pan transform for one viewer page. Holds no Android/Compose dependency;
 * all distances are a single consistent linear unit chosen by the caller (the real
 * screen will pass pixels). Starts at scale = 1f, offsetX = 0f, offsetY = 0f.
 */
class PageZoomState(private val maxScale: Float = 4f) {

    var scale: Float = 1f
        private set

    var offsetX: Float = 0f
        private set

    var offsetY: Float = 0f
        private set

    /**
     * Applies one incremental step of an in-progress pinch/pan gesture.
     * [zoomChange] and [panChangeX]/[panChangeY] are deltas since the previous call;
     * [centroidX]/[centroidY] is the gesture's current focal point in container
     * coordinates. [contentWidth]/[contentHeight] is the page's unscaled (1x) size;
     * [containerWidth]/[containerHeight] is the visible viewport size.
     * Resulting scale is clamped to at most [maxScale] and is not clamped below 1
     * during the gesture. Resulting offset is clamped so the scaled content never
     * leaves the viewport for the resolved scale.
     */
    fun onGestureChange(
        contentWidth: Float,
        contentHeight: Float,
        containerWidth: Float,
        containerHeight: Float,
        zoomChange: Float,
        panChangeX: Float,
        panChangeY: Float,
        centroidX: Float,
        centroidY: Float
    ) {
        val previousScale = scale
        val rawNewScale = previousScale * zoomChange
        val newScale = rawNewScale.coerceIn(MIN_SCALE, maxScale)
        val appliedRatio = if (previousScale != 0f) newScale / previousScale else 1f

        val centerX = containerWidth / 2f
        val centerY = containerHeight / 2f

        val rawOffsetX = (centroidX - centerX) -
            (centroidX - centerX - offsetX) * appliedRatio + panChangeX
        val rawOffsetY = (centroidY - centerY) -
            (centroidY - centerY - offsetY) * appliedRatio + panChangeY

        val maxExcessX = ((contentWidth * newScale - containerWidth) / 2f).coerceAtLeast(0f)
        val maxExcessY = ((contentHeight * newScale - containerHeight) / 2f).coerceAtLeast(0f)

        scale = newScale
        offsetX = rawOffsetX.coerceIn(-maxExcessX, maxExcessX)
        offsetY = rawOffsetY.coerceIn(-maxExcessY, maxExcessY)
    }

    /**
     * Called when all pointers are lifted. If scale < 1, resets to scale = 1 and
     * offset = (0, 0). Otherwise leaves scale/offset unchanged.
     */
    fun onGestureEnd() {
        if (scale < 1f) {
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }
    }

    /**
     * Toggles between 1x and [maxScale]. If scale < maxScale, zooms to maxScale,
     * keeping the content point under ([tapX], [tapY]) (container coordinates)
     * fixed under that same point. If scale == maxScale, resets to scale = 1 and
     * offset = (0, 0) (centered).
     */
    fun onDoubleTap(
        contentWidth: Float,
        contentHeight: Float,
        containerWidth: Float,
        containerHeight: Float,
        tapX: Float,
        tapY: Float
    ) {
        if (scale < maxScale) {
            val previousScale = scale
            val targetScale = maxScale
            val appliedRatio = targetScale / previousScale

            val centerX = containerWidth / 2f
            val centerY = containerHeight / 2f

            val rawOffsetX = (tapX - centerX) - (tapX - centerX - offsetX) * appliedRatio
            val rawOffsetY = (tapY - centerY) - (tapY - centerY - offsetY) * appliedRatio

            val maxExcessX = ((contentWidth * targetScale - containerWidth) / 2f).coerceAtLeast(0f)
            val maxExcessY = ((contentHeight * targetScale - containerHeight) / 2f).coerceAtLeast(0f)

            scale = targetScale
            offsetX = rawOffsetX.coerceIn(-maxExcessX, maxExcessX)
            offsetY = rawOffsetY.coerceIn(-maxExcessY, maxExcessY)
        } else {
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }
    }

    private companion object {
        const val MIN_SCALE = 0.1f
    }
}

/**
 * The unscaled (1x) size at which an image of [contentAspectRatio] (width / height) is laid
 * out inside a [containerWidth] x [containerHeight] box under `ContentScale.Fit` semantics:
 * the whole image fits inside the box, touching it on exactly one axis and letterboxed on
 * the other. Callers must pass this — not the container size, and not the image's raw pixel
 * resolution — as the `contentWidth`/`contentHeight` of [PageZoomState]'s gesture methods,
 * since the pan clamp measures the content's actual on-screen footprint against the
 * container, and a mismatch there was leaving the viewer's zoomed corners unreachable.
 */
fun fitContentSize(
    containerWidth: Float,
    containerHeight: Float,
    contentAspectRatio: Float
): Pair<Float, Float> {
    if (containerWidth <= 0f || containerHeight <= 0f || contentAspectRatio <= 0f) {
        return containerWidth to containerHeight
    }
    val containerAspectRatio = containerWidth / containerHeight
    return if (containerAspectRatio > contentAspectRatio) {
        val height = containerHeight
        val width = height * contentAspectRatio
        width to height
    } else {
        val width = containerWidth
        val height = width / contentAspectRatio
        width to height
    }
}
