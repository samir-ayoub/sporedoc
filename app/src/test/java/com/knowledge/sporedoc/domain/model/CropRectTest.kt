package com.knowledge.sporedoc.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-23 R3: "O retângulo inicial dos quatro nós tem uma posição e um tamanho fixos sobre a
 * página, iguais sempre que se entra neste estado; não há deteção automática de margens."
 *
 * [CropRect] coordinates are fractions of the page's width/height (0f..1f), with
 * `left < right` and `top < bottom`, so the same rectangle can describe both the on-screen
 * page box and the PDF's native page size at save time without the domain touching either.
 */
class CropRectTest {

    @Test
    fun `Initial has left and top coordinates within the 0 to 1 fraction range`() {
        val initial = CropRect.Initial

        assertTrue(initial.left in 0f..1f)
        assertTrue(initial.top in 0f..1f)
    }

    @Test
    fun `Initial has right and bottom coordinates within the 0 to 1 fraction range`() {
        val initial = CropRect.Initial

        assertTrue(initial.right in 0f..1f)
        assertTrue(initial.bottom in 0f..1f)
    }

    @Test
    fun `Initial describes a non-collapsed rectangle, left strictly less than right`() {
        assertTrue(CropRect.Initial.left < CropRect.Initial.right)
    }

    @Test
    fun `Initial describes a non-collapsed rectangle, top strictly less than bottom`() {
        assertTrue(CropRect.Initial.top < CropRect.Initial.bottom)
    }

    @Test
    fun `Initial is the same value every time it is read, so re-entering crop mode is always identical`() {
        val first = CropRect.Initial
        val second = CropRect.Initial

        assertEquals(first, second)
    }
}
