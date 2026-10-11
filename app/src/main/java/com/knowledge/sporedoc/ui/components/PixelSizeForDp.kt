package com.knowledge.sporedoc.ui.components

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.knowledge.sporedoc.domain.model.PixelSize
import kotlin.math.roundToInt

/**
 * Converts a dp-sized display box into a [PixelSize] using the current [Density], so the
 * dp -> px decision for a render target lives where density is available (ui/) instead of
 * being duplicated between the list's thumbnail and the viewer's page stage.
 */
fun Density.pixelSizeForDp(widthDp: Dp, heightDp: Dp): PixelSize =
    PixelSize(
        widthPx = (widthDp.value * density).roundToInt(),
        heightPx = (heightDp.value * density).roundToInt()
    )
