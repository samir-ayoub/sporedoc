package com.knowledge.sporedoc.ui.documents

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.ui.components.DeleteConfirmationDialog
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocTheme
import com.knowledge.sporedoc.ui.theme.SporeDocTypography
import com.knowledge.sporedoc.ui.theme.effects.BackgroundScreen
import com.knowledge.sporedoc.ui.theme.effects.EclipseOrb
import com.knowledge.sporedoc.ui.theme.effects.Grain
import com.knowledge.sporedoc.ui.theme.effects.ScreenBackground
import com.knowledge.sporedoc.ui.theme.effects.Vignette

/**
 * Stateless rendering of the DocumentsScreen layer stack (`screens.DocumentsScreen.layers`):
 * background, orb, content (header + list), bottom dock, vignette, grain, in that order.
 */
@Composable
fun DocumentsContent(
    state: DocumentsUiState,
    onCapture: () -> Unit,
    loadThumbnail: suspend (DocumentId, PixelSize) -> PageImage?,
    onImportRequested: () -> Unit = {},
    onOpenDocument: (DocumentId, String) -> Unit = { _, _ -> },
    onShareRequested: (DocumentId) -> Unit = {},
    onRenameRequested: (DocumentId, String) -> Unit = { _, _ -> },
    onRenameConfirmed: (String) -> Unit = {},
    onRenameCancelled: () -> Unit = {},
    onDeleteRequested: (DocumentId) -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
    onDeleteCancelled: () -> Unit = {},
    onSelectModeEntered: () -> Unit = {},
    onSelectionCleared: () -> Unit = {},
    onSelectAllToggled: () -> Unit = {},
    onRowLongPressed: (DocumentId) -> Unit = {},
    onRowToggled: (DocumentId) -> Unit = {},
    onShareSelectedRequested: () -> Unit = {},
    onMergeRequested: () -> Unit = {},
    onPurgeRequested: () -> Unit = {},
    onPurgeConfirmed: () -> Unit = {},
    onPurgeCancelled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isHeaderCollapsed by remember {
        derivedStateOf {
            isDocumentsHeaderCollapsed(
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ScreenBackground(screen = BackgroundScreen.Documents)
        EclipseOrb()
        // The header overlays the list so its size never changes the list viewport
        // (resizing it in a Column made the scroll position clamp back to 0 and flicker).
        DocumentsBody(
            state = state,
            loadThumbnail = loadThumbnail,
            onOpenDocument = onOpenDocument,
            onShareRequested = onShareRequested,
            onRenameRequested = onRenameRequested,
            onDeleteRequested = onDeleteRequested,
            onSelectModeEntered = onSelectModeEntered,
            onSelectionCleared = onSelectionCleared,
            onSelectAllToggled = onSelectAllToggled,
            onRowLongPressed = onRowLongPressed,
            onRowToggled = onRowToggled,
            listState = listState,
            modifier = Modifier.fillMaxSize()
        )
        DocumentsHeader(
            isCollapsed = isHeaderCollapsed,
            modifier = Modifier.align(Alignment.TopStart)
        )
        val selectionMode = state.selectionMode
        if (selectionMode != null) {
            DocumentsContextualBar(
                summary = selectionMode.summary,
                onShareRequested = onShareSelectedRequested,
                onMergeRequested = onMergeRequested,
                onPurgeRequested = onPurgeRequested,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            BottomDock(
                onImportRequested = onImportRequested,
                onCapture = onCapture,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        Vignette()
        Grain()
    }

    val renameDialog = state.renameDialog
    if (renameDialog != null) {
        RenameDialog(
            initialName = renameDialog.initialName,
            error = renameDialog.error,
            onConfirm = onRenameConfirmed,
            onDismiss = onRenameCancelled
        )
    }

    if (state.pendingDeleteId != null) {
        DeleteConfirmationDialog(onConfirm = onDeleteConfirmed, onDismiss = onDeleteCancelled)
    }

    if (state.isPurgeConfirmationVisible) {
        DeleteConfirmationDialog(
            documentCount = state.selectionMode?.selectedIds?.size ?: 0,
            onConfirm = onPurgeConfirmed,
            onDismiss = onPurgeCancelled
        )
    }
}

/**
 * Pure scroll-threshold decision (SD-16): the header is collapsed whenever the list
 * is not exactly at the top. No other state (timers, etc.) feeds this decision.
 */
fun isDocumentsHeaderCollapsed(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int
): Boolean {
    return !(firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0)
}

private const val HEADER_ANIMATION_MS = 350

@Composable
private fun DocumentsHeader(isCollapsed: Boolean, modifier: Modifier = Modifier) {
    val height by animateDpAsState(
        targetValue = if (isCollapsed) SporeDocSpacing.compactHeaderHeightDp else SporeDocSpacing.headerHeightDp,
        animationSpec = tween(durationMillis = HEADER_ANIMATION_MS),
        label = "headerHeight"
    )
    val paddingBottom by animateDpAsState(
        targetValue = if (isCollapsed) SporeDocSpacing.compactHeaderPaddingBottomDp else SporeDocSpacing.headerPaddingBottomDp,
        animationSpec = tween(durationMillis = HEADER_ANIMATION_MS),
        label = "headerPaddingBottom"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0.6f to SporeDocColors.bgEdge.copy(alpha = 0.94f),
                    1f to SporeDocColors.bgEdge.copy(alpha = 0f)
                )
            )
            .height(height)
            .padding(
                start = SporeDocSpacing.screenGutterDp,
                end = SporeDocSpacing.screenGutterDp,
                bottom = paddingBottom
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Crossfade(targetState = isCollapsed, animationSpec = tween(durationMillis = HEADER_ANIMATION_MS)) { collapsed ->
            if (collapsed) {
                Text(text = stringResource(R.string.documents_wordmark), style = SporeDocTypography.wordmarkCompact)
            } else {
                Column {
                    Text(text = stringResource(R.string.documents_wordmark), style = SporeDocTypography.wordmark)
                    Text(
                        text = stringResource(R.string.documents_subtitle),
                        style = SporeDocTypography.subtitle,
                        modifier = Modifier.padding(top = SporeDocSpacing.subtitleMarginTopDp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentsBody(
    state: DocumentsUiState,
    loadThumbnail: suspend (DocumentId, PixelSize) -> PageImage?,
    onOpenDocument: (DocumentId, String) -> Unit,
    onShareRequested: (DocumentId) -> Unit,
    onRenameRequested: (DocumentId, String) -> Unit,
    onDeleteRequested: (DocumentId) -> Unit,
    onSelectModeEntered: () -> Unit,
    onSelectionCleared: () -> Unit,
    onSelectAllToggled: () -> Unit,
    onRowLongPressed: (DocumentId) -> Unit,
    onRowToggled: (DocumentId) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    if (state.sections.isEmpty()) {
        Column(modifier = modifier.padding(top = SporeDocSpacing.headerHeightDp)) {
            DocumentsSelectionBar(
                selectionMode = state.selectionMode,
                onSelectModeEntered = onSelectModeEntered,
                onSelectionCleared = onSelectionCleared,
                onSelectAllToggled = onSelectAllToggled
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.documents_empty_state), style = SporeDocTypography.fileMeta)
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = SporeDocSpacing.headerHeightDp,
            bottom = SporeDocSpacing.bodyBottomContentPaddingDp
        )
    ) {
        item(key = "selection-bar") {
            DocumentsSelectionBar(
                selectionMode = state.selectionMode,
                onSelectModeEntered = onSelectModeEntered,
                onSelectionCleared = onSelectionCleared,
                onSelectAllToggled = onSelectAllToggled
            )
        }
        state.sections.forEach { section ->
            item(key = "header-${section.label}") {
                DocumentSectionHeader(label = section.label)
            }
            items(section.rows, key = { it.id.value }) { row ->
                DocumentRow(
                    row = row,
                    loadThumbnail = loadThumbnail,
                    onOpenDocument = onOpenDocument,
                    onShareRequested = onShareRequested,
                    onRenameRequested = onRenameRequested,
                    onDeleteRequested = onDeleteRequested,
                    selectionMode = state.selectionMode,
                    onRowLongPressed = onRowLongPressed,
                    onRowToggled = onRowToggled
                )
            }
        }
    }
}

@Composable
private fun BottomDock(
    onImportRequested: () -> Unit,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to SporeDocColors.bgEdge.copy(alpha = 0f),
                    0.42f to SporeDocColors.bgEdge.copy(alpha = 0.94f)
                )
            )
            .padding(
                start = SporeDocSpacing.screenGutterDp,
                end = SporeDocSpacing.screenGutterDp,
                top = SporeDocSpacing.dockPaddingTopDp,
                bottom = SporeDocSpacing.dockPaddingBottomDp
            )
    ) {
        DocumentsFooter(onImportRequested = onImportRequested, onCapture = onCapture)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DocumentsContentPreview() {
    SporeDocTheme {
        DocumentsContent(
            state = DocumentsUiState(),
            onCapture = {},
            loadThumbnail = { _, _ -> null }
        )
    }
}
