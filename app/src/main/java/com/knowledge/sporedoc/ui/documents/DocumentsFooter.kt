package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * Renders the tripartite bottom dock: IMPORT, CAPTURE, QR CODE, three equal-width boxes
 * side by side (SD-15 R2/R3).
 */
@Composable
fun DocumentsFooter(
    onImportRequested: () -> Unit,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SporeDocSpacing.footerBoxGapDp)
    ) {
        ImportBox(onClick = onImportRequested, modifier = Modifier.weight(1f))
        CaptureBox(onClick = onCapture, modifier = Modifier.weight(2f))
        QrCodeBox(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ImportBox(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.import_content_description)
    Box(
        modifier = modifier
            .height(SporeDocSpacing.footerBoxHeightDp)
            .border(SporeDocStroke.footerBoxBorderDp, SporeDocColors.goldBrushed)
            .background(SporeDocColors.footerBoxBackground)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ImportGlyph()
            Spacer(modifier = Modifier.height(SporeDocSpacing.footerIconLabelGapDp))
            Text(
                text = stringResource(
                R.string.import_label),
                style = SporeDocTypography.primaryButton,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun CaptureBox(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.capture_content_description)
    Box(
        modifier = modifier
            .height(SporeDocSpacing.footerBoxHeightDp)
            .border(SporeDocStroke.footerBoxBorderDp, SporeDocColors.goldBrushed)
            .background(SporeDocColors.footerBoxBackground)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CaptureIcon()
            Spacer(modifier = Modifier.height(SporeDocSpacing.footerIconLabelGapDp))
            Text(text = stringResource(R.string.capture_label), style = SporeDocTypography.primaryButton)
        }
    }
}

/** Visually faded reserve box; not clickable and carries disabled semantics (SD-15 R8-R10). */
@Composable
private fun QrCodeBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(SporeDocSpacing.footerBoxHeightDp)
            .border(SporeDocStroke.footerBoxBorderDp, SporeDocColors.qrCodeMutedStroke)
            .background(SporeDocColors.footerBoxBackgroundDisabled)
            .semantics {
                disabled()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            QrCodeGlyph()
            Spacer(modifier = Modifier.height(SporeDocSpacing.footerIconLabelGapDp))
            Text(
                text = stringResource(R.string.qr_code_label),
                style = SporeDocTypography.primaryButton,
                color = SporeDocColors.qrCodeMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/** Decorative viewfinder-corners icon; the box's own semantics carry the content description. */
@Composable
private fun CaptureIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.footerIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val w = size.width
        val h = size.height
        val arm = w * 0.3f
        val color = SporeDocColors.goldBrushed
        val cap = StrokeCap.Square

        drawLine(color, Offset(0f, arm), Offset(0f, 0f), strokeWidthPx, cap)
        drawLine(color, Offset(0f, 0f), Offset(arm, 0f), strokeWidthPx, cap)

        drawLine(color, Offset(w - arm, 0f), Offset(w, 0f), strokeWidthPx, cap)
        drawLine(color, Offset(w, 0f), Offset(w, arm), strokeWidthPx, cap)

        drawLine(color, Offset(w, h - arm), Offset(w, h), strokeWidthPx, cap)
        drawLine(color, Offset(w, h), Offset(w - arm, h), strokeWidthPx, cap)

        drawLine(color, Offset(arm, h), Offset(0f, h), strokeWidthPx, cap)
        drawLine(color, Offset(0f, h), Offset(0f, h - arm), strokeWidthPx, cap)
    }
}

/** Decorative import-arrow icon; the box's own semantics carry the content description. */
@Composable
private fun ImportGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.footerIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.goldMatte
        val midX = size.width / 2f
        drawLine(color, Offset(midX, size.height), Offset(midX, 0f), strokeWidthPx, StrokeCap.Square)
        drawLine(color, Offset(midX, 0f), Offset(0f, size.height * 0.4f), strokeWidthPx, StrokeCap.Square)
        drawLine(color, Offset(midX, 0f), Offset(size.width, size.height * 0.4f), strokeWidthPx, StrokeCap.Square)
    }
}

/** Purely decorative placeholder glyph for the disabled QR CODE box; no semantics of its own. */
@Composable
private fun QrCodeGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.footerIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.qrCodeMuted
        drawRect(color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx))
    }
}
