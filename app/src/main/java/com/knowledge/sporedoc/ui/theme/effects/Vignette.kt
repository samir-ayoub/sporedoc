package com.knowledge.sporedoc.ui.theme.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * tokens.effects.vignette: ellipse 130% x 90% at 50% 45%, transparent -> black@50%.
 * Drawn above content, below grain (R7) by composition order in callers.
 */
@Composable
fun Vignette(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width * 1.30f
        val height = size.height * 0.90f
        val center = Offset(size.width * 0.50f, size.height * 0.45f)
        val radius = (maxOf(width, height) / 2f).coerceAtLeast(1f)
        val brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.55f to Color.Transparent,
                1.0f to Color.Black.copy(alpha = 0.5f),
            ),
            center = center,
            radius = radius,
        )
        drawRect(brush = brush)
    }
}
