package com.knowledge.sporedoc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-8 R3 / acceptance criterion:
 * "Dados res/drawable/splash_icon.xml e res/drawable/splash_mark.xml, quando se comparam com
 * os ficheiros homónimos do brand kit, então o conteúdo é o mesmo."
 */
class SplashDrawableResourcesTest {

    private val resDrawableDir = File("src/main/res/drawable")
    private val brandKitDrawableDir = File("../docs/product/sporedoc-brand-kit/android/res/drawable")

    @Test
    fun `splash_icon content matches the brand kit file exactly`() {
        val projectFile = File(resDrawableDir, "splash_icon.xml")
        val kitFile = File(brandKitDrawableDir, "splash_icon.xml")
        assertTrue("Expected brand kit file to exist: ${kitFile.path}", kitFile.exists())
        assertTrue("Expected project file to exist: ${projectFile.path}", projectFile.exists())
        assertEquals(kitFile.readText(), projectFile.readText())
    }

    @Test
    fun `splash_mark content matches the brand kit file exactly`() {
        val projectFile = File(resDrawableDir, "splash_mark.xml")
        val kitFile = File(brandKitDrawableDir, "splash_mark.xml")
        assertTrue("Expected brand kit file to exist: ${kitFile.path}", kitFile.exists())
        assertTrue("Expected project file to exist: ${projectFile.path}", projectFile.exists())
        assertEquals(kitFile.readText(), projectFile.readText())
    }
}
