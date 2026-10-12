package com.knowledge.sporedoc.ui.theme.effects

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-8 R9 / acceptance criterion:
 * "Dado o componente de fundo e o de vinheta, quando são chamados sem opacidade, então
 * desenham exatamente o que desenhavam antes desta entrega."
 *
 * `Vignette` is a `@Composable`, so — per this project's established pattern (see GrainTest) —
 * it cannot be invoked or have its rendered output asserted from a JVM unit test. The one part
 * of the Architect's public contract reachable from `src/test/` is the compiled parameter list:
 * today `Vignette` only takes a `Modifier`, so this fails to find a `float` parameter until the
 * Dev adds `opacity: Float = 1f`.
 */
class VignetteOpacityTest {

    @Test
    fun `Vignette exposes a float opacity parameter`() {
        val method = Class.forName("com.knowledge.sporedoc.ui.theme.effects.VignetteKt")
            .declaredMethods
            .firstOrNull { it.name == "Vignette" }

        requireNotNull(method) { "Expected a top-level Vignette(...) function in VignetteKt" }

        val hasOpacityParameter = method.parameterTypes.any { it == Float::class.javaPrimitiveType }
        assertTrue(
            "Expected Vignette(...) to declare a float opacity parameter per the SD-8 contract",
            hasOpacityParameter
        )
    }
}
