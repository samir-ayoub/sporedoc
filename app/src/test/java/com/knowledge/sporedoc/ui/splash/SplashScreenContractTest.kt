package com.knowledge.sporedoc.ui.splash

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-8, Architect's public contract:
 * "`@Composable fun SplashScreen(onFinished: () -> Unit, modifier: Modifier = Modifier)`".
 *
 * `SplashScreen` is a `@Composable`, so — per this project's established pattern (see
 * GrainTest) — it cannot be invoked or have its rendered output asserted from a JVM unit test.
 * The one part of the contract reachable from `src/test/` is that the function exists at all,
 * with a no-argument callback parameter for `onFinished`, and takes no loading-signal argument
 * (R6: "o ecrã recebe apenas o que precisa para desenhar e para avisar que acabou").
 */
class SplashScreenContractTest {

    @Test
    fun `SplashScreen composable exists with an onFinished callback parameter`() {
        val clazz = Class.forName("com.knowledge.sporedoc.ui.splash.SplashScreenKt")
        val method = clazz.declaredMethods.firstOrNull { it.name == "SplashScreen" }

        requireNotNull(method) {
            "Expected a top-level SplashScreen(...) function in ui.splash.SplashScreenKt"
        }

        val hasOnFinishedCallback = method.parameterTypes.any { it == kotlin.jvm.functions.Function0::class.java }
        assertTrue(
            "Expected SplashScreen(...) to declare an onFinished: () -> Unit parameter",
            hasOnFinishedCallback
        )
    }
}
