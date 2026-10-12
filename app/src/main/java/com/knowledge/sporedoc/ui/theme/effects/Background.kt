package com.knowledge.sporedoc.ui.theme.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.knowledge.sporedoc.ui.theme.SporeDocColors

/** Which screen is asking for its background stack. */
enum class BackgroundScreen { Documents, Viewer, Splash }

data class GradientStop(val color: Color, val offset: Float)

data class GradientLayer(
    val widthFraction: Float,
    val heightFraction: Float,
    val centerXFraction: Float,
    val centerYFraction: Float,
    val stops: List<GradientStop>,
)

// tokens.effects.background.layers[0]: shared by both screens.
private val baseLayer = GradientLayer(
    widthFraction = 1.20f,
    heightFraction = 0.85f,
    centerXFraction = 0.50f,
    centerYFraction = 0.45f,
    stops = listOf(
        GradientStop(SporeDocColors.bgCenter, 0f),
        GradientStop(SporeDocColors.bgMid, 0.45f),
        GradientStop(SporeDocColors.bgEdge, 0.80f),
    ),
)

// tokens.effects.background.layers[1]: Documents only.
private val nebulaHazeLayer = GradientLayer(
    widthFraction = 0.60f,
    heightFraction = 0.38f,
    centerXFraction = 0.18f,
    centerYFraction = 0.88f,
    stops = listOf(
        GradientStop(SporeDocColors.nebulaHaze.copy(alpha = 0.26f), 0f),
        GradientStop(Color.Transparent, 0.70f),
    ),
)

// tokens.effects.background.layers[2]: Viewer only (halo behind the page).
private val haloLayer = GradientLayer(
    widthFraction = 0.48f,
    heightFraction = 0.32f,
    centerXFraction = 0.50f,
    centerYFraction = 0.46f,
    stops = listOf(
        GradientStop(Color(0xFF4E1C6C).copy(alpha = 0.42f), 0f),
        GradientStop(Color.Transparent, 0.75f),
    ),
)

// tokens.effects.background.layers[3]: Viewer only.
private val magmaLayer = GradientLayer(
    widthFraction = 0.30f,
    heightFraction = 0.18f,
    centerXFraction = 0.78f,
    centerYFraction = 0.22f,
    stops = listOf(
        GradientStop(SporeDocColors.magma.copy(alpha = 0.07f), 0f),
        GradientStop(Color.Transparent, 0.70f),
    ),
)

// SD-8 R9: Splash only. Centered nebula haze, radius ~75% of width, ~30% alpha.
private val splashHazeLayer = GradientLayer(
    widthFraction = 1.50f,
    heightFraction = 1.50f,
    centerXFraction = 0.50f,
    centerYFraction = 0.50f,
    stops = listOf(
        GradientStop(SporeDocColors.nebulaHaze.copy(alpha = 0.30f), 0f),
        GradientStop(Color.Transparent, 0.70f),
    ),
)

/**
 * Pure seam (R6): which gradient layers belong to which screen.
 * Documents = [base, nebulaHaze]. Viewer = [base, halo, magma]. Splash = [base, haze].
 */
fun backgroundLayers(screen: BackgroundScreen): List<GradientLayer> = when (screen) {
    BackgroundScreen.Documents -> listOf(baseLayer, nebulaHazeLayer)
    BackgroundScreen.Viewer -> listOf(baseLayer, haloLayer, magmaLayer)
    BackgroundScreen.Splash -> listOf(baseLayer, splashHazeLayer)
}

@Composable
fun ScreenBackground(screen: BackgroundScreen, modifier: Modifier = Modifier, opacity: Float = 1f) {
    val layers = backgroundLayers(screen)
    Canvas(modifier = modifier.fillMaxSize()) {
        layers.forEach { layer -> drawGradientLayer(layer, opacity) }
    }
}

private fun DrawScope.drawGradientLayer(layer: GradientLayer, opacity: Float = 1f) {
    val width = size.width * layer.widthFraction
    val height = size.height * layer.heightFraction
    val center = Offset(size.width * layer.centerXFraction, size.height * layer.centerYFraction)
    val radius = (maxOf(width, height) / 2f).coerceAtLeast(1f)
    val brush = Brush.radialGradient(
        colorStops = layer.stops.map { it.offset to it.color }.toTypedArray(),
        center = center,
        radius = radius,
    )
    drawRect(brush = brush, alpha = opacity)
}
