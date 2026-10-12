package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-15: opening a PDF from another app (Files) while SporeDoc was already running from the
 * launcher icon left two separate instances in Recents, each its own task. `singleTop` only
 * collapses a relaunch that targets the same task's top; it does nothing when the activity is
 * started from a different task context, which is exactly how the launcher and an external
 * VIEW/SEND intent each start it. `singleTask` makes the activity a singleton task system-wide,
 * so every entry point reuses the existing instance via [MainActivity.onNewIntent] instead of
 * spawning a new one.
 *
 * Read as plain text because no Android/aapt runtime is available in a JVM unit test (same
 * approach as [ManifestSplashThemeTest]).
 */
class ManifestLaunchModeTest {

    @Test
    fun `MainActivity declares singleTask so every entry point reuses the same instance`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val activityBlock = Regex(
            """<activity[^>]*android:name="\.MainActivity"[\s\S]*?</activity>"""
        ).find(manifest)

        requireNotNull(activityBlock) { "Expected to find the MainActivity <activity> declaration" }
        assertTrue(
            "Expected MainActivity's android:launchMode to be singleTask, " +
                "found block: ${activityBlock.value}",
            activityBlock.value.contains("""android:launchMode="singleTask"""")
        )
    }
}
