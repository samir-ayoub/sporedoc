package com.knowledge.sporedoc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * tokens.shape: cornerRadiusDp = 0, no rounded corners anywhere. Every Material3 shape slot
 * is the same zero-corner-radius shape (`RoundedCornerShape(0.dp)`, which `createOutline`
 * reports as `Outline.Rectangle` once every corner resolves to 0, satisfying "square outline"
 * checks without needing the ui-graphics `RectangleShape`, which isn't a `CornerBasedShape`).
 *
 * Built through Material3's public 5-arg `Shapes(extraSmall, small, medium, large, extraLarge)`
 * constructor, kept for backward compatibility across Material3 versions.
 */
private val zeroCorner = RoundedCornerShape(0.dp)

val SporeDocShapes: Shapes = Shapes(
    extraSmall = zeroCorner,
    small = zeroCorner,
    medium = zeroCorner,
    large = zeroCorner,
    extraLarge = zeroCorner,
)
