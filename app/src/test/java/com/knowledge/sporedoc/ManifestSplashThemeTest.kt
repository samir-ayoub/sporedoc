package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-8 R4 / acceptance criterion:
 * "Dado o AndroidManifest.xml, quando se lê a declaração da MainActivity, então o seu tema é
 * @style/Theme.SporeDoc.Starting."
 *
 * Read as plain text because no Android/aapt runtime is available in a JVM unit test.
 */
class ManifestSplashThemeTest {

    @Test
    fun `MainActivity declares the starting splash theme`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val activityBlock = Regex(
            """<activity[^>]*android:name="\.MainActivity"[\s\S]*?(?:/>|</activity>)"""
        ).find(manifest)

        requireNotNull(activityBlock) { "Expected to find the MainActivity <activity> declaration" }
        assertTrue(
            "Expected MainActivity's android:theme to be @style/Theme.SporeDoc.Starting, " +
                "found block: ${activityBlock.value}",
            activityBlock.value.contains("""android:theme="@style/Theme.SporeDoc.Starting"""")
        )
    }
}
