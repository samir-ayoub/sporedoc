package com.knowledge.sporedoc.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * SD-10 R1/R3: [PixelSize] is the pure-Kotlin render-target size, in device pixels, that
 * crosses the domain boundary instead of the full PDF page resolution.
 */
class PixelSizeTest {

    @Test
    fun `two sizes with the same width and height are equal`() {
        assertEquals(PixelSize(widthPx = 80, heightPx = 110), PixelSize(widthPx = 80, heightPx = 110))
    }

    @Test
    fun `sizes with a different width or height are not equal`() {
        assertNotEquals(PixelSize(widthPx = 80, heightPx = 110), PixelSize(widthPx = 81, heightPx = 110))
        assertNotEquals(PixelSize(widthPx = 80, heightPx = 110), PixelSize(widthPx = 80, heightPx = 111))
    }

    @Test
    fun `widthPx and heightPx are plain Kotlin Int fields, not any android type`() {
        val widthField = PixelSize::class.java.getDeclaredField("widthPx")
        val heightField = PixelSize::class.java.getDeclaredField("heightPx")

        assertEquals(Int::class.javaPrimitiveType, widthField.type)
        assertEquals(Int::class.javaPrimitiveType, heightField.type)
    }
}
