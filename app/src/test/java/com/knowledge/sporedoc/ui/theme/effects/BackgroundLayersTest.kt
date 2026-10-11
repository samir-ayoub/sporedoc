package com.knowledge.sporedoc.ui.theme.effects

import org.junit.Assert.assertEquals
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
}
