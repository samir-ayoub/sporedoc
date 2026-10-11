package com.knowledge.sporedoc.ui.theme.effects

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-9, requirement 3 / acceptance criterion:
 * "Dado o componente de grão, quando é chamado com uma opacidade, então desenha a textura
 * com essa opacidade."
 *
 * `Grain` is a `@Composable`, so it cannot be invoked or have its rendered output asserted
 * from a plain JVM unit test (no Compose test rule / Robolectric is available in this
 * project, and the ticket explicitly rules out adding instrumented tests). The one part of
 * the Architect's public contract — `Grain(modifier: Modifier = Modifier, opacity: Float =
 * 0.08f)` — that is reachable from `src/test/` without rendering anything is the compiled
 * method's parameter list: today `Grain` only takes a `Modifier`, so this fails to find a
 * `float` parameter until the Dev adds `opacity`. It will keep passing afterwards regardless
 * of the exact default value or how the opacity is applied to the drawing, which is why the
 * mosaic-vs-circles and decode-once acceptance criteria are listed as gaps below, not asserted
 * here.
 */
class GrainTest {

    @Test
    fun `Grain exposes a float opacity parameter`() {
        val grainMethod = Class.forName("com.knowledge.sporedoc.ui.theme.effects.GrainKt")
            .declaredMethods
            .firstOrNull { it.name == "Grain" }

        requireNotNull(grainMethod) { "Expected a top-level Grain(...) function in GrainKt" }

        val hasOpacityParameter = grainMethod.parameterTypes.any { it == Float::class.javaPrimitiveType }
        assertTrue(
            "Expected Grain(...) to declare a float opacity parameter per the SD-9 contract",
            hasOpacityParameter
        )
    }
}
