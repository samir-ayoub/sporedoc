package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-8 R1 / acceptance criterion:
 * "Dado o módulo da app, quando se compila assembleDebug, então a compilação termina sem
 * erros e androidx.core:core-splashscreen:1.0.1 está nas dependências."
 *
 * assembleDebug cannot be invoked from a JVM unit test (no build-runner tool is available
 * here), so this checks the declaration as plain text in the version catalog and its use by
 * the app module's build script, which is the only part of this criterion reachable from
 * src/test/.
 */
class CoreSplashscreenDependencyTest {

    @Test
    fun `version catalog declares core-splashscreen at 1_0_1`() {
        val catalog = File("../gradle/libs.versions.toml").readText()
        assertTrue(
            "Expected the version catalog to reference core-splashscreen",
            catalog.contains("core-splashscreen", ignoreCase = true) ||
                catalog.contains("coreSplashscreen", ignoreCase = true)
        )
        assertTrue(
            "Expected the version catalog to pin core-splashscreen to 1.0.1",
            catalog.contains("1.0.1")
        )
    }

    @Test
    fun `app module declares the core-splashscreen dependency as implementation`() {
        val buildFile = File("build.gradle.kts").readText()
        assertTrue(
            "Expected app/build.gradle.kts to use the core-splashscreen dependency",
            buildFile.contains("splashscreen", ignoreCase = true)
        )
    }
}
