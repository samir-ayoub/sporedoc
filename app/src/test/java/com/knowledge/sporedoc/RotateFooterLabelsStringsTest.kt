package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-22 R1: "Dado o rodapé normal, quando se toca em rotate, então o rodapé passa a mostrar
 * ROTATE LEFT, SAVE e CANCEL [...]". `CANCEL` already exists as a literal-text string
 * resource (e.g. `delete_dialog_cancel`/`rename_dialog_cancel`), so this test only covers
 * the two new labels this delivery introduces.
 */
class RotateFooterLabelsStringsTest {

    private fun stringsXml(): String = File("src/main/res/values/strings.xml").readText()

    @Test
    fun `a string resource with the exact value ROTATE LEFT exists for the rotate footer label`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string> resource with the exact text ROTATE LEFT for the rotate footer label",
            xml.contains(">ROTATE LEFT<")
        )
    }

    @Test
    fun `a string resource with the exact value SAVE exists for the rotate footer label`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string> resource with the exact text SAVE for the rotate footer label",
            xml.contains(">SAVE<")
        )
    }
}
