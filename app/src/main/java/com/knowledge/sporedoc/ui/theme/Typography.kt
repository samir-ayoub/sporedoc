package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Named text styles from docs/design/sporedoc-handover.json -> tokens.typography.styles.
 *
 * `fileName` and `appBarTitle` are limited to one line with ellipsis overflow per the JSON;
 * since TextStyle has no maxLines/overflow fields, those two values are exposed as paired
 * constants right next to the style they belong to.
 */
object SporeDocTypography {
    val wordmark = TextStyle(
        fontFamily = SporeDocFonts.display,
        fontSize = 60.sp,
        lineHeight = 54.sp, // 0.9 * 60sp
        letterSpacing = 15.sp,
        color = SporeDocColors.ivory,
    )

    val wordmarkCompact = TextStyle(
        fontFamily = SporeDocFonts.display,
        fontSize = 24.sp,
        lineHeight = 24.sp,
        letterSpacing = 6.sp,
        color = SporeDocColors.ivory,
    )

    val subtitle = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(400),
        fontSize = 8.sp,
        letterSpacing = 4.6.sp,
        color = SporeDocColors.goldMatte,
    )

    val sectionLabel = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(500),
        fontSize = 9.5.sp,
        letterSpacing = 3.4.sp,
        color = SporeDocColors.goldSection,
    )

    val fileName = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(400),
        fontSize = 13.sp,
        color = SporeDocColors.ivory,
    )
    const val fileNameMaxLines: Int = 1
    val fileNameOverflow: TextOverflow = TextOverflow.Ellipsis

    val fileMeta = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(300),
        fontSize = 10.sp,
        letterSpacing = 0.6.sp,
        color = SporeDocColors.ivoryMeta,
    )

    val textAction = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(400),
        fontSize = 10.5.sp,
        letterSpacing = 2.4.sp,
        color = SporeDocColors.magma,
    )

    val primaryButton = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        letterSpacing = 7.sp,
        color = SporeDocColors.ivory,
    )

    val exportButton = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(400),
        fontSize = 11.5.sp,
        letterSpacing = 4.5.sp,
        color = SporeDocColors.ivory,
    )

    val appBarTitle = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        letterSpacing = 0.6.sp,
        color = SporeDocColors.ivory,
    )
    const val appBarTitleMaxLines: Int = 1
    val appBarTitleOverflow: TextOverflow = TextOverflow.Ellipsis

    val pageIndicator = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontWeight = FontWeight(300),
        fontSize = 10.sp,
        letterSpacing = 4.sp,
        color = SporeDocColors.ivoryMeta,
    )

    // SD-8: splash baseline line ("KNOWLEDGE APPLICATIONS").
    val splashBaseline = TextStyle(
        fontFamily = SporeDocFonts.mono,
        fontSize = 8.sp,
        letterSpacing = 3.4.sp,
        color = SporeDocColors.ivoryMeta,
    )
}
