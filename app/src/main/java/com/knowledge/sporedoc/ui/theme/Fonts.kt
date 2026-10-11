package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.knowledge.sporedoc.R

/**
 * Font families from docs/design/sporedoc-handover.json -> tokens.typography.
 * Responsibility: load/expose the two bundled font families. Named styles live in Typography.kt.
 */
object SporeDocFonts {
    val display: FontFamily = FontFamily(
        Font(resId = R.font.anton, weight = FontWeight.Normal),
    )

    val mono: FontFamily = FontFamily(
        Font(resId = R.font.jetbrains_mono_light, weight = FontWeight.W300),
        Font(resId = R.font.jetbrains_mono_regular, weight = FontWeight.W400),
        Font(resId = R.font.jetbrains_mono_medium, weight = FontWeight.W500),
    )
}
