package com.knowledge.sporedoc.ui.theme.effects

import com.knowledge.sporedoc.ui.theme.SporeDocColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-1 R6 / acceptance criteria:
 * "Dado o composable de fundo, quando se pede o fundo do ecrã de documentos, então as
 * camadas desenhadas são a de base e a de bruma, e não as duas específicas do visualizador."
 * "Dado o composable de fundo, quando se pede o fundo do visualizador, então as camadas
 * desenhadas são a de base, o halo atrás da página e a de magma, e não a bruma do ecrã
 * de documentos."
 *
 * This is the only part of R6 reachable without a Compose/Android test runner, per the
 * Architect's public contract: backgroundLayers(screen) is the pure seam for layer selection.
 */
class BackgroundLayersTest {

    @Test
    fun `documents screen gets exactly base and nebula haze layers`() {
        val layers = backgroundLayers(BackgroundScreen.Documents)
        assertEquals(2, layers.size)
    }

    @Test
    fun `documents screen layers do not include the viewer-only halo or magma layers`() {
        val documentsLayers = backgroundLayers(BackgroundScreen.Documents)
        val viewerLayers = backgroundLayers(BackgroundScreen.Viewer)

        // The two viewer-specific layers (halo, magma) must not appear for Documents.
        val viewerOnlyLayers = viewerLayers.drop(1) // everything after the shared base layer
        viewerOnlyLayers.forEach { viewerOnlyLayer ->
            assertEquals(false, documentsLayers.contains(viewerOnlyLayer))
        }
    }

    @Test
    fun `viewer screen gets exactly base, halo and magma layers`() {
        val layers = backgroundLayers(BackgroundScreen.Viewer)
        assertEquals(3, layers.size)
    }

    @Test
    fun `viewer screen layers do not include the documents-only nebula haze layer`() {
        val documentsLayers = backgroundLayers(BackgroundScreen.Documents)
        val viewerLayers = backgroundLayers(BackgroundScreen.Viewer)

        val nebulaHazeLayer = documentsLayers[1] // base is shared, index 1 is Documents-only
        assertEquals(false, viewerLayers.contains(nebulaHazeLayer))
    }

    @Test
    fun `both screens share the same base layer as the first entry`() {
        val documentsLayers = backgroundLayers(BackgroundScreen.Documents)
        val viewerLayers = backgroundLayers(BackgroundScreen.Viewer)

        assertEquals(documentsLayers[0], viewerLayers[0])
    }

    /**
     * SD-8 R9 / Architect's technical approach for `BackgroundScreen.Splash`:
     * "reuses the existing shared baseLayer plus one new haze layer using
     * SporeDocColors.nebulaHaze, centered on screen, radius ≈ 75% of width, alpha ≈ 0.30".
     */
    @Test
    fun `splash screen gets exactly the shared base layer plus one nebula haze layer`() {
        val documentsLayers = backgroundLayers(BackgroundScreen.Documents)
        val splashLayers = backgroundLayers(BackgroundScreen.Splash)

        assertEquals(2, splashLayers.size)
        assertEquals(documentsLayers[0], splashLayers[0]) // shared base layer, same as the other screens
    }

    @Test
    fun `splash haze layer is centered on screen using the nebulaHaze token at about 30 percent alpha`() {
        val hazeLayer = backgroundLayers(BackgroundScreen.Splash)[1]

        assertEquals(0.50f, hazeLayer.centerXFraction, 0.01f)
        assertEquals(0.50f, hazeLayer.centerYFraction, 0.01f)

        val hazeStop = hazeLayer.stops.firstOrNull { it.color.alpha > 0f }
        requireNotNull(hazeStop) { "Expected the haze layer to have a visible (non-transparent) stop" }
        assertEquals(SporeDocColors.nebulaHaze.red, hazeStop.color.red, 0.01f)
        assertEquals(SporeDocColors.nebulaHaze.green, hazeStop.color.green, 0.01f)
        assertEquals(SporeDocColors.nebulaHaze.blue, hazeStop.color.blue, 0.01f)
        assertEquals(0.30f, hazeStop.color.alpha, 0.05f)
    }

    /**
     * SD-8 R9: "o fundo e a vinheta passam a aceitar uma opacidade, com o valor atual por
     * omissão". `ScreenBackground` is a `@Composable`, so — per this project's established
     * pattern (see GrainTest) — its rendered output cannot be asserted from a JVM unit test;
     * only the compiled parameter list is reachable here.
     */
    @Test
    fun `ScreenBackground exposes a float opacity parameter`() {
        val method = Class.forName("com.knowledge.sporedoc.ui.theme.effects.BackgroundKt")
            .declaredMethods
            .firstOrNull { it.name == "ScreenBackground" }

        requireNotNull(method) { "Expected a top-level ScreenBackground(...) function in BackgroundKt" }

        val hasOpacityParameter = method.parameterTypes.any { it == Float::class.javaPrimitiveType }
        assertTrue(
            "Expected ScreenBackground(...) to declare a float opacity parameter per the SD-8 contract",
            hasOpacityParameter
        )
    }
}
