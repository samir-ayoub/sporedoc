package com.knowledge.sporedoc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-7 acceptance criteria. Read as plain text/tree-walk because no Android/aapt runtime is
 * available in a JVM unit test; this checks literal XML content and file presence/absence, not
 * that the adaptive icon parses or renders on-device (that is covered by manual validation and
 * by `assembleDebug`'s AAPT2 resource-linking step, outside this test's reach).
 */
class LauncherIconResourcesTest {

    private val resDir = File("src/main/res")
    private val brandKitDrawableDir =
        File("../docs/product/sporedoc-brand-kit/android/res/drawable")

    private fun res(relativePath: String): File = File(resDir, relativePath)

    @Test
    fun `ic_launcher declares background, foreground and monochrome layers from the brand kit`() {
        val xml = res("mipmap-anydpi/ic_launcher.xml").readText()
        assertTrue(
            "Expected <background> pointing to @mipmap/ic_launcher_background",
            xml.contains("""android:drawable="@mipmap/ic_launcher_background"""")
        )
        assertTrue(
            "Expected <foreground> pointing to @drawable/ic_launcher_foreground",
            xml.contains("""android:drawable="@drawable/ic_launcher_foreground"""")
        )
        assertTrue(
            "Expected <monochrome> pointing to @drawable/ic_launcher_monochrome",
            xml.contains("""android:drawable="@drawable/ic_launcher_monochrome"""")
        )
    }

    @Test
    fun `ic_launcher_round declares the same three layers as ic_launcher`() {
        val xml = res("mipmap-anydpi/ic_launcher_round.xml").readText()
        assertTrue(
            "Expected <background> pointing to @mipmap/ic_launcher_background",
            xml.contains("""android:drawable="@mipmap/ic_launcher_background"""")
        )
        assertTrue(
            "Expected <foreground> pointing to @drawable/ic_launcher_foreground",
            xml.contains("""android:drawable="@drawable/ic_launcher_foreground"""")
        )
        assertTrue(
            "Expected <monochrome> pointing to @drawable/ic_launcher_monochrome",
            xml.contains("""android:drawable="@drawable/ic_launcher_monochrome"""")
        )
    }

    @Test
    fun `ic_launcher_foreground content matches the brand kit file exactly`() {
        val projectFile = res("drawable/ic_launcher_foreground.xml")
        val kitFile = File(brandKitDrawableDir, "ic_launcher_foreground.xml")
        assertTrue("Expected brand kit file to exist: ${kitFile.path}", kitFile.exists())
        assertTrue("Expected project file to exist: ${projectFile.path}", projectFile.exists())
        assertEquals(kitFile.readText(), projectFile.readText())
    }

    @Test
    fun `ic_launcher_monochrome content matches the brand kit file exactly`() {
        val projectFile = res("drawable/ic_launcher_monochrome.xml")
        val kitFile = File(brandKitDrawableDir, "ic_launcher_monochrome.xml")
        assertTrue("Expected brand kit file to exist: ${kitFile.path}", kitFile.exists())
        assertTrue(
            "Expected ic_launcher_monochrome.xml to exist in the project: ${projectFile.path}",
            projectFile.exists()
        )
        assertEquals(kitFile.readText(), projectFile.readText())
    }

    @Test
    fun `template background drawable is removed`() {
        val file = res("drawable/ic_launcher_background.xml")
        assertFalse("Expected ${file.path} to be removed", file.exists())
    }

    @Test
    fun `per-density launcher bitmaps are removed for every density`() {
        val densities = listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
        for (density in densities) {
            val icon = res("mipmap-$density/ic_launcher.webp")
            val round = res("mipmap-$density/ic_launcher_round.webp")
            assertFalse("Expected ${icon.path} to be removed", icon.exists())
            assertFalse("Expected ${round.path} to be removed", round.exists())
        }
    }

    @Test
    fun `no resource file references the removed background drawable`() {
        val offenders = resDir.walkTopDown()
            .filter { it.isFile && it.extension == "xml" }
            .filter { it.readText().contains("@drawable/ic_launcher_background") }
            .map { it.path }
            .toList()
        assertTrue(
            "Expected no resource file to reference @drawable/ic_launcher_background, " +
                "found: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `no project source file references the removed background drawable`() {
        val mainDir = File("src/main")
        val offenders = mainDir.walkTopDown()
            .filter { it.isFile && (it.extension == "xml" || it.extension == "kt") }
            .filter { it.readText().contains("@drawable/ic_launcher_background") }
            .map { it.path }
            .toList()
        assertTrue(
            "Expected no file under src/main (resources, Kotlin sources or the manifest) to " +
                "reference @drawable/ic_launcher_background, found: $offenders",
            offenders.isEmpty()
        )
    }
}
