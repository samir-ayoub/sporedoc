package com.knowledge.sporedoc.ui.documents

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure scroll-threshold function introduced by SD-16:
 * [isDocumentsHeaderCollapsed] decides whether the documents header should
 * render expanded (196dp, wordmark + subtitle) or collapsed (48dp, wordmark only),
 * based only on the list's first-visible-item index/offset.
 */
class IsDocumentsHeaderCollapsedTest {

    @Test
    fun `list exactly at top is not collapsed`() {
        val result = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 0,
            firstVisibleItemScrollOffset = 0
        )

        assertFalse(result)
    }

    @Test
    fun `nonzero scroll offset on first item collapses header`() {
        val result = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 0,
            firstVisibleItemScrollOffset = 1
        )

        assertTrue(result)
    }

    @Test
    fun `first visible item beyond index zero collapses header even with zero offset`() {
        val result = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 1,
            firstVisibleItemScrollOffset = 0
        )

        assertTrue(result)
    }

    @Test
    fun `first visible item beyond index zero with nonzero offset collapses header`() {
        val result = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 3,
            firstVisibleItemScrollOffset = 120
        )

        assertTrue(result)
    }

    @Test
    fun `large scroll offset on first item still collapses header`() {
        val result = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 0,
            firstVisibleItemScrollOffset = 5000
        )

        assertTrue(result)
    }

    @Test
    fun `returning to index zero and offset zero reports expanded again`() {
        // Simulates scrolling down then back up to the exact top: the boolean
        // must flip back deterministically from the same two inputs (R4/R5).
        val whileScrolled = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 2,
            firstVisibleItemScrollOffset = 80
        )
        val backAtTop = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 0,
            firstVisibleItemScrollOffset = 0
        )

        assertTrue(whileScrolled)
        assertFalse(backAtTop)
    }
}
