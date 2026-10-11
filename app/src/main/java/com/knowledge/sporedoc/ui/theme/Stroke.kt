package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Stroke tokens from docs/design/sporedoc-handover.json -> tokens.stroke. */
object SporeDocStroke {
    val hairlineDp: Dp = 0.5.dp
    val frameDp: Dp = 1.dp
    val iconStrokeDp: Dp = 1.dp
    val iconJoin: StrokeJoin = StrokeJoin.Miter
    val iconCap: StrokeCap = StrokeCap.Square
}
