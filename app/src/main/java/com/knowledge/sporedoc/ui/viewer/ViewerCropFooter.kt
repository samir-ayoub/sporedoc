package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke

/**
 * SD-23 R1/R2/R10: the dedicated crop-mode footer that replaces the normal five-box footer
 * while `ViewerUiState.isCropModeActive` is true - two equally weighted boxes (SAVE/CANCEL),
 * each at least `tokens.spacing.minTouchTargetDp`, each with its own button-role content
 * description. No business logic lives here: taps are only reported outward.
 */
@Composable
fun ViewerCropFooter(
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
            ViewerFooterActionBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.crop_save_label),
                contentDescription = stringResource(R.string.crop_save_content_description),
                onClick = onSaveClicked
            )
            ViewerFooterActionBox(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.delete_dialog_cancel),
                contentDescription = stringResource(R.string.crop_cancel_content_description),
                onClick = onCancelClicked
            )
        }
    }
}
