package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * SD-22 R1/R2/R10: the dedicated rotate-mode footer that replaces the normal five-box footer
 * while `ViewerUiState.isRotateModeActive` is true - three equally weighted boxes, each at
 * least `tokens.spacing.minTouchTargetDp`, each with its own button-role content description.
 * No business logic lives here: taps are only reported outward.
 */
@Composable
fun ViewerRotateFooter(
    onRotateLeftClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onCancelClicked: () -> Unit,
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
            RotateFooterBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.rotate_left_label),
                contentDescription = stringResource(R.string.rotate_left_content_description),
                onClick = onRotateLeftClicked
            )
            RotateFooterBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.rotate_save_label),
                contentDescription = stringResource(R.string.rotate_save_content_description),
                onClick = onSaveClicked
            )
            RotateFooterBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.delete_dialog_cancel),
                contentDescription = stringResource(R.string.rotate_cancel_content_description),
                onClick = onCancelClicked
            )
        }
    }
}

/** One labeled, clickable rotate-mode footer box, at least `minTouchTargetDp` tall. */
@Composable
private fun RotateFooterBox(
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        Text(text = label, style = SporeDocTypography.viewerFooterShareLabel)
    }
}
