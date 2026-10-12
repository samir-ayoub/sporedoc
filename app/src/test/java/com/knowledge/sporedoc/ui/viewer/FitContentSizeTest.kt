package com.knowledge.sporedoc.ui.viewer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [fitContentSize]: the `ContentScale.Fit` footprint fed into [PageZoomState]'s
 * pan clamp. Regression for the viewer-zoom defect where that clamp measured the content
 * against the wrong size, leaving the zoomed page's corners unreachable.
 */
class FitContentSizeTest {

    @Test
    fun `wider container than content fits by height and letterboxes the sides`() {
        val (width, height) = fitContentSize(
            containerWidth = 1000f,
            containerHeight = 500f,
            contentAspectRatio = 1f
        )

        assertEquals(500f, width, 0.001f)
        assertEquals(500f, height, 0.001f)
    }

    @Test
    fun `taller container than content fits by width and letterboxes top and bottom`() {
        val (width, height) = fitContentSize(
            containerWidth = 400f,
            containerHeight = 1000f,
            contentAspectRatio = 282f / 398f
        )

        assertEquals(400f, width, 0.001f)
        assertEquals(400f / (282f / 398f), height, 0.001f)
    }

    @Test
    fun `container matching the content aspect ratio exactly fills it`() {
        val (width, height) = fitContentSize(
            containerWidth = 282f,
            containerHeight = 398f,
            contentAspectRatio = 282f / 398f
        )

        assertEquals(282f, width, 0.001f)
        assertEquals(398f, height, 0.001f)
    }

    @Test
    fun `zero container size falls back to the container size instead of dividing by zero`() {
        val (width, height) = fitContentSize(
            containerWidth = 0f,
            containerHeight = 0f,
            contentAspectRatio = 0.7f
        )

        assertEquals(0f, width, 0.001f)
        assertEquals(0f, height, 0.001f)
    }
}
