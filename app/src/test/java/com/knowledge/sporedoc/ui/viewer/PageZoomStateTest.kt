package com.knowledge.sporedoc.ui.viewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [PageZoomState]: the pure zoom/pan transform for one viewer page.
 *
 * Covers (from SD-11's acceptance criteria):
 * - pinch to 2x sets scale to 2x.
 * - pinch past 4x clamps scale at 4x.
 * - continuing to pinch at 4x keeps scale at 4x.
 * - releasing below 1x snaps back to scale = 1 and offset = (0, 0).
 * - dragging a page above 1x moves the visible offset by the drag amount.
 * - dragging past the pan limit clamps the offset, never exposing empty space.
 * - double tap zooms to 4x keeping the tapped content point under the finger.
 * - double tap at 4x resets to 1x, centered (offset = (0, 0)).
 *
 * Not covered here (not reachable from this pure unit, see Architect's note):
 * - "pinch to 4x then pan triggers zero page-render calls" — requires a Compose UI
 *   interaction test in src/androidTest/, which does not count for this project's gate.
 */
class PageZoomStateTest {

    private val containerWidth = 400f
    private val containerHeight = 800f
    private val contentWidth = 400f
    private val contentHeight = 800f

    @Test
    fun `initial state starts at scale 1 with no offset`() {
        val state = PageZoomState()

        assertEquals(1f, state.scale, 0.001f)
        assertEquals(0f, state.offsetX, 0.001f)
        assertEquals(0f, state.offsetY, 0.001f)
    }

    @Test
    fun `pinch zoom out by 2x sets scale to 2x`() {
        val state = PageZoomState()

        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 2f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )

        assertEquals(2f, state.scale, 0.001f)
    }

    @Test
    fun `pinch zoom out by 10x clamps scale at 4x`() {
        val state = PageZoomState()

        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 10f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )

        assertEquals(4f, state.scale, 0.001f)
    }

    @Test
    fun `continuing to pinch out while already at 4x keeps scale at 4x`() {
        val state = PageZoomState()
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 10f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )
        assertEquals(4f, state.scale, 0.001f)

        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 2f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )

        assertEquals(4f, state.scale, 0.001f)
    }

    @Test
    fun `releasing pointers after zooming below 1x snaps back to 1x and centered offset`() {
        val state = PageZoomState()
        // Reach 2x first.
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 2f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )
        assertEquals(2f, state.scale, 0.001f)

        // Then pinch down to 0.5x of that: 2 * 0.25 = 0.5.
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 0.25f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )
        assertTrue("scale should be below 1x mid-gesture", state.scale < 1f)

        state.onGestureEnd()

        assertEquals(1f, state.scale, 0.001f)
        assertEquals(0f, state.offsetX, 0.001f)
        assertEquals(0f, state.offsetY, 0.001f)
    }

    @Test
    fun `releasing pointers while scale is at or above 1x leaves scale and offset unchanged`() {
        val state = PageZoomState()
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 2f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )
        val scaleBeforeEnd = state.scale

        state.onGestureEnd()

        assertEquals(scaleBeforeEnd, state.scale, 0.001f)
    }

    @Test
    fun `dragging a page scaled above 1x moves the offset by the drag amount`() {
        val state = PageZoomState()
        // Zoom to 3x first (content now 1200x2400 vs container 400x800: plenty of room to pan).
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 3f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )
        assertEquals(3f, state.scale, 0.001f)
        val offsetXBeforeDrag = state.offsetX

        // Pure pan: zoomChange = 1 (no scale change), panChangeX = -100 (drag left).
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 1f,
            panChangeX = -100f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )

        assertEquals(offsetXBeforeDrag - 100f, state.offsetX, 0.001f)
        assertEquals(0f, state.offsetY, 0.001f)
    }

    @Test
    fun `dragging past the pan limit clamps the offset instead of exposing empty space`() {
        val state = PageZoomState()
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 3f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )

        // Drag far beyond what 3x scale allows: the maximum horizontal excess is
        // (contentWidth * 3 - containerWidth) / 2 = (1200 - 400) / 2 = 400.
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 1f,
            panChangeX = -10_000f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )
        val offsetAtLimit = state.offsetX
        val maxExcessX = (contentWidth * state.scale - containerWidth) / 2f
        assertTrue(
            "offset should not exceed the content's horizontal excess",
            kotlin.math.abs(offsetAtLimit) <= maxExcessX + 0.5f
        )

        // Dragging further in the same direction must not move the offset any further.
        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 1f,
            panChangeX = -500f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )

        assertEquals(offsetAtLimit, state.offsetX, 0.001f)
    }

    @Test
    fun `double tap from 1x zooms to 4x keeping the tapped point under the finger`() {
        val state = PageZoomState()
        // Tap near the top-left corner of the container.
        val tapX = 50f
        val tapY = 100f

        state.onDoubleTap(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            tapX = tapX,
            tapY = tapY
        )

        assertEquals(4f, state.scale, 0.001f)

        // The content point under the tap before zooming, expressed relative to the
        // container center (since content and transforms are centered), must still
        // project onto (tapX, tapY) after the zoom: centerX + offsetX + (tapX - centerX) * scale == tapX.
        val centerX = containerWidth / 2f
        val centerY = containerHeight / 2f
        val projectedX = centerX + state.offsetX + (tapX - centerX) * state.scale
        val projectedY = centerY + state.offsetY + (tapY - centerY) * state.scale

        assertEquals(tapX, projectedX, 0.5f)
        assertEquals(tapY, projectedY, 0.5f)
    }

    @Test
    fun `double tap while at 4x resets to 1x and centers the page`() {
        val state = PageZoomState()
        state.onDoubleTap(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            tapX = 50f,
            tapY = 100f
        )
        assertEquals(4f, state.scale, 0.001f)

        state.onDoubleTap(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            tapX = 50f,
            tapY = 100f
        )

        assertEquals(1f, state.scale, 0.001f)
        assertEquals(0f, state.offsetX, 0.001f)
        assertEquals(0f, state.offsetY, 0.001f)
    }

    @Test
    fun `scale never exceeds the configured maxScale`() {
        val state = PageZoomState(maxScale = 4f)

        state.onGestureChange(
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoomChange = 1_000f,
            panChangeX = 0f,
            panChangeY = 0f,
            centroidX = containerWidth / 2f,
            centroidY = containerHeight / 2f
        )

        assertEquals(4f, state.scale, 0.001f)
    }
}
