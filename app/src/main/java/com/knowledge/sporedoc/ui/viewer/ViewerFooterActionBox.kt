package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * One labeled, clickable dedicated-footer box (SAVE/CANCEL/ROTATE LEFT, ...), at least
 * `tokens.spacing.minTouchTargetDp` tall, with its own button-role content description.
 * Shared by [ViewerRotateFooter] and [ViewerCropFooter], the viewer's two dedicated footers.
 */
@Composable
fun ViewerFooterActionBox(
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
