package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color tokens from docs/design/sporedoc-handover.json -> tokens.color.
 * Names and values (including alpha) mirror the JSON exactly.
 */
object SporeDocColors {
    val bgCenter = Color(0xFF12091F)
    val bgMid = Color(0xFF0A0612)
    val bgEdge = Color(0xFF030305)
    val nebulaHaze = Color(0xFF541E70)

    val ivory = Color(0xFFFDFBF7)
    val ivoryMeta = Color(0x6BFDFBF7)
    val ivoryIcon = Color(0x73FDFBF7)
    val ivoryMuted = Color(0x80FDFBF7)

    val goldBrushed = Color(0xFFD4AF37)
    val goldMatte = Color(0xFFB89047)
    val goldSection = Color(0xFF9C8152)
    val goldDividerSoft = Color(0x59B89047)
    val goldDividerFooter = Color(0x73B89047)

    val brassVeil = Color(0x12D4AF37)

    val magma = Color(0xFFE65C00)
    val laserGreen = Color(0xFF39FF14)

    val paper = Color(0xFFE4DDCD)
    val paperThumb = Color(0xFFCFC8B8)
    val ink = Color(0xFF17131C)
    val inkThumb = Color(0xFF1A1520)
    val inkRule = Color(0xFF7F786C)

    val orbCore = Color(0xFF1E1033)
    val orbMid = Color(0xFF0C0716)
    val orbEdge = Color(0xFF050309)

    // SD-8: splash-only tokens from the brand kit.
    val abyss = Color(0xFF050508)
    val laserHot = Color(0xFFE9FFE2)

    // SD-15: footer tripartite dock, QR CODE disabled-box tokens (no handover-JSON entry).
    val qrCodeMuted = Color(0x40FDFBF7)
    val qrCodeMutedStroke = Color(0x2EB89047)

    // SD-15: footer box backgrounds, pre-composed with their alpha (bgEdge at 55%/30%).
    val footerBoxBackground = Color(0x8C030305)
    val footerBoxBackgroundDisabled = Color(0x4D030305)
}
