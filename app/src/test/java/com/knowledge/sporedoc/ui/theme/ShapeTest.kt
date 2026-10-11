package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-1 R4 / acceptance criterion:
 * "Dado o tema da app, quando se lê a forma de qualquer superfície ou botão, então o raio
 * dos cantos é 0dp."
 *
 * tokens.shape.cornerRadiusDp = 0 for every surface/button shape in SporeDocShapes.
 */
class ShapeTest {

    private val density = Density(1f)
    private val size = Size(100f, 100f)

    private fun isSquareOutline(outline: Outline): Boolean = when (outline) {
        is Outline.Rectangle -> true
        else -> false
    }

    @Test
    fun `extraSmall shape has zero corner radius`() {
        val outline = SporeDocShapes.extraSmall.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(isSquareOutline(outline))
    }

    @Test
    fun `small shape has zero corner radius`() {
        val outline = SporeDocShapes.small.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(isSquareOutline(outline))
    }

    @Test
    fun `medium shape has zero corner radius`() {
        val outline = SporeDocShapes.medium.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(isSquareOutline(outline))
    }

    @Test
    fun `large shape has zero corner radius`() {
        val outline = SporeDocShapes.large.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(isSquareOutline(outline))
    }

    @Test
    fun `extraLarge shape has zero corner radius`() {
        val outline = SporeDocShapes.extraLarge.createOutline(size, LayoutDirection.Ltr, density)
        assertTrue(isSquareOutline(outline))
    }
}
