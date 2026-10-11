package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * SD-1 R2: "As duas famílias de tokens.typography estão disponíveis como fontes da app:
 * Anton para display e JetBrains Mono (pesos 300, 400 e 500) para mono."
 *
 * The three weights themselves are exercised indirectly through SporeDocTypography
 * (sectionLabel uses weight 500, fileMeta/pageIndicator use weight 300, the rest use 400),
 * because FontFamily's internal font list is not stable public API to assert against directly
 * on the JVM without an Android/Robolectric runtime. See tester report for this gap.
 */
class FontsTest {

    @Test
    fun `display family is bundled and is not the system default`() {
        assertNotNull(SporeDocFonts.display)
        assertNotEquals(FontFamily.Default, SporeDocFonts.display)
        assertNotEquals(FontFamily.SansSerif, SporeDocFonts.display)
    }

    @Test
    fun `mono family is bundled and is not the system default`() {
        assertNotNull(SporeDocFonts.mono)
        assertNotEquals(FontFamily.Default, SporeDocFonts.mono)
        assertNotEquals(FontFamily.Monospace, SporeDocFonts.mono)
    }

    @Test
    fun `display and mono are distinct families`() {
        assertNotEquals(SporeDocFonts.display, SporeDocFonts.mono)
    }
}
