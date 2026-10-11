package com.knowledge.sporedoc.ui.theme.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.ui.theme.SporeDocColors

/**
 * tokens.effects.eclipseOrb: Documents only, 360dp, top-right offset +150dp/-170dp,
 * radial gradient orbCore -> orbMid -> orbEdge. Drawn behind the header by callers.
 */
@Composable
fun EclipseOrb(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(360.dp)
            .offset(x = 150.dp, y = (-170).dp),
    ) {
        drawOrb()
    }
}

private fun DrawScope.drawOrb() {
    val center = Offset(size.width * 0.34f, size.height * 0.72f)
    val radius = maxOf(size.width, size.height)
    val brush = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to SporeDocColors.orbCore,
            0.48f to SporeDocColors.orbMid,
            0.70f to SporeDocColors.orbEdge,
        ),
        center = center,
        radius = radius.coerceAtLeast(1f),
    )
    drawCircle(brush = brush, radius = radius, center = center)
}
