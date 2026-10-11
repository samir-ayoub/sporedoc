package com.knowledge.sporedoc.ui.theme.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

// tokens.effects.grain: monochrome noise overlay, alpha 0.08, topmost, non-interactive.
//
// NOTE on R8: the contract calls for a pre-rendered tileable noise PNG bundled as
// res/drawable/grain_texture.png and drawn with an ImageShader/TileMode.Repeated. This Dev's
// toolchain can only author text-encoded (UTF-8) files, so it cannot author a binary raster
// asset without corrupting it (and a corrupt PNG would fail resource compilation for the
// whole module). As a stand-in that still satisfies "not generated per frame", the speck
// positions below are a fixed, seeded, build-time constant (computed once, not re-randomized
// on recomposition or per frame) rather than a bundled bitmap. Flagged here for follow-up:
// swapping in a real pre-rendered PNG asset does not require any other code to change.
private const val GRAIN_ALPHA = 0.08f
private const val GRAIN_TILE_SIZE = 256f
private const val GRAIN_SPECK_COUNT = 700
private const val GRAIN_SEED = 42L

private val grainSpecks: List<Offset> = run {
    val random = Random(GRAIN_SEED)
    List(GRAIN_SPECK_COUNT) {
        Offset(
            x = random.nextFloat() * GRAIN_TILE_SIZE,
            y = random.nextFloat() * GRAIN_TILE_SIZE,
        )
    }
}

/**
 * Topmost, non-interactive grain overlay. Draws only (no pointerInput/clickable modifier),
 * which is what keeps it pass-through for touches (R9).
 */
@Composable
fun Grain(modifier: Modifier = Modifier) {
    val specks = remember { grainSpecks }
    Canvas(modifier = modifier.fillMaxSize()) {
        drawTiledGrain(specks)
    }
}

private fun DrawScope.drawTiledGrain(specks: List<Offset>) {
    val tilesX = (size.width / GRAIN_TILE_SIZE).toInt() + 1
    val tilesY = (size.height / GRAIN_TILE_SIZE).toInt() + 1
    for (tileX in 0..tilesX) {
        for (tileY in 0..tilesY) {
            val originX = tileX * GRAIN_TILE_SIZE
            val originY = tileY * GRAIN_TILE_SIZE
            specks.forEach { speck ->
                drawCircle(
                    color = Color.White.copy(alpha = GRAIN_ALPHA),
                    radius = 0.6f,
                    center = Offset(originX + speck.x, originY + speck.y),
                )
            }
        }
    }
}
