package com.knowledge.sporedoc.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SD-22 R3/R4: one ROTATE LEFT tap accumulates 90° counterclockwise, wrapping at 360°, so
 * four consecutive taps return a page to the orientation it had when the rotate-mode state
 * was entered.
 */
class AccumulateRotationTest {

    private val accumulateRotation = AccumulateRotation()

    @Test
    fun `from 0 degrees one tap moves to 90`() {
        assertEquals(90, accumulateRotation(0))
    }

    @Test
    fun `from 90 degrees one tap moves to 180`() {
        assertEquals(180, accumulateRotation(90))
    }

    @Test
    fun `from 180 degrees one tap moves to 270`() {
        assertEquals(270, accumulateRotation(180))
    }

    @Test
    fun `from 270 degrees one tap wraps back to 0`() {
        assertEquals(0, accumulateRotation(270))
    }

    @Test
    fun `four consecutive taps starting from 0 degrees return to 0 degrees`() {
        var degrees = 0
        repeat(4) { degrees = accumulateRotation(degrees) }
        assertEquals(0, degrees)
    }

    @Test
    fun `four consecutive taps starting from 180 degrees return to 180 degrees`() {
        var degrees = 180
        val start = degrees
        repeat(4) { degrees = accumulateRotation(degrees) }
        assertEquals(start, degrees)
    }
}
