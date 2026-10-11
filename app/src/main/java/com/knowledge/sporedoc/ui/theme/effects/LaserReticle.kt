package com.knowledge.sporedoc.ui.theme.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.ui.theme.SporeDocColors

/**
 * tokens.effects.laserReticle: 8 lines total, one horizontal and one vertical arm per
 * corner, laserGreen, offset 9dp from the page, arm length 13dp.
 */
@Composable
fun LaserReticle(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val armLength = 13.dp.toPx()
        val offsetFromPage = 9.dp.toPx()
        val thickness = 0.75.dp.toPx()

        val corners = listOf(
            Offset(offsetFromPage, offsetFromPage),
            Offset(size.width - offsetFromPage, offsetFromPage),
            Offset(offsetFromPage, size.height - offsetFromPage),
            Offset(size.width - offsetFromPage, size.height - offsetFromPage),
        )

        corners.forEach { corner ->
            drawLine(
                color = SporeDocColors.laserGreen,
                start = Offset(corner.x - armLength / 2f, corner.y),
                end = Offset(corner.x + armLength / 2f, corner.y),
                strokeWidth = thickness,
            )
            drawLine(
                color = SporeDocColors.laserGreen,
                start = Offset(corner.x, corner.y - armLength / 2f),
                end = Offset(corner.x, corner.y + armLength / 2f),
                strokeWidth = thickness,
            )
        }
    }
}
