package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.CropCorner
import com.knowledge.sporedoc.domain.model.CropRect

/**
 * Computes a new [CropRect] from a dragged corner's requested position (SD-23 R4/R5 and the
 * confirmed PO decision for the "would invert/collapse the rectangle" case).
 *
 * Only [draggedCorner]'s own two coordinates ever move; the opposite corner is the anchor
 * and never moves, which is what keeps the rectangle's sides parallel to the page (R4) - no
 * rotation state exists to accidentally introduce.
 *
 * [requestedX]/[requestedY] are first clamped to the page's 0f..1f bounds (R5). If the
 * clamped request would cross or collapse the opposite corner closer than
 * [minDimensionFraction], the moved coordinate is locked at exactly [minDimensionFraction]
 * away from the anchor instead, so the rectangle never inverts or collapses.
 */
class AdjustCropRect(private val minDimensionFraction: Float = DEFAULT_MIN_DIMENSION_FRACTION) {

    operator fun invoke(
        current: CropRect,
        draggedCorner: CropCorner,
        requestedX: Float,
        requestedY: Float
    ): CropRect {
        val clampedX = requestedX.coerceIn(0f, 1f)
        val clampedY = requestedY.coerceIn(0f, 1f)

        return when (draggedCorner) {
            CropCorner.TOP_LEFT -> current.copy(
                left = lockToMinimum(clampedX, anchor = current.right, movingTowardsAnchor = true),
                top = lockToMinimum(clampedY, anchor = current.bottom, movingTowardsAnchor = true)
            )
            CropCorner.TOP_RIGHT -> current.copy(
                right = lockToMinimum(clampedX, anchor = current.left, movingTowardsAnchor = false),
                top = lockToMinimum(clampedY, anchor = current.bottom, movingTowardsAnchor = true)
            )
            CropCorner.BOTTOM_LEFT -> current.copy(
                left = lockToMinimum(clampedX, anchor = current.right, movingTowardsAnchor = true),
                bottom = lockToMinimum(clampedY, anchor = current.top, movingTowardsAnchor = false)
            )
            CropCorner.BOTTOM_RIGHT -> current.copy(
                right = lockToMinimum(clampedX, anchor = current.left, movingTowardsAnchor = false),
                bottom = lockToMinimum(clampedY, anchor = current.top, movingTowardsAnchor = false)
            )
        }
    }

    /**
     * When [movingTowardsAnchor] is true, [value] is a left/top coordinate moving towards its
     * right/bottom anchor, and must stay at most `anchor - minDimensionFraction`. Otherwise
     * [value] is a right/bottom coordinate moving towards its left/top anchor, and must stay
     * at least `anchor + minDimensionFraction`.
     */
    private fun lockToMinimum(value: Float, anchor: Float, movingTowardsAnchor: Boolean): Float =
        if (movingTowardsAnchor) {
            value.coerceAtMost(anchor - minDimensionFraction)
        } else {
            value.coerceAtLeast(anchor + minDimensionFraction)
        }

    companion object {
        const val DEFAULT_MIN_DIMENSION_FRACTION: Float = 0.05f
    }
}
