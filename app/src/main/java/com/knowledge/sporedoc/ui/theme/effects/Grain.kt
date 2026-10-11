package com.knowledge.sporedoc.ui.theme.effects

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.knowledge.sporedoc.R

private const val GRAIN_OPACITY = 0.08f

/**
 * Topmost, non-interactive grain overlay: draws the pre-rendered [R.drawable.grain_tile]
 * texture repeated in mosaic across both axes.
 *
 * Draws only (no pointerInput/clickable modifier), which is what keeps it pass-through for
 * touches (R9). The bitmap is decoded once (via [BitmapFactory]/[remember]) and reused across
 * recompositions, never re-decoded per frame.
 */
@Composable
fun Grain(modifier: Modifier = Modifier, opacity: Float = GRAIN_OPACITY) {
    val context = LocalContext.current
    val brush = remember {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.grain_tile)
            .asImageBitmap()
        ShaderBrush(
            ImageShader(
                image = bitmap,
                tileModeX = TileMode.Repeated,
                tileModeY = TileMode.Repeated
            )
        )
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(brush = brush, alpha = opacity)
    }
}
