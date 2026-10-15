package com.knowledge.sporedoc.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for the shared [formatSizeMb] rule: one decimal place, HALF_UP rounding, and a
 * minimum of "0.1" so a tiny file is never shown as "0.0 MB". This is the rule
 * [com.knowledge.sporedoc.domain.usecase.FormatDocumentMetaLine] already relies on and
 * that [com.knowledge.sporedoc.domain.usecase.SummarizeSelectedDocuments] reuses.
 */
class MegabyteFormatTest {

    @Test
    fun `formats a typical size to one decimal place`() {
        assertEquals("1.4", formatSizeMb(1_468_006L))
    }

    @Test
    fun `tiny sizes are floored to the 0,1 minimum and never shown as 0,0`() {
        assertEquals("0.1", formatSizeMb(20_000L))
    }

    @Test
    fun `zero bytes is clamped to the 0,1 minimum`() {
        assertEquals("0.1", formatSizeMb(0L))
    }

    @Test
    fun `rounds half-up to one decimal place`() {
        assertEquals("1.4", formatSizeMb(1_450_000L))
    }
}
