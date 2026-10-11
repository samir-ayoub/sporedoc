package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * Top bar (R3/R4/R18/R19): 3-column grid (back icon, centered title, delete icon), bottom
 * rule, transparent background, both icons at least [SporeDocSpacing.minTouchTargetDp].
 */
@Composable
fun ViewerAppBar(
    fileName: String,
    onBack: () -> Unit,
    onDeleteRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SporeDocSpacing.viewerAppBarHeightDp)
                .padding(horizontal = SporeDocSpacing.viewerAppBarHorizontalPaddingDp),
            horizontalArrangement = Arrangement.spacedBy(SporeDocSpacing.viewerAppBarColumnGapDp),
            verticalAlignment = Alignment.Bottom
        ) {
            IconColumn(
                contentDescription = stringResource(R.string.viewer_back_content_description),
                onClick = onBack
            ) { BackIcon() }

            Text(
                text = fileName,
                style = SporeDocTypography.appBarTitle,
                maxLines = SporeDocTypography.appBarTitleMaxLines,
                overflow = SporeDocTypography.appBarTitleOverflow,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = SporeDocSpacing.viewerAppBarVerticalPaddingDp)
            )

            IconColumn(
                contentDescription = stringResource(R.string.viewer_delete_content_description),
                onClick = onDeleteRequested
            ) { DeleteIcon() }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SporeDocStroke.hairlineDp)
                .background(SporeDocColors.goldMatte)
        )
    }
}

@Composable
private fun IconColumn(
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = SporeDocSpacing.viewerAppBarIconColumnWidthDp, height = SporeDocSpacing.minTouchTargetDp)
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun BackIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.ivoryIcon
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height / 2f), strokeWidthPx, StrokeCap.Square)
        drawLine(color, Offset(0f, size.height / 2f), Offset(size.width, size.height), strokeWidthPx, StrokeCap.Square)
    }
}

@Composable
private fun DeleteIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val color = SporeDocColors.ivoryIcon
        drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), strokeWidthPx, StrokeCap.Square)
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), strokeWidthPx, StrokeCap.Square)
    }
}

