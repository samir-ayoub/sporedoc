package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-8 R2 / acceptance criterion:
 * "Dado res/values/themes_splash.xml, quando se lê o estilo Theme.SporeDoc.Starting, então
 * tem windowSplashScreenBackground na cor #050508, windowSplashScreenAnimatedIcon em
 * @drawable/splash_icon, windowSplashScreenAnimationDuration a 0 e postSplashScreenTheme em
 * @style/Theme.SporeDoc."
 *
 * Read as plain text because no Android/aapt runtime is available in a JVM unit test; this
 * checks literal XML content and presence, not that the theme resolves on-device.
 */
class ThemesSplashResourceTest {

    private fun themesSplashXml(): String {
        val file = File("src/main/res/values/themes_splash.xml")
        assertTrue("Expected ${file.path} to exist", file.exists())
        return file.readText()
    }

    @Test
    fun `Theme SporeDoc Starting declares the abyss background colour`() {
        val xml = themesSplashXml()
        assertTrue(
            "Expected windowSplashScreenBackground to resolve to #FF050508 (literally or via a color resource)",
            xml.contains("#FF050508") || xml.contains("#050508")
        )
        assertTrue(xml.contains("Theme.SporeDoc.Starting"))
        assertTrue(xml.contains("windowSplashScreenBackground"))
    }

    @Test
    fun `Theme SporeDoc Starting points its animated icon at splash_icon`() {
        val xml = themesSplashXml()
        assertTrue(
            xml.contains("""name="windowSplashScreenAnimatedIcon">@drawable/splash_icon<""")
        )
    }

    @Test
    fun `Theme SporeDoc Starting has animation duration zero`() {
        val xml = themesSplashXml()
        assertTrue(
            xml.contains("""name="windowSplashScreenAnimationDuration">0<""")
        )
    }

    @Test
    fun `Theme SporeDoc Starting hands off to the post-splash app theme`() {
        val xml = themesSplashXml()
        assertTrue(
            xml.contains("""name="postSplashScreenTheme">@style/Theme.SporeDoc<""")
        )
    }
}
