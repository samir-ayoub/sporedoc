package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-8 R10 / acceptance criterion:
 * "Dados os recursos de texto, quando se procura a linha da base do splash, então existe uma
 * string com KNOWLEDGE APPLICATIONS, e o wordmark e o subtítulo são as strings já usadas no
 * cabeçalho dos documentos."
 */
class StringsResourcesSplashTest {

    private fun stringsXml(): String = File("src/main/res/values/strings.xml").readText()

    @Test
    fun `a baseline string with the exact value KNOWLEDGE APPLICATIONS exists`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string> resource with the exact text KNOWLEDGE APPLICATIONS",
            xml.contains(">KNOWLEDGE APPLICATIONS<")
        )
    }
}
