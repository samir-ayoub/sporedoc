package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.ui.components.pixelSizeForDp
import com.knowledge.sporedoc.ui.components.toImageBitmapOrNull
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * Renders one [DocumentRowUi]: framed thumbnail, file name, metadata line, the `[ SHARE ]`
 * text action, the ⋮ menu (Rename/Share/Delete), and the trailing divider
 * (`components.DocumentRow.dividerBetweenRows`). Tapping the thumbnail or the file name
 * opens the document (R1); the ⋮ menu order and item taps are R2/R5/R7/R10.
 */
@Composable
fun DocumentRow(
    row: DocumentRowUi,
    loadThumbnail: suspend (DocumentId, PixelSize) -> PageImage?,
    onOpenDocument: (DocumentId, String) -> Unit = { _, _ -> },
    onShareRequested: (DocumentId) -> Unit = {},
    onRenameRequested: (DocumentId, String) -> Unit = { _, _ -> },
    onDeleteRequested: (DocumentId) -> Unit = {},
    selectionMode: SelectionModeUiState? = null,
    onRowLongPressed: (DocumentId) -> Unit = {},
    onRowToggled: (DocumentId) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSelected = selectionMode != null && row.id in selectionMode.selectedIds
    val rowModifier = if (selectionMode != null) {
        Modifier
            .background(if (isSelected) SporeDocColors.selectionTint else androidx.compose.ui.graphics.Color.Transparent)
            .border(
                width = if (isSelected) SporeDocStroke.hairlineDp else 0.dp,
                color = if (isSelected) SporeDocColors.goldMatte else androidx.compose.ui.graphics.Color.Transparent
            )
            .combinedClickable(onClick = { onRowToggled(row.id) })
    } else {
        Modifier.combinedClickable(
            onClick = { onOpenDocument(row.id, row.fileName) },
            onLongClick = { onRowLongPressed(row.id) }
        )
    }

    Column(modifier = modifier.fillMaxWidth().then(rowModifier)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = SporeDocSpacing.rowPaddingStartDp,
                    end = SporeDocSpacing.rowPaddingEndDp,
                    top = SporeDocSpacing.rowPaddingVerticalDp,
                    bottom = SporeDocSpacing.rowPaddingVerticalDp
                ),
            verticalAlignment = Alignment.Top
        ) {
            DocumentThumbnail(
                id = row.id,
                loadThumbnail = loadThumbnail
            )
            Column(
                modifier = Modifier
                    .padding(start = SporeDocSpacing.rowGapThumbToTextDp)
                    .weight(1f)
            ) {
                Text(
                    text = row.fileName,
                    style = SporeDocTypography.fileName,
                    maxLines = SporeDocTypography.fileNameMaxLines,
                    overflow = SporeDocTypography.fileNameOverflow
                )
                Text(
                    text = row.metaLine,
                    style = SporeDocTypography.fileMeta,
                    modifier = Modifier.padding(top = SporeDocSpacing.textColumnGapDp)
                )
                if (selectionMode == null) {
                    ShareTextAction(onClick = { onShareRequested(row.id) })
                }
            }
            if (selectionMode == null) {
                MoreOptionsButton(
                    onRename = { onRenameRequested(row.id, row.fileName) },
                    onShare = { onShareRequested(row.id) },
                    onDelete = { onDeleteRequested(row.id) }
                )
            } else {
                RowCheckbox(isSelected = isSelected)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SporeDocSpacing.rowDividerInsetDp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SporeDocStroke.hairlineDp)
                    .background(SporeDocColors.goldDividerSoft)
            )
        }
    }
}

/** The square checkbox shown in place of `[ SHARE ]`/`⋮` in selection mode (R7/R13). */
@Composable
private fun RowCheckbox(isSelected: Boolean, modifier: Modifier = Modifier) {
    val description = stringResource(
        if (isSelected) {
            R.string.row_checkbox_selected_content_description
        } else {
            R.string.row_checkbox_unselected_content_description
        }
    )
    Box(
        modifier = modifier
            .size(SporeDocSpacing.minTouchTargetDp)
            .semantics {
                contentDescription = description
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(SporeDocSpacing.rowCheckboxSizeDp)
                .background(if (isSelected) SporeDocColors.goldMatte else androidx.compose.ui.graphics.Color.Transparent)
                .border(SporeDocStroke.frameDp, SporeDocColors.goldMatte)
        )
    }
}

/** The `[ SHARE ]` text action below the metadata line (R3/R4/R5). */
@Composable
private fun ShareTextAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = SporeDocSpacing.textColumnGapDp)
            .height(SporeDocSpacing.minTouchTargetDp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = stringResource(R.string.share_text_action_label),
            style = SporeDocTypography.textAction
        )
    }
}

/** The row's ⋮ menu button, opening Rename/Share/Delete in that order (R1/R2). */
@Composable
private fun MoreOptionsButton(
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val moreOptionsDescription = stringResource(R.string.more_options_content_description)
    Box(
        modifier = modifier
            .size(SporeDocSpacing.minTouchTargetDp)
            .clickable { expanded = true }
            .semantics {
                contentDescription = moreOptionsDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "⋮", style = SporeDocTypography.fileName)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.more_options_rename)) },
                onClick = { expanded = false; onRename() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.more_options_share)) },
                onClick = { expanded = false; onShare() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.more_options_delete)) },
                onClick = { expanded = false; onDelete() }
            )
        }
    }
}

/** Lazily loads and renders the document's first-page thumbnail; nothing is shown on failure (R7). */
@Composable
private fun DocumentThumbnail(
    id: DocumentId,
    loadThumbnail: suspend (DocumentId, PixelSize) -> PageImage?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val targetSize = remember(density) {
        density.pixelSizeForDp(
            widthDp = SporeDocSpacing.thumbnailWidthDp,
            heightDp = SporeDocSpacing.thumbnailHeightDp
        )
    }
    val pageImage by produceState<PageImage?>(initialValue = null, key1 = id) {
        value = loadThumbnail(id, targetSize)
    }
    Box(
        modifier = modifier
            .border(SporeDocStroke.frameDp, SporeDocColors.goldMatte)
            .padding(SporeDocSpacing.thumbnailFramePaddingDp)
    ) {
        Box(
            modifier = Modifier
                .size(width = SporeDocSpacing.thumbnailWidthDp, height = SporeDocSpacing.thumbnailHeightDp)
                .background(SporeDocColors.paperThumb)
        ) {
            val currentImage = pageImage
            if (currentImage != null) {
                val bitmap = remember(currentImage) { currentImage.toImageBitmapOrNull() }
                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        modifier = Modifier.size(
                            width = SporeDocSpacing.thumbnailWidthDp,
                            height = SporeDocSpacing.thumbnailHeightDp
                        ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
