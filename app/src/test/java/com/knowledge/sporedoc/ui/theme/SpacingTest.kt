package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SD-1 R5 / acceptance criterion:
 * "Dado o tema da app, quando se leem os tokens de espessura e de espaçamento, então os
 * valores correspondem a tokens.stroke e tokens.spacing."
 *
 * Values from docs/design/sporedoc-handover.json -> tokens.spacing.
 */
class SpacingTest {

    @Test
    fun `spacing tokens match the handover values`() {
        assertEquals(24.dp, SporeDocSpacing.screenGutterDp)
        assertEquals(14.dp, SporeDocSpacing.rowPaddingVerticalDp)
        assertEquals(18.dp, SporeDocSpacing.rowGapThumbToTextDp)
        assertEquals(14.dp, SporeDocSpacing.sectionTopGapDp)
        assertEquals(16.dp, SporeDocSpacing.sectionLabelPaddingTopDp)
        assertEquals(4.dp, SporeDocSpacing.sectionLabelPaddingBottomDp)
        assertEquals(44.dp, SporeDocSpacing.minTouchTargetDp)
    }

    // SD-15 R1/R2: the header no longer has an import icon (it moves into the tripartite
    // footer), so the former `importButtonSizeDp` / `importButtonPaddingEndDp` /
    // `importButtonPaddingTopDp` / `importGlyphSizeDp` tokens this test used to assert on are
    // retired by this ticket's structural plan. The new footer-box tokens it introduces have no
    // fixed names in the spec/design handover, so they are not asserted here (see tester's
    // reported test gaps).
}
