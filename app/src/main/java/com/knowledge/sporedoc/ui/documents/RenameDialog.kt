package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocShapes
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * The rename dialog (R10/R11/R13/R14): a text field pre-filled with [initialName] (extension
 * already stripped), zero-corner shape, gold hairline border, mono typography. Owns only the
 * field's own typed value; validity is decided by the ViewModel/repository via [error].
 */
@Composable
fun RenameDialog(
    initialName: String,
    error: RenameDialogError?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(initialName) { mutableStateOf(initialName) }

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
                text = stringResource(R.string.rename_dialog_title),
                style = SporeDocTypography.fileName
            )
        },
        text = {
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = SporeDocTypography.fileMeta
                )
                val errorMessage = when (error) {
                    RenameDialogError.InvalidName -> stringResource(R.string.rename_dialog_error_invalid_name)
                    RenameDialogError.NameCollision -> stringResource(R.string.rename_dialog_error_name_collision)
                    null -> null
                }
                if (errorMessage != null) {
                    Text(text = errorMessage, style = SporeDocTypography.fileMeta)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text(
                    text = stringResource(R.string.rename_dialog_confirm),
                    style = SporeDocTypography.textAction
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.rename_dialog_cancel),
                    style = SporeDocTypography.textAction
                )
            }
        }
    )
}
