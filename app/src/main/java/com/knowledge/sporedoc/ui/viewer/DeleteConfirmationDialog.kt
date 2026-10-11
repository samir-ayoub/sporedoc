package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocShapes
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * The delete confirmation dialog (R12/R13/R15): zero-corner shape, gold hairline border,
 * mono typography, matching the rest of the app's tokens. Lives here until SD-5 promotes
 * it to `ui/components`.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SporeDocShapes.extraSmall,
        containerColor = SporeDocColors.bgEdge,
        modifier = Modifier
            .border(SporeDocStroke.hairlineDp, SporeDocColors.goldBrushed)
            .background(SporeDocColors.bgEdge)
            .padding(SporeDocSpacing.dialogPaddingDp),
        title = {
            Text(
                text = stringResource(R.string.viewer_delete_dialog_title),
                style = SporeDocTypography.fileName
            )
        },
        text = {
            Text(
                text = stringResource(R.string.viewer_delete_dialog_body),
                style = SporeDocTypography.fileMeta
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.viewer_delete_dialog_confirm),
                    style = SporeDocTypography.textAction
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.viewer_delete_dialog_cancel),
                    style = SporeDocTypography.textAction
                )
            }
        }
    )
}
