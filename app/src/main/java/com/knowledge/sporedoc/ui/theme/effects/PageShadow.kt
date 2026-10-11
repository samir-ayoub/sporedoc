package com.knowledge.sporedoc.ui.theme.effects

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

/**
 * tokens.effects.pageShadow: outer blur below the page so it rises out of the dark
 * background rather than sitting flat on top of it.
 */
fun Modifier.pageShadow(): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = android.graphics.Color.argb(235, 10, 4, 20)
            maskFilter = BlurMaskFilter(80.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRect(
            0f,
            34.dp.toPx(),
            size.width,
            size.height + 34.dp.toPx(),
            paint,
        )
    }
}
