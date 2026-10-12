package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-8 R4 / acceptance criterion:
 * "Dada a MainActivity, quando se lê o onCreate, então installSplashScreen() é chamado antes de
 * super.onCreate(...)."
 *
 * `MainActivity` is excluded from Kover and has no JVM-testable seam for its Android lifecycle
 * behavior, so this is read as plain source text, which is how the spec itself phrases this
 * acceptance criterion ("quando se lê o onCreate").
 */
class MainActivitySplashTest {

    @Test
    fun `onCreate calls installSplashScreen before super onCreate`() {
        val source = File("src/main/java/com/knowledge/sporedoc/MainActivity.kt").readText()

        val installIndex = source.indexOf("installSplashScreen()")
        val superOnCreateIndex = source.indexOf("super.onCreate(")

        assertTrue("Expected installSplashScreen() to be called in MainActivity", installIndex >= 0)
        assertTrue("Expected super.onCreate(...) to be called in MainActivity", superOnCreateIndex >= 0)
        assertTrue(
            "Expected installSplashScreen() to be called before super.onCreate(...)",
            installIndex < superOnCreateIndex
        )
    }
}
