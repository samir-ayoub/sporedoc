package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-12 REQ-1/REQ-2/REQ-3 and their acceptance criteria:
 * "Dadas as declarações da app, quando o sistema procura apps capazes de ver/receber
 * application/pdf numa URI content://, file://, ou pela folha de partilha, então a
 * SporeDoc consta da lista."
 *
 * Read as plain text because no Android/aapt runtime is available in a JVM unit test
 * (same approach as [ManifestSplashThemeTest], the existing precedent for manifest facts).
 */
class ManifestExternalPdfIntentFiltersTest {

    private fun mainActivityBlock(): String {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val activityBlock = Regex(
            """<activity[^>]*android:name="\.MainActivity"[\s\S]*?</activity>"""
        ).find(manifest)
        requireNotNull(activityBlock) { "Expected to find the MainActivity <activity> declaration with a closing tag" }
        return activityBlock.value
    }

    @Test
    fun `MainActivity declares an ACTION_VIEW filter for application-pdf over a content scheme`() {
        val block = mainActivityBlock()
        val filters = Regex("""<intent-filter>[\s\S]*?</intent-filter>""").findAll(block).map { it.value }.toList()

        val matching = filters.filter {
            it.contains("""android.intent.action.VIEW""") &&
                it.contains("""android.intent.category.DEFAULT""") &&
                it.contains("""mimeType="application/pdf"""") &&
                it.contains("""scheme="content"""")
        }

        assertTrue(
            "Expected an ACTION_VIEW intent-filter with category DEFAULT, mimeType application/pdf " +
                "and scheme content, found filters: $filters",
            matching.isNotEmpty()
        )
    }

    @Test
    fun `MainActivity declares an ACTION_VIEW filter for application-pdf over a file scheme`() {
        val block = mainActivityBlock()
        val filters = Regex("""<intent-filter>[\s\S]*?</intent-filter>""").findAll(block).map { it.value }.toList()

        val matching = filters.filter {
            it.contains("""android.intent.action.VIEW""") &&
                it.contains("""android.intent.category.DEFAULT""") &&
                it.contains("""mimeType="application/pdf"""") &&
                it.contains("""scheme="file"""")
        }

        assertTrue(
            "Expected an ACTION_VIEW intent-filter with category DEFAULT, mimeType application/pdf " +
                "and scheme file, found filters: $filters",
            matching.isNotEmpty()
        )
    }

    @Test
    fun `MainActivity declares an ACTION_SEND filter for application-pdf for the share sheet`() {
        val block = mainActivityBlock()
        val filters = Regex("""<intent-filter>[\s\S]*?</intent-filter>""").findAll(block).map { it.value }.toList()

        val matching = filters.filter {
            it.contains("""android.intent.action.SEND""") &&
                it.contains("""android.intent.category.DEFAULT""") &&
                it.contains("""mimeType="application/pdf"""")
        }

        assertTrue(
            "Expected an ACTION_SEND intent-filter with category DEFAULT and mimeType application/pdf, " +
                "found filters: $filters",
            matching.isNotEmpty()
        )
    }

    @Test
    fun `MainActivity keeps its original MAIN-LAUNCHER filter untouched for a plain icon launch`() {
        val block = mainActivityBlock()
        val filters = Regex("""<intent-filter>[\s\S]*?</intent-filter>""").findAll(block).map { it.value }.toList()

        val matching = filters.filter {
            it.contains("""android.intent.action.MAIN""") &&
                it.contains("""android.intent.category.LAUNCHER""")
        }

        assertTrue(
            "Expected the original MAIN/LAUNCHER intent-filter to still be present, found filters: $filters",
            matching.isNotEmpty()
        )
    }
}
