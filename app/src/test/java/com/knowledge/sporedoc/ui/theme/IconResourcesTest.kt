package com.knowledge.sporedoc.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-1 R14 / acceptance criterion:
 * "Dado cada recurso vetorial de icons, quando se lê o seu conteúdo, então o path e o viewBox
 * correspondem ao JSON e não há preenchimento."
 *
 * Values from docs/design/sporedoc-handover.json -> icons. Read as plain text because no
 * Android/aapt runtime is available in a JVM unit test; this only checks the attribute
 * strings are present as literal XML, not that the drawable parses correctly on-device.
 *
 * Icon "more" is excluded from the pathData assertion: the handover only gives a prose
 * description ("Three vertical 1x4dp bars at y=1, 8, 15") with no literal path string, so its
 * exact pathData cannot be derived from the contract. See tester report for this gap.
 */
class IconResourcesTest {

    private fun drawable(name: String): String {
        val file = File("src/main/res/drawable/$name.xml")
        assertTrue("Expected drawable resource to exist: ${file.path}", file.exists())
        return file.readText()
    }

    private fun assertNoFill(xml: String) {
        assertFalse("Vector icon must not declare a fillColor", xml.contains("fillColor"))
    }

    private fun assertStrokeStyle(xml: String) {
        assertTrue(xml.contains("strokeLineJoin=\"miter\""))
        assertTrue(xml.contains("strokeLineCap=\"square\""))
        assertTrue(xml.contains("strokeWidth=\"1dp\""))
    }

    @Test
    fun `ic_import has the handover path and 24x24 viewBox, no fill`() {
        val xml = drawable("ic_import")
        assertTrue(xml.contains("viewportWidth=\"24\""))
        assertTrue(xml.contains("viewportHeight=\"24\""))
        assertTrue(xml.contains("M12 3 21.5 20.5h-19z"))
        assertTrue(xml.contains("M12 3v17.5"))
        assertTrue(xml.contains("#B89047")) // goldMatte
        assertNoFill(xml)
        assertStrokeStyle(xml)
    }

    @Test
    fun `ic_capture has the handover path and 24x24 viewBox, no fill`() {
        val xml = drawable("ic_capture")
        assertTrue(xml.contains("viewportWidth=\"24\""))
        assertTrue(xml.contains("viewportHeight=\"24\""))
        assertTrue(xml.contains("M3 8V3h5"))
        assertTrue(xml.contains("M16 3h5v5"))
        assertTrue(xml.contains("M21 16v5h-5"))
        assertTrue(xml.contains("M8 21H3v-5"))
        assertTrue(xml.contains("#D4AF37")) // goldBrushed
        assertNoFill(xml)
        assertStrokeStyle(xml)
    }

    @Test
    fun `ic_back has the handover path and 24x24 viewBox, no fill`() {
        val xml = drawable("ic_back")
        assertTrue(xml.contains("viewportWidth=\"24\""))
        assertTrue(xml.contains("viewportHeight=\"24\""))
        assertTrue(xml.contains("M21 12H3.5"))
        assertTrue(xml.contains("M10 5.5 3.5 12l6.5 6.5"))
        assertTrue(xml.contains("#FDFBF7")) // ivory
        assertNoFill(xml)
        assertStrokeStyle(xml)
    }

    @Test
    fun `ic_delete has the handover path and 24x24 viewBox, no fill`() {
        val xml = drawable("ic_delete")
        assertTrue(xml.contains("viewportWidth=\"24\""))
        assertTrue(xml.contains("viewportHeight=\"24\""))
        assertTrue(xml.contains("M2.5 4.5h19L12 21z"))
        assertTrue(xml.contains("#E65C00")) // magma
        assertNoFill(xml)
        assertStrokeStyle(xml)
    }

    @Test
    fun `ic_export has the handover path and 24x24 viewBox, no fill`() {
        val xml = drawable("ic_export")
        assertTrue(xml.contains("viewportWidth=\"24\""))
        assertTrue(xml.contains("viewportHeight=\"24\""))
        assertTrue(xml.contains("M12 15V3"))
        assertTrue(xml.contains("M7 8l5-5 5 5"))
        assertTrue(xml.contains("M4 14v7h16v-7"))
        assertTrue(xml.contains("#D4AF37")) // goldBrushed
        assertNoFill(xml)
        assertStrokeStyle(xml)
    }

    @Test
    fun `ic_more has the handover 14x20 viewBox, no fill`() {
        val xml = drawable("ic_more")
        assertTrue(xml.contains("viewportWidth=\"14\""))
        assertTrue(xml.contains("viewportHeight=\"20\""))
        assertTrue(xml.contains("#73FDFBF7")) // ivoryIcon = ivory @ 45% alpha
        assertNoFill(xml)
        assertStrokeStyle(xml)
    }
}
