package com.knowledge.sporedoc.ui.splash

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Timeline (ms), per SD-8 R5:
 *   0-400    atmosphere (background haze + vignette + grain) fades in
 *   150-650  laser beam grows from the mark's circle up to the top edge
 *   450-750  apex halo pulses once (1.0 -> 1.25 -> 1.0)
 *   500-900  wordmark fades in, rising
 *   650-900  subtitle follows, 150ms after the wordmark starts
 *   >= 900   calls [onFinished] once (no loading signal is read: R6)
 *
 * With the system's animator-duration scale at 0 (reduced motion), every value starts and
 * stays at its final state and [onFinished] is called immediately.
 *
 * Single responsibility: *when* things happen. [SplashContent] draws *what* a frame looks like.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val start = if (reduceMotion) 1f else 0f

    val atmosphere = remember { Animatable(start) }
    val beam = remember { Animatable(start) }
    var halo by remember { mutableFloatStateOf(0f) }
    val word = remember { Animatable(start) }
    val subtitle = remember { Animatable(start) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            onFinished()
            return@LaunchedEffect
        }
        launch { atmosphere.animateTo(1f, tween(400, easing = LinearEasing)) }
        launch { delay(150); beam.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch {
            delay(450)
            val halo1 = Animatable(0f)
            halo1.animateTo(1f, tween(150, easing = FastOutSlowInEasing)) { halo = value }
            halo1.animateTo(0f, tween(150, easing = FastOutSlowInEasing)) { halo = value }
        }
        launch { delay(500); word.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { delay(650); subtitle.animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }
        delay(900)
        onFinished()
    }

    SplashContent(
        atmosphereProgress = atmosphere.value,
        beamProgress = beam.value,
        haloProgress = halo,
        wordmarkProgress = word.value,
        subtitleProgress = subtitle.value,
        modifier = modifier,
    )
}
