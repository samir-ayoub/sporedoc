package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-15 R2/R3 and the "QR CODE" label decision (PO decision: use literal text "QR CODE" as the
 * label, faded/disabled styling, ignoring the icon/label shown in the reference images):
 * "Dado o DocumentsScreen composto, quando se olha para o rodapé, então aparecem três caixas de
 * largura igual, pela ordem IMPORT, CAPTURE, QR CODE."
 *
 * The footer box labels are new visible-text string resources, distinct from the existing
 * accessibility description `import_content_description` ("Import PDF") and from the unchanged
 * `capture_label` ("CAPTURE").
 */
class FooterLabelsStringsTest {

    private fun stringsXml(): String = File("src/main/res/values/strings.xml").readText()

    @Test
    fun `a string resource with the exact value IMPORT exists for the footer label`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string> resource with the exact text IMPORT for the footer IMPORT box label",
            xml.contains(">IMPORT<")
        )
    }

    @Test
    fun `a string resource with the exact value QR CODE exists for the footer label`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string> resource with the exact text QR CODE for the footer QR CODE box label",
            xml.contains(">QR CODE<")
        )
    }
}
