package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * The footer (SD-20 R2-R9, reconnected by SD-21 R1-R8): a fixed, non-scrolling row of five
 * boxes, in order `crop, rotate, sign, delete, share`. `crop`, `rotate` and `sign` remain
 * inert with disabled semantics (R8); `delete` and `share` are wired to [onDeleteClicked] and
 * [onShareClicked] respectively, with button-role content descriptions (R2, R3, R6, R7).
 */
@Composable
fun ViewerFooter(
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SporeDocColors.bgEdge)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SporeDocStroke.hairlineDp)
                .background(SporeDocColors.goldDividerFooter)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = SporeDocSpacing.viewerFooterPaddingHorizontalDp,
                    end = SporeDocSpacing.viewerFooterPaddingHorizontalDp,
                    top = SporeDocSpacing.viewerFooterPaddingTopDp,
                    bottom = SporeDocSpacing.viewerFooterPaddingBottomDp
                ),
            horizontalArrangement = Arrangement.spacedBy(SporeDocSpacing.viewerFooterBoxGapDp)
        ) {
            IconOnlyBox(modifier = Modifier.weight(1f)) { CropIcon() }
            IconOnlyBox(modifier = Modifier.weight(1f)) { RotateIcon() }
            IconOnlyBox(modifier = Modifier.weight(1f)) { SignIcon() }
            EnabledIconOnlyBox(
                modifier = Modifier.weight(1f),
                onClick = onDeleteClicked,
                contentDescription = stringResource(R.string.delete_box_content_description)
            ) { DeleteIcon() }
            ShareBox(modifier = Modifier.weight(2f), onClick = onShareClicked)
        }
    }
}

/** Icon-only disabled box shared by `crop`, `rotate`, `sign` and `delete` (R3, R6, R7). */
@Composable
private fun IconOnlyBox(modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .height(SporeDocSpacing.viewerFooterBoxHeightDp)
            .border(SporeDocStroke.hairlineDp, SporeDocColors.viewerFooterBoxStroke)
            .background(SporeDocColors.viewerFooterBoxBackground)
            .semantics { disabled() },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/**
 * Enabled box shared by `delete` (SD-21 R2, R7): icon only, clickable, with a button-role
 * content description so a screen reader announces it as tappable.
 */
@Composable
private fun EnabledIconOnlyBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    contentDescription: String,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(SporeDocSpacing.viewerFooterBoxHeightDp)
            .border(SporeDocStroke.hairlineDp, SporeDocColors.viewerFooterBoxStroke)
            .background(SporeDocColors.viewerFooterBoxBackground)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/**
 * Box for `share` (SD-21 R1, R6): icon plus the `SHARE` label, heavier visual weight than the
 * other four boxes, pinned to the footer's trailing edge, clickable with a button-role
 * content description.
 */
@Composable
private fun ShareBox(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shareContentDescription = stringResource(R.string.viewer_footer_share_content_description)
    Box(
        modifier = modifier
            .height(SporeDocSpacing.viewerFooterBoxHeightDp)
            .border(SporeDocStroke.hairlineDp, SporeDocColors.viewerFooterShareStroke)
            .background(SporeDocColors.viewerFooterShareBackground)
            .padding(horizontal = SporeDocSpacing.viewerFooterShareHorizontalPaddingDp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = shareContentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShareIcon()
            Spacer(modifier = Modifier.width(SporeDocSpacing.viewerFooterShareIconLabelGapDp))
            Text(
                text = stringResource(R.string.viewer_footer_share_label),
                style = SporeDocTypography.viewerFooterShareLabel
            )
        }
    }
}

/** Decorative crop-corners glyph; the box's own semantics mark it as disabled. */
@Composable
private fun CropIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.viewerFooterIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.ivoryIcon
        val arm = size.width * 0.35f
        val cap = StrokeCap.Square
        drawLine(color, Offset(0f, arm), Offset(0f, 0f), strokeWidthPx, cap)
        drawLine(color, Offset(0f, 0f), Offset(arm, 0f), strokeWidthPx, cap)
        drawLine(color, Offset(size.width - arm, size.height), Offset(size.width, size.height), strokeWidthPx, cap)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - arm), strokeWidthPx, cap)
    }
}

/** Decorative rotate-arrow glyph; the box's own semantics mark it as disabled. */
@Composable
private fun RotateIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.viewerFooterIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.ivoryIcon
        drawArc(
            color = color,
            startAngle = -45f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidthPx)
        )
        drawLine(color, Offset(size.width, size.height * 0.5f), Offset(size.width * 0.7f, size.height * 0.2f), strokeWidthPx, StrokeCap.Square)
    }
}

/** Decorative signature-stroke glyph; the box's own semantics mark it as disabled. */
@Composable
private fun SignIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.viewerFooterIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.ivoryIcon
        drawLine(color, Offset(0f, size.height), Offset(size.width * 0.3f, 0f), strokeWidthPx, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.3f, 0f), Offset(size.width * 0.6f, size.height), strokeWidthPx, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.6f, size.height), Offset(size.width, 0f), strokeWidthPx, StrokeCap.Round)
    }
}

/** Decorative delete-cross glyph; the box's own semantics mark it as disabled. */
@Composable
private fun DeleteIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.viewerFooterIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.ivoryIcon
        drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), strokeWidthPx, StrokeCap.Square)
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), strokeWidthPx, StrokeCap.Square)
    }
}

/** Decorative share-arrow glyph; the box's own semantics mark it as disabled. */
@Composable
private fun ShareIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(SporeDocSpacing.viewerFooterIconSizeDp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.ivory
        val midX = size.width / 2f
        drawLine(color, Offset(midX, size.height), Offset(midX, 0f), strokeWidthPx, StrokeCap.Square)
        drawLine(color, Offset(midX, 0f), Offset(0f, size.height * 0.4f), strokeWidthPx, StrokeCap.Square)
        drawLine(color, Offset(midX, 0f), Offset(size.width, size.height * 0.4f), strokeWidthPx, StrokeCap.Square)
    }
}
