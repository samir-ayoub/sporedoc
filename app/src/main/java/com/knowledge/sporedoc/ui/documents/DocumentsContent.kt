package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.PageImage
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
    loadThumbnail: suspend (DocumentId) -> PageImage?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        ScreenBackground(screen = BackgroundScreen.Documents)
        EclipseOrb()
        Column(modifier = Modifier.fillMaxSize()) {
            DocumentsHeader()
            DocumentsBody(
                state = state,
                loadThumbnail = loadThumbnail,
                modifier = Modifier.weight(1f)
            )
        }
        BottomDock(onCapture = onCapture, modifier = Modifier.align(Alignment.BottomCenter))
        Vignette()
        Grain()
    }
}

@Composable
private fun DocumentsHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SporeDocSpacing.headerHeightDp)
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
private fun DocumentsBody(
    state: DocumentsUiState,
    loadThumbnail: suspend (DocumentId) -> PageImage?,
    modifier: Modifier = Modifier
) {
    if (state.sections.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.documents_empty_state), style = SporeDocTypography.fileMeta)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = SporeDocSpacing.bodyBottomContentPaddingDp)
    ) {
        state.sections.forEach { section ->
            item(key = "header-${section.label}") {
                DocumentSectionHeader(label = section.label)
            }
            items(section.rows, key = { it.id.value }) { row ->
                DocumentRow(row = row, loadThumbnail = loadThumbnail)
            }
        }
    }
}

@Composable
private fun BottomDock(onCapture: () -> Unit, modifier: Modifier = Modifier) {
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
        OutlinedPrimaryButton(onClick = onCapture)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DocumentsContentPreview() {
    SporeDocTheme {
        DocumentsContent(
            state = DocumentsUiState(),
            onCapture = {},
            loadThumbnail = { null }
        )
    }
}
