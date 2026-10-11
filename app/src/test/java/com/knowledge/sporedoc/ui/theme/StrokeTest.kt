package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SD-1 R5 / acceptance criterion:
 * "Dado o tema da app, quando se leem os tokens de espessura e de espaçamento, então os
 * valores correspondem a tokens.stroke e tokens.spacing."
 *
 * Values from docs/design/sporedoc-handover.json -> tokens.stroke.
 */
class StrokeTest {

    @Test
    fun `stroke widths match the handover values`() {
        assertEquals(0.5.dp, SporeDocStroke.hairlineDp)
        assertEquals(1.dp, SporeDocStroke.frameDp)
        assertEquals(1.dp, SporeDocStroke.iconStrokeDp)
    }

    @Test
    fun `icon join and cap match the handover values`() {
        assertEquals(StrokeJoin.Miter, SporeDocStroke.iconJoin)
        assertEquals(StrokeCap.Square, SporeDocStroke.iconCap)
    }
}
