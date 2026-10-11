package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SD-1 R1 / acceptance criterion:
 * "Dado o tema da app, quando se lê cada token de cor pelo nome, então o valor corresponde
 * ao de tokens.color, incluindo o canal alfa dos tokens que o especificam."
 *
 * Values come straight from docs/design/sporedoc-handover.json -> tokens.color.
 */
class ColorsTest {

    @Test
    fun `solid colors match the handover hex values`() {
        assertEquals(Color(0xFF12091F), SporeDocColors.bgCenter)
        assertEquals(Color(0xFF0A0612), SporeDocColors.bgMid)
        assertEquals(Color(0xFF030305), SporeDocColors.bgEdge)
        assertEquals(Color(0xFF541E70), SporeDocColors.nebulaHaze)
        assertEquals(Color(0xFFFDFBF7), SporeDocColors.ivory)
        assertEquals(Color(0xFFD4AF37), SporeDocColors.goldBrushed)
        assertEquals(Color(0xFFB89047), SporeDocColors.goldMatte)
        assertEquals(Color(0xFF9C8152), SporeDocColors.goldSection)
        assertEquals(Color(0xFFE65C00), SporeDocColors.magma)
        assertEquals(Color(0xFF39FF14), SporeDocColors.laserGreen)
        assertEquals(Color(0xFFE4DDCD), SporeDocColors.paper)
        assertEquals(Color(0xFFCFC8B8), SporeDocColors.paperThumb)
        assertEquals(Color(0xFF17131C), SporeDocColors.ink)
        assertEquals(Color(0xFF1A1520), SporeDocColors.inkThumb)
        assertEquals(Color(0xFF7F786C), SporeDocColors.inkRule)
        assertEquals(Color(0xFF1E1033), SporeDocColors.orbCore)
        assertEquals(Color(0xFF0C0716), SporeDocColors.orbMid)
        assertEquals(Color(0xFF050309), SporeDocColors.orbEdge)
    }

    @Test
    fun `colors with alpha match the handover ARGB values`() {
        // ivory @ 42/45/50% alpha
        assertEquals(Color(0x6BFDFBF7), SporeDocColors.ivoryMeta)
        assertEquals(Color(0x73FDFBF7), SporeDocColors.ivoryIcon)
        assertEquals(Color(0x80FDFBF7), SporeDocColors.ivoryMuted)
        // goldBrushed/goldMatte-derived alpha tokens
        assertEquals(Color(0x59B89047), SporeDocColors.goldDividerSoft)
        assertEquals(Color(0x73B89047), SporeDocColors.goldDividerFooter)
        assertEquals(Color(0x12D4AF37), SporeDocColors.brassVeil)
    }
}
