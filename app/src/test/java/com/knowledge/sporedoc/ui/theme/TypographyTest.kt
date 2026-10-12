package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SD-1 R3 / acceptance criterion:
 * "Dado o tema da app, quando se lê cada estilo de tokens.typography.styles pelo nome, então
 * a família, o peso, o tamanho, o espaçamento entre letras e a cor correspondem ao JSON."
 *
 * Values come from docs/design/sporedoc-handover.json -> tokens.typography.styles.
 *
 * Note: maxLines/overflow for fileName and appBarTitle are not asserted here because the
 * Architect explicitly left their exposed shape ("paired constants or a small wrapper") to the
 * Dev's discretion. See tester report for this gap.
 */
class TypographyTest {

    @Test
    fun `wordmark uses the display family at 60sp with 15sp letter spacing in ivory`() {
        val style = SporeDocTypography.wordmark
        assertEquals(SporeDocFonts.display, style.fontFamily)
        assertEquals(60.sp, style.fontSize)
        assertEquals(15.sp, style.letterSpacing)
        assertEquals(SporeDocColors.ivory, style.color)
    }

    @Test
    fun `subtitle uses mono 400 at 8sp with 4_6sp letter spacing in goldMatte`() {
        val style = SporeDocTypography.subtitle
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(400), style.fontWeight)
        assertEquals(8.sp, style.fontSize)
        assertEquals(4.6.sp, style.letterSpacing)
        assertEquals(SporeDocColors.goldMatte, style.color)
    }

    @Test
    fun `sectionLabel uses mono 500 at 9_5sp with 3_4sp letter spacing in goldSection`() {
        val style = SporeDocTypography.sectionLabel
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(500), style.fontWeight)
        assertEquals(9.5.sp, style.fontSize)
        assertEquals(3.4.sp, style.letterSpacing)
        assertEquals(SporeDocColors.goldSection, style.color)
    }

    @Test
    fun `fileName uses mono 400 at 13sp in ivory`() {
        val style = SporeDocTypography.fileName
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(400), style.fontWeight)
        assertEquals(13.sp, style.fontSize)
        assertEquals(SporeDocColors.ivory, style.color)
    }

    @Test
    fun `fileMeta uses mono 300 at 10sp with 0_6sp letter spacing in ivoryMeta`() {
        val style = SporeDocTypography.fileMeta
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(300), style.fontWeight)
        assertEquals(10.sp, style.fontSize)
        assertEquals(0.6.sp, style.letterSpacing)
        assertEquals(SporeDocColors.ivoryMeta, style.color)
    }

    @Test
    fun `textAction uses mono 400 at 10_5sp with 2_4sp letter spacing in magma`() {
        val style = SporeDocTypography.textAction
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(400), style.fontWeight)
        assertEquals(10.5.sp, style.fontSize)
        assertEquals(2.4.sp, style.letterSpacing)
        assertEquals(SporeDocColors.magma, style.color)
    }

    @Test
    fun `primaryButton uses mono 400 at 12sp with 7sp letter spacing in ivory`() {
        val style = SporeDocTypography.primaryButton
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(400), style.fontWeight)
        assertEquals(12.sp, style.fontSize)
        assertEquals(7.sp, style.letterSpacing)
        assertEquals(SporeDocColors.ivory, style.color)
    }

    @Test
    fun `exportButton uses mono 400 at 11_5sp with 4_5sp letter spacing in ivory`() {
        val style = SporeDocTypography.exportButton
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(400), style.fontWeight)
        assertEquals(11.5.sp, style.fontSize)
        assertEquals(4.5.sp, style.letterSpacing)
        assertEquals(SporeDocColors.ivory, style.color)
    }

    @Test
    fun `appBarTitle uses mono 400 at 12sp with 0_6sp letter spacing in ivory`() {
        val style = SporeDocTypography.appBarTitle
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(400), style.fontWeight)
        assertEquals(12.sp, style.fontSize)
        assertEquals(0.6.sp, style.letterSpacing)
        assertEquals(SporeDocColors.ivory, style.color)
    }

    @Test
    fun `pageIndicator uses mono 300 at 10sp with 4sp letter spacing in ivoryMeta`() {
        val style = SporeDocTypography.pageIndicator
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(FontWeight(300), style.fontWeight)
        assertEquals(10.sp, style.fontSize)
        assertEquals(4.sp, style.letterSpacing)
        assertEquals(SporeDocColors.ivoryMeta, style.color)
    }

    /**
     * SD-8, Architect's public contract for `ui/theme/Typography.kt`:
     * "splashBaseline: fontFamily = SporeDocFonts.mono, fontSize = 8.sp, letterSpacing = 3.4.sp,
     * color = SporeDocColors.ivoryMeta".
     */
    @Test
    fun `splashBaseline uses mono at 8sp with 3_4sp letter spacing in ivoryMeta`() {
        val style = SporeDocTypography.splashBaseline
        assertEquals(SporeDocFonts.mono, style.fontFamily)
        assertEquals(8.sp, style.fontSize)
        assertEquals(3.4.sp, style.letterSpacing)
        assertEquals(SporeDocColors.ivoryMeta, style.color)
    }
}
