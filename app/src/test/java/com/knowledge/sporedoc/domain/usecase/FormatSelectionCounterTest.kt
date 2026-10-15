package com.knowledge.sporedoc.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [FormatSelectionCounter]: zero is a special case with no padding (confirmed
 * product decision), 1..9 are zero-padded to two digits, and counts of 10 or more grow
 * unpadded, as required by R3/R5 and their acceptance criteria.
 */
class FormatSelectionCounterTest {

    @Test
    fun `zero selected has no leading zero`() {
        assertEquals("0", FormatSelectionCounter()(0))
    }

    @Test
    fun `single digit counts below 10 are zero-padded to two digits`() {
        assertEquals("01", FormatSelectionCounter()(1))
        assertEquals("03", FormatSelectionCounter()(3))
        assertEquals("09", FormatSelectionCounter()(9))
    }

    @Test
    fun `counts of 10 or more are shown unpadded`() {
        assertEquals("10", FormatSelectionCounter()(10))
        assertEquals("11", FormatSelectionCounter()(11))
        assertEquals("100", FormatSelectionCounter()(100))
    }
}
