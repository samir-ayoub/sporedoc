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
}
