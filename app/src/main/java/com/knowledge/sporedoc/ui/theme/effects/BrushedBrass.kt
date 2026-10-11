package com.knowledge.sporedoc.ui.theme.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocStroke

/**
 * tokens.effects.brushedBrass: brassVeil base, horizontal goldBrushed@3.5% streaks every
 * 3dp, 0.5dp goldBrushed border.
 */
@Composable
fun BrushedBrassSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .background(SporeDocColors.brassVeil)
            .drawBehind { drawBrassStreaks() }
            .border(SporeDocStroke.hairlineDp, SporeDocColors.goldBrushed),
        content = content,
    )
}

private fun DrawScope.drawBrassStreaks() {
    val streakGap = 3.dp.toPx()
    val streakWidth = 1.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = SporeDocColors.goldBrushed.copy(alpha = 0.035f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = streakWidth,
        )
        y += streakGap
    }
}
