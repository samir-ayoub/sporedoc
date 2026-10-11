package com.elsewhere.sporedoc.ui.splash

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsewhere.sporedoc.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SporeDoc brand splash. Shown right after the system splash (Theme.SporeDoc.Starting).
 *
 * Frame 0 is identical to the system splash: abyss #050508 + the 288dp mark centred.
 * Timeline (ms):
 *   0-400    nebula gradient, vignette and 8% grain fade in
 *   150-650  laser beam grows from the icon circle (±96dp) up to the top edge of the screen
 *   450-750  apex halo pulses once (1.0 -> 1.25 -> 1.0)
 *   500-900  SPOREDOC wordmark fades in, rising 8dp; subtitle follows 150ms later
 *   >= 900   hold until [isReady] is true, then call [onFinished] (navigate with a 250ms crossfade)
 * With animations disabled (developer setting / accessibility) everything is shown at its final state.
 *
 * Fonts: [display] = Anton, [mono] = JetBrains Mono (same families as the app theme).
 */
@Composable
fun BrandSplash(
    isReady: () -> Boolean,
    onFinished: () -> Unit,
    display: FontFamily,
    mono: FontFamily,
) {
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val start = if (reduceMotion) 1f else 0f
    val atmosphere = remember { Animatable(start) }
    val beam = remember { Animatable(start) }
    val halo = remember { Animatable(0f) }
    val word = remember { Animatable(start) }
    val sub = remember { Animatable(start) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            launch { atmosphere.animateTo(1f, tween(400, easing = LinearEasing)) }
            launch { delay(150); beam.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
            launch {
                delay(450)
                halo.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
                halo.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
            }
            launch { delay(500); word.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
            launch { delay(650); sub.animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }
        }
        delay(900)
        while (!isReady()) delay(50)
        onFinished()
    }

    val grain: ImageBitmap = ImageBitmap.imageResource(R.drawable.grain_tile)
    val grainBrush = remember(grain) { ShaderBrush(ImageShader(grain, TileMode.Repeated, TileMode.Repeated)) }

    Box(
        Modifier
            .fillMaxSize()
            .semantics { contentDescription = "SporeDoc" }
    ) {
        // 1. Abyss + nebula + vignette + beam (one Canvas, drawn back to front)
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            drawRect(Abyss)
            val a = atmosphere.value
            drawRect(
                Brush.radialGradient(
                    0f to NebulaCenter.copy(alpha = a),
                    0.42f to NebulaMid.copy(alpha = a),
                    0.80f to AbyssEdge.copy(alpha = a),
                    center = Offset(c.x, size.height * 0.48f),
                    radius = size.maxDimension * 0.62f,
                )
            )
            drawRect(
                Brush.radialGradient(
                    0f to Haze.copy(alpha = 0.30f * a),
                    0.7f to Color.Transparent,
                    center = c,
                    radius = size.width * 0.75f,
                )
            )

            // Laser beam: 2dp core + 0.8dp hot centre + soft glow; fades out over 70dp below the pyramid
            val px = 1.dp.toPx()
            val topStart = c.y - 96 * px                // edge of the system-splash icon circle
            val top = topStart * (1f - beam.value)      // grows to y = 0
            val bottom = c.y + 70 * px
            val fade = Brush.verticalGradient(
                0f to Laser, ((bottom - 70 * px - top) / (bottom - top)).coerceIn(0f, 1f) to Laser, 1f to Color.Transparent,
                startY = top, endY = bottom,
            )
            drawRect(
                Brush.horizontalGradient(
                    0f to Color.Transparent, 0.5f to Laser.copy(alpha = 0.22f), 1f to Color.Transparent,
                    startX = c.x - 6 * px, endX = c.x + 6 * px,
                ),
                topLeft = Offset(c.x - 6 * px, top), size = Size(12 * px, bottom - top - 70 * px), alpha = 0.9f,
            )
            drawRect(fade, topLeft = Offset(c.x - 1.07f * px, top), size = Size(2.13f * px, bottom - top))
            drawRect(LaserHot, topLeft = Offset(c.x - 0.4f * px, top), size = Size(0.8f * px, bottom - top - 70 * px), alpha = 0.85f)

            // Apex halo pulse (apex sits 56dp above centre in the 288dp mark)
            if (halo.value > 0f) {
                val apex = Offset(c.x, c.y - 56 * px)
                drawCircle(
                    Brush.radialGradient(
                        0f to LaserHot.copy(alpha = 0.6f * halo.value), 1f to Color.Transparent,
                        center = apex, radius = 28 * px,
                    ),
                    radius = 28 * px * (1f + 0.25f * halo.value), center = apex,
                )
            }
        }

        // 2. The mark: same 288dp box and position as the system splash icon -> seamless hand-off
        Image(
            painter = painterResource(R.drawable.splash_mark),
            contentDescription = null,
            modifier = Modifier.size(288.dp).align(Alignment.Center),
        )

        // 3. Wordmark, 118dp below centre
        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y = 151.dp) // column is ~66dp tall and centred -> its top lands 118dp below centre
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            androidx.compose.material3.Text(
                "SPOREDOC",
                modifier = Modifier.graphicsLayer { alpha = word.value; translationY = (1f - word.value) * 8.dp.toPx() },
                style = TextStyle(fontFamily = display, fontSize = 40.sp, letterSpacing = 13.sp, color = Ivory, textAlign = TextAlign.Center),
            )
            androidx.compose.material3.Text(
                "DOCUMENT SCANNER",
                modifier = Modifier.padding(top = 14.dp).graphicsLayer { alpha = sub.value },
                style = TextStyle(fontFamily = mono, fontSize = 8.sp, letterSpacing = 4.6.sp, color = GoldMatte),
            )
        }

        androidx.compose.material3.Text(
            "ELSEWHERE APPLICATIONS",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp).graphicsLayer { alpha = sub.value },
            style = TextStyle(fontFamily = mono, fontSize = 8.sp, letterSpacing = 3.4.sp, color = Ivory.copy(alpha = 0.42f)),
        )

        // 4. Vignette + 8% analog grain over everything
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.5f * atmosphere.value),
                    center = Offset(size.width / 2f, size.height * 0.48f), radius = size.maxDimension * 0.62f,
                )
            )
            drawRect(grainBrush, alpha = 0.08f * atmosphere.value)
        }
    }
}

private val Abyss = Color(0xFF050508)
private val AbyssEdge = Color(0xFF030305)
private val NebulaCenter = Color(0xFF12091F)
private val NebulaMid = Color(0xFF0A0612)
private val Haze = Color(0xFF5A2483)
private val Laser = Color(0xFF39FF14)
private val LaserHot = Color(0xFFE9FFE2)
private val Ivory = Color(0xFFFDFBF7)
private val GoldMatte = Color(0xFFB89047)
