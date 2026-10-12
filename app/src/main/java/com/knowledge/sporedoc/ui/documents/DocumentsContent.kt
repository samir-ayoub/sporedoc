package com.knowledge.sporedoc.ui.documents

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
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
        Column(modifier = Modifier.fillMaxSize()) {
            DocumentsHeader(collapsed = isHeaderCollapsed)
            DocumentsBody(
                state = state,
                listState = listState,
                loadThumbnail = loadThumbnail,
                onOpenDocument = onOpenDocument,
                onShareRequested = onShareRequested,
                onRenameRequested = onRenameRequested,
                onDeleteRequested = onDeleteRequested,
                modifier = Modifier.weight(1f)
            )
        }
        BottomDock(
            onImportRequested = onImportRequested,
            onCapture = onCapture,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
}

@Composable
private fun DocumentsHeader(collapsed: Boolean, modifier: Modifier = Modifier) {
    val headerHeight by animateDpAsState(
        targetValue = if (collapsed) SporeDocSpacing.headerCollapsedHeightDp else SporeDocSpacing.headerHeightDp,
        label = "documentsHeaderHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        Crossfade(targetState = collapsed, label = "documentsHeaderContent") { isCollapsed ->
            if (isCollapsed) {
                CollapsedDocumentsHeader()
            } else {
                ExpandedDocumentsHeader()
            }
        }
    }
}

@Composable
private fun ExpandedDocumentsHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = SporeDocSpacing.screenGutterDp,
                end = SporeDocSpacing.screenGutterDp,
                bottom = SporeDocSpacing.headerPaddingBottomDp
            ),
        contentAlignment = Alignment.BottomStart
    ) {
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

@Composable
private fun CollapsedDocumentsHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SporeDocSpacing.screenGutterDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.splash_mark),
            contentDescription = null,
            modifier = Modifier
                .size(SporeDocSpacing.headerCollapsedIconSizeDp)
                .clearAndSetSemantics { }
        )
        Text(
            text = stringResource(R.string.documents_wordmark),
            style = SporeDocTypography.wordmark,
            modifier = Modifier.padding(start = SporeDocSpacing.headerCollapsedIconLabelGapDp)
        )
    }
}

@Composable
private fun DocumentsBody(
    state: DocumentsUiState,
    listState: LazyListState,
    loadThumbnail: suspend (DocumentId, PixelSize) -> PageImage?,
    onOpenDocument: (DocumentId, String) -> Unit,
    onShareRequested: (DocumentId) -> Unit,
    onRenameRequested: (DocumentId, String) -> Unit,
    onDeleteRequested: (DocumentId) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.sections.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.documents_empty_state), style = SporeDocTypography.fileMeta)
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = SporeDocSpacing.bodyBottomContentPaddingDp)
    ) {
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
                    onDeleteRequested = onDeleteRequested
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
