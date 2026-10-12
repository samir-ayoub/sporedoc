package com.knowledge.sporedoc.ui.documents

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [isDocumentsHeaderCollapsed], the pure decision function that drives the
 * Documents header's collapsed/expanded state from the list's scroll position (SD-16, R1/R2/R5).
 */
class DocumentsHeaderCollapseTest {

    @Test
    fun `list at rest at the very top is not collapsed`() {
        assertFalse(
            isDocumentsHeaderCollapsed(
                firstVisibleItemIndex = 0,
                firstVisibleItemScrollOffset = 0
            )
        )
    }

    @Test
    fun `small scroll offset from the top collapses the header`() {
        assertTrue(
            isDocumentsHeaderCollapsed(
                firstVisibleItemIndex = 0,
                firstVisibleItemScrollOffset = 1
            )
        )
    }

    @Test
    fun `large scroll offset from the top collapses the header`() {
        assertTrue(
            isDocumentsHeaderCollapsed(
                firstVisibleItemIndex = 0,
                firstVisibleItemScrollOffset = 500
            )
        )
    }

    @Test
    fun `nonzero first visible item index collapses the header even with zero offset`() {
        assertTrue(
            isDocumentsHeaderCollapsed(
                firstVisibleItemIndex = 1,
                firstVisibleItemScrollOffset = 0
            )
        )
    }

    @Test
    fun `nonzero first visible item index with nonzero offset collapses the header`() {
        assertTrue(
            isDocumentsHeaderCollapsed(
                firstVisibleItemIndex = 3,
                firstVisibleItemScrollOffset = 42
            )
        )
    }

    @Test
    fun `scrolling back to exactly the top expands the header again`() {
        // Simulates R4: after being collapsed, returning to index 0 / offset 0 must be
        // indistinguishable from never having scrolled (R1), since R5 says the state depends
        // only on the current scroll position, not on history.
        val whileScrolled = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 2,
            firstVisibleItemScrollOffset = 10
        )
        val backAtTop = isDocumentsHeaderCollapsed(
            firstVisibleItemIndex = 0,
            firstVisibleItemScrollOffset = 0
        )

        assertTrue(whileScrolled)
        assertFalse(backAtTop)
    }
}
