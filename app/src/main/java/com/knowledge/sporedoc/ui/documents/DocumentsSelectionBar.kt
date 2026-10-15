package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * The list bar between the header and the list (R1). Normal state shows only `[ SELECT ]`;
 * selection mode shows `✕`, the `NN SELECTED` counter and `[ ALL ]` (R3).
 */
@Composable
fun DocumentsSelectionBar(
    selectionMode: SelectionModeUiState?,
    onSelectModeEntered: () -> Unit,
    onSelectionCleared: () -> Unit,
    onSelectAllToggled: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SporeDocSpacing.selectionBarPaddingHorizontalDp)
    ) {
        if (selectionMode == null) {
            SelectButton(onClick = onSelectModeEntered, modifier = Modifier.align(Alignment.CenterStart))
        } else {
            ClearSelectionButton(onClick = onSelectionCleared, modifier = Modifier.align(Alignment.CenterStart))
            SelectionCounter(counterText = selectionMode.counterText, modifier = Modifier.align(Alignment.Center))
            SelectAllButton(onClick = onSelectAllToggled, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun SelectButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.select_action_content_description)
    Box(
        modifier = modifier
            .height(SporeDocSpacing.minTouchTargetDp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = stringResource(R.string.select_action_label), style = SporeDocTypography.textAction)
    }
}

@Composable
private fun ClearSelectionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.selection_clear_content_description)
    Box(
        modifier = modifier
            .size(SporeDocSpacing.minTouchTargetDp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.selection_clear_symbol), style = SporeDocTypography.fileName)
    }
}

@Composable
private fun SelectionCounter(counterText: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.selection_counter_template, counterText),
        style = SporeDocTypography.fileMeta,
        modifier = modifier
    )
}

@Composable
private fun SelectAllButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.select_all_content_description)
    Box(
        modifier = modifier
            .height(SporeDocSpacing.minTouchTargetDp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(text = stringResource(R.string.select_all_label), style = SporeDocTypography.textAction)
    }
}
