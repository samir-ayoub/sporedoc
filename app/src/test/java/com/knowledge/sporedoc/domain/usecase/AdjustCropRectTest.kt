package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.CropCorner
import com.knowledge.sporedoc.domain.model.CropRect
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SD-23 R4/R5 and the confirmed PO decision for the "dragged node would invert/collapse the
 * rectangle" case ("definir uma dimensao minima aceita"):
 *
 * - R4: "Arrastar um nó move esse canto do retângulo, mantendo os lados paralelos às bordas
 *   da página, sem o inclinar." [AdjustCropRect] only ever moves the dragged corner's own two
 *   coordinates; the opposite corner never moves, which is what keeps the sides axis-aligned.
 * - R5: "Os quatro nós não podem ser arrastados para fora dos limites da página." Requested
 *   coordinates are clamped to 0f..1f before anything else.
 * - Confirmed decision: a minimum accepted dimension. When the clamped request would cross or
 *   collapse the opposite corner closer than `minDimensionFraction`, the moved coordinate is
 *   locked at exactly `minDimensionFraction` from the opposite corner instead.
 */
class AdjustCropRectTest {

    private val current = CropRect(left = 0.2f, top = 0.2f, right = 0.8f, bottom = 0.8f)

    // --- R4: only the dragged corner's own coordinates move ---

    @Test
    fun `dragging TOP_LEFT moves left and top only, right and bottom stay as the opposite anchor`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.TOP_LEFT, 0.3f, 0.35f)

        assertEquals(0.3f, result.left)
        assertEquals(0.35f, result.top)
        assertEquals(0.8f, result.right)
        assertEquals(0.8f, result.bottom)
    }

    @Test
    fun `dragging TOP_RIGHT moves right and top only, left and bottom stay as the opposite anchor`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.TOP_RIGHT, 0.75f, 0.3f)

        assertEquals(0.75f, result.right)
        assertEquals(0.3f, result.top)
        assertEquals(0.2f, result.left)
        assertEquals(0.8f, result.bottom)
    }

    @Test
    fun `dragging BOTTOM_LEFT moves left and bottom only, right and top stay as the opposite anchor`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.BOTTOM_LEFT, 0.25f, 0.7f)

        assertEquals(0.25f, result.left)
        assertEquals(0.7f, result.bottom)
        assertEquals(0.8f, result.right)
        assertEquals(0.2f, result.top)
    }

    @Test
    fun `dragging BOTTOM_RIGHT moves right and bottom only, left and top stay as the opposite anchor`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.BOTTOM_RIGHT, 0.78f, 0.75f)

        assertEquals(0.78f, result.right)
        assertEquals(0.75f, result.bottom)
        assertEquals(0.2f, result.left)
        assertEquals(0.2f, result.top)
    }

    // --- R5: clamp requested coordinates to the page's 0f..1f bounds ---

    @Test
    fun `a requestedX below 0 is clamped to the page's left edge`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.TOP_LEFT, -0.5f, 0.3f)

        assertEquals(0f, result.left)
    }

    @Test
    fun `a requestedX above 1 is clamped to the page's right edge`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.BOTTOM_RIGHT, 1.5f, 0.75f)

        assertEquals(1f, result.right)
    }

    @Test
    fun `a requestedY below 0 is clamped to the page's top edge`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.TOP_LEFT, 0.3f, -0.2f)

        assertEquals(0f, result.top)
    }

    @Test
    fun `a requestedY above 1 is clamped to the page's bottom edge`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        val result = adjust(current, CropCorner.BOTTOM_LEFT, 0.3f, 1.5f)

        assertEquals(1f, result.bottom)
    }

    // --- Confirmed decision: minimum accepted dimension, instead of inverting/collapsing ---

    @Test
    fun `dragging a corner too close to the opposite corner locks it at exactly minDimensionFraction away, not closer`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        // right is at 0.8; requesting 0.75 would leave only 0.05 of width, under the 0.1 minimum.
        val result = adjust(current, CropCorner.TOP_LEFT, 0.75f, 0.2f)

        assertEquals(0.7f, result.left)
        assertEquals(0.8f, result.right)
    }

    @Test
    fun `dragging a corner past the opposite corner locks it at minDimensionFraction away instead of inverting the rectangle`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.1f)

        // requesting 0.95 would cross right (0.8) entirely, inverting the rectangle.
        val result = adjust(current, CropCorner.TOP_LEFT, 0.95f, 0.2f)

        assertEquals(0.7f, result.left)
        assertEquals(0.8f, result.right)
    }

    @Test
    fun `a different minDimensionFraction locks the moved coordinate at that distance instead`() {
        val adjust = AdjustCropRect(minDimensionFraction = 0.2f)

        val result = adjust(current, CropCorner.TOP_LEFT, 0.75f, 0.2f)

        assertEquals(0.6f, result.left)
    }

    @Test
    fun `without an explicit minDimensionFraction, the default constant is used to lock a collapsing drag`() {
        val adjust = AdjustCropRect()
        val fullPageRect = CropRect(left = 0f, top = 0f, right = 1f, bottom = 1f)

        val result = adjust(fullPageRect, CropCorner.TOP_LEFT, 1f, 0f)

        assertEquals(1f - AdjustCropRect.DEFAULT_MIN_DIMENSION_FRACTION, result.left, 0.0001f)
    }

    @Test
    fun `the default minimum dimension fraction is a positive fraction smaller than a whole page`() {
        assertEquals(
            true,
            AdjustCropRect.DEFAULT_MIN_DIMENSION_FRACTION > 0f && AdjustCropRect.DEFAULT_MIN_DIMENSION_FRACTION < 1f
        )
    }
}
